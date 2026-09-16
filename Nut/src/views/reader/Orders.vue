<template>
  <div class="orders page-container">
    <div class="section-title">我的订单</div>
    <div class="card">
      <div class="filter-bar">
        <el-select v-model="query.payStatus" placeholder="支付状态" clearable style="width: 140px" @change="reload">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已关闭" :value="2" />
          <el-option label="已退款" :value="3" />
        </el-select>
        <el-select v-model="query.type" placeholder="订单类型" clearable style="width: 140px" @change="reload">
          <el-option label="单章" :value="1" />
          <el-option label="整本" :value="2" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="orders" stripe style="width: 100%; margin-top: 14px">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="内容" min-width="160">
          <template #default="{ row }">
            <div>{{ row.bookTitle }}</div>
            <div v-if="row.chapterTitle" class="muted sub">{{ row.chapterTitle }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.typeText || '' }}</el-tag>
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.payStatus === 0" type="primary" size="small" :loading="paying === row.orderNo" @click="pay(row)">
              去支付
            </el-button>
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

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders, orderDetail, payOrder } from '@/api/order'
import { useUserStore } from '@/store/user'
import { fenToYuan, formatTime, payStatusTagMap } from '@/utils/format'

const userStore = useUserStore()

const orders = ref([])
const total = ref(0)
const loading = ref(false)
const paying = ref('')
const query = reactive({ page: 1, size: 10, payStatus: null, type: null })

const detailVisible = ref(false)
const detail = ref(null)

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.payStatus !== null && query.payStatus !== '') params.payStatus = query.payStatus
    if (query.type !== null && query.type !== '') params.type = query.type
    const res = await myOrders(params)
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

async function pay(row) {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ¥${fenToYuan(row.amount)}？将从余额中扣除。`,
      '确认支付',
      { type: 'info', confirmButtonText: '确认支付', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  paying.value = row.orderNo
  try {
    await payOrder(row.orderNo)
    ElMessage.success('支付成功')
    userStore.refreshInfo()
    load()
  } catch (e) { /* 全局提示（余额不足会打开充值弹窗） */ } finally {
    paying.value = ''
  }
}

async function showDetail(orderNo) {
  try {
    detail.value = await orderDetail(orderNo)
    detailVisible.value = true
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
}
.sub {
  font-size: 12px;
  margin-top: 2px;
}
</style>
