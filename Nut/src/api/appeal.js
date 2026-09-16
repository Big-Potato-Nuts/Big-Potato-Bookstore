import request from './request'

// 用户申诉（读者端）
export const createAppeal = (data) => request.post('/appeal/create', data)
export const myAppeals = (params) => request.get('/appeal/my', { params })
export const hasPendingAppeal = () => request.get('/appeal/has-pending')
