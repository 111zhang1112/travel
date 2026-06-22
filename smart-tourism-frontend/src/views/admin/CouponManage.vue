<template>
  <div class="coupon-manage">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="handleCreate">新建优惠券</el-button>
    </div>
    
    <!-- 搜索 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="优惠券名称" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'AMOUNT' ? 'danger' : 'primary'" size="small">
            {{ row.type === 'AMOUNT' ? '满减券' : '折扣券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优惠" width="120">
        <template #default="{ row }">
          <span v-if="row.type === 'AMOUNT'" class="coupon-value">¥{{ row.value }}</span>
          <span v-else class="coupon-value">{{ (row.value * 10).toFixed(0) }}折</span>
        </template>
      </el-table-column>
      <el-table-column label="使用门槛" width="120">
        <template #default="{ row }">
          {{ row.minAmount > 0 ? `满${row.minAmount}元` : '无门槛' }}
        </template>
      </el-table-column>
      <el-table-column label="库存" width="120">
        <template #default="{ row }">
          {{ row.remainCount }} / {{ row.totalCount }}
        </template>
      </el-table-column>
      <el-table-column label="有效期" width="200">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button 
            :type="row.status === 1 ? 'warning' : 'success'" 
            size="small" 
            text 
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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
    
    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新建优惠券'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="AMOUNT">满减券</el-radio>
            <el-radio label="DISCOUNT">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值" prop="value">
          <el-input-number 
            v-model="form.value" 
            :min="form.type === 'DISCOUNT' ? 0.1 : 1" 
            :max="form.type === 'DISCOUNT' ? 0.99 : 9999"
            :step="form.type === 'DISCOUNT' ? 0.05 : 5"
            :precision="form.type === 'DISCOUNT' ? 2 : 0"
          />
          <span style="margin-left: 8px">{{ form.type === 'DISCOUNT' ? '(0.9表示9折)' : '元' }}</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :step="10" />
          <span style="margin-left: 8px">元 (0表示无门槛)</span>
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" :step="100" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="使用说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入使用说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminGetCouponList, adminCreateCoupon, adminUpdateCoupon, adminDeleteCoupon, adminUpdateCouponStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  type: 'AMOUNT',
  value: 10,
  minAmount: 0,
  totalCount: 100,
  dateRange: [],
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放数量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminGetCouponList({
      page: pagination.page,
      size: pagination.size,
      keyword: keyword.value
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    type: 'AMOUNT',
    value: 10,
    minAmount: 0,
    totalCount: 100,
    dateRange: [],
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    value: row.value,
    minAmount: row.minAmount,
    totalCount: row.totalCount,
    dateRange: [row.startTime, row.endTime],
    description: row.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  const data = {
    name: form.name,
    type: form.type,
    value: form.value,
    minAmount: form.minAmount,
    totalCount: form.totalCount,
    remainCount: isEdit.value ? undefined : form.totalCount,
    startTime: form.dateRange[0],
    endTime: form.dateRange[1],
    description: form.description
  }
  
  if (isEdit.value) {
    await adminUpdateCoupon(form.id, data)
    ElMessage.success('更新成功')
  } else {
    data.remainCount = form.totalCount
    await adminCreateCoupon(data)
    ElMessage.success('创建成功')
  }
  
  dialogVisible.value = false
  loadData()
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}该优惠券？`, '确认')
  await adminUpdateCouponStatus(row.id, newStatus)
  ElMessage.success(`已${action}`)
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该优惠券？', '删除确认')
  await adminDeleteCoupon(id)
  ElMessage.success('删除成功')
  loadData()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.coupon-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { font-size: 20px; }
}

.filter-card {
  margin-bottom: 20px;
}

.coupon-value {
  color: #f56c6c;
  font-weight: 600;
}
</style>
