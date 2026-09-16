<template>
  <div class="bookshelf page-container">
    <div class="section-title">我的书架</div>

    <el-tabs v-model="tab" class="shelf-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="已收藏" name="collect" />
      <el-tab-pane label="阅读记录" name="read" />
    </el-tabs>

    <div v-if="filteredList.length" class="card shelf-list">
      <div v-for="item in filteredList" :key="item.bookId" class="shelf-item">
        <img :src="item.cover || defaultCover" class="shelf-cover" alt="封面" @click="$router.push(`/book/${item.bookId}`)" />
        <div class="shelf-info" @click="$router.push(`/book/${item.bookId}`)">
          <div class="shelf-title">{{ item.bookTitle }}</div>
          <div class="shelf-meta muted">{{ item.authorName || '佚名' }} · {{ bookStatusMap[item.bookStatus] || '' }}</div>
          <div class="shelf-progress">
            <span class="muted">读到：{{ item.readChapterTitle || '未开始阅读' }}</span>
            <el-progress
              v-if="item.readPosition !== null && item.readPosition !== undefined && item.readPosition > 0"
              :percentage="Number(item.readPosition)"
              :show-text="false"
              class="progress-bar"
            />
          </div>
          <div class="shelf-time muted">更新于 {{ formatTime(item.updateTime) }}</div>
        </div>
        <div class="shelf-actions">
          <el-button type="primary" @click="continueRead(item)">继续阅读</el-button>
          <el-button :type="item.isCollect === 1 ? 'default' : 'warning'" @click="toggleCollect(item)">
            {{ item.isCollect === 1 ? '取消收藏' : '收藏' }}
          </el-button>
          <el-button text type="danger" @click="remove(item)">移除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="书架还是空的，去书城逛逛吧" :image-size="100">
      <el-button type="primary" @click="$router.push('/book')">去书城</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBookshelf, setCollect, removeBookshelf } from '@/api/bookshelf'
import { formatTime, bookStatusMap } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'

const router = useRouter()
const list = ref([])
const tab = ref('all')
const loading = ref(false)

const filteredList = computed(() => {
  if (tab.value === 'collect') return list.value.filter((i) => i.isCollect === 1)
  if (tab.value === 'read') return list.value.filter((i) => i.readChapterId)
  return list.value
})

async function load() {
  loading.value = true
  try {
    list.value = (await listBookshelf()) || []
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

function continueRead(item) {
  if (item.readChapterId) {
    router.push({ path: `/read/${item.readChapterId}`, query: { bookId: item.bookId } })
  } else {
    router.push(`/book/${item.bookId}`)
  }
}

async function toggleCollect(item) {
  const next = item.isCollect === 1 ? 0 : 1
  try {
    await setCollect({ bookId: item.bookId, isCollect: next })
    item.isCollect = next
    ElMessage.success(next === 1 ? '已收藏' : '已取消收藏')
  } catch (e) { /* 全局提示 */ }
}

async function remove(item) {
  try {
    await ElMessageBox.confirm(`确定将《${item.bookTitle}》移出书架吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await removeBookshelf(item.bookId)
    ElMessage.success('已移出书架')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
</script>

<style scoped>
.shelf-list {
  padding: 4px 20px;
}
.shelf-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px 4px;
  border-bottom: 1px solid #f2f2f4;
}
.shelf-item:last-child {
  border-bottom: none;
}
.shelf-cover {
  width: 64px;
  height: 86px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  flex-shrink: 0;
}
.shelf-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.shelf-title {
  font-size: 16px;
  font-weight: 600;
}
.shelf-meta {
  font-size: 13px;
  margin: 4px 0;
}
.shelf-progress {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
}
.progress-bar {
  width: 180px;
}
.shelf-time {
  font-size: 12px;
  margin-top: 4px;
}
.shelf-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
