<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-sub-menu index="content">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/scenic">
            <el-icon><Location /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/hotel">
            <el-icon><House /></el-icon>
            <span>酒店管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/guide">
            <el-icon><Document /></el-icon>
            <span>攻略管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/community">
            <el-icon><ChatLineSquare /></el-icon>
            <span>社区管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/companion">
            <el-icon><User /></el-icon>
            <span>结伴管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/qa">
            <el-icon><QuestionFilled /></el-icon>
            <span>问答管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/customer-service">
          <el-icon><ChatDotRound /></el-icon>
          <span>客服工作台</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </template>
          <el-menu-item index="/admin/banner">
            <el-icon><Picture /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notice">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/route">
            <el-icon><Guide /></el-icon>
            <span>路线管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="marketing">
          <template #title>
            <el-icon><Ticket /></el-icon>
            <span>营销管理</span>
          </template>
          <el-menu-item index="/admin/review">
            <el-icon><ChatLineSquare /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/coupon">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/share-statistics">
            <el-icon><Share /></el-icon>
            <span>分享统计</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="merchant">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>商家管理</span>
          </template>
          <el-menu-item index="/admin/merchant/scenic-audit">
            <el-icon><Location /></el-icon>
            <span>景点商家审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchant/hotel-audit">
            <el-icon><House /></el-icon>
            <span>酒店商家审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchant/manage">
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </el-menu-item>
        </el-sub-menu>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SwitchButton, Folder, ChatLineSquare, User, Picture, Grid, Bell, Guide, Ticket, Share, OfficeBuilding, Location, House, Shop } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logoutAdmin()
    ElMessage.success('已退出登录')
    router.push('/admin-login')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.admin-layout {
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

.el-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  
  /* 自定义滚动条样式 */
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
  overflow-x: hidden;
  position: relative;
  
  /* 风景背景图 - 使用Unsplash免费图片 */
  background-image: url('https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1920&q=80');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  
  /* 半透明白色遮罩，确保数据清晰可见 */
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 220px;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(2px);
    pointer-events: none;
    z-index: 0;
  }
  
  /* 确保内容在背景之上 */
  > * {
    position: relative;
    z-index: 1;
  }
  
  /* 自定义滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.3);
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 4px;
    
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}
</style>
