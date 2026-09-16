<template>
  <div class="reader-page" :class="'theme-' + theme">
    <header class="reader-top">
      <el-button text class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>&nbsp;返回
      </el-button>
      <div class="reader-title" :title="chapter ? chapter.bookTitle + ' · ' + chapter.title : ''">
        {{ chapter ? chapter.bookTitle + ' · ' + chapter.title : '阅读' }}
      </div>
      <div class="reader-tools">
        <el-dropdown trigger="click" @command="theme = $event">
          <el-button text>
            <el-icon><Brush /></el-icon>&nbsp;主题
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="light">白昼</el-dropdown-item>
              <el-dropdown-item command="sepia">护眼</el-dropdown-item>
              <el-dropdown-item command="dark">夜间</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="reader-body">
      <!-- 付费拦截 -->
      <div v-if="purchaseNeeded" class="purchase-panel card">
        <h2 class="purchase-title">付费章节</h2>
        <p class="purchase-desc">
          本章为付费章节，价格 <b>¥{{ fenToYuan(price) }}</b>，购买后永久可读。
        </p>
        <div class="purchase-actions">
          <el-button type="primary" :loading="buying" @click="buyChapter">购买本章</el-button>
          <el-button :loading="buying" @click="buyBook">购买整本</el-button>
          <el-button text type="primary" @click="goRecharge">余额不足？去充值</el-button>
        </div>
        <p v-if="!price" class="muted tip">价格信息加载中，请稍候或返回书籍详情页购买。</p>
      </div>

      <!-- 正文 -->
      <div v-else-if="chapter" class="reader-content">
        <h1 class="content-title">{{ chapter.title }}</h1>
        <p v-for="(p, i) in paragraphs" :key="i" class="paragraph">{{ p }}</p>
        <div class="reader-nav">
          <el-button v-if="chapter.prevChapterId" @click="loadChapter(chapter.prevChapterId)">上一章</el-button>
          <el-button v-else disabled>已是第一章</el-button>
          <el-button v-if="chapter.nextChapterId" type="primary" @click="loadChapter(chapter.nextChapterId)">下一章</el-button>
          <el-button v-else disabled>已是最后一章</el-button>
        </div>
      </div>

      <div v-else class="reader-loading">
        <el-skeleton :rows="12" animated />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { chapterContent, chapters } from '@/api/book'
import { updateProgress } from '@/api/bookshelf'
import { createOrder, payOrder } from '@/api/order'
import { fenToYuan } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const chapter = ref(null)
const paragraphs = computed(() => (chapter.value?.content || '').split(/\r?\n+/).filter((s) => s.trim()))
const loading = ref(true)
const purchaseNeeded = ref(false)
const buying = ref(false)
const price = ref(0)
const theme = ref('light')

let bookId = route.query.bookId ? Number(route.query.bookId) : null
let chapterMetaMap = {}
let scrollTimer = null

// 当前章节 id（路由参数，切换章节时同步更新）
const currentId = ref(Number(route.params.chapterId))

async function loadChapter(id) {
  // 同步地址栏，保证刷新 / 分享后仍定位到当前章节
  if (Number(route.params.chapterId) !== Number(id)) {
    router.replace({ path: `/read/${id}`, query: bookId ? { bookId } : {} })
  }
  currentId.value = Number(id)
  loading.value = true
  purchaseNeeded.value = false
  chapter.value = null
  try {
    const data = await chapterContent(id)
    chapter.value = data
    bookId = data.bookId
    // 拉取目录元信息（价格 / 标题），供付费拦截与下一章跳转使用
    fetchMeta(bookId)
    reportProgress()
    window.scrollTo(0, 0)
  } catch (e) {
    if (e && e.code === 403) {
      // 付费章节未购买：尝试通过目录元信息获取价格
      purchaseNeeded.value = true
      if (bookId) {
        fetchMeta(bookId)
      }
    }
    // 401 由全局拦截器跳登录
  } finally {
    loading.value = false
  }
}

async function fetchMeta(bid) {
  try {
    const list = (await chapters(bid)) || []
    chapterMetaMap = {}
    list.forEach((c) => {
      chapterMetaMap[c.id] = c
    })
    const current = currentId.value
    if (chapterMetaMap[current]) {
      price.value = Number(chapterMetaMap[current].price) || 0
      if (!bookId) bookId = Number(chapterMetaMap[current].bookId)
    }
  } catch (e) { /* 忽略 */ }
}

