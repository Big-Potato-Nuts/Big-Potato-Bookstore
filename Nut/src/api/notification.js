import request from './request'

// 通知
export const listNotifications = (params) => request.get('/notification/list', { params })
export const unreadCount = () => request.get('/notification/unread-count')
export const readNotification = (id) => request.put(`/notification/${id}/read`)
export const readAll = () => request.put('/notification/read-all')
