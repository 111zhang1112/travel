<template>
  <div class="search-history" v-if="historyList.length > 0">
    <div class="history-header">
      <span class="history-title">
        <el-icon><Clock /></el-icon>
        搜索历史
      </span>
      <el-button 
        link 
        type="danger" 
        size="small" 
        @click="handleClearAll">
        清空
      </el-button>
    </div>
    <div class="history-list">
      <el-tag
        v-for="item in historyList"
        :key="item.id"
        class="history-item"
        closable
        @click="handleSearch(item.keyword)"
        @close="handleDelete(item.id)">
        {{ item.keyword }}
      </el-tag>
    </div>
  </div>
  <div v-else class="empty-history">
    <el-empty description="暂无搜索历史" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import axios from 'axios'

const emit = defineEmits(['search'])

const historyList = ref([])

// 加载搜索历史
const loadHistory = async () => {
  try {
    const response = await axios.get('/api/search/history', {
      params: { limit: 10 }
    })
    if (response.data.success) {
      historyList.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
    // 如果未登录，从本地存储加载
    loadLocalHistory()
  }
}

// 从本地存储加载历史
const loadLocalHistory = () => {
  try {
    const localHistory = localStorage.getItem('searchHistory')
    if (localHistory) {
      const history = JSON.parse(localHistory)
      historyList.value = history.slice(0, 10)
    }
  } catch (error) {
    console.error('加载本地搜索历史失败:', error)
  }
}

// 点击搜索
const handleSearch = (keyword) => {
  emit('search', keyword)
}

// 删除单条历史
const handleDelete = async (id) => {
  try {
    const response = await axios.delete(`/api/search/history/${id}`)
    if (response.data.success) {
      historyList.value = historyList.value.filter(item => item.id !== id)
      ElMessage.success('删除成功')
    }
  } catch (error) {
    console.error('删除搜索历史失败:', error)
    // 如果是本地存储，直接删除
    deleteLocalHistory(id)
  }
}

// 删除本地历史
const deleteLocalHistory = (id) => {
  try {
    historyList.value = historyList.value.filter(item => item.id !== id)
    localStorage.setItem('searchHistory', JSON.stringify(historyList.value))
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除本地历史失败:', error)
  }
}

// 清空所有历史
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有搜索历史吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete('/api/search/history/clear')
    if (response.data.success) {
      historyList.value = []
      ElMessage.success('清空成功')
    }
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('清空搜索历史失败:', error)
    // 如果是本地存储，直接清空
    clearLocalHistory()
  }
}

// 清空本地历史
const clearLocalHistory = () => {
  try {
    localStorage.removeItem('searchHistory')
    historyList.value = []
    ElMessage.success('清空成功')
  } catch (error) {
    console.error('清空本地历史失败:', error)
  }
}

// 暴露刷新方法
const refresh = () => {
  loadHistory()
}

defineExpose({ refresh })

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.search-history {
  padding: 12px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  cursor: pointer;
  transition: all 0.3s;
}

.history-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-history {
  padding: 20px 0;
}
</style>
