<template>
  <div class="book-detail page-container">
    <!-- 顶部信息 -->
    <div class="card detail-card">
      <img :src="book.cover || defaultCover" class="detail-cover" alt="封面" />
      <div class="detail-info">
        <h1 class="detail-title">{{ book.title || '加载中...' }}</h1>
        <div class="tags">
          <el-tag v-if="book.statusText" :type="bookStatusTagMap[book.status] || 'info'">{{ book.statusText }}</el-tag>
          <el-tag v-if="book.categoryName" effect="plain">{{ book.categoryName }}</el-tag>
          <el-tag v-if="book.isRecommend === 1" type="danger" effect="plain">编辑推荐</el-tag>
        </div>
        <p class="detail-author">作者：{{ book.authorName || '佚名' }}</p>
        <p class="detail-intro">{{ book.intro || '暂无简介' }}</p>
        <div class="stats muted">
          <span>{{ formatWords(book.clickCount) }} 阅读</span>
          <span>{{ formatWords(book.collectCount) }} 收藏</span>
          <span>{{ book.chapterCount ?? 0 }} 章</span>
          <span v-if="book.latestChapterTitle">最新：{{ book.latestChapterTitle }}</span>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" @click="startRead">
            <el-icon><Reading /></el-icon>&nbsp;{{ continueChapterId ? '继续阅读' : '立即阅读' }}
          </el-button>
          <el-button size="large" :type="book.collected ? 'info' : 'warning'" :loading="collectLoading" @click="toggleCollect">
            <el-icon><StarFilled v-if="book.collected" /><Star v-else /></el-icon>&nbsp;{{ book.collected ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            v-if="paidChapters.length && !book.purchased"
            size="large"
            type="danger"
            plain
            @click="buy(2)"
          >
            整本购买 ¥{{ fenToYuan(wholePrice) }}
          </el-button>
          <el-tag v-if="book.purchased" type="success" size="large" effect="dark">已购整本</el-tag>
        </div>
      </div>
    </div>

    <!-- 章节目录 -->
    <div class="section-title">章节目录（共 {{ chapterList.length }} 章）</div>
    <div class="card chapter-card">
      <div v-for="ch in chapterList" :key="ch.id" class="chapter-item" @click="readChapter(ch)">
        <span class="chapter-title">{{ ch.title }}</span>
        <span class="chapter-tags">
          <el-tag v-if="ch.isFree === 1" size="small" type="success">免费</el-tag>
          <el-tag v-else size="small" type="danger">¥{{ fenToYuan(ch.price) }}</el-tag>
        </span>
        <span class="chapter-meta muted">{{ formatWords(ch.words) }} 字 · {{ formatWords(ch.readCount) }} 阅读</span>
      </div>
      <el-empty v-if="!chapterList.length" description="暂无章节" :image-size="80" />
    </div>

    <!-- 评论 -->
    <div class="section-title">评论（{{ commentTotal }}）</div>
    <div class="card comment-card">
      <div class="comment-input">
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="写下你的看法吧……（登录后可评论）"
          :disabled="!userStore.isLogin"
        />
        <div class="comment-input-actions">
          <span v-if="!userStore.isLogin" class="muted" @click="$router.push({ path: '/login', query: { redirect: route.fullPath } })" style="cursor: pointer">
            登录后参与评论
          </span>
          <el-button type="primary" :loading="commenting" @click="postComment">发表评论</el-button>
        </div>
      </div>
      <div v-if="comments.length" class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="36" :src="c.avatar || defaultAvatar">{{ (c.nickname || '?').charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-head">
              <span class="comment-nick">{{ c.nickname || '匿名' }}</span>
              <span class="muted">{{ formatTime(c.createTime) }}</span>
            </div>
            <p class="comment-content">{{ c.content }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无评论，快来抢沙发" :image-size="80" />
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="commentTotal"
          :page-size="commentQuery.size"
          :current-page="commentQuery.page"
          @current-change="loadComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { bookDetail, chapters } from '@/api/book'
import { bookComments, addComment } from '@/api/comment'
import { listBookshelf, addBookshelf, setCollect } from '@/api/bookshelf'
import { createOrder, payOrder } from '@/api/order'
import { fenToYuan, formatTime, formatWords, bookStatusTagMap } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'
import defaultAvatar from '@/assets/default-avatar.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const bookId = Number(route.params.id)
const book = ref({})
const chapterList = ref([])
const continueChapterId = ref(null)

const comments = ref([])
const commentTotal = ref(0)
const commentText = ref('')
const commenting = ref(false)
const commentQuery = reactive({ page: 1, size: 5 })

const collectLoading = ref(false)

const paidChapters = computed(() => chapterList.value.filter((c) => c.isFree === 0))
const wholePrice = computed(() => paidChapters.value.reduce((s, c) => s + (Number(c.price) || 0), 0))

async function loadDetail() {
  try {
    book.value = await bookDetail(bookId)
  } catch (e) {
    // 404 等错误由全局提示，并返回上一页
    if (e && e.code === 404) {
      setTimeout(() => router.back(), 800)
    }
  }
}

async function loadChapters() {
  try {
    chapterList.value = (await chapters(bookId)) || []
  } catch (e) { /* 忽略 */ }
}

async function loadComments(p) {
  commentQuery.page = p || commentQuery.page
  try {
    const res = await bookComments(bookId, { page: commentQuery.page, size: commentQuery.size })
    comments.value = res?.records || []
    commentTotal.value = res?.total || 0
  } catch (e) { /* 忽略 */ }
}

// 登录后读取书架，定位“继续阅读”
async function loadShelf() {
  if (!userStore.isLogin) return
  try {
    const list = await listBookshelf()
    const item = (list || []).find((i) => Number(i.bookId) === bookId)
    if (item?.readChapterId) continueChapterId.value = item.readChapterId
  } catch (e) { /* 忽略 */ }
}

function startRead() {
  if (continueChapterId.value) {
    router.push({ path: `/read/${continueChapterId.value}`, query: { bookId } })
    return
  }
  const first = chapterList.value[0]
  if (first) router.push({ path: `/read/${first.id}`, query: { bookId } })
}

function readChapter(ch) {
  router.push({ path: `/read/${ch.id}`, query: { bookId, price: ch.price } })
}

async function toggleCollect() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  collectLoading.value = true
  try {
    if (!book.value.collected) {
      await addBookshelf({ bookId, isCollect: 1 })
      ElMessage.success('已加入书架并收藏')
    } else {
      await setCollect({ bookId, isCollect: 0 })
      ElMessage.success('已取消收藏')
    }
    book.value.collected = !book.value.collected
    book.value.collectCount = Math.max(0, (book.value.collectCount || 0) + (book.value.collected ? 1 : -1))
  } catch (e) { /* 全局提示 */ } finally {
    collectLoading.value = false
  }
}

async function postComment() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const content = commentText.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    await addComment(bookId, content)
    ElMessage.success('评论发表成功')
    commentText.value = ''
    loadComments(1)
  } catch (e) { /* 全局提示 */ } finally {
    commenting.value = false
  }
}

// 购买：type=1 单章 / type=2 整本
async function buy(type, chapterId) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const order = await createOrder({ bookId, type, chapterId })
    await confirmPay(order)
  } catch (e) {
    if (e && e.code === 3002) {
      // 已购买，无需重复购买：刷新状态
      loadDetail()
      loadChapters()
    }
    // 3001 / 400 由全局处理
  }
}

