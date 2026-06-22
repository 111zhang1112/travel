<template>
  <div class="hotel-detail-page" v-if="detail">
    <!-- 日期选择区域 -->
    <div class="date-selector-section">
      <div class="date-selector-container">
        <div class="date-label">
          <el-icon><Calendar /></el-icon>
          <span>选择入住日期</span>
        </div>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="退房日期"
          :disabled-date="disabledDate"
          :clearable="false"
          size="large"
          style="width: 400px"
        />
        <div class="nights-display">
          <span class="nights-count">{{ nights }}</span>
          <span class="nights-text">晚</span>
        </div>
      </div>
    </div>
    
    <div class="detail-header">
      <div class="images">
        <el-carousel height="400px">
          <el-carousel-item v-for="(img, i) in images" :key="i">
            <img :src="img" :alt="detail.name" />
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="info">
        <div class="name-row">
          <h1>{{ detail.name }}</h1>
          <div class="stars">
            <el-icon v-for="i in detail.starRating" :key="i" color="#f7ba2a"><Star /></el-icon>
          </div>
        </div>
        <p class="address"><el-icon><Location /></el-icon> {{ detail.address || '市中心位置' }}</p>
        <div class="amenities" v-if="amenities.length">
          <el-tag v-for="a in amenities" :key="a" size="small">{{ a }}</el-tag>
        </div>
        <div class="price-range">
          <span class="label">价格区间</span>
          <span class="value">¥{{ detail.priceMin }} - ¥{{ detail.priceMax }}</span>
        </div>
        <div class="action-buttons" style="display: flex; gap: 10px;">
          <el-button 
            :type="isFavorite ? 'warning' : 'default'" 
            size="large" 
            @click="handleToggleFavorite"
            :icon="Star"
            style="flex: 1"
          >
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
          <ShareButton
            v-if="detail"
            content-type="hotel"
            :content-id="detail.id"
            :content-data="{
              name: detail.name,
              coverImage: images[0],
              minPrice: detail.priceMin,
              starRating: detail.starRating,
              description: detail.description
            }"
            size="large"
            @share-success="handleShareSuccess"
          />
        </div>
      </div>
    </div>
    
    <!-- 房间列表 -->
    <div class="room-section" v-if="rooms.length">
      <h2>可选房型</h2>
      <div class="room-list">
        <div class="room-card" v-for="room in rooms" :key="room.id">
          <!-- 房型图片轮播 -->
          <div class="room-images" v-if="parseRoomImages(room.images).length">
            <el-carousel height="200px" indicator-position="inside">
              <el-carousel-item v-for="(img, i) in parseRoomImages(room.images)" :key="i">
                <el-image 
                  :src="img" 
                  fit="cover" 
                  style="width: 100%; height: 100%"
                  :preview-src-list="parseRoomImages(room.images)"
                  :initial-index="i"
                />
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <div class="room-content">
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <div class="room-tags">
                <el-tag size="small" type="info">{{ room.bedType }}</el-tag>
                <el-tag size="small" type="info">{{ room.area }}㎡</el-tag>
                <el-tag size="small" type="info">最多{{ room.maxGuests }}人</el-tag>
                <el-tag v-if="room.breakfast" size="small" type="success">含早餐</el-tag>
                <el-tag v-if="room.wifi" size="small">WiFi</el-tag>
                <el-tag v-if="room.window" size="small" type="warning">有窗</el-tag>
              </div>
              <p class="room-desc" v-if="room.description">{{ room.description }}</p>
            </div>
            <div class="room-action">
              <div class="room-price">
                <span class="price">¥{{ room.price }}</span>
                <span class="unit">/晚</span>
              </div>
              <div class="room-stock" :class="{ low: room.availableRooms <= 3, out: room.availableRooms === 0 }">
                <el-icon v-if="room.availableRooms <= 3 && room.availableRooms > 0"><Warning /></el-icon>
                <span v-if="room.availableRooms > 0">剩余 {{ room.availableRooms }} 间</span>
                <span v-else>已订完</span>
              </div>
              <el-button 
                type="primary" 
                :disabled="room.availableRooms === 0" 
                @click="handleBookRoom(room)"
                size="large"
              >
                {{ room.availableRooms === 0 ? '已订完' : '立即预订' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="detail-content">
      <div class="main">
        <h2>酒店介绍</h2>
        <p>{{ detail.description }}</p>
        
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
                  placeholder="分享您的入住体验..."
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
            <el-alert type="success" :closable="false">您已经评价过该酒店了</el-alert>
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
        <h3>位置信息</h3>
        <MapContainer
          v-if="detail.latitude && detail.longitude"
          :latitude="Number(detail.latitude)"
          :longitude="Number(detail.longitude)"
          :title="detail.name"
          style="height: 300px"
        />
      </div>
    </div>
  </div>
  
  <!-- 预订确认对话框 -->
  <el-dialog
    v-model="bookingDialogVisible"
    title="确认预订"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="booking-dialog-content" v-if="selectedRoom">
      <div class="booking-summary">
        <h3>预订信息</h3>
        <div class="summary-item">
          <span class="label">酒店名称：</span>
          <span class="value">{{ detail.name }}</span>
        </div>
        <div class="summary-item">
          <span class="label">房型名称：</span>
          <span class="value">{{ selectedRoom.name }}</span>
        </div>
        <div class="summary-item">
          <span class="label">入住日期：</span>
          <span class="value">{{ dayjs(dateRange[0]).format('YYYY年MM月DD日') }}</span>
        </div>
        <div class="summary-item">
          <span class="label">退房日期：</span>
          <span class="value">{{ dayjs(dateRange[1]).format('YYYY年MM月DD日') }}</span>
        </div>
        <div class="summary-item">
          <span class="label">入住天数：</span>
          <span class="value highlight">{{ nights }} 晚</span>
        </div>
      </div>
      
      <el-divider />
      
      <div class="quantity-selector">
        <span class="label">房间数量：</span>
        <el-input-number
          v-model="roomQuantity"
          :min="1"
          :max="selectedRoom.availableRooms"
          size="large"
        />
        <span class="stock-hint">（剩余 {{ selectedRoom.availableRooms }} 间）</span>
      </div>
      
      <el-divider />
      
      <div class="price-calculator">
        <div class="price-item">
          <span class="label">房间单价：</span>
          <span class="value">¥{{ selectedRoom.price }} / 晚</span>
        </div>
        <div class="price-item">
          <span class="label">房间数量：</span>
          <span class="value">{{ roomQuantity }} 间</span>
        </div>
        <div class="price-item">
          <span class="label">入住天数：</span>
          <span class="value">{{ nights }} 晚</span>
        </div>
        <el-divider />
        <div class="price-item total">
          <span class="label">订单总价：</span>
          <span class="value">¥{{ totalPrice }}</span>
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="bookingDialogVisible = false" size="large">取消</el-button>
      <el-button type="primary" @click="handleConfirmBooking" size="large">确认预订</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHotelDetail, getHotelRooms } from '@/api/hotel'
import { submitReview, getReviewList, getReviewStats, toggleReviewLike } from '@/api/review'
import { addFavorite, removeFavorite as removeFavoriteApi, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus, Star, Location, Calendar, Warning } from '@element-plus/icons-vue'
import MapContainer from '@/components/MapContainer.vue'
import ShareButton from '@/components/ShareButton.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const detail = ref(null)
const rooms = ref([])
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

// 日期选择状态
const dateRange = ref([
  dayjs().toDate(),
  dayjs().add(1, 'day').toDate()
])

// 预订对话框状态
const bookingDialogVisible = ref(false)
const selectedRoom = ref(null)
const roomQuantity = ref(1)

const uploadHeaders = {
  'satoken': localStorage.getItem('satoken') || ''
}

// 计算入住天数
const nights = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) return 1
  const checkIn = dayjs(dateRange.value[0])
  const checkOut = dayjs(dateRange.value[1])
  return checkOut.diff(checkIn, 'day')
})

