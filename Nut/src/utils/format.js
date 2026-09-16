// 通用格式化工具

/** 分 → 元（保留两位小数） */
export function fenToYuan(fen) {
  if (fen === null || fen === undefined || fen === '') return '0.00'
  return (Number(fen) / 100).toFixed(2)
}

function pad(n) {
  return String(n).padStart(2, '0')
}

/** ISO-8601 → YYYY-MM-DD HH:mm */
export function formatTime(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** ISO-8601 → YYYY-MM-DD */
export function formatDate(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 数字千分位 */
export function formatWords(n) {
  const num = Number(n)
  if (!Number.isFinite(num)) return '0'
  return num.toLocaleString('zh-CN')
}

// 状态文案与标签映射
export const bookStatusMap = { 0: '已下架', 1: '连载中', 2: '已完结', 3: '待审核' }
export const bookStatusTagMap = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }
export const payStatusMap = { 0: '待支付', 1: '已支付', 2: '已关闭', 3: '已退款' }
export const payStatusTagMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
export const auditStatusMap = { 0: '待审核', 1: '已通过', 2: '已驳回' }
export const auditStatusTagMap = { 0: 'warning', 1: 'success', 2: 'danger' }
export const roleMap = { 0: '读者', 1: '作者', 2: '管理员' }
export const orderTypeMap = { 1: '单章', 2: '整本' }
export const commentStatusMap = { 0: '已屏蔽', 1: '正常' }
export const notificationTypeMap = { PURCHASE: '购买', AUDIT: '审核', SYSTEM: '系统' }
