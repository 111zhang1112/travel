<template>
  <div class="ai-chat-page">
    <div class="chat-container">
      <!-- 聊天区域 -->
      <div class="chat-panel">
        <div class="chat-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 旅游助手</span>
        </div>
        
        <div class="chat-messages" ref="messagesRef">
          <div class="welcome-message" v-if="messages.length === 0">
            <div class="ai-avatar">
              <el-icon :size="32"><Promotion /></el-icon>
            </div>
            <div class="welcome-text">
              <h3>你好！我是小游 🌍</h3>
              <p>我可以帮你：</p>
              <ul>
                <li>推荐旅游景点和酒店</li>
                <li>规划旅行行程</li>
                <li>解答旅游相关问题</li>
              </ul>
              <div class="quick-actions">
                <el-button size="small" @click="sendQuickMessage('推荐浙江有什么好玩的景点')">浙江景点推荐</el-button>
                <el-button size="small" @click="planTrip('杭州', 3)">杭州3日游规划</el-button>
                <el-button size="small" @click="planTrip('宁波', 2)">宁波2日游规划</el-button>
              </div>
            </div>
          </div>
          
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar">
              <el-icon v-if="msg.role === 'assistant'"><Promotion /></el-icon>
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="content">
              <div class="text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
          
          <div v-if="isLoading" class="message assistant">
            <div class="avatar"><el-icon><Promotion /></el-icon></div>
            <div class="content">
              <div class="typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="输入你的问题，例如：帮我规划一个杭州3日游"
            @keyup.enter="sendMessage"
            :disabled="isLoading"
          >
            <template #append>
              <el-button type="primary" @click="sendMessage" :loading="isLoading">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 行程展示区域 -->
      <div class="itinerary-panel" v-if="currentItinerary">
        <div class="itinerary-header">
          <h3>{{ currentItinerary.destination }} {{ currentItinerary.days }}日游行程</h3>
          <el-button size="small" @click="currentItinerary = null">关闭</el-button>
        </div>
        <div class="itinerary-content" v-html="formatMessage(currentItinerary.content)"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { createAiChatStream, createItineraryStream } from '@/api/ai'
import { marked } from 'marked'

const userStore = useUserStore()
const messagesRef = ref()
const inputMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const currentItinerary = ref(null)

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch {
    return content.replace(/\n/g, '<br>')
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 检查是否是行程规划请求
  const tripMatch = message.match(/(?:帮我|请|给我)?(?:规划|制定|安排)(?:一个)?(.+?)(\d+)日游/)
  if (tripMatch) {
    planTrip(tripMatch[1].trim(), parseInt(tripMatch[2]))
    return
  }
  
  // 普通对话
  messages.value.push({ role: 'user', content: message })
  scrollToBottom()
  
  isLoading.value = true
  let assistantMessage = { role: 'assistant', content: '' }
  messages.value.push(assistantMessage)
  
  // 使用SSE流式接收AI回复
  createAiChatStream(
    message,
    userStore.user?.id,
    (data) => {
      // 逐字追加内容,实现打字机效果
      assistantMessage.content += data
      scrollToBottom()
    },
    (error) => {
      console.error('AI对话错误:', error)
      assistantMessage.content = '抱歉，AI 服务暂时不可用，请稍后重试。'
      isLoading.value = false
    },
    () => {
      // 流式传输完成
      isLoading.value = false
      scrollToBottom()
    }
  )
}

const sendQuickMessage = (message) => {
  inputMessage.value = message
  sendMessage()
}

const planTrip = (destination, days) => {
  messages.value.push({ role: 'user', content: `帮我规划${destination}${days}日游` })
  scrollToBottom()
  
  isLoading.value = true
  let content = ''
  
  currentItinerary.value = {
    destination,
    days,
    content: ''
  }
  
  // 使用SSE流式接收行程规划
  createItineraryStream(
    { destination, days, userId: userStore.user?.id },
    (data) => {
      // 逐字追加内容,实现打字机效果
      content += data
      currentItinerary.value.content = content
      scrollToBottom()
    },
    (error) => {
      console.error('行程规划错误:', error)
      currentItinerary.value.content = '抱歉，行程规划服务暂时不可用。'
      isLoading.value = false
    },
    () => {
      // 流式传输完成
      isLoading.value = false
      messages.value.push({ role: 'assistant', content: `已为你生成${destination}${days}日游行程，请查看右侧面板。` })
      scrollToBottom()
    }
  )
}
</script>

<style lang="scss" scoped>
.ai-chat-page {
  height: calc(100vh - 140px);
  padding: 20px;
  background: var(--bg-color);
}

.chat-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  gap: 20px;
}

.chat-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;

  .ai-avatar {
    width: 48px;
    height: 48px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .welcome-text {
    h3 { margin-bottom: 8px; }
    p { margin-bottom: 8px; opacity: 0.9; }
    ul { margin: 0 0 16px 20px; opacity: 0.9; }
  }

  .quick-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user {
    flex-direction: row-reverse;
    .content { background: var(--primary-color); color: #fff; }
  }

  &.assistant {
    .content { background: #f5f7fa; }
  }

  .avatar {
    width: 36px;
    height: 36px;
    background: #e0e0e0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .content {
    max-width: 70%;
    padding: 12px 16px;
    border-radius: 12px;
    line-height: 1.6;

    .text {
      :deep(h1, h2, h3) { margin: 16px 0 8px; }
      :deep(ul, ol) { margin: 8px 0; padding-left: 20px; }
      :deep(p) { margin: 8px 0; }
    }
  }
}

.typing {
  display: flex;
  gap: 4px;
  span {
    width: 8px;
    height: 8px;
    background: #999;
    border-radius: 50%;
    animation: typing 1.4s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.itinerary-panel {
  width: 500px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .itinerary-header {
    padding: 16px 20px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    h3 { font-size: 16px; font-weight: 600; }
  }

  .itinerary-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    line-height: 1.8;

    :deep(h2) { color: var(--primary-color); margin: 20px 0 12px; font-size: 18px; }
    :deep(h3) { color: #333; margin: 16px 0 8px; font-size: 15px; }
    :deep(ul) { margin: 8px 0; padding-left: 20px; }
  }
}
</style>
