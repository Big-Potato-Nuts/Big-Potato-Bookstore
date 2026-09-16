import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import pinia from '@/store'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

// 请求拦截：统一携带 Authorization token
request.interceptors.request.use((config) => {
  const userStore = useUserStore(pinia)
  if (userStore.token && !config.skipAuth) {
    config.headers.Authorization = userStore.token
  }
  return config
})

// 响应拦截：以业务 code 为准（HTTP 固定 200）
// 约定：resolve 时直接返回 data；失败时 reject 原始响应体，调用方可取 err.code / err.message
request.interceptors.response.use(
  (response) => {
    const res = response.data
    const config = response.config
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      // 未登录 / 登录过期：清 token 跳登录
      const userStore = useUserStore(pinia)
      userStore.logout()
      ElMessage.error(res.message || '登录已过期，请重新登录')
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        router.push({ path: '/login', query: { redirect: current.fullPath } })
      }
      return Promise.reject(res)
    }
    if (res.code === 3001) {
      // 余额不足：打开全局充值弹窗
      const appStore = useAppStore(pinia)
      appStore.openRecharge()
      if (!config.skipErrorMessage) {
        ElMessage.warning(res.message || '余额不足，请先充值')
      }
      return Promise.reject(res)
    }
    if (!config.skipErrorMessage) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(res)
  },
  (error) => {
    const config = error.config || {}
    if (!config.skipErrorMessage) {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
