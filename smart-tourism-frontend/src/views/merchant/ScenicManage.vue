<template>
  <div class="scenic-manage">
    <h2 class="page-title">景点管理</h2>
    
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加景点
        </el-button>
      </div>
      
      <el-table :data="scenicList" stripe v-loading="loading">
        <el-table-column prop="name" label="景点名称" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="ticketPrice" label="门票价格" width="120">
          <template #default="{ row }">
            ¥{{ row.ticketPrice }}
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
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
        <el-form-item label="景点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="门票价格" prop="ticketPrice">
          <el-input-number v-model="form.ticketPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="景点描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入景点描述" />
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-input v-model="form.openTime" placeholder="例如：08:00-18:00" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="经纬度" required>
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-form-item prop="latitude" style="flex: 1; margin-bottom: 0;">
              <el-input v-model="form.latitude" placeholder="纬度" />
            </el-form-item>
            <el-form-item prop="longitude" style="flex: 1; margin-bottom: 0;">
              <el-input v-model="form.longitude" placeholder="经度" />
            </el-form-item>
            <el-button @click="handleSelectLocation">地图选点</el-button>
          </div>
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            可以手动输入或点击"地图选点"在地图上选择位置
          </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantScenicList, createMerchantScenic, updateMerchantScenic, deleteMerchantScenic, updateMerchantScenicStatus } from '@/api/merchant'
import AMapLoader from '@amap/amap-jsapi-loader'

let AMap = null

const loading = ref(false)
const scenicList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('添加景点')
const formRef = ref(null)
const submitting = ref(false)
const form = ref({
  id: null,
  name: '',
  address: '',
  ticketPrice: 0,
  description: '',
  openTime: '',
  phone: '',
  latitude: null,
  longitude: null
})

const rules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  ticketPrice: [{ required: true, message: '请输入门票价格', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度或使用地图选点', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度或使用地图选点', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMerchantScenicList({ page: page.value, pageSize: pageSize.value })
    scenicList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载景点列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加景点'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑景点'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await updateMerchantScenic(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createMerchantScenic(form.value)
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
    await updateMerchantScenicStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该景点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMerchantScenic(row.id)
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
    name: '',
    address: '',
    ticketPrice: 0,
    description: '',
    openTime: '',
    phone: '',
    latitude: null,
    longitude: null
  }
  formRef.value?.resetFields()
}

// 地图选点功能
const handleSelectLocation = () => {
  if (!AMap) {
    ElMessage.warning('地图加载中，请稍后再试')
    return
  }

  // 创建遮罩层
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 9998;'
  
  // 创建地图选点对话框
  const mapDialog = document.createElement('div')
  mapDialog.style.cssText = 'position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 80%; max-width: 800px; height: 600px; background: white; z-index: 9999; box-shadow: 0 2px 12px rgba(0,0,0,0.3); border-radius: 4px; display: flex; flex-direction: column;'
  
  // 添加标题
  const titleBar = document.createElement('div')
  titleBar.style.cssText = 'padding: 15px 20px; border-bottom: 1px solid #eee; font-size: 16px; font-weight: 600;'
  titleBar.innerHTML = '选择景点位置 <span style="font-size: 12px; color: #999; font-weight: 400; margin-left: 10px;">点击地图任意位置选择坐标</span>'
  
  // 地图容器
  const mapContainer = document.createElement('div')
  mapContainer.id = 'map-picker-' + Date.now()
  mapContainer.style.cssText = 'flex: 1; width: 100%; position: relative;'
  
  // 按钮容器
  const buttonContainer = document.createElement('div')
  buttonContainer.style.cssText = 'height: 60px; display: flex; justify-content: center; align-items: center; gap: 10px; border-top: 1px solid #eee;'
  
  const confirmBtn = document.createElement('button')
  confirmBtn.textContent = '确定'
  confirmBtn.style.cssText = 'padding: 8px 20px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px;'
  
  const cancelBtn = document.createElement('button')
  cancelBtn.textContent = '取消'
  cancelBtn.style.cssText = 'padding: 8px 20px; background: #f5f5f5; color: #333; border: none; border-radius: 4px; cursor: pointer; font-size: 14px;'
  
  buttonContainer.appendChild(cancelBtn)
  buttonContainer.appendChild(confirmBtn)
  
  mapDialog.appendChild(titleBar)
  mapDialog.appendChild(mapContainer)
  mapDialog.appendChild(buttonContainer)
  
  document.body.appendChild(overlay)
  document.body.appendChild(mapDialog)

  // 延迟初始化地图，确保容器已渲染
  setTimeout(() => {
    // 初始化地图
    const map = new AMap.Map(mapContainer.id, {
      zoom: 13,
      center: form.value.longitude && form.value.latitude 
        ? [form.value.longitude, form.value.latitude]
        : [120.153576, 30.287459], // 默认杭州
      viewMode: '2D',
      resizeEnable: true,
      layers: [
        // 使用标准图层（包含道路、地名等）
        new AMap.TileLayer(),
        // 使用路网图层（显示道路网络）
        new AMap.TileLayer.RoadNet()
      ]
    })
    
    // 添加比例尺控件
    map.addControl(new AMap.Scale())
    
    // 添加工具条控件（缩放、平移等）
    map.addControl(new AMap.ToolBar({
      position: 'RB'  // 右下角
    }))

    let marker = null
    let selectedLng = form.value.longitude
    let selectedLat = form.value.latitude

    // 如果已有坐标，显示标记
    if (selectedLng && selectedLat) {
      marker = new AMap.Marker({
        position: [selectedLng, selectedLat],
        map: map
      })
    }

    // 点击地图选点
    map.on('click', (e) => {
      selectedLng = e.lnglat.getLng()
      selectedLat = e.lnglat.getLat()
      
      if (marker) {
        marker.setPosition([selectedLng, selectedLat])
      } else {
        marker = new AMap.Marker({
          position: [selectedLng, selectedLat],
          map: map
        })
      }
    })

    // 确定按钮
    confirmBtn.onclick = () => {
      if (selectedLng && selectedLat) {
        form.value.longitude = selectedLng
        form.value.latitude = selectedLat
        ElMessage.success('位置选择成功')
        document.body.removeChild(overlay)
        document.body.removeChild(mapDialog)
        map.destroy()
      } else {
        ElMessage.warning('请在地图上点击选择位置')
      }
    }

    // 取消按钮
    cancelBtn.onclick = () => {
      document.body.removeChild(overlay)
      document.body.removeChild(mapDialog)
      map.destroy()
    }
    
    // 点击遮罩层关闭
    overlay.onclick = () => {
      document.body.removeChild(overlay)
      document.body.removeChild(mapDialog)
      map.destroy()
    }
  }, 100)
}

// 初始化
onMounted(async () => {
  try {
    AMap = await AMapLoader.load({
      key: 'ce9e34be95d07974a6cd953d1934ce7e',
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.Scale', 'AMap.ToolBar']
    })
  } catch (e) {
    console.error('Failed to load AMap', e)
  }
  loadList()
})
</script>

<style lang="scss" scoped>
.scenic-manage {
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
