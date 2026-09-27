<template>
  <div class="chat-page">
    <div class="chat-toolbar card">
      <el-select v-model="data.chatModelConfigId" clearable style="width: 260px" placeholder="请选择聊天模型">
        <el-option v-for="item in data.chatModelList" :key="item.id" :label="item.name + '（' + item.modelName + '）'" :value="item.id"></el-option>
      </el-select>
      <el-select v-model="data.embeddingModelConfigId" clearable style="width: 260px" placeholder="请选择向量模型">
        <el-option v-for="item in data.embeddingModelList" :key="item.id" :label="item.name + '（' + item.modelName + '）'" :value="item.id"></el-option>
      </el-select>
      <div class="toolbar-field">
        <span>TopK</span>
        <el-input-number v-model="data.topK" :min="1" :max="10" controls-position="right" style="width: 110px" />
      </div>
      <div class="toolbar-field threshold-field">
        <span>相似度</span>
        <el-slider v-model="data.similarityThreshold" :min="0" :max="1" :step="0.05" style="width: 160px" />
        <span class="threshold-value">{{ data.similarityThreshold.toFixed(2) }}</span>
      </div>
    </div>

    <div class="chat-main">
      <section class="chat-window card">
        <div class="message-list" ref="messageListRef">
          <div v-for="(item, index) in data.messages" :key="index" :class="['message-row', item.role]">
            <div class="message-bubble">
              <div class="message-role">{{ item.role === 'user' ? '管理员' : '智能客服' }}</div>
              <div class="message-content" v-if="item.role === 'user'">{{ item.content }}</div>
              <div class="message-content markdown-body" v-else v-html="renderMarkdown(item.content)"></div>
            </div>
          </div>
        </div>
        <div class="question-box">
          <el-input
              v-model="data.question"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="请输入要测试的企业知识库问题，例如：产品支持私有化部署吗？"
              @keyup.ctrl.enter="ask"
          ></el-input>
          <el-button type="primary" :loading="data.loading" @click="ask">发送问题</el-button>
        </div>
      </section>

      <aside class="source-panel card">
        <div class="source-header">
          <div>
            <h3>资料来源</h3>
            <p>展示本次回答命中的知识片段</p>
          </div>
          <el-tag type="success" v-if="data.sources.length">{{ data.sources.length }} 条</el-tag>
          <el-tag type="info" v-else>暂无</el-tag>
        </div>
        <div class="source-list" v-if="data.sources.length">
          <div class="source-card" v-for="item in data.sources" :key="item.segmentId">
            <div class="source-meta">
              <span>片段 #{{ item.segmentId }}</span>
              <el-tag size="small" type="warning">相似度 {{ formatSimilarity(item.similarity) }}</el-tag>
            </div>
            <p>{{ item.content }}</p>
          </div>
        </div>
        <el-empty v-else description="发送问题后显示资料来源" />
      </aside>
    </div>
  </div>
</template>

