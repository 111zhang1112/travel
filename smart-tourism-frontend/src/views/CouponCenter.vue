<template>
  <div class="coupon-center-page">
    <div class="page-header">
      <h1>优惠券中心</h1>
      <p>领取优惠券，享受更多折扣</p>
    </div>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="领券中心" name="available">
        <div class="coupon-grid">
          <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card" :class="{ received: coupon.received }">
            <div class="coupon-left" :class="coupon.type === 'DISCOUNT' ? 'discount' : 'amount'">
              <div class="coupon-value">
                <template v-if="coupon.type === 'AMOUNT'">
                  <span class="symbol">¥</span>
                  <span class="number">{{ coupon.value }}</span>
                </template>
                <template v-else>
                  <span class="number">{{ (coupon.value * 10).toFixed(0) }}</span>
                  <span class="symbol">折</span>
                </template>
              </div>
              <div class="coupon-condition" v-if="coupon.minAmount > 0">
                满{{ coupon.minAmount }}元可用
              </div>
              <div class="coupon-condition" v-else>无门槛</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.name }}</div>
              <div class="coupon-desc">{{ coupon.description }}</div>
              <div class="coupon-time">
                {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
              </div>
              <el-button 
                v-if="!coupon.received" 
                type="primary" 
                size="small"
                :disabled="!coupon.canReceive"
                @click="handleReceive(coupon.id)"
              >
                {{ coupon.remainCount > 0 ? '立即领取' : '已领完' }}
              </el-button>
              <el-tag v-else type="info" size="small">已领取</el-tag>
            </div>
          </div>
          <el-empty v-if="availableCoupons.length === 0" description="暂无可领取的优惠券" />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="我的优惠券" name="my">
        <div class="filter-bar">
          <el-radio-group v-model="couponStatus" @change="loadMyCoupons">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="UNUSED">未使用</el-radio-button>
            <el-radio-button label="USED">已使用</el-radio-button>
            <el-radio-button label="EXPIRED">已过期</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="coupon-grid">
          <div 
            v-for="uc in myCoupons" 
            :key="uc.id" 
            class="coupon-card"
            :class="{ disabled: uc.status !== 'UNUSED' }"
          >
            <div class="coupon-left" :class="uc.coupon?.type === 'DISCOUNT' ? 'discount' : 'amount'">
              <div class="coupon-value">
                <template v-if="uc.coupon?.type === 'AMOUNT'">
                  <span class="symbol">¥</span>
                  <span class="number">{{ uc.coupon?.value }}</span>
                </template>
                <template v-else>
                  <span class="number">{{ ((uc.coupon?.value || 1) * 10).toFixed(0) }}</span>
                  <span class="symbol">折</span>
                </template>
              </div>
              <div class="coupon-condition" v-if="uc.coupon?.minAmount > 0">
                满{{ uc.coupon?.minAmount }}元可用
              </div>
              <div class="coupon-condition" v-else>无门槛</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ uc.coupon?.name }}</div>
              <div class="coupon-desc">{{ uc.coupon?.description }}</div>
              <div class="coupon-time">
                有效期至 {{ formatDate(uc.coupon?.endTime) }}
              </div>
              <el-tag :type="getStatusType(uc.status)" size="small">
                {{ getStatusText(uc.status) }}
              </el-tag>
            </div>
            <div class="coupon-stamp" v-if="uc.status !== 'UNUSED'">
              {{ uc.status === 'USED' ? '已使用' : '已过期' }}
            </div>
          </div>
          <el-empty v-if="myCoupons.length === 0" description="暂无优惠券" />
        </div>
        
        <el-pagination
          v-if="myCouponTotal > 10"
          v-model:current-page="myCouponPage"
          :page-size="10"
          :total="myCouponTotal"
          layout="prev, pager, next"
          @current-change="loadMyCoupons"
          style="margin-top: 20px; text-align: center"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAvailableCoupons, receiveCoupon, getMyCoupons } from '@/api/coupon'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('available')
const availableCoupons = ref([])
const myCoupons = ref([])
const myCouponPage = ref(1)
const myCouponTotal = ref(0)
const couponStatus = ref('')

onMounted(async () => {
  await loadAvailableCoupons()
  if (userStore.isLoggedIn) {
    await loadMyCoupons()
  }
})

const loadAvailableCoupons = async () => {
  availableCoupons.value = await getAvailableCoupons() || []
}

const loadMyCoupons = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const res = await getMyCoupons({
    status: couponStatus.value,
    page: myCouponPage.value,
    size: 10
  })
  myCoupons.value = res.records || []
  myCouponTotal.value = res.total || 0
}

const handleReceive = async (couponId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await receiveCoupon(couponId)
    ElMessage.success('领取成功')
    await loadAvailableCoupons()
    await loadMyCoupons()
  } catch (e) {
    // 错误已处理
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getStatusType = (status) => {
  const map = { UNUSED: 'success', USED: 'info', EXPIRED: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' }
  return map[status] || status
}
</script>

<style lang="scss" scoped>
.coupon-center-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  
  h1 {
    font-size: 32px;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  p {
    color: #666;
    font-size: 16px;
  }
}

.filter-bar {
  margin-bottom: 20px;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  
  &.received, &.disabled {
    opacity: 0.7;
    
    .coupon-left {
      background: #ccc !important;
    }
  }
  
  .coupon-left {
    width: 120px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    
    &.amount {
      background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
    }
    
    &.discount {
      background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    }
    
    .coupon-value {
      display: flex;
      align-items: baseline;
      
      .symbol {
        font-size: 16px;
      }
      
      .number {
        font-size: 36px;
        font-weight: 700;
      }
    }
    
    .coupon-condition {
      font-size: 12px;
      margin-top: 8px;
      opacity: 0.9;
    }
  }
  
  .coupon-right {
    flex: 1;
    padding: 16px 20px;
    display: flex;
    flex-direction: column;
    
    .coupon-name {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .coupon-desc {
      font-size: 13px;
      color: #666;
      margin-bottom: 8px;
      flex: 1;
    }
    
    .coupon-time {
      font-size: 12px;
      color: #999;
      margin-bottom: 12px;
    }
  }
  
  .coupon-stamp {
    position: absolute;
    top: 50%;
    right: 20px;
    transform: translateY(-50%) rotate(-15deg);
    font-size: 24px;
    font-weight: 700;
    color: rgba(0, 0, 0, 0.1);
    border: 3px solid rgba(0, 0, 0, 0.1);
    padding: 8px 16px;
    border-radius: 8px;
  }
}
</style>
