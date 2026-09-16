<template>
  <div class="book-list page-container">
    <!-- 页头 Banner -->
    <div class="list-banner">
      <h2 class="list-banner-title">书城</h2>
      <p class="list-banner-sub">海量小说 · 免费试读 · 匠心打磨好作品</p>
    </div>

    <!-- 筛选区 -->
    <div class="card filter-bar">
      <div class="search">
        <el-input
          v-model="query.keyword"
          placeholder="请输入书名/作者/主角"
          clearable
          class="search-input"
          @keyup.enter="search"
          @clear="search"
        />
        <button class="search-btn" @click="search">搜索</button>
      </div>
      <div class="cat-chips">
        <span
          class="cat-chip"
          :class="{ on: !query.categoryId }"
          @click="pickCategory(null)"
        >全部</span>
        <span
          v-for="c in categories"
          :key="c.id"
          class="cat-chip"
          :class="{ on: query.categoryId === c.id }"
          @click="pickCategory(c.id)"
        >{{ c.name }}</span>
      </div>
      <span v-if="total !== null" class="muted result-count">共 {{ total }} 本小说</span>
    </div>

    <div v-if="books.length" class="book-grid" style="margin-top: 18px">
      <BookCard v-for="b in books" :key="b.id" :book="b" />
    </div>
    <el-empty v-else-if="!loading" description="没有找到相关小说，换个关键词试试吧" :image-size="100" />

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BookCard from '@/components/BookCard.vue'
import { pageBooks, categoryList } from '@/api/book'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const books = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  page: 1,
  size: 12,
  categoryId: null,
  keyword: ''
})

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.keyword) params.keyword = query.keyword
    const res = await pageBooks(params)
    books.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    books.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function pickCategory(id) {
  query.categoryId = id
  search()
}

function search() {
  query.page = 1
  syncQuery()
  load()
}

function onPage(p) {
  query.page = p
  syncQuery()
  load()
}

// 与 URL query 同步，便于分享/刷新
function syncQuery() {
  const q = {}
  if (query.keyword) q.keyword = query.keyword
  if (query.categoryId) q.categoryId = query.categoryId
  if (query.page > 1) q.page = query.page
  router.replace({ path: '/book', query: q })
}

watch(
  () => route.query,
  (q) => {
    const qk = String(q.keyword || '')
    const qc = q.categoryId ? Number(q.categoryId) : null
    const qp = q.page ? Number(q.page) : 1
    if (qk !== query.keyword || qc !== query.categoryId || qp !== query.page) {
      query.keyword = qk
      query.categoryId = qc
      query.page = qp
      load()
    }
  },
  { immediate: true }
)

onMounted(async () => {
  try {
    categories.value = (await categoryList()) || []
  } catch (e) { /* 忽略 */ }
  // 直接访问 /book（无 URL 参数）时 watch 不会触发，这里补一次加载
  load()
})
</script>

<style scoped>
/* 页头 Banner */
.list-banner {
  background: var(--nut-gradient);
  border-radius: 14px;
  padding: 30px 36px;
  color: #fff;
  margin-bottom: 18px;
  box-shadow: 0 8px 24px rgba(255, 78, 69, 0.22);
}
.list-banner-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 6px;
}
.list-banner-sub {
  margin: 0;
  opacity: 0.9;
  font-size: 13px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
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
  width: 300px;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
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
  padding: 0 20px;
  cursor: pointer;
}
.search-btn:hover {
  opacity: 0.9;
}

/* 分类标签 */
.cat-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.cat-chip {
  background: #f7f7f9;
  border: 1px solid #f0f0f3;
  color: #555;
  border-radius: 15px;
  padding: 5px 15px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.cat-chip:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.cat-chip.on {
  background: var(--nut-gradient);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 3px 8px rgba(255, 78, 69, 0.3);
}
.result-count {
  font-size: 13px;
  margin-left: auto;
}
</style>
