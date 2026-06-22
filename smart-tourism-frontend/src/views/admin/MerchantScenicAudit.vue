<template>
  <div class="merchant-audit">
    <h2 class="page-title">景点商家审核</h2>
    
    <el-card>
      <el-table :data="merchantList" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="merchantName" label="商家名称" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="businessLicense" label="营业执照号" width="180" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button link type="danger" @click="handleReject(row)">拒绝</el-button>
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
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
    
    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝审核" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="handleRejectSubmit" :loading="submitting">确定拒绝</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="商家详情" width="600px">
      <el-descriptions :column="1" border v-if="currentMerchant">
        <el-descriptions-item label="用户名">{{ currentMerchant.username }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentMerchant.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商家类型">
          <el-tag type="success">景点商家</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentMerchant.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号">{{ currentMerchant.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="商家地址">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="商家简介">{{ currentMerchant.description }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentMerchant.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetPendingScenicMerchants, adminApproveMerchant, adminRejectMerchant } from '@/api/admin'

const loading = ref(false)
const merchantList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const rejectVisible = ref(false)
const detailVisible = ref(false)
const currentMerchant = ref(null)
const rejectFormRef = ref(null)
const submitting = ref(false)

const rejectForm = ref({
  merchantId: null,
  reason: ''
})

const rejectRules = {
  reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminGetPendingScenicMerchants({ page: page.value, pageSize: pageSize.value })
    merchantList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (row) => {
  ElMessageBox.confirm(`确定通过商家"${row.merchantName}"的审核吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await adminApproveMerchant(row.id)
      ElMessage.success('审核通过')
      loadList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  currentMerchant.value = row
  rejectForm.value.merchantId = row.id
  rejectForm.value.reason = ''
  rejectVisible.value = true
}

const handleRejectSubmit = async () => {
  await rejectFormRef.value.validate()
  submitting.value = true
  try {
    await adminRejectMerchant(rejectForm.value.merchantId, rejectForm.value.reason)
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    loadList()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleViewDetail = (row) => {
  currentMerchant.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.merchant-audit {
  .page-title {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
}
</style>
