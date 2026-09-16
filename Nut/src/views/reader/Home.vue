<template>
  <div class="home page-container">
    <!-- ========== ① 轮播 Banner（七猫风格） ========== -->
    <div v-if="banners.length" class="banner-wrap">
      <div
        v-for="(b, i) in banners"
        :key="b.id"
        class="banner"
        :class="{ active: i === activeIndex }"
        :style="{ background: banners[i]._gradient }"
        @click="$router.push(`/book/${b.id}`)"
      >
        <div class="banner-info">
          <h2 class="banner-title">{{ b.title }}</h2>
          <p class="banner-sub">{{ b.authorName || '佚名' }} · {{ b.categoryName || '未分类' }} · {{ b.statusText || '连载中' }}</p>
          <p class="banner-intro">{{ b.intro }}</p>
          <div class="banner-actions">
            <button class="banner-btn primary" @click.stop="$router.push(`/book/${b.id}`)">立即阅读</button>
            <button class="banner-btn ghost" @click.stop="$router.push('/book')">进入书城</button>
          </div>
        </div>
        <img :src="b.cover || defaultCover" class="banner-cover" alt="封面" />
      </div>

      <!-- 左右切换 -->
      <button class="banner-arrow left" @click.stop="prevSlide">‹</button>
      <button class="banner-arrow right" @click.stop="nextSlide">›</button>

      <!-- 指示点 -->
      <div class="banner-dots">
        <span
          v-for="(b, i) in banners"
          :key="'dot' + b.id"
          class="dot"
          :class="{ on: i === activeIndex }"
          @click.stop="activeIndex = i"
        />
      </div>
    </div>
    <div v-else class="banner-wrap">
      <div class="banner active" :style="{ background: bannerGradients[0] }">
        <div class="banner-info">
          <h2 class="banner-title">在线小说书城</h2>
          <p class="banner-sub">匠心打磨好作品 · 海量小说免费阅读</p>
          <p class="banner-intro">发现好书，从书城首页开始。按分类浏览、查看排行榜、收藏到书架，随时继续阅读。</p>
          <div class="banner-actions">
            <button class="banner-btn primary" @click="$router.push('/book')">进入书城</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== ② 快捷入口 ========== -->
    <div class="quick-entries">
      <div class="quick-card" @click="goApply">
        <span class="quick-icon" style="background: linear-gradient(135deg, #ffb25a, #ff7a3d)">
          <el-icon :size="26"><EditPen /></el-icon>
        </span>
        <div class="quick-info">
          <div class="quick-title">作家专区</div>
          <div class="quick-sub">高价保底 · 无责全勤</div>
        </div>
        <span class="quick-arrow">›</span>
      </div>
      <div class="quick-card" @click="$router.push('/book')">
        <span class="quick-icon" style="background: linear-gradient(135deg, #ff7a70, #ff4e45)">
          <el-icon :size="26"><Reading /></el-icon>
        </span>
        <div class="quick-info">
          <div class="quick-title">免费书城</div>
          <div class="quick-sub">海量小说 · 免费试读</div>
        </div>
        <span class="quick-arrow">›</span>
      </div>
      <div class="quick-card" @click="goNotifications">
        <span class="quick-icon" style="background: linear-gradient(135deg, #9a7bff, #6c5ce7)">
          <el-icon :size="26"><Bell /></el-icon>
        </span>
        <div class="quick-info">
          <div class="quick-title">消息动态</div>
          <div class="quick-sub">
            {{ userStore.isLogin ? (appStore.unreadCount ? appStore.unreadCount + ' 条未读消息' : '暂无未读消息') : '登录后查看' }}
          </div>
        </div>
        <span class="quick-arrow">›</span>
      </div>
    </div>

    <!-- ========== ③ 分类书库（女频 / 男频） ========== -->
    <div class="section-title">分类书库</div>
    <div class="card cats-card">
      <div class="cat-row">
        <span class="cat-label female">女生原创</span>
        <div class="cat-chips">
          <span
            v-for="c in femaleCats"
            :key="c.id"
            class="cat-chip"
            @click="$router.push({ path: '/book', query: { categoryId: c.id } })"
          >{{ c.name }}</span>
        </div>
      </div>
      <div class="cat-row">
        <span class="cat-label male">男生原创</span>
        <div class="cat-chips">
          <span
            v-for="c in maleCats"
            :key="c.id"
            class="cat-chip"
            @click="$router.push({ path: '/book', query: { categoryId: c.id } })"
          >{{ c.name }}</span>
        </div>
      </div>
    </div>

    <!-- ========== ④ 排行榜（大热榜 / 新书榜 × 女频 / 男频） ========== -->
    <div class="section-title" id="rank">排行榜</div>
    <div class="rank-grid">
      <div v-for="col in rankColumns" :key="col.key" class="card rank-card">
        <div class="rank-head">
          <span class="rank-name" :class="{ female: col.female, male: !col.female }">{{ col.name }}</span>
          <span class="rank-type">{{ col.type }}</span>
        </div>
        <div v-if="col.items.length" class="rank-list">
          <div
            v-for="(b, i) in col.items"
            :key="b.id"
            class="rank-item"
            @click="$router.push(`/book/${b.id}`)"
          >
            <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
            <div class="rank-info">
              <div class="rank-title">{{ b.title }}</div>
              <div class="rank-meta muted">{{ b.authorName || '佚名' }}</div>
            </div>
            <span class="rank-stat muted">
              {{ col.type === '新书榜' ? formatTime(b.updateTime) : formatWords(b.clickCount) + ' 点击' }}
            </span>
          </div>
        </div>
        <div v-else class="rank-empty muted">暂无数据</div>
      </div>
    </div>

    <!-- ========== ⑤ 编辑推荐 ========== -->
    <div class="section-title">编辑推荐</div>
    <div v-if="recommends.length" class="book-grid">
      <BookCard v-for="b in recommends" :key="b.id" :book="b" />
    </div>
    <el-empty v-else description="暂无推荐作品" :image-size="90" />
  </div>

  <!-- ========== ⑥ 右侧悬浮侧边栏 ========== -->
  <div class="float-side">
    <div class="float-item" @click="goApply">
      <el-icon :size="20"><EditPen /></el-icon>
      <span>作家入驻</span>
    </div>
    <div class="float-item" @click="goShelf">
      <el-icon :size="20"><Notebook /></el-icon>
      <span>我的书架</span>
    </div>
    <div class="float-item" @click="scrollTop">
      <el-icon :size="20"><Top /></el-icon>
      <span>回到顶部</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditPen, Reading, Bell, Notebook, Top } from '@element-plus/icons-vue'
