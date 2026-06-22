<template>
  <div class="merchant-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon><Shop /></el-icon>
        <span>商家后台</span>
      </div>
      <div class="merchant-info">
        <el-avatar :size="50">
          {{ merchantInfo.merchantName?.charAt(0) || '商' }}
        </el-avatar>
        <div class="info">
          <div class="name">{{ merchantInfo.merchantName || '商家' }}</div>
          <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
        </div>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409eff"
      >
        <el-menu-item index="/merchant/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <template v-if="merchantInfo.merchantType === 'SCENIC'">
          <el-menu-item index="/merchant/scenic">
            <el-icon><Location /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
        </template>
        <template v-if="merchantInfo.merchantType === 'HOTEL'">
          <el-menu-item index="/merchant/hotel">
            <el-icon><OfficeBuilding /></el-icon>
            <span>酒店管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/rooms">
            <el-icon><Grid /></el-icon>
            <span>房型管理</span>
          </el-menu-item>
        </template>
        <el-menu-item index="/merchant/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/profile">
          <el-icon><User /></el-icon>
          <span>商家信息</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <div class="back-link">
          <router-link to="/home">
            <el-icon><Back /></el-icon>
            返回前台
          </router-link>
        </div>
        <div class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </div>
      </div>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantInfo } from '@/api/merchant'

const router = useRouter()
const merchantInfo = ref({})

const statusType = computed(() => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'DISABLED': 'info'
  }
  return statusMap[merchantInfo.value.status] || 'info'
})

const statusText = computed(() => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'DISABLED': '已禁用'
  }
  return textMap[merchantInfo.value.status] || '未知'
})

const loadMerchantInfo = async () => {
  try {
    const res = await getMerchantInfo()
    merchantInfo.value = res || {}
    
    // 如果商家未通过审核，提示并跳转
    if (res?.status === 'PENDING') {
      ElMessage.warning('您的商家账号正在审核中，请耐心等待')
    } else if (res?.status === 'REJECTED') {
      ElMessage.error(`您的商家账号审核未通过：${res.rejectReason || '请联系管理员'}`)
    } else if (res?.status === 'DISABLED') {
      ElMessage.error('您的商家账号已被禁用，请联系管理员')
    }
  } catch (error) {
    console.error('获取商家信息失败:', error)
    ElMessage.error('获取商家信息失败')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('merchantToken')
    localStorage.removeItem('merchantInfo')
    ElMessage.success('已退出登录')
    router.push('/merchant/login')
  }).catch(() => {})
}

onMounted(() => {
  loadMerchantInfo()
})
</script>

<style lang="scss" scoped>
.merchant-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: #001529;
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.merchant-info {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  
  .info {
    flex: 1;
    min-width: 0;
    
    .name {
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.el-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.back-link {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  a {
    color: rgba(255, 255, 255, 0.65);
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    
    &:hover {
      color: #fff;
    }
  }
}

.logout-btn {
  padding: 16px 20px;
  color: rgba(255, 255, 255, 0.65);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    color: #ff4d4f;
    background: rgba(255, 77, 79, 0.1);
  }
}

.main-content {
  flex: 1;
  margin-left: 220px;
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
