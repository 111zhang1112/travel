import request from './request'

// 添加收藏
export function addFavorite(targetType, targetId) {
  return request.post('/favorite', null, {
    params: { targetType, targetId }
  })
}

// 取消收藏
export function removeFavorite(targetType, targetId) {
  return request.delete('/favorite', {
    params: { targetType, targetId }
  })
}

// 检查是否已收藏
export function checkFavorite(targetType, targetId) {
  return request.get('/favorite/check', {
    params: { targetType, targetId }
  })
}

// 获取收藏列表
export function getFavoriteList(targetType) {
  return request.get('/favorite/list', {
    params: { targetType }
  })
}
