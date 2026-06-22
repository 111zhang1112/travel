<template>
  <div class="route-detail" v-loading="loading">
    <div class="detail-header" v-if="routeData">
      <div class="header-content">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/routes' }">路线</el-breadcrumb-item>
            <el-breadcrumb-item>{{ routeData.name }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ routeData.name }}</h1>
        <div class="header-meta">
          <span class="days">
            <el-icon><Calendar /></el-icon>
            {{ routeData.days }}天{{ routeData.days - 1 }}晚
          </span>
          <span class="price">¥{{ routeData.price }}/人起</span>
        </div>
      </div>
    </div>

    <div class="detail-container" v-if="routeData">
      <div class="main-content">
        <div class="cover-image">
          <img :src="routeData.coverImage || 'https://via.placeholder.com/800x400'" :alt="routeData.name" />
        </div>

        <div class="content-section">
          <h2>路线介绍</h2>
          <p class="description">{{ routeData.description }}</p>
        </div>

        <div class="content-section" v-if="scenicSpots.length > 0">
          <h2>包含景点</h2>
          <div class="scenic-list">
            <div v-for="spot in scenicSpots" :key="spot.id" class="scenic-item" @click="goToScenic(spot.id)">
              <div class="scenic-image">
                <img :src="getFirstImage(spot.images)" :alt="spot.name" />
              </div>
              <div class="scenic-info">
                <h4>{{ spot.name }}</h4>
                <p>{{ spot.description }}</p>
                <div class="scenic-meta">
                  <span class="rating">
                    <el-icon><Star /></el-icon>
                    {{ spot.rating }}
                  </span>
                  <span class="price">¥{{ spot.ticketPrice }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <div class="booking-card">
          <div class="price-info">
            <span class="label">价格</span>
            <span class="price">¥{{ routeData.price }}</span>
            <span class="unit">/人起</span>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="handleBooking" style="flex: 1">立即预订</el-button>
            <ShareButton
              v-if="routeData"
              content-type="route"
              :content-id="routeData.id"
              :content-data="{
                name: routeData.name,
                coverImage: routeData.coverImage,
                days: routeData.days,
                totalCost: routeData.price,
                description: routeData.description
              }"
              size="large"
              @share-success="handleShareSuccess"
            />
          </div>
          <div class="tips">
            <p><el-icon><InfoFilled /></el-icon> 此价格为参考价，实际价格以咨询为准</p>
          </div>
        </div>

        <div class="info-card">
          <h3>行程信息</h3>
          <div class="info-item">
            <span class="label">行程天数</span>
            <span class="value">{{ routeData.days }}天{{ routeData.days - 1 }}晚</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ formatTime(routeData.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRouteDetail } from '@/api/system'
import { getScenicDetail } from '@/api/scenic'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Calendar, Star, InfoFilled } from '@element-plus/icons-vue'
import ShareButton from '@/components/ShareButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const routeData = ref(null)
const loading = ref(false)
const scenicSpots = ref([])

