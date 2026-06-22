import request from './request'

// 获取攻略列表
export function getGuideList(params) {
  return request.get('/guide/list', { params })
}

// 获取攻略详情
export function getGuideDetail(id) {
  return request.get(`/guide/detail/${id}`)
}

// 创建攻略
export function createGuide(data) {
  return request.post('/guide/create', data)
}

// 更新攻略
export function updateGuide(id, data) {
  return request.put(`/guide/update/${id}`, data)
}
