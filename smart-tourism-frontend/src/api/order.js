import request from './request'

// 创建订单
export function createOrder(data) {
  return request.post('/order/create', data)
}

// 获取订单列表
export function getOrderList(params) {
  return request.get('/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/order/detail/${id}`)
}

// 支付订单（模拟）
export function payOrder(orderNo) {
  return request.post(`/order/pay/${orderNo}`)
}

// 取消订单
export function cancelOrder(orderNo) {
  return request.post(`/order/cancel/${orderNo}`)
}
