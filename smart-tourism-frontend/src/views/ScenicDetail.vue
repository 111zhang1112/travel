<template>
  <div class="scenic-detail-page" v-if="detail">
    <div class="detail-header">
      <div class="images">
        <el-carousel height="400px">
          <el-carousel-item v-for="(img, i) in images" :key="i">
            <img :src="img" :alt="detail.name" />
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="info">
        <h1>{{ detail.name }}</h1>
        <div class="rating">
          <el-rate v-model="detail.rating" disabled />
          <span>{{ detail.rating }} 分</span>
        </div>
        <div class="meta">
          <p><el-icon><Clock /></el-icon> {{ detail.openingHours || '全天开放' }}</p>
          <p><el-icon><View /></el-icon> {{ detail.viewCount }} 人浏览</p>
        </div>
        <div class="tags" v-if="tags.length">
          <el-tag v-for="tag in tags" :key="tag" type="info">{{ tag }}</el-tag>
        </div>
        <div class="price">
          <span class="label">门票价格</span>
          <span class="value">¥{{ detail.ticketPrice }}</span>
        </div>
        <div class="action-buttons">
          <el-button 
            :type="isFavorite ? 'warning' : 'default'" 
            size="large" 
            @click="handleToggleFavorite"
            :icon="Star"
          >
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
          <ShareButton
            v-if="detail"
            content-type="scenic"
            :content-id="detail.id"
            :content-data="{
              name: detail.name,
              image: images[0],
              ticketPrice: detail.ticketPrice,
              rating: detail.rating,
              description: detail.description
            }"
            size="large"
            @share-success="handleShareSuccess"
          />
          <el-button type="primary" size="large" @click="handleBook">立即预订</el-button>
        </div>
      </div>
    </div>
    
    <div class="detail-content">
      <div class="main">
        <h2>景点介绍</h2>
        <p>{{ detail.description }}</p>
        
        <!-- 详细信息区域 -->
        <div class="detail-info-section" v-if="hasDetailInfo">
          <div class="info-card" v-if="detail.address">
            <h3>📍 详细地址</h3>
            <p>{{ detail.address }}</p>
          </div>
          
          <div class="info-card" v-if="detail.trafficGuide">
            <h3>🚗 交通指南</h3>
            <p class="multiline">{{ detail.trafficGuide }}</p>
          </div>
          
          <div class="info-card" v-if="detail.travelTips">
            <h3>💡 游玩建议</h3>
            <p class="multiline">{{ detail.travelTips }}</p>
          </div>
          
          <div class="info-row" v-if="detail.recommendedDuration || detail.bestSeason">
            <div class="info-card half" v-if="detail.recommendedDuration">
              <h3>⏱️ 推荐游玩时长</h3>
              <p>{{ detail.recommendedDuration }}</p>
            </div>
            
            <div class="info-card half" v-if="detail.bestSeason">
              <h3>🌸 最佳游玩季节</h3>
              <p>{{ detail.bestSeason }}</p>
            </div>
          </div>
        </div>
        
        <!-- 评价区域 -->
        <div class="review-section">
          <div class="review-header">
            <h2>用户评价</h2>
            <div class="review-stats" v-if="reviewStats">
              <el-rate v-model="reviewStats.averageRating" disabled show-score />
              <span class="review-count">{{ reviewStats.totalCount }} 条评价</span>
            </div>
          </div>
          
          <!-- 评价表单 -->
          <div class="review-form" v-if="userStore.isLoggedIn && !hasReviewed">
            <h3>发表评价</h3>
            <el-form :model="reviewForm" label-width="80px">
              <el-form-item label="评分">
                <el-rate v-model="reviewForm.rating" :max="5" show-score />
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input
                  v-model="reviewForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="分享您的游玩体验..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="上传图片">
                <el-upload
                  v-model:file-list="reviewForm.imageList"
                  action="/api/upload/image"
                  list-type="picture-card"
                  :headers="uploadHeaders"
                  :limit="9"
                  :on-success="handleImageSuccess"
                  :on-remove="handleImageRemove"
                  accept="image/*"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSubmitReview">提交评价</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="review-tip" v-else-if="!userStore.isLoggedIn">
            <el-alert type="info" :closable="false">
              <template #default>
                <span>请先 <el-link type="primary" @click="router.push('/login')">登录</el-link> 后发表评价</span>
              </template>
            </el-alert>
          </div>
          
          <div class="review-tip" v-else-if="hasReviewed">
            <el-alert type="success" :closable="false">您已经评价过该景点了</el-alert>
          </div>
          
          <!-- 评价列表 -->
          <div class="review-list">
            <div class="review-item" v-for="review in reviews" :key="review.id">
              <div class="review-user">
                <el-avatar :src="review.userAvatar || '/default-avatar.png'" />
                <div class="user-info">
                  <div class="username">{{ review.username }}</div>
                  <div class="review-time">{{ formatDate(review.createTime) }}</div>
                </div>
              </div>
              <div class="review-content">
                <el-rate v-model="review.rating" disabled />
                <p>{{ review.content }}</p>
                <!-- 评价图片 -->
                <div class="review-images" v-if="review.images">
                  <el-image
                    v-for="(img, i) in parseImages(review.images)"
                    :key="i"
                    :src="img"
                    :preview-src-list="parseImages(review.images)"
                    fit="cover"
                    class="review-image"
                  />
                </div>
                <!-- 点赞 -->
                <div class="review-actions">
                  <el-button 
                    :type="review.liked ? 'primary' : 'default'" 
                    size="small" 
                    text
                    @click="handleLike(review)"
                  >
                    <el-icon><Star /></el-icon>
                    {{ review.likeCount || 0 }} 有用
                  </el-button>
                </div>
              </div>
            </div>
            
            <el-empty v-if="reviews.length === 0" description="暂无评价" />
            
            <el-pagination
              v-if="reviewTotal > reviewPageSize"
              v-model:current-page="reviewPage"
              :page-size="reviewPageSize"
              :total="reviewTotal"
              layout="prev, pager, next"
              @current-change="loadReviews"
              style="margin-top: 20px; text-align: center"
            />
          </div>
        </div>
      </div>
      <div class="sidebar">
        <!-- 天气信息 -->
        <div class="weather-section" v-if="detail.latitude && detail.longitude">
          <h3>🌤️ 当地天气</h3>
          <WeatherCard 
            :latitude="Number(detail.latitude)" 
            :longitude="Number(detail.longitude)" 
          />
        </div>
        
        <h3>位置信息</h3>
        <div class="map-section">
          <MapContainer
            v-if="detail.latitude && detail.longitude"
            :latitude="Number(detail.latitude)"
            :longitude="Number(detail.longitude)"
            :title="detail.name"
            :current-scenic-id="detail.id"
            :show-nearby="true"
            :zoom="13"
            style="height: 400px"
          />
          <div v-else class="no-location">
            <el-empty description="暂无位置信息" :image-size="80" />
          </div>
        </div>
        
        <!-- 景点详细信息面板 -->
        <div class="info-panel" v-if="detail.latitude && detail.longitude">
          <h4>{{ detail.name }}</h4>
          <div class="info-item">
            <span class="label">📍 地址：</span>
            <span class="value">{{ detail.address || '浙江省' }}</span>
          </div>
          <div class="info-item">
            <span class="label">💰 门票：</span>
            <span class="value price">¥{{ detail.ticketPrice }}</span>
          </div>
          <div class="info-item">
            <span class="label">🕐 开放时间：</span>
            <span class="value">{{ detail.openingHours || '全天开放' }}</span>
          </div>
          <div class="info-item">
            <span class="label">⭐ 评分：</span>
            <span class="value">{{ detail.rating }} 分</span>
          </div>
          <div class="info-item">
            <span class="label">👁️ 浏览量：</span>
            <span class="value">{{ detail.viewCount }} 人</span>
          </div>
          <div class="info-tags" v-if="tags.length">
            <el-tag v-for="tag in tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getScenicDetail } from '@/api/scenic'
