<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-logo" @click="$router.push('/')">
        <el-icon :size="26"><Reading /></el-icon>
        <span>小说书城</span>
      </div>
      <h2 class="auth-title">欢迎回来</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <div class="auth-links">
        还没有账号？<router-link to="/register" class="link">立即注册</router-link>
      </div>
      <el-divider><span class="muted divider-text">测试账号（密码均为 123456）</span></el-divider>
      <div class="test-accounts">
        <el-tag
          v-for="acc in testAccounts"
          :key="acc.username"
          class="test-tag"
          effect="plain"
          @click="fill(acc)"
        >
          {{ acc.label }}：{{ acc.username }}
        </el-tag>
      </div>
      <p class="muted tip">点击测试账号自动填充，再点击“登录”即可</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const testAccounts = [
  { label: '读者', username: 'reader' },
  { label: '作者', username: 'author01' },
  { label: '管理员', username: 'admin' }
]

function fill(acc) {
  form.username = acc.username
  form.password = '123456'
  ElMessage.info(`已填入 ${acc.username}，点击登录`)
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    userStore.setLogin(data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (redirect && String(redirect).startsWith('/')) {
      router.push(String(redirect))
    } else if (data.user?.role === 2) {
      router.push('/admin/users')
    } else if (data.user?.role === 1) {
      router.push('/author/dashboard')
    } else {
      router.push('/')
    }
  } catch (e) {
    /* 全局提示 */
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
.test-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.test-tag {
  cursor: pointer;
}
.divider-text {
  font-size: 12px;
}
.tip {
  text-align: center;
  font-size: 12px;
  margin-top: 10px;
}
</style>
