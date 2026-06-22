<template>
  <div class="admin-login-page">
    <div class="admin-login-box">
      <div class="logo-section">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <h2 class="title">管理后台</h2>
      </div>
      <p class="subtitle">智慧旅游管理系统</p>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="管理员账号" 
            prefix-icon="User" 
            size="large" 
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock" 
            size="large" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="submit-btn" 
            :loading="loading" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="user-entry">
        <router-link to="/login">
          <el-icon><Back /></el-icon>
          返回用户登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Setting, Back } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  
  try {
    await userStore.adminLogin(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.admin-login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.admin-login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 32px;
  color: #1a1a2e;
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.subtitle {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  :deep(.el-input__wrapper) {
    background: #f5f5f5;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, #2a2a3e 0%, #26314e 100%);
  }
}

.user-entry {
  text-align: center;
  margin-top: 24px;
  
  a {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #666;
    text-decoration: none;
    font-size: 14px;
    transition: color 0.3s;
    
    &:hover {
      color: #1a1a2e;
    }
  }
}
</style>