const loadRouteDetail = async () => {
  loading.value = true
  try {
    const res = await getRouteDetail(route.params.id)
    routeData.value = res
    
    // 加载包含的景点信息
    if (res.scenicIds) {
      try {
        const ids = JSON.parse(res.scenicIds)
        const spotPromises = ids.map(id => getScenicDetail(id).catch(() => null))
        const spots = await Promise.all(spotPromises)
        scenicSpots.value = spots.filter(spot => spot !== null)
      } catch (e) {
        console.error('Failed to parse scenic IDs', e)
      }
    }
  } catch (e) {
    ElMessage.error('加载路线详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getFirstImage = (images) => {
  if (!images) return 'https://via.placeholder.com/200x150'
  try {
    const arr = JSON.parse(images)
    return arr[0] || 'https://via.placeholder.com/200x150'
  } catch {
    return 'https://via.placeholder.com/200x150'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

const handleBooking = () => {
  console.log('[RouteDetail] 开始预订流程')
  
  if (!userStore.isLoggedIn) {
    console.log('[RouteDetail] 用户未登录，跳转到登录页')
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!routeData.value) {
    console.error('[RouteDetail] 路线数据不存在')
    ElMessage.error('路线信息加载失败，请刷新页面重试')
    return
  }

  // 验证必要参数
  if (!routeData.value.id) {
    console.error('[RouteDetail] 路线ID不存在')
    ElMessage.error('路线ID无效')
    return
  }

  if (!routeData.value.name) {
    console.error('[RouteDetail] 路线名称不存在')
    ElMessage.error('路线名称无效')
    return
  }

  if (!routeData.value.price || routeData.value.price <= 0) {
    console.error('[RouteDetail] 路线价格无效:', routeData.value.price)
    ElMessage.error('路线价格无效')
    return
  }

  const bookingParams = {
    productType: 'ROUTE',
    productId: routeData.value.id,
    productName: routeData.value.name,
    amount: routeData.value.price
  }
  
  console.log('[RouteDetail] 预订参数:', bookingParams)
  console.log('[RouteDetail] 参数类型检查:', {
    productType: typeof bookingParams.productType,
    productId: typeof bookingParams.productId,
    productName: typeof bookingParams.productName,
    amount: typeof bookingParams.amount
  })

  // 直接跳转到支付页面
  router.push({
    path: '/payment',
    query: bookingParams
  })
  
  console.log('[RouteDetail] 已跳转到支付页面')
}

const handleShareSuccess = (data) => {
  ElMessage.success('分享成功！获得5积分奖励')
}

const goToScenic = (id) => {
  router.push(`/scenic/${id}`)
}

onMounted(() => {
  loadRouteDetail()
})
</script>

<script>
export default {
  name: 'RouteDetail'
}
</script>

<style lang="scss" scoped>
.route-detail {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 40px 20px;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;

    .breadcrumb {
      margin-bottom: 20px;

      :deep(.el-breadcrumb__inner) {
        color: rgba(255, 255, 255, 0.8);

        &:hover {
          color: #fff;
        }
      }

      :deep(.el-breadcrumb__separator) {
        color: rgba(255, 255, 255, 0.6);
      }
    }

    h1 {
      font-size: 32px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .header-meta {
      display: flex;
      align-items: center;
      gap: 24px;
      font-size: 16px;

      .days {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .price {
        font-size: 24px;
        font-weight: 600;
      }
    }
  }
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 30px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.main-content {
  .cover-image {
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 30px;

    img {
      width: 100%;
      height: 400px;
      object-fit: cover;
    }
  }

  .content-section {
    background: #fff;
    border-radius: 12px;
    padding: 30px;
    margin-bottom: 20px;

    h2 {
      font-size: 22px;
      font-weight: 600;
      color: #333;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid #f0f0f0;
    }

    .description {
      font-size: 15px;
      color: #666;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .scenic-list {
    display: grid;
    gap: 16px;

    .scenic-item {
      display: flex;
      gap: 16px;
      padding: 16px;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: var(--primary-color);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      }

      .scenic-image {
        flex-shrink: 0;
        width: 160px;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .scenic-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
        }

        p {
          font-size: 13px;
          color: #999;
          line-height: 1.6;
          margin-bottom: auto;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .scenic-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-top: 8px;

          .rating {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #f7ba2a;
            font-size: 14px;
          }

          .price {
            color: #f56c6c;
            font-weight: 600;
            font-size: 16px;
          }
        }
      }
    }
  }
}

.sidebar {
  .booking-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .price-info {
      text-align: center;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;

      .label {
        display: block;
        font-size: 14px;
        color: #999;
        margin-bottom: 8px;
      }

      .price {
        font-size: 32px;
        font-weight: 600;
        color: #f56c6c;
      }

      .unit {
        font-size: 14px;
        color: #999;
        margin-left: 4px;
      }
    }

    .el-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
    }

    .action-buttons {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .tips {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;

      p {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #999;
        line-height: 1.6;

        .el-icon {
          flex-shrink: 0;
        }
      }
    }
  }

  .info-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #999;
        font-size: 14px;
      }

      .value {
        color: #333;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}
</style>
