<template>
  <div class="login-page">
    <div class="login-box">
      <h2 class="title">{{ isRegister ? '注册账号' : '诗画浙江欢迎您' }}</h2>
      <p class="subtitle">{{ isRegister ? '创建您的旅游账号' : '开启您的诗画之旅' }}</p>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        
        <template v-if="isRegister">
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称（选填）" prefix-icon="UserFilled" size="large" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号（选填）" prefix-icon="Phone" size="large" />
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="switch-mode">
        <span>{{ isRegister ? '已有账号？' : '没有账号？' }}</span>
        <el-link type="primary" @click="isRegister = !isRegister">
          {{ isRegister ? '立即登录' : '立即注册' }}
        </el-link>
      </div>
      
      <div class="admin-entry">
        <router-link to="/admin-login">
          <el-icon><Setting /></el-icon>
          管理员登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const isRegister = ref(false)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: ''
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

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  
  try {
    if (isRegister.value) {
      await userStore.register(form)
      ElMessage.success('注册成功')
    } else {
      await userStore.login(form.username, form.password)
      ElMessage.success('登录成功')
    }
    
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920') center/cover no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.8);
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.admin-entry {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  
  a {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #999;
    text-decoration: none;
    font-size: 13px;
    transition: color 0.3s;
    
    &:hover {
      color: #409eff;
    }
  }
}
</style>