// 格式化日期显示
const formatDateRange = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) return ''
  return `${dayjs(dateRange.value[0]).format('MM月DD日')} - ${dayjs(dateRange.value[1]).format('MM月DD日')}`
})

// 计算订单总价
const totalPrice = computed(() => {
  if (!selectedRoom.value) return 0
  return (selectedRoom.value.price * nights.value * roomQuantity.value).toFixed(2)
})

const images = computed(() => {
  if (!detail.value?.images) return ['https://via.placeholder.com/800x400']
  try { return JSON.parse(detail.value.images) }
  catch { return ['https://via.placeholder.com/800x400'] }
})

const amenities = computed(() => {
  if (!detail.value?.amenities) return []
  try { return JSON.parse(detail.value.amenities) }
  catch { return [] }
})

// 禁用今天之前的日期
const disabledDate = (time) => {
  const today = dayjs().startOf('day')
  const testDate = dayjs(time).startOf('day')
  return testDate.isBefore(today)
}

onMounted(async () => {
  const hotelId = route.params.id
  detail.value = await getHotelDetail(hotelId)
  rooms.value = await getHotelRooms(hotelId) || []
  
  // 检查是否已收藏
  if (userStore.isLoggedIn) {
    try {
      isFavorite.value = await checkFavorite('HOTEL', hotelId)
    } catch (e) {
      console.error('检查收藏状态失败', e)
    }
  }
  
  // 加载评价数据
  await loadReviews()
  await loadReviewStats()
})