function computePosition() {
  const doc = document.documentElement
  const max = doc.scrollHeight - window.innerHeight
  if (max <= 0) return 0
  return Math.min(100, Math.max(0, Math.round((window.scrollY / max) * 100)))
}

function reportProgress() {
  if (!userStore.isLogin || !chapter.value || !bookId) return
  updateProgress({ bookId, chapterId: chapter.value.id, position: computePosition() }).catch(() => {})
}

function onScroll() {
  clearTimeout(scrollTimer)
  scrollTimer = setTimeout(reportProgress, 800)
}

// 购买流程
async function doPay(order) {
  try {
    await ElMessageBox.confirm(
      `订单金额 ¥${fenToYuan(order.amount)}（${order.typeText || ''}），确认支付？`,
      '确认支付',
      { type: 'info', confirmButtonText: '确认支付', cancelButtonText: '稍后支付' }
    )
  } catch (e) {
    ElMessage.info('订单已生成，可在“我的订单”中继续支付')
    return false
  }
  try {
    await payOrder(order.orderNo)
    ElMessage.success('支付成功，开始阅读吧！')
    userStore.refreshInfo()
    return true
  } catch (e) {
    if (e && e.code === 3001) {
      // 全局充值弹窗已打开
    }
    return false
  }
}

async function buyChapter() {
  if (!bookId) {
    ElMessage.warning('书籍信息缺失，请返回详情页购买')
    return
  }
  buying.value = true
  try {
    const order = await createOrder({ bookId, type: 1, chapterId: currentId.value })
    const ok = await doPay(order)
    if (ok) loadChapter(currentId.value)
  } catch (e) {
    if (e && e.code === 3002) {
      // 已购买：直接刷新正文
      loadChapter(currentId.value)
    }
  } finally {
    buying.value = false
  }
}

async function buyBook() {
  if (!bookId) {
    ElMessage.warning('书籍信息缺失，请返回详情页购买')
    return
  }
  buying.value = true
  try {
    const order = await createOrder({ bookId, type: 2 })
    const ok = await doPay(order)
    if (ok) loadChapter(currentId.value)
  } catch (e) {
    if (e && e.code === 3002) {
      loadChapter(currentId.value)
    }
  } finally {
    buying.value = false
  }
}

function goRecharge() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  appStore.openRecharge()
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push(bookId ? `/book/${bookId}` : '/')
}

onMounted(() => {
  loadChapter(currentId.value)
  window.addEventListener('scroll', onScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  clearTimeout(scrollTimer)
  // 退出时上报最终进度
  reportProgress()
})
</script>

<style scoped>
.reader-page {
  min-height: 100vh;
  background: #fff;
  color: #333;
  transition: background 0.3s, color 0.3s;
}
.reader-top {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: inherit;
}
.reader-title {
  font-size: 14px;
  max-width: 60%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.reader-body {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 20px 60px;
}
.content-title {
  text-align: center;
  font-size: 22px;
  margin: 10px 0 28px;
}
.paragraph {
  font-size: 17px;
  line-height: 1.95;
  text-indent: 2em;
  margin: 0 0 18px;
  word-break: break-word;
}
.reader-nav {
  display: flex;
  justify-content: center;
  gap: 18px;
  margin-top: 40px;
}
.reader-loading {
  padding-top: 20px;
}
.purchase-panel {
  max-width: 460px;
  margin: 60px auto;
  text-align: center;
  padding: 36px 28px;
}
.purchase-title {
  font-size: 20px;
}
.purchase-desc {
  color: #666;
  line-height: 1.8;
  margin-bottom: 22px;
}
.purchase-desc b {
  color: var(--el-color-primary);
  font-size: 18px;
}
.purchase-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
.tip {
  font-size: 12px;
  margin-top: 14px;
}

/* 主题：白昼 */
.theme-light {
  background: #fff;
  color: #333;
}
.theme-light .paragraph {
  color: #333;
}

/* 主题：护眼 */
.theme-sepia {
  background: #f7f1e3;
  color: #5b4636;
}
.theme-sepia .paragraph {
  color: #5b4636;
}

/* 主题：夜间 */
.theme-dark {
  background: #1e1e1e;
  color: #cfcfcf;
}
.theme-dark .reader-top {
  border-bottom-color: rgba(255, 255, 255, 0.1);
}
.theme-dark :deep(.el-button) {
  color: #cfcfcf;
}
.theme-dark .paragraph {
  color: #cfcfcf;
}
.theme-dark .content-title {
  color: #eaeaea;
}
</style>
