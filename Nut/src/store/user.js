import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'nut_token'
const USER_KEY = 'nut_user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    role: (s) => (s.user ? s.user.role : -1),
    roleText: (s) => (s.user ? s.user.roleText : ''),
    isAdmin: (s) => s.user?.role === 2,
    isAuthor: (s) => s.user?.role === 1,
    balance: (s) => s.user?.balance ?? 0
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(data.user))
    },
    setUser(user) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    // 重新拉取当前用户信息（如入驻审核通过后角色升级）
    async refreshInfo() {
      if (!this.token) return
      try {
        const user = await authApi.getInfo()
        this.setUser({ ...(this.user || {}), ...user })
      } catch (e) {
        /* 401 由全局拦截器处理 */
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
