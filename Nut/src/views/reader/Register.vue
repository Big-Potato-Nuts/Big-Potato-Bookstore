<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-logo" @click="$router.push('/')">
        <el-icon :size="26"><Reading /></el-icon>
        <span>小说书城</span>
      </div>
      <h2 class="auth-title">创建账号</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-20 位字符" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="昵称（可选）" prop="nickname">
          <el-input v-model="form.nickname" placeholder="默认与用户名相同" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <div class="auth-links">
        已有账号？<router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => (value === form.password ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur'
    }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const data = await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined
    })
    userStore.setLogin(data)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } catch (e) {
    /* 全局提示（如用户名已存在） */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2d2a54 0%, #4a3f9e 60%, #7c5cf0 100%);
  padding: 20px;
}
.auth-card {
  width: 400px;
  padding: 36px 32px;
}
.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--el-color-primary);
  font-size: 22px;
  font-weight: 700;
  cursor: pointer;
  margin-bottom: 8px;
}
.auth-title {
  text-align: center;
  margin: 0 0 24px;
  font-size: 20px;
}
.submit-btn {
  width: 100%;
  margin-top: 4px;
}
.auth-links {
  text-align: center;
  margin-top: 14px;
  font-size: 13px;
  color: #666;
}
.link {
  color: var(--el-color-primary);
}
</style>
