<template>
  <el-container class="work-layout">
    <el-aside width="220px" class="aside">
      <div class="aside-logo" @click="$router.push('/admin/users')">
        <el-icon :size="22"><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu :default-active="route.path" router class="aside-menu">
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/appeals">
          <el-icon><Bell /></el-icon>
          <span>用户申诉</span>
        </el-menu-item>
        <el-menu-item index="/admin/authors">
          <el-icon><Stamp /></el-icon>
          <span>作者审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Notebook /></el-icon>
          <span>小说管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/book-audit">
          <el-icon><DocumentChecked /></el-icon>
          <span>书籍审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Collection /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-bottom">
        <el-button text @click="onCommand('logout')"><el-icon><SwitchButton /></el-icon>退出登录</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">{{ route.meta.title || '管理后台' }}</div>
        <div class="topbar-right">
          <el-dropdown trigger="click" @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="userStore.user?.avatar || defaultAvatar">
                {{ (userStore.user?.nickname || userStore.user?.username || '?').charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <el-tag size="small" type="danger">管理员</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import defaultAvatar from '@/assets/default-avatar.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function onCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await logout()
        } catch (e) { /* 忽略 */ }
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
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