async function confirmPay(order) {
  try {
    await ElMessageBox.confirm(
      `订单金额 ¥${fenToYuan(order.amount)}（${order.typeText || ''}）\n确认支付将从余额中扣除相应金额。`,
      '确认支付',
      { type: 'info', confirmButtonText: '确认支付', cancelButtonText: '稍后支付' }
    )
  } catch (e) {
    ElMessage.info('订单已生成，可在“我的订单”中继续支付')
    return
  }
  try {
    await payOrder(order.orderNo)
    ElMessage.success('支付成功，开始阅读吧！')
    userStore.refreshInfo() // 同步余额
    loadDetail()
    loadChapters()
  } catch (e) {
    if (e && e.code === 3001) {
      // 全局充值弹窗已打开
    }
  }
}

onMounted(() => {
  loadDetail()
  loadChapters()
  loadComments(1)
  loadShelf()
})
</script>

<style scoped>
.detail-card {
  display: flex;
  gap: 28px;
  padding: 30px;
  background: linear-gradient(180deg, #fff 0%, #fffdfd 100%);
}
.detail-cover {
  width: 200px;
  height: 266px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
  flex-shrink: 0;
  transition: transform 0.2s;
}
.detail-cover:hover {
  transform: translateY(-3px);
}
.detail-info {
  flex: 1;
  min-width: 0;
}
.detail-title {
  font-size: 26px;
  margin: 0 0 12px;
}
.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.detail-author {
  color: #666;
  margin: 0 0 10px;
}
.detail-intro {
  line-height: 1.8;
  color: #444;
  margin: 0 0 14px;
  background: var(--el-color-primary-light-9);
  border-left: 4px solid var(--el-color-primary);
  border-radius: 8px;
  padding: 12px 14px;
}
.stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  margin-bottom: 18px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.chapter-card {
  padding: 8px 20px;
}
.chapter-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 13px 6px;
  border-bottom: 1px solid #f2f2f4;
  cursor: pointer;
  transition: background 0.15s;
}
.chapter-item:last-child {
  border-bottom: none;
}
.chapter-item:hover {
  background: #fafafa;
}
.chapter-title {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
.chapter-tags {
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}
.chapter-meta {
  font-size: 12px;
  flex-shrink: 0;
}
.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  margin-top: 10px;
}
.comment-list {
  margin-top: 18px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f2f2f4;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}
.comment-nick {
  font-weight: 600;
}
.comment-content {
  margin: 0;
  line-height: 1.7;
}
</style>
