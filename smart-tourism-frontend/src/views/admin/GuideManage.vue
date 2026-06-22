<template>
  <div class="guide-manage">
    <div class="page-header">
      <h2>攻略管理</h2>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadData">
        <el-option label="待审核" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="viewCount" label="浏览量" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row.id)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <span v-else class="text-muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadData" style="margin-top: 20px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetGuideList, adminApproveGuide, adminRejectGuide } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const page = ref(1)
const total = ref(0)
const statusFilter = ref(null)

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetGuideList({ page: page.value, size: 10, status: statusFilter.value })
  list.value = res.records || []
  total.value = res.total || 0
}

const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const statusText = (s) => ({ 0: '待审核', 1: '已发布', 2: '已拒绝' }[s] || '未知')

const handleApprove = async (id) => {
  await ElMessageBox.confirm('确认通过该攻略？', '审核确认')
  await adminApproveGuide(id)
  ElMessage.success('审核通过')
  loadData()
}

const handleReject = async (id) => {
  await ElMessageBox.confirm('确认拒绝该攻略？', '审核确认')
  await adminRejectGuide(id)
  ElMessage.success('已拒绝')
  loadData()
}
</script>

<style lang="scss" scoped>
.guide-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; } }
.text-muted { color: #999; font-size: 13px; }
</style>
