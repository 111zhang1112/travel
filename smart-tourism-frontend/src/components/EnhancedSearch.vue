<template>
  <div class="enhanced-search">
    <div class="search-main">
      <!-- 搜索类型选择（仅在首页显示） -->
      <el-select 
        v-if="!disableNavigation"
        v-model="searchType" 
        class="search-type-select"
        placeholder="类型"
      >
        <el-option label="全部" value="all" />
        <el-option label="景点" value="scenic" />
        <el-option label="酒店" value="hotel" />
        <el-option label="路线" value="route" />
      </el-select>

      <!-- 搜索输入框 -->
      <div class="search-input-wrapper">
        <el-input
          ref="searchInputRef"
          v-model="searchKeyword"
          size="large"
          :placeholder="getPlaceholder()"
          class="search-input"
          @keyup.enter="handleSearch"
          @input="handleInput"
          @focus="showSuggestions = true"
          @blur="handleBlur"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <!-- 搜索建议下拉框 -->
        <div v-if="showSuggestions && (searchHistory.length > 0 || filteredSuggestions.length > 0)" class="suggestions-dropdown">
          <!-- 搜索历史 -->
          <div v-if="searchHistory.length > 0" class="suggestion-section">
            <div class="section-header">
              <span class="section-title">
                <el-icon><Clock /></el-icon>
                搜索历史
              </span>
              <el-button text size="small" @click="clearHistory">清空</el-button>
            </div>
            <div 
              v-for="(item, index) in searchHistory.slice(0, 5)" 
              :key="'history-' + index"
              class="suggestion-item"
              @mousedown.prevent="selectSuggestion(item)"
            >
              <el-icon><Clock /></el-icon>
              <span class="suggestion-text">{{ item }}</span>
              <el-icon class="remove-icon" @click.stop="removeHistory(index)"><Close /></el-icon>
            </div>
          </div>

          <!-- 搜索建议 -->
          <div v-if="filteredSuggestions.length > 0" class="suggestion-section">
            <div class="section-header">
              <span class="section-title">
                <el-icon><Search /></el-icon>
                搜索建议
              </span>
            </div>
            <div 
              v-for="(item, index) in filteredSuggestions" 
              :key="'suggestion-' + index"
              class="suggestion-item"
              @mousedown.prevent="selectSuggestion(item)"
            >
              <el-icon><Search /></el-icon>
              <span class="suggestion-text" v-html="highlightMatch(item)"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索按钮 -->
      <el-button type="primary" size="large" class="search-button" @click="handleSearch">
        搜索
      </el-button>

      <!-- 高级筛选按钮 -->
      <el-button size="large" class="filter-button" @click="showAdvancedFilter = !showAdvancedFilter">
        <el-icon><Filter /></el-icon>
        筛选
      </el-button>
    </div>

    <!-- 高级筛选面板 -->
    <el-collapse-transition>
      <div v-if="showAdvancedFilter" class="advanced-filter">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">价格区间：</span>
            <el-slider 
              v-model="priceRange" 
              range 
              :max="5000" 
              :step="50"
              :format-tooltip="formatPrice"
              style="width: 200px"
            />
            <span class="filter-value">¥{{ priceRange[0] }} - ¥{{ priceRange[1] }}</span>
          </div>

          <div class="filter-item">
            <span class="filter-label">地区：</span>
            <el-select v-model="selectedRegion" placeholder="选择地区" clearable style="width: 150px">
              <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
            </el-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">评分：</span>
            <el-rate v-model="minRating" show-score />
          </div>

          <!-- 酒店专属筛选 -->
          <template v-if="searchType === 'hotel' || currentPage === 'hotel'">
            <div class="filter-item">
              <span class="filter-label">星级：</span>
              <el-select v-model="starRating" placeholder="选择星级" clearable style="width: 120px">
                <el-option label="五星级" :value="5" />
                <el-option label="四星级" :value="4" />
                <el-option label="三星级" :value="3" />
                <el-option label="二星级" :value="2" />
                <el-option label="一星级" :value="1" />
              </el-select>
            </div>
          </template>

          <!-- 景点专属筛选 -->
          <template v-if="searchType === 'scenic' || currentPage === 'scenic'">
            <div class="filter-item">
              <span class="filter-label">分类：</span>
              <el-select v-model="selectedCategory" placeholder="选择分类" clearable style="width: 150px">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </div>
          </template>
        </div>

        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">排序：</span>
            <el-radio-group v-model="sortBy" size="small">
              <el-radio-button label="default">默认</el-radio-button>
              <el-radio-button label="price-asc">价格升序</el-radio-button>
              <el-radio-button label="price-desc">价格降序</el-radio-button>
              <el-radio-button label="rating">评分优先</el-radio-button>
              <el-radio-button label="popular">热度优先</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="filter-actions">
          <el-button size="small" @click="resetFilters">重置</el-button>
          <el-button type="primary" size="small" @click="applyFilters">应用筛选</el-button>
        </div>
      </div>
    </el-collapse-transition>

    <!-- 热门搜索 -->
    <div class="hot-keywords">
      <span class="label">热门搜索：</span>
      <div class="keywords-scroll">
        <el-tag
          v-for="keyword in hotKeywords"
          :key="keyword.keyword"
          size="small"
          class="keyword-tag"
          @click="searchByKeyword(keyword.keyword)"
        >
          {{ keyword.keyword }}
          <span v-if="keyword.count" class="keyword-count">{{ keyword.count }}</span>
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Clock, Close, Filter } from '@element-plus/icons-vue'
import { getHotKeywords } from '@/api/system'
import axios from 'axios'
import { saveLocalSearchHistory, getLocalSearchHistory } from '@/utils/searchHistory'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const emit = defineEmits(['search'])
const userStore = useUserStore()

