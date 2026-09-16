<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <div class="cover-wrap">
      <img :src="book.cover || defaultCover" :alt="book.title" class="cover" loading="lazy" />
      <span v-if="book.isRecommend === 1" class="rec-tag">推荐</span>
      <span v-if="book.status !== undefined" class="status-tag">{{ book.statusText || '' }}</span>
    </div>
    <div class="info">
      <div class="title" :title="book.title">{{ book.title }}</div>
      <div class="author" :title="book.authorName">{{ book.authorName || '佚名' }}</div>
      <div class="meta muted">
        <span>{{ book.categoryName || '未分类' }}</span>
        <span v-if="book.chapterCount !== undefined">{{ book.chapterCount }}章</span>
        <span v-if="book.clickCount !== undefined">{{ formatWords(book.clickCount) }}人在读</span>
      </div>
      <div v-if="book.intro" class="intro muted">{{ book.intro }}</div>
    </div>
  </div>
</template>

<script setup>
import defaultCover from '@/assets/default-cover.svg'
import { formatWords } from '@/utils/format'

defineProps({
  book: { type: Object, required: true }
})
</script>

<style scoped>
.book-card {
  cursor: pointer;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}
.cover-wrap {
  position: relative;
  aspect-ratio: 3 / 4;
  background: #f0f0f2;
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.rec-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #ff6a3d, #ff2e63);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-tag {
  position: absolute;
  bottom: 8px;
  right: 8px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.info {
  padding: 10px 12px 14px;
}
.title {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.author {
  font-size: 13px;
  color: #666;
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  font-size: 12px;
  display: flex;
  gap: 10px;
}
.intro {
  font-size: 12px;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
