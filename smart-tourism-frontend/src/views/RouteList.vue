<template>
  <div class="route-list">
    <div class="page-header">
      <h1>精选路线</h1>
      <p>为您精心策划的旅游路线</p>
    </div>

    <div class="route-container">
      <div class="route-grid">
        <div v-for="route in routes" :key="route.id" class="route-card card-hover" @click="goToDetail(route.id)">
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

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <el-empty v-if="routes.length === 0 && !loading" description="暂无路线数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRoutes } from '@/api/system'
import { ElMessage } from 'element-plus'

const router = useRouter()

const routes = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const loadRoutes = async () => {
  loading.value = true
  try {
    const res = await getRoutes({
      page: currentPage.value,
      size: pageSize.value
    })
    routes.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载路线列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadRoutes()
}

const handleCurrentChange = () => {
  loadRoutes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (id) => {
  router.push(`/routes/${id}`)
}

onMounted(() => {
  loadRoutes()
})
</script>

<style lang="scss" scoped>
.route-list {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  text-align: center;
  padding: 60px 20px;

  h1 {
    font-size: 36px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  p {
    font-size: 16px;
    opacity: 0.9;
  }
}

.route-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.route-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;

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
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    position: relative;
    height: 220px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .days-badge {
      position: absolute;
      top: 12px;
      right: 12px;
      background: var(--primary-color);
      color: #fff;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 600;
    }
  }

  .card-content {
    padding: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-bottom: 10px;
    }

    .route-desc {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 44px;
    }

    .card-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .price {
        color: #f56c6c;
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
