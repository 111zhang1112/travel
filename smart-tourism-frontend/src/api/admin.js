import request from './request'

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

// 获取订单趋势数据
export function getOrderTrend(params) {
  return request.get('/admin/dashboard/order-trend', { params })
}

// 获取订单类型分布
export function getOrderTypeDistribution() {
  return request.get('/admin/dashboard/order-distribution')
}

// 获取用户增长趋势
export function getUserGrowthTrend() {
  return request.get('/admin/dashboard/user-growth')
}

// 获取收入趋势
export function getRevenueTrend(period = 'month') {
  return request.get('/admin/dashboard/revenue-trend', { params: { period } })
}

// 获取热门景点TOP10
export function getTopScenic() {
  return request.get('/admin/dashboard/top-scenic')
}

// 获取热门酒店TOP10
export function getTopHotels() {
  return request.get('/admin/dashboard/top-hotels')
}

// 获取订单状态分布
export function getOrderStatusDistribution() {
  return request.get('/admin/dashboard/order-status-distribution')
}

// 获取综合统计（雷达图）
export function getComprehensiveStats() {
  return request.get('/admin/dashboard/comprehensive-stats')
}

// 景点管理
export function adminGetScenicList(params) {
  return request.get('/admin/scenic/list', { params })
}

export function adminCreateScenic(data) {
  return request.post('/admin/scenic/create', data)
}

export function adminUpdateScenic(id, data) {
  return request.put(`/admin/scenic/update/${id}`, data)
}

export function adminDeleteScenic(id) {
  return request.delete(`/admin/scenic/delete/${id}`)
}

export function adminUpdateScenicStatus(id, status) {
  return request.put(`/admin/scenic/updateStatus/${id}`, null, { params: { status } })
}

export function adminGetScenicCategories() {
  return request.get('/admin/scenic/categories')
}

// 酒店管理
export function adminGetHotelList(params) {
  return request.get('/admin/hotel/list', { params })
}

export function adminCreateHotel(data) {
  return request.post('/admin/hotel/create', data)
}

export function adminUpdateHotel(id, data) {
  return request.put(`/admin/hotel/update/${id}`, data)
}

export function adminDeleteHotel(id) {
  return request.delete(`/admin/hotel/delete/${id}`)
}

// 酒店房间管理
export function adminGetHotelRooms(hotelId) {
  return request.get(`/admin/hotel/${hotelId}/rooms`)
}

export function adminCreateHotelRoom(hotelId, data) {
  return request.post(`/admin/hotel/${hotelId}/rooms`, data)
}

export function adminUpdateHotelRoom(roomId, data) {
  return request.put(`/admin/hotel/rooms/${roomId}`, data)
}

export function adminDeleteHotelRoom(roomId) {
  return request.delete(`/admin/hotel/rooms/${roomId}`)
}

export function adminUpdateHotelRoomStatus(roomId, status) {
  return request.put(`/admin/hotel/rooms/${roomId}/status`, null, { params: { status } })
}

// 攻略审核
export function adminGetGuideList(params) {
  return request.get('/admin/guide/list', { params })
}

export function adminApproveGuide(id) {
  return request.post(`/admin/guide/approve/${id}`)
}

export function adminRejectGuide(id) {
  return request.post(`/admin/guide/reject/${id}`)
}

// 订单管理
export function adminGetOrderList(params) {
  return request.get('/admin/order/list', { params })
}

export function adminCompleteOrder(orderNo) {
  return request.post(`/admin/order/complete/${orderNo}`)
}

// 客服 - 获取待接入用户列表
export function getWaitingUsers() {
  return request.get('/admin/chat/waiting-users')
}

// 客服 - 获取聊天历史
export function getChatHistory(userId) {
  return request.get(`/admin/chat/history/${userId}`)
}

// 用户管理
export function adminGetUserList(params) {
  return request.get('/admin/user/list', { params })
}

export function adminUpdateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}

export function adminDeleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}

export function adminCreateUser(data) {
  return request.post('/admin/user/create', data)
}

export function adminAdjustUserPoints(userId, data) {
  return request.post(`/admin/user/${userId}/points`, data)
}

// 系统配置 - 轮播图管理
export function adminGetBannerList(params) {
  return request.get('/admin/system/banner/list', { params })
}

export function adminCreateBanner(data) {
  return request.post('/admin/system/banner', data)
}

export function adminUpdateBanner(id, data) {
  return request.put(`/admin/system/banner/${id}`, data)
}

export function adminDeleteBanner(id) {
  return request.delete(`/admin/system/banner/${id}`)
}

// 系统配置 - 分类管理
export function adminGetCategoryList(params) {
  return request.get('/admin/system/category/list', { params })
}

export function adminCreateCategory(data) {
  return request.post('/admin/system/category', data)
}

