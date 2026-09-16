import request from './request'

// 作者端
export const apply = (data) => request.post('/author/apply', data)
export const applyStatus = () => request.get('/author/apply/status')
export const authorBooks = () => request.get('/author/book/list')
export const createBook = (data) => request.post('/author/book', data)
export const updateBook = (id, data) => request.put(`/author/book/${id}`, data)
export const updateBookStatus = (id, status) => request.put(`/author/book/${id}/status`, { status })
export const resubmitBook = (id) => request.post(`/author/book/${id}/resubmit`)
export const deleteBook = (id) => request.delete(`/author/book/${id}`)
export const authorChapters = (bookId) => request.get(`/author/book/${bookId}/chapter/list`)
export const createChapter = (data) => request.post('/author/chapter', data)
export const updateChapter = (id, data) => request.put(`/author/chapter/${id}`, data)
export const deleteChapter = (id) => request.delete(`/author/chapter/${id}`)
export const authorStats = () => request.get('/author/stats')
