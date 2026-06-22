<template>
  <div class="payment-result-page">
    <div class="result-container">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>正在查询支付结果...</p>
      </div>

      <div v-else-if="status === 'SUCCESS'" class="result-success">
        <div class="icon">✓</div>
        <h2>支付成功!</h2>
        <p class="amount">¥{{ amount }}</p>
        <p class="tip">您的订单已支付成功</p>
        <p class="countdown-tip">{{ countdown }} 秒后自动跳转到景点列表...</p>
        
        <div class="actions">
          <button class="btn-primary" @click="goToScenic">立即浏览景点</button>
          <button class="btn-default" @click="goToOrders">查看订单</button>
          <button class="btn-default" @click="goToHome">返回首页</button>
        </div>
      </div>

      <div v-else class="result-failed">
        <div class="icon">×</div>
        <h2>支付失败</h2>
        <p class="tip">{{ errorMessage || '支付未完成或已取消' }}</p>
        <div class="actions">
          <button class="btn-primary" @click="retry">重新支付</button>
          <button class="btn-default" @click="goToHome">返回首页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { queryPaymentStatus } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const status = ref('')
const amount = ref('0')
const errorMessage = ref('')
const orderNo = ref('')
const countdown = ref(3)

onMounted(async () => {
  // 从URL参数获取订单号
  orderNo.value = route.query.out_trade_no || route.query.orderNo
  
  if (!orderNo.value) {
    loading.value = false
    status.value = 'FAILED'
    errorMessage.value = '订单号不存在'
    return
  }

  try {
    // 查询支付状态
    const payment = await queryPaymentStatus(orderNo.value)
    status.value = payment.status
    amount.value = payment.amount
    
    if (payment.status === 'SUCCESS') {
      // 支付成功，3秒后自动跳转到景点列表
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          router.push('/scenic')
        }
      }, 1000)
    } else {
      errorMessage.value = '支付未完成'
    }
  } catch (error) {
    status.value = 'FAILED'
    errorMessage.value = error.message || '查询支付状态失败'
  } finally {
    loading.value = false
  }
})

const goToScenic = () => {
  router.push('/scenic')
}

const goToOrders = () => {
  router.push('/user')
}

const goToHome = () => {
  router.push('/')
}

const retry = () => {
  router.back()
}
</script>

<style scoped>
.payment-result-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.result-container {
  background: white;
  border-radius: 8px;
  padding: 60px 40px;
  text-align: center;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading {
  padding: 40px 0;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.result-success .icon,
.result-failed .icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
  margin: 0 auto 20px;
  font-weight: bold;
}

.result-success .icon {
  background: #f6ffed;
  color: #52c41a;
  border: 3px solid #52c41a;
}

.result-failed .icon {
  background: #fff2f0;
  color: #ff4d4f;
  border: 3px solid #ff4d4f;
}

h2 {
  font-size: 28px;
  margin-bottom: 15px;
  color: #333;
}

.amount {
  font-size: 36px;
  color: #ff4d4f;
  font-weight: bold;
  margin: 20px 0;
}

.tip {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.countdown-tip {
  font-size: 14px;
  color: #1890ff;
  margin-bottom: 20px;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary,
.btn-default {
  padding: 12px 30px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-default {
  background: #f5f5f5;
  color: #666;
}

.btn-default:hover {
  background: #e8e8e8;
}
</style>
