import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://travelbackend-production-7526.up.railway.app/api'
const request = axios.create({
  baseURL: BASE_URL,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 判断是否是管理后台请求
    const isAdminRequest = config.url?.startsWith('/admin/')
    // 判断是否是商家请求
    const isMerchantRequest = config.url?.startsWith('/merchant/')
    
    // 根据请求类型选择不同的 token
    let token
    if (isAdminRequest) {
      token = localStorage.getItem('adminToken')
    } else if (isMerchantRequest) {
      token = localStorage.getItem('merchantToken')
    } else {
      token = localStorage.getItem('token')
    }
    
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    
    // 如果code不是200，显示错误并拒绝
    if (code !== 200) {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
    
    // 直接返回data部分
    return data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      const isAdminRequest = error.config?.url?.startsWith('/admin/')
      const isMerchantRequest = error.config?.url?.startsWith('/merchant/')
      
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        
        if (isAdminRequest) {
          // 管理后台登录过期
          localStorage.removeItem('adminToken')
          localStorage.removeItem('adminUser')
          router.push('/admin-login')
        } else if (isMerchantRequest) {
          // 商家登录过期
          localStorage.removeItem('merchantToken')
          localStorage.removeItem('merchantInfo')
          router.push('/merchant/login')
        } else {
          // 前台登录过期
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
        }
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else {
        ElMessage.error(data?.message || '网络错误，请稍后重试')
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
