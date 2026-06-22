<template>
  <div class="hotel-manage">
    <div class="page-header">
      <h2>酒店管理</h2>
      <el-button type="primary" @click="openHotelDialog()">新增酒店</el-button>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="starRating" label="星级" width="120">
        <template #default="{ row }">
          <el-icon v-for="i in row.starRating" :key="i" color="#f7ba2a"><Star /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="价格区间" width="150">
        <template #default="{ row }">¥{{ row.priceMin }} - ¥{{ row.priceMax }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            inline-prompt
            active-text="上"
            inactive-text="下"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openRoomManage(row)">房间管理</el-button>
          <el-button size="small" @click="openHotelDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteHotel(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadData" style="margin-top: 20px" />
    
    <!-- 酒店编辑弹窗 -->
    <el-dialog v-model="hotelDialogVisible" :title="hotelForm.id ? '编辑酒店' : '新增酒店'" width="600px">
      <el-form :model="hotelForm" label-width="100px">
        <el-form-item label="名称" required><el-input v-model="hotelForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="hotelForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="星级"><el-rate v-model="hotelForm.starRating" :max="5" /></el-form-item>
        <el-form-item label="纬度"><el-input-number v-model="hotelForm.latitude" :precision="7" /></el-form-item>
        <el-form-item label="经度"><el-input-number v-model="hotelForm.longitude" :precision="7" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="hotelForm.address" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="hotelForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hotelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitHotel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 房间管理弹窗 -->
    <el-dialog v-model="roomDialogVisible" :title="`${currentHotel?.name} - 房间管理`" width="900px">
      <div class="room-header">
        <el-button type="primary" size="small" @click="openRoomForm()">新增房型</el-button>
      </div>
      <el-table :data="roomList" stripe size="small">
        <el-table-column prop="name" label="房型名称" width="120" />
        <el-table-column prop="bedType" label="床型" width="80" />
        <el-table-column prop="area" label="面积" width="70">
          <template #default="{ row }">{{ row.area }}㎡</template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="maxGuests" label="入住人数" width="80" />
        <el-table-column label="设施" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.breakfast" size="small" type="success" style="margin-right:4px">含早</el-tag>
            <el-tag v-if="row.wifi" size="small" type="info" style="margin-right:4px">WiFi</el-tag>
            <el-tag v-if="row.window" size="small" type="warning">有窗</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="房间数" width="100">
          <template #default="{ row }">{{ row.availableRooms }}/{{ row.totalRooms }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="openRoomForm(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteRoom(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 房间编辑弹窗 -->
    <el-dialog v-model="roomFormVisible" :title="roomForm.id ? '编辑房型' : '新增房型'" width="550px">
      <el-form :model="roomForm" label-width="100px">
        <el-form-item label="房型名称" required><el-input v-model="roomForm.name" placeholder="如：豪华大床房" /></el-form-item>
        <el-form-item label="房型描述"><el-input v-model="roomForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" required><el-input-number v-model="roomForm.price" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="床型">
              <el-select v-model="roomForm.bedType" style="width:100%">
                <el-option label="大床" value="大床" />
                <el-option label="双床" value="双床" />
                <el-option label="单人床" value="单人床" />
                <el-option label="榻榻米" value="榻榻米" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面积(㎡)"><el-input-number v-model="roomForm.area" :min="10" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大入住"><el-input-number v-model="roomForm.maxGuests" :min="1" :max="10" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总房间数"><el-input-number v-model="roomForm.totalRooms" :min="1" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用房间"><el-input-number v-model="roomForm.availableRooms" :min="0" :max="roomForm.totalRooms" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设施">
          <el-checkbox v-model="roomForm.breakfast" :true-value="1" :false-value="0">含早餐</el-checkbox>
          <el-checkbox v-model="roomForm.wifi" :true-value="1" :false-value="0">WiFi</el-checkbox>
          <el-checkbox v-model="roomForm.window" :true-value="1" :false-value="0">有窗</el-checkbox>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roomForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRoom">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  adminGetHotelList, adminCreateHotel, adminUpdateHotel, adminDeleteHotel,
  adminGetHotelRooms, adminCreateHotelRoom, adminUpdateHotelRoom, adminDeleteHotelRoom
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'