import BookCard from '@/components/BookCard.vue'
import { recommend, rank, pageBooks, categoryList } from '@/api/book'
import { formatTime, formatWords } from '@/utils/format'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import defaultCover from '@/assets/default-cover.svg'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const categories = ref([])
const recommends = ref([])
const clickRanks = ref([])
const updateRanks = ref([])
const activeIndex = ref(0)

// Banner 渐变配色（七猫红橙系）
const bannerGradients = [
  'linear-gradient(120deg, #ff6a3d 0%, #ff4e45 55%, #e8352e 100%)',
  'linear-gradient(120deg, #ff8a3d 0%, #ff5a45 55%, #f43b3b 100%)',
  'linear-gradient(120deg, #ff5e62 0%, #ff4e78 55%, #d6336c 100%)',
  'linear-gradient(120deg, #ff9c2e 0%, #ff6a3d 55%, #f43b3b 100%)'
]

// 轮播取推荐位前 4 本
const banners = computed(() =>
  recommends.value.slice(0, 4).map((b, i) => ({ ...b, _gradient: bannerGradients[i % bannerGradients.length] }))
)

// 女频 / 男频分类（演示口径：言情归女频，其余归男频，可按需调整）
const femaleCats = computed(() => categories.value.filter((c) => c.name === '言情'))
const maleCats = computed(() => categories.value.filter((c) => c.name !== '言情'))

const isFemaleBook = (b) => b.categoryName === '言情'

// 排行榜四栏：大热榜/新书榜 × 女频/男频
const rankColumns = computed(() => [
  { key: 'hot-f', name: '大热榜', type: '女频', female: true, items: clickRanks.value.filter(isFemaleBook).slice(0, 5) },
  { key: 'hot-m', name: '大热榜', type: '男频', female: false, items: clickRanks.value.filter((b) => !isFemaleBook(b)).slice(0, 5) },
  { key: 'new-f', name: '新书榜', type: '女频', female: true, items: updateRanks.value.filter(isFemaleBook).slice(0, 5) },
  { key: 'new-m', name: '新书榜', type: '男频', female: false, items: updateRanks.value.filter((b) => !isFemaleBook(b)).slice(0, 5) }
])

let timer = null

function nextSlide() {
  if (banners.value.length < 2) return
  activeIndex.value = (activeIndex.value + 1) % banners.value.length
}
function prevSlide() {
  if (banners.value.length < 2) return
  activeIndex.value = (activeIndex.value - 1 + banners.value.length) % banners.value.length
}
function startAutoPlay() {
  timer = setInterval(nextSlide, 4500)
}

async function loadRecommend() {
  try {
    recommends.value = (await recommend(8)) || []
  } catch (e) { /* 全局提示 */ }
}

async function loadRank() {
  try {
    const [c, u] = await Promise.all([
      rank('click', 30),
      rank('update', 30)
    ])
    clickRanks.value = c || []
    updateRanks.value = u || []
  } catch (e) { /* 全局提示 */ }
}

async function loadCategories() {
  try {
    categories.value = (await categoryList()) || []
  } catch (e) { /* 全局提示 */ }
}

function goApply() {
  if (userStore.isAuthor) router.push('/author/dashboard')
  else if (userStore.isLogin) router.push('/author-apply')
  else router.push('/login')
}
function goShelf() {
  if (userStore.isLogin) router.push('/bookshelf')
  else router.push('/login')
}
function goNotifications() {
  if (userStore.isLogin) router.push('/notifications')
  else router.push('/login')
}
function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  await Promise.all([loadRecommend(), loadRank(), loadCategories()])
  // 无推荐时兜底展示
  if (!recommends.value.length) {
    try {
      const page = await pageBooks({ page: 1, size: 8 })
      recommends.value = page?.records || []
    } catch (e) { /* 忽略 */ }
  }
  startAutoPlay()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.home {
  padding-top: 18px;
}

