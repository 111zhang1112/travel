<template>
  <div class="guide-list-page">
    <div class="page-header">
      <h1>旅游攻略</h1>
    </div>
    
    <div class="guide-grid">
      <div v-for="guide in list" :key="guide.id" class="guide-card card-hover" @click="goToDetail(guide.id)">
        <div class="card-image">
          <img :src="guide.coverImage || 'https://via.placeholder.com/400x250'" :alt="guide.title" />
        </div>
        <div class="card-content">
          <h3>{{ guide.title }}</h3>
          <div class="author">
            <el-avatar :size="24">{{ guide.authorName?.charAt(0) }}</el-avatar>
            <span>{{ guide.authorName }}</span>
          </div>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ guide.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ guide.likeCount }}</span>
          </div>
          <div class="card-actions">
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
              text="分享"
              size="small"
              @share-success="handleShareSuccess"
            />
          </div>
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <el-pagination v-model:current-page="page" :page-size="12" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGuideList } from '@/api/guide'
import { ElMessage } from 'element-plus'
import { View, Star } from '@element-plus/icons-vue'
import ShareButton from '@/components/ShareButton.vue'

const router = useRouter()
const list = ref([])
const page = ref(1)
const total = ref(0)

onMounted(() => loadData())

const loadData = async () => {
  const res = await getGuideList({ page: page.value, size: 12 })
  list.value = res.records || []
  total.value = res.total || 0
}

const goToDetail = (id) => {
  router.push(`/guide/${id}`)
}

const handleShareSuccess = (data) => {
  ElMessage.success('分享成功！获得5积分奖励')
}
</script>

<style lang="scss" scoped>
.guide-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  margin-bottom: 30px;
  h1 { font-size: 28px; }
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.guide-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  
  .card-image {
    height: 180px;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  
  .card-content {
    padding: 16px;
    h3 { font-size: 16px; margin-bottom: 12px; line-height: 1.4; }
    .author { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #666; margin-bottom: 12px; }
    .stats { display: flex; gap: 16px; font-size: 13px; color: #999; span { display: flex; align-items: center; gap: 4px; } }
    .card-actions { margin-top: 12px; display: flex; justify-content: flex-end; }
  }
}

.pagination { margin-top: 40px; display: flex; justify-content: center; }
</style>
