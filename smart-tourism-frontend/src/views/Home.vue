<template>
  <div class="home">
    <!-- Banner 轮播 -->
    <section class="banner">
      <el-carousel height="400px" :interval="5000">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="banner-content">
              <h1>{{ item.title }}</h1>
              <p>{{ item.subtitle }}</p>
              <div class="banner-actions">
                <el-button type="primary" size="large" round @click="router.push('/scenic')">
                  立即探索
                </el-button>
                <el-button type="default" size="large" round @click="router.push('/routes')">
                  查看路线
                </el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 公告滚动栏 -->
    <section class="notice-bar" v-if="notices.length > 0">
      <div class="notice-container">
        <div class="notice-icon">
          <el-icon><Bell /></el-icon>
          <span>公告</span>
        </div>
        <div class="notice-content">
          <el-carousel height="32px" direction="vertical" :autoplay="true" :interval="4000" indicator-position="none">
            <el-carousel-item v-for="notice in notices" :key="notice.id">
              <div class="notice-item" @click="showNoticeDetail(notice)">
                <el-tag size="small" :type="getNoticeTagType(notice.type)">{{ getNoticeTypeName(notice.type) }}</el-tag>
                <span class="notice-title">{{ notice.title }}</span>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </section>

    <!-- 搜索入口 -->
    <section class="search-section">
      <div class="search-container">
        <EnhancedSearch />
      </div>
    </section>

    <!-- 推荐景点 -->
    <section class="section recommend-section">
      <div class="section-header">
        <h2>为你推荐</h2>
        <span class="hint">基于你的浏览偏好</span>
      </div>
      <!-- 骨架屏 -->
      <div v-if="loading" class="scenic-grid">
        <div v-for="i in 8" :key="i" class="scenic-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 200px" />
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <div style="display: flex; gap: 16px; margin-top: 12px">
                  <el-skeleton-item variant="text" style="width: 30%" />
                  <el-skeleton-item variant="text" style="width: 30%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div v-else class="scenic-grid">
        <div v-for="spot in recommendations" :key="spot.id" class="scenic-card card-hover" @click="goToDetail(spot.id)">
          <div class="card-image">
            <!-- 多图轮播 -->
            <el-carousel 
              v-if="parseImages(spot.images).length > 1"
              height="200px" 
              :interval="3000" 
              indicator-position="none"
              arrow="hover"
              :autoplay="true"
            >
              <el-carousel-item v-for="(img, idx) in parseImages(spot.images)" :key="idx">
                <img :src="img" :alt="spot.name" />
              </el-carousel-item>
            </el-carousel>
            <img v-else :src="getFirstImage(spot.images)" :alt="spot.name" />
            <div class="recommend-badge">推荐</div>
            <div v-if="parseImages(spot.images).length > 1" class="image-count">{{ parseImages(spot.images).length }}张</div>
          </div>
          <div class="card-content">
            <h3>{{ spot.name }}</h3>
            <div class="card-meta">
              <span class="rating">
                <el-icon><Star /></el-icon>
                {{ spot.rating }}
              </span>
              <span class="price">¥{{ spot.ticketPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门目的地 -->
    <section class="section region-section">
      <div class="section-header">
        <h2>热门目的地</h2>
        <span class="hint">探索浙江各地美景</span>
      </div>
      
      <!-- 地区标签导航 -->
      <div class="region-tabs">
        <div 
          v-for="region in regions" 
          :key="region" 
          class="region-tab"
          :class="{ active: selectedRegion === region }"
          @click="selectRegion(region)"
        >
          {{ region }}
        </div>
      </div>
      
      <!-- 地区景点展示 -->
      <div v-if="loadingRegion" class="scenic-grid">
        <div v-for="i in 8" :key="i" class="scenic-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 200px" />
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <div style="display: flex; gap: 16px; margin-top: 12px">
                  <el-skeleton-item variant="text" style="width: 30%" />
                  <el-skeleton-item variant="text" style="width: 30%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div v-else-if="regionScenics.length > 0" class="scenic-grid">
        <div v-for="spot in regionScenics" :key="spot.id" class="scenic-card card-hover" @click="goToDetail(spot.id)">
          <div class="card-image">
            <!-- 多图轮播 -->
            <el-carousel 
              v-if="parseImages(spot.images).length > 1"
              height="200px" 
              :interval="3000" 
              indicator-position="none"
              arrow="hover"
              :autoplay="true"
            >
              <el-carousel-item v-for="(img, idx) in parseImages(spot.images)" :key="idx">
                <img :src="img" :alt="spot.name" />
              </el-carousel-item>
            </el-carousel>
            <img v-else :src="getFirstImage(spot.images)" :alt="spot.name" />
            <div class="region-badge">{{ spot.location }}</div>
            <div class="price-tag">¥{{ spot.ticketPrice }}</div>
            <div v-if="parseImages(spot.images).length > 1" class="image-count">{{ parseImages(spot.images).length }}张</div>
          </div>
          <div class="card-content">
            <h3>{{ spot.name }}</h3>
            <div class="card-meta">
              <span class="rating">
                <el-icon><Star /></el-icon>
                {{ spot.rating }}
              </span>
              <span class="views">{{ spot.viewCount }} 人浏览</span>
            </div>
            <div class="tags" v-if="spot.tags">
              <el-tag v-for="tag in parseTags(spot.tags).slice(0, 3)" :key="tag" size="small" type="info">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <el-empty description="该地区暂无景点数据" />
      </div>
    </section>

    <!-- 精选酒店 -->
    <section class="section featured-hotel-section">
      <div class="section-header">
        <h2>精选酒店</h2>
        <router-link to="/hotel" class="more">查看更多 →</router-link>
      </div>
      <!-- 骨架屏 -->
      <div v-if="loading" class="hotel-grid">
        <div v-for="i in 8" :key="i" class="hotel-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 200px" />
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 60%" />
                <div style="display: flex; gap: 16px; margin-top: 12px">
                  <el-skeleton-item variant="text" style="width: 30%" />
                  <el-skeleton-item variant="text" style="width: 30%" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div v-else class="hotel-grid">
        <div v-for="hotel in hotels" :key="hotel.id" class="hotel-card card-hover" @click="goToHotelDetail(hotel.id)">
          <div class="card-image">
            <!-- 多图轮播 -->
            <el-carousel 
              v-if="parseImages(hotel.images).length > 1"
              height="200px" 
              :interval="3000" 
              indicator-position="none"
              arrow="hover"
              :autoplay="true"
            >
              <el-carousel-item v-for="(img, idx) in parseImages(hotel.images)" :key="idx">
                <img :src="img" :alt="hotel.name" />
              </el-carousel-item>
            </el-carousel>
            <img v-else :src="getFirstImage(hotel.images)" :alt="hotel.name" />
            <div class="price-tag">¥{{ hotel.minPrice }}起</div>
            <div v-if="parseImages(hotel.images).length > 1" class="image-count">{{ parseImages(hotel.images).length }}张</div>
          </div>
          <div class="card-content">
            <h3>{{ hotel.name }}</h3>
            <div class="card-meta">
              <span class="rating">
                <el-icon><Star /></el-icon>
                {{ hotel.rating }}
              </span>
              <span class="address">{{ hotel.address }}</span>
            </div>
            <div class="tags" v-if="hotel.tags">
              <el-tag v-for="tag in parseTags(hotel.tags).slice(0, 3)" :key="tag" size="small" type="info">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 精选路线 -->
    <section class="section featured-route-section">
      <div class="section-header">
        <h2>精选路线</h2>
        <router-link to="/routes" class="more">查看更多 →</router-link>
      </div>
      <!-- 骨架屏 -->
      <div v-if="loading" class="route-grid">
        <div v-for="i in 4" :key="i" class="route-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="height: 200px" />
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 70%" />
                <el-skeleton-item variant="text" style="margin-top: 12px" />
                <el-skeleton-item variant="text" style="margin-top: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div v-else class="route-grid">
        <div v-for="route in featuredRoutes" :key="route.id" class="route-card card-hover" @click="goToRouteDetail(route.id)">
          <div class="card-image">
            <img :src="route.coverImage || 'https://via.placeholder.com/400x250'" :alt="route.name" />
            <div class="days-badge">{{ route.days }}天{{ route.days - 1 }}晚</div>
          </div>
          <div class="card-content">
            <h3>{{ route.name }}</h3>
            <p class="route-desc">{{ route.description }}</p>
            <div class="card-footer">
              <span class="price">¥{{ route.price }}</span>
              <el-button type="primary" size="small">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>

  <!-- 公告详情弹窗 -->
  <el-dialog v-model="noticeDialogVisible" :title="currentNotice?.title" width="600px">
    <div class="notice-detail">
      <div class="notice-meta">
        <el-tag size="small" :type="getNoticeTagType(currentNotice?.type)">{{ getNoticeTypeName(currentNotice?.type) }}</el-tag>
        <span class="notice-time">{{ formatTime(currentNotice?.createTime) }}</span>
      </div>
      <div class="notice-body">{{ currentNotice?.content }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getScenicList, getScenicByRegion, getRegions } from '@/api/scenic'
import { getHotelList } from '@/api/hotel'
import { getRecommendScenic } from '@/api/recommend'
import { getBanners, getNotices, getFeaturedRoutes } from '@/api/system'
import { Bell, Star } from '@element-plus/icons-vue'
import EnhancedSearch from '@/components/EnhancedSearch.vue'

const router = useRouter()

const loading = ref(true)
const loadingRegion = ref(false)
const banners = ref([])
const notices = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref(null)

const scenicSpots = ref([])
const hotels = ref([])
const recommendations = ref([])
const featuredRoutes = ref([])

// 地区相关
const regions = ref(['全部', '杭州', '宁波', '温州', '绍兴', '湖州', '金华', '衢州', '舟山', '台州', '丽水'])
const selectedRegion = ref('全部')
const regionScenics = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const [scenicRes, hotelRes, recommendRes, bannerRes, noticeRes, routeRes] = await Promise.all([
      getScenicList({ page: 1, size: 8 }),
      getHotelList({ page: 1, size: 8 }),
      getRecommendScenic({ limit: 8 }),
      getBanners(),
      getNotices(),
      getFeaturedRoutes({ limit: 4 })
    ])
    scenicSpots.value = scenicRes.records || []
    hotels.value = hotelRes.records || []
    recommendations.value = recommendRes || []
    banners.value = (bannerRes || []).map(b => ({
      id: b.id,
      title: b.title,
      subtitle: b.subtitle,
      image: b.imageUrl
    }))
    notices.value = noticeRes || []
    featuredRoutes.value = routeRes || []
    
    // 加载地区数据
    await loadRegionData()
  } catch (e) {
    console.error('Failed to load data', e)
  } finally {
    loading.value = false
  }
})

