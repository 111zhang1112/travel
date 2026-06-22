import request from './request'

/**
 * 获取轮播图列表
 */
export function getBanners() {
  return request.get('/system/banners')
}

/**
 * 获取公告列表
 */
export function getNotices() {
  return request.get('/system/notices')
}

/**
 * 获取精选路线（首页展示）
 */
export function getFeaturedRoutes(params) {
  return request.get('/system/routes/featured', { params })
}

/**
 * 获取路线列表（分页）
 */
export function getRoutes(params) {
  return request.get('/system/routes', { params })
}

/**
 * 获取路线详情
 */
export function getRouteDetail(id) {
  return request.get(`/system/routes/${id}`)
}

/**
 * 获取热门搜索关键词
 */
export function getHotKeywords() {
  return request.get('/system/hot-keywords')
}
