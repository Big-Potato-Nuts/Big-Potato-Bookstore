<template>
  <div class="author-book-edit">
    <div class="section-title">{{ isEdit ? '编辑作品' : '新建作品' }}</div>
    <div class="card form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 640px">
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
          <el-input v-model="form.intro" type="textarea" :rows="6" maxlength="2000" show-word-limit placeholder="介绍一下你的作品吧" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">{{ isEdit ? '保存修改' : '创建作品' }}</el-button>
          <el-button @click="$router.push('/author/books')">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createBook, updateBook } from '@/api/author'
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

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
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
  max-width: 720px;
}
.cover-preview {
  width: 90px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}
</style>
