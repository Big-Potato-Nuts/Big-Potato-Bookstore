<template>
  <div class="home page-container">
    <!-- Hero 推荐位 -->
    <div v-if="hero" class="hero card" @click="$router.push(`/book/${hero.id}`)">
      <div class="hero-info">
        <h1 class="hero-title">{{ hero.title }}</h1>
        <p class="hero-sub">{{ hero.authorName || '佚名' }} · {{ hero.categoryName || '未分类' }} · {{ hero.statusText }}</p>
        <p class="hero-intro">{{ hero.intro }}</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click.stop="$router.push(`/book/${hero.id}`)">立即阅读</el-button>
          <el-button size="large" @click.stop="$router.push('/book')">进入书城</el-button>
        </div>
      </div>
      <img :src="hero.cover || defaultCover" class="hero-cover" alt="推荐封面" />
    </div>

    <!-- 分类入口 -->
    <div class="section-title">分类书库</div>
    <div class="cats">
      <div class="cat-item" @click="$router.push('/book')">全部</div>
      <div v-for="c in categories" :key="c.id" class="cat-item" @click="$router.push({ path: '/book', query: { categoryId: c.id } })">
        {{ c.name }}
      </div>
    </div>

    <!-- 编辑推荐 -->
    <div class="section-title">编辑推荐</div>
    <div v-if="recommends.length" class="book-grid">
      <BookCard v-for="b in recommends" :key="b.id" :book="b" />
    </div>
    <el-empty v-else description="暂无推荐作品" :image-size="90" />

    <!-- 排行榜 -->
    <div class="section-title">排行榜</div>
    <el-tabs v-model="rankType" class="rank-tabs" @tab-change="loadRank">
      <el-tab-pane label="人气榜" name="click" />
      <el-tab-pane label="收藏榜" name="collect" />
      <el-tab-pane label="更新榜" name="update" />
    </el-tabs>
    <div v-if="ranks.length" class="rank-list card">
      <div v-for="(b, i) in ranks" :key="b.id" class="rank-item" @click="$router.push(`/book/${b.id}`)">
        <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
        <img :src="b.cover || defaultCover" class="rank-cover" alt="封面" />
        <div class="rank-info">
          <div class="rank-title">{{ b.title }}</div>
          <div class="rank-meta muted">{{ b.authorName || '佚名' }} · {{ b.categoryName || '未分类' }}</div>
        </div>
        <span class="rank-stat muted">
          {{ rankType === 'collect' ? formatWords(b.collectCount) + ' 收藏' : rankType === 'update' ? formatTime(b.updateTime) : formatWords(b.clickCount) + ' 点击' }}
        </span>
      </div>
    </div>
    <el-empty v-else description="暂无数据" :image-size="90" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import BookCard from '@/components/BookCard.vue'
import { recommend, rank, pageBooks } from '@/api/book'
import { categoryList } from '@/api/book'
import { formatTime, formatWords } from '@/utils/format'
import defaultCover from '@/assets/default-cover.svg'

const categories = ref([])
const recommends = ref([])
const ranks = ref([])
const rankType = ref('click')

// Hero 取推荐第一位，无推荐时取最近更新榜第一名
const hero = computed(() => recommends.value[0] || ranks.value[0] || null)

async function loadRecommend() {
  try {
    recommends.value = (await recommend(8)) || []
  } catch (e) { /* 全局提示 */ }
}

async function loadRank() {
  try {
    ranks.value = (await rank(rankType.value, 10)) || []
  } catch (e) { /* 全局提示 */ }
}

async function loadCategories() {
  try {
    categories.value = (await categoryList()) || []
  } catch (e) { /* 全局提示 */ }
}

onMounted(async () => {
  // 无推荐时兜底展示
  await Promise.all([loadRecommend(), loadRank(), loadCategories()])
  if (!recommends.value.length) {
    try {
      const page = await pageBooks({ page: 1, size: 8 })
      recommends.value = page?.records || []
    } catch (e) { /* 忽略 */ }
  }
})
</script>

<style scoped>
.hero {
  display: flex;
  gap: 32px;
  align-items: center;
  background: linear-gradient(120deg, #2d2a54 0%, #4a3f9e 55%, #7c5cf0 100%);
  color: #fff;
  border-radius: 14px;
  padding: 32px;
  cursor: pointer;
  margin-top: 4px;
}
.hero-info {
  flex: 1;
  min-width: 0;
}
.hero-title {
  font-size: 30px;
  margin: 0 0 10px;
}
.hero-sub {
  opacity: 0.85;
  margin: 0 0 14px;
  font-size: 14px;
}
.hero-intro {
  opacity: 0.9;
  line-height: 1.8;
  margin: 0 0 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hero-actions :deep(.el-button--default) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}
.hero-cover {
  width: 180px;
  height: 240px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
  flex-shrink: 0;
}
.cats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.cat-item {
  background: #fff;
  border: 1px solid #e8e8ec;
  border-radius: 20px;
  padding: 8px 22px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.2s;
}
.cat-item:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.rank-tabs {
  margin-bottom: 4px;
}
.rank-list {
  padding: 8px 20px;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 8px;
  border-bottom: 1px solid #f2f2f4;
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
  width: 28px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #aaa;
  font-style: italic;
}
.rank-no.top {
  color: #e6a23c;
}
.rank-cover {
  width: 44px;
  height: 58px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}
.rank-info {
  flex: 1;
  min-width: 0;
}
.rank-title {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-meta {
  font-size: 12px;
  margin-top: 4px;
}
.rank-stat {
  font-size: 12px;
  flex-shrink: 0;
}
</style>
