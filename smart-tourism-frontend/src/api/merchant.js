import request from './request'

// 商家认证相关
export function merchantRegister(data) {
  return request({
    url: '/merchant/auth/register',
    method: 'post',
    data
  })
}

export function merchantLogin(data) {
  return request({
    url: '/merchant/auth/login',
    method: 'post',
    data
  })
}

export function getMerchantInfo() {
  return request({
    url: '/merchant/auth/info',
    method: 'get'
  })
}

export function merchantLogout() {
  return request({
    url: '/merchant/auth/logout',
    method: 'post'
  })
}

export function updateMerchantProfile(data) {
  return request({
    url: '/merchant/auth/profile',
    method: 'put',
    data
  })
}

export function changeMerchantPassword(data) {
  return request({
    url: '/merchant/auth/change-password',
    method: 'post',
    data
  })
}

// 商家景点管理
export function getMerchantScenicList(params) {
  return request({
    url: '/merchant/scenic/list',
    method: 'get',
    params
  })
}

export function createMerchantScenic(data) {
  return request({
    url: '/merchant/scenic',
    method: 'post',
    data
  })
}

export function updateMerchantScenic(id, data) {
  return request({
    url: `/merchant/scenic/${id}`,
    method: 'put',
    data
  })
}

export function deleteMerchantScenic(id) {
  return request({
    url: `/merchant/scenic/${id}`,
    method: 'delete'
  })
}

export function getMerchantScenicDetail(id) {
  return request({
    url: `/merchant/scenic/${id}`,
    method: 'get'
  })
}

export function updateMerchantScenicStatus(id, status) {
  return request({
    url: `/merchant/scenic/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 商家酒店管理
export function getMerchantHotelList(params) {
  return request({
    url: '/merchant/hotel/list',
    method: 'get',
    params
  })
}

export function createMerchantHotel(data) {
  return request({
    url: '/merchant/hotel',
    method: 'post',
    data
  })
}

export function updateMerchantHotel(id, data) {
  return request({
    url: `/merchant/hotel/${id}`,
    method: 'put',
    data
  })
}

export function deleteMerchantHotel(id) {
  return request({
    url: `/merchant/hotel/${id}`,
    method: 'delete'
  })
}

export function getMerchantHotelDetail(id) {
  return request({
    url: `/merchant/hotel/${id}`,
    method: 'get'
  })
}

export function updateMerchantHotelStatus(id, status) {
  return request({
    url: `/merchant/hotel/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 商家房型管理
export function getMerchantRoomList(hotelId) {
  return request({
    url: `/merchant/hotel/${hotelId}/rooms`,
    method: 'get'
  })
}

export function createMerchantRoom(data) {
  return request({
    url: `/merchant/hotel/${data.hotelId}/rooms`,
    method: 'post',
    data
  })
}

export function updateMerchantRoom(data) {
  return request({
    url: `/merchant/hotel/rooms/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteMerchantRoom(id) {
  return request({
    url: `/merchant/hotel/rooms/${id}`,
    method: 'delete'
  })
}

export function getMerchantRoomDetail(id) {
  return request({
    url: `/merchant/hotel/rooms/${id}`,
    method: 'get'
  })
}

// 商家订单管理
export function getMerchantOrders(params) {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params
  })
}

export function getMerchantOrderDetail(orderNo) {
  return request({
    url: `/merchant/orders/${orderNo}`,
    method: 'get'
  })
}

export function getMerchantOrderStats() {
  return request({
    url: '/merchant/orders/stats',
    method: 'get'
  })
}

// 商家数据统计
export function getMerchantStatsOverview() {
  return request({
    url: '/merchant/stats/overview',
    method: 'get'
  })
}

export function getMerchantOrderTrend() {
  return request({
    url: '/merchant/stats/order-trend',
    method: 'get'
  })
}

export function getMerchantRevenueTrend(params) {
  return request({
    url: '/merchant/stats/revenue-trend',
    method: 'get',
    params
  })
}

export function getMerchantProductRanking(limit = 10) {
  return request({
    url: '/merchant/stats/product-ranking',
    method: 'get',
    params: { limit }
  })
}
