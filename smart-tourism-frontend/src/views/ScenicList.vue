<template>
  <div class="scenic-list-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <EnhancedSearch 
        current-page="scenic" 
        :disable-navigation="true" 
        @search="handleSearch" 
      />
    </div>
    
    <!-- 景点列表 -->
    <div class="scenic-grid">
      <div v-for="spot in list" :key="spot.id" class="scenic-card card-hover" @click="goToDetail(spot.id)">
        <div class="card-image">
          <!-- 多图轮播 -->
          <el-carousel 
            v-if="parseImages(spot.images).length > 1"
            height="180px" 
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
          <div class="price-tag">¥{{ spot.ticketPrice }}</div>
          <div v-if="parseImages(spot.images).length > 1" class="image-count">{{ parseImages(spot.images).length }}张</div>
        </div>
        <div class="card-content">
          <h3>{{ spot.name }}</h3>
          <p class="desc">{{ spot.description?.substring(0, 60) }}...</p>
          <div class="card-meta">
            <span class="rating"><el-icon><Star /></el-icon> {{ spot.rating }}</span>
            <span class="views">{{ spot.viewCount }} 浏览</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getScenicList } from '@/api/scenic'
import { Star } from '@element-plus/icons-vue'
import EnhancedSearch from '@/components/EnhancedSearch.vue'

const router = useRouter()
const route = useRoute()
const list = ref([])
const page = ref(1)
const total = ref(0)

onMounted(() => {
  loadData()
})

// 监听路由变化，重新加载数据
watch(() => route.query, () => {
  page.value = 1
  loadData()
}, { deep: true })

const loadData = async () => {
  // 从URL查询参数构建请求参数
  const params = {
    page: page.value,
    size: 12,
    keyword: route.query.keyword || '',
    priceMin: route.query.priceMin ? Number(route.query.priceMin) : undefined,
    priceMax: route.query.priceMax ? Number(route.query.priceMax) : undefined,
    region: route.query.region || undefined,
    minRating: route.query.minRating ? Number(route.query.minRating) : undefined,
    categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
    sortBy: route.query.sortBy || undefined
  }
  
  const res = await getScenicList(params)
  list.value = res.records || []
  total.value = res.total || 0
}

// 处理搜索事件（由EnhancedSearch组件触发）
const handleSearch = (searchParams) => {
  // EnhancedSearch组件会自动处理路由跳转
  // 这里只需要重新加载数据
  page.value = 1
  loadData()
}

const getFirstImage = (images) => {
  if (!images) return 'https://via.placeholder.com/400x300'
  try { return JSON.parse(images)[0] || 'https://via.placeholder.com/400x300' }
  catch { return 'https://via.placeholder.com/400x300' }
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

const goToDetail = (id) => router.push(`/scenic/${id}`)
</script>

<style lang="scss" scoped>
.scenic-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.search-section {
  margin-bottom: 24px;
}

.scenic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.scenic-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  
  .card-image {
    position: relative;
    height: 180px;
    
    img { width: 100%; height: 100%; object-fit: cover; }
    
    :deep(.el-carousel) {
      height: 180px;
      
      .el-carousel__container {
        height: 180px;
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .price-tag {
      position: absolute;
      bottom: 10px;
      right: 10px;
      background: rgba(0,0,0,0.7);
      color: #fff;
      padding: 4px 12px;
      border-radius: 20px;
      font-weight: 600;
      z-index: 10;
    }
    
    .image-count {
      position: absolute;
      top: 10px;
      right: 10px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      z-index: 10;
    }
  }
  
  .card-content {
    padding: 16px;
    h3 { font-size: 16px; margin-bottom: 8px; }
    .desc { font-size: 13px; color: #999; margin-bottom: 8px; line-height: 1.5; }
    .card-meta {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: #999;
      .rating { color: #f7ba2a; display: flex; align-items: center; gap: 4px; }
    }
  }
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.card-hover {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}
</style>
