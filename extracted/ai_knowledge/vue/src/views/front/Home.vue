<template>
  <div class="front-home">
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-eyebrow">RAG · PGVector · 企业知识库问答</div>
        <h1>让企业资料变成可问答的智能客服</h1>
        <p>把产品手册、售后流程、公司制度沉淀为可检索、可追溯、可持续维护的企业知识库。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="openChat">咨询 AI 客服</el-button>
          <el-button size="large" @click="scrollToFeatures">查看能力</el-button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="panel-header">
          <span>智能客服回答预览</span>
          <el-tag type="success">资料可追溯</el-tag>
        </div>
        <div class="preview-question">产品支持私有化部署吗？</div>
        <div class="preview-answer">
          支持。企业版可以部署在客户自有服务器，并根据网络环境配置服务地址、数据库和存储策略。
        </div>
      </div>
    </section>

    <section class="feature-section" ref="featureRef">
      <div class="section-title">
        <h2>企业知识服务能力</h2>
        <p>覆盖资料管理、语义检索、智能回答和在线咨询。</p>
      </div>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-index">01</div>
          <h3>产品文档问答</h3>
          <p>用户可以直接询问部署方式、功能说明、版本差异等产品问题。</p>
        </div>
        <div class="feature-card">
          <div class="feature-index">02</div>
          <h3>售后流程咨询</h3>
          <p>退款、发票、工单、响应时效等客服流程可以被统一检索和回答。</p>
        </div>
        <div class="feature-card">
          <div class="feature-index">03</div>
          <h3>公司制度助手</h3>
          <p>报销、考勤、权限申请等内部制度资料可以变成员工自助问答入口。</p>
        </div>
        <div class="feature-card">
          <div class="feature-index">04</div>
          <h3>稳定客服体验</h3>
          <p>用户只需要输入问题，系统会基于企业资料生成清晰、准确、适合客服场景的回答。</p>
        </div>
      </div>
    </section>

    <button class="chat-float" @click="openChat">
      <span>AI</span>
      <em>智能客服</em>
    </button>

    <div class="chat-drawer" v-if="data.chatVisible">
      <div class="chat-card">
        <div class="chat-header">
          <div>
            <h3>AI 智能客服</h3>
            <p>基于企业知识库回答问题</p>
          </div>
          <el-button text @click="data.chatVisible = false">关闭</el-button>
        </div>
        <div class="chat-messages" ref="messageRef">
          <div v-for="(item, index) in data.messages" :key="index" :class="['chat-message', item.role]">
            <div class="bubble" v-if="item.role === 'user'">{{ item.content }}</div>
            <div class="bubble markdown-body" v-else v-html="renderMarkdown(item.content)"></div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
              v-model="data.question"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="请输入你的问题"
              @keyup.ctrl.enter="ask"
          ></el-input>
          <el-button type="primary" :loading="data.loading" @click="ask">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL
const user = JSON.parse(localStorage.getItem('sys-user') || '{}')

const featureRef = ref()
const messageRef = ref()

const data = reactive({
  chatVisible: false,
  question: '',
  loading: false,
  messages: [
    {
      role: 'assistant',
      content: '你好，我是企业智能客服。你可以咨询产品部署、售后流程或公司制度相关问题。'
    }
  ]
})

const openChat = () => {
  data.chatVisible = true
  scrollMessage()
}

const scrollToFeatures = () => {
  featureRef.value?.scrollIntoView({ behavior: 'smooth' })
}

const ask = () => {
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
  scrollMessage()

  // 先放一条空的客服消息，流式内容会不断追加到它上面
  data.messages.push({ role: 'assistant', content: '' })
  const answer = data.messages[data.messages.length - 1]

  streamAsk('/knowledgeChat/frontAskStream', { question }, answer).finally(() => {
    data.loading = false
  })
}

// SSE 流式请求：message 事件把增量追加到 answer.content（用户端不展示资料来源，忽略 sources 事件）
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
  if (eventName === 'message') {
    answer.content += eventData
    scrollMessage()
    return
  }
  if (eventName === 'error') {
    ElMessage.error(eventData || 'AI 流式生成失败')
  }
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

const scrollMessage = () => {
  nextTick(() => {
    if (messageRef.value) {
      messageRef.value.scrollTop = messageRef.value.scrollHeight
    }
  })
}

</script>

