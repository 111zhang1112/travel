<template>
  <div class="share-statistics">
    <el-card class="header-card">
      <h2>分享统计</h2>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="时间范围">
          <el-select v-model="queryForm.days" @change="loadData">
            <el-option label="最近7天" :value="7" />
            <el-option label="最近30天" :value="30" />
            <el-option label="最近90天" :value="90" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 总体统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409EFF">
            <el-icon :size="30"><Share /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overallStats.totalShares || 0 }}</div>
            <div class="stat-label">总分享次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon :size="30"><View /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overallStats.totalViews || 0 }}</div>
            <div class="stat-label">总访问次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overallStats.totalUsers || 0 }}</div>
            <div class="stat-label">分享用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #F56C6C">
            <el-icon :size="30"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ conversionRate }}%</div>
            <div class="stat-label">访问转化率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 平台分享统计 -->
    <el-card class="chart-card">
      <template #header>
        <span>平台分享分布</span>
      </template>
      <div ref="platformChartRef" style="height: 300px"></div>
    </el-card>

    <!-- 内容分享排行榜 -->
    <el-card class="ranking-card">
      <template #header>
        <div class="ranking-header">
          <span>内容分享排行榜</span>
          <el-radio-group v-model="contentType" @change="loadRanking">
            <el-radio-button label="scenic">景点</el-radio-button>
            <el-radio-button label="hotel">酒店</el-radio-button>
            <el-radio-button label="guide">攻略</el-radio-button>
            <el-radio-button label="route">路线</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="contentRanking" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="contentName" label="内容名称" min-width="200" />
        <el-table-column prop="shareCount" label="分享次数" width="120" align="center" />
        <el-table-column prop="viewCount" label="访问次数" width="120" align="center" />
        <el-table-column label="转化率" width="120" align="center">
          <template #default="{ row }">
            {{ ((row.viewCount / row.shareCount) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 用户分享排行榜 -->
    <el-card class="ranking-card">
      <template #header>
        <span>用户分享排行榜</span>
      </template>
      <el-table :data="userRanking" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="shareCount" label="分享次数" width="120" align="center" />
        <el-table-column prop="viewCount" label="获得访问" width="120" align="center" />
        <el-table-column prop="totalPoints" label="获得积分" width="120" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Share, View, User, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getShareStatistics, getShareRanking, getPlatformStatistics } from '@/api/share'

const queryForm = ref({
  days: 7
})

const contentType = ref('scenic')
const overallStats = ref({})
const platformStats = ref([])
const contentRanking = ref([])
const userRanking = ref([])
const platformChartRef = ref(null)
let platformChart = null

const conversionRate = computed(() => {
  if (!overallStats.value.totalShares || overallStats.value.totalShares === 0) {
    return 0
  }
  return ((overallStats.value.totalViews / overallStats.value.totalShares) * 100).toFixed(1)
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  await Promise.all([
    loadOverallStats(),
    loadPlatformStats(),
    loadRanking()
  ])
}

const loadOverallStats = async () => {
  try {
    const { data } = await getShareStatistics(queryForm.value.days)
    // 处理空数据情况,设置默认值
    overallStats.value = {
      totalShares: data?.totalShares || 0,
      totalUsers: data?.totalUsers || 0,
      totalViews: data?.totalViews || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 设置默认值而不是显示错误
    overallStats.value = {
      totalShares: 0,
      totalUsers: 0,
      totalViews: 0
    }
  }
}

const loadPlatformStats = async () => {
  try {
    const { data } = await getPlatformStatistics(queryForm.value.days)
    platformStats.value = data || []
    await nextTick()
    renderPlatformChart()
  } catch (error) {
    console.error('加载平台统计失败:', error)
    // 设置空数组而不是显示错误
    platformStats.value = []
    await nextTick()
    renderPlatformChart()
  }
}

const loadRanking = async () => {
  try {
    const [contentRes, userRes] = await Promise.all([
      getShareRanking({
        contentType: contentType.value,
        days: queryForm.value.days,
        limit: 10
      }),
      getShareRanking({
        type: 'user',
        days: queryForm.value.days,
        limit: 10
      })
    ])
    contentRanking.value = contentRes.data?.ranking || []
    userRanking.value = userRes.data || []
  } catch (error) {
    console.error('加载排行榜失败:', error)
    // 设置空数组而不是显示错误
    contentRanking.value = []
    userRanking.value = []
  }
}

const renderPlatformChart = () => {
  if (!platformChartRef.value) return

  if (platformChart) {
    platformChart.dispose()
  }

  platformChart = echarts.init(platformChartRef.value)

  const platformNames = {
    wechat: '微信',
    weibo: '微博',
    qq: 'QQ',
    link: '复制链接'
  }

  // 处理空数据情况
  const chartData = platformStats.value && platformStats.value.length > 0
    ? platformStats.value.map(item => ({
        name: platformNames[item.platform] || item.platform,
        value: item.count
      }))
    : [{ name: '暂无数据', value: 1 }]

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: platformStats.value && platformStats.value.length > 0 
        ? '{b}: {c} ({d}%)' 
        : '暂无分享数据'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '分享平台',
        type: 'pie',
        radius: '50%',
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        // 空数据时显示灰色
        itemStyle: platformStats.value && platformStats.value.length > 0 
          ? {} 
          : { color: '#e0e0e0' }
      }
    ]
  }

  platformChart.setOption(option)
}
</script>

<style scoped>
.share-statistics {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #303133;
}

.query-form {
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-card {
  margin-bottom: 20px;
}

.ranking-card {
  margin-bottom: 20px;
}

.ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
