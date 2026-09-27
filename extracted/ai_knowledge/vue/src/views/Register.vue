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
          <h1>创建账号</h1>
          <p>RAG · Embedding · PGVector · 企业知识库问答</p>
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
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
                v-model="data.form.confirmPassword"
                type="password"
                placeholder="确认密码"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="register"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="register" :loading="data.loading">创建账号</el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <span>已有账号？</span>
          <a href="/login">返回登录</a>
        </div>
      </section>

      <section class="product-panel">
        <div class="panel-top">
          <div>
            <div class="preview-label">Knowledge Pipeline</div>
            <h2>把企业资料变成可问答的客服知识</h2>
          </div>
          <div class="status-pill">新账号</div>
        </div>

        <div class="pipeline-card">
          <div class="pipeline-step">
            <span>01</span>
            <div>
              <h3>上传企业文档</h3>
              <p>管理员维护产品手册、制度文档和售后资料。</p>
            </div>
          </div>
          <div class="pipeline-step">
            <span>02</span>
            <div>
              <h3>解析并向量化</h3>
              <p>系统将文档切分为知识片段，为检索增强生成做准备。</p>
            </div>
          </div>
          <div class="pipeline-step">
            <span>03</span>
            <div>
              <h3>智能客服回答</h3>
              <p>用户提问后，AI 根据企业资料生成答案并展示来源。</p>
            </div>
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

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else {
    if (value !== data.form.password) {
      callback(new Error("确认密码跟原密码不一致!"))
    }
    callback()
  }
}

const data = reactive({
  form: { },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    confirmPassword: [
      { validator: validatePass, trigger: 'blur' }
    ]
  },
  loading: false
})

const formRef = ref()

const register = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.loading = true
      request.post('/user/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功')
          setTimeout(() => {
            location.href = '/login'
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
  margin-bottom: 56px;
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
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid #fed7aa;
}

.pipeline-card {
  max-width: 720px;
  display: grid;
  gap: 14px;
  padding: 26px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.10);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.18);
}

.pipeline-step {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 14px;
  padding: 16px;
  border-radius: 6px;
  background: #f8fafc;
}

.pipeline-step span {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #1f7a53;
  color: #fff;
  font-weight: 800;
}

.pipeline-step h3 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.pipeline-step p {
  margin: 7px 0 0;
  color: #64748b;
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
