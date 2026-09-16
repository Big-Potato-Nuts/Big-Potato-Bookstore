<template>
  <div class="profile page-container">
    <div class="section-title">个人中心</div>
    <el-tabs v-model="tab" class="profile-tabs">
      <el-tab-pane label="基本资料" name="info" />
      <el-tab-pane label="修改密码" name="password" />
      <el-tab-pane label="余额与充值" name="balance" />
      <el-tab-pane label="处罚申诉" name="appeal" />
    </el-tabs>

    <!-- 基本资料 -->
    <div v-show="tab === 'info'" class="card form-card">
      <el-form ref="infoRef" :model="infoForm" :rules="infoRules" label-width="90px" style="max-width: 460px">
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-avatar :size="72" :src="infoForm.avatar || defaultAvatar">
              {{ (infoForm.nickname || '?').charAt(0) }}
            </el-avatar>
            <span class="muted hint">支持图片 URL 地址（如 <code>https://xxx/1.png</code>）</span>
          </div>
        </el-form-item>
        <el-form-item label="头像 URL" prop="avatar">
          <el-input v-model="infoForm.avatar" placeholder="https://..." clearable />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveInfo">保存资料</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 修改密码 -->
    <div v-show="tab === 'password'" class="card form-card">
      <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px" style="max-width: 460px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingPwd" @click="savePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 余额 -->
    <div v-show="tab === 'balance'" class="card balance-card">
      <div class="balance-box">
        <div class="muted">当前余额</div>
        <div class="balance-num">¥ {{ fenToYuan(userStore.balance) }}</div>
        <el-button type="primary" @click="appStore.openRecharge()">
          <el-icon><Wallet /></el-icon>&nbsp;立即充值
        </el-button>
      </div>
      <p class="muted tip">模拟充值：选择金额后点击确认，余额即时到账，用于购买付费章节。</p>
    </div>

    <!-- 处罚申诉 -->
    <div v-show="tab === 'appeal'" class="card appeal-card">
      <div v-if="hasBan" class="ban-info">
        <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
          <template #title>
            <span>当前账号存在生效处罚</span>
          </template>
          <div class="ban-detail">
            <p v-if="userStore.user?.banStatusText"><strong>处罚状态：</strong>{{ userStore.user.banStatusText }}</p>
            <p v-if="userStore.user?.banReason"><strong>处罚原因：</strong>{{ userStore.user.banReason }}</p>
            <p v-if="userStore.user?.banExpireTime"><strong>到期时间：</strong>{{ formatTime(userStore.user.banExpireTime) }}</p>
            <p v-else-if="userStore.user?.status === 0"><strong>到期时间：</strong>永久</p>
          </div>
        </el-alert>
        <el-button type="primary" @click="goAppeals">前往申诉</el-button>
      </div>
      <div v-else>
        <el-empty description="当前账号无生效处罚" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { updateProfile, updatePassword } from '@/api/user'
import { fenToYuan, formatTime } from '@/utils/format'
import defaultAvatar from '@/assets/default-avatar.svg'

const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()

const tab = ref('info')

// 是否有生效处罚
const hasBan = computed(() => {
  const u = userStore.user
  if (!u) return false
  if (u.status === 0) return true
  return u.banFlags && u.banFlags > 0
})

function goAppeals() {
  router.push('/appeals')
}

// 基本资料
const infoRef = ref()
const saving = ref(false)
const infoForm = reactive({ nickname: '', avatar: '' })
const infoRules = {
  nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }]
}

// 修改密码
const pwdRef = ref()
const savingPwd = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => (value === pwdForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur'
    }
  ]
}

function fillForm() {
  infoForm.nickname = userStore.user?.nickname || ''
  infoForm.avatar = userStore.user?.avatar || ''
}

async function saveInfo() {
  try {
    await infoRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const user = await updateProfile({ nickname: infoForm.nickname, avatar: infoForm.avatar || undefined })
    userStore.setUser({ ...userStore.user, ...user })
    ElMessage.success('资料已更新')
  } catch (e) { /* 全局提示 */ } finally {
    saving.value = false
  }
}

async function savePassword() {
  try {
    await pwdRef.value.validate()
  } catch (e) {
    return
  }
  savingPwd.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) { /* 全局提示（如原密码错误） */ } finally {
    savingPwd.value = false
  }
}

onMounted(fillForm)
</script>

<style scoped>
.form-card {
  max-width: 680px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.hint {
  font-size: 12px;
}
.balance-box {
  text-align: center;
  padding: 30px 0 10px;
}
.balance-num {
  font-size: 40px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin: 8px 0 18px;
}
.tip {
  text-align: center;
  font-size: 12px;
  margin-top: 20px;
}
.appeal-card {
  max-width: 680px;
}
.ban-detail p {
  margin: 6px 0;
}
</style>
