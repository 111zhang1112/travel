import request from './request'

/**
 * 获取积分余额
 */
export const getPointsBalance = () => {
  return request.get('/points/balance')
}

/**
 * 获取积分记录
 */
export const getPointsRecords = (params) => {
  return request.get('/points/records', { params })
}
