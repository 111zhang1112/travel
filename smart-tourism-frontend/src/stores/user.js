import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, adminLogin as adminLoginApi, register as registerApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 前台用户使用 'token' 和 'user'
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  // 后台管理员使用 'adminToken' 和 'adminUser'
  const adminToken = ref(localStorage.getItem('adminToken') || '')
  const adminUser = ref(JSON.parse(localStorage.getItem('adminUser') || 'null'))

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => !!token.value && !!user.value && user.value?.role === 'ADMIN')
  const isAdminLoggedIn = computed(() => !!adminToken.value && !!adminUser.value && adminUser.value?.role === 'ADMIN')

  // 初始化时验证token有效性
  async function initAuth() {
    if (!token.value) {
      logout()
      return
    }
    try {
      const res = await getUserInfo()
      user.value = res
      localStorage.setItem('user', JSON.stringify(res))
    } catch (e) {
      // token无效，清除登录状态
      logout()
    }
  }
  
  // 初始化管理员认证
  async function initAdminAuth() {
    if (!adminToken.value) {
      logoutAdmin()
      return
    }
    try {
      const res = await getUserInfo()
      adminUser.value = res
      localStorage.setItem('adminUser', JSON.stringify(res))
    } catch (e) {
      // token无效，清除登录状态
      logoutAdmin()
    }
  }

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.token
    user.value = res.user
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    return res
  }

  async function adminLogin(username, password) {
    const res = await adminLoginApi({ username, password })
    adminToken.value = res.token
    adminUser.value = res.user
    localStorage.setItem('adminToken', res.token)
    localStorage.setItem('adminUser', JSON.stringify(res.user))
    return res
  }

  async function register(data) {
    const res = await registerApi(data)
    return res
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      user.value = res
      localStorage.setItem('user', JSON.stringify(res))
    } catch (e) {
      logout()
    }
  }
  
  async function fetchAdminInfo() {
    if (!adminToken.value) return
    try {
      const res = await getUserInfo()
      adminUser.value = res
      localStorage.setItem('adminUser', JSON.stringify(res))
    } catch (e) {
      logoutAdmin()
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  function logoutAdmin() {
    adminToken.value = ''
    adminUser.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUser')
  }

  return {
    token,
    user,
    adminToken,
    adminUser,
    isLoggedIn,
    isAdmin,
    isAdminLoggedIn,
    login,
    adminLogin,
    register,
    fetchUserInfo,
    fetchAdminInfo,
    initAuth,
    initAdminAuth,
    logout,
    logoutAdmin
  }
})
