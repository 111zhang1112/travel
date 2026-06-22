<template>
  <div class="order-manage">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadData">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column prop="productType" label="类型" width="100">
        <template #default="{ row }">{{ row.productType === 'SCENIC' ? '景点' : '酒店' }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PAID'" size="small" type="success" @click="handleComplete(row.orderNo)">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadData" style="margin-top: 20px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetOrderList, adminCompleteOrder } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const page = ref(1)
const total = ref(0)
const statusFilter = ref(null)

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetOrderList({ page: page.value, size: 10, status: statusFilter.value })
  list.value = res.records || []
  total.value = res.total || 0
}

const statusType = (s) => ({ PENDING: 'warning', PAID: 'primary', COMPLETED: 'success', CANCELLED: 'info' }[s] || 'info')
const statusText = (s) => ({ PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)

const handleComplete = async (orderNo) => {
  await ElMessageBox.confirm('确认完成该订单？', '操作确认')
  await adminCompleteOrder(orderNo)
  ElMessage.success('订单已完成')
  loadData()
}
</script>

<style lang="scss" scoped>
.order-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; } }
</style>
