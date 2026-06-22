<template>
  <div class="hotel-manage">
    <h2 class="page-title">酒店管理</h2>
    
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加酒店
        </el-button>
      </div>
      
      <el-table :data="hotelList" stripe v-loading="loading">
        <el-table-column prop="name" label="酒店名称" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="star" label="星级" width="100">
          <template #default="{ row }">
            {{ row.star }}星级
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleManageRooms(row)">房型管理</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="酒店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="星级" prop="star">
          <el-select v-model="form.star" placeholder="请选择星级">
            <el-option :value="3" label="三星级" />
            <el-option :value="4" label="四星级" />
            <el-option :value="5" label="五星级" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入酒店描述" />
        </el-form-item>
        <el-form-item label="设施服务" prop="facilities">
          <el-input v-model="form.facilities" placeholder="例如：WiFi、停车场、游泳池" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantHotelList, createMerchantHotel, updateMerchantHotel, deleteMerchantHotel, updateMerchantHotelStatus } from '@/api/merchant'

const router = useRouter()
const loading = ref(false)
const hotelList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('添加酒店')
const formRef = ref(null)
const submitting = ref(false)
const form = ref({
  id: null,
  name: '',
  address: '',
  star: 4,
  description: '',
  facilities: '',
  phone: ''
})

const rules = {
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  star: [{ required: true, message: '请选择星级', trigger: 'change' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMerchantHotelList({ page: page.value, size: pageSize.value })
    console.log('酒店列表响应:', res)
    hotelList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载酒店列表失败:', error)
    ElMessage.error('加载酒店列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加酒店'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑酒店'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await updateMerchantHotel(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createMerchantHotel(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateMerchantHotelStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该酒店吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMerchantHotel(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleManageRooms = (row) => {
  router.push({ path: '/merchant/rooms', query: { hotelId: row.id, hotelName: row.name } })
}

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    address: '',
    star: 4,
    description: '',
    facilities: '',
    phone: ''
  }
  formRef.value?.resetFields()
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.hotel-manage {
  .page-title {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
  
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
