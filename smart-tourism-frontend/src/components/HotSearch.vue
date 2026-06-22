<template>
  <div class="hot-search">
    <div class="hot-header">
      <span class="hot-title">
        <el-icon><TrendCharts /></el-icon>
        热门搜索
      </span>
    </div>
    <div class="hot-list" v-loading="loading">
      <el-tag
        v-for="item in hotList"
        :key="item.ranking"
        :type="getTagType(item.ranking)"
        :effect="item.ranking <= 3 ? 'dark' : 'plain'"
        class="hot-item"
        @click="handleSearch(item.keyword)">
        <span class="ranking">{{ item.ranking }}</span>
        <span class="keyword">{{ item.keyword }}</span>
        <span class="count" v-if="showCount">{{ item.searchCount }}</span>
      </el-tag>
    </div>
    <div v-if="!loading && hotList.length === 0" class="empty-hot">
      <el-empty description="暂无热门搜索" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps({
  showCount: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search'])

const hotList = ref([])
const loading = ref(false)

// 加载热门搜索
const loadHotSearches = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/search/hot', {
      params: { limit: 10 }
    })
    if (response.data.success) {
      hotList.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载热门搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取标签类型
const getTagType = (ranking) => {
  if (ranking === 1) return 'danger'
  if (ranking === 2) return 'warning'
  if (ranking === 3) return 'success'
  return 'info'
}

// 点击搜索
const handleSearch = (keyword) => {
  emit('search', keyword)
}

// 暴露刷新方法
const refresh = () => {
  loadHotSearches()
}

defineExpose({ refresh })

onMounted(() => {
  loadHotSearches()
})
</script>

<style scoped>
.hot-search {
  padding: 12px 0;
}

.hot-header {
  margin-bottom: 12px;
}

.hot-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.hot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
}

.hot-item {
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
}

.hot-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ranking {
  font-weight: bold;
  font-size: 12px;
}

.keyword {
  font-size: 13px;
}

.count {
  font-size: 11px;
  opacity: 0.7;
  margin-left: 4px;
}

.empty-hot {
  padding: 20px 0;
}
</style>
