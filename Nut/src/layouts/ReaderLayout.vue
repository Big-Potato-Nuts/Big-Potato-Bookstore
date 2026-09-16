<template>
  <div class="reader-layout">
    <header class="nav">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="24"><Reading /></el-icon>
          <span class="logo-text">小说书城</span>
        </div>
        <nav class="menu">
          <router-link to="/" :class="{ active: route.path === '/' }">首页</router-link>
          <router-link to="/book" :class="{ active: route.path.startsWith('/book') && route.name !== 'bookDetail' }">书城</router-link>
          <router-link v-if="userStore.isLogin" to="/bookshelf" :class="{ active: route.path === '/bookshelf' }">书架</router-link>
          <router-link v-if="userStore.isLogin" to="/notifications" class="msg-link" :class="{ active: route.path === '/notifications' }">
            消息
            <el-badge :value="appStore.unreadCount" :max="99" :hidden="appStore.unreadCount === 0" class="msg-badge" />
          </router-link>
        </nav>
        <div class="nav-right">
          <el-input
            v-model="keyword"
            class="search"
            placeholder="搜索书名 / 作者"
            clearable
            @keyup.enter="doSearch"
            @clear="doSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
          <template v-if="userStore.isLogin">
            <span class="balance" title="点击充值" @click="appStore.openRecharge()">
              <el-icon><Wallet /></el-icon>
              ¥{{ fenToYuan(userStore.balance) }}
            </span>
            <el-dropdown trigger="click" @command="onCommand">
              <span class="user-trigger">
                <el-avatar :size="32" :src="userStore.user?.avatar || defaultAvatar">
                  {{ (userStore.user?.nickname || userStore.user?.username || '?').charAt(0) }}
                </el-avatar>
                <span class="nickname">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                <el-tag v-if="userStore.isAuthor" size="small" type="warning">作者</el-tag>
                <el-tag v-if="userStore.isAdmin" size="small" type="danger">管理员</el-tag>
                <el-icon class="arrow"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="comments">我的评论</el-dropdown-item>
                  <el-dropdown-item command="notifications">消息通知</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAuthor" command="author" divided>作者中心</el-dropdown-item>
                  <el-dropdown-item v-else command="apply" divided>申请成为作者</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">在线小说书城 · 毕业设计演示系统 · 前端 Vue3 + Element Plus</footer>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { logout } from '@/api/auth'
import { unreadCount } from '@/api/notification'
import { fenToYuan } from '@/utils/format'
import defaultAvatar from '@/assets/default-avatar.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const keyword = ref('')

function doSearch() {
  router.push({ path: '/book', query: keyword.value ? { keyword: keyword.value } : {} })
}

async function loadUnread() {
  if (!userStore.isLogin) {
    appStore.setUnread(0)
    return
  }
  try {
    const res = await unreadCount()
    appStore.setUnread(res.unreadCount || 0)
  } catch (e) {
    /* 忽略 */
  }
}

function onCommand(cmd) {
  const map = {
    profile: '/profile',
    orders: '/orders',
    comments: '/my-comments',
    notifications: '/notifications',
    apply: '/author-apply',
    author: '/author/dashboard',
    admin: '/admin/users'
  }
  if (map[cmd]) {
    router.push(map[cmd])
    return
  }
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await logout()
        } catch (e) {
          /* token 已失效也照常清理 */
        }
        userStore.logout()
        appStore.setUnread(0)
        ElMessage.success('已退出登录')
        router.push('/')
      })
      .catch(() => {})
  }
}

watch(
  () => userStore.isLogin,
  (v) => {
    if (v) loadUnread()
    else appStore.setUnread(0)
  }
)

// 路由变化后刷新未读数（例如从通知页返回）
watch(
  () => route.path,
  () => loadUnread()
)

onMounted(loadUnread)
</script>

<style scoped>
.reader-layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: var(--nut-header-height);
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 0 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 20px;
}
.menu {
  display: flex;
  align-items: center;
  gap: 22px;
  flex: 1;
}
.menu a {
  font-size: 15px;
  color: #555;
  padding: 6px 2px;
  position: relative;
  transition: color 0.2s;
}
.menu a:hover {
  color: var(--el-color-primary);
}
.menu a.active {
  color: var(--el-color-primary);
  font-weight: 600;
}
.menu a.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  border-radius: 2px;
  background: var(--el-color-primary);
}
.msg-link {
  display: inline-flex;
  align-items: center;
}
.msg-badge {
  margin-left: 4px;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search {
  width: 220px;
}
.balance {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-weight: 600;
  font-size: 14px;
}
.balance:hover {
  opacity: 0.85;
}
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.nickname {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow {
  color: #999;
  font-size: 12px;
}
.main {
  flex: 1;
}
.footer {
  text-align: center;
  color: #a8abb2;
  font-size: 13px;
  padding: 24px 0;
}
</style>
