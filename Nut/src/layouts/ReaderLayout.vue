<template>
  <div class="reader-layout">
    <header class="nav">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-mark">书</span>
          <span class="logo-text">小说书城</span>
          <span class="logo-slogan">匠心打磨好作品</span>
        </div>

        <nav class="menu">
          <router-link to="/" :class="{ active: route.path === '/' }">首页</router-link>
          <router-link to="/book" :class="{ active: route.path.startsWith('/book') && route.name !== 'bookDetail' }">书城</router-link>
          <a class="nav-link" :class="{ active: route.path === '/' }" @click="goRank">排行榜</a>
          <router-link v-if="userStore.isLogin" to="/bookshelf" :class="{ active: route.path === '/bookshelf' }">书架</router-link>
          <router-link v-if="userStore.isLogin" to="/notifications" class="msg-link" :class="{ active: route.path === '/notifications' }">
            消息
            <el-badge :value="appStore.unreadCount" :max="99" :hidden="appStore.unreadCount === 0" class="msg-badge" />
          </router-link>
        </nav>

        <div class="nav-right">
          <div class="search">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="请输入书名/作者/主角"
              clearable
              @keyup.enter="doSearch"
              @clear="doSearch"
            />
            <button class="search-btn" @click="doSearch">搜索</button>
          </div>

          <template v-if="userStore.isLogin">
            <span class="balance" title="点击充值" @click="appStore.openRecharge()">
              <el-icon><Wallet /></el-icon>
              ¥{{ fenToYuan(userStore.balance) }}
            </span>
            <el-dropdown trigger="click" @command="onCommand">
              <span class="user-trigger">
                <el-avatar :size="30" :src="userStore.user?.avatar || defaultAvatar">
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
            <button class="login-btn" @click="$router.push('/login')">立即登录</button>
            <button class="register-btn" @click="$router.push('/register')">免费注册</button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <span class="footer-logo">小说书城</span>
        <span class="footer-text">在线小说书城 · 毕业设计演示系统 · 前端 Vue3 + Element Plus</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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

// 排行榜入口：回到首页并滚动到排行榜区域
function goRank() {
  if (route.path === '/') {
    document.getElementById('rank')?.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push('/').then(() => {
      nextTick(() => {
        setTimeout(() => document.getElementById('rank')?.scrollIntoView({ behavior: 'smooth' }), 120)
      })
    })
  }
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

/* ===== 顶部导航（七猫风格） ===== */
.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: var(--nut-header-height);
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 0 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: var(--nut-gradient);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(255, 78, 69, 0.35);
}
.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #ff4e45;
  letter-spacing: 1px;
}
.logo-slogan {
  font-size: 12px;
  color: #b8b8be;
  border-left: 1px solid #e8e8ec;
  padding-left: 10px;
  white-space: nowrap;
}
.menu {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}
.menu a,
.menu .nav-link {
  font-size: 15px;
  color: #444;
  padding: 6px 2px;
  position: relative;
  transition: color 0.2s;
  cursor: pointer;
  user-select: none;
}
.menu a:hover,
.menu .nav-link:hover {
  color: var(--el-color-primary);
}
.menu a.active,
.menu .nav-link.active {
  color: var(--el-color-primary);
  font-weight: 600;
}
.menu a.active::after,
.menu .nav-link.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -4px;
  height: 3px;
  border-radius: 2px;
  background: var(--nut-gradient);
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
  gap: 14px;
  flex-shrink: 0;
}

/* 搜索框 */
.search {
  display: flex;
  align-items: center;
  border: 2px solid var(--el-color-primary);
  border-radius: 22px;
  overflow: hidden;
  background: #fff;
  height: 38px;
}
.search-input {
  width: 220px;
}
.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding: 0 14px;
}
.search-input :deep(.el-input__inner) {
  height: 34px;
  font-size: 13px;
}
.search-btn {
  border: none;
  background: var(--nut-gradient);
  color: #fff;
  font-size: 14px;
  height: 100%;
  padding: 0 22px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.search-btn:hover {
  opacity: 0.9;
}

/* 登录 / 注册按钮 */
.login-btn {
  border: 1px solid var(--el-color-primary);
  background: #fff;
  color: var(--el-color-primary);
  border-radius: 18px;
  padding: 6px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.login-btn:hover {
  background: var(--el-color-primary-light-9);
}
.register-btn {
  border: none;
  background: var(--nut-gradient);
  color: #fff;
  border-radius: 18px;
  padding: 7px 20px;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(255, 78, 69, 0.28);
  transition: all 0.2s;
}
.register-btn:hover {
  box-shadow: 0 6px 14px rgba(255, 78, 69, 0.36);
  transform: translateY(-1px);
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
  max-width: 80px;
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

/* ===== 页脚（七猫风格） ===== */
.footer {
  background: #fff;
  border-top: 1px solid #f0f0f2;
  padding: 22px 0;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #a8abb2;
  font-size: 13px;
}
.footer-logo {
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 14px;
}
</style>
