import request from './request'

// 获取推荐景点
export function getRecommendScenic(params) {
  return request.get('/recommend/scenic', { params })
}

// 记录用户行为
export function recordInteraction(data) {
  return request.post('/recommend/interaction', data)
}
