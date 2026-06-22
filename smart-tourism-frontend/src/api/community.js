import request from './request'

/**
 * 获取动态列表
 */
export const getPosts = (params) => {
  return request.get('/community/posts', { params })
}

/**
 * 获取动态详情
 */
export const getPostDetail = (id) => {
  return request.get(`/community/posts/${id}`)
}

/**
 * 发布动态
 */
export const createPost = (data) => {
  return request.post('/community/posts', data)
}

/**
 * 删除动态
 */
export const deletePost = (id) => {
  return request.delete(`/community/posts/${id}`)
}

/**
 * 点赞动态
 */
export const likePost = (id) => {
  return request.post(`/community/posts/${id}/like`)
}

/**
 * 获取我的动态
 */
export const getMyPosts = (params) => {
  return request.get('/community/my-posts', { params })
}

/**
 * 获取评论列表
 */
export const getComments = (params) => {
  return request.get('/community/comments', { params })
}

/**
 * 发表评论
 */
export const createComment = (data) => {
  return request.post('/community/comments', data)
}

/**
 * 删除评论
 */
export const deleteComment = (id) => {
  return request.delete(`/community/comments/${id}`)
}

/**
 * 点赞评论
 */
export const likeComment = (id) => {
  return request.post(`/community/comments/${id}/like`)
}
