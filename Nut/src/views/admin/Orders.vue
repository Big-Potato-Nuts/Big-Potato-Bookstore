<template>
  <div class="admin-orders">
    <div class="filter-bar card">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 220px" @keyup.enter="reload" @clear="reload" />
      <el-input v-model="query.username" placeholder="用户名" clearable style="width: 140px" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.payStatus" placeholder="支付状态" clearable style="width: 130px" @change="reload">
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已关闭" :value="2" />
        <el-option label="已退款" :value="3" />
      </el-select>
      <el-select v-model="query.type" placeholder="订单类型" clearable style="width: 130px" @change="reload">
        <el-option label="单章" :value="1" />
        <el-option label="整本" :value="2" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="orders" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" min-width="110" />
        <el-table-column label="内容" min-width="150">
          <template #default="{ row }">
            <div>{{ row.bookTitle }}</div>
            <div v-if="row.chapterTitle" class="muted sub">{{ row.chapterTitle }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.typeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ fenToYuan(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="payStatusTagMap[row.payStatus] || 'info'" size="small">{{ row.payStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="支付时间" width="150">
          <template #default="{ row }">{{ formatTime(row.payTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row.orderNo)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单" :image-size="80" />
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

    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.username }}（{{ detail.nickname }}）</el-descriptions-item>
        <el-descriptions-item label="书籍">{{ detail.bookTitle }}</el-descriptions-item>
        <el-descriptions-item label="章节">{{ detail.chapterTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.typeText }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ fenToYuan(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.payStatusText }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(detail.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ formatTime(detail.closeTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { orderPage, adminOrderDetail } from '@/api/admin'
import { fenToYuan, formatTime, payStatusTagMap } from '@/utils/format'

const orders = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, orderNo: '', username: '', payStatus: null, type: null })

const detailVisible = ref(false)
const detail = ref(null)

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.orderNo) params.orderNo = query.orderNo
    if (query.username) params.username = query.username
    if (query.payStatus !== null && query.payStatus !== '') params.payStatus = query.payStatus
    if (query.type !== null && query.type !== '') params.type = query.type
    const res = await orderPage(params)
    orders.value = res?.records || []
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

async function showDetail(orderNo) {
  try {
    detail.value = await adminOrderDetail(orderNo)
    detailVisible.value = true
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: 16px 20px;
}
.sub {
  font-size: 12px;
  margin-top: 2px;
}
</style>
