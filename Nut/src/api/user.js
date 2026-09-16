import request from './request'

// 用户模块
export const updateProfile = (data) => request.put('/user/profile', data)
export const updatePassword = (data) => request.put('/user/password', data)
export const recharge = (amount) => request.post('/user/recharge', { amount })
export const getBalance = () => request.get('/user/balance')
