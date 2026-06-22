<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <div class="header-actions">
        <el-select v-model="filterType" placeholder="筛选类型" clearable @change="loadData" style="width: 150px; margin-right: 12px">
          <el-option label="景点分类" value="SCENIC" />
          <el-option label="酒店分类" value="HOTEL" />
          <el-option label="攻略分类" value="GUIDE" />
        </el-select>
        <el-button type="primary" @click="showDialog()"><el-icon><Plus /></el-icon>添加分类</el-button>
      </div>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" width="150" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeMap[row.type]?.color">{{ typeMap[row.type]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '添加分类'" width="450px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="景点分类" value="SCENIC" />
            <el-option label="酒店分类" value="HOTEL" />
            <el-option label="攻略分类" value="GUIDE" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称（可选）" />
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
import { adminGetCategoryList, adminCreateCategory, adminUpdateCategory, adminDeleteCategory } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const typeMap = {
  SCENIC: { label: '景点', color: 'success' },
  HOTEL: { label: '酒店', color: 'primary' },
  GUIDE: { label: '攻略', color: 'warning' }
}

const list = ref([])
const filterType = ref('')
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const form = ref({ name: '', type: 'SCENIC', icon: '', sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

onMounted(() => loadData())

const loadData = async () => {
  const params = filterType.value ? { type: filterType.value } : {}
  list.value = await adminGetCategoryList(params) || []
}

const showDialog = (row = null) => {
  form.value = row ? { ...row } : { name: '', type: 'SCENIC', icon: '', sort: 0, status: 1 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (form.value.id) {
      await adminUpdateCategory(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateCategory(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该分类？', '删除确认', { type: 'warning' })
  await adminDeleteCategory(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style lang="scss" scoped>
.category-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; margin: 0; } }
.header-actions { display: flex; align-items: center; }
</style>
