<template>
  <div class="admin-authors">
    <div class="filter-bar card">
      <el-select v-model="query.auditStatus" placeholder="全部状态" clearable style="width: 160px" @change="reload">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="list" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="110" />
        <el-table-column prop="idCard" label="身份证号" min-width="170" />
        <el-table-column prop="intro" label="作者简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="auditStatusTagMap[row.auditStatus] || 'info'" size="small">{{ row.auditStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核原因" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.auditReason || '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.auditStatus === 0">
              <el-button size="small" type="success" plain @click="audit(row, true)">通过</el-button>
              <el-button size="small" type="danger" plain @click="audit(row, false)">驳回</el-button>
            </template>
            <span v-else class="muted">已处理</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无入驻申请" :image-size="80" />
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

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回申请" width="460px">
      <el-form label-width="80px">
        <el-form-item label="申请人">{{ current?.username }}</el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="请填写驳回原因，将反馈给申请人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="auditing" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authorPage, auditAuthor } from '@/api/admin'
import { formatTime, auditStatusTagMap } from '@/utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, auditStatus: null })

const rejectVisible = ref(false)
const current = ref(null)
const rejectReason = ref('')
const auditing = ref(false)

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.auditStatus !== null && query.auditStatus !== '') params.auditStatus = query.auditStatus
    const res = await authorPage(params)
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

async function audit(row, pass) {
  current.value = row
  if (!pass) {
    rejectReason.value = ''
    rejectVisible.value = true
    return
  }
  try {
    await ElMessageBox.confirm(`确定通过「${row.username}」的入驻申请吗？通过后该用户将升级为作者。`, '提示', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  auditing.value = true
  try {
    await auditAuthor(row.id, { pass: true })
    ElMessage.success('已通过，用户已升级为作者')
    load()
  } catch (e) { /* 全局提示 */ } finally {
    auditing.value = false
  }
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  auditing.value = true
  try {
    await auditAuthor(current.value.id, { pass: false, reason: rejectReason.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    load()
  } catch (e) { /* 全局提示 */ } finally {
    auditing.value = false
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
</style>
