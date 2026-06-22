import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('@/views/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/merchant/intro',
    name: 'MerchantIntro',
    component: () => import('@/views/MerchantIntro.vue'),
    meta: { title: '商家入驻' }
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/MerchantRegister.vue'),
    meta: { title: '商家注册' }
  },
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/MerchantLogin.vue'),
    meta: { title: '商家登录' }
  },
  {
    path: '/s/:encoded',
    name: 'ShareRedirect',
    component: () => import('@/views/ShareRedirect.vue'),
    meta: { title: '分享链接' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'scenic',
        name: 'ScenicList',
        component: () => import('@/views/ScenicList.vue'),
        meta: { title: '景点' }
      },
      {
        path: 'scenic/:id',
        name: 'ScenicDetail',
        component: () => import('@/views/ScenicDetail.vue'),
        meta: { title: '景点详情' }
      },
      {
        path: 'hotel',
        name: 'HotelList',
        component: () => import('@/views/HotelList.vue'),
        meta: { title: '酒店' }
      },
      {
        path: 'hotel/:id',
        name: 'HotelDetail',
        component: () => import('@/views/HotelDetail.vue'),
        meta: { title: '酒店详情' }
      },
      {
        path: 'guide',
        name: 'GuideList',
        component: () => import('@/views/GuideList.vue'),
        meta: { title: '攻略' }
      },
      {
        path: 'guide/:id',
        name: 'GuideDetail',
        component: () => import('@/views/GuideDetail.vue'),
        meta: { title: '攻略详情' }
      },
      {
        path: 'routes',
        name: 'RouteList',
        component: () => import('@/views/RouteList.vue'),
        meta: { title: '路线' }
      },
      {
        path: 'routes/:id',
        name: 'RouteDetail',
        component: () => import('@/views/RouteDetail.vue'),
        meta: { title: '路线详情' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/Community.vue'),
        meta: { title: '社区' }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { title: 'AI助手', requireAuth: true }
      },
      {
        path: 'customer-service',
        name: 'CustomerChat',
        component: () => import('@/views/CustomerChat.vue'),
        meta: { title: '联系客服' }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/UserCenter.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('@/views/Payment.vue'),
        meta: { title: '订单支付', requireAuth: true }
      },
      {
        path: 'payment/result',
        name: 'PaymentResult',
        component: () => import('@/views/PaymentResult.vue'),
        meta: { title: '支付结果' }
      },
      {
        path: 'ranking',
        name: 'Ranking',
        component: () => import('@/views/Ranking.vue'),
        meta: { title: '热门排行' }
      },
      {
        path: 'route-planner',
        name: 'RoutePlanner',
        component: () => import('@/views/RoutePlanner.vue'),
        meta: { title: '行程规划' }
      },
      {
        path: 'coupon',
        name: 'CouponCenter',
        component: () => import('@/views/CouponCenter.vue'),
        meta: { title: '优惠券中心' }
      }
    ]
  },
  {
    path: '/merchant',
    component: () => import('@/views/merchant/Layout.vue'),
    redirect: '/merchant/dashboard',
    meta: { requireMerchantAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'scenic',
        name: 'MerchantScenic',
        component: () => import('@/views/merchant/ScenicManage.vue'),
        meta: { title: '景点管理', merchantType: 'SCENIC' }
      },
      {
        path: 'hotel',
        name: 'MerchantHotel',
        component: () => import('@/views/merchant/HotelManage.vue'),
        meta: { title: '酒店管理', merchantType: 'HOTEL' }
      },
      {
        path: 'rooms',
        name: 'MerchantRooms',
        component: () => import('@/views/merchant/RoomManage.vue'),
        meta: { title: '房型管理', merchantType: 'HOTEL' }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'profile',
        name: 'MerchantProfile',
        component: () => import('@/views/merchant/Profile.vue'),
        meta: { title: '商家信息' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    redirect: '/admin/dashboard',
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'scenic',
        name: 'AdminScenic',
        component: () => import('@/views/admin/ScenicManage.vue'),
        meta: { title: '景点管理' }
      },
      {
        path: 'hotel',
        name: 'AdminHotel',
        component: () => import('@/views/admin/HotelManage.vue'),
        meta: { title: '酒店管理' }
      },
      {
        path: 'guide',
        name: 'AdminGuide',
        component: () => import('@/views/admin/GuideManage.vue'),
        meta: { title: '攻略管理' }
      },
      {
        path: 'community',
        name: 'AdminCommunity',
        component: () => import('@/views/admin/CommunityManage.vue'),
        meta: { title: '社区管理' }
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'customer-service',
        name: 'CustomerService',
        component: () => import('@/views/admin/CustomerService.vue'),
        meta: { title: '客服工作台' }
      },
      {
        path: 'banner',
        name: 'AdminBanner',
        component: () => import('@/views/admin/BannerManage.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'route',
        name: 'AdminRoute',
        component: () => import('@/views/admin/RouteManage.vue'),
        meta: { title: '路线管理' }
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/ReviewManage.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'coupon',
        name: 'AdminCoupon',
        component: () => import('@/views/admin/CouponManage.vue'),
        meta: { title: '优惠券管理' }
      },
      {
        path: 'share-statistics',
        name: 'AdminShareStatistics',
        component: () => import('@/views/admin/ShareStatistics.vue'),
        meta: { title: '分享统计' }
      },
      {
        path: 'companion',
        name: 'AdminCompanion',
        component: () => import('@/views/admin/CompanionManage.vue'),
        meta: { title: '结伴管理' }
      },
      {
        path: 'qa',
        name: 'AdminQa',
        component: () => import('@/views/admin/QaManage.vue'),
        meta: { title: '问答管理' }
      },
      {
        path: 'merchant/scenic-audit',
        name: 'AdminMerchantScenicAudit',
        component: () => import('@/views/admin/MerchantScenicAudit.vue'),
        meta: { title: '景点商家审核' }
      },
      {
        path: 'merchant/hotel-audit',
        name: 'AdminMerchantHotelAudit',
        component: () => import('@/views/admin/MerchantHotelAudit.vue'),
        meta: { title: '酒店商家审核' }
      },
      {
        path: 'merchant/manage',
        name: 'AdminMerchantManage',
        component: () => import('@/views/admin/MerchantManage.vue'),
        meta: { title: '商家管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 诗画浙江` : '诗画浙江 · 智游天下'
  
  const userStore = useUserStore()
  
  // 判断是否是后台路由
  const isAdminRoute = to.path.startsWith('/admin')
  const isMerchantRoute = to.path.startsWith('/merchant') && !to.path.includes('/merchant/intro') && !to.path.includes('/merchant/register') && !to.path.includes('/merchant/login')
  
  if (isAdminRoute) {
    // 后台路由：检查管理员登录状态
    if (to.meta.requireAdmin && !userStore.isAdminLoggedIn) {
      next({ path: '/admin-login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else if (isMerchantRoute) {
    // 商家路由：检查商家登录状态
    const merchantToken = localStorage.getItem('merchantToken')
    const merchantInfo = JSON.parse(localStorage.getItem('merchantInfo') || '{}')
    
    if (!merchantToken) {
      next({ path: '/merchant/login', query: { redirect: to.fullPath } })
      return
    }
    
    // 检查商家类型权限
    if (to.meta.merchantType && merchantInfo.merchantType !== to.meta.merchantType) {
      ElMessage.error('您没有权限访问此页面')
      next('/merchant/dashboard')
      return
    }
    
    next()
  } else {
    // 前台路由：检查普通用户登录状态
    if (to.meta.requireAuth && !userStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  }
})

export default router