<style scoped>
.front-home {
  min-height: calc(100vh - 70px);
  background: #f5f7fb;
}

.hero-section {
  min-height: 560px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  align-items: center;
  gap: 48px;
  padding: 68px 8vw;
  background: linear-gradient(135deg, #eef6ff 0%, #f7fafc 48%, #eefdf5 100%);
}

.hero-eyebrow {
  margin-bottom: 18px;
  color: #0f766e;
  font-weight: 700;
}

.hero-content h1 {
  max-width: 720px;
  margin: 0;
  color: #102033;
  font-size: 52px;
  line-height: 1.12;
}

.hero-content p {
  max-width: 620px;
  margin: 22px 0 0;
  color: #5f6b7a;
  font-size: 18px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 32px;
}

.hero-panel {
  padding: 26px;
  border-radius: 8px;
  background: #0b2a25;
  color: #ffffff;
  box-shadow: 0 24px 70px rgba(13, 42, 37, 0.22);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  color: #bbf7d0;
}

.preview-question {
  margin-left: auto;
  max-width: 82%;
  padding: 14px 16px;
  border-radius: 8px;
  background: #16a34a;
}

.preview-answer {
  max-width: 88%;
  margin-top: 18px;
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  line-height: 1.7;
}

.preview-source {
  margin-top: 18px;
  padding: 12px 14px;
  border-left: 3px solid #22c55e;
  background: rgba(255, 255, 255, 0.08);
  color: #d1fae5;
}

.feature-section {
  padding: 64px 8vw 90px;
}

.section-title {
  margin-bottom: 28px;
}

.section-title h2 {
  margin: 0 0 10px;
  color: #102033;
  font-size: 32px;
}

.section-title p {
  margin: 0;
  color: #697586;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  min-height: 188px;
  padding: 22px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e7edf5;
}

.feature-index {
  color: #2563eb;
  font-weight: 800;
}

.feature-card h3 {
  margin: 20px 0 10px;
  color: #1f2937;
}

.feature-card p {
  margin: 0;
  color: #667085;
  line-height: 1.7;
}

.chat-float {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border: none;
  border-radius: 999px;
  background: #16a34a;
  color: #ffffff;
  box-shadow: 0 12px 32px rgba(22, 163, 74, 0.35);
  cursor: pointer;
}

.chat-float span {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  font-weight: 800;
}

.chat-float em {
  font-style: normal;
  font-weight: 700;
}

.chat-drawer {
  position: fixed;
  right: 28px;
  bottom: 92px;
  z-index: 21;
  width: 420px;
}

.chat-card {
  display: flex;
  flex-direction: column;
  height: 620px;
  padding: 16px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 22px 70px rgba(15, 23, 42, 0.2);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf0f5;
}

.chat-header h3 {
  margin: 0 0 4px;
  color: #172033;
}

.chat-header p {
  margin: 0;
  color: #8a95a5;
  font-size: 13px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px 0;
}

.chat-message {
  display: flex;
  margin-bottom: 12px;
}

.chat-message.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 82%;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  line-height: 1.65;
  white-space: pre-wrap;
}

.chat-message.user .bubble {
  background: #2563eb;
  color: #ffffff;
}

/* Markdown 渲染的客服气泡：关掉 pre-wrap，交给标签排版 */
.bubble.markdown-body {
  white-space: normal;
}

.bubble.markdown-body :deep(p) {
  margin: 4px 0;
}

.bubble.markdown-body :deep(p:first-child) {
  margin-top: 0;
}

.bubble.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.bubble.markdown-body :deep(h1),
.bubble.markdown-body :deep(h2),
.bubble.markdown-body :deep(h3),
.bubble.markdown-body :deep(h4),
.bubble.markdown-body :deep(h5),
.bubble.markdown-body :deep(h6) {
  margin: 8px 0 4px;
  font-size: 15px;
  font-weight: 600;
}

.bubble.markdown-body :deep(ul),
.bubble.markdown-body :deep(ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.bubble.markdown-body :deep(li) {
  margin: 2px 0;
}

.bubble.markdown-body :deep(strong) {
  font-weight: 600;
}

.bubble.markdown-body :deep(code) {
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.06);
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 76px;
  gap: 8px;
}

@media (max-width: 980px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chat-drawer {
    left: 16px;
    right: 16px;
    width: auto;
  }
}
</style>