// 加载地区数据
const loadRegionData = async () => {
  try {
    // 获取数据库中的地区列表
    const dbRegions = await getRegions()
    if (dbRegions && dbRegions.length > 0) {
      regions.value = ['全部', ...dbRegions]
    }
    
    // 默认加载全部地区的景点
    await selectRegion('全部')
  } catch (e) {
    console.error('Failed to load regions', e)
  }
}

// 选择地区
const selectRegion = async (region) => {
  selectedRegion.value = region
  loadingRegion.value = true
  
  try {
    if (region === '全部') {
      // 加载热门景点
      const res = await getScenicList({ page: 1, size: 8 })
      regionScenics.value = res.records || []
    } else {
      // 按地区加载
      const res = await getScenicByRegion(region, 8)
      regionScenics.value = res || []
    }
  } catch (e) {
    console.error('Failed to load region scenics', e)
    regionScenics.value = []
  } finally {
    loadingRegion.value = false
  }
}

const getNoticeTypeName = (type) => {
  const types = { SYSTEM: '系统', ACTIVITY: '活动', NOTICE: '通知' }
  return types[type] || '公告'
}

const getNoticeTagType = (type) => {
  const types = { SYSTEM: 'danger', ACTIVITY: 'success', NOTICE: 'warning' }
  return types[type] || 'info'
}

