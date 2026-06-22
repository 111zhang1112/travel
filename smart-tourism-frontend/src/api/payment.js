import request from './request'

/**
 * 创建支付订单
 */
export function createPayment(data) {
  return request({
    url: '/payment/create',
    method: 'post',
    data
  })
}

/**
 * 支付宝支付
 */
export function alipayPay(orderNo) {
  return request({
    url: `/payment/alipay/pay/${orderNo}`,
    method: 'get'
  })
}

/**
 * 查询支付状态
 */
export function queryPaymentStatus(orderNo) {
  return request({
    url: `/payment/status/${orderNo}`,
    method: 'get'
  })
}
