<template>
  <div class="author-apply page-container">
    <div class="section-title">作者入驻</div>

    <!-- 待审核 -->
    <div v-if="status === 0" class="card state-card">
      <el-result icon="warning" title="申请审核中" sub-title="你的入驻申请已提交，审核通过后将自动升级为作者角色，请耐心等待。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
      <el-descriptions v-if="applyInfo" :column="2" border size="small">
        <el-descriptions-item label="真实姓名">{{ applyInfo.realName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ applyInfo.idCard }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(applyInfo.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag type="warning" size="small">{{ applyInfo.auditStatusText }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 已通过 -->
    <div v-else-if="status === 1" class="card state-card">
      <el-result icon="success" title="恭喜，入驻申请已通过！" sub-title="你现在已经是平台作者，可以创建并发布自己的小说作品了。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/author/dashboard')">进入作者中心</el-button>
        </template>
      </el-result>
    </div>

    <!-- 已驳回 -->
    <div v-else-if="status === 2" class="card state-card">
      <el-result icon="error" title="申请未通过" :sub-title="applyInfo?.auditReason || '请修改资料后重新提交'">
        <template #extra>
          <el-button type="primary" @click="showForm = true">修改后重新提交</el-button>
        </template>
      </el-result>
    </div>

    <!-- 申请表单（未申请 / 驳回后重新提交） -->
    <div v-if="status === null || (status === 2 && showForm)" class="card form-card">
      <h3 class="form-title">{{ status === 2 ? '重新提交申请' : '填写入驻资料' }}</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 520px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="用于实名认证（展示时脱敏）" maxlength="18" />
        </el-form-item>
        <el-form-item label="作者简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="介绍一下自己吧（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交申请</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apply, applyStatus } from '@/api/author'
import { useUserStore } from '@/store/user'
import { formatTime } from '@/utils/format'

const userStore = useUserStore()

// null=未申请 / 0=待审核 / 1=已通过 / 2=已驳回
const status = ref(null)
const applyInfo = ref(null)
const showForm = ref(false)
const submitting = ref(false)

const formRef = ref()
const form = reactive({ realName: '', idCard: '', intro: '' })

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

async function loadStatus() {
  try {
    const res = await applyStatus()
    if (res) {
      applyInfo.value = res
      status.value = res.auditStatus
      if (res.auditStatus === 1) {
        // 角色已升级，刷新本地用户信息
        userStore.refreshInfo()
      }
    }
  } catch (e) {
    // 404（未申请过）视为 null
    if (e && e.code === 404) {
      status.value = null
    } else {
      status.value = null
    }
  }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const res = await apply({ realName: form.realName, idCard: form.idCard, intro: form.intro || undefined })
    ElMessage.success('申请提交成功')
    applyInfo.value = res
    status.value = res.auditStatus
    showForm.value = false
  } catch (e) { /* 全局提示 */ } finally {
    submitting.value = false
  }
}

onMounted(loadStatus)
</script>

<style scoped>
.state-card {
  max-width: 720px;
}
.form-card {
  max-width: 720px;
  margin-top: 20px;
}
.form-title {
  margin: 0 0 20px;
}
</style>
