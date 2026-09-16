<template>
  <div class="admin-appeals">
    <div class="filter-bar card">
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="reload">
        <el-option label="待处理" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
        <el-option label="已减轻处罚" :value="3" />
      </el-select>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="appeals" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" width="130">
          <template #default="{ row }">
            <div>{{ row.nickname || row.username }}</div>
            <div class="muted sub">{{ row.username }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申诉类型" width="110">
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
        <el-table-column label="处理人" width="100">
          <template #default="{ row }">{{ row.adminName || '-' }}</template>
        </el-table-column>
        <el-table-column label="申诉时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" @click="viewUserBan(row)">查看处罚</el-button>
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
              <el-button size="small" type="warning" @click="openReduce(row)">减轻</el-button>
            </template>
            <span v-else class="muted">已处理</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无申诉" :image-size="80" />
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

    <!-- 用户当前处罚详情弹窗 -->
    <el-dialog v-model="banDetailVisible" title="用户当前处罚" width="480px">
      <div v-if="banDetailUser" class="ban-detail">
        <p><strong>用户：</strong>{{ banDetailUser.nickname || banDetailUser.username }}（{{ banDetailUser.username }}）</p>
        <p><strong>账号状态：</strong>{{ banDetailUser.statusText }}</p>
        <p><strong>处罚状态：</strong>{{ banDetailUser.banStatusText || '无处罚' }}</p>
        <p v-if="banDetailUser.banReason"><strong>处罚原因：</strong>{{ banDetailUser.banReason }}</p>
        <p v-if="banDetailUser.banExpireTime"><strong>到期时间：</strong>{{ formatTime(banDetailUser.banExpireTime) }}</p>
      </div>
    </el-dialog>

    <!-- 减轻处罚弹窗 -->
    <el-dialog v-model="reduceVisible" title="减轻处罚" width="520px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>{{ reduceTarget?.nickname || reduceTarget?.username }}</span>
        </el-form-item>
        <el-form-item label="限制功能" required>
          <el-checkbox-group v-model="reduceForm.banFlagsList">
            <el-checkbox :value="1">禁止登录（永久）</el-checkbox>
            <el-checkbox :value="64">全功能限制（可登录）</el-checkbox>
            <el-checkbox :value="2">封禁评论</el-checkbox>
            <el-checkbox :value="4">封禁点赞</el-checkbox>
            <el-checkbox :value="8">封禁阅读购买</el-checkbox>
            <el-checkbox :value="16">作者警告</el-checkbox>
            <el-checkbox :value="32">封禁写书</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="处罚时长" required>
          <el-select v-model="reduceForm.banDays" style="width: 200px">
            <el-option label="1天" :value="1" />
            <el-option label="3天" :value="3" />
            <el-option label="7天" :value="7" />
            <el-option label="15天" :value="15" />
            <el-option label="30天" :value="30" />
            <el-option label="90天" :value="90" />
            <el-option label="永久" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处罚原因">
          <el-input v-model="reduceForm.banReason" placeholder="请填写新的处罚原因" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="管理员回复">
          <el-input v-model="reduceForm.adminReply" type="textarea" :rows="2" placeholder="请填写回复，将通知给用户" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reduceVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmReduce">确认减轻</el-button>
      </template>
    </el-dialog>

    <!-- 驳回/通过回复弹窗 -->
    <el-dialog v-model="replyVisible" :title="replyMode === 'approve' ? '通过申诉' : '驳回申诉'" width="480px">
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="3"
        :placeholder="replyMode === 'approve' ? '请填写回复（可选），将通知给用户' : '请填写驳回原因，将通知给用户'"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button :type="replyMode === 'approve' ? 'success' : 'danger'" :loading="submitting" @click="confirmReply">
          {{ replyMode === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appealPage, handleAppeal, userDetail } from '@/api/admin'
import { formatTime } from '@/utils/format'

const appeals = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, status: null })

const statusTagMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'primary' }

// 处罚详情
const banDetailVisible = ref(false)
const banDetailUser = ref(null)

// 减轻处罚
const reduceVisible = ref(false)
const reduceTarget = ref(null)
const reduceForm = reactive({ banFlagsList: [2], banDays: 7, banReason: '', adminReply: '' })

// 回复弹窗
const replyVisible = ref(false)
const replyMode = ref('approve')
const replyTarget = ref(null)
const replyText = ref('')

const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.status !== null && query.status !== '') params.status = query.status
    const res = await appealPage(params)
    appeals.value = res?.records || []
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

async function viewUserBan(row) {
  try {
    const user = await userDetail(row.userId)
    banDetailUser.value = user
    banDetailVisible.value = true
  } catch (e) { /* 全局提示 */ }
}

function handleApprove(row) {
  replyMode.value = 'approve'
  replyTarget.value = row
  replyText.value = ''
  replyVisible.value = true
}

function handleReject(row) {
  replyMode.value = 'reject'
  replyTarget.value = row
  replyText.value = ''
  replyVisible.value = true
}

async function confirmReply() {
  if (replyMode.value === 'reject' && !replyText.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    const action = replyMode.value === 'approve' ? 1 : 2
    await handleAppeal(replyTarget.value.id, { action, adminReply: replyText.value })
    ElMessage.success(replyMode.value === 'approve' ? '已通过申诉' : '已驳回申诉')
    replyVisible.value = false
    load()
  } catch (e) { /* 全局提示 */ } finally {
    submitting.value = false
  }
}

function openReduce(row) {
  reduceTarget.value = row
  reduceForm.banFlagsList = [2]
  reduceForm.banDays = 7
  reduceForm.banReason = ''
  reduceForm.adminReply = ''
  reduceVisible.value = true
}

async function confirmReduce() {
  if (reduceForm.banFlagsList.length === 0) {
    ElMessage.warning('请至少选择一项限制功能')
    return
  }
  const banFlags = reduceForm.banFlagsList.reduce((a, b) => a + b, 0)
  submitting.value = true
  try {
    await handleAppeal(reduceTarget.value.id, {
      action: 3,
      adminReply: reduceForm.adminReply,
      banFlags,
      banDays: reduceForm.banDays,
      banReason: reduceForm.banReason
    })
    ElMessage.success('已减轻处罚')
    reduceVisible.value = false
    load()
  } catch (e) { /* 全局提示 */ } finally {
    submitting.value = false
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
.sub {
  font-size: 12px;
  margin-top: 2px;
}
.muted {
  color: #909399;
}
</style>
