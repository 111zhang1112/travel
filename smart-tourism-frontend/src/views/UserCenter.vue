<template>
  <div class="user-center-page">
    <div class="user-center-container">
      <!-- 左侧导航 -->
      <aside class="sidebar">
        <!-- 用户信息卡片 -->
        <div class="user-profile-card">
          <div class="avatar-section">
            <el-avatar :size="80" :src="userStore.user?.avatar">
              {{ userStore.user?.nickname?.charAt(0) }}
            </el-avatar>
            <el-upload
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
              class="avatar-upload"
            >
              <div class="upload-trigger">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
          </div>
          <h3 class="user-name">{{ userStore.user?.nickname }}</h3>
          <p class="user-id">ID: {{ userStore.user?.id }}</p>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <div 
            v-for="item in menuItems" 
            :key="item.key"
            class="menu-item"
            :class="{ active: activeMenu === item.key }"
            @click="activeMenu = item.key"
          >
            <el-icon class="menu-icon">
              <component :is="item.icon" />
            </el-icon>
            <span class="menu-label">{{ item.label }}</span>
            <el-badge v-if="item.badge" :value="item.badge" class="menu-badge" />
          </div>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="main-content">
        <!-- 个人资料 -->
        <div v-show="activeMenu === 'profile'" class="content-panel">
          <h2 class="panel-title">个人资料</h2>
          <el-form :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="头像">
              <el-avatar :size="100" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) }}
              </el-avatar>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 我的订单 -->
        <div v-show="activeMenu === 'orders'" class="content-panel">
          <div class="panel-header">
            <h2 class="panel-title">我的订单</h2>
            <el-button 
              type="primary" 
              :icon="Refresh" 
              @click="loadOrders"
              :loading="loadingOrders"
              size="default"
            >
              刷新
            </el-button>
          </div>
          
          <!-- 订单分类标签 -->
          <el-tabs v-model="orderType" class="order-tabs" @tab-change="handleOrderTypeChange">
            <el-tab-pane label="全部订单" name="all">
              <div class="order-list">
                <div v-for="order in filteredOrders" :key="order.id" class="order-card">
                  <div class="order-header">
                    <div class="order-header-left">
                      <el-tag :type="order.productType === 'HOTEL' ? 'success' : 'primary'" size="small">
                        {{ order.productType === 'HOTEL' ? '酒店' : '景点' }}
                      </el-tag>
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                    </div>
                    <el-tag :type="getOrderStatusType(order.status)">
                      {{ getOrderStatusText(order.status) }}
                    </el-tag>
                  </div>
                  <div class="order-body">
                    <div class="order-info">
                      <h4>{{ order.productName }}</h4>
                      <p class="order-meta">
                        <span>数量：{{ order.quantity }}</span>
                        <span>{{ formatDate(order.createTime) }}</span>
                      </p>
                    </div>
                    <div class="order-amount">¥{{ order.amount }}</div>
                  </div>
                  
                  <div class="order-footer">
                    <template v-if="order.status === 'PENDING'">
                      <el-button size="small" @click="cancelOrder(order.orderNo)">取消订单</el-button>
                      <el-button type="primary" size="small" @click="retryPayment(order.orderNo)">
                        去支付
                      </el-button>
                    </template>
                  </div>
                </div>
                <el-empty v-if="filteredOrders.length === 0" description="暂无订单" />
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="酒店订单" name="hotel">
              <div class="order-list">
                <div v-for="order in filteredOrders" :key="order.id" class="order-card">
                  <div class="order-header">
                    <div class="order-header-left">
                      <el-tag type="success" size="small">酒店</el-tag>
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                    </div>
                    <el-tag :type="getOrderStatusType(order.status)">
                      {{ getOrderStatusText(order.status) }}
                    </el-tag>
                  </div>
                  <div class="order-body">
                    <div class="order-info">
                      <h4>{{ order.productName }}</h4>
                      <p class="order-meta">
                        <span>数量：{{ order.quantity }}</span>
                        <span v-if="order.checkInDate && order.checkOutDate">
                          入住：{{ formatDate(order.checkInDate) }} - {{ formatDate(order.checkOutDate) }}
                        </span>
                        <span v-if="order.nights">{{ order.nights }}晚</span>
                        <span>{{ formatDate(order.createTime) }}</span>
                      </p>
                    </div>
                    <div class="order-amount">¥{{ order.amount }}</div>
                  </div>
                  
                  <div class="order-footer">
                    <template v-if="order.status === 'PENDING'">
                      <el-button size="small" @click="cancelOrder(order.orderNo)">取消订单</el-button>
                      <el-button type="primary" size="small" @click="retryPayment(order.orderNo)">
                        去支付
                      </el-button>
                    </template>
                  </div>
                </div>
                <el-empty v-if="filteredOrders.length === 0" description="暂无酒店订单" />
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="景点订单" name="scenic">
              <div class="order-list">
                <div v-for="order in filteredOrders" :key="order.id" class="order-card">
                  <div class="order-header">
                    <div class="order-header-left">
                      <el-tag type="primary" size="small">景点</el-tag>
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                    </div>
                    <el-tag :type="getOrderStatusType(order.status)">
                      {{ getOrderStatusText(order.status) }}
                    </el-tag>
                  </div>
                  <div class="order-body">
                    <div class="order-info">
                      <h4>{{ order.productName }}</h4>
                      <p class="order-meta">
                        <span>数量：{{ order.quantity }}</span>
                        <span>{{ formatDate(order.createTime) }}</span>
                      </p>
                    </div>
                    <div class="order-amount">¥{{ order.amount }}</div>
                  </div>
                  
                  <div class="order-footer">
                    <template v-if="order.status === 'PENDING'">
                      <el-button size="small" @click="cancelOrder(order.orderNo)">取消订单</el-button>
                      <el-button type="primary" size="small" @click="retryPayment(order.orderNo)">
                        去支付
                      </el-button>
                    </template>
                  </div>
                </div>
                <el-empty v-if="filteredOrders.length === 0" description="暂无景点订单" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 我的收藏 -->
        <div v-show="activeMenu === 'favorites'" class="content-panel">
          <h2 class="panel-title">我的收藏</h2>
          <el-tabs v-model="favoriteType" class="favorite-tabs">
            <el-tab-pane label="景点" name="scenic">
              <div class="favorite-grid">
                <div v-for="item in scenicFavorites" :key="item.id" class="favorite-card">
                  <img :src="item.image" :alt="item.name" class="favorite-image" />
                  <div class="favorite-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.location }}</p>
                    <div class="favorite-actions">
                      <el-button size="small" @click="viewDetail('scenic', item.id)">
                        查看详情
                      </el-button>
                      <el-button size="small" text type="danger" @click="removeFavorite('scenic', item.id)">
                        取消收藏
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-if="scenicFavorites.length === 0" description="暂无收藏" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="酒店" name="hotel">
              <div class="favorite-grid">
                <div v-for="item in hotelFavorites" :key="item.id" class="favorite-card">
                  <img :src="item.image" :alt="item.name" class="favorite-image" />
                  <div class="favorite-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.location }}</p>
                    <div class="favorite-actions">
                      <el-button size="small" @click="viewDetail('hotel', item.id)">
                        查看详情
                      </el-button>
                      <el-button size="small" text type="danger" @click="removeFavorite('hotel', item.id)">
                        取消收藏
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-if="hotelFavorites.length === 0" description="暂无收藏" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 我的评价 -->
        <div v-show="activeMenu === 'reviews'" class="content-panel">
          <h2 class="panel-title">我的评价</h2>
          <div class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-card">
              <div class="review-header">
                <el-tag :type="review.targetType === 'SCENIC' ? 'primary' : 'success'" size="small">
                  {{ review.targetType === 'SCENIC' ? '景点' : '酒店' }}
                </el-tag>
                <span class="target-name">{{ review.targetName }}</span>
                <el-button type="danger" size="small" text @click="handleDeleteReview(review.id)">
                  删除
                </el-button>
              </div>
              <el-rate v-model="review.rating" disabled show-score />
              <p class="review-content">{{ review.content }}</p>
              <div class="review-time">{{ formatDate(review.createTime) }}</div>
            </div>
            <el-empty v-if="reviews.length === 0" description="暂无评价" />
          </div>
          <el-pagination
            v-if="reviewTotal > reviewPageSize"
            v-model:current-page="reviewPage"
            :page-size="reviewPageSize"
            :total="reviewTotal"
            layout="prev, pager, next"
            @current-change="loadReviews"
            class="pagination"
          />
        </div>

        <!-- 我的优惠券 -->
        <div v-show="activeMenu === 'coupons'" class="content-panel">
          <h2 class="panel-title">我的优惠券</h2>
          <div class="filter-bar">
            <el-radio-group v-model="couponStatus" @change="loadCoupons">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="UNUSED">未使用</el-radio-button>
              <el-radio-button label="USED">已使用</el-radio-button>
              <el-radio-button label="EXPIRED">已过期</el-radio-button>
            </el-radio-group>
          </div>
          <div class="coupon-list">
            <div v-for="uc in coupons" :key="uc.id" class="coupon-card" :class="{ disabled: uc.status !== 'UNUSED' }">
              <div class="coupon-left">
                <div class="coupon-value" :class="uc.coupon?.type === 'DISCOUNT' ? 'discount' : 'amount'">
                  <template v-if="uc.coupon?.type === 'AMOUNT'">
                    <span class="currency">¥</span>
                    <span class="value">{{ uc.coupon?.value }}</span>
                  </template>
                  <template v-else>
                    <span class="value">{{ ((uc.coupon?.value || 1) * 10).toFixed(0) }}</span>
                    <span class="unit">折</span>
                  </template>
                </div>
              </div>
              <div class="coupon-right">
                <h4>{{ uc.coupon?.name }}</h4>
                <p class="coupon-time">有效期至 {{ formatDate(uc.coupon?.endTime) }}</p>
                <el-tag :type="getCouponStatusType(uc.status)" size="small">
                  {{ getCouponStatusText(uc.status) }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="coupons.length === 0" description="暂无优惠券" />
          </div>
          <el-pagination
            v-if="couponTotal > 10"
            v-model:current-page="couponPage"
            :page-size="10"
            :total="couponTotal"
            layout="prev, pager, next"
            @current-change="loadCoupons"
            class="pagination"
          />
        </div>

        <!-- 我的积分 -->
        <div v-show="activeMenu === 'points'" class="content-panel">
          <h2 class="panel-title">我的积分</h2>
          <div class="points-banner">
            <div class="points-icon">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="points-info">
              <div class="points-label">当前积分</div>
              <div class="points-value">{{ pointsBalance }}</div>
            </div>
          </div>
          <div class="points-records">
            <div v-for="record in pointsRecords" :key="record.id" class="points-record">
              <div class="record-left">
                <div class="record-icon" :class="{ positive: record.points > 0 }">
                  <el-icon v-if="record.points > 0"><Plus /></el-icon>
                  <el-icon v-else><Minus /></el-icon>
                </div>
                <div class="record-info">
                  <div class="record-type">{{ getPointsTypeText(record.type) }}</div>
                  <div class="record-desc">{{ record.description }}</div>
                  <div class="record-time">{{ formatDate(record.createTime) }}</div>
                </div>
              </div>
              <div class="record-points" :class="{ positive: record.points > 0 }">
                {{ record.points > 0 ? '+' : '' }}{{ record.points }}
              </div>
            </div>
            <el-empty v-if="pointsRecords.length === 0" description="暂无积分记录" />
          </div>
          <el-pagination
            v-if="pointsTotal > 10"
            v-model:current-page="pointsPage"
            :page-size="10"
            :total="pointsTotal"
            layout="prev, pager, next"
            @current-change="loadPoints"
            class="pagination"
          />
        </div>

        <!-- 修改密码 -->
        <div v-show="activeMenu === 'password'" class="content-panel">
          <h2 class="panel-title">修改密码</h2>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
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
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                确认修改
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </main>
    </div>
  </div>
</template>


<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderList, payOrder as payOrderApi, cancelOrder as cancelOrderApi } from '@/api/order'
import { getMyReviews, deleteReview } from '@/api/review'
import { getMyCoupons } from '@/api/coupon'
import { getPointsBalance, getPointsRecords } from '@/api/points'
import { updateAvatar, updateUserInfo, uploadImage, changePassword } from '@/api/user'
import { getFavoriteList, removeFavorite as removeFavoriteApi } from '@/api/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, ShoppingBag, Star, TrophyBase, Lock, StarFilled,
  Camera, Medal, Plus, Minus, Refresh
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('profile')
const favoriteType = ref('scenic')

