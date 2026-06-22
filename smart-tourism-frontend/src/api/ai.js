// AI 相关 API - 使用 SSE 流式接口
const BASE_URL = '/api'

// 创建 SSE 连接进行 AI 对话
export function createAiChatStream(message, userId, onMessage, onError, onComplete) {
  const url = `${BASE_URL}/ai/chat/stream?message=${encodeURIComponent(message)}${userId ? '&userId=' + userId : ''}`
  const eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource.close()
      onComplete && onComplete()
    } else {
      onMessage && onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE连接错误:', error)
    eventSource.close()
    onError && onError(error)
  }

  // 监听自定义错误事件
  eventSource.addEventListener('error', (event) => {
    if (event.data) {
      console.error('AI服务错误:', event.data)
      onError && onError(new Error(event.data))
    }
  })

  return eventSource
}

// 创建 SSE 连接进行行程规划
export function createItineraryStream(request, onMessage, onError, onComplete) {
  const url = `${BASE_URL}/ai/itinerary/stream?destination=${encodeURIComponent(request.destination)}&days=${request.days}${request.preferences ? '&preferences=' + encodeURIComponent(request.preferences) : ''}${request.userId ? '&userId=' + request.userId : ''}`
  const eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource.close()
      onComplete && onComplete()
    } else {
      onMessage && onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE连接错误:', error)
    eventSource.close()
    onError && onError(error)
  }

  // 监听自定义错误事件
  eventSource.addEventListener('error', (event) => {
    if (event.data) {
      console.error('AI服务错误:', event.data)
      onError && onError(new Error(event.data))
    }
  })

  return eventSource
}
