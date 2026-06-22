<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog"><el-icon><Plus /></el-icon>添加用户</el-button>
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width: 250px" clearable @clear="loadData" @keyup.enter="loadData">
          <template #append>
            <el-button @click="loadData"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
    </div>
    
    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template #default="{ row }">{{ row.nickname || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="积分" width="80" align="center">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: 500;">{{ row.points || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="70" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <template v-if="row.role !== 'ADMIN'">
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="showPointsDialog(row)">积分</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
          <span v-else class="admin-tip">管理员</span>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination 
      v-model:current-page="page" 
      :page-size="10" 
      :total="total" 
      layout="prev, pager, next, total" 
      @current-change="loadData" 
      style="margin-top: 20px" 
    />

    <!-- 积分调整对话框 -->
    <el-dialog v-model="pointsDialogVisible" title="调整积分" width="400px">
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ pointsForm.username }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <span style="color: #e6a23c; font-weight: 600;">{{ pointsForm.currentPoints }}</span>
        </el-form-item>
        <el-form-item label="调整类型">
          <el-radio-group v-model="pointsForm.type">
            <el-radio label="add">增加</el-radio>
            <el-radio label="subtract">扣减</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量">
          <el-input-number v-model="pointsForm.points" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="pointsForm.description" type="textarea" :rows="2" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustPoints" :loading="pointsLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加用户对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加用户" width="450px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称（可选）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号（可选）" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetUserList, adminUpdateUserStatus, adminDeleteUser, adminCreateUser, adminAdjustUserPoints } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const list = ref([])
const page = ref(1)
const total = ref(0)
const keyword = ref('')

// 积分调整相关
const pointsDialogVisible = ref(false)
const pointsLoading = ref(false)
const pointsForm = ref({
  userId: null,
  username: '',
  currentPoints: 0,
  type: 'add',
  points: 100,
  description: ''
})

// 添加用户相关
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref(null)
const addForm = ref({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  role: 'USER'
})
const addRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(() => loadData())

const loadData = async () => {
  const res = await adminGetUserList({ page: page.value, size: 10, keyword: keyword.value })
  list.value = res.records || []
  total.value = res.total || 0
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}用户 "${row.username}" 吗？`, '操作确认')
  await adminUpdateUserStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该用户？此操作不可恢复', '删除确认', { type: 'warning' })
  await adminDeleteUser(id)
  ElMessage.success('删除成功')
  loadData()
}

const showAddDialog = () => {
  addForm.value = { username: '', password: '', nickname: '', phone: '', role: 'USER' }
  addDialogVisible.value = true
}

const handleAddUser = async () => {
  await addFormRef.value.validate()
  addLoading.value = true
  try {
    await adminCreateUser(addForm.value)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    loadData()
  } finally {
    addLoading.value = false
  }
}

const showPointsDialog = (row) => {
  pointsForm.value = {
    userId: row.id,
    username: row.username,
    currentPoints: row.points || 0,
    type: 'add',
    points: 100,
    description: ''
  }
  pointsDialogVisible.value = true
}

const handleAdjustPoints = async () => {
  if (!pointsForm.value.description) {
    ElMessage.warning('请输入调整原因')
    return
  }
  pointsLoading.value = true
  try {
    const adjustPoints = pointsForm.value.type === 'add' ? pointsForm.value.points : -pointsForm.value.points
    await adminAdjustUserPoints(pointsForm.value.userId, {
      points: adjustPoints,
      description: pointsForm.value.description
    })
    ElMessage.success('积分调整成功')
    pointsDialogVisible.value = false
    loadData()
  } finally {
    pointsLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-manage { background: #fff; padding: 20px; border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { font-size: 18px; margin: 0; } }
.header-actions { display: flex; gap: 12px; align-items: center; }
.admin-tip { color: #999; font-size: 12px; }
:deep(.el-table) {
  .el-button + .el-button { margin-left: 4px; }
}
</style>
