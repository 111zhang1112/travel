<template>
  <div class="scenic-manage">
    <div class="page-header">
      <h2>景点管理</h2>
      <el-button type="primary" @click="openDialog()">新增景点</el-button>
    </div>
    
    <!-- 搜索筛选区域 -->
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索景点名称" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="searchCategoryId" placeholder="选择分类" clearable style="width: 150px; margin-left: 10px" @change="loadData">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="searchStatus" placeholder="选择状态" clearable style="width: 120px; margin-left: 10px" @change="loadData">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="loadData">搜索</el-button>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="categoryName" label="分类" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.categoryName" size="small">{{ row.categoryName }}</el-tag>
          <span v-else style="color: #999">未分类</span>
        </template>
      </el-table-column>
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <div v-if="parseImages(row.images).length > 0" class="image-carousel-cell">
            <el-carousel 
              v-if="parseImages(row.images).length > 1"
              height="50px" 
              :interval="3000" 
              indicator-position="none"
              arrow="never"
              :autoplay="true"
            >
              <el-carousel-item v-for="(img, idx) in parseImages(row.images)" :key="idx">
                <el-image 
                  :src="img" 
                  style="width: 50px; height: 50px" 
                  fit="cover" 
                  :preview-src-list="parseImages(row.images)"
                  :initial-index="idx"
                />
              </el-carousel-item>
            </el-carousel>
            <el-image 
              v-else
              :src="getFirstImage(row.images)" 
              style="width: 50px; height: 50px" 
              fit="cover" 
              :preview-src-list="parseImages(row.images)" 
            />
            <span v-if="parseImages(row.images).length > 1" class="image-count">{{ parseImages(row.images).length }}张</span>
          </div>
          <span v-else style="color: #999">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="ticketPrice" label="门票价格" width="100">
        <template #default="{ row }">¥{{ row.ticketPrice }}</template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" width="80" />
      <el-table-column prop="viewCount" label="浏览量" width="80" />
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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadData" style="margin-top: 20px" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑景点' : '新增景点'" width="700px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="景点名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入景点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="景点图片">
          <el-upload
            v-model:file-list="imageList"
            action="/api/upload/image"
            list-type="picture-card"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
            :before-upload="beforeUpload"
            accept="image/jpeg,image/png,image/gif"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg、png、gif格式，单张不超过5MB</div>
        </el-form-item>
        
        <el-form-item label="景点描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入景点描述" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门票价格" prop="ticketPrice">
              <el-input-number v-model="form.ticketPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开放时间">
              <el-input v-model="form.openingHours" placeholder="如：09:00-18:00" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="7" :step="0.0001" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="7" :step="0.0001" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分">
              <el-rate v-model="form.rating" allow-half show-score />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { adminGetScenicList, adminCreateScenic, adminUpdateScenic, adminDeleteScenic, adminUpdateScenicStatus, adminGetScenicCategories } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const categories = ref([])
const imageList = ref([])

// 上传请求头（携带认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: token } : {}
})

// 搜索条件
const searchKeyword = ref('')
const searchCategoryId = ref(null)
const searchStatus = ref(null)

const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  description: '',
  images: '',
  ticketPrice: 0,
  openingHours: '',
  address: '',
  latitude: 39.9,
  longitude: 116.4,
  tags: '',
  rating: 4
})

const formRules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  ticketPrice: [{ required: true, message: '请输入门票价格', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadCategories()
})

const loadData = async () => {
  const params = { page: page.value, size: 10 }
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (searchCategoryId.value) params.categoryId = searchCategoryId.value
  if (searchStatus.value !== null && searchStatus.value !== '') params.status = searchStatus.value
  
  const res = await adminGetScenicList(params)
  list.value = res.records || []
  total.value = res.total || 0
}

const loadCategories = async () => {
  try {
    const res = await adminGetScenicCategories()
    categories.value = res || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const openDialog = (row = null) => {
  if (row) {
    Object.assign(form, row)
    // 解析图片列表
    imageList.value = parseImages(row.images).map((url, index) => ({
      name: `image-${index}`,
      url: url
    }))
  } else {
    Object.assign(form, {
      id: null, name: '', categoryId: null, description: '', images: '',
      ticketPrice: 0, openingHours: '', address: '', latitude: 39.9, longitude: 116.4, tags: '', rating: 4
    })
    imageList.value = []
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  // 处理图片 - 优先使用 url，其次使用 response.data.url
  const imageUrls = imageList.value.map(f => {
    if (f.url) return f.url
    if (f.response?.data?.url) return f.response.data.url
    if (f.response?.url) return f.response.url
    return null
  }).filter(Boolean)
  form.images = JSON.stringify(imageUrls)
  
  if (form.id) {
    await adminUpdateScenic(form.id, form)
    ElMessage.success('更新成功')
  } else {
    await adminCreateScenic(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该景点？', '删除确认')
  await adminDeleteScenic(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleStatusChange = async (row) => {
  try {
    // 传递完整的景点信息，只修改status字段
    const scenicData = {
      name: row.name,
      categoryId: row.categoryId,
      description: row.description,
      images: row.images,
      ticketPrice: row.ticketPrice,
      openingHours: row.openingHours,
      address: row.address,
      latitude: row.latitude || 39.9,
      longitude: row.longitude || 116.4,
      tags: row.tags,
      rating: row.rating,
      status: row.status
    }
    await adminUpdateScenic(row.id, scenicData)
    ElMessage.success('状态更新成功')
  } catch (e) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 图片处理
const normalizeImageUrl = (url) => {
  if (!url) return null
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 兼容旧路径格式 /images/scenic/xxx.jpg -> /uploads/images/xxx.jpg
  if (url.startsWith('/images/') && !url.startsWith('/images/scenic/')) {
    return url
  }
  if (url.startsWith('/images/scenic/')) {
    // 旧格式，尝试转换（如果文件已迁移）或保持原样
    return url
  }
  // 新格式 /uploads/images/xxx.jpg
  return url
}

const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return arr.map(url => normalizeImageUrl(url)).filter(Boolean)
  } catch {
    return images ? [normalizeImageUrl(images)].filter(Boolean) : []
  }
}

const getFirstImage = (images) => {
  const arr = parseImages(images)
  return arr.length > 0 ? arr[0] : null
}

const handleUploadSuccess = (response, file) => {
  // 后端返回格式: { code: 200, data: { url: "...", name: "..." }, message: "success" }
  console.log('上传成功响应:', response)
  if (response && response.code === 200 && response.data) {
    file.url = response.data.url
  } else if (response && response.url) {
    file.url = response.url
  } else {
    console.error('上传响应格式异常:', response)
    ElMessage.error('上传响应格式异常')
  }
}

const handleUploadError = (error, file) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

const handleUploadRemove = (file) => {
  const index = imageList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) imageList.value.splice(index, 1)
}

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/GIF 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}
</script>

<style lang="scss" scoped>
.scenic-manage {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { font-size: 18px; margin: 0; }
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

:deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
}

.image-carousel-cell {
  position: relative;
  width: 50px;
  height: 50px;
  
  :deep(.el-carousel) {
    width: 50px;
    height: 50px;
    border-radius: 4px;
    overflow: hidden;
  }
  
  :deep(.el-carousel__container) {
    height: 50px !important;
  }
  
  :deep(.el-carousel__item) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .image-count {
    position: absolute;
    bottom: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 10px;
    padding: 1px 4px;
    border-radius: 2px 0 4px 0;
    line-height: 1.2;
  }
}
</style>