import { createOrder } from '@/api/order'
import { recordInteraction } from '@/api/recommend'
import { submitReview, getReviewList, getReviewStats, toggleReviewLike } from '@/api/review'
import { addFavorite, removeFavorite as removeFavoriteApi, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus, Star } from '@element-plus/icons-vue'
import MapContainer from '@/components/MapContainer.vue'
import WeatherCard from '@/components/WeatherCard.vue'
import ShareButton from '@/components/ShareButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const detail = ref(null)
const reviews = ref([])
const reviewStats = ref(null)
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)
const hasReviewed = ref(false)
const isFavorite = ref(false)
const reviewForm = ref({
  rating: 5,
  content: '',
  imageList: [],
  images: []
})

const uploadHeaders = {
  'satoken': localStorage.getItem('satoken') || ''
}

const images = computed(() => {
  if (!detail.value?.images) return ['https://via.placeholder.com/800x400']
  try { return JSON.parse(detail.value.images) }
  catch { return ['https://via.placeholder.com/800x400'] }
})

const tags = computed(() => {
  if (!detail.value?.tags) return []
  try { return JSON.parse(detail.value.tags) }
  catch { return [] }
})

const hasDetailInfo = computed(() => {
  return detail.value?.address || 
         detail.value?.trafficGuide || 
         detail.value?.travelTips || 
         detail.value?.recommendedDuration || 
         detail.value?.bestSeason
})

