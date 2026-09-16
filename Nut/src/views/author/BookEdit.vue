<template>
  <div class="author-book-edit">
    <div class="section-title">{{ isEdit ? '编辑作品' : '新建作品' }}</div>
    <div class="card form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 720px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 220px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面 URL" prop="cover">
          <el-input v-model="form.cover" placeholder="https://...（可选）" clearable />
        </el-form-item>
        <el-form-item label="封面预览">
          <img :src="form.cover || defaultCover" class="cover-preview" alt="封面预览" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="5" maxlength="2000" show-word-limit placeholder="介绍一下你的作品吧" />
        </el-form-item>

        <!-- 新建作品时显示章节编辑区域 -->
        <template v-if="!isEdit">
          <el-divider content-position="left">
            <span class="divider-title">章节内容（可选，也可创建后再添加）</span>
          </el-divider>

          <el-form-item>
            <div class="chapter-toolbar">
              <el-button type="primary" plain size="small" :icon="Plus" @click="addChapter">添加章节</el-button>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeTxtUpload"
                accept=".txt"
              >
                <el-button size="small" :icon="Upload">导入 TXT</el-button>
              </el-upload>
              <span class="muted hint">支持「第X章」格式自动拆分，默认全部免费</span>
            </div>
          </el-form-item>

          <div v-for="(ch, idx) in chapters" :key="idx" class="chapter-item">
            <div class="chapter-head">
              <span class="chapter-index">第 {{ idx + 1 }} 章</span>
              <el-button type="danger" text size="small" @click="removeChapter(idx)">删除</el-button>
            </div>
            <el-input
              v-model="ch.title"
              placeholder="章节标题，如：第一章 初入江湖"
              maxlength="100"
              class="chapter-title-input"
            />
            <el-input
              v-model="ch.content"
              type="textarea"
              :rows="6"
              placeholder="章节正文内容"
              class="chapter-content-input"
            />
            <div class="chapter-meta">
              <el-radio-group v-model="ch.isFree" size="small">
                <el-radio :value="1">免费</el-radio>
                <el-radio :value="0">付费</el-radio>
              </el-radio-group>
              <el-input-number
                v-if="ch.isFree === 0"
                v-model="ch.price"
                :min="1"
                :max="9999"
                size="small"
                placeholder="价格(分)"
                style="width: 140px; margin-left: 12px"
              />
              <span v-if="ch.isFree === 0" class="muted" style="margin-left: 8px">1元=100分</span>
            </div>
          </div>
          <el-empty v-if="chapters.length === 0" description="暂未添加章节，可创建后再管理章节" :image-size="60" />
        </template>

        <el-form-item style="margin-top: 24px">
          <el-button type="primary" :loading="saving" @click="submit">
            {{ isEdit ? '保存修改' : (chapters.length > 0 ? '创建作品并发布章节' : '创建作品') }}
          </el-button>
          <el-button @click="$router.push('/author/books')">返回</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- TXT 导入预览弹窗 -->
    <el-dialog v-model="importVisible" title="TXT 导入预览" width="640px">
      <div v-if="importPreview.length" class="import-preview">
        <p class="muted">共识别到 <strong>{{ importPreview.length }}</strong> 个章节，确认后将追加到当前作品：</p>
        <div class="import-list">
          <div v-for="(ch, idx) in importPreview" :key="idx" class="import-chapter-item">
            <span class="import-chapter-title">{{ idx + 1 }}. {{ ch.title }}</span>
            <span class="muted">{{ ch.content.length }} 字</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="未能识别到章节，请确保 TXT 中包含「第X章」格式的标题" :image-size="60" />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!importPreview.length" @click="confirmImport">确认导入到章节列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { createBook, createBookWithChapters, updateBook } from '@/api/author'
import { categoryList, bookDetail } from '@/api/book'
import defaultCover from '@/assets/default-cover.svg'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const bookId = computed(() => (route.params.id ? Number(route.params.id) : null))

const categories = ref([])
const saving = ref(false)
const formRef = ref()

const form = reactive({ title: '', categoryId: null, cover: '', intro: '' })

// 内联章节列表
const chapters = ref([])

// TXT 导入
const importVisible = ref(false)
const importPreview = ref([])

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function addChapter() {
  chapters.value.push({ title: '', content: '', isFree: 1, price: null })
}

function removeChapter(idx) {
  chapters.value.splice(idx, 1)
}

function beforeTxtUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target.result
    importPreview.value = parseChapters(text)
    importVisible.value = true
  }
  reader.readAsText(file, 'UTF-8')
  return false // 阻止自动上传
}

/** 前端解析 TXT，按章节标题拆分（与后端正则一致） */
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
      result.push({ title, content, isFree: 1, price: null })
    }
  }
  return result
}

function confirmImport() {
  chapters.value.push(...importPreview.value)
  importVisible.value = false
  importPreview.value = []
  ElMessage.success(`已导入 ${chapters.value.length} 章到章节列表`)
}

async function loadCategories() {
  try {
    categories.value = (await categoryList()) || []
  } catch (e) { /* 忽略 */ }
}

async function loadBook() {
  if (!bookId.value) return
  try {
    const b = await bookDetail(bookId.value)
    form.title = b.title || ''
    form.categoryId = b.categoryId ?? null
    form.cover = b.cover || ''
    form.intro = b.intro || ''
  } catch (e) { /* 全局提示 */ }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  // 校验章节
  for (let i = 0; i < chapters.value.length; i++) {
    const ch = chapters.value[i]
    if (!ch.title.trim()) {
      ElMessage.warning(`第 ${i + 1} 章标题不能为空`)
      return
    }
    if (!ch.content.trim()) {
      ElMessage.warning(`第 ${i + 1} 章「${ch.title}」正文不能为空`)
      return
    }
    if (ch.isFree === 0 && (!ch.price || ch.price <= 0)) {
      ElMessage.warning(`第 ${i + 1} 章「${ch.title}」为付费章节，请设置价格`)
      return
    }
  }

  saving.value = true
  const payload = {
    title: form.title,
    categoryId: form.categoryId,
    cover: form.cover || undefined,
    intro: form.intro
  }
  try {
    if (isEdit.value) {
      await updateBook(bookId.value, payload)
      ElMessage.success('作品已更新')
    } else if (chapters.value.length > 0) {
      await createBookWithChapters({
        ...payload,
        chapters: chapters.value.map(ch => ({
          title: ch.title.trim(),
          content: ch.content.trim(),
          isFree: ch.isFree,
          price: ch.isFree === 0 ? ch.price : undefined
        }))
      })
      ElMessage.success(`创建成功，已发布 ${chapters.value.length} 章`)
    } else {
      await createBook(payload)
      ElMessage.success('创建成功，去添加章节吧')
    }
    router.push('/author/books')
  } catch (e) { /* 全局提示 */ } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadBook()
})
</script>

<style scoped>
.form-card {
  max-width: 800px;
}
.cover-preview {
  width: 90px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}
.divider-title {
  font-size: 14px;
  font-weight: 600;
}
.chapter-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hint {
  font-size: 12px;
}
.chapter-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafafa;
}
.chapter-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.chapter-index {
  font-weight: 600;
  color: var(--el-color-primary);
}
.chapter-title-input {
  margin-bottom: 10px;
}
.chapter-content-input {
  margin-bottom: 10px;
}
.chapter-meta {
  display: flex;
  align-items: center;
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
