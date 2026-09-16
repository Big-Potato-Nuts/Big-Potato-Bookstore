<template>
  <el-container class="work-layout">
    <el-aside width="220px" class="aside">
      <div class="aside-logo" @click="$router.push('/author/dashboard')">
        <el-icon :size="22"><EditPen /></el-icon>
        <span>作者中心</span>
      </div>
      <el-menu :default-active="route.path" router class="aside-menu">
        <el-menu-item index="/author/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/author/books">
          <el-icon><Notebook /></el-icon>
          <span>作品管理</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-bottom">
        <el-button text @click="$router.push('/')"><el-icon><Back /></el-icon>返回前台</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">{{ route.meta.title || '作者中心' }}</div>
        <div class="topbar-right">
          <span class="balance" @click="appStore.openRecharge()">
            <el-icon><Wallet /></el-icon>
            ¥{{ fenToYuan(userStore.balance) }}
          </span>
          <el-dropdown trigger="click" @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="userStore.user?.avatar || defaultAvatar">
                {{ (userStore.user?.nickname || userStore.user?.username || '?').charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname || userStore.user?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { logout } from '@/api/auth'
import { fenToYuan } from '@/utils/format'
import defaultAvatar from '@/assets/default-avatar.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 进入作者中心时刷新用户信息（如审核通过后角色升级）
onMounted(() => userStore.refreshInfo())

function onCommand(cmd) {
  if (cmd === 'home') return router.push('/')
  if (cmd === 'profile') return router.push('/profile')
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await logout()
        } catch (e) { /* 忽略 */ }
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.work-layout {
  height: 100%;
}
.aside {
  background: #1f2733;
  display: flex;
  flex-direction: column;
}
.aside-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.aside-menu {
  border-right: none;
  background: transparent;
  flex: 1;
}
.aside-menu :deep(.el-menu-item) {
  color: #b8c2cf;
}
.aside-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.aside-menu :deep(.el-menu-item.is-active) {
  background: var(--el-color-primary);
  color: #fff;
}
.aside-bottom {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.aside-bottom :deep(.el-button) {
  color: #b8c2cf;
}
.topbar {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}
.topbar-title {
  font-size: 16px;
  font-weight: 600;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.balance {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-weight: 600;
}
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.nickname {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.content {
  background: #f5f6f8;
}
</style>