// 酒店列表
const list = ref([])
const page = ref(1)
const total = ref(0)
const hotelDialogVisible = ref(false)
const hotelForm = reactive({ id: null, name: '', description: '', starRating: 4, latitude: 39.9, longitude: 116.4, address: '', status: 1 })

// 房间管理
const roomDialogVisible = ref(false)
const roomFormVisible = ref(false)
const currentHotel = ref(null)
const roomList = ref([])
const roomForm = reactive({
  id: null, name: '', description: '', price: 300, bedType: '大床', area: 30,
  maxGuests: 2, breakfast: 0, wifi: 1, window: 1, totalRooms: 10, availableRooms: 10, status: 1
})

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetHotelList({ page: page.value, size: 10 })
  list.value = res.records || []
  total.value = res.total || 0
}

// 酒店操作
const openHotelDialog = (row = null) => {
  if (row) Object.assign(hotelForm, row)
  else Object.assign(hotelForm, { id: null, name: '', description: '', starRating: 4, latitude: 39.9, longitude: 116.4, address: '', status: 1 })
  hotelDialogVisible.value = true
}

const handleSubmitHotel = async () => {
  if (hotelForm.id) await adminUpdateHotel(hotelForm.id, hotelForm)
  else await adminCreateHotel(hotelForm)
  ElMessage.success(hotelForm.id ? '更新成功' : '创建成功')
  hotelDialogVisible.value = false
  loadData()
}

const handleStatusChange = async (row) => {
  try {
    // 传递完整的酒店信息，只修改status字段
    // 如果经纬度为空，使用默认值避免验证失败
    const hotelData = {
      name: row.name,
      description: row.description,
      starRating: row.starRating,
      latitude: row.latitude || 30.0,  // 默认纬度
      longitude: row.longitude || 120.0,  // 默认经度
      address: row.address,
      status: row.status
    }
    await adminUpdateHotel(row.id, hotelData)
    ElMessage.success('状态更新成功')
  } catch (e) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDeleteHotel = async (id) => {
  await ElMessageBox.confirm('确认删除该酒店？删除后其下所有房间也将被删除', '删除确认')
  await adminDeleteHotel(id)
  ElMessage.success('删除成功')
  loadData()
}

// 房间操作
const openRoomManage = async (hotel) => {
  currentHotel.value = hotel
  roomDialogVisible.value = true
  await loadRooms()
}

const loadRooms = async () => {
  if (!currentHotel.value) return
  const res = await adminGetHotelRooms(currentHotel.value.id)
  roomList.value = res || []
}

const openRoomForm = (row = null) => {
  if (row) {
    Object.assign(roomForm, row)
  } else {
    Object.assign(roomForm, {
      id: null, name: '', description: '', price: 300, bedType: '大床', area: 30,
      maxGuests: 2, breakfast: 0, wifi: 1, window: 1, totalRooms: 10, availableRooms: 10, status: 1
    })
  }
  roomFormVisible.value = true
}

const handleSubmitRoom = async () => {
  if (roomForm.id) {
    await adminUpdateHotelRoom(roomForm.id, roomForm)
  } else {
    await adminCreateHotelRoom(currentHotel.value.id, roomForm)
  }
  ElMessage.success(roomForm.id ? '更新成功' : '创建成功')
  roomFormVisible.value = false
  await loadRooms()
  loadData() // 刷新酒店列表以更新价格区间
}

const handleDeleteRoom = async (id) => {
  await ElMessageBox.confirm('确认删除该房型？', '删除确认')
  await adminDeleteHotelRoom(id)
  ElMessage.success('删除成功')
  await loadRooms()
  loadData()
}
</script>

<style lang="scss" scoped>
.hotel-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; } }
.room-header { margin-bottom: 16px; }
</style>
