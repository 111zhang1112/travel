import request from './request'

/**
 * 创建分享
 */
export function createShare(data) {
  return request({
    url: '/share/create',
    method: 'post',
    data
  })
}

/**
 * 获取分享链接详情
 */
export function getShareUrl(encoded) {
  return request({
    url: `/share/url/${encoded}`,
    method: 'get'
  })
}

/**
 * 获取内容分享统计
 */
export function getStatistics(contentType, contentId) {
  return request({
    url: `/share/statistics/${contentType}/${contentId}`,
    method: 'get'
  })
}

/**
 * 记录分享链接访问
 */
export function recordView(shareId) {
  return request({
    url: `/share/record-view/${shareId}`,
    method: 'post'
  })
}

/**
 * 获取分享排行榜(管理员)
 */
export function getShareRanking(params) {
  return request({
    url: '/admin/share/ranking',
    method: 'get',
    params
  })
}

/**
 * 获取平台分享统计(管理员)
 */
export function getPlatformStatistics(days) {
  return request({
    url: '/admin/share/platform-statistics',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取总体统计数据(管理员)
 */
export function getShareStatistics(days) {
  return request({
    url: '/admin/share/statistics',
    method: 'get',
    params: { days }
  })
}
