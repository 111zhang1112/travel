<template>
  <div class="guide-detail-page">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    
    <template v-else-if="guide">
      <div class="guide-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/guide' }">旅游攻略</el-breadcrumb-item>
          <el-breadcrumb-item>攻略详情</el-breadcrumb-item>
        </el-breadcrumb>
        
        <h1 class="guide-title">{{ guide.title }}</h1>
        
        <div class="guide-meta">
          <div class="author-info">
            <el-avatar :size="40" :src="guide.authorAvatar">{{ guide.authorName?.charAt(0) }}</el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ guide.authorName }}</span>
              <span class="publish-time">发布于 {{ formatDate(guide.createTime) }}</span>
            </div>
          </div>
          <div class="guide-stats">
            <span><el-icon><View /></el-icon> {{ guide.viewCount || 0 }} 阅读</span>
            <span><el-icon><Star /></el-icon> {{ guide.likeCount || 0 }} 点赞</span>
            <span><el-icon><CollectionTag /></el-icon> {{ guide.collectCount || 0 }} 收藏</span>
          </div>
        </div>
      </div>

      <div class="guide-cover" v-if="guide.coverImage">
        <img :src="guide.coverImage" :alt="guide.title" />
      </div>

      <div class="guide-content" v-html="guide.content"></div>

      <div class="guide-footer">
        <div class="action-buttons">
          <el-button type="primary" plain :icon="Star">点赞</el-button>
          <el-button type="warning" plain :icon="CollectionTag">收藏</el-button>
          <ShareButton
            content-type="guide"
            :content-id="guide.id"
            :content-data="{
              title: guide.title,
              coverImage: guide.coverImage,
              author: guide.authorName,
              viewCount: guide.viewCount,
              introduction: guide.content?.substring(0, 100)
            }"
            text="分享攻略"
            @share-success="handleShareSuccess"
          />
        </div>
      </div>
    </template>

    <el-empty v-else description="攻略内容不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getGuideDetail } from '@/api/guide'
import { ElMessage } from 'element-plus'
import { View, Star, CollectionTag } from '@element-plus/icons-vue'
import ShareButton from '@/components/ShareButton.vue'
import dayjs from 'dayjs'

const route = useRoute()
const guide = ref(null)
const loading = ref(true)

const loadData = async () => {
  try {
    loading.value = true
    const res = await getGuideDetail(route.params.id)
    guide.value = res
  } catch (error) {
    console.error('Failed to load guide detail:', error)
    ElMessage.error('加载攻略详情失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const handleShareSuccess = () => {
  ElMessage.success('分享成功！获得5积分奖励')
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.guide-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  background: #fff;
  min-height: 100vh;
}

.guide-header {
  margin-bottom: 30px;
  
  .el-breadcrumb {
    margin-bottom: 24px;
  }
  
  .guide-title {
    font-size: 32px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
    line-height: 1.4;
  }
  
  .guide-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    
    .author-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .author-detail {
        display: flex;
        flex-direction: column;
        
        .author-name {
          font-weight: 500;
          color: #333;
        }
        
        .publish-time {
          font-size: 13px;
          color: #999;
          margin-top: 2px;
        }
      }
    }
    
    .guide-stats {
      display: flex;
      gap: 20px;
      font-size: 14px;
      color: #666;
      
      span {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }
}

.guide-cover {
  width: 100%;
  margin-bottom: 30px;
  border-radius: 12px;
  overflow: hidden;
  
  img {
    width: 100%;
    max-height: 500px;
    object-fit: cover;
  }
}

.guide-content {
  font-size: 16px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 50px;
  
  :deep(p) {
    margin-bottom: 1.5em;
  }
  
  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 20px 0;
  }
}

.guide-footer {
  padding-top: 30px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: center;
  
  .action-buttons {
    display: flex;
    gap: 16px;
  }
}

.loading-state {
  padding: 40px 0;
}
</style>
