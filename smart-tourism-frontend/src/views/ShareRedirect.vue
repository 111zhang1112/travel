<template>
  <div class="share-redirect" v-loading="loading" element-loading-text="正在跳转...">
    <div class="redirect-content">
      <el-icon class="loading-icon" :size="60"><Loading /></el-icon>
      <p>正在为您跳转到分享内容...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShareUrl } from '@/api/share'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)

onMounted(async () => {
  try {
    const encoded = route.params.encoded
    
    // 调用API获取分享链接详情
    const { data } = await getShareUrl(encoded)
    
    // 根据内容类型跳转到对应页面
    const redirectUrl = data.redirectUrl
    
    // 延迟一下让用户看到加载效果
    setTimeout(() => {
      router.replace(redirectUrl)
    }, 500)
    
  } catch (error) {
    loading.value = false
    ElMessage.error(error.message || '分享链接无效或已过期')
    
    // 3秒后跳转到首页
    setTimeout(() => {
      router.replace('/home')
    }, 3000)
  }
})
</script>

<style scoped>
.share-redirect {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.redirect-content {
  text-align: center;
  color: #fff;
}

.loading-icon {
  margin-bottom: 20px;
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
}
</style>
