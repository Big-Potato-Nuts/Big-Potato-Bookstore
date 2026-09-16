import request from './request'

// 书架
export const listBookshelf = () => request.get('/bookshelf/list')
export const addBookshelf = (data) => request.post('/bookshelf/add', data)
export const updateProgress = (data) => request.put('/bookshelf/progress', data)
export const setCollect = (data) => request.put('/bookshelf/collect', data)
export const removeBookshelf = (bookId) => request.delete(`/bookshelf/${bookId}`)