<script setup>
import { nextTick, reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL
const user = JSON.parse(localStorage.getItem('sys-user') || '{}')

const messageListRef = ref()

const data = reactive({
  chatModelList: [],
  embeddingModelList: [],
  chatModelConfigId: null,
  embeddingModelConfigId: null,
  topK: 5,
  similarityThreshold: 0.2,
  question: '',
  loading: false,
  messages: [
    {
      role: 'assistant',
      content: '你好，我是企业知识库智能客服。你可以输入一个问题，我会基于已向量化的企业资料回答，并在右侧展示资料来源。'
    }
  ],
  sources: []
})

const loadModelList = () => {
  request.get('/aiModelConfig/selectAll', {
    params: {
      enabled: '是'
    }
  }).then(res => {
    if (res.code === '200') {
      const list = res.data || []
      data.chatModelList = list.filter(item => item.modelType === '聊天模型')
      data.embeddingModelList = list.filter(item => item.modelType === '向量模型')
      if (!data.chatModelConfigId && data.chatModelList.length) {
        data.chatModelConfigId = data.chatModelList[0].id
      }
      if (!data.embeddingModelConfigId && data.embeddingModelList.length) {
        data.embeddingModelConfigId = data.embeddingModelList[0].id
      }
    }
  })
}

const ask = () => {
  if (!data.chatModelConfigId) {
    ElMessage.warning('请选择聊天模型')
    return
  }
  if (!data.embeddingModelConfigId) {
    ElMessage.warning('请选择向量模型')
    return
  }
  if (!data.question.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  const question = data.question.trim()
  data.messages.push({
    role: 'user',
    content: question
  })
  data.question = ''
  data.loading = true
  scrollToBottom()

  // 清空上一次来源，并先放一条空的客服消息，流式内容会不断追加到它上面
  data.sources = []
  data.messages.push({ role: 'assistant', content: '' })
  const answer = data.messages[data.messages.length - 1]

  streamAsk('/knowledgeChat/askStream', {
    question,
    chatModelConfigId: data.chatModelConfigId,
    embeddingModelConfigId: data.embeddingModelConfigId,
    topK: data.topK,
    similarityThreshold: data.similarityThreshold
  }, answer).finally(() => {
    data.loading = false
  })
}

// 通用 SSE 流式请求：sources 事件填充右侧来源，message 事件把增量追加到 answer.content
const streamAsk = async (url, payload, answer) => {
  try {
    const response = await fetch(baseUrl + url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=utf-8',
        token: user.token || ''
      },
      body: JSON.stringify(payload)
    })
    if (!response.ok || !response.body) {
      ElMessage.error('AI 流式生成失败')
      return
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''
      parts.forEach(part => handleSsePart(part, answer))
    }
    if (buffer) {
      handleSsePart(buffer, answer)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('AI 流式生成失败')
  }
}

const handleSsePart = (part, answer) => {
  const lines = part.split('\n')
  let eventName = 'message'
  const dataLines = []
  lines.forEach(line => {
    if (line.startsWith('event:')) {
      eventName = line.substring(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.substring(5))
    }
  })
  const eventData = dataLines.join('\n')
  if (eventName === 'sources') {
    // sources 事件携带命中的知识片段 JSON，渲染到右侧来源区
    try {
      data.sources = JSON.parse(eventData) || []
    } catch (e) {
      data.sources = []
    }
    return
  }
  if (eventName === 'message') {
    // message 是模型增量文本，直接追加
    answer.content += eventData
    scrollToBottom()
    return
  }
  if (eventName === 'error') {
    ElMessage.error(eventData || 'AI 流式生成失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatSimilarity = (value) => {
  return Number(value || 0).toFixed(2)
}

const escapeHtml = (value = '') => {
  return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
}

const renderInline = (text) => {
  return text
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\*([^*]+)\*/g, '<em>$1</em>')
}

// 轻量 Markdown 渲染（先转义再替换，防 XSS）：支持标题、有序/无序列表、加粗、行内代码和段落
const renderMarkdown = (value = '') => {
  const source = escapeHtml(value).replace(/\r\n/g, '\n').trim()
  if (!source) {
    return ''
  }
  const html = []
  let listType = null
  const closeList = () => {
    if (listType) {
      html.push('</' + listType + '>')
      listType = null
    }
  }
  source.split('\n').forEach(rawLine => {
    const line = rawLine.trimEnd()
    if (!line.trim()) {
      closeList()
      return
    }
    const heading = line.match(/^(#{1,6})\s+(.*)$/)
    if (heading) {
      closeList()
      const level = heading[1].length
      html.push('<h' + level + '>' + renderInline(heading[2]) + '</h' + level + '>')
      return
    }
    const ordered = line.match(/^\s*\d+\.\s+(.*)$/)
    if (ordered) {
      if (listType !== 'ol') {
        closeList()
        html.push('<ol>')
        listType = 'ol'
      }
      html.push('<li>' + renderInline(ordered[1]) + '</li>')
      return
    }
    const unordered = line.match(/^\s*[-*]\s+(.*)$/)
    if (unordered) {
      if (listType !== 'ul') {
        closeList()
        html.push('<ul>')
        listType = 'ul'
      }
      html.push('<li>' + renderInline(unordered[1]) + '</li>')
      return
    }
    closeList()
    html.push('<p>' + renderInline(line) + '</p>')
  })
  closeList()
  return html.join('')
}

loadModelList()
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 92px);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 62px;
}

.toolbar-field {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.threshold-field {
  flex: 1;
  max-width: 260px;
}

.threshold-value {
  width: 42px;
  color: #409eff;
}

.chat-main {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 12px;
}

.chat-window,
.source-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #edf0f5;
  border-radius: 6px;
}

.message-row {
  display: flex;
  margin-bottom: 14px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 76%;
  padding: 12px 14px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e8ecf2;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
}

.message-row.user .message-bubble {
  color: #ffffff;
  background: #2563eb;
  border-color: #2563eb;
}

.message-role {
  margin-bottom: 6px;
  font-size: 12px;
  opacity: 0.72;
}

.message-content {
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown 渲染区：关掉 pre-wrap，交给标签本身排版 */
.message-content.markdown-body {
  white-space: normal;
}

.message-content.markdown-body :deep(p) {
  margin: 4px 0;
}

.message-content.markdown-body :deep(p:first-child) {
  margin-top: 0;
}

.message-content.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content.markdown-body :deep(h1),
.message-content.markdown-body :deep(h2),
.message-content.markdown-body :deep(h3),
.message-content.markdown-body :deep(h4),
.message-content.markdown-body :deep(h5),
.message-content.markdown-body :deep(h6) {
  margin: 8px 0 4px;
  font-size: 15px;
  font-weight: 600;
}

.message-content.markdown-body :deep(ul),
.message-content.markdown-body :deep(ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.message-content.markdown-body :deep(li) {
  margin: 2px 0;
}

.message-content.markdown-body :deep(strong) {
  font-weight: 600;
}

.message-content.markdown-body :deep(code) {
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.06);
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.question-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 110px;
  gap: 10px;
  margin-top: 12px;
}

.source-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf0f5;
}

.source-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.source-header p {
  margin: 0;
  color: #8a95a5;
  font-size: 13px;
}

.source-list {
  flex: 1;
  overflow-y: auto;
  padding-top: 12px;
}

.source-card {
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #fbfdff;
}

.source-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-weight: 600;
}

.source-card p {
  margin: 0;
  color: #4b5563;
  line-height: 1.65;
  word-break: break-word;
}
</style>
