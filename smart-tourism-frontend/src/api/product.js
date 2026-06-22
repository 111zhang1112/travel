import request from './request'

/**
 * 获取商品列表
 */
export const getProductList = (params) => {
  return request.get('/products', { params })
}

/**
 * 获取商品详情
 */
export const getProductDetail = (id) => {
  return request.get(`/products/${id}`)
}

/**
 * 购买商品
 */
export const purchaseProduct = (data) => {
  return request.post('/products/purchase', data)
}

/**
 * 获取我的购买记录
 */
export const getMyPurchases = () => {
  return request.get('/products/purchases')
}

// ========== 管理后台接口 ==========

/**
 * 获取商品列表（管理后台）
 */
export const adminGetProductList = (params) => {
  return request.get('/admin/products', { params })
}

/**
 * 添加商品
 */
export const adminAddProduct = (data) => {
  return request.post('/admin/products', data)
}

/**
 * 更新商品
 */
export const adminUpdateProduct = (id, data) => {
  return request.put(`/admin/products/${id}`, data)
}

/**
 * 删除商品
 */
export const adminDeleteProduct = (id) => {
  return request.delete(`/admin/products/${id}`)
}
