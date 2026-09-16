<template>
  <div class="admin-books">
    <div class="filter-bar card">
      <el-input v-model="query.keyword" placeholder="书名 / 作者" clearable style="width: 220px" @keyup.enter="reload" @clear="reload">
        <template #append>
          <el-button :icon="Search" @click="reload" />
        </template>
      </el-input>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="reload">
        <el-option label="连载中" :value="1" />
        <el-option label="已完结" :value="2" />
        <el-option label="已下架" :value="0" />
        <el-option label="待审核" :value="3" />
      </el-select>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="books" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="70">
          <template #default="{ row }">
            <img :src="row.cover || defaultCover" class="book-cover" alt="封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" min-width="110" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="bookStatusTagMap[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90">
          <template #default="{ row }">
            <el-tooltip :content="row.status === 1 || row.status === 2 ? '' : '仅上架作品可在读者端展示推荐位'" :disabled="row.status === 1 || row.status === 2">
              <el-switch
                v-model="row.isRecommend"
                :active-value="1"
                :inactive-value="0"
                :disabled="row.status !== 1 && row.status !== 2"
                @change="(val) => onRecommendChange(row, val)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="点击" width="90">
          <template #default="{ row }">{{ formatWords(row.clickCount) }}</template>
        </el-table-column>
        <el-table-column label="收藏" width="90">
          <template #default="{ row }">{{ formatWords(row.collectCount) }}</template>
        </el-table-column>
        <el-table-column label="章节" width="80">
          <template #default="{ row }">{{ row.chapterCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="150">
          <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => onStatusCmd(row, cmd)">
              <el-button size="small" type="warning" plain>
                {{ bookStatusMap[row.status] || '状态' }}
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="row.status === 1" command="1">上架（连载中）</el-dropdown-item>
                  <el-dropdown-item :disabled="row.status === 2" command="2">设为完结</el-dropdown-item>
                  <el-dropdown-item :disabled="row.status === 0" command="0" divided>下架</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无小说" :image-size="80" />
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
import { adminBookPage, updateBookStatus, updateBookRecommend } from '@/api/admin'
import { formatTime, formatWords, bookStatusMap, bookStatusTagMap } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'

const books = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '', status: null })

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== null && query.status !== '') params.status = query.status
    const res = await adminBookPage(params)
    books.value = res?.records || []
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

async function onStatusCmd(row, status) {
  const target = Number(status)
  const label = bookStatusMap[target]
  try {
    await ElMessageBox.confirm(`确定将《${row.title}》${label === '已下架' ? '下架' : '设置为“' + label + '”'}吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await updateBookStatus(row.id, target)
    ElMessage.success(`已${label}`)
    load()
  } catch (e) { /* 全局提示 */ }
}

async function onRecommendChange(row, val) {
  const action = val === 1 ? '设为推荐' : '取消推荐'
  try {
    await ElMessageBox.confirm(
      `确定${action}《${row.title}》吗？${val === 1 ? '设为推荐后将在读者端首页“编辑推荐”位展示。' : '取消后将从读者端首页推荐位移除。'}`,
      '提示',
      { type: 'warning' }
    )
  } catch (e) {
    row.isRecommend = val === 1 ? 0 : 1 // 取消操作：回滚开关
    return
  }
  try {
    await updateBookRecommend(row.id, val)
    ElMessage.success(`已${action}`)
  } catch (e) {
    row.isRecommend = val === 1 ? 0 : 1 // 请求失败：回滚开关
  }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
}
.book-cover {
  width: 36px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
