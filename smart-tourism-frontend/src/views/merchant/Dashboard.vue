<template>
  <div class="merchant-dashboard">
    <h2 class="page-title">数据概览</h2>
    
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ overview.totalRevenue || 0 }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ overview.todayRevenue || 0 }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
              <el-radio-group v-model="orderTrendPeriod" size="small" @change="loadOrderTrend">
                <el-radio-button label="month">近12个月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="orderTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>收入趋势</span>
              <el-radio-group v-model="revenueTrendPeriod" size="small" @change="loadRevenueTrend">
                <el-radio-button label="day">按天</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="revenueTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 产品销量排名 -->
    <el-card class="ranking-card">
      <template #header>
        <span>产品销量排名 TOP 10</span>
      </template>
      <el-table :data="productRanking" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.productType === 'SCENIC' ? 'success' : 'primary'" size="small">
              {{ row.productType === 'SCENIC' ? '景点' : '酒店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="totalRevenue" label="总收入" width="120">
          <template #default="{ row }">
            ¥{{ row.totalRevenue }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getMerchantStatsOverview, getMerchantOrderTrend, getMerchantRevenueTrend, getMerchantProductRanking } from '@/api/merchant'

const overview = ref({})
const orderTrendPeriod = ref('month')
const revenueTrendPeriod = ref('day')
const productRanking = ref([])

const orderTrendChart = ref(null)
const revenueTrendChart = ref(null)
let orderChart = null
let revenueChart = null

// 加载经营概况
const loadOverview = async () => {
  try {
    const res = await getMerchantStatsOverview()
    overview.value = res || {}
  } catch (error) {
    console.error('加载经营概况失败:', error)
    ElMessage.error('加载经营概况失败')
  }
}

// 加载订单趋势
const loadOrderTrend = async () => {
  try {
    const res = await getMerchantOrderTrend()
    const data = res || []
    
    await nextTick()
    if (!orderChart && orderTrendChart.value) {
      orderChart = echarts.init(orderTrendChart.value)
    }
    
    if (orderChart) {
      orderChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.month)
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '订单数',
          type: 'line',
          data: data.map(item => item.orderCount),
          smooth: true,
          itemStyle: {
            color: '#409eff'
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载订单趋势失败:', error)
    ElMessage.error('加载订单趋势失败')
  }
}

// 加载收入趋势
const loadRevenueTrend = async () => {
  try {
    const res = await getMerchantRevenueTrend({ period: revenueTrendPeriod.value })
    const data = res || []
    
    await nextTick()
    if (!revenueChart && revenueTrendChart.value) {
      revenueChart = echarts.init(revenueTrendChart.value)
    }
    
    if (revenueChart) {
      // 根据不同周期使用不同的字段名
      const periodField = revenueTrendPeriod.value === 'day' ? 'date' : 
                         revenueTrendPeriod.value === 'week' ? 'week' : 'month'
      
      revenueChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item[periodField])
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '收入',
          type: 'bar',
          data: data.map(item => item.revenue),
          itemStyle: {
            color: '#67c23a'
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载收入趋势失败:', error)
    ElMessage.error('加载收入趋势失败')
  }
}

// 加载产品排名
const loadProductRanking = async () => {
  try {
    const res = await getMerchantProductRanking()
    productRanking.value = res || []
  } catch (error) {
    console.error('加载产品排名失败:', error)
    ElMessage.error('加载产品排名失败')
  }
}

onMounted(() => {
  loadOverview()
  loadOrderTrend()
  loadRevenueTrend()
  loadProductRanking()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    orderChart?.resize()
    revenueChart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.merchant-dashboard {
  .page-title {
    margin: 0 0 20px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
  
  .stats-cards {
    margin-bottom: 20px;
    
    .stat-card {
      :deep(.el-card__body) {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px;
      }
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;
      }
      
      .stat-content {
        flex: 1;
        
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .charts-row {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .ranking-card {
    :deep(.el-table) {
      .el-table__row {
        &:nth-child(1) .el-table__cell:first-child {
          color: #f56c6c;
          font-weight: 600;
        }
        &:nth-child(2) .el-table__cell:first-child {
          color: #e6a23c;
          font-weight: 600;
        }
        &:nth-child(3) .el-table__cell:first-child {
          color: #67c23a;
          font-weight: 600;
        }
      }
    }
  }
}
</style>