const showNoticeDetail = (notice) => {
  currentNotice.value = notice
  noticeDialogVisible.value = true
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getFirstImage = (images) => {
  if (!images) return 'https://via.placeholder.com/400x300'
  try {
    const arr = JSON.parse(images)
    return arr[0] || 'https://via.placeholder.com/400x300'
  } catch {
    return 'https://via.placeholder.com/400x300'
  }
}

const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return arr.filter(Boolean)
  } catch {
    return []
  }
}

const parseTags = (tags) => {
  if (!tags) return []
  try {
    return JSON.parse(tags)
  } catch {
    return []
  }
}

const goToDetail = (id) => {
  router.push(`/scenic/${id}`)
}

const goToHotelDetail = (id) => {
  router.push(`/hotel/${id}`)
}

const goToRouteDetail = (id) => {
  router.push(`/routes/${id}`)
}
</script>

<style lang="scss" scoped>
.home {
  background: #f5f7fa;
  min-height: 100vh;
}

.banner {
  .banner-item {
    height: 100%;
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .banner-content {
    text-align: center;
    color: #fff;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

    h1 {
      font-size: 48px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    p {
      font-size: 20px;
      margin-bottom: 32px;
    }

    .banner-actions {
      display: flex;
      gap: 16px;
      justify-content: center;

      .el-button {
        padding: 16px 40px;
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
}

.search-section {
  background: #f5f7fa;
  padding: 32px 0;

  .search-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
}

.notice-bar {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  .notice-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 12px 20px;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .notice-icon {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #ff6b35;
    font-weight: 500;
    white-space: nowrap;
    font-size: 14px;

    .el-icon {
      font-size: 16px;
    }
  }

  .notice-content {
    flex: 1;
    overflow: hidden;
  }

  .notice-item {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    height: 32px;
    line-height: 32px;

    &:hover .notice-title {
      color: var(--primary-color);
    }

    .notice-title {
      color: #666;
      font-size: 13px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transition: color 0.2s;
    }
  }
}

.notice-detail {
  .notice-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;

    .notice-time {
      color: #999;
      font-size: 13px;
    }
  }

  .notice-body {
    color: #333;
    line-height: 1.8;
    font-size: 14px;
    white-space: pre-wrap;
  }
}

.section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: '';
      width: 3px;
      height: 18px;
      background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
      border-radius: 2px;
    }
  }

  .more {
    color: #999;
    text-decoration: none;
    font-size: 13px;
    font-weight: 400;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 4px;

    &:hover {
      color: var(--primary-color);
    }
  }

  .hint {
    color: rgba(255, 255, 255, 0.85);
    font-size: 13px;
    font-weight: 400;
  }
}

.scenic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.route-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.route-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    position: relative;
    height: 180px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .days-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }
  }

  .card-content {
    padding: 14px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .route-desc {
      font-size: 12px;
      color: #999;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      height: 36px;
    }

    .card-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .price {
        color: #ff6b6b;
        font-size: 18px;
        font-weight: 600;
      }

      .el-button {
        font-size: 13px;
        padding: 6px 16px;
      }
    }
  }
}

