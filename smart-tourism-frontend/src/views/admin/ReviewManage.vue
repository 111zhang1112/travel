<template>
  <div class="review-manage">
    <div class="page-header">
      <h2>评价管理</h2>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.targetType" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="景点" value="SCENIC" />
            <el-option label="酒店" value="HOTEL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedIds.length > 0">
      <span>已选择 {{ selectedIds.length }} 项</span>
      <el-button type="success" size="small" @click="batchAudit(1)">批量通过</el-button>
      <el-button type="danger" size="small" @click="batchAudit(2)">批量拒绝</el-button>
    </div>
    
    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.targetType === 'SCENIC' ? 'primary' : 'success'" size="small">
            {{ row.targetType === 'SCENIC' ? '景点' : '酒店' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetName" label="目标名称" width="150" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column label="评分" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <template v-if="row.images">
            <el-image
              v-for="(img, i) in parseImages(row.images).slice(0, 3)"
              :key="i"
              :src="img"
              :preview-src-list="parseImages(row.images)"
              style="width: 30px; height: 30px; margin-right: 4px"
              fit="cover"
            />
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" text @click="handleAudit(row.id, 1)">通过</el-button>
          <el-button v-if="row.status === 0" type="warning" size="small" text @click="handleAudit(row.id, 2)">拒绝</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminGetReviewList, adminAuditReview, adminBatchAuditReviews, adminDeleteReview } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const searchForm = reactive({
  status: null,
  targetType: ''
})
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminGetReviewList({
      page: pagination.page,
      size: pagination.size,
      status: searchForm.status,
      targetType: searchForm.targetType
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.status = null
  searchForm.targetType = ''
  pagination.page = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAudit = async (id, status) => {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}该评价？`, '审核确认')
  await adminAuditReview(id, status)
  ElMessage.success(`已${action}`)
  loadData()
}

const batchAudit = async (status) => {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认批量${action}选中的 ${selectedIds.value.length} 条评价？`, '批量审核')
  await adminBatchAuditReviews(selectedIds.value, status)
  ElMessage.success(`已批量${action}`)
  selectedIds.value = []
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该评价？', '删除确认')
  await adminDeleteReview(id)
  ElMessage.success('删除成功')
  loadData()
}

const parseImages = (images) => {
  if (!images) return []
  try { return JSON.parse(images) }
  catch { return [] }
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
.review-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  h2 { font-size: 20px; }
}

.filter-card {
  margin-bottom: 20px;
}

.batch-actions {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  
  span {
    color: #666;
    font-size: 14px;
  }
}
</style>
