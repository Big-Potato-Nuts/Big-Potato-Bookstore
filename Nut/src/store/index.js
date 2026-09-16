import { createPinia } from 'pinia'

// 独立的 pinia 实例：供 request 拦截器 / 路由守卫在组件外使用
const pinia = createPinia()

export default pinia
