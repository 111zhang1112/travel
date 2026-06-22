import request from './request'

/**
 * 提交评价
 */
export const submitReview = (data) => {
  return request.post('/review/submit', data)
}

/**
 * 获取目标的评价列表
 */
export const getReviewList = (params) => {
  return request.get('/review/list', { params })
}

/**
 * 获取我的评价列表
 */
export const getMyReviews = (params) => {
  return request.get('/review/my', { params })
}

/**
 * 删除评价
 */
export const deleteReview = (id) => {
  return request.delete(`/review/${id}`)
}

/**
 * 获取评价统计
 */
export const getReviewStats = (params) => {
  return request.get('/review/stats', { params })
}

/**
 * 点赞/取消点赞评价
 */
export const toggleReviewLike = (id) => {
  return request.post(`/review/like/${id}`)
}
