import request from './request'

// 认证模块
export const register = (data) => request.post('/auth/register', data)
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getInfo = () => request.get('/auth/info')
