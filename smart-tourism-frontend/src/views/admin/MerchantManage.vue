<template>
  <div class="merchant-manage">
    <h2 class="page-title">商家管理</h2>
    
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商家名称或用户名"
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
        <el-select v-model="searchStatus" placeholder="审核状态" style="width: 150px; margin-left: 12px;" @change="loadList" clearable>
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已禁用" value="DISABLED" />
        </el-select>
        <el-select v-model="searchType" placeholder="商家类型" style="width: 150px; margin-left: 12px;" @change="loadList" clearable>
          <el-option label="全部" value="" />
          <el-option label="景点商家" value="SCENIC" />
          <el-option label="酒店商家" value="HOTEL" />
        </el-select>
      </div>
      
      <el-table :data="merchantList" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="merchantName" label="商家名称" />
        <el-table-column prop="merchantType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.merchantType === 'SCENIC' ? 'success' : 'primary'" size="small">
              {{ row.merchantType === 'SCENIC' ? '景点' : '酒店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              link
              type="danger"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-if="row.status === 'DISABLED'"
              link
              type="success"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
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
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="商家详情" width="600px">
      <el-descriptions :column="1" border v-if="currentMerchant">
        <el-descriptions-item label="用户名">{{ currentMerchant.username }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentMerchant.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商家类型">
          <el-tag :type="currentMerchant.merchantType === 'SCENIC' ? 'success' : 'primary'">
            {{ currentMerchant.merchantType === 'SCENIC' ? '景点商家' : '酒店商家' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentMerchant.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号">{{ currentMerchant.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="商家地址">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="商家简介">{{ currentMerchant.description }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusType(currentMerchant.status)">
            {{ getStatusText(currentMerchant.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="currentMerchant.status === 'REJECTED'">
          {{ currentMerchant.rejectReason }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentMerchant.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetMerchantList, adminDisableMerchant, adminEnableMerchant } from '@/api/admin'

const loading = ref(false)
const merchantList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStatus = ref('')
const searchType = ref('')

const detailVisible = ref(false)
const currentMerchant = ref(null)

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'DISABLED': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'DISABLED': '已禁用'
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
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (searchStatus.value) {
      params.status = searchStatus.value
    }
    if (searchType.value) {
      params.merchantType = searchType.value
    }
    
    const res = await adminGetMerchantList(params)
    merchantList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  currentMerchant.value = row
  detailVisible.value = true
}

const handleDisable = (row) => {
  ElMessageBox.confirm(`确定要禁用商家"${row.merchantName}"吗？禁用后该商家的所有产品将自动下架。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminDisableMerchant(row.id)
      ElMessage.success('禁用成功')
      loadList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleEnable = (row) => {
  ElMessageBox.confirm(`确定要启用商家"${row.merchantName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await adminEnableMerchant(row.id)
      ElMessage.success('启用成功')
      loadList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.merchant-manage {
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
