<template>
  <div class="dashboard">
    <h2 class="page-title">数据看板</h2>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <el-icon><Location /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.scenicCount }}</div>
          <div class="stat-label">景点数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
          <div class="stat-sub">今日: {{ stats.todayOrderCount }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.totalRevenue }}</div>
          <div class="stat-label">总收入</div>
          <div class="stat-sub">今日: ¥{{ stats.todayRevenue }}</div>
        </div>
      </div>
    </div>
    
    <!-- 第一行图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>订单趋势</h3>
        <div ref="lineChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>订单类型分布</h3>
        <div ref="pieChartRef" class="chart"></div>
      </div>
    </div>
    
    <!-- 第二行图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>收入趋势</h3>
          <el-radio-group v-model="revenuePeriod" size="small" @change="loadRevenueTrend">
            <el-radio-button label="day">最近7天</el-radio-button>
            <el-radio-button label="week">最近12周</el-radio-button>
            <el-radio-button label="month">最近12月</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="revenueChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>订单状态分布</h3>
        <div ref="statusChartRef" class="chart"></div>
      </div>
    </div>
    
    <!-- 第三行图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>用户增长趋势</h3>
        <div ref="userGrowthChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>平台综合评分</h3>
        <div ref="radarChartRef" class="chart"></div>
      </div>
    </div>
    
    <!-- 第四行：热门排行 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>热门景点 TOP10</h3>
        <div ref="topScenicChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>热门酒店 TOP10</h3>
        <div ref="topHotelChartRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { 
  getDashboardStats, 
  getOrderTrend, 
  getOrderTypeDistribution,
  getUserGrowthTrend,
  getRevenueTrend,
  getTopScenic,
  getTopHotels,
  getOrderStatusDistribution,
  getComprehensiveStats
} from '@/api/admin'

const stats = ref({
  userCount: 0,
  scenicCount: 0,
  orderCount: 0,
  totalRevenue: 0,
  todayOrderCount: 0,
  todayRevenue: 0
})

const lineChartRef = ref()
const pieChartRef = ref()
const revenueChartRef = ref()
const statusChartRef = ref()
const userGrowthChartRef = ref()
const radarChartRef = ref()
const topScenicChartRef = ref()
const topHotelChartRef = ref()

let lineChart = null
let pieChart = null
let revenueChart = null
let statusChart = null
let userGrowthChart = null
let radarChart = null
let topScenicChart = null
let topHotelChart = null

const revenuePeriod = ref('month')

onMounted(async () => {
  // 加载统计数据
  await loadStats()
  
  // 初始化所有图表
  await initAllCharts()
})

onUnmounted(() => {
  lineChart?.dispose()
  pieChart?.dispose()
  revenueChart?.dispose()
  statusChart?.dispose()
  userGrowthChart?.dispose()
  radarChart?.dispose()
  topScenicChart?.dispose()
  topHotelChart?.dispose()
})

const loadStats = async () => {
  try {
    const data = await getDashboardStats()
    stats.value = data
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const initAllCharts = async () => {
  await initLineChart()
  await initPieChart()
  await initRevenueChart()
  await initStatusChart()
  await initUserGrowthChart()
  await initRadarChart()
  await initTopScenicChart()
  await initTopHotelChart()
}

const initLineChart = async () => {
  try {
    const trendData = await getOrderTrend()
    const months = trendData && trendData.length > 0 ? trendData.map(item => item.month) : []
    const counts = trendData && trendData.length > 0 ? trendData.map(item => item.count) : []
    
    lineChart = echarts.init(lineChartRef.value)
    const option = {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: months },
      yAxis: { type: 'value' },
      series: [{
        name: '订单数',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        data: counts
      }]
    }
    lineChart.setOption(option)
  } catch (e) {
    console.error('加载订单趋势失败', e)
  }
}

const initPieChart = async () => {
  try {
    const typeData = await getOrderTypeDistribution()
    const typeNameMap = {
      'SCENIC': '景点门票',
      'HOTEL': '酒店预订',
      'PACKAGE': '套餐产品'
    }
    const colorMap = {
      'SCENIC': '#667eea',
      'HOTEL': '#f093fb',
      'PACKAGE': '#4facfe'
    }
    const pieData = typeData && typeData.length > 0 
      ? typeData.map(item => ({
          value: item.count,
          name: typeNameMap[item.type] || item.type,
          itemStyle: { color: colorMap[item.type] || '#999' }
        }))
      : []
    
    pieChart = echarts.init(pieChartRef.value)
    const option = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        data: pieData
      }]
    }
    pieChart.setOption(option)
  } catch (e) {
    console.error('加载订单类型分布失败', e)
  }
}

const initRevenueChart = async () => {
  await loadRevenueTrend()
}