// 接收属性
const props = defineProps({
  // 当前页面类型：'home', 'scenic', 'hotel', 'route'
  currentPage: {
    type: String,
    default: 'home'
  },
  // 是否禁用页面跳转（在列表页使用时设为true）
  disableNavigation: {
    type: Boolean,
    default: false
  }
})

// 搜索相关
const searchType = ref('all')
const searchKeyword = ref('')
const searchInputRef = ref(null)
const showSuggestions = ref(false)
const showAdvancedFilter = ref(false)

// 根据当前页面自动设置搜索类型
onMounted(() => {
  if (props.currentPage === 'scenic') {
    searchType.value = 'scenic'
  } else if (props.currentPage === 'hotel') {
    searchType.value = 'hotel'
  } else if (props.currentPage === 'route') {
    searchType.value = 'route'
  }
})

// 搜索历史（从localStorage读取）
const searchHistory = ref([])

// 搜索建议数据
const suggestions = ref([])

// 热门关键词
const hotKeywords = ref([])

// 高级筛选
const priceRange = ref([0, 5000])
const selectedRegion = ref('')
const minRating = ref(0)
const dateRange = ref([])
const starRating = ref(null)
const selectedCategory = ref(null)
const sortBy = ref('default')
const regions = ref(['杭州', '宁波', '温州', '绍兴', '湖州', '金华', '衢州', '舟山', '台州', '丽水'])
const categories = ref([])

// 防抖定时器
let debounceTimer = null

onMounted(() => {
  loadSearchHistory()
  loadHotKeywords()
  loadCategories()
})

