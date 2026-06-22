<template>
  <div class="route-planner-page">
    <div class="planner-header">
      <h1>🗺️ 行程规划</h1>
      <p>选择多个景点,智能规划最优游览路线</p>
    </div>
    
    <div class="planner-content">
      <!-- 左侧:景点选择和路线信息 -->
      <div class="planner-sidebar">
        <!-- 已选景点列表 -->
        <div class="selected-scenics">
          <h3>已选景点 ({{ selectedScenics.length }})</h3>
          
          <el-empty v-if="selectedScenics.length === 0" description="请添加景点到行程" :image-size="100" />
          
          <draggable v-else v-model="selectedScenics" item-key="id" class="scenic-list">
            <template #item="{ element, index }">
              <div class="scenic-item">
                <div class="scenic-order">{{ index + 1 }}</div>
                <div class="scenic-info">
                  <div class="scenic-name">{{ element.name }}</div>
                  <div class="scenic-address">{{ element.address || '浙江省' }}</div>
                </div>
                <el-button type="danger" size="small" text @click="removeScenic(index)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </template>
          </draggable>
          
          <el-button type="primary" @click="showScenicSelector = true" style="width: 100%; margin-top: 16px">
            <el-icon><Plus /></el-icon> 添加景点
          </el-button>
        </div>
        
        <!-- 出行方式选择 -->
        <div class="travel-mode" v-if="selectedScenics.length >= 2">
          <h3>出行方式</h3>
          <el-radio-group v-model="travelMode" @change="planRoute">
            <el-radio label="driving">🚗 驾车</el-radio>
            <el-radio label="walking">🚶 步行</el-radio>
            <el-radio label="transit">🚌 公交</el-radio>
          </el-radio-group>
        </div>
        
        <!-- 路线统计 -->
        <div class="route-summary" v-if="routeData">
          <h3>路线概览</h3>
          <div class="summary-item">
            <span class="label">总距离:</span>
            <span class="value">{{ formatDistance(routeData.totalDistance) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">预计时间:</span>
            <span class="value">{{ formatDuration(routeData.totalDuration) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">途经景点:</span>
            <span class="value">{{ routeData.scenicSpots.length }} 个</span>
          </div>
        </div>
        
        <!-- 路线详情 -->
        <div class="route-details" v-if="routeData && routeData.segments">
          <h3>路线详情</h3>
          <div class="segment" v-for="(segment, index) in routeData.segments" :key="index">
            <div class="segment-header">
              <span class="segment-number">{{ index + 1 }}</span>
              <span class="segment-title">{{ segment.from.name }} → {{ segment.to.name }}</span>
            </div>
            <div class="segment-info">
              <span>{{ formatDistance(segment.distance) }}</span>
              <span>{{ formatDuration(segment.duration) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧:地图显示 -->
      <div class="planner-map">
        <div id="route-map" ref="mapContainer"></div>
      </div>
    </div>
    
    <!-- 景点选择对话框 -->
    <el-dialog v-model="showScenicSelector" title="选择景点" width="800px">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索景点名称"
        clearable
        style="margin-bottom: 16px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <div class="scenic-selector-list">
        <div
          v-for="scenic in filteredScenics"
          :key="scenic.id"
          class="scenic-selector-item"
          @click="addScenic(scenic)"
        >
          <img :src="getFirstImage(scenic.images)" :alt="scenic.name" />
          <div class="scenic-selector-info">
            <div class="scenic-selector-name">{{ scenic.name }}</div>
            <div class="scenic-selector-tags">
              <el-tag v-for="tag in parseTags(scenic.tags)" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
          </div>
          <el-button
            v-if="isSelected(scenic.id)"
            type="success"
            size="small"
            disabled
          >
            已添加
          </el-button>
          <el-button v-else type="primary" size="small">添加</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { getScenicList } from '@/api/scenic'
import { ElMessage } from 'element-plus'
import { Plus, Close, Search } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import AMapLoader from '@amap/amap-jsapi-loader'

const selectedScenics = ref([])
const travelMode = ref('driving')
const routeData = ref(null)
const showScenicSelector = ref(false)
const searchKeyword = ref('')
const allScenics = ref([])
const mapContainer = ref(null)
let map = null
let driving = null
let walking = null
let transfer = null
let markers = []  // 存储标记点
let polylines = []  // 存储路线

const filteredScenics = computed(() => {
  if (!searchKeyword.value) return allScenics.value
  return allScenics.value.filter(s =>
    s.name.includes(searchKeyword.value)
  )
})

onMounted(async () => {
  // 加载所有景点
  const res = await getScenicList({ page: 1, size: 100 })
  allScenics.value = res.records || []
  
  // 初始化地图
  await initMap()
})

// 监听景点列表变化
watch(selectedScenics, () => {
  if (selectedScenics.value.length >= 2) {
    planRoute()
  } else {
    routeData.value = null
    clearMap()
  }
}, { deep: true })

const initMap = async () => {
  try {
    const AMap = await AMapLoader.load({
      key: 'ce9e34be95d07974a6cd953d1934ce7e',
      version: '2.0',
      plugins: ['AMap.Driving', 'AMap.Walking', 'AMap.Transfer', 'AMap.Scale', 'AMap.ToolBar']
    })
    
    // 等待DOM渲染完成
    await nextTick()
    
    if (!mapContainer.value) {
      console.error('地图容器未找到')
      return
    }
    
    // 使用完整的地图配置，显示道路和地名
    map = new AMap.Map('route-map', {
      zoom: 10,
      center: [120.153576, 30.287459],
      viewMode: '2D',
      layers: [
        // 使用标准图层（包含道路、地名等）
        new AMap.TileLayer(),
        // 使用路网图层（显示道路网络）
        new AMap.TileLayer.RoadNet()
      ]
    })
    
    // 添加比例尺控件
    map.addControl(new AMap.Scale())
    
    // 添加工具条控件（缩放、平移等）
    map.addControl(new AMap.ToolBar({
      position: 'RB'  // 右下角
    }))
    
    // 初始化路线规划服务（不自动显示在地图上）
    driving = new AMap.Driving({ 
      map: null,  // 不自动显示
      panel: null,
      hideMarkers: true
    })
    walking = new AMap.Walking({ 
      map: null,
      panel: null,
      hideMarkers: true
    })
    transfer = new AMap.Transfer({ 
      map: null, 
      city: '杭州',
      panel: null,
      hideMarkers: true
    })
    
    console.log('地图初始化成功')
    
  } catch (e) {
    console.error('地图加载失败', e)
    ElMessage.error('地图加载失败')
  }
}

const addScenic = (scenic) => {
  if (isSelected(scenic.id)) {
    ElMessage.warning('该景点已添加')
    return
  }
  selectedScenics.value.push(scenic)
  showScenicSelector.value = false
  ElMessage.success('已添加到行程')
}

const removeScenic = (index) => {
  selectedScenics.value.splice(index, 1)
}

const isSelected = (id) => {
  return selectedScenics.value.some(s => s.id === id)
}

const planRoute = async () => {
  if (selectedScenics.value.length < 2) return
  
  try {
    const scenicIds = selectedScenics.value.map(s => s.id)
    const response = await fetch('/api/route/plan', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ scenicIds, travelMode: travelMode.value })
    })
    
    const result = await response.json()
    if (result.code === 200) {
      routeData.value = result.data
      
      // 检查是否使用了降级方案
      const fallbackCount = result.data.segments ? 
        result.data.segments.filter(seg => seg.fallback).length : 0
      
      if (fallbackCount > 0) {
        const totalDistance = result.data.totalDistance / 1000 // 转换为公里
        
        if (fallbackCount === result.data.segments.length) {
          // 全部使用降级方案
          if (totalDistance > 100) {
            ElMessage.info('景点距离较远，显示直线路径供参考。建议分段规划行程。')
          } else if (totalDistance > 50 && travelMode.value === 'walking') {
            ElMessage.info('步行距离较远，显示直线路径供参考。建议选择驾车或公交。')
          } else {
            ElMessage.info('部分景点可能需要特殊交通方式（如轮渡），显示直线路径供参考。')
          }
        } else {
          // 部分使用降级方案
          ElMessage.info('部分路段显示直线路径供参考，其他路段显示实际路线。')
        }
      }
      
      drawRoute()
    } else {
      ElMessage.error(result.message || '路线规划失败')
    }
  } catch (e) {
    console.error('路线规划失败', e)
    ElMessage.error('路线规划失败')
  }
}

const drawRoute = () => {
  if (!map || !routeData.value) return
  
  clearMap()
  
  const waypoints = selectedScenics.value.map(s => [s.longitude, s.latitude])
  
  if (waypoints.length < 2) return
  
  // 检查是否所有路段都使用了降级方案
  const allFallback = routeData.value.segments && 
    routeData.value.segments.every(seg => seg.fallback)
  
  if (allFallback) {
    // 全部使用降级方案，直接绘制直线
    console.log('使用降级方案：绘制直线连接')
    const color = travelMode.value === 'driving' ? '#FF5722' : 
                  travelMode.value === 'walking' ? '#4CAF50' : '#9C27B0'
    drawStraightLines(color)
    addMarkers()
    return
  }
  
  const start = waypoints[0]
  const end = waypoints[waypoints.length - 1]
  const midPoints = waypoints.slice(1, -1).map(p => ({ lnglat: p }))
  
  // 根据出行方式选择不同的路线规划服务和颜色
  if (travelMode.value === 'driving') {
    driving.search(start, end, { waypoints: midPoints }, (status, result) => {
      if (status === 'complete') {
        console.log('驾车路线规划成功', result)
        drawPolyline(result.routes[0], '#FF5722', '驾车')  // 橙红色
        addMarkers()
      } else {
        console.error('驾车路线规划失败', status, result)
        ElMessage.warning('路线规划失败，显示直线路径供参考')
        drawStraightLines('#FF5722')  // 降级方案：绘制直线
        addMarkers()
      }
    })
  } else if (travelMode.value === 'walking') {
    // 步行只支持两点之间,需要分段规划
    drawWalkingRoute(waypoints)
  } else if (travelMode.value === 'transit') {
    // 公交路线
    transfer.search(start, end, (status, result) => {
      if (status === 'complete') {
        console.log('公交路线规划成功', result)
        drawTransitRoute(result.plans[0], '#9C27B0')  // 紫色
        addMarkers()
      } else {
        console.error('公交路线规划失败', status, result)
        ElMessage.warning('路线规划失败，显示直线路径供参考')
        drawStraightLines('#9C27B0')  // 降级方案：绘制直线
        addMarkers()
      }
    })
  }
}

const drawWalkingRoute = async (waypoints) => {
  let allPaths = []
  let successCount = 0
  
  for (let i = 0; i < waypoints.length - 1; i++) {
    await new Promise((resolve) => {
      walking.search(waypoints[i], waypoints[i + 1], (status, result) => {
        if (status === 'complete') {
          successCount++
          console.log(`步行路线段 ${i + 1} 规划成功`, result)
          if (result.routes && result.routes[0]) {
            allPaths.push(result.routes[0])
          }
        } else {
          console.error(`步行路线段 ${i + 1} 规划失败`, status, result)
        }
        resolve()
      })
    })
  }
  
  if (successCount === 0) {
    ElMessage.warning('路线规划失败，显示直线路径供参考')
    drawStraightLines('#4CAF50')  // 降级方案：绘制直线
  } else if (successCount < waypoints.length - 1) {
    // 部分成功，绘制成功的路段，失败的用直线连接
    allPaths.forEach(route => {
      drawPolyline(route, '#4CAF50', '步行')  // 绿色
    })
    ElMessage.info('部分路段距离较远，显示直线路径供参考')
  } else {
    // 全部成功
    allPaths.forEach(route => {
      drawPolyline(route, '#4CAF50', '步行')  // 绿色
    })
  }
  
  addMarkers()
}

// 绘制路线（驾车/步行）
const drawPolyline = (route, color, label) => {
  if (!route || !route.steps) return
  
  const path = []
  route.steps.forEach(step => {
    if (step.path) {
      step.path.forEach(point => {
        path.push([point.lng, point.lat])
      })
    }
  })
  
  if (path.length > 0) {
    const polyline = new AMap.Polyline({
      path: path,
      strokeColor: color,
      strokeWeight: 6,
      strokeOpacity: 0.9,
      lineJoin: 'round',
      lineCap: 'round',
      zIndex: 50
    })
    
    map.add(polyline)
    polylines.push(polyline)
    
    console.log(`${label}路线已绘制，颜色：${color}，点数：${path.length}`)
  }
}

// 绘制公交路线
const drawTransitRoute = (plan, color) => {
  if (!plan || !plan.segments) return
  
  plan.segments.forEach(segment => {
    if (segment.transit && segment.transit.path) {
      const path = segment.transit.path.map(point => [point.lng, point.lat])
      
      const polyline = new AMap.Polyline({
        path: path,
        strokeColor: color,
        strokeWeight: 6,
        strokeOpacity: 0.9,
        lineJoin: 'round',
        lineCap: 'round',
        zIndex: 50,
        strokeStyle: 'dashed'  // 虚线表示公交
      })
      
      map.add(polyline)
      polylines.push(polyline)
    }
  })
  
  console.log(`公交路线已绘制，颜色：${color}`)
}

// 降级方案：绘制直线连接各景点
const drawStraightLines = (color) => {
  const path = selectedScenics.value.map(s => [s.longitude, s.latitude])
  
  const polyline = new AMap.Polyline({
    path: path,
    strokeColor: color,
    strokeWeight: 4,
    strokeOpacity: 0.7,
    lineJoin: 'round',
    lineCap: 'round',
    zIndex: 50,
    strokeStyle: 'dashed'  // 虚线表示直线连接
  })
  
  map.add(polyline)
  polylines.push(polyline)
  
  console.log(`直线路线已绘制（降级方案），颜色：${color}`)
}

const addMarkers = () => {
  // 清除旧标记
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  // 添加新标记
  selectedScenics.value.forEach((scenic, index) => {
    const marker = new AMap.Marker({
      position: [scenic.longitude, scenic.latitude],
      label: {
        content: `<div style="background: #409eff; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold;">${index + 1}</div>`,
        offset: new AMap.Pixel(0, -30)
      }
    })
    map.add(marker)
    markers.push(marker)
  })
  
  // 调整视野以显示所有景点
  if (selectedScenics.value.length > 0) {
    const bounds = new AMap.Bounds(
      selectedScenics.value.map(s => [s.longitude, s.latitude])
    )
    map.setBounds(bounds)
  }
}

const clearMap = () => {
  if (!map) return
  
  // 清除标记
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  // 清除路线
  polylines.forEach(polyline => map.remove(polyline))
  polylines = []
  
  // 清除路线规划服务的缓存
  if (driving) {
    driving.clear()
  }
  if (walking) {
    walking.clear()
  }
  if (transfer) {
    transfer.clear()
  }
}

const formatDistance = (distance) => {
  if (!distance) return '-'
  const d = parseFloat(distance)
  if (d >= 1000) {
    return (d / 1000).toFixed(1) + ' 公里'
  }
  return d.toFixed(0) + ' 米'
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  const d = parseInt(duration)
  const hours = Math.floor(d / 3600)
  const minutes = Math.floor((d % 3600) / 60)
  
  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分钟`
  }
  return `${minutes} 分钟`
}

const getFirstImage = (images) => {
  try {
    const arr = JSON.parse(images)
    return arr[0] || 'https://via.placeholder.com/100'
  } catch {
    return 'https://via.placeholder.com/100'
  }
}

const parseTags = (tags) => {
  try {
    return JSON.parse(tags) || []
  } catch {
    return []
  }
}
</script>

<style lang="scss" scoped>
.route-planner-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.planner-header {
  text-align: center;
  margin-bottom: 24px;
  
  h1 {
    font-size: 32px;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
    font-size: 16px;
  }
}

.planner-content {
  display: flex;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.planner-sidebar {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.selected-scenics,
.travel-mode,
.route-summary,
.route-details {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  h3 {
    font-size: 16px;
    margin-bottom: 16px;
    color: #333;
  }
}

.scenic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scenic-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: move;
  
  &:hover {
    background: #f0f0f0;
  }
  
  .scenic-order {
    width: 32px;
    height: 32px;
    background: #409eff;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    flex-shrink: 0;
  }
  
  .scenic-info {
    flex: 1;
    min-width: 0;
    
    .scenic-name {
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .scenic-address {
      font-size: 12px;
      color: #999;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .label {
    color: #666;
  }
  
  .value {
    font-weight: 500;
    color: #409eff;
  }
}

.segment {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 12px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .segment-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .segment-number {
      width: 24px;
      height: 24px;
      background: #409eff;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: bold;
      flex-shrink: 0;
    }
    
    .segment-title {
      font-weight: 500;
      font-size: 14px;
    }
  }
  
  .segment-info {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #666;
    padding-left: 32px;
  }
}

.planner-map {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  #route-map {
    width: 100%;
    height: 800px;
  }
}

.scenic-selector-list {
  max-height: 500px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scenic-selector-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409eff;
    background: #f0f7ff;
  }
  
  img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
  }
  
  .scenic-selector-info {
    flex: 1;
    min-width: 0;
    
    .scenic-selector-name {
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .scenic-selector-tags {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }
  }
}
</style>
