<template>
  <div class="reader-appeals">
    <div class="page-header">
      <h2>处罚申诉</h2>
      <p class="muted">如对处罚有异议，可在此发起申诉，管理员审核后会给出处理结果。</p>
    </div>

    <!-- 当前处罚信息 -->
    <div v-if="currentBan" class="card ban-info">
      <div class="ban-info-header">
        <el-icon :size="20" color="#e6a23c"><Warning /></el-icon>
        <span class="ban-title">当前生效处罚</span>
      </div>
      <div class="ban-info-body">
        <p><strong>处罚状态：</strong>{{ currentBan.banStatusText || currentBan.statusText }}</p>
        <p v-if="currentBan.banReason"><strong>处罚原因：</strong>{{ currentBan.banReason }}</p>
        <p v-if="currentBan.banExpireTime"><strong>到期时间：</strong>{{ formatTime(currentBan.banExpireTime) }}</p>
        <p v-else-if="currentBan.status === 0"><strong>到期时间：</strong>永久</p>
      </div>
      <el-button v-if="!hasPending" type="primary" @click="openCreate">发起申诉</el-button>
      <el-alert v-else type="warning" title="你已有一条待处理的申诉，请等待管理员审核" :closable="false" show-icon style="margin-top: 12px" />
    </div>
    <div v-else class="card no-ban">
      <el-empty description="当前账号无生效处罚" :image-size="80" />
    </div>

    <!-- 申诉记录 -->
    <div class="card" style="margin-top: 16px">
      <h3 class="section-title">申诉记录</h3>
      <el-table v-loading="loading" :data="appeals" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="申诉类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.appealTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申诉理由" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reason }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员回复" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.adminReply || '-' }}</template>
        </el-table-column>
        <el-table-column label="申诉时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="处理时间" width="150">
          <template #default="{ row }">{{ formatTime(row.handleTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无申诉记录" :image-size="80" />
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

    <!-- 发起申诉弹窗 -->
    <el-dialog v-model="createVisible" title="发起申诉" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="申诉类型" required>
          <el-select v-model="form.appealType" style="width: 100%">
            <el-option label="评论封禁" :value="1" />
            <el-option label="账号封禁" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉理由" required>
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="5"
            placeholder="请详细说明申诉理由，最多500字"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { createAppeal, myAppeals } from '@/api/appeal'
import { getInfo } from '@/api/auth'
import { formatTime } from '@/utils/format'

const appeals = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })

const currentBan = ref(null)
const hasPending = ref(false)

const createVisible = ref(false)
const submitting = ref(false)
const form = reactive({ appealType: 2, reason: '' })

const statusTagMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'primary' }

async function loadCurrentUser() {
  try {
    const user = await getInfo()
    currentBan.value = user
    // 检查是否有待处理申诉
    hasPending.value = appeals.value.some(a => a.status === 0)
  } catch (e) { /* 忽略 */ }
}

async function load() {
  loading.value = true
  try {
    const res = await myAppeals({ page: query.page, size: query.size })
    appeals.value = res?.records || []
    total.value = res?.total || 0
    hasPending.value = appeals.value.some(a => a.status === 0)
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

function onPage(p) {
  query.page = p
  load()
}

function openCreate() {
  form.appealType = 2
  form.reason = ''
  createVisible.value = true
}

async function submitAppeal() {
  if (!form.reason.trim()) {
    ElMessage.warning('请填写申诉理由')
    return
  }
  submitting.value = true
  try {
    await createAppeal({ appealType: form.appealType, reason: form.reason })
    ElMessage.success('申诉提交成功，请等待管理员审核')
    createVisible.value = false
    load()
    loadCurrentUser()
  } catch (e) { /* 全局提示 */ } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCurrentUser()
  load()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0 0 8px;
}
.section-title {
  margin: 0 0 16px;
  font-size: 16px;
}
.ban-info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.ban-title {
  font-weight: 600;
  font-size: 15px;
}
.ban-info-body p {
  margin: 6px 0;
}
.no-ban {
  padding: 20px 0;
}
.muted {
  color: #909399;
  font-size: 13px;
}
</style>