// 加载搜索历史
const loadSearchHistory = async () => {
  // 如果已登录，从服务器加载
  if (userStore.isLoggedIn) {
    try {
      const response = await axios.get('/api/search/history', {
        params: { limit: 10 }
      })
      if (response.data.success) {
        searchHistory.value = response.data.data.map(item => item.keyword)
        return
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
  
  // 未登录或加载失败，从本地存储加载
  const localHistory = getLocalSearchHistory()
  searchHistory.value = localHistory.map(item => item.keyword)
}

// 保存搜索历史
const saveSearchHistory = async (keyword) => {
  if (!keyword || keyword.trim() === '') return
  
  // 如果已登录，保存到服务器
  if (userStore.isLoggedIn) {
    try {
      await axios.post('/api/search/history', {
        keyword: keyword.trim(),
        searchType: searchType.value
      })
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }
  
  // 同时保存到本地存储
  saveLocalSearchHistory(keyword.trim(), searchType.value)
  
  // 更新显示
  loadSearchHistory()
}

// 清空搜索历史
const clearHistory = async () => {
  if (userStore.isLoggedIn) {
    try {
      await axios.delete('/api/search/history/clear')
    } catch (error) {
      console.error('清空搜索历史失败:', error)
    }
  }
  
  // 清空本地存储
  localStorage.removeItem('searchHistory')
  searchHistory.value = []
}

// 删除单条历史
const removeHistory = async (index) => {
  const keyword = searchHistory.value[index]
  
  if (userStore.isLoggedIn) {
    try {
      // 需要找到对应的ID，这里简化处理，直接重新加载
      await loadSearchHistory()
    } catch (error) {
      console.error('删除搜索历史失败:', error)
    }
  }
  
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

// 加载热门关键词
const loadHotKeywords = async () => {
  try {
    const response = await axios.get('/api/search/hot', {
      params: { limit: 10 }
    })
    if (response.data.success) {
      hotKeywords.value = response.data.data.map(item => ({
        keyword: item.keyword,
        count: item.searchCount
      }))
      return
    }
  } catch (e) {
    console.error('加载热门搜索失败:', e)
  }
  
  // 使用默认数据
  hotKeywords.value = [
    { keyword: '西湖', count: 1250 },
    { keyword: '千岛湖', count: 980 },
    { keyword: '普陀山', count: 856 },
    { keyword: '乌镇', count: 742 },
    { keyword: '雁荡山', count: 635 },
    { keyword: '莫干山', count: 521 }
  ]
}

// 加载搜索建议（实时从服务器获取）
const loadSuggestions = async (keyword) => {
  if (!keyword || keyword.trim() === '') {
    suggestions.value = []
    return
  }
  
  try {
    const response = await axios.get('/api/search/suggestions', {
      params: { 
        keyword: keyword.trim(),
        limit: 8
      }
    })
    if (response.data.success) {
      suggestions.value = response.data.data.map(item => item.keyword)
    }
  } catch (error) {
    console.error('加载搜索建议失败:', error)
    suggestions.value = []
  }
}

// 过滤搜索建议
const filteredSuggestions = computed(() => {
  return suggestions.value
})

// 输入处理（带防抖）
const handleInput = () => {
  showSuggestions.value = true
  
  // 清除之前的定时器
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  
  // 设置新的定时器（300ms防抖）
  debounceTimer = setTimeout(() => {
    loadSuggestions(searchKeyword.value)
  }, 300)
}

// 高亮匹配文本
const highlightMatch = (text) => {
  if (!searchKeyword.value) return text
  const keyword = searchKeyword.value
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 失焦处理
const handleBlur = () => {
  // 延迟隐藏，以便点击建议项
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// 选择建议
const selectSuggestion = (keyword) => {
  searchKeyword.value = keyword
  showSuggestions.value = false
  handleSearch()
}

// 获取占位符
const getPlaceholder = () => {
  const placeholders = {
    all: '搜索景点、酒店、路线...',
    scenic: '搜索景点名称...',
    hotel: '搜索酒店名称...',
    route: '搜索路线名称...'
  }
  return placeholders[searchType.value] || placeholders.all
}

// 格式化价格
const formatPrice = (value) => {
  return `¥${value}`
}

// 加载景点分类
const loadCategories = async () => {
  try {
    const response = await axios.get('/api/scenic/categories')
    if (response.data.success) {
      categories.value = response.data.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 重置筛选
const resetFilters = () => {
  priceRange.value = [0, 5000]
  selectedRegion.value = ''
  minRating.value = 0
  dateRange.value = []
  starRating.value = null
  selectedCategory.value = null
  sortBy.value = 'default'
}

// 应用筛选
const applyFilters = () => {
  handleSearch()
}

// 执行搜索
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  
  // 如果有关键词，保存到搜索历史
  if (keyword) {
    saveSearchHistory(keyword)
  }
  
  // 构建搜索参数
  const params = {
    keyword,
    type: searchType.value,
    priceMin: priceRange.value[0],
    priceMax: priceRange.value[1],
    region: selectedRegion.value,
    minRating: minRating.value,
    starRating: starRating.value,
    categoryId: selectedCategory.value,
    sortBy: sortBy.value,
    dateRange: dateRange.value
  }
  
  // 触发搜索事件
  emit('search', params)
  
  // 构建查询参数
  const queryParams = {
    ...(keyword && { keyword }),  // 只有当keyword不为空时才添加
    ...(priceRange.value[0] > 0 && { priceMin: priceRange.value[0] }),
    ...(priceRange.value[1] < 5000 && { priceMax: priceRange.value[1] }),
    ...(selectedRegion.value && { region: selectedRegion.value }),
    ...(minRating.value > 0 && { minRating: minRating.value }),
    ...(starRating.value && { starRating: starRating.value }),
    ...(selectedCategory.value && { categoryId: selectedCategory.value }),
    ...(sortBy.value !== 'default' && { sortBy: sortBy.value })
  }
  
  // 如果禁用导航，使用 push 更新URL参数（会触发watch）
  if (props.disableNavigation) {
    router.push({
      path: route.path,
      query: queryParams
    })
    return
  }
  
  // 根据类型跳转到相应页面（仅在首页使用）
  const routes = {
    all: '/scenic',
    scenic: '/scenic',
    hotel: '/hotel',
    route: '/routes'
  }
  
  router.push({
    path: routes[searchType.value],
    query: queryParams
  })
}

// 通过关键词搜索
const searchByKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 监听搜索类型变化，重置筛选
watch(searchType, () => {
  resetFilters()
})
</script>

<style lang="scss" scoped>
.enhanced-search {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-main {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-type-select {
  width: 100px;
  flex-shrink: 0;
  
  :deep(.el-input__wrapper) {
    border-radius: 24px 0 0 24px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
  }
}

.search-input-wrapper {
  flex: 1;
  position: relative;
}

.search-input {
  :deep(.el-input__wrapper) {
    border-radius: 0;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
  }
}

// 当没有类型选择器时，搜索框左侧添加圆角
.enhanced-search:not(:has(.search-type-select)) .search-input {
  :deep(.el-input__wrapper) {
    border-radius: 24px 0 0 24px;
  }
}

.search-button {
  border-radius: 0 24px 24px 0;
  padding: 0 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 500;

  &:hover {
    opacity: 0.9;
  }
}

.filter-button {
  border-radius: 24px;
  padding: 0 20px;
}

.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.suggestion-section {
  padding: 8px 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid #f0f0f0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #999;
    font-weight: 500;
  }
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  .el-icon {
    color: #999;
    font-size: 14px;
  }
  
  .suggestion-text {
    flex: 1;
    font-size: 14px;
    color: #333;
    
    :deep(.highlight) {
      color: #667eea;
      font-weight: 600;
    }
  }
  
  .remove-icon {
    opacity: 0;
    transition: opacity 0.2s;
    
    &:hover {
      color: #f56c6c;
    }
  }
  
  &:hover .remove-icon {
    opacity: 1;
  }
}

.advanced-filter {
  margin-top: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .filter-label {
    font-size: 14px;
    color: #666;
    white-space: nowrap;
  }
  
  .filter-value {
    font-size: 13px;
    color: #999;
    margin-left: 8px;
  }
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.hot-keywords {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  
  .label {
    color: #999;
    font-size: 13px;
    white-space: nowrap;
  }
  
  .keywords-scroll {
    flex: 1;
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding: 4px 0;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 2px;
    }
  }
  
  .keyword-tag {
    flex-shrink: 0;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid #e0e0e0;
    background: #fff;
    display: flex;
    align-items: center;
    gap: 4px;
    
    &:hover {
      color: #667eea;
      border-color: #667eea;
      transform: translateY(-2px);
    }
    
    .keyword-count {
      font-size: 11px;
      color: #999;
      margin-left: 2px;
    }
  }
}
</style>
