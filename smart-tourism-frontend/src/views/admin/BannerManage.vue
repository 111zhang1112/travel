<template>
  <div class="banner-manage">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="showDialog()"><el-icon><Plus /></el-icon>添加轮播图</el-button>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="150" />
      <el-table-column prop="subtitle" label="副标题" width="180" show-overflow-tooltip />
      <el-table-column label="图片" width="150">
        <template #default="{ row }">
          <el-image :src="row.imageUrl" style="width: 100px; height: 50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="linkUrl" label="跳转链接" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '添加轮播图'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="请输入副标题（可选）" />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetBannerList, adminCreateBanner, adminUpdateBanner, adminDeleteBanner } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const form = ref({ title: '', subtitle: '', imageUrl: '', linkUrl: '', sort: 0, status: 1 })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片地址', trigger: 'blur' }]
}

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetBannerList({ page: 1, size: 100 })
  list.value = res.records || []
}

const showDialog = (row = null) => {
  form.value = row ? { ...row } : { title: '', subtitle: '', imageUrl: '', linkUrl: '', sort: 0, status: 1 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (form.value.id) {
      await adminUpdateBanner(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateBanner(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该轮播图？', '删除确认', { type: 'warning' })
  await adminDeleteBanner(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style lang="scss" scoped>
.banner-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; margin: 0; } }
</style>
