<template>
  <div class="customer-chat">
    <div class="chat-container">
      <div class="chat-header">
        <el-icon><Service /></el-icon>
        <span>在线客服</span>
        <el-tag v-if="isOnline" type="success" size="small">在线</el-tag>
        <el-tag v-else type="info" size="small">离线</el-tag>
      </div>
      
      <div class="chat-messages" ref="messagesRef">
        <div v-for="msg in messages" :key="msg.id" :class="['message', msg.senderId === userStore.user?.id ? 'sent' : 'received']">
          <div class="content">{{ msg.content }}</div>
          <div class="time">{{ formatTime(msg.createTime) }}</div>
        </div>
        <el-empty v-if="messages.length === 0" description="暂无消息，开始和客服聊天吧" />
      </div>
      
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :disabled="!userStore.isLoggedIn"
        >
          <template #append>
            <el-button type="primary" @click="sendMessage" :disabled="!userStore.isLoggedIn">
              发送
            </el-button>
          </template>
        </el-input>
        <div v-if="!userStore.isLoggedIn" class="login-tip">
          <el-text type="warning">请先登录后再联系客服</el-text>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getChatHistory, markAsRead } from '@/api/chat'

const userStore = useUserStore()
const messages = ref([])
const inputMessage = ref('')
const messagesRef = ref()
const isOnline = ref(false)
let ws = null

// 假设客服的 ID 为 1（管理员）
const CUSTOMER_SERVICE_ID = 1

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadHistory()
    connectWebSocket()
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})

const connectWebSocket = () => {
  const userId = userStore.user?.id
  if (!userId) return
  
  // 使用当前后端服务器地址
  const wsUrl = `ws://${location.hostname}:8080/ws/chat/${userId}`
  console.log('Connecting to WebSocket:', wsUrl)
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket connected')
    isOnline.value = true
  }
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    console.log('Received:', data)
    
    if (data.type === 'chat' && data.senderId === CUSTOMER_SERVICE_ID) {
      messages.value.push({
        id: data.messageId,
        senderId: data.senderId,
        content: data.content,
        createTime: data.createTime
      })
      scrollToBottom()
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
    isOnline.value = false
  }
  
  ws.onclose = () => {
    console.log('WebSocket disconnected')
    isOnline.value = false
  }
}

const loadHistory = async () => {
  try {
    const res = await getChatHistory(CUSTOMER_SERVICE_ID, 100)
    if (res && res.length > 0) {
      messages.value = res
      scrollToBottom()
      // 标记消息为已读
      await markAsRead(CUSTOMER_SERVICE_ID)
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !ws || !userStore.isLoggedIn) return
  
  if (ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，请刷新页面重试')
    return
  }
  
  ws.send(JSON.stringify({
    type: 'chat',
    receiverId: CUSTOMER_SERVICE_ID,
    content: inputMessage.value,
    messageType: 'TEXT'
  }))
  
  messages.value.push({
    id: Date.now(),
    senderId: userStore.user?.id,
    content: inputMessage.value,
    createTime: new Date().toISOString()
  })
  
  inputMessage.value = ''
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString()
}
</script>

<style lang="scss" scoped>
.customer-chat {
  min-height: calc(100vh - 140px);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chat-container {
  width: 100%;
  max-width: 800px;
  height: 600px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  
  .message {
    margin-bottom: 16px;
    
    &.sent {
      text-align: right;
      .content {
        background: var(--primary-color);
        color: #fff;
      }
    }
    
    &.received {
      text-align: left;
      .content {
        background: #f5f7fa;
      }
    }
    
    .content {
      display: inline-block;
      padding: 10px 16px;
      border-radius: 12px;
      max-width: 60%;
      word-break: break-word;
    }
    
    .time {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  
  .login-tip {
    margin-top: 8px;
    text-align: center;
  }
}
</style>
