<template>
  <div class="author-chapters">
    <div class="toolbar">
      <div class="section-title no-margin">
        <el-button text @click="$router.push('/author/books')">
          <el-icon><ArrowLeft /></el-icon>&nbsp;返回
        </el-button>
        {{ bookTitle }} · 章节管理
      </div>
      <div class="toolbar-right">
        <el-upload
          :show-file-list="false"
          :before-upload="beforeTxtUpload"
          accept=".txt"
        >
          <el-button :icon="Upload">导入 TXT</el-button>
        </el-upload>
        <el-button type="primary" :icon="Plus" @click="$router.push(`/author/books/${bookId}/chapters/new`)">新建章节</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="chapters" stripe style="width: 100%">
      <el-table-column prop="sort" label="序号" width="80" />
      <el-table-column prop="title" label="章节标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="属性" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.isFree === 1" type="success" size="small">免费</el-tag>
          <el-tag v-else type="danger" size="small">付费 ¥{{ fenToYuan(row.price) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="字数" width="90">
        <template #default="{ row }">{{ formatWords(row.words) }}</template>
      </el-table-column>
      <el-table-column label="阅读" width="90">
        <template #default="{ row }">{{ formatWords(row.readCount) }}</template>
      </el-table-column>
      <el-table-column label="付费阅读" width="100">
        <template #default="{ row }">{{ formatWords(row.payCount) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/author/chapters/${row.id}/edit?bookId=${bookId}`)">编辑</el-button>
          <el-button size="small" type="danger" text @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="还没有章节，点击右上角“新建章节”开始创作">
          <el-button type="primary" @click="$router.push(`/author/books/${bookId}/chapters/new`)">新建章节</el-button>
        </el-empty>
      </template>
    </el-table>

    <!-- TXT 导入预览弹窗 -->
    <el-dialog v-model="importVisible" title="TXT 导入预览" width="640px">
      <div v-if="importPreview.length" class="import-preview">
        <p class="muted">共识别到 <strong>{{ importPreview.length }}</strong> 个章节，确认后将追加到本书：</p>
        <div class="import-list">
          <div v-for="(ch, idx) in importPreview" :key="idx" class="import-chapter-item">
            <span class="import-chapter-title">{{ idx + 1 }}. {{ ch.title }}</span>
            <span class="muted">{{ ch.content.length }} 字</span>
          </div>
        </div>
        <div class="import-settings" style="margin-top: 16px">
          <el-radio-group v-model="importIsFree" size="small">
            <el-radio :value="1">全部免费</el-radio>
            <el-radio :value="0">全部付费</el-radio>
          </el-radio-group>
          <el-input-number
            v-if="importIsFree === 0"
            v-model="importPrice"
            :min="1"
            :max="9999"
            size="small"
            placeholder="价格(分)"
            style="width: 140px; margin-left: 12px"
          />
        </div>
      </div>
      <el-empty v-else description="未能识别到章节，请确保 TXT 中包含「第X章」格式的标题" :image-size="60" />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!importPreview.length" :loading="importing" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { authorChapters, deleteChapter, importChapters } from '@/api/author'
import { bookDetail } from '@/api/book'
import { fenToYuan, formatTime, formatWords } from '@/utils/format'

const route = useRoute()
const bookId = Number(route.params.id)

const chapters = ref([])
const bookTitle = ref('')
const loading = ref(false)

// TXT 导入
const importVisible = ref(false)
const importPreview = ref([])
const importIsFree = ref(1)
const importPrice = ref(null)
const importing = ref(false)

function beforeTxtUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target.result
    importPreview.value = parseChapters(text)
    importIsFree.value = 1
    importPrice.value = null
    importVisible.value = true
  }
  reader.readAsText(file, 'UTF-8')
  return false
}

function parseChapters(text) {
  const result = []
  if (!text) return result
  const normalized = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  const regex = /^(第[一二三四五六七八九十百千万零〇\d]+[章节卷回集部篇]|Chapter\s+\d+|第\d+章).*$/gm
  const matches = []
  let m
  while ((m = regex.exec(normalized)) !== null) {
    matches.push({ start: m.index, end: m.index + m[0].length })
  }
  if (matches.length === 0) return result
  for (let i = 0; i < matches.length; i++) {
    const titleStart = matches[i].start
    const titleEnd = matches[i].end
    const contentEnd = i + 1 < matches.length ? matches[i + 1].start : normalized.length
    const title = normalized.substring(titleStart, titleEnd).trim()
    const content = normalized.substring(titleEnd, contentEnd).trim()
    if (content) {
      result.push({ title, content })
    }
  }
  return result
}

async function confirmImport() {
  if (importIsFree.value === 0 && (!importPrice.value || importPrice.value <= 0)) {
    ElMessage.warning('付费章节请设置价格')
    return
  }
  importing.value = true
  try {
    const count = await importChapters({
      bookId,
      text: importPreview.value.map(ch => ch.title + '\n' + ch.content).join('\n\n'),
      isFree: importIsFree.value,
      price: importIsFree.value === 0 ? importPrice.value : null
    })
    ElMessage.success(`成功导入 ${count} 章`)
    importVisible.value = false
    importPreview.value = []
    load()
  } catch (e) { /* 全局提示 */ } finally {
    importing.value = false
  }
}

async function load() {
  loading.value = true
  try {
    chapters.value = (await authorChapters(bookId)) || []
  } catch (e) { /* 全局提示 */ } finally {
    loading.value = false
  }
}

async function loadBook() {
  try {
    const b = await bookDetail(bookId)
    bookTitle.value = b?.title || ''
  } catch (e) { /* 忽略 */ }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除章节「${row.title}」吗？删除后不可恢复。`, '危险操作', {
      type: 'error',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch (e) {
    return
  }
  try {
    await deleteChapter(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* 全局提示 */ }
}

onMounted(() => {
  load()
  loadBook()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar-right {
  display: flex;
  gap: 10px;
}
.no-margin {
  margin: 0;
}
.import-preview p {
  margin: 0 0 12px;
}
.import-list {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 8px;
}
.import-chapter-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.import-chapter-item:last-child {
  border-bottom: none;
}
.import-chapter-title {
  font-weight: 500;
}
</style>
