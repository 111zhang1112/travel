import request from './request'

// 获取景点列表
export function getScenicList(params) {
  return request.get('/scenic/list', { params })
}

// 获取景点详情
export function getScenicDetail(id) {
  return request.get(`/scenic/detail/${id}`)
}

// 搜索景点
export function searchScenic(keyword) {
  return request.get('/scenic/search', { params: { keyword } })
}

// 按地区查询景点
export function getScenicByRegion(region, limit = 10) {
  return request.get('/scenic/by-region', { params: { region, limit } })
}

// 获取所有地区列表
export function getRegions() {
  return request.get('/scenic/regions')
}