const loadReviews = async () => {
  const hotelId = route.params.id
  const res = await getReviewList({
    targetType: 'HOTEL',
    targetId: hotelId,
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
  const hotelId = route.params.id
  reviewStats.value = await getReviewStats({
    targetType: 'HOTEL',
    targetId: hotelId
  })
}

const handleSubmitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  try {
    await submitReview({
      targetType: 'HOTEL',
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
    // 刷新酒店详情以更新评分
    detail.value = await getHotelDetail(route.params.id)
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

const parseRoomImages = (images) => {
  if (!images) return []
  try { 
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  }
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

const handleBookRoom = async (room) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 打开预订对话框
  selectedRoom.value = room
  roomQuantity.value = 1
  bookingDialogVisible.value = true
}

const handleConfirmBooking = async () => {
  if (!selectedRoom.value) return
  
  // 跳转到支付页面
  bookingDialogVisible.value = false
  router.push({
    path: '/payment',
    query: {
      productType: 'HOTEL',
      productId: detail.value.id,
      productName: `${detail.value.name} - ${selectedRoom.value.name}`,
      amount: totalPrice.value,
      quantity: roomQuantity.value,
      checkInDate: dayjs(dateRange.value[0]).format('YYYY-MM-DD'),
      checkOutDate: dayjs(dateRange.value[1]).format('YYYY-MM-DD'),
      roomId: selectedRoom.value.id,
      nights: nights.value
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
      await removeFavoriteApi('HOTEL', detail.value.id)
      ElMessage.success('已取消收藏')
      isFavorite.value = false
    } else {
      await addFavorite('HOTEL', detail.value.id)
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
.hotel-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.date-selector-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .date-selector-container {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .date-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: #333;
      min-width: 140px;
    }
    
    .nights-display {
      display: flex;
      align-items: baseline;
      gap: 4px;
      padding: 8px 16px;
      background: #f0f9ff;
      border-radius: 8px;
      
      .nights-count {
        font-size: 24px;
        font-weight: 600;
        color: #409eff;
      }
      
      .nights-text {
        font-size: 14px;
        color: #666;
      }
    }
  }
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
    .name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    h1 { font-size: 28px; }
    .address { color: #666; margin-bottom: 16px; display: flex; align-items: center; gap: 4px; }
    .amenities { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
    .price-range {
      background: #fff5f5;
      padding: 16px;
      border-radius: 8px;
      .label { color: #999; }
      .value { font-size: 24px; color: #f56c6c; font-weight: 600; margin-left: 8px; }
    }
  }
}

.room-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 40px;
  
  h2 {
    font-size: 20px;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
  }
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.room-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #eee;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  }
  
  .room-images {
    width: 300px;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
  }
  
  .room-content {
    flex: 1;
    display: flex;
    justify-content: space-between;
  }
  
  .room-info {
    flex: 1;
    
    h3 {
      font-size: 18px;
      margin-bottom: 10px;
    }
    
    .room-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 8px;
    }
    
    .room-desc {
      color: #999;
      font-size: 13px;
      margin: 0;
    }
  }
  
  .room-action {
    text-align: right;
    min-width: 140px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .room-price {
      margin-bottom: 8px;
      
      .price {
        font-size: 24px;
        color: #f56c6c;
        font-weight: 600;
      }
      
      .unit {
        color: #999;
        font-size: 12px;
      }
    }
    
    .room-stock {
      font-size: 12px;
      color: #67c23a;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 4px;
      
      &.low {
        color: #e6a23c;
      }
      
      &.out {
        color: #f56c6c;
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
  }
  
  .sidebar {
    width: 400px;
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    h3 { font-size: 16px; margin-bottom: 16px; }
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

.booking-dialog-content {
  .booking-summary {
    h3 {
      font-size: 16px;
      margin-bottom: 16px;
      color: #333;
    }
    
    .summary-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      
      .label {
        color: #666;
      }
      
      .value {
        color: #333;
        font-weight: 500;
        
        &.highlight {
          color: #409eff;
          font-size: 16px;
        }
      }
    }
  }
  
  .quantity-selector {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .label {
      font-size: 14px;
      color: #666;
    }
    
    .stock-hint {
      font-size: 12px;
      color: #999;
    }
  }
  
  .price-calculator {
    .price-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      
      .label {
        color: #666;
      }
      
      .value {
        color: #333;
        font-weight: 500;
      }
      
      &.total {
        .label {
          font-size: 18px;
          font-weight: 600;
          color: #333;
        }
        
        .value {
          font-size: 24px;
          font-weight: 600;
          color: #f56c6c;
        }
      }
    }
  }
}

// 移动端响应式适配
@media (max-width: 768px) {
  .hotel-detail-page {
    padding: 20px 10px;
  }
  
  .date-selector-section {
    .date-selector-container {
      flex-direction: column;
      align-items: stretch;
      
      .date-label {
        min-width: auto;
      }
      
      :deep(.el-date-editor) {
        width: 100% !important;
      }
      
      .nights-display {
        justify-content: center;
      }
    }
  }
  
  .detail-header {
    flex-direction: column;
    gap: 20px;
    
    .info {
      width: 100%;
    }
  }
  
  .room-card {
    flex-direction: column;
    
    .room-images {
      width: 100%;
    }
    
    .room-content {
      flex-direction: column;
      gap: 16px;
    }
    
    .room-action {
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      min-width: auto;
      
      .room-price {
        margin-bottom: 0;
      }
      
      .room-stock {
        margin-bottom: 0;
      }
    }
  }
  
  .detail-content {
    flex-direction: column;
    
    .sidebar {
      width: 100%;
    }
  }
  
  .booking-dialog-content {
    .quantity-selector {
      flex-direction: column;
      align-items: stretch;
      
      .stock-hint {
        text-align: center;
      }
    }
  }
}
</style>
