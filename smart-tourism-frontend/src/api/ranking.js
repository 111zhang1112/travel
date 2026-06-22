import request from './request'

/**
 * 获取景点热门榜
 */
export const getScenicRanking = (params) => {
  return request.get('/ranking/scenic', { params })
}

/**
 * 获取酒店热门榜
 */
export const getHotelRanking = (params) => {
  return request.get('/ranking/hotel', { params })
}

/**
 * 获取综合排行数据
 */
export const getAllRanking = () => {
  return request.get('/ranking/all')
}
