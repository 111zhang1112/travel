<template>
  <div class="layout">
    <header class="header">
      <div class="logo">
        <el-icon><Location /></el-icon>
        <span>诗画浙江</span>
      </div>
      <nav class="nav">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/scenic" class="nav-item">景点</router-link>
        <router-link to="/hotel" class="nav-item">酒店</router-link>
        <router-link to="/guide" class="nav-item">攻略</router-link>
        <router-link to="/routes" class="nav-item">路线</router-link>
        <router-link to="/route-planner" class="nav-item">行程规划</router-link>
        <router-link to="/ranking" class="nav-item">排行榜</router-link>
        <router-link to="/community" class="nav-item">社区</router-link>
        <router-link to="/coupon" class="nav-item">优惠券</router-link>
        <router-link to="/ai-chat" class="nav-item">AI助手</router-link>
        <router-link to="/merchant/intro" class="nav-item">商家入驻</router-link>
        <router-link to="/customer-service" class="nav-item">联系客服</router-link>
      </nav>
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
        </template>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>© 2026 智慧旅游推荐导览平台 | 让旅行更智能</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/home')
  } else if (command === 'user') {
    router.push('/user')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  padding: 0 40px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
  cursor: pointer;
}

.nav {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 40px;
}

.nav-item {
  color: #666;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;

  &:hover, &.router-link-active {
    color: var(--primary-color);
    border-bottom-color: var(--primary-color);
  }
}

.user-area {
  display: flex;
  align-items: center;
  
  :deep(.el-dropdown) {
    outline: none;
    
    &:focus {
      outline: none;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  
  &:focus {
    outline: none;
  }
}

.nickname {
  color: #333;
  font-size: 14px;
}

.main {
  flex: 1;
  background: var(--bg-color);
}

.footer {
  background: #333;
  color: #999;
  text-align: center;
  padding: 20px;
  font-size: 14px;
}
</style>
