<template>
  <div class="author-chapters">
    <div class="toolbar">
      <div class="section-title no-margin">
        <el-button text @click="$router.push('/author/books')">
          <el-icon><ArrowLeft /></el-icon>&nbsp;返回
        </el-button>
        {{ bookTitle }} · 章节管理
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push(`/author/books/${bookId}/chapters/new`)">新建章节</el-button>
    </div>

    <el-table v-loading="loading" :data="chapters" stripe style="width: 100%">
      <el-table-column prop="sort" label="序号" width="80" />
      <el-table-column prop="title" label="章节标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="属性" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.isFree === 1" type="success" size="small">免费</el-tag>
          <el-tag v-else type="danger" size="small">付费 ¥{{ fenToYuan(row.price) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="字数" width="90">
        <template #default="{ row }">{{ formatWords(row.words) }}</template>
      </el-table-column>
      <el-table-column label="阅读" width="90">
        <template #default="{ row }">{{ formatWords(row.readCount) }}</template>
      </el-table-column>
      <el-table-column label="付费阅读" width="100">
        <template #default="{ row }">{{ formatWords(row.payCount) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/author/chapters/${row.id}/edit?bookId=${bookId}`)">编辑</el-button>
          <el-button size="small" type="danger" text @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="还没有章节，点击右上角“新建章节”开始创作">
          <el-button type="primary" @click="$router.push(`/author/books/${bookId}/chapters/new`)">新建章节</el-button>
        </el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { authorChapters, deleteChapter } from '@/api/author'
import { bookDetail } from '@/api/book'
import { fenToYuan, formatTime, formatWords } from '@/utils/format'

const route = useRoute()
const bookId = Number(route.params.id)

const chapters = ref([])
const bookTitle = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    chapters.value = (await authorChapters(bookId)) || []
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

async function loadBook() {
  try {
    const b = await bookDetail(bookId)
    bookTitle.value = b?.title || ''
  } catch (e) { /* 忽略 */ }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除章节「${row.title}」吗？删除后不可恢复。`, '危险操作', {
      type: 'error',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }
  try {
    await deleteChapter(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(() => {
  load()
  loadBook()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.no-margin {
  margin: 0;
}
</style>