// 菜单项
const menuItems = computed(() => [
  { key: 'profile', label: '个人资料', icon: User },
  { key: 'orders', label: '我的订单', icon: ShoppingBag, badge: orders.value.filter(o => o.status === 'PENDING').length || null },
  { key: 'favorites', label: '我的收藏', icon: StarFilled, badge: scenicFavorites.value.length + hotelFavorites.value.length || null },
  { key: 'reviews', label: '我的评价', icon: Star },
  { key: 'points', label: '我的积分', icon: TrophyBase },
  { key: 'password', label: '修改密码', icon: Lock }
])

// 个人资料
const profileForm = reactive({
  nickname: '',
  phone: '',
  email: ''
})
const updating = ref(false)

// 订单
const orders = ref([])
const loadingOrders = ref(false)
const orderType = ref('all') // 订单类型筛选: all, hotel, scenic

// 根据订单类型筛选订单,并按ID降序排序（ID越大越新）
const filteredOrders = computed(() => {
  let filtered = []
  if (orderType.value === 'all') {
    filtered = orders.value
  } else if (orderType.value === 'hotel') {
    filtered = orders.value.filter(order => order.productType === 'HOTEL')
  } else if (orderType.value === 'scenic') {
    filtered = orders.value.filter(order => order.productType === 'SCENIC')
  } else {
    filtered = orders.value
  }
  
  // 直接按ID降序排序（ID越大表示越新创建）
  // 这样比按时间排序更可靠，因为ID是自增的
  return [...filtered].sort((a, b) => b.id - a.id)
})

