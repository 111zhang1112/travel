<template>
  <div class="ranking-page">
    <div class="page-header">
      <h1>热门排行榜</h1>
      <p>发现最受欢迎的景点和酒店</p>
    </div>
    
    <div class="ranking-content">
      <!-- 景点排行榜 -->
      <div class="ranking-section">
        <div class="section-header">
          <h2><el-icon><Trophy /></el-icon> 景点热门榜</h2>
        </div>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in scenicRanking" 
            :key="item.id" 
            class="ranking-item"
            @click="router.push(`/scenic/${item.id}`)"
          >
            <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
            <div class="item-image">
              <img :src="getFirstImage(item.images)" :alt="item.name" />
            </div>
            <div class="item-info">
              <h3>{{ item.name }}</h3>
              <div class="item-meta">
                <el-rate v-model="item.rating" disabled size="small" />
                <span class="rating-text">{{ item.rating }}分</span>
              </div>
              <div class="item-stats">
                <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}浏览</span>
                <span class="price">¥{{ item.ticketPrice }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="scenicRanking.length === 0" description="暂无数据" />
        </div>
      </div>
      
      <!-- 酒店排行榜 -->
      <div class="ranking-section">
        <div class="section-header">
          <h2><el-icon><Trophy /></el-icon> 酒店热门榜</h2>
        </div>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in hotelRanking" 
            :key="item.id" 
            class="ranking-item"
            @click="router.push(`/hotel/${item.id}`)"
          >
            <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
            <div class="item-image">
              <img :src="getFirstImage(item.images)" :alt="item.name" />
            </div>
            <div class="item-info">
              <h3>{{ item.name }}</h3>
              <div class="item-meta">
                <div class="stars">
                  <el-icon v-for="i in item.starRating" :key="i" color="#f7ba2a"><Star /></el-icon>
                </div>
                <el-rate v-model="item.rating" disabled size="small" />
                <span class="rating-text">{{ item.rating }}分</span>
              </div>
              <div class="item-stats">
                <span class="price">¥{{ item.priceMin }} - ¥{{ item.priceMax }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="hotelRanking.length === 0" description="暂无数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getScenicRanking, getHotelRanking } from '@/api/ranking'
import { Trophy, View, Star } from '@element-plus/icons-vue'

const router = useRouter()
const scenicRanking = ref([])
const hotelRanking = ref([])

onMounted(async () => {
  scenicRanking.value = await getScenicRanking({ limit: 10 }) || []
  hotelRanking.value = await getHotelRanking({ limit: 10 }) || []
})

const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
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
</script>

<style lang="scss" scoped>
.ranking-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  
  h1 {
    font-size: 32px;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  p {
    color: #666;
    font-size: 16px;
  }
}

.ranking-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  
  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.ranking-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  .section-header {
    margin-bottom: 20px;
    
    h2 {
      font-size: 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #333;
      
      .el-icon {
        color: #f7ba2a;
      }
    }
  }
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .rank-badge {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #e0e0e0;
    color: #666;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
    
    &.gold {
      background: linear-gradient(135deg, #ffd700, #ffb700);
      color: #fff;
    }
    
    &.silver {
      background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
      color: #fff;
    }
    
    &.bronze {
      background: linear-gradient(135deg, #cd7f32, #b87333);
      color: #fff;
    }
  }
  
  .item-image {
    width: 80px;
    height: 60px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .item-info {
    flex: 1;
    min-width: 0;
    
    h3 {
      font-size: 15px;
      margin-bottom: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .item-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      
      .stars {
        display: flex;
        gap: 2px;
      }
      
      .rating-text {
        font-size: 12px;
        color: #f7ba2a;
      }
    }
    
    .item-stats {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #999;
      
      .price {
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }
}
</style>
