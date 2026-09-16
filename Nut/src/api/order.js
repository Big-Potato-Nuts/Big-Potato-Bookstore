import request from './request'

// 订单
export const createOrder = (data) => request.post('/order/create', data)
export const payOrder = (orderNo) => request.post('/order/pay', { orderNo })
export const myOrders = (params) => request.get('/order/my', { params })
export const orderDetail = (orderNo) => request.get(`/order/${orderNo}`)
