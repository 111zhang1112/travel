<template>
  <div class="companion-manage">
    <div class="page-header">
      <h2>结伴管理</h2>
      <div class="stats">
        <el-statistic title="待审核" :value="stats.pending || 0" />
        <el-statistic title="招募中" :value="stats.recruiting || 0" />
        <el-statistic title="已满员" :value="stats.full || 0" />
        <el-statistic title="已结束" :value="stats.ended || 0" />
      </div>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-select v-model="filter.status" placeholder="状态" clearable style="width: 150px">
          <el-option label="待审核" :value="0" />
          <el-option label="招募中" :value="1" />
          <el-option label="已满员" :value="2" />
          <el-option label="已结束" :value="3" />
          <el-option label="已拒绝" :value="4" />
          <el-option label="已删除" :value="5" />
        </el-select>
        <el-input v-model="filter.destination" placeholder="目的地" clearable style="width: 200px" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="destination" label="目的地" width="100" />
        <el-table-column label="日期" width="200">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column label="人数" width="80">
          <template #default="{ row }">
            {{ row.currentPeople }}/{{ row.peopleNeeded + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="发布者" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">招募中</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">已满员</el-tag>
            <el-tag v-else-if="row.status === 3" type="info">已结束</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="row.status === 5" type="info">已删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="warning" size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="结伴详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ currentRow.destination }}</el-descriptions-item>
        <el-descriptions-item label="出行日期">
          {{ currentRow.startDate }} 至 {{ currentRow.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="人数">
          {{ currentRow.currentPeople }}/{{ currentRow.peopleNeeded + 1 }}
        </el-descriptions-item>
        <el-descriptions-item label="预算范围">{{ getBudgetLabel(currentRow.budgetRange) }}</el-descriptions-item>
        <el-descriptions-item label="旅行风格">{{ getStyleLabel(currentRow.travelStyle) }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ getContactLabel(currentRow.contactMethod) }}</el-descriptions-item>
        <el-descriptions-item label="联系信息">{{ currentRow.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">{{ currentRow.content }}</el-descriptions-item>
        <el-descriptions-item label="其他要求">{{ currentRow.requirements || '无' }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentRow.nickname || currentRow.username }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="currentRow.rejectReason">
          {{ currentRow.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])
const stats = ref({})
const detailVisible = ref(false)
const currentRow = ref(null)

const filter = ref({
  status: null,
  destination: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
  loadStats()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/companion/list', {
      params: {
        page: pagination.value.page,
        size: pagination.value.size,
        status: filter.value.status,
        destination: filter.value.destination
      }
    })
    tableData.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    stats.value = await request.get('/admin/companion/stats')
  } catch (e) {
    console.error(e)
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该结伴信息？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await request.post(`/admin/companion/${row.id}/approve`)
    ElMessage.success('审核通过')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因',
      inputValidator: (value) => {
        if (!value) return '请输入拒绝原因'
        return true
      }
    })
    
    await request.post(`/admin/companion/${row.id}/reject`, { reason })
    ElMessage.success('已拒绝')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该结伴信息？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/admin/companion/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

const getBudgetLabel = (budget) => {
  const map = { LOW: '经济', MEDIUM: '中等', HIGH: '豪华' }
  return map[budget] || budget
}

const getStyleLabel = (style) => {
  const map = { LEISURE: '休闲', ADVENTURE: '冒险', CULTURE: '文化', FOOD: '美食' }
  return map[style] || style
}

const getContactLabel = (method) => {
  const map = { WECHAT: '微信', PHONE: '电话', QQ: 'QQ' }
  return map[method] || method
}
</script>

<style lang="scss" scoped>
.companion-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  h2 {
    margin-bottom: 16px;
  }
  
  .stats {
    display: flex;
    gap: 40px;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
