<template>
  <div class="admin-users">
    <div class="filter-bar card">
      <el-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable style="width: 220px" @keyup.enter="reload" @clear="reload">
        <template #append>
          <el-button :icon="Search" @click="reload" />
        </template>
      </el-input>
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 140px" @change="reload">
        <el-option label="读者" :value="0" />
        <el-option label="作者" :value="1" />
        <el-option label="管理员" :value="2" />
      </el-select>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar || defaultAvatar">{{ (row.nickname || row.username || '?').charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : row.role === 1 ? 'warning' : 'info'" size="small">{{ row.roleText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="余额" width="110">
          <template #default="{ row }">¥{{ fenToYuan(row.balance) }}</template>
        </el-table-column>
        <el-table-column label="账号状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处罚状态" min-width="200">
          <template #default="{ row }">
            <template v-if="row.banStatusText">
              <el-tooltip :content="buildBanTooltip(row)" placement="top">
                <el-tag type="warning" size="small" effect="dark">{{ row.banStatusText }}</el-tag>
              </el-tooltip>
            </template>
            <span v-else class="muted">无处罚</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role !== 2">
              <el-dropdown trigger="click" @command="(cmd) => onAction(cmd, row)">
                <el-button size="small" type="primary">
                  操作
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <!-- 读者处罚选项 -->
                    <template v-if="row.role === 0">
                      <el-dropdown-item command="customBan" :icon="Warning">自定义处罚</el-dropdown-item>
                      <el-dropdown-item divided command="banComment7" :icon="ChatDotRound">封禁评论 7天</el-dropdown-item>
                      <el-dropdown-item command="banComment30" :icon="ChatDotRound">封禁评论 30天</el-dropdown-item>
                      <el-dropdown-item divided command="banAccount7" :icon="Lock">封禁账号 7天</el-dropdown-item>
                      <el-dropdown-item command="banAccount30" :icon="Lock">封禁账号 30天</el-dropdown-item>
                      <el-dropdown-item divided command="banForever" :icon="Lock">永久封禁</el-dropdown-item>
                    </template>
                    <!-- 作者处罚选项 -->
                    <template v-else-if="row.role === 1">
                      <el-dropdown-item command="customBan" :icon="Warning">自定义处罚</el-dropdown-item>
                      <el-dropdown-item divided command="authorWarning7" :icon="View">警告 7天</el-dropdown-item>
                      <el-dropdown-item command="banWrite7" :icon="EditPen">封禁写书 7天</el-dropdown-item>
                      <el-dropdown-item divided command="banAccount30" :icon="Lock">封禁账号 30天</el-dropdown-item>
                      <el-dropdown-item divided command="banForever" :icon="Lock">永久封禁</el-dropdown-item>
                    </template>
                    <el-dropdown-item v-if="isBanned(row) || row.status === 0" divided command="unban" :icon="Unlock">解除处罚/启用</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户" :image-size="80" />
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

    <!-- 自定义处罚弹窗 -->
    <el-dialog v-model="banDialogVisible" title="自定义处罚" width="520px" :close-on-click-modal="false">
      <el-form v-if="banTarget" label-width="100px">
        <el-form-item label="处罚用户">
          <span>{{ banTarget.nickname || banTarget.username }}（{{ banTarget.username }}）</span>
        </el-form-item>
        <el-form-item label="限制功能" required>
          <el-checkbox-group v-model="banForm.banFlagsList">
            <el-checkbox :value="1">封禁登录（全功能，无法登录）</el-checkbox>
            <el-checkbox :value="2">封禁评论</el-checkbox>
            <el-checkbox :value="4">封禁点赞</el-checkbox>
            <el-checkbox :value="8">封禁阅读/购买</el-checkbox>
            <template v-if="banTarget?.role === 1">
              <el-checkbox :value="16">作者警告（新书需审核）</el-checkbox>
              <el-checkbox :value="32">封禁写书（禁止上传/编辑）</el-checkbox>
            </template>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="处罚时长" required>
          <el-select v-model="banForm.banDays" style="width: 200px">
            <el-option label="1天" :value="1" />
            <el-option label="3天" :value="3" />
            <el-option label="7天（一周）" :value="7" />
            <el-option label="15天" :value="15" />
            <el-option label="30天（一个月）" :value="30" />
            <el-option label="90天（三个月）" :value="90" />
            <el-option label="180天（半年）" :value="180" />
            <el-option label="365天（一年）" :value="365" />
            <el-option label="永久封禁" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处罚原因">
          <el-input
            v-model="banForm.banReason"
            type="textarea"
            :rows="3"
            placeholder="请填写处罚原因，如：违反社区公约、发布违规评论等"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="banSubmitting" @click="submitBan">确认处罚</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown, Warning, ChatDotRound, Star, Reading, ChatLineRound, Lock, Unlock, View, EditPen } from '@element-plus/icons-vue'
import { userPage, updateUserStatus, banUser, unbanUser } from '@/api/admin'
import { fenToYuan, formatTime } from '@/utils/format'
import defaultAvatar from '@/assets/default-avatar.svg'

const users = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '', role: null })

