<template>
  <div class="merchant-register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>商家注册</h2>
          <p>加入我们,开启您的智慧旅游之旅</p>
        </div>
      </template>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <el-form-item label="商家名称" prop="merchantName">
          <el-input v-model="registerForm.merchantName" placeholder="请输入商家名称" />
        </el-form-item>

        <el-form-item label="商家类型" prop="merchantType">
          <el-radio-group v-model="registerForm.merchantType">
            <el-radio label="SCENIC">景点商家</el-radio>
            <el-radio label="HOTEL">酒店商家</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="registerForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="registerForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱(选填)" />
        </el-form-item>

        <el-form-item label="经营地址" prop="address">
          <el-input v-model="registerForm.address" placeholder="请输入经营地址(选填)" />
        </el-form-item>

        <el-form-item label="商家描述" prop="description">
          <el-input v-model="registerForm.description" type="textarea" :rows="3" placeholder="请输入商家描述(选填)" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <router-link to="/merchant/login">已有账号?立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { merchantRegister } from '@/api/merchant'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  merchantName: '',
  merchantType: 'SCENIC',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  description: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  merchantName: [
    { required: true, message: '请输入商家名称', trigger: 'blur' }
  ],
  merchantType: [
    { required: true, message: '请选择商家类型', trigger: 'change' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const { confirmPassword, ...data } = registerForm
    await merchantRegister(data)
    
    ElMessage.success('注册成功,请等待管理员审核')
    router.push('/merchant/login')
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.merchant-register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.register-card {
  width: 600px;
  max-width: 100%;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
