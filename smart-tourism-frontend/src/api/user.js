import request from './request'

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
  return request.get('/auth/info')
}

/**
 * 更新用户头像
 */
export const updateAvatar = (avatarUrl) => {
  return request.post('/auth/avatar', { avatarUrl })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (data) => {
  return request.put('/auth/info', data)
}

/**
 * 上传图片
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改密码
 */
export const changePassword = (data) => {
  return request.post('/auth/change-password', data)
}
