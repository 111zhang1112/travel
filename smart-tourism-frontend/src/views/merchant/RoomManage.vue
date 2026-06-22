<template>
  <div class="room-manage">
    <h2 class="page-title">房型管理</h2>
    
    <el-card>
      <div class="toolbar">
        <el-select v-model="selectedHotelId" placeholder="请选择酒店" style="width: 300px;" @change="loadList">
          <el-option
            v-for="hotel in hotelList"
            :key="hotel.id"
            :label="hotel.name"
            :value="hotel.id"
          />
        </el-select>
        <el-button type="primary" @click="handleAdd" :disabled="!selectedHotelId" style="margin-left: 12px;">
          <el-icon><Plus /></el-icon>
          添加房型
        </el-button>
      </div>
      
      <el-table :data="roomList" stripe v-loading="loading">
        <el-table-column prop="name" label="房型名称" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}/晚
          </template>
        </el-table-column>
        <el-table-column prop="bedType" label="床型" width="120" />
        <el-table-column prop="area" label="面积" width="100">
          <template #default="{ row }">
            {{ row.area }}㎡
          </template>
        </el-table-column>
        <el-table-column prop="maxGuests" label="最大入住人数" width="120" />
        <el-table-column prop="totalRooms" label="房间数量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：豪华大床房" />
        </el-form-item>
        <el-form-item label="价格(元/晚)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="床型" prop="bedType">
          <el-select v-model="form.bedType" placeholder="请选择床型">
            <el-option value="大床" label="大床" />
            <el-option value="双床" label="双床" />
            <el-option value="三床" label="三床" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="area">
          <el-input-number v-model="form.area" :min="0" />
        </el-form-item>
        <el-form-item label="最大入住人数" prop="maxGuests">
          <el-input-number v-model="form.maxGuests" :min="1" />
        </el-form-item>
        <el-form-item label="房间数量" prop="totalRooms">
          <el-input-number v-model="form.totalRooms" :min="1" />
        </el-form-item>
        <el-form-item label="房型描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入房型描述" />
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantHotelList, getMerchantRoomList, createMerchantRoom, updateMerchantRoom, deleteMerchantRoom } from '@/api/merchant'

const route = useRoute()
const loading = ref(false)
const hotelList = ref([])
const selectedHotelId = ref(null)
const roomList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加房型')
const formRef = ref(null)
const submitting = ref(false)
const form = ref({
  id: null,
  hotelId: null,
  name: '',
  price: 0,
  bedType: '',
  area: 0,
  maxGuests: 2,
  totalRooms: 10,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  bedType: [{ required: true, message: '请选择床型', trigger: 'change' }],
  maxGuests: [{ required: true, message: '请输入最大入住人数', trigger: 'blur' }],
  totalRooms: [{ required: true, message: '请输入房间数量', trigger: 'blur' }]
}

const loadHotelList = async () => {
  try {
    const res = await getMerchantHotelList({ page: 1, pageSize: 100 })
    hotelList.value = res.records || []
    
    // 如果URL中有hotelId参数，自动选中
    if (route.query.hotelId) {
      selectedHotelId.value = parseInt(route.query.hotelId)
      loadList()
    }
  } catch (error) {
    ElMessage.error('加载酒店列表失败')
  }
}

const loadList = async () => {
  if (!selectedHotelId.value) {
    roomList.value = []
    return
  }
  
  loading.value = true
  try {
    const res = await getMerchantRoomList(selectedHotelId.value)
    roomList.value = res || []
  } catch (error) {
    ElMessage.error('加载房型列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加房型'
  form.value.hotelId = selectedHotelId.value
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑房型'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    // 表单验证失败,不继续执行
    return
  }
  
  submitting.value = true
  try {
    if (form.value.id) {
      await updateMerchantRoom(form.value)
      ElMessage.success('更新成功')
    } else {
      await createMerchantRoom(form.value)
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该房型吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMerchantRoom(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const resetForm = () => {
  form.value = {
    id: null,
    hotelId: selectedHotelId.value,
    name: '',
    price: 0,
    bedType: '',
    area: 0,
    maxGuests: 2,
    totalRooms: 10,
    description: ''
  }
  formRef.value?.resetFields()
}

onMounted(() => {
  loadHotelList()
})
</script>

<style lang="scss" scoped>
.room-manage {
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
