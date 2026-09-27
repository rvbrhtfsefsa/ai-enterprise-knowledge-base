<template>
  <div class="auth-page">
    <div class="auth-card">
      <section class="login-panel">
        <div class="brand-row">
          <div class="brand-logo">K</div>
          <div>
            <div class="brand-name">AI 企业知识库</div>
            <div class="brand-subtitle">智能客服管理系统</div>
          </div>
        </div>

        <div class="form-title">
          <h1>欢迎回来</h1>
          <p>RAG · Embedding · PGVector · SSE 流式问答</p>
        </div>

        <el-form ref="formRef" :model="data.form" :rules="data.rules" class="auth-form">
          <el-form-item prop="username">
            <el-input v-model="data.form.username" placeholder="账号" :prefix-icon="User"/>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
                v-model="data.form.password"
                type="password"
                placeholder="密码"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="login"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="login" :loading="data.loading">进入系统</el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <a href="/register">创建账号</a>
        </div>
      </section>

      <section class="product-panel">
        <div class="panel-top">
          <div>
            <div class="preview-label">Customer Service Preview</div>
            <h2>让客服回答基于企业资料</h2>
          </div>
          <div class="status-pill">知识库在线</div>
        </div>

        <div class="chat-preview">
          <div class="message user-message">产品支持私有化部署吗？</div>
          <div class="message ai-message">
            支持。企业版可以部署在客户自有服务器，并对接内部账号体系、文档知识库和客服工单流程。
          </div>
        </div>

        <div class="source-board">
          <div class="source-title">本次回答来源</div>
          <div class="source-card">
            <div class="source-meta">产品部署手册.pdf · 相似度 0.88</div>
            <p>企业版支持私有化部署，可根据客户网络环境配置服务地址和数据存储策略。</p>
          </div>
          <div class="source-card accent">
            <div class="source-meta">售前常见问题.docx · 相似度 0.81</div>
            <p>客户如需本地化部署，应由售前确认服务器、数据库和账号集成方式。</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const data = reactive({
  form: {},
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ]
  },
  loading: false
})

const formRef = ref()

const login = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.loading = true
      request.post('/user/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          const user = res.data
          localStorage.setItem('sys-user', JSON.stringify(user))
          setTimeout(() => {
            // 按角色分流：管理员进后台，普通用户进前台
            location.href = user.role === 'ADMIN' ? '/manager/home' : '/front/FrontHome'
          }, 500)
        } else {
          ElMessage.error(res.msg)
        }
      }).finally(() => {
        data.loading = false
      })
    }
  })
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 38px;
  background:
      radial-gradient(circle at 18% 20%, rgba(31, 122, 83, 0.16), transparent 28%),
      radial-gradient(circle at 86% 72%, rgba(234, 179, 8, 0.18), transparent 30%),
      #edf3ef;
  color: #1f2937;
}

.auth-card {
  width: min(1120px, 100%);
  min-height: 660px;
  display: grid;
  grid-template-columns: 410px 1fr;
  overflow: hidden;
  border: 1px solid #d7e2d8;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 32px 90px rgba(15, 23, 42, 0.16);
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 50px;
  background: #ffffff;
  border-right: 1px solid #dfe7de;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 62px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #1f7a53;
  color: #fff;
  font-weight: 800;
  box-shadow: 0 10px 20px rgba(31, 122, 83, 0.22);
}

.brand-name {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 3px;
  color: #6b7280;
  font-size: 13px;
}

.form-title {
  margin-bottom: 28px;
}

.form-title h1 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
  line-height: 1.2;
  letter-spacing: 0;
}

.form-title p {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-input__wrapper) {
  height: 42px;
  border-radius: 6px;
  background: #f8fafc;
  box-shadow: 0 0 0 1px #d8e0d7 inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #1f7a53 inset;
}

.submit-btn {
  width: 100%;
  height: 42px;
  border: 0;
  border-radius: 6px;
  background: #1f7a53 !important;
  font-weight: 700;
}

.submit-btn:hover {
  background: #17643f !important;
}

.auth-footer {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  color: #6b7280;
  font-size: 14px;
}

.auth-footer a {
  color: #1f7a53;
  font-weight: 700;
  text-decoration: none;
}

.product-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 58px;
  background:
      linear-gradient(145deg, rgba(9, 49, 38, 0.96), rgba(24, 82, 63, 0.94)),
      #0f2f26;
  color: #fff;
}

.panel-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  max-width: 760px;
  margin-bottom: 28px;
}

.preview-label {
  color: rgba(255, 255, 255, 0.62);
  font-size: 13px;
}

.panel-top h2 {
  margin: 8px 0 0;
  color: #fff;
  font-size: 34px;
  line-height: 1.25;
  letter-spacing: 0;
}

.status-pill {
  flex: 0 0 auto;
  padding: 8px 12px;
  border-radius: 999px;
  background: #ecfdf3;
  color: #166534;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid #bbf7d0;
}

.chat-preview {
  max-width: 760px;
  min-height: 230px;
  padding: 26px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.10);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.18);
}

.message {
  max-width: 78%;
  margin-bottom: 18px;
  padding: 13px 15px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.user-message {
  margin-left: auto;
  background: #1f7a53;
  color: #fff;
}

.ai-message {
  background: rgba(255, 255, 255, 0.92);
  color: #1f2937;
}

.source-board {
  max-width: 760px;
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.source-title {
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  font-weight: 700;
}

.source-card {
  padding: 15px 16px;
  border-left: 4px solid #1f7a53;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(31, 41, 55, 0.06);
}

.source-card.accent {
  border-left-color: #d97706;
}

.source-meta {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.source-card p {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
  line-height: 1.65;
}

@media (max-width: 900px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .login-panel {
    border-right: 0;
    border-bottom: 1px solid #dfe7de;
  }

  .product-panel {
    padding: 36px;
  }
}
</style>