export function adminUpdateCategory(id, data) {
  return request.put(`/admin/system/category/${id}`, data)
}

export function adminDeleteCategory(id) {
  return request.delete(`/admin/system/category/${id}`)
}

// 系统配置 - 公告管理
export function adminGetNoticeList(params) {
  return request.get('/admin/system/notice/list', { params })
}

export function adminCreateNotice(data) {
  return request.post('/admin/system/notice', data)
}

export function adminUpdateNotice(id, data) {
  return request.put(`/admin/system/notice/${id}`, data)
}

export function adminDeleteNotice(id) {
  return request.delete(`/admin/system/notice/${id}`)
}

export function adminPublishNotice(id) {
  return request.put(`/admin/system/notice/${id}/publish`)
}

// 系统配置 - 路线管理
export function adminGetRouteList(params) {
  return request.get('/admin/system/route/list', { params })
}

export function adminCreateRoute(data) {
  return request.post('/admin/system/route', data)
}

export function adminUpdateRoute(id, data) {
  return request.put(`/admin/system/route/${id}`, data)
}

export function adminDeleteRoute(id) {
  return request.delete(`/admin/system/route/${id}`)
}

export function adminUpdateRouteStatus(id, status) {
  return request.put(`/admin/system/route/${id}/status`, null, { params: { status } })
}

// 社区管理 - 动态
export function adminGetPostList(params) {
  return request.get('/admin/community/post/list', { params })
}

export function adminGetPostDetail(id) {
  return request.get(`/admin/community/post/${id}`)
}

export function adminApprovePost(id) {
  return request.post(`/admin/community/post/approve/${id}`)
}

export function adminRejectPost(id, reason) {
  return request.post(`/admin/community/post/reject/${id}`, { reason })
}

export function adminDeletePost(id) {
  return request.delete(`/admin/community/post/${id}`)
}

export function adminRestorePost(id) {
  return request.post(`/admin/community/post/restore/${id}`)
}

export function adminGetPostStats() {
  return request.get('/admin/community/post/stats')
}

// 社区管理 - 评论
export function adminGetCommentList(params) {
  return request.get('/admin/community/comment/list', { params })
}

export function adminDeleteComment(id) {
  return request.delete(`/admin/community/comment/${id}`)
}

export function adminRestoreComment(id) {
  return request.post(`/admin/community/comment/restore/${id}`)
}

export function adminBatchDeleteComments(ids) {
  return request.post('/admin/community/comment/batch-delete', ids)
}

export function adminGetCommentStats() {
  return request.get('/admin/community/comment/stats')
}

// 评价管理
export function adminGetReviewList(params) {
  return request.get('/admin/review/list', { params })
}

export function adminGetPendingReviews(params) {
  return request.get('/admin/review/pending', { params })
}

export function adminAuditReview(id, status) {
  return request.post(`/admin/review/audit/${id}`, null, { params: { status } })
}

export function adminBatchAuditReviews(ids, status) {
  return request.post('/admin/review/audit/batch', ids, { params: { status } })
}

export function adminDeleteReview(id) {
  return request.delete(`/admin/review/${id}`)
}

// 优惠券管理
export function adminGetCouponList(params) {
  return request.get('/admin/coupon/list', { params })
}

export function adminCreateCoupon(data) {
  return request.post('/admin/coupon', data)
}

export function adminUpdateCoupon(id, data) {
  return request.put(`/admin/coupon/${id}`, data)
}

export function adminDeleteCoupon(id) {
  return request.delete(`/admin/coupon/${id}`)
}

export function adminUpdateCouponStatus(id, status) {
  return request.post(`/admin/coupon/${id}/status`, null, { params: { status } })
}

// 商家管理 - 待审核景点商家列表
export function adminGetPendingScenicMerchants(params) {
  return request.get('/admin/merchant/pending/scenic', { params })
}

// 商家管理 - 待审核酒店商家列表
export function adminGetPendingHotelMerchants(params) {
  return request.get('/admin/merchant/pending/hotel', { params })
}

// 商家管理 - 审核通过
export function adminApproveMerchant(id) {
  return request.post(`/admin/merchant/approve/${id}`)
}

// 商家管理 - 审核拒绝
export function adminRejectMerchant(id, reason) {
  return request.post(`/admin/merchant/reject/${id}`, { reason })
}

// 商家管理 - 禁用商家
export function adminDisableMerchant(id) {
  return request.post(`/admin/merchant/disable/${id}`)
}

// 商家管理 - 启用商家
export function adminEnableMerchant(id) {
  return request.post(`/admin/merchant/enable/${id}`)
}

// 商家管理 - 商家列表
export function adminGetMerchantList(params) {
  return request.get('/admin/merchant/list', { params })
}
