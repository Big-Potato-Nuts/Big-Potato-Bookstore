<template>
  <div class="my-comments page-container">
    <div class="section-title">我的评论</div>
    <div class="card">
      <el-table v-loading="loading" :data="comments" stripe style="width: 100%">
        <el-table-column label="书籍" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/book/${row.bookId}`)">{{ row.bookTitle }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有发表过评论" :image-size="80" />
        </template>
      </el-table>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { myComments } from '@/api/comment'
import { formatTime } from '@/utils/format'

const comments = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })

async function load() {
  loading.value = true
  try {
    const res = await myComments({ page: query.page, size: query.size })
    comments.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

function onPage(p) {
  query.page = p
  load()
}

onMounted(load)
</script>
