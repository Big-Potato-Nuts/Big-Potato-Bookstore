<template>
  <div class="author-dashboard">
    <div v-loading="loading">
      <!-- 统计卡片 -->
      <div class="stat-grid" v-if="stats">
        <div class="stat-card">
          <div class="stat-label muted">作品数</div>
          <div class="stat-num">{{ stats.bookCount ?? 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label muted">总点击</div>
          <div class="stat-num">{{ formatWords(stats.totalClick) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label muted">总收藏</div>
          <div class="stat-num">{{ formatWords(stats.totalCollect) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label muted">付费次数</div>
          <div class="stat-num">{{ formatWords(stats.totalPayCount) }}</div>
        </div>
        <div class="stat-card highlight">
          <div class="stat-label muted">总收益</div>
          <div class="stat-num">¥{{ fenToYuan(stats.totalRevenue) }}</div>
        </div>
      </div>

      <!-- 作品数据 -->
      <div class="section-title">作品数据</div>
      <el-table :data="stats?.books || []" stripe style="width: 100%">
        <el-table-column prop="title" label="作品" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="bookStatusTagMap[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chapterCount" label="章节数" width="90" />
        <el-table-column label="点击" width="90">
          <template #default="{ row }">{{ formatWords(row.clickCount) }}</template>
        </el-table-column>
        <el-table-column label="收藏" width="90">
          <template #default="{ row }">{{ formatWords(row.collectCount) }}</template>
        </el-table-column>
        <el-table-column label="付费次数" width="100">
          <template #default="{ row }">{{ formatWords(row.payCount) }}</template>
        </el-table-column>
        <el-table-column label="收益" width="110">
          <template #default="{ row }">¥{{ fenToYuan(row.revenue) }}</template>
        </el-table-column>
        <el-table-column label="最近更新" width="150">
          <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/author/books/${row.bookId}/chapters`)">章节</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有作品，先去创建一本吧">
            <el-button type="primary" @click="$router.push('/author/books/new')">新建作品</el-button>
          </el-empty>
        </template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { authorStats } from '@/api/author'
import { fenToYuan, formatTime, formatWords, bookStatusTagMap } from '@/utils/format'

const stats = ref(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    stats.value = await authorStats()
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.stat-card.highlight {
  background: linear-gradient(135deg, #c4402f, #e06b3d);
  color: #fff;
}
.stat-card.highlight .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
.stat-label {
  font-size: 13px;
}
.stat-num {
  font-size: 26px;
  font-weight: 700;
  margin-top: 6px;
}
</style>
