<template>
  <div class="customer-service">
    <div class="user-list">
      <h3>用户列表</h3>
      <div v-for="user in waitingUsers" :key="user.userId" :class="['user-item', { active: currentUserId === user.userId }]" @click="selectUser(user)">
        <el-avatar :size="40">{{ user.nickname?.charAt(0) }}</el-avatar>
        <div class="user-info">
          <span class="name">{{ user.nickname }}</span>
          <span :class="['status', { online: user.online }]">{{ user.online ? '在线' : '离线' }}</span>
        </div>
      </div>
      <el-empty v-if="waitingUsers.length === 0" description="暂无用户" />
    </div>
    
    <div class="chat-area">
      <template v-if="currentUserId">
        <div class="chat-header">与 {{ currentUserName }} 的对话</div>
        <div class="chat-messages" ref="messagesRef">
          <div v-for="msg in messages" :key="msg.id" :class="['message', msg.senderId === currentUserId ? 'received' : 'sent']">
            <div class="content">{{ msg.content }}</div>
            <div class="time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input v-model="inputMessage" placeholder="输入消息..." @keyup.enter="sendMessage">
            <template #append>
              <el-button type="primary" @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </template>
      <el-empty v-else description="请选择一个用户开始对话" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getWaitingUsers, getChatHistory } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const waitingUsers = ref([])
const currentUserId = ref(null)
const currentUserName = ref('')
const messages = ref([])
const inputMessage = ref('')
const messagesRef = ref()
let ws = null

onMounted(async () => {
  const res = await getWaitingUsers()
  waitingUsers.value = res || []
  
  // 连接 WebSocket
  connectWebSocket()
})

const connectWebSocket = () => {
  const userId = userStore.user?.id
  if (!userId) return
  
  // 使用当前后端服务器地址
  const wsUrl = `ws://${location.hostname}:8080/ws/chat/${userId}`
  console.log('Connecting to WebSocket:', wsUrl)
  ws = new WebSocket(wsUrl)
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.type === 'chat' && data.senderId === currentUserId.value) {
      messages.value.push({
        id: data.messageId,
        senderId: data.senderId,
        content: data.content,
        createTime: data.createTime
      })
      scrollToBottom()
    }
  }
}

const selectUser = async (user) => {
  currentUserId.value = user.userId
  currentUserName.value = user.nickname
  
  try {
    const res = await getChatHistory(user.userId)
    messages.value = res || []
    scrollToBottom()
    
    // 标记消息为已读
    if (messages.value.length > 0) {
      // 刷新用户列表（移除已读用户）
      setTimeout(async () => {
        const updatedRes = await getWaitingUsers()
        waitingUsers.value = updatedRes || []
      }, 500)
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !ws) return
  
  ws.send(JSON.stringify({
    type: 'chat',
    receiverId: currentUserId.value,
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
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString()
}
</script>

<style lang="scss" scoped>
.customer-service {
  display: flex;
  height: calc(100vh - 100px);
  gap: 20px;
}

.user-list {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
  
  h3 { font-size: 16px; margin-bottom: 16px; }
  
  .user-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    
    &:hover, &.active { background: #f5f7fa; }
    
    .user-info {
      display: flex;
      flex-direction: column;
      .name { font-size: 14px; }
      .status { font-size: 12px; color: #999; &.online { color: #67c23a; } }
    }
  }
}

.chat-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  
  .chat-header {
    padding: 16px;
    border-bottom: 1px solid #eee;
    font-weight: 600;
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    .message {
      margin-bottom: 16px;
      
      &.sent { text-align: right; .content { background: var(--primary-color); color: #fff; } }
      &.received { text-align: left; .content { background: #f5f7fa; } }
      
      .content { display: inline-block; padding: 10px 16px; border-radius: 12px; max-width: 60%; }
      .time { font-size: 12px; color: #999; margin-top: 4px; }
    }
  }
  
  .chat-input { padding: 16px; border-top: 1px solid #eee; }
}
</style>