// 收藏
const scenicFavorites = ref([])
const hotelFavorites = ref([])

// 评价
const reviews = ref([])
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)

// 优惠券
const coupons = ref([])
const couponPage = ref(1)
const couponTotal = ref(0)
const couponStatus = ref('')

// 积分
const pointsBalance = ref(0)
const pointsRecords = ref([])
const pointsPage = ref(1)
const pointsTotal = ref(0)

// 修改密码
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changingPassword = ref(false)

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  initProfileForm()
  loadOrders()
  loadFavorites()
  loadReviews()
  loadCoupons()
  loadPoints()
  
  // 监听页面可见性变化，当页面重新可见时刷新订单
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const handleVisibilityChange = () => {
  if (!document.hidden && activeMenu.value === 'orders') {
    console.log('页面重新可见，刷新订单列表')
    loadOrders()
  }
}

const initProfileForm = () => {
  profileForm.nickname = userStore.user?.nickname || ''
  profileForm.phone = userStore.user?.phone || ''
  profileForm.email = userStore.user?.email || ''
}

// 头像上传
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async ({ file }) => {
  try {
    const res = await uploadImage(file)
    const avatarUrl = res.url
    await updateAvatar(avatarUrl)
    await userStore.fetchUserInfo()
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error(e.message || '头像上传失败')
  }
}

