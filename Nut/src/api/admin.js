import request from './request'

// 管理端
export const userPage = (params) => request.get('/admin/user/page', { params })
export const userDetail = (id) => request.get(`/admin/user/${id}`)
export const updateUserStatus = (id, status) => request.put(`/admin/user/${id}/status`, { status })
export const banUser = (id, data) => request.post(`/admin/user/${id}/ban`, data)
export const unbanUser = (id) => request.post(`/admin/user/${id}/unban`)
export const authorPage = (params) => request.get('/admin/author/page', { params })
export const auditAuthor = (id, data) => request.post(`/admin/author/${id}/audit`, data)
export const adminBookPage = (params) => request.get('/admin/book/page', { params })
export const updateBookStatus = (id, status) => request.put(`/admin/book/${id}/status`, { status })
// 书籍审核
export const pendingBookPage = (params) => request.get('/admin/book/pending', { params })
export const approveBook = (id) => request.post(`/admin/book/${id}/approve`)
export const rejectBook = (id, reason) => request.post(`/admin/book/${id}/reject`, null, { params: { reason } })
export const categoryList = () => request.get('/admin/category/list')
export const createCategory = (data) => request.post('/admin/category', data)
export const updateCategory = (id, data) => request.put(`/admin/category/${id}`, data)
export const deleteCategory = (id) => request.delete(`/admin/category/${id}`)
export const orderPage = (params) => request.get('/admin/order/page', { params })
export const adminOrderDetail = (orderNo) => request.get(`/admin/order/${orderNo}`)
export const commentPage = (params) => request.get('/admin/comment/page', { params })
export const updateCommentStatus = (id, status) => request.put(`/admin/comment/${id}/status`, { status })
export const deleteComment = (id) => request.delete(`/admin/comment/${id}`)
// 用户申诉管理
export const appealPage = (params) => request.get('/admin/appeal/page', { params })
export const handleAppeal = (id, data) => request.post(`/admin/appeal/${id}/handle`, data)
