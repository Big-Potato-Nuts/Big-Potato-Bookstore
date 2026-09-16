<template>
  <div class="author-books">
    <div class="toolbar">
      <div class="section-title no-margin">我的作品</div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/author/books/new')">新建作品</el-button>
    </div>

    <el-table v-loading="loading" :data="books" stripe style="width: 100%">
      <el-table-column label="封面" width="90">
        <template #default="{ row }">
          <img :src="row.cover || defaultCover" class="book-cover" alt="封面" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="bookStatusTagMap[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chapterCount" label="章节" width="80" />
      <el-table-column label="点击" width="90">
        <template #default="{ row }">{{ formatWords(row.clickCount) }}</template>
      </el-table-column>
      <el-table-column label="收藏" width="90">
        <template #default="{ row }">{{ formatWords(row.collectCount) }}</template>
      </el-table-column>
      <el-table-column label="最新章节" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.latestChapterTitle || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="$router.push(`/author/books/${row.id}/chapters`)">章节管理</el-button>
          <el-button size="small" @click="$router.push(`/author/books/${row.id}/edit`)">编辑</el-button>
          <template v-if="row.status === 3">
            <el-tag type="warning" size="small">审核中</el-tag>
          </template>
          <template v-else-if="row.status === 0">
            <el-button size="small" type="success" plain @click="resubmit(row)">重新提交</el-button>
          </template>
          <template v-else>
            <el-dropdown trigger="click" @command="(cmd) => onStatusCmd(row, cmd)">
              <el-button size="small" type="warning" plain>
                {{ bookStatusMap[row.status] || '状态' }}
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="row.status === 1" command="1">设为连载中</el-dropdown-item>
                  <el-dropdown-item :disabled="row.status === 2" command="2">设为已完结</el-dropdown-item>
                  <el-dropdown-item :disabled="row.status === 0" command="0" divided>下架</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button size="small" type="danger" text @click="removeBook(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="还没有作品，创建第一本吧">
          <el-button type="primary" @click="$router.push('/author/books/new')">新建作品</el-button>
        </el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { authorBooks, updateBookStatus, resubmitBook, deleteBook } from '@/api/author'
import { formatWords, bookStatusMap, bookStatusTagMap } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'

const books = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    books.value = (await authorBooks()) || []
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

async function onStatusCmd(row, status) {
  const target = Number(status)
  const label = bookStatusMap[target]
  try {
    await ElMessageBox.confirm(`确定将《${row.title}》${label === '下架' ? '下架' : '设置为“' + label + '”'}吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await updateBookStatus(row.id, target)
    ElMessage.success(`已${label}`)
    load()
  } catch (e) { /* 全局提示 */ }
}

async function removeBook(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？删除后不可恢复。`, '危险操作', {
      type: 'error',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }
  try {
    await deleteBook(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* 全局提示（有章节时后端会拦截） */ }
}

async function resubmit(row) {
  try {
    await ElMessageBox.confirm(`确定重新提交《${row.title}》审核吗？提交后读者暂时不可见，需等待管理员审核。`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await resubmitBook(row.id)
    ElMessage.success('已重新提交审核')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
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
.book-cover {
  width: 44px;
  height: 58px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
