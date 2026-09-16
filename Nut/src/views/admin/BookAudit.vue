<template>
  <div class="admin-book-audit">
    <div class="filter-bar card">
      <el-input v-model="query.keyword" placeholder="书名 / 作者" clearable style="width: 220px" @keyup.enter="reload" @clear="reload">
        <template #append>
          <el-button :icon="Search" @click="reload" />
        </template>
      </el-input>
    </div>

    <div class="card" style="margin-top: 16px">
      <el-table v-loading="loading" :data="books" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="70">
          <template #default="{ row }">
            <img :src="row.cover || defaultCover" class="book-cover" alt="封面" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" min-width="110" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column label="简介" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.intro }}</template>
        </el-table-column>
        <el-table-column label="章节数" width="80">
          <template #default="{ row }">{{ row.chapterCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" type="success" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待审核书籍" :image-size="80" />
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

    <!-- 书籍详情弹窗 -->
    <el-dialog v-model="detailVisible" title="书籍详情" width="640px">
      <div v-if="detailBook" class="book-detail">
        <div class="detail-header">
          <img :src="detailBook.cover || defaultCover" class="detail-cover" />
          <div class="detail-info">
            <h3>{{ detailBook.title }}</h3>
            <p class="muted">作者：{{ detailBook.authorName }} | 分类：{{ detailBook.categoryName }}</p>
            <p class="muted">章节数：{{ detailBook.chapterCount ?? 0 }} | 提交时间：{{ formatTime(detailBook.createTime) }}</p>
          </div>
        </div>
        <div class="detail-section">
          <h4>简介</h4>
          <p>{{ detailBook.intro || '暂无简介' }}</p>
        </div>
        <div class="detail-section">
          <h4>章节目录</h4>
          <el-table v-loading="chaptersLoading" :data="chapters" size="small" max-height="300">
            <el-table-column prop="sort" label="序号" width="70" />
            <el-table-column prop="title" label="章节标题" />
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.isFree === 1 ? 'success' : 'warning'">{{ row.isFree === 1 ? '免费' : '付费' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" text type="primary" @click="viewChapter(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 章节正文弹窗 -->
    <el-dialog v-model="chapterVisible" :title="currentChapter?.title || '章节内容'" width="600px">
      <div v-if="chapterContent" class="chapter-content">{{ chapterContent }}</div>
      <div v-else-if="chapterLoading" class="loading-tip">加载中...</div>
    </el-dialog>

    <!-- 驳回原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回原因" width="480px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请填写驳回原因，将通知给作者"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { pendingBookPage, approveBook, rejectBook } from '@/api/admin'
import { chapters as bookChapters, chapterContent as chapterDetail } from '@/api/book'
import { formatTime } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'

const books = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })

const detailVisible = ref(false)
const detailBook = ref(null)
const chapters = ref([])
const chaptersLoading = ref(false)

const chapterVisible = ref(false)
const currentChapter = ref(null)
const chapterContent = ref('')
const chapterLoading = ref(false)

const rejectVisible = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')
const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    const res = await pendingBookPage(params)
    books.value = res?.records || []
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

async function viewDetail(row) {
  detailBook.value = row
  detailVisible.value = true
  chaptersLoading.value = true
  try {
    const res = await bookChapters(row.id)
    chapters.value = res || []
  } catch (e) {
    chapters.value = []
  } finally {
    chaptersLoading.value = false
  }
}

async function viewChapter(chapter) {
  currentChapter.value = chapter
  chapterVisible.value = true
  chapterLoading.value = true
  chapterContent.value = ''
  try {
    const res = await chapterDetail(chapter.id)
    chapterContent.value = res?.content || ''
  } catch (e) {
    chapterContent.value = '加载失败'
  } finally {
    chapterLoading.value = false
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(`确定通过《${row.title}》的审核吗？通过后读者即可看到该书。`, '审核通过', { type: 'success' })
  } catch (e) {
    return
  }
  try {
    await approveBook(row.id)
    ElMessage.success('已通过审核')
    load()
  } catch (e) { /* 全局提示 */ }
}

function reject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await rejectBook(rejectTarget.value.id, rejectReason.value)
    ElMessage.success('已驳回')
    rejectVisible.value = false
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
.book-cover {
  width: 36px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}
.book-detail .detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.detail-cover {
  width: 80px;
  height: 110px;
  object-fit: cover;
  border-radius: 6px;
}
.detail-info h3 {
  margin: 0 0 8px;
}
.detail-section {
  margin-top: 16px;
}
.detail-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #333;
}
.chapter-content {
  white-space: pre-wrap;
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
}
.loading-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
.muted {
  color: #909399;
  font-size: 13px;
}
</style>