/* ===== ① Banner ===== */
.banner-wrap {
  position: relative;
  height: 330px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(255, 78, 69, 0.18);
}
.banner {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 44px 56px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.6s ease;
  pointer-events: none;
  cursor: pointer;
}
.banner.active {
  opacity: 1;
  pointer-events: auto;
}
.banner-info {
  flex: 1;
  min-width: 0;
}
.banner-title {
  font-size: 34px;
  font-weight: 800;
  margin: 0 0 12px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
}
.banner-sub {
  opacity: 0.9;
  margin: 0 0 12px;
  font-size: 14px;
  letter-spacing: 0.5px;
}
.banner-intro {
  opacity: 0.92;
  line-height: 1.8;
  margin: 0 0 24px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
}
.banner-actions {
  display: flex;
  gap: 14px;
}
.banner-btn {
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  padding: 10px 30px;
  cursor: pointer;
  transition: all 0.2s;
}
.banner-btn.primary {
  background: #fff;
  color: var(--el-color-primary);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
}
.banner-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.24);
}
.banner-btn.ghost {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.45);
}
.banner-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.28);
}
.banner-cover {
  width: 176px;
  height: 236px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.35);
  flex-shrink: 0;
  transform: rotate(1.5deg);
}
.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 2;
}
.banner-arrow:hover {
  background: rgba(255, 255, 255, 0.38);
}
.banner-arrow.left {
  left: 16px;
}
.banner-arrow.right {
  right: 16px;
}
.banner-dots {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 8px;
  z-index: 2;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: all 0.25s;
}
.dot.on {
  width: 22px;
  border-radius: 4px;
  background: #fff;
}

/* ===== ② 快捷入口 ===== */
.quick-entries {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 18px;
}
.quick-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: var(--nut-card-shadow);
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.1);
}
.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}
.quick-info {
  flex: 1;
  min-width: 0;
}
.quick-title {
  font-size: 16px;
  font-weight: 700;
}
.quick-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.quick-arrow {
  color: #c8c8ce;
  font-size: 22px;
  font-weight: 300;
}

/* ===== ③ 分类书库 ===== */
.cats-card {
  padding: 20px 24px;
}
.cat-row {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 10px 0;
}
.cat-row + .cat-row {
  border-top: 1px dashed #eeeef2;
}
.cat-label {
  width: 84px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 700;
  padding-top: 7px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cat-label::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
}
.cat-label.female::before {
  background: linear-gradient(180deg, #ff7a9e, #ff4e78);
}
.cat-label.male::before {
  background: linear-gradient(180deg, #5b8def, #3d6de8);
}
.cat-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.cat-chip {
  background: #f7f7f9;
  border: 1px solid #f0f0f3;
  color: #555;
  border-radius: 16px;
  padding: 6px 18px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.cat-chip:hover {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

/* ===== ④ 排行榜 ===== */
.rank-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.rank-card {
  padding: 18px 18px 8px;
}
.rank-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.rank-name {
  font-size: 17px;
  font-weight: 800;
}
.rank-name.female {
  color: #ff4e78;
}
.rank-name.male {
  color: #3d6de8;
}
.rank-type {
  font-size: 12px;
  color: #909399;
}
.rank-list {
  margin-top: 6px;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 4px;
  border-bottom: 1px solid #f4f4f6;
  cursor: pointer;
  transition: background 0.15s;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-item:hover {
  background: #fafafa;
}
.rank-no {
  width: 22px;
  text-align: center;
  font-size: 15px;
  font-weight: 800;
  color: #c4c4ca;
  font-style: italic;
  flex-shrink: 0;
}
.rank-no.top {
  color: var(--nut-gold);
}
.rank-no.top:nth-child(1) {
  color: #ff4e45;
}
.rank-info {
  flex: 1;
  min-width: 0;
}
.rank-title {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-meta {
  font-size: 12px;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-stat {
  font-size: 11px;
  flex-shrink: 0;
}
.rank-empty {
  text-align: center;
  padding: 40px 0;
  font-size: 13px;
}

/* ===== ⑥ 右侧悬浮侧边栏 ===== */
.float-side {
  position: fixed;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 90;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}
.float-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 10px;
  font-size: 11px;
  color: #666;
  cursor: pointer;
  border-bottom: 1px solid #f4f4f6;
  transition: all 0.2s;
  user-select: none;
}
.float-item:last-child {
  border-bottom: none;
}
.float-item:hover {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

/* 响应式 */
@media (max-width: 1100px) {
  .rank-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .banner {
    padding: 34px 40px;
  }
}
@media (max-width: 768px) {
  .quick-entries {
    grid-template-columns: 1fr;
  }
  .rank-grid {
    grid-template-columns: 1fr;
  }
  .banner-cover {
    display: none;
  }
  .float-side {
    display: none;
  }
}
</style>
