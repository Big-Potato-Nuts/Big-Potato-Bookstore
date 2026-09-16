<template>
  <div class="admin-comments">
    <div class="filter-bar card">
      <el-input v-model="query.keyword" placeholder="评论内容关键词" clearable style="width: 220px" @keyup.enter="reload" @clear="reload">
        <template #append>
          <el-button :icon="Search" @click="reload" />
        </template>
      </el-input>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="reload">
        <el-option label="正常" :value="1" />
        <el-option label="已屏蔽" :value="0" />
      </el-select>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="list" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="nickname" label="用户" min-width="110" />
        <el-table-column label="书籍" min-width="140">
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="warning" plain @click="toggleStatus(row, 0)">屏蔽</el-button>
            <el-button v-else size="small" type="success" plain @click="toggleStatus(row, 1)">恢复</el-button>
            <el-button size="small" type="danger" text @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无评论" :image-size="80" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { commentPage, updateCommentStatus, deleteComment } from '@/api/admin'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '', status: null })

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== null && query.status !== '') params.status = query.status
    const res = await commentPage(params)
    list.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  load()
}

function onPage(p) {
  query.page = p
  load()
}

async function toggleStatus(row, status) {
  const action = status === 0 ? '屏蔽' : '恢复'
  try {
    await ElMessageBox.confirm(`确定${action}该评论吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await updateCommentStatus(row.id, status)
    ElMessage.success(`已${action}`)
    load()
  } catch (e) { /* 全局提示 */ }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？删除后不可恢复。', '危险操作', {
      type: 'error',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }
  try {
    await deleteComment(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
}
</style>
