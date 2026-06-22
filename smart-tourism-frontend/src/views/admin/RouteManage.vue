<template>
  <div class="route-manage">
    <div class="page-header">
      <h2>路线管理</h2>
      <el-button type="primary" @click="showDialog()"><el-icon><Plus /></el-icon>添加路线</el-button>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="路线名称" width="180" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="days" label="天数" width="80">
        <template #default="{ row }">{{ row.days }}天</template>
      </el-table-column>
      <el-table-column prop="price" label="参考价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadData" style="margin-top: 20px" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑路线' : '添加路线'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="路线名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入路线名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入路线描述" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入封面图URL（可选）" />
        </el-form-item>
        <el-form-item label="行程天数" prop="days">
          <el-input-number v-model="form.days" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="参考价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
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
import { adminGetRouteList, adminCreateRoute, adminUpdateRoute, adminDeleteRoute, adminUpdateRouteStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const form = ref({ name: '', description: '', coverImage: '', days: 1, price: 0, sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入路线名称', trigger: 'blur' }],
  days: [{ required: true, message: '请输入行程天数', trigger: 'blur' }]
}

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetRouteList({ page: page.value, size: 10 })
  list.value = res.records || []
  total.value = res.total || 0
}

const showDialog = (row = null) => {
  form.value = row ? { ...row } : { name: '', description: '', coverImage: '', days: 1, price: 0, sort: 0, status: 1 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (form.value.id) {
      await adminUpdateRoute(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateRoute(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  await ElMessageBox.confirm(`确认${action}该路线？`, '操作确认')
  await adminUpdateRouteStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该路线？', '删除确认', { type: 'warning' })
  await adminDeleteRoute(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style lang="scss" scoped>
.route-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; margin: 0; } }
</style>