.scenic-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    position: relative;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    :deep(.el-carousel) {
      height: 200px;
      
      .el-carousel__container {
        height: 200px;
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .price-tag {
      position: absolute;
      bottom: 8px;
      right: 8px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
      color: #fff;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 600;
      z-index: 10;
      box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
    }

    .recommend-badge {
      position: absolute;
      top: 8px;
      left: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      z-index: 10;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }
    
    .image-count {
      position: absolute;
      top: 8px;
      right: 8px;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      padding: 3px 8px;
      border-radius: 10px;
      font-size: 11px;
      z-index: 10;
      backdrop-filter: blur(4px);
    }
  }

  .card-content {
    padding: 14px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 13px;
      color: #999;
      margin-bottom: 8px;

      .rating {
        display: flex;
        align-items: center;
        gap: 3px;
        color: #ff9500;
        font-weight: 500;
      }

      .price {
        color: #ff6b6b;
        font-weight: 600;
        font-size: 16px;
      }

      .views {
        color: #999;
        font-size: 12px;
      }
    }

    .tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
}

.recommend-section {
  background: #f0f4ff;
  max-width: none;
  padding: 32px 20px;
  margin-bottom: 0;

  .section-header {
    max-width: 1200px;
    margin: 0 auto 20px;

    h2 {
      color: #333;
      font-size: 22px;
      
      &::before {
        background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
      }
    }
    
    .hint {
      color: #999;
    }
  }

  .scenic-grid {
    max-width: 1200px;
    margin: 0 auto;
  }
}

.region-section {
  background: #fff;
  max-width: none;
  padding: 32px 20px;
  margin-bottom: 0;

  .section-header {
    max-width: 1200px;
    margin: 0 auto 20px;

    h2 {
      color: #333;
      font-size: 22px;
      
      &::before {
        background: linear-gradient(180deg, #ff6b6b 0%, #ff8e53 100%);
      }
    }
    
    .hint {
      color: #999;
    }
  }

  .region-tabs {
    max-width: 1200px;
    margin: 0 auto 24px;
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding: 8px 0;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 2px;
    }

    .region-tab {
      flex-shrink: 0;
      padding: 8px 20px;
      background: #f5f5f5;
      border-radius: 20px;
      font-size: 14px;
      color: #666;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;

      &:hover {
        background: #fff;
        color: #ff6b6b;
        border-color: #ff6b6b;
      }

      &.active {
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
        color: #fff;
        font-weight: 500;
        box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
      }
    }
  }

  .scenic-grid {
    max-width: 1200px;
    margin: 0 auto;
  }

  .empty-state {
    max-width: 1200px;
    margin: 40px auto;
    text-align: center;
  }

  .region-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
    color: #fff;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    z-index: 10;
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  }
}

.featured-hotel-section {
  background: #fff;
}

.hotel-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.hotel-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    position: relative;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    :deep(.el-carousel) {
      height: 200px;
      
      .el-carousel__container {
        height: 200px;
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .price-tag {
      position: absolute;
      bottom: 8px;
      right: 8px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
      color: #fff;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 600;
      z-index: 10;
      box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
    }
    
    .image-count {
      position: absolute;
      top: 8px;
      right: 8px;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      padding: 3px 8px;
      border-radius: 10px;
      font-size: 11px;
      z-index: 10;
      backdrop-filter: blur(4px);
    }
  }

  .card-content {
    padding: 14px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 13px;
      color: #999;
      margin-bottom: 8px;

      .rating {
        display: flex;
        align-items: center;
        gap: 3px;
        color: #ff9500;
        font-weight: 500;
      }

      .address {
        color: #999;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-left: 8px;
      }
    }

    .tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
}

.featured-route-section {
  background: #f9fafb;
}

.card-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

</style>
