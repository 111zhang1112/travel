import request from './request'

// 获取酒店列表
export function getHotelList(params) {
  return request.get('/hotel/list', { params })
}

// 获取酒店详情
export function getHotelDetail(id) {
  return request.get(`/hotel/detail/${id}`)
}

// 获取酒店房间列表
export function getHotelRooms(hotelId) {
  return request.get(`/hotel/${hotelId}/rooms`)
}

// 搜索酒店
export function searchHotel(params) {
  return request.get('/hotel/search', { params })
}
