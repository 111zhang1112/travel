<template>
  <div class="order-manage">
    <h2 class="page-title">订单管理</h2>
    
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchOrderNo"
          placeholder="请输入订单号搜索"
          style="width: 300px;"
          clearable
          @clear="loadList"
        >
          <template #append>
            <el-button @click="loadList">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-select v-model="searchStatus" placeholder="订单状态" style="width: 150px; margin-left: 12px;" @change="loadList" clearable>
          <el-option label="全部" value="" />
          <el-option label="待支付" value="PENDING" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      
      <el-table :data="orderList" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="产品名称" show-overflow-tooltip />
        <el-table-column prop="productType" label="产品类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.productType === 'SCENIC' ? 'success' : 'primary'" size="small">
              {{ row.productType === 'SCENIC' ? '景点' : '酒店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadList"
        @size-change="loadList"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="1" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">
          <el-tag :type="currentOrder.productType === 'SCENIC' ? 'success' : 'primary'" size="small">
            {{ currentOrder.productType === 'SCENIC' ? '景点' : '酒店' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)" size="small">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ currentOrder.userNickname }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="currentOrder.payTime">{{ currentOrder.payTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" v-if="currentOrder.remark">{{ currentOrder.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantOrders, getMerchantOrderDetail } from '@/api/merchant'

const loading = ref(false)
const orderList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchOrderNo = ref('')
const searchStatus = ref('')

const detailVisible = ref(false)
const currentOrder = ref(null)

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PAID': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || '未知'
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    if (searchOrderNo.value) {
      params.orderNo = searchOrderNo.value
    }
    if (searchStatus.value) {
      params.status = searchStatus.value
    }
    
    const res = await getMerchantOrders(params)
    orderList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (row) => {
  try {
    const res = await getMerchantOrderDetail(row.orderNo)
    currentOrder.value = res
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.order-manage {
  .page-title {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
  
  .toolbar {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
  }
}
</style>
