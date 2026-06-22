import request from './request'

/**
 * 获取结伴列表
 */
export const getCompanions = (params) => {
  return request.get('/companion/list', { params })
}

/**
 * 获取结伴详情
 */
export const getCompanionDetail = (id) => {
  return request.get(`/companion/${id}`)
}

/**
 * 发布结伴
 */
export const createCompanion = (data) => {
  return request.post('/companion/create', data)
}

/**
 * 更新结伴
 */
export const updateCompanion = (id, data) => {
  return request.put(`/companion/${id}`, data)
}

/**
 * 删除结伴
 */
export const deleteCompanion = (id) => {
  return request.delete(`/companion/${id}`)
}

/**
 * 结束招募
 */
export const endRecruit = (id) => {
  return request.post(`/companion/${id}/end`)
}

/**
 * 点赞结伴
 */
export const likeCompanion = (id) => {
  return request.post(`/companion/${id}/like`)
}

/**
 * 获取我发布的结伴
 */
export const getMyCompanions = (params) => {
  return request.get('/companion/my-posts', { params })
}

/**
 * 申请加入
 */
export const applyCompanion = (id, data) => {
  return request.post(`/companion/${id}/apply`, data)
}

/**
 * 获取我的申请
 */
export const getMyApplications = (params) => {
  return request.get('/companion/my-applications', { params })
}

/**
 * 获取结伴的申请列表
 */
export const getCompanionApplications = (id, params) => {
  return request.get(`/companion/${id}/applications`, { params })
}

/**
 * 同意申请
 */
export const approveApplication = (id, data) => {
  return request.post(`/companion/applications/${id}/approve`, data)
}

/**
 * 拒绝申请
 */
export const rejectApplication = (id, data) => {
  return request.post(`/companion/applications/${id}/reject`, data)
}