onMounted(async () => {
  const id = route.params.id
  detail.value = await getScenicDetail(id)
  
  // 记录浏览行为
  if (userStore.isLoggedIn) {
    recordInteraction({ targetType: 'SCENIC', targetId: id, actionType: 'VIEW' })
    
    // 检查是否已收藏
    try {
      isFavorite.value = await checkFavorite('SCENIC', id)
    } catch (e) {
      console.error('检查收藏状态失败', e)
    }
  }
  
  // 加载评价数据
  await loadReviews()
  await loadReviewStats()
})

const loadReviews = async () => {
  const id = route.params.id
  const res = await getReviewList({
    targetType: 'SCENIC',
    targetId: id,
    page: reviewPage.value,
    size: reviewPageSize.value
  })
  reviews.value = res.records || []
  reviewTotal.value = res.total || 0
  
  // 检查当前用户是否已评价
  if (userStore.isLoggedIn) {
    hasReviewed.value = reviews.value.some(r => r.userId === userStore.user?.id)
  }
}

const loadReviewStats = async () => {
  const id = route.params.id
  reviewStats.value = await getReviewStats({
    targetType: 'SCENIC',
    targetId: id
  })
}

const handleSubmitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  try {
    await submitReview({
      targetType: 'SCENIC',
      targetId: route.params.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      images: reviewForm.value.images.length > 0 ? JSON.stringify(reviewForm.value.images) : null
    })
    ElMessage.success('评价提交成功')
    reviewForm.value.content = ''
    reviewForm.value.rating = 5
    reviewForm.value.imageList = []
    reviewForm.value.images = []
    hasReviewed.value = true
    await loadReviews()
    await loadReviewStats()
    // 刷新景点详情以更新评分
    detail.value = await getScenicDetail(route.params.id)
  } catch (e) {
    // 错误已处理
  }
}

const handleImageSuccess = (response) => {
  if (response.code === 200 && response.data?.url) {
    reviewForm.value.images.push(response.data.url)
  }
}

const handleImageRemove = (file) => {
  const url = file.response?.data?.url || file.url
  const index = reviewForm.value.images.indexOf(url)
  if (index > -1) {
    reviewForm.value.images.splice(index, 1)
  }
}

const parseImages = (images) => {
  if (!images) return []
  try { return JSON.parse(images) }
  catch { return [] }
}

const handleLike = async (review) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const liked = await toggleReviewLike(review.id)
    review.liked = liked
    review.likeCount = (review.likeCount || 0) + (liked ? 1 : -1)
  } catch (e) {
    // 错误已处理
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const handleBook = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 直接跳转到支付页面
  router.push({
    path: '/payment',
    query: {
      productType: 'SCENIC',
      productId: detail.value.id,
      productName: detail.value.name,
      amount: detail.value.ticketPrice
    }
  })
}

const handleToggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    if (isFavorite.value) {
      await removeFavoriteApi('SCENIC', detail.value.id)
      ElMessage.success('已取消收藏')
      isFavorite.value = false
    } else {
      await addFavorite('SCENIC', detail.value.id)
      ElMessage.success('收藏成功')
      isFavorite.value = true
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleShareSuccess = (data) => {
  ElMessage.success('分享成功！获得5积分奖励')
}
</script>

<style lang="scss" scoped>
.scenic-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.detail-header {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  
  .images {
    flex: 1;
    border-radius: 12px;
    overflow: hidden;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  
  .info {
    width: 400px;
    h1 { font-size: 28px; margin-bottom: 16px; }
    .rating { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
    .meta { color: #666; margin-bottom: 16px; p { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; } }
    .tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
    .price {
      background: #fff5f5;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 20px;
      .label { color: #999; }
      .value { font-size: 32px; color: #f56c6c; font-weight: 600; margin-left: 8px; }
    }
    
    .action-buttons {
      display: flex;
      gap: 12px;
      
      .el-button {
        flex: 1;
      }
    }
  }
}

.detail-content {
  display: flex;
  gap: 40px;
  
  .main {
    flex: 1;
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    h2 { font-size: 20px; margin-bottom: 16px; }
    p { line-height: 1.8; color: #666; }
    
    .detail-info-section {
      margin-top: 32px;
      
      .info-card {
        background: #f9f9f9;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 16px;
        
        h3 {
          font-size: 16px;
          color: #333;
          margin-bottom: 12px;
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        p {
          color: #666;
          line-height: 1.8;
          margin: 0;
          
          &.multiline {
            white-space: pre-line;
          }
        }
        
        &.half {
          flex: 1;
        }
      }
      
      .info-row {
        display: flex;
        gap: 16px;
        
        .info-card {
          margin-bottom: 16px;
        }
      }
    }
  }
  
  .sidebar {
    width: 400px;
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    h3 { font-size: 16px; margin-bottom: 16px; }
    
    .weather-section {
      margin-bottom: 32px;
    }
    
    .map-section {
      margin-bottom: 24px;
      border-radius: 8px;
      overflow: hidden;
      
      .no-location {
        height: 300px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;
      }
    }
    
    .info-panel {
      background: #f9f9f9;
      padding: 16px;
      border-radius: 8px;
      
      h4 {
        font-size: 16px;
        margin-bottom: 12px;
        color: #333;
      }
      
      .info-item {
        display: flex;
        margin-bottom: 10px;
        font-size: 14px;
        line-height: 1.6;
        
        .label {
          color: #666;
          min-width: 90px;
        }
        
        .value {
          color: #333;
          flex: 1;
          
          &.price {
            color: #f56c6c;
            font-weight: 600;
            font-size: 16px;
          }
        }
      }
      
      .info-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid #e8e8e8;
      }
    }
  }
}

.review-section {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid #eee;
  
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h2 { margin: 0; }
    
    .review-stats {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .review-count {
        color: #999;
        font-size: 14px;
      }
    }
  }
  
  .review-form {
    background: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 24px;
    
    h3 {
      font-size: 16px;
      margin-bottom: 16px;
    }
  }
  
  .review-tip {
    margin-bottom: 24px;
  }
  
  .review-list {
    .review-item {
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .review-user {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        
        .user-info {
          .username {
            font-weight: 500;
            margin-bottom: 4px;
          }
          
          .review-time {
            font-size: 12px;
            color: #999;
          }
        }
      }
      
      .review-content {
        padding-left: 52px;
        
        p {
          margin-top: 8px;
          line-height: 1.6;
          color: #666;
        }
        
        .review-images {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-top: 12px;
          
          .review-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            cursor: pointer;
          }
        }
        
        .review-actions {
          margin-top: 12px;
          
          .el-button {
            color: #999;
            
            &.el-button--primary {
              color: #409eff;
            }
          }
        }
      }
    }
  }
}
</style>
