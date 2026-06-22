<template>
  <div class="hotel-list-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <EnhancedSearch 
        current-page="hotel" 
        :disable-navigation="true" 
        @search="handleSearch" 
      />
    </div>
    
    <!-- 酒店列表 -->
    <div class="hotel-list">
      <div v-for="hotel in list" :key="hotel.id" class="hotel-card card-hover" @click="goToDetail(hotel.id)">
        <div class="hotel-image">
          <img :src="getFirstImage(hotel.images)" :alt="hotel.name" />
        </div>
        <div class="hotel-info">
          <div class="hotel-header">
            <h3>{{ hotel.name }}</h3>
            <div class="star-rating">
              <el-icon v-for="i in hotel.starRating" :key="i" color="#f7ba2a"><Star /></el-icon>
            </div>
          </div>
          <p class="address"><el-icon><Location /></el-icon> {{ hotel.address || '市中心' }}</p>
          <p class="desc">{{ hotel.description?.substring(0, 100) }}...</p>
          <div class="hotel-footer">
            <div class="price">
              <span class="from">起</span>
              <span class="value">¥{{ hotel.priceMin }}</span>
            </div>
            <el-button type="primary">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getHotelList } from '@/api/hotel'
import { Star, Location } from '@element-plus/icons-vue'
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
    size: 10,
    keyword: route.query.keyword || '',
    priceMin: route.query.priceMin ? Number(route.query.priceMin) : undefined,
    priceMax: route.query.priceMax ? Number(route.query.priceMax) : undefined,
    starRating: route.query.starRating ? Number(route.query.starRating) : undefined,
    region: route.query.region || undefined,
    minRating: route.query.minRating ? Number(route.query.minRating) : undefined,
    sortBy: route.query.sortBy || undefined
  }
  
  const res = await getHotelList(params)
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
  if (!images) return 'https://via.placeholder.com/300x200'
  try { return JSON.parse(images)[0] || 'https://via.placeholder.com/300x200' }
  catch { return 'https://via.placeholder.com/300x200' }
}

const goToDetail = (id) => router.push(`/hotel/${id}`)
</script>

<style lang="scss" scoped>
.hotel-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.search-section {
  margin-bottom: 24px;
}

.hotel-list {
  margin-bottom: 32px;
}

.hotel-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .hotel-image {
    width: 280px;
    height: 200px;
    flex-shrink: 0;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  
  .hotel-info {
    flex: 1;
    padding: 20px;
    display: flex;
    flex-direction: column;
    
    .hotel-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      h3 { font-size: 18px; }
    }
    
    .address { 
      color: #999; 
      font-size: 13px; 
      margin-bottom: 8px; 
      display: flex; 
      align-items: center; 
      gap: 4px; 
    }
    
    .desc { 
      color: #666; 
      font-size: 14px; 
      line-height: 1.6; 
      flex: 1; 
    }
    
    .hotel-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16px;
      
      .price {
        .from { color: #999; font-size: 12px; }
        .value { font-size: 24px; color: #f56c6c; font-weight: 600; }
      }
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
}
</style>