// 处罚弹窗
const banDialogVisible = ref(false)
const banSubmitting = ref(false)
const banTarget = ref(null)
const banForm = reactive({
  banFlagsList: [],
  banDays: 7,
  banReason: ''
})

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.role !== null && query.role !== '') params.role = query.role
    const res = await userPage(params)
    users.value = res?.records || []
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

/** 判断用户是否处于处罚中 */
function isBanned(row) {
  return row.banFlags && row.banFlags > 0 && !isBanExpired(row)
}

/** 判断处罚是否已过期 */
function isBanExpired(row) {
  if (!row.banExpireTime) return false
  return new Date(row.banExpireTime).getTime() < Date.now()
}

/** 构建处罚 tooltip */
function buildBanTooltip(row) {
  let text = ''
  if (row.banReason) text += '原因：' + row.banReason + '\n'
  if (row.banExpireTime) {
    text += '到期时间：' + formatTime(row.banExpireTime)
  } else {
    text += '到期时间：永久'
  }
  return text
}

/** 打开发处罚弹窗 */
function openBanDialog(row) {
  banTarget.value = row
  banForm.banFlagsList = [2] // 默认勾选评论
  banForm.banDays = 7
  banForm.banReason = ''
  banDialogVisible.value = true
}

/** 提交处罚 */
async function submitBan() {
  if (banForm.banFlagsList.length === 0) {
    ElMessage.warning('请至少选择一项限制功能')
    return
  }
  const banFlags = banForm.banFlagsList.reduce((a, b) => a + b, 0)
  banSubmitting.value = true
  try {
    await banUser(banTarget.value.id, {
      banFlags,
      banDays: banForm.banDays,
      banReason: banForm.banReason
    })
    ElMessage.success('处罚已生效')
    banDialogVisible.value = false
    load()
  } catch (e) { /* 全局提示 */ } finally {
    banSubmitting.value = false
  }
}

/** 解除处罚 */
async function doUnban(row) {
  try {
    await ElMessageBox.confirm(`确定解除用户「${row.username}」的所有处罚吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await unbanUser(row.id)
    ElMessage.success('已解除处罚')
    load()
  } catch (e) { /* 全局提示 */ }
}

async function toggleStatus(row, status) {
  const action = status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.username}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await updateUserStatus(row.id, status)
    ElMessage.success(`已${action}`)
    load()
  } catch (e) { /* 全局提示 */ }
}

/** 下拉菜单操作统一入口 */
async function onAction(cmd, row) {
  switch (cmd) {
    case 'customBan':
      openBanDialog(row)
      break
    // 读者处罚
    case 'banComment7':
      await quickBan(row, 6, 7, '违反社区公约，封禁评论和点赞7天')
      break
    case 'banComment30':
      await quickBan(row, 6, 30, '多次违规，封禁评论和点赞30天')
      break
    case 'banAccount7':
      await quickBan(row, 1, 7, '严重违规，封禁账号7天')
      break
    case 'banAccount30':
      await quickBan(row, 1, 30, '严重违规，封禁账号30天')
      break
    // 作者处罚
    case 'authorWarning7':
      await quickBan(row, 16, 7, '作品违规，警告7天，期间新书需审核')
      break
    case 'banWrite7':
      await quickBan(row, 38, 7, '违规创作，封禁写书7天')
      break
    // 通用
    case 'banForever':
      await quickBan(row, 1, -1, '严重违规，永久封禁')
      break
    case 'unban':
      if (row.status === 0) {
        await toggleStatus(row, 1)
      } else {
        await doUnban(row)
      }
      break
  }
}

/** 快捷封禁（带确认弹窗） */
async function quickBan(row, banFlags, banDays, banReason) {
  const flagText = []
  if (banFlags & 1) flagText.push('登录(全功能)')
  if (banFlags & 2) flagText.push('评论')
  if (banFlags & 4) flagText.push('点赞')
  if (banFlags & 8) flagText.push('阅读购买')
  if (banFlags & 16) flagText.push('作者警告(新书待审核)')
  if (banFlags & 32) flagText.push('封禁写书')
  const daysText = banDays === -1 ? '永久' : banDays + '天'
  try {
    await ElMessageBox.confirm(
      `确定对用户「${row.username}」执行处罚吗？\n限制功能：${flagText.join('、')}\n处罚时长：${daysText}`,
      '处罚确认',
      { type: 'warning', confirmButtonText: '确认处罚', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await banUser(row.id, { banFlags, banDays, banReason })
    ElMessage.success('处罚已生效')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
}
.muted {
  color: #909399;
  font-size: 13px;
}
</style>
