<template>
  <div class="simple-search">
    <div class="search-main">
      <!-- 搜索输入框 -->
      <el-input
        v-model="searchKeyword"
        size="large"
        placeholder="搜索景点、酒店、路线..."
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 搜索按钮 -->
      <el-button type="primary" size="large" class="search-button" @click="handleSearch">
        搜索
      </el-button>
    </div>

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
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getHotKeywords } from '@/api/system'

const router = useRouter()

const searchKeyword = ref('')
const hotKeywords = ref([])

onMounted(() => {
  loadHotKeywords()
})

// 加载热门关键词
const loadHotKeywords = async () => {
  try {
    const res = await getHotKeywords()
    hotKeywords.value = res || [
      { keyword: '西湖' },
      { keyword: '千岛湖' },
      { keyword: '普陀山' },
      { keyword: '乌镇' },
      { keyword: '雁荡山' },
      { keyword: '莫干山' }
    ]
  } catch (e) {
    // 使用默认数据
    hotKeywords.value = [
      { keyword: '西湖' },
      { keyword: '千岛湖' },
      { keyword: '普陀山' },
      { keyword: '乌镇' },
      { keyword: '雁荡山' },
      { keyword: '莫干山' }
    ]
  }
}

// 执行搜索
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  
  // 跳转到景点列表页
  router.push({
    path: '/scenic',
    query: { keyword }
  })
}

// 通过关键词搜索
const searchByKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}
</script>

<style lang="scss" scoped>
.simple-search {
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

.search-input {
  flex: 1;
  
  :deep(.el-input__wrapper) {
    border-radius: 24px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
  }
}

.search-button {
  border-radius: 24px;
  padding: 0 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 500;

  &:hover {
    opacity: 0.9;
  }
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
    
    &:hover {
      color: #667eea;
      border-color: #667eea;
      transform: translateY(-2px);
    }
  }
}
</style>
