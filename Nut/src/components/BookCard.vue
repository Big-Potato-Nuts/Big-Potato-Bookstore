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
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.22s, box-shadow 0.22s;
}
.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 26px rgba(0, 0, 0, 0.12);
}
.cover-wrap {
  position: relative;
  aspect-ratio: 3 / 4;
  background: #f0f0f2;
  overflow: hidden;
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}
.book-card:hover .cover {
  transform: scale(1.04);
}
/* 推荐角标：七猫红渐变 */
.rec-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--nut-gradient);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 9px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(255, 78, 69, 0.4);
}
.status-tag {
  position: absolute;
  bottom: 8px;
  right: 8px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.info {
  padding: 10px 12px 14px;
}
.title {
  font-size: 15px;
  font-weight: 700;
  color: #2b2b2b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.author {
  font-size: 12px;
  color: #8a8a90;
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  font-size: 12px;
  display: flex;
  gap: 10px;
  color: #a8abb2;
}
.intro {
  font-size: 12px;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #a8abb2;
}
</style>
