import { defineStore } from 'pinia'

// 全局应用状态：充值弹窗、未读消息数
export const useAppStore = defineStore('app', {
  state: () => ({
    rechargeVisible: false,
    unreadCount: 0
  }),
  actions: {
    openRecharge() {
      this.rechargeVisible = true
    },
    closeRecharge() {
      this.rechargeVisible = false
    },
    setUnread(n) {
      this.unreadCount = n
    }
  }
})
