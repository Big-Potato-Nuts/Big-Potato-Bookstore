import request from './request'

// 分类
export const categoryList = () => request.get('/category/list')

// 小说
export const pageBooks = (params) => request.get('/book/page', { params })
export const recommend = (limit = 10) => request.get('/book/recommend', { params: { limit } })
export const rank = (type = 'click', limit = 10) => request.get('/book/rank', { params: { type, limit } })
export const bookDetail = (id) => request.get(`/book/${id}`)
export const chapters = (id) => request.get(`/book/${id}/chapters`)
export const chapterContent = (id) => request.get(`/chapter/${id}`)
