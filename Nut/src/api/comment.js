import request from './request'

// 评论
export const bookComments = (bookId, params) => request.get(`/book/${bookId}/comment`, { params })
export const addComment = (bookId, content) => request.post(`/book/${bookId}/comment`, { content })
export const myComments = (params) => request.get('/comment/my', { params })
