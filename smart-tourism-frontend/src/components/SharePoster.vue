<template>
  <el-dialog
    v-model="dialogVisible"
    title="分享海报"
    width="400px"
  >
    <div class="poster-container">
      <canvas ref="canvasRef" width="600" height="900"></canvas>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="downloadPoster">下载海报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'

const props = defineProps({
  modelValue: Boolean,
  contentType: String,
  contentId: Number,
  contentData: Object,
  shareUrl: String
})

const emit = defineEmits(['update:modelValue'])

const dialogVisible = ref(false)
const canvasRef = ref(null)

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    nextTick(() => {
      generatePoster()
    })
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const generatePoster = async () => {
  try {
    const canvas = canvasRef.value
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 绘制背景
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // 绘制主图
    await drawImage(ctx, props.contentData.image || props.contentData.coverImage, 0, 0, 600, 400)

    // 绘制标题
    ctx.fillStyle = '#303133'
    ctx.font = 'bold 32px Arial'
    ctx.textAlign = 'left'
    wrapText(ctx, props.contentData.name || props.contentData.title, 40, 460, 520, 40)

    // 绘制价格/评分
    ctx.fillStyle = '#F56C6C'
    ctx.font = 'bold 28px Arial'
    const priceText = getContentInfo()
    ctx.fillText(priceText, 40, 560)

    // 绘制描述
    ctx.fillStyle = '#909399'
    ctx.font = '20px Arial'
    const description = props.contentData.description || props.contentData.introduction || ''
    wrapText(ctx, description, 40, 620, 520, 30, 3)

    // 绘制二维码
    const qrCodeDataUrl = await QRCode.toDataURL(props.shareUrl || 'http://localhost:5173', {
      width: 150,
      margin: 1
    })
    await drawImage(ctx, qrCodeDataUrl, 225, 720, 150, 150)

    // 绘制提示文字
    ctx.fillStyle = '#606266'
    ctx.font = '18px Arial'
    ctx.textAlign = 'center'
    ctx.fillText('扫码查看详情', 300, 890)

  } catch (error) {
    console.error('生成海报失败:', error)
    ElMessage.error('生成海报失败')
  }
}

const drawImage = (ctx, src, x, y, width, height) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      ctx.drawImage(img, x, y, width, height)
      resolve()
    }
    img.onerror = reject
    img.src = src
  })
}

const wrapText = (ctx, text, x, y, maxWidth, lineHeight, maxLines = 999) => {
  const words = text.split('')
  let line = ''
  let lineCount = 0

  for (let i = 0; i < words.length; i++) {
    const testLine = line + words[i]
    const metrics = ctx.measureText(testLine)
    
    if (metrics.width > maxWidth && i > 0) {
      ctx.fillText(line, x, y)
      line = words[i]
      y += lineHeight
      lineCount++
      
      if (lineCount >= maxLines) {
        ctx.fillText(line + '...', x, y)
        return
      }
    } else {
      line = testLine
    }
  }
  ctx.fillText(line, x, y)
}

const getContentInfo = () => {
  const data = props.contentData
  
  switch (props.contentType) {
    case 'scenic':
      return `¥${data.ticketPrice || 0} | ⭐${data.rating || 0}`
    case 'hotel':
      return `¥${data.minPrice || 0}起 | ${data.starRating || ''}星级`
    case 'guide':
      return `${data.author || '匿名'} | ${data.viewCount || 0}次浏览`
    case 'route':
      return `${data.days || 0}天 | ¥${data.totalCost || 0}`
    default:
      return ''
  }
}

const downloadPoster = () => {
  try {
    const canvas = canvasRef.value
    const dataUrl = canvas.toDataURL('image/png')
    
    const link = document.createElement('a')
    link.download = `share-poster-${Date.now()}.png`
    link.href = dataUrl
    link.click()
    
    ElMessage.success('海报已下载')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}
</script>

<style scoped>
.poster-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

canvas {
  max-width: 100%;
  height: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