// 更新个人资料
const handleUpdateProfile = async () => {
  updating.value = true
  try {
    await updateUserInfo(profileForm)
    await userStore.fetchUserInfo()
    ElMessage.success('资料更新成功')
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    updating.value = false
  }
}

// 订单相关
const loadOrders = async () => {
  if (loadingOrders.value) return
  
  loadingOrders.value = true
  try {
    console.log('开始加载订单列表...')
    const res = await getOrderList({ page: 1, size: 50 })
    console.log('订单列表响应:', res)
    orders.value = res.records || []
    console.log('订单数量:', orders.value.length)
    
    // 调试：打印前5个订单的创建时间和ID
    if (orders.value.length > 0) {
      console.log('前5个订单的创建时间和ID:')
      orders.value.slice(0, 5).forEach(order => {
        console.log(`ID: ${order.id}, 订单号: ${order.orderNo}, 创建时间: ${order.createTime}`)
      })
    }
    
    if (orders.value.length === 0) {
      console.warn('订单列表为空')
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    ElMessage.error('加载订单失败: ' + (e.message || '未知错误'))
  } finally {
    loadingOrders.value = false
  }
}

const getOrderStatusType = (status) => {
  const map = { PENDING: 'warning', PAID: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getOrderStatusText = (status) => {
  const map = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

const retryPayment = async (orderNo) => {
  try {
    // 跳转到支付页面
    router.push({
      path: '/payment',
      query: { orderNo }
    })
  } catch (e) {
    ElMessage.error('跳转失败: ' + (e.message || '未知错误'))
  }
}

const cancelOrder = async (orderNo) => {
  await ElMessageBox.confirm('确认取消该订单？', '取消确认')
  await cancelOrderApi(orderNo)
  ElMessage.success('订单已取消')
  loadOrders()
}

const handleOrderTypeChange = (type) => {
  console.log('切换订单类型:', type)
}

// 收藏相关
const loadFavorites = async () => {
  try {
    // 加载景点收藏
    const scenicList = await getFavoriteList('SCENIC')
    scenicFavorites.value = scenicList.map(item => ({
      id: item.targetId,
      name: item.name,
      location: item.location,
      image: item.image || 'https://via.placeholder.com/280x180'
    }))
    
    // 加载酒店收藏
    const hotelList = await getFavoriteList('HOTEL')
    hotelFavorites.value = hotelList.map(item => ({
      id: item.targetId,
      name: item.name,
      location: item.location,
      image: item.image || 'https://via.placeholder.com/280x180'
    }))
  } catch (e) {
    console.error('加载收藏失败', e)
  }
}

const viewDetail = (type, id) => {
  if (type === 'scenic') {
    router.push(`/scenic/${id}`)
  } else if (type === 'hotel') {
    router.push(`/hotel/${id}`)
  }
}

const removeFavorite = async (type, id) => {
  await ElMessageBox.confirm('确认取消收藏？', '提示')
  try {
    const targetType = type === 'scenic' ? 'SCENIC' : 'HOTEL'
    await removeFavoriteApi(targetType, id)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e) {
    ElMessage.error(e.message || '取消收藏失败')
  }
}

// 评价相关
const loadReviews = async () => {
  try {
    const res = await getMyReviews({
      page: reviewPage.value,
      size: reviewPageSize.value
    })
    reviews.value = res.records || []
    reviewTotal.value = res.total || 0
  } catch (e) {
    console.error('加载评价失败', e)
  }
}

const handleDeleteReview = async (reviewId) => {
  await ElMessageBox.confirm('确认删除该评价？', '删除确认')
  await deleteReview(reviewId)
  ElMessage.success('评价已删除')
  loadReviews()
}

// 优惠券相关
const loadCoupons = async () => {
  try {
    const res = await getMyCoupons({
      status: couponStatus.value,
      page: couponPage.value,
      size: 10
    })
    coupons.value = res.records || []
    couponTotal.value = res.total || 0
  } catch (e) {
    console.error('加载优惠券失败', e)
  }
}

const getCouponStatusType = (status) => {
  const map = { UNUSED: 'success', USED: 'info', EXPIRED: 'info' }
  return map[status] || 'info'
}

const getCouponStatusText = (status) => {
  const map = { UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' }
  return map[status] || status
}

// 积分相关
const loadPoints = async () => {
  try {
    pointsBalance.value = await getPointsBalance() || 0
    const res = await getPointsRecords({
      page: pointsPage.value,
      size: 10
    })
    pointsRecords.value = res.records || []
    pointsTotal.value = res.total || 0
  } catch (e) {
    console.error('加载积分失败', e)
  }
}

const getPointsTypeText = (type) => {
  const map = { ORDER: '订单奖励', REVIEW: '评价奖励', EXCHANGE: '积分兑换', ADMIN: '系统调整' }
  return map[type] || type
}

// 修改密码
const handleChangePassword = async () => {
  await passwordFormRef.value.validate()
  changingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    resetPasswordForm()
    // 退出登录
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (e) {
    ElMessage.error(e.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

// 工具函数
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>


<style lang="scss" scoped>
.user-center-page {
  min-height: calc(100vh - 140px);
  background: #f5f7fa;
  padding: 20px;
}

.user-center-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  
  @media (max-width: 968px) {
    flex-direction: column;
  }
}

// 左侧边栏
.sidebar {
  width: 280px;
  flex-shrink: 0;
  
  @media (max-width: 968px) {
    width: 100%;
  }
}

.user-profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  
  .avatar-section {
    position: relative;
    display: inline-block;
    margin-bottom: 16px;
    
    .el-avatar {
      border: 4px solid rgba(255, 255, 255, 0.3);
    }
    
    .avatar-upload {
      position: absolute;
      bottom: 0;
      right: 0;
      
      .upload-trigger {
        width: 32px;
        height: 32px;
        background: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #667eea;
        font-size: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        transition: all 0.3s;
        
        &:hover {
          transform: scale(1.1);
        }
      }
    }
  }
  
  .user-name {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  .user-id {
    font-size: 13px;
    opacity: 0.8;
    margin: 0;
  }
}

.nav-menu {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  .menu-item {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    cursor: pointer;
    transition: all 0.3s;
    border-left: 3px solid transparent;
    position: relative;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.active {
      background: #f0f2ff;
      border-left-color: #667eea;
      color: #667eea;
      
      .menu-icon {
        color: #667eea;
      }
    }
    
    .menu-icon {
      font-size: 20px;
      margin-right: 12px;
      color: #666;
    }
    
    .menu-label {
      flex: 1;
      font-size: 15px;
      font-weight: 500;
    }
    
    .menu-badge {
      margin-left: auto;
    }
  }
}

// 右侧内容区
.main-content {
  flex: 1;
  min-width: 0;
}

.content-panel {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  .panel-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    color: #2c3e50;
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
  
  .panel-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    color: #2c3e50;
  }
}

// 个人资料表单
.profile-form {
  max-width: 600px;
  
  .el-avatar {
    cursor: pointer;
  }
}

// 订单标签
.order-tabs {
  margin-top: 20px;
  
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

// 订单列表
.order-list {
  display: grid;
  gap: 16px;
}

.order-card {
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  }
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
    
    .order-header-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .order-no {
      font-size: 13px;
      color: #999;
    }
  }
  
  .order-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .order-info {
      flex: 1;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 8px;
        color: #2c3e50;
      }
      
      .order-meta {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: #999;
      }
    }
    
    .order-amount {
      font-size: 24px;
      font-weight: 700;
      color: #e74c3c;
    }
  }
  
  .order-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 收藏
.favorite-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.favorite-card {
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    transform: translateY(-4px);
  }
  
  .favorite-image {
    width: 100%;
    height: 180px;
    object-fit: cover;
  }
  
  .favorite-info {
    padding: 16px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 8px;
      color: #2c3e50;
    }
    
    p {
      font-size: 13px;
      color: #999;
      margin: 0 0 12px;
    }
    
    .favorite-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 评价列表
.review-list {
  display: grid;
  gap: 16px;
}

.review-card {
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  }
  
  .review-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    
    .target-name {
      flex: 1;
      font-weight: 600;
      font-size: 15px;
      color: #2c3e50;
    }
  }
  
  .review-content {
    margin: 12px 0;
    line-height: 1.8;
    color: #555;
    font-size: 14px;
  }
  
  .review-time {
    font-size: 12px;
    color: #999;
  }
}

// 优惠券
.filter-bar {
  margin-bottom: 20px;
}

.coupon-list {
  display: grid;
  gap: 16px;
}

.coupon-card {
  display: flex;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  
  &:hover:not(.disabled) {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    transform: translateX(4px);
  }
  
  &.disabled {
    opacity: 0.5;
    filter: grayscale(1);
  }
  
  .coupon-left {
    width: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    
    .coupon-value {
      display: flex;
      align-items: baseline;
      gap: 4px;
      
      &.amount {
        color: #fff;
      }
      
      &.discount {
        color: #fff;
      }
      
      .currency {
        font-size: 18px;
        font-weight: 600;
      }
      
      .value {
        font-size: 40px;
        font-weight: 700;
        line-height: 1;
      }
      
      .unit {
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .coupon-right {
    flex: 1;
    padding: 20px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 8px;
      color: #2c3e50;
    }
    
    .coupon-time {
      font-size: 13px;
      color: #999;
      margin: 0 0 12px;
    }
  }
}

// 积分
.points-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 24px;
  color: #fff;
  
  .points-icon {
    font-size: 64px;
    opacity: 0.9;
  }
  
  .points-info {
    .points-label {
      font-size: 16px;
      opacity: 0.9;
      margin-bottom: 8px;
    }
    
    .points-value {
      font-size: 56px;
      font-weight: 700;
    }
  }
}

.points-records {
  display: grid;
  gap: 12px;
}

.points-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  }
  
  .record-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .record-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      background: #fee;
      color: #e74c3c;
      
      &.positive {
        background: #e8f5e9;
        color: #4caf50;
      }
    }
    
    .record-info {
      .record-type {
        font-weight: 600;
        font-size: 14px;
        margin-bottom: 4px;
        color: #2c3e50;
      }
      
      .record-desc {
        font-size: 12px;
        color: #999;
        margin-bottom: 2px;
      }
      
      .record-time {
        font-size: 11px;
        color: #bbb;
      }
    }
  }
  
  .record-points {
    font-size: 24px;
    font-weight: 700;
    color: #e74c3c;
    
    &.positive {
      color: #4caf50;
    }
  }
}

// 修改密码表单
.password-form {
  max-width: 500px;
}

// 分页
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
