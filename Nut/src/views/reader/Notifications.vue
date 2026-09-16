<template>
  <div class="notifications page-container">
    <div class="section-title">
      消息通知
      <el-button v-if="list.length" size="small" style="margin-left: 12px" @click="markAllRead">全部已读</el-button>
    </div>
    <div class="card">
      <div
        v-for="item in list"
        :key="item.id"
        class="notify-item"
        :class="{ unread: item.isRead === 0 }"
        @click="markRead(item)"
      >
        <span class="notify-dot" :class="{ 'is-unread': item.isRead === 0 }" />
        <div class="notify-body">
          <div class="notify-head">
            <el-tag size="small" effect="plain">{{ notificationTypeMap[item.type] || item.type }}</el-tag>
            <span class="notify-title">{{ item.title }}</span>
            <span class="muted time">{{ formatTime(item.createTime) }}</span>
          </div>
          <p class="notify-content">{{ item.content }}</p>
        </div>
      </div>
      <el-empty v-if="!list.length" description="暂无通知" :image-size="90" />
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
import { ElMessage } from 'element-plus'
import { listNotifications, readNotification, readAll, unreadCount } from '@/api/notification'
import { useAppStore } from '@/store/app'
import { formatTime, notificationTypeMap } from '@/utils/format'

const appStore = useAppStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })

async function refreshBadge() {
  try {
    const res = await unreadCount()
    appStore.setUnread(res.unreadCount || 0)
  } catch (e) { /* 忽略 */ }
}

async function load() {
  loading.value = true
  try {
    const res = await listNotifications({ page: query.page, size: query.size })
    list.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

function onPage(p) {
  query.page = p
  load()
}

async function markRead(item) {
  if (item.isRead === 1) return
  try {
    await readNotification(item.id)
    item.isRead = 1
    refreshBadge()
  } catch (e) { /* 全局提示 */ }
}

async function markAllRead() {
  try {
    await readAll()
    ElMessage.success('已全部标记为已读')
    list.value.forEach((i) => (i.isRead = 1))
    refreshBadge()
  } catch (e) { /* 全局提示 */ }
}

onMounted(() => {
  load()
  refreshBadge()
})
</script>

<style scoped>
.notify-item {
  display: flex;
  gap: 12px;
  padding: 16px 8px;
  border-bottom: 1px solid #f2f2f4;
  cursor: pointer;
}
.notify-item:last-child {
  border-bottom: none;
}
.notify-item:hover {
  background: #fafafa;
}
.notify-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: transparent;
  margin-top: 7px;
  flex-shrink: 0;
}
.notify-dot.is-unread {
  background: var(--el-color-primary);
}
.notify-body {
  flex: 1;
  min-width: 0;
}
.notify-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.notify-title {
  font-weight: 600;
}
.notify-item.unread .notify-title {
  font-weight: 700;
}
.time {
  margin-left: auto;
  font-size: 12px;
}
.notify-content {
  margin: 0;
  color: #666;
  line-height: 1.7;
}
</style>
