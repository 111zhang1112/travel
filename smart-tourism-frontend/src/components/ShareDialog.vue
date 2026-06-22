<template>
  <el-dialog
    v-model="dialogVisible"
    title="分享"
    width="500px"
    @close="handleClose"
  >
    <div class="share-dialog">
      <!-- 分享平台选项 -->
      <div class="share-platforms">
        <div 
          class="platform-item" 
          @click="handleShare('wechat')"
        >
          <el-icon :size="40" color="#07C160">
            <ChatDotRound />
          </el-icon>
          <span>微信</span>
        </div>
        <div 
          class="platform-item" 
          @click="handleShare('weibo')"
        >
          <el-icon :size="40" color="#E6162D">
            <Share />
          </el-icon>
          <span>微博</span>
        </div>
        <div 
          class="platform-item" 
          @click="handleShare('qq')"
        >
          <el-icon :size="40" color="#12B7F5">
            <ChatLineRound />
          </el-icon>
          <span>QQ</span>
        </div>
        <div 
          class="platform-item" 
          @click="handleCopyLink"
        >
          <el-icon :size="40" color="#409EFF">
            <Link />
          </el-icon>
          <span>复制链接</span>
        </div>
      </div>

      <!-- 生成海报按钮 -->
      <el-button 
        type="primary" 
        style="width: 100%; margin-top: 20px"
        @click="showPoster = true"
      >
        生成分享海报
      </el-button>

      <!-- 二维码显示(微信分享) -->
      <div v-if="showQrCode" class="qrcode-container">
        <div class="qrcode-title">长按识别二维码分享到微信</div>
        <div ref="qrcodeRef" class="qrcode"></div>
        <div class="share-url">{{ shareUrl }}</div>
      </div>
    </div>

    <!-- 海报生成组件 -->
    <SharePoster
      v-model="showPoster"
      :content-type="contentType"
      :content-id="contentId"
      :content-data="contentData"
      :share-url="shareUrl"
    />
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Share, ChatLineRound, Link } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { createShare } from '@/api/share'
import SharePoster from './SharePoster.vue'

const props = defineProps({
  modelValue: Boolean,
  contentType: String,
  contentId: Number,
  contentData: Object
})

const emit = defineEmits(['update:modelValue', 'share-success'])

const dialogVisible = ref(false)
const showQrCode = ref(false)
const showPoster = ref(false)
const shareUrl = ref('')
const shareId = ref(null)
const qrcodeRef = ref(null)

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    showQrCode.value = false
  }
})

const handleShare = async (platform) => {
  try {
    const data = await createShare({
      contentType: props.contentType,
      contentId: props.contentId,
      platform
    })

    shareUrl.value = data.fullUrl
    shareId.value = data.shareId

    if (platform === 'wechat') {
      // 显示二维码
      showQrCode.value = true
      await nextTick()
      generateQRCode(data.fullUrl)
    } else {
      ElMessage.success('分享成功')
      emit('share-success', data)
    }
  } catch (error) {
    ElMessage.error(error.message || '分享失败')
  }
}

const handleCopyLink = async () => {
  try {
    const data = await createShare({
      contentType: props.contentType,
      contentId: props.contentId,
      platform: 'link'
    })

    shareUrl.value = data.fullUrl

    // 复制到剪贴板
    await navigator.clipboard.writeText(data.fullUrl)
    ElMessage.success('链接已复制到剪贴板')
    emit('share-success', data)
  } catch (error) {
    ElMessage.error(error.message || '复制失败')
  }
}

const generateQRCode = async (url) => {
  try {
    if (qrcodeRef.value) {
      qrcodeRef.value.innerHTML = ''
      await QRCode.toCanvas(url, {
        width: 200,
        margin: 2
      }).then(canvas => {
        qrcodeRef.value.appendChild(canvas)
      })
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
  }
}

const handleClose = () => {
  showQrCode.value = false
  shareUrl.value = ''
}
</script>

<style scoped>
.share-dialog {
  padding: 20px 0;
}

.share-platforms {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.platform-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.platform-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.platform-item span {
  font-size: 14px;
  color: #606266;
}

.qrcode-container {
  margin-top: 30px;
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.qrcode-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
  font-weight: 500;
}

.qrcode {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.share-url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}
</style>
