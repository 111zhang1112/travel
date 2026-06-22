import request from './request'

/**
 * 获取可领取的优惠券列表
 */
export const getAvailableCoupons = () => {
  return request.get('/coupon/available')
}

/**
 * 领取优惠券
 */
export const receiveCoupon = (id) => {
  return request.post(`/coupon/receive/${id}`)
}

/**
 * 获取我的优惠券列表
 */
export const getMyCoupons = (params) => {
  return request.get('/coupon/my', { params })
}

/**
 * 获取可用优惠券（下单时）
 */
export const getUsableCoupons = (amount) => {
  return request.get('/coupon/usable', { params: { amount } })
}
