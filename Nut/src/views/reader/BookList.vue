<template>
  <div class="book-list page-container">
    <div class="card filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索书名或作者"
        clearable
        style="width: 260px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #append>
          <el-button :icon="Search" @click="search" />
        </template>
      </el-input>
      <el-select
        v-model="query.categoryId"
        placeholder="全部分类"
        clearable
        style="width: 160px"
        @change="search"
      >
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
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
import { Search } from '@element-plus/icons-vue'
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
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.result-count {
  font-size: 13px;
}
</style>