const loadRevenueTrend = async () => {
  try {
    const data = await getRevenueTrend(revenuePeriod.value)
    const labels = data && data.length > 0 ? data.map(item => item.date || item.week || item.month) : []
    const revenues = data && data.length > 0 ? data.map(item => item.revenue) : []
    
    if (!revenueChart) {
      revenueChart = echarts.init(revenueChartRef.value)
    }
    
    const option = {
      tooltip: { trigger: 'axis', formatter: '{b}<br/>收入: ¥{c}' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [{
        name: '收入',
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#43e97b' },
            { offset: 1, color: '#38f9d7' }
          ])
        },
        data: revenues
      }]
    }
    revenueChart.setOption(option)
  } catch (e) {
    console.error('加载收入趋势失败', e)
  }
}

const initStatusChart = async () => {
  try {
    const data = await getOrderStatusDistribution()
    const statusNameMap = {
      'PENDING': '待支付',
      'PAID': '已支付',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消',
      'REFUNDED': '已退款'
    }
    const colorMap = {
      'PENDING': '#E6A23C',
      'PAID': '#409EFF',
      'COMPLETED': '#67C23A',
      'CANCELLED': '#909399',
      'REFUNDED': '#F56C6C'
    }
    const statusData = data && data.length > 0
      ? data.map(item => ({
          value: item.count,
          name: statusNameMap[item.status] || item.status,
          itemStyle: { color: colorMap[item.status] || '#999' }
        }))
      : []
    
    statusChart = echarts.init(statusChartRef.value)
    const option = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: statusData,
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
      }]
    }
    statusChart.setOption(option)
  } catch (e) {
    console.error('加载订单状态分布失败', e)
  }
}

const initUserGrowthChart = async () => {
  try {
    const data = await getUserGrowthTrend()
    const months = data && data.length > 0 ? data.map(item => item.month) : []
    const counts = data && data.length > 0 ? data.map(item => item.count) : []
    
    userGrowthChart = echarts.init(userGrowthChartRef.value)
    const option = {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: [{
        name: '新增用户',
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        data: counts
      }]
    }
    userGrowthChart.setOption(option)
  } catch (e) {
    console.error('加载用户增长趋势失败', e)
  }
}

const initRadarChart = async () => {
  try {
    const data = await getComprehensiveStats()
    
    radarChart = echarts.init(radarChartRef.value)
    const option = {
      tooltip: {},
      radar: {
        indicator: [
          { name: '用户活跃度', max: 100 },
          { name: '内容丰富度', max: 100 },
          { name: '用户满意度', max: 100 },
          { name: '订单转化率', max: 100 },
          { name: '平台活跃度', max: 100 }
        ],
        radius: '60%'
      },
      series: [{
        type: 'radar',
        data: [{
          value: [
            data.userActivity || 0,
            data.contentRichness || 0,
            data.userSatisfaction || 0,
            data.conversionRate || 0,
            data.platformActivity || 0
          ],
          name: '平台指标',
          areaStyle: {
            color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
            ])
          },
          lineStyle: { color: '#667eea', width: 2 },
          itemStyle: { color: '#667eea' }
        }]
      }]
    }
    radarChart.setOption(option)
  } catch (e) {
    console.error('加载综合统计失败', e)
  }
}

const initTopScenicChart = async () => {
  try {
    const data = await getTopScenic()
    const names = data && data.length > 0 ? data.map(item => item.name) : []
    const viewCounts = data && data.length > 0 ? data.map(item => item.view_count) : []
    
    topScenicChart = echarts.init(topScenicChartRef.value)
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: names.reverse(), axisLabel: { interval: 0 } },
      series: [{
        name: '浏览量',
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#f093fb' },
            { offset: 1, color: '#f5576c' }
          ])
        },
        data: viewCounts.reverse()
      }]
    }
    topScenicChart.setOption(option)
  } catch (e) {
    console.error('加载热门景点失败', e)
  }
}

const initTopHotelChart = async () => {
  try {
    const data = await getTopHotels()
    const names = data && data.length > 0 ? data.map(item => item.name) : []
    const orderCounts = data && data.length > 0 ? data.map(item => item.order_count) : []
    
    topHotelChart = echarts.init(topHotelChartRef.value)
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: names.reverse(), axisLabel: { interval: 0 } },
      series: [{
        name: '订单数',
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#4facfe' },
            { offset: 1, color: '#00f2fe' }
          ])
        },
        data: orderCounts.reverse()
      }]
    }
    topHotelChart.setOption(option)
  } catch (e) {
    console.error('加载热门酒店失败', e)
  }
}

// 响应窗口大小变化
window.addEventListener('resize', () => {
  lineChart?.resize()
  pieChart?.resize()
  revenueChart?.resize()
  statusChart?.resize()
  userGrowthChart?.resize()
  radarChart?.resize()
  topScenicChart?.resize()
  topHotelChart?.resize()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #333;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 24px;
  }

  .stat-info {
    flex: 1;
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #333;
    }
    
    .stat-label {
      font-size: 14px;
      color: #999;
      margin-top: 4px;
    }
    
    .stat-sub {
      font-size: 12px;
      color: #67C23A;
      margin-top: 4px;
    }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
  }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      margin-bottom: 0;
    }
  }

  .chart {
    height: 300px;
  }
}
</style>
