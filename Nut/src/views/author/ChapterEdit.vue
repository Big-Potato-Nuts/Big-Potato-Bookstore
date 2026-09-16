<template>
  <div class="author-chapter-edit">
    <div class="section-title">{{ isEdit ? '编辑章节' : '新建章节' }}</div>
    <div class="card form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="form.title" placeholder="如：第一章 初入江湖" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="章节序号" prop="sort">
          <el-input-number v-model="form.sort" :min="1" :max="9999" />
          <span class="muted hint">按 1、2、3… 顺序排列</span>
        </el-form-item>
        <el-form-item label="付费属性" prop="isFree">
          <el-radio-group v-model="form.isFree">
            <el-radio :value="1">免费</el-radio>
            <el-radio :value="0">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.isFree === 0" label="价格（分）" prop="price">
          <el-input-number v-model="form.price" :min="1" :max="100000" />
          <span class="muted hint">金额单位：分（如 50 = 0.50 元）</span>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="18"
            maxlength="200000"
            show-word-limit
            placeholder="请输入章节正文，支持分段换行"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">{{ isEdit ? '保存修改' : '发布章节' }}</el-button>
          <el-button @click="$router.push(`/author/books/${bookId}/chapters`)">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createChapter, updateChapter, authorChapters } from '@/api/author'
import { chapterContent } from '@/api/book'

const route = useRoute()
const router = useRouter()

// 编辑章节路由 /author/chapters/:id/edit 才进入编辑模式；
// 新建章节路由 /author/books/:id/chapters/new 的 params.id 是 bookId，不能据此判定编辑
const isEdit = computed(() => route.name === 'authorChapterEdit')
const chapterId = computed(() => (isEdit.value ? Number(route.params.id) : null))
const bookId = computed(() => Number(route.query.bookId || route.params.id))

const saving = ref(false)
const formRef = ref()

const form = reactive({ title: '', sort: 1, isFree: 1, price: 50, content: '' })

const rules = {
  title: [{ required: true, message: '请输入章节标题', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入章节序号', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }],
  price: [
    {
      validator: (rule, value, cb) => {
        if (form.isFree === 0 && (!value || value <= 0)) cb(new Error('付费章节请输入正确的价格'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function loadChapter() {
  if (!isEdit.value) return
  try {
    // 作者本人可通过公开章节接口读取自己作品的正文
    const data = await chapterContent(chapterId.value)
    form.title = data.title || ''
    form.sort = data.sort || 1
    form.isFree = data.isFree
    form.price = data.price || 50
    form.content = data.content || ''
  } catch (e) {
    // 作品下架等场景正文接口不可用：退回章节列表元数据
    try {
      const list = (await authorChapters(bookId.value)) || []
      const cur = list.find((c) => Number(c.id) === chapterId.value)
      if (cur) {
        form.title = cur.title || ''
        form.sort = cur.sort || 1
        form.isFree = cur.isFree
        form.price = cur.price || 50
      }
    } catch (e2) { /* 忽略 */ }
  }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('正文内容不能为空')
    return
  }
  saving.value = true
  const payload = {
    bookId: bookId.value,
    title: form.title,
    content: form.content,
    isFree: form.isFree,
    price: form.isFree === 0 ? form.price : undefined,
    sort: form.sort
  }
  try {
    if (isEdit.value) {
      await updateChapter(chapterId.value, payload)
      ElMessage.success('章节已更新')
    } else {
      await createChapter(payload)
      ElMessage.success('章节发布成功')
    }
    router.push(`/author/books/${bookId.value}/chapters`)
  } catch (e) { /* 全局提示 */ } finally {
    saving.value = false
  }
}

onMounted(loadChapter)
</script>

<style scoped>
.form-card {
  max-width: 820px;
}
.hint {
  margin-left: 10px;
  font-size: 12px;
}
</style>
