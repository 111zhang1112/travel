<template>
  <div class="notice-manage">
    <div class="page-header">
      <h2>公告管理</h2>
      <div class="header-actions">
        <el-select v-model="filterType" placeholder="筛选类型" clearable @change="loadData" style="width: 150px; margin-right: 12px">
          <el-option label="系统公告" value="SYSTEM" />
          <el-option label="活动公告" value="ACTIVITY" />
        </el-select>
        <el-button type="primary" @click="showDialog()"><el-icon><Plus /></el-icon>添加公告</el-button>
      </div>
    </div>
    
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'SYSTEM' ? 'info' : 'warning'">{{ row.type === 'SYSTEM' ? '系统公告' : '活动公告' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handlePublish(row.id)">发布</el-button>
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadData" style="margin-top: 20px" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '添加公告'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" value="SYSTEM" />
            <el-option label="活动公告" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
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
import { adminGetNoticeList, adminCreateNotice, adminUpdateNotice, adminDeleteNotice, adminPublishNotice } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const page = ref(1)
const total = ref(0)
const filterType = ref('')
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const form = ref({ title: '', type: 'SYSTEM', content: '', status: 0 })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

onMounted(() => loadData())

const loadData = async () => {
  const params = { page: page.value, size: 10 }
  if (filterType.value) params.type = filterType.value
  const res = await adminGetNoticeList(params)
  list.value = res.records || []
  total.value = res.total || 0
}

const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 19) : '-'

const showDialog = (row = null) => {
  form.value = row ? { ...row } : { title: '', type: 'SYSTEM', content: '', status: 0 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (form.value.id) {
      await adminUpdateNotice(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateNotice(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

const handlePublish = async (id) => {
  await ElMessageBox.confirm('确认发布该公告？', '发布确认')
  await adminPublishNotice(id)
  ElMessage.success('发布成功')
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该公告？', '删除确认', { type: 'warning' })
  await adminDeleteNotice(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style lang="scss" scoped>
.notice-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; margin: 0; } }
.header-actions { display: flex; align-items: center; }
</style>
