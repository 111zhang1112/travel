<template>
  <div class="merchant-profile">
    <h2 class="page-title">商家信息</h2>
    
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="商家名称" prop="merchantName">
              <el-input v-model="form.merchantName" placeholder="请输入商家名称" />
            </el-form-item>
            <el-form-item label="商家类型">
              <el-tag :type="form.merchantType === 'SCENIC' ? 'success' : 'primary'">
                {{ form.merchantType === 'SCENIC' ? '景点商家' : '酒店商家' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="营业执照号" prop="businessLicense">
              <el-input v-model="form.businessLicense" placeholder="请输入营业执照号" />
            </el-form-item>
            <el-form-item label="商家地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入商家地址" />
            </el-form-item>
            <el-form-item label="商家简介" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商家简介" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="submitting">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>账号状态</span>
          </template>
          <div class="status-info">
            <div class="status-item">
              <span class="label">审核状态：</span>
              <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
            </div>
            <div class="status-item" v-if="form.status === 'REJECTED' && form.rejectReason">
              <span class="label">拒绝原因：</span>
              <div class="reject-reason">{{ form.rejectReason }}</div>
            </div>
            <div class="status-item">
              <span class="label">注册时间：</span>
              <span>{{ form.createTime }}</span>
            </div>
          </div>
        </el-card>
        
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordSubmitting">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantInfo, updateMerchantProfile, changeMerchantPassword } from '@/api/merchant'

const formRef = ref(null)
const passwordFormRef = ref(null)
const submitting = ref(false)
const passwordSubmitting = ref(false)

const form = ref({
  merchantName: '',
  merchantType: '',
  contactPerson: '',
  contactPhone: '',
  businessLicense: '',
  address: '',
  description: '',
  status: '',
  rejectReason: '',
  createTime: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const statusType = computed(() => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'DISABLED': 'info'
  }
  return typeMap[form.value.status] || 'info'
})

const statusText = computed(() => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'DISABLED': '已禁用'
  }
  return textMap[form.value.status] || '未知'
})

const loadInfo = async () => {
  try {
    const res = await getMerchantInfo()
    form.value = res
  } catch (error) {
    ElMessage.error('获取商家信息失败')
  }
}

const handleUpdate = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await updateMerchantProfile(form.value)
    ElMessage.success('更新成功')
    loadInfo()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const handleChangePassword = async () => {
  await passwordFormRef.value.validate()
  passwordSubmitting.value = true
  try {
    await changeMerchantPassword(passwordForm.value)
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    passwordFormRef.value.resetFields()
    
    // 3秒后跳转到登录页
    setTimeout(() => {
      localStorage.removeItem('merchantToken')
      localStorage.removeItem('merchantInfo')
      window.location.href = '/merchant/login'
    }, 3000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(() => {
  loadInfo()
})
</script>

<style lang="scss" scoped>
.merchant-profile {
  .page-title {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
  
  .status-info {
    .status-item {
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        color: #666;
        font-size: 14px;
        display: block;
        margin-bottom: 8px;
      }
      
      .reject-reason {
        color: #f56c6c;
        font-size: 14px;
        line-height: 1.6;
        padding: 12px;
        background: #fef0f0;
        border-radius: 4px;
      }
    }
  }
}
</style>
