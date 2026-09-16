<template>
  <el-dialog
    v-model="visible"
    title="余额充值"
    width="420px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="balance-tip">
      当前余额：<span class="balance">¥ {{ fenToYuan(userStore.balance) }}</span>
    </div>
    <div class="amounts">
      <div
        v-for="a in presets"
        :key="a"
        class="amount-item"
        :class="{ active: selected === a }"
        @click="selected = a; custom = ''"
      >
        {{ a }}元
      </div>
      <div class="amount-item custom" :class="{ active: selected === 0 }" @click="selected = 0">
        <el-input v-model="custom" placeholder="自定义" size="small" @focus="selected = 0" />
      </div>
    </div>
    <p class="muted tip">模拟充值：充值金额将直接计入账户余额，用于购买付费章节。</p>
    <template #footer>
      <el-button @click="appStore.closeRecharge()">取消</el-button>
      <el-button type="primary" :loading="loading" @click="doRecharge">确认充值</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { recharge } from '@/api/user'
import { fenToYuan } from '@/utils/format'

const appStore = useAppStore()
const userStore = useUserStore()

const visible = computed({
  get: () => appStore.rechargeVisible,
  set: (v) => {
    if (!v) appStore.closeRecharge()
  }
})

const presets = [10, 50, 100, 200]
const selected = ref(10)
const custom = ref('')
const loading = ref(false)

async function doRecharge() {
  const amountYuan = selected.value ? selected.value : Number(custom.value)
  if (!amountYuan || amountYuan <= 0) {
    ElMessage.warning('请输入正确的充值金额')
    return
  }
  loading.value = true
  try {
    const res = await recharge(Math.round(amountYuan * 100))
    userStore.setUser({ ...userStore.user, balance: res.balance })
    ElMessage.success(`充值成功，到账 ¥${fenToYuan(res.recharged)}`)
    appStore.closeRecharge()
  } catch (e) {
    /* 全局错误提示已处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.balance-tip {
  margin-bottom: 14px;
  font-size: 14px;
}
.balance {
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 18px;
}
.amounts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 8px;
}
.amount-item {
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.2s;
  background: #fff;
}
.amount-item:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.amount-item.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}
.amount-item.custom {
  padding: 0 6px;
}
.amount-item.custom :deep(.el-input__wrapper) {
  box-shadow: none;
}
.tip {
  font-size: 12px;
  margin: 6px 0 0;
}
</style>
