import request from './request'

// 获取聊天历史
export function getChatHistory(targetUserId, limit = 100) {
  return request.get(`/chat/history/${targetUserId}`, { params: { limit } })
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/chat/unread-count')
}

// 标记消息已读
export function markAsRead(senderId) {
  return request.post(`/chat/mark-read/${senderId}`)
}
