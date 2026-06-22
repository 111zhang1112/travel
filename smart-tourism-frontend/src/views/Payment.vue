<template>
  <div class="payment-page">
    <div class="payment-container">
      <div class="payment-header">
        <h2>订单支付</h2>
      </div>

      <div class="order-info">
        <h3>订单信息</h3>
        <div class="info-item">
          <span class="label">商品名称:</span>
          <span class="value">{{ productName }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额:</span>
          <span class="value price">¥{{ amount }}</span>
        </div>
      </div>

      <div class="payment-method">
        <h3>支付方式</h3>
        <div class="method-list">
          <div 
            class="method-item" 
            :class="{ active: paymentMethod === 'ALIPAY' }"
            @click="paymentMethod = 'ALIPAY'"
          >
            <div class="method-icon">
              <svg viewBox="0 0 1024 1024" width="40" height="40">
                <path d="M1024 701.9v162.8c0 88.4-71.7 160.1-160.1 160.1H160.1C71.7 1024.8 0 953.1 0 864.7V159.1C0 70.7 71.7-1 160.1-1h703.8c88.4 0 160.1 71.7 160.1 160.1v442.7l-0.8 0.5c-73.5-55.5-202.4-142.8-366.6-203.9 24.8-51.9 44.9-108.7 58.8-169.3H565V305h187.2v-50.9H565v-89.1h-65.5c-5.8 0-10.5 4.7-10.5 10.5v78.6H301.8V305H489v84.1H353.2c-16.4 73.3-44.9 139.8-82.6 195.3-52.8-44.9-98.7-99.7-134.2-161.6l-60.9 29.3c40.1 71.7 93.9 135.2 157.3 187.2-60.9 79.6-139.8 143-229.5 183.1 22.8 22.8 53.5 44.9 78.6 60.9 89.1-44.9 166.1-108.7 226.5-187.2 60.9 78.6 137.9 142.8 226.5 187.2l0.5-0.8c-0.5 0.5-0.5 0.5 0 0l0.5 0.8c25.1-16.4 55.8-38.5 78.6-60.9-89.1-40.1-168-103.9-229.5-183.1 166.1 60.9 295 148.2 368.5 203.9z" fill="#009FE8"/>
              </svg>
            </div>
            <div class="method-name">支付宝支付</div>
            <div class="method-check">
              <span v-if="paymentMethod === 'ALIPAY'">✓</span>
            </div>
          </div>
        </div>
      </div>

      <div class="payment-actions">
        <button class="btn-cancel" @click="handleCancel">取消</button>
        <button class="btn-pay" @click="handlePay" :disabled="loading">
          {{ loading ? '处理中...' : '立即支付' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPayment, alipayPay } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const productType = ref(route.query.productType || '')
const productId = ref(route.query.productId || '')
const productName = ref(route.query.productName || '')
const amount = ref(route.query.amount || '0')

// Hotel-specific parameters
const quantity = ref(route.query.quantity ? parseInt(route.query.quantity) : 1)
const checkInDate = ref(route.query.checkInDate || null)
const checkOutDate = ref(route.query.checkOutDate || null)
const roomId = ref(route.query.roomId ? parseInt(route.query.roomId) : null)
const nights = ref(route.query.nights ? parseInt(route.query.nights) : null)

const paymentMethod = ref('ALIPAY')
const loading = ref(false)
const orderNo = ref('')

onMounted(() => {
  console.log('[Payment] 页面加载 - 接收到的query参数:', route.query)
  console.log('[Payment] 参数详情:', {
    productType: productType.value,
    productId: productId.value,
    productName: productName.value,
    amount: amount.value,
    quantity: quantity.value
  })
  
  // 验证必要参数
  if (!productType.value) {
    console.error('[Payment] 缺少productType参数')
    ElMessage.error('订单信息不完整：缺少产品类型')
    router.back()
    return
  }
  
  if (!productId.value) {
    console.error('[Payment] 缺少productId参数')
    ElMessage.error('订单信息不完整：缺少产品ID')
    router.back()
    return
  }
  
  if (!productName.value) {
    console.error('[Payment] 缺少productName参数')
    ElMessage.error('订单信息不完整：缺少产品名称')
    router.back()
    return
  }
  
  if (!amount.value || amount.value === '0') {
    console.error('[Payment] 缺少或无效的amount参数:', amount.value)
    ElMessage.error('订单信息不完整：缺少或无效的金额')
    router.back()
    return
  }
  
  // 验证数值类型
  const parsedAmount = parseFloat(amount.value)
  if (isNaN(parsedAmount) || parsedAmount <= 0) {
    console.error('[Payment] 金额格式无效:', amount.value)
    ElMessage.error('金额格式无效')
    router.back()
    return
  }
  
  const parsedProductId = parseInt(productId.value)
  if (isNaN(parsedProductId) || parsedProductId <= 0) {
    console.error('[Payment] 产品ID格式无效:', productId.value)
    ElMessage.error('产品ID格式无效')
    router.back()
    return
  }
  
  console.log('[Payment] 参数验证通过')
})

const handleCancel = () => {
  router.back()
}

const handlePay = async () => {
  console.log('[Payment] 开始支付流程')
  
  if (!paymentMethod.value) {
    console.warn('[Payment] 未选择支付方式')
    ElMessage.warning('请选择支付方式')
    return
  }

  loading.value = true
  try {
    // 创建支付订单
    const paymentData = {
      productType: productType.value,
      productId: parseInt(productId.value),
      productName: productName.value,
      amount: parseFloat(amount.value),
      paymentMethod: paymentMethod.value,
      quantity: quantity.value
    }
    
    console.log('[Payment] 支付数据:', paymentData)
    console.log('[Payment] 数据类型检查:', {
      productType: typeof paymentData.productType,
      productId: typeof paymentData.productId,
      productName: typeof paymentData.productName,
      amount: typeof paymentData.amount,
      paymentMethod: typeof paymentData.paymentMethod,
      quantity: typeof paymentData.quantity
    })
    
    // 如果是酒店订单，添加酒店特定参数
    if (productType.value === 'HOTEL' && roomId.value) {
      paymentData.roomId = roomId.value
      paymentData.checkInDate = checkInDate.value
      paymentData.checkOutDate = checkOutDate.value
      paymentData.nights = nights.value
      console.log('[Payment] 酒店订单额外参数:', {
        roomId: paymentData.roomId,
        checkInDate: paymentData.checkInDate,
        checkOutDate: paymentData.checkOutDate,
        nights: paymentData.nights
      })
    }
    
    console.log('[Payment] 调用创建支付订单API')
    const payment = await createPayment(paymentData)
    console.log('[Payment] 支付订单创建成功:', payment)

    orderNo.value = payment.orderNo
    console.log('[Payment] 订单号:', orderNo.value)

    // 发起支付宝支付
    if (paymentMethod.value === 'ALIPAY') {
      console.log('[Payment] 调用支付宝支付API')
      const formHtml = await alipayPay(orderNo.value)
      console.log('[Payment] 支付宝表单HTML长度:', formHtml.length)
      
      // 创建一个临时div来存放表单
      const div = document.createElement('div')
      div.innerHTML = formHtml
      document.body.appendChild(div)
      
      // 自动提交表单（会跳转到支付宝页面）
      const form = div.querySelector('form')
      if (form) {
        console.log('[Payment] 提交支付宝表单')
        form.submit()
      } else {
        console.error('[Payment] 未找到支付宝表单')
        ElMessage.error('支付表单生成失败')
        loading.value = false
      }
    }
  } catch (error) {
    console.error('[Payment] 支付失败:', error)
    console.error('[Payment] 错误详情:', {
      message: error.message,
      response: error.response,
      stack: error.stack
    })
    ElMessage.error(error.message || '支付失败')
    loading.value = false
  }
}
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40px 20px;
}

.payment-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.payment-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.payment-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.order-info {
  margin-bottom: 30px;
}

.order-info h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.info-item .price {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: bold;
}

.payment-method {
  margin-bottom: 30px;
}

.payment-method h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.method-list {
  display: flex;
  gap: 15px;
}

.method-item {
  flex: 1;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.method-item:hover {
  border-color: #1890ff;
}

.method-item.active {
  border-color: #1890ff;
  background: #f0f8ff;
}

.method-icon {
  margin-bottom: 10px;
}

.method-name {
  font-size: 16px;
  color: #333;
}

.method-check {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.method-item.active .method-check {
  opacity: 1;
}

.payment-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn-cancel,
.btn-pay {
  padding: 12px 40px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-pay {
  background: #1890ff;
  color: white;
}

.btn-pay:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-pay:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}
</style>
