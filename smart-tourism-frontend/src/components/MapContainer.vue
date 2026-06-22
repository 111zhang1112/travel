<template>
  <div ref="mapContainer" class="map-container"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getScenicList } from '@/api/scenic'

const props = defineProps({
  latitude: {
    type: Number,
    required: true
  },
  longitude: {
    type: Number,
    required: true
  },
  markers: {
    type: Array,
    default: () => []
  },
  zoom: {
    type: Number,
    default: 15
  },
  title: {
    type: String,
    default: ''
  },
  showNearby: {
    type: Boolean,
    default: true  // 默认显示周边景点
  },
  currentScenicId: {
    type: Number,
    default: null  // 当前景点ID，用于高亮显示
  }
})

const emit = defineEmits(['markerClick'])

const mapContainer = ref()
let map = null
let AMap = null
let markerInstances = []
let allScenics = []  // 存储所有景点数据

onMounted(async () => {
  try {
    AMap = await AMapLoader.load({
      key: 'ce9e34be95d07974a6cd953d1934ce7e', // 高德地图 API Key
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.PlaceSearch', 'AMap.Scale', 'AMap.ToolBar']
    })
    
    // 如果需要显示周边景点，先加载所有景点数据
    if (props.showNearby) {
      await loadAllScenics()
    }
    
    initMap()
  } catch (e) {
    console.error('Failed to load AMap', e)
  }
})

onUnmounted(() => {
  map?.destroy()
})

// 加载所有景点数据
const loadAllScenics = async () => {
  try {
    const res = await getScenicList({ page: 1, size: 1000, status: 1 })
    allScenics = res.records || []
  } catch (e) {
    console.error('Failed to load scenics', e)
  }
}

const initMap = () => {
  map = new AMap.Map(mapContainer.value, {
    zoom: props.zoom,
    center: [props.longitude, props.latitude],
    viewMode: '2D',
    layers: [
      // 使用标准图层
      new AMap.TileLayer(),
      // 使用路网图层
      new AMap.TileLayer.RoadNet()
    ]
  })
  
  // 添加比例尺控件
  map.addControl(new AMap.Scale())
  
  // 添加工具条控件（缩放、平移等）
  map.addControl(new AMap.ToolBar({
    position: 'RB'  // 右下角
  }))
  
  // 如果有传入的标记，添加它们
  if (props.markers.length > 0) {
    props.markers.forEach(marker => {
      const isCurrent = marker.id === props.currentScenicId
      addMarker(marker.latitude, marker.longitude, marker.title, marker, isCurrent)
    })
  } else if (props.showNearby && allScenics.length > 0) {
    // 显示所有有坐标的景点
    allScenics.forEach(scenic => {
      if (scenic.latitude && scenic.longitude) {
        const isCurrent = scenic.id === props.currentScenicId
        addMarker(
          Number(scenic.latitude), 
          Number(scenic.longitude), 
          scenic.name, 
          {
            id: scenic.id,
            title: scenic.name,
            ticketPrice: scenic.ticketPrice,
            openingHours: scenic.openingHours,
            rating: scenic.rating,
            address: scenic.address
          },
          isCurrent
        )
      }
    })
  } else {
    // 如果没有额外标记，添加主标记
    addMarker(props.latitude, props.longitude, props.title, null, true)
  }
  
  // 监听地图缩放事件，可以根据缩放级别调整显示的标记
  map.on('zoomend', () => {
    const zoom = map.getZoom()
    // 可以根据缩放级别过滤显示的标记
    console.log('Current zoom level:', zoom)
  })
}

const addMarker = (lat, lng, title, data = null, isCurrent = false) => {
  // 根据是否为当前景点设置不同的标记样式
  const markerIcon = isCurrent 
    ? 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png'  // 红色标记（当前景点）
    : 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png'  // 蓝色标记（其他景点）
  
  const marker = new AMap.Marker({
    position: [lng, lat],
    title: title,
    icon: markerIcon,
    label: {
      content: `<div style="background: ${isCurrent ? '#ff4d4f' : 'white'}; color: ${isCurrent ? 'white' : '#333'}; padding: 4px 8px; border-radius: 4px; box-shadow: 0 2px 6px rgba(0,0,0,0.2); font-size: 12px; white-space: nowrap; font-weight: ${isCurrent ? '600' : '400'};">${title}</div>`,
      offset: new AMap.Pixel(0, -35),
      direction: 'top'
    }
  })
  
  // 创建信息窗口内容
  const infoContent = `
    <div style="padding: 12px; min-width: 200px;">
      <h3 style="margin: 0 0 8px 0; font-size: 16px; color: #333;">${title}${isCurrent ? ' <span style="color: #ff4d4f; font-size: 12px;">(当前)</span>' : ''}</h3>
      ${data ? `
        <p style="margin: 4px 0; color: #666; font-size: 13px;">
          <span style="color: #999;">📍 位置：</span>${data.address || '浙江省'}
        </p>
        ${data.ticketPrice !== undefined ? `
          <p style="margin: 4px 0; color: #666; font-size: 13px;">
            <span style="color: #999;">💰 门票：</span>
            <span style="color: #f56c6c; font-weight: 600;">¥${data.ticketPrice}</span>
          </p>
        ` : ''}
        ${data.openingHours ? `
          <p style="margin: 4px 0; color: #666; font-size: 13px;">
            <span style="color: #999;">🕐 开放时间：</span>${data.openingHours}
          </p>
        ` : ''}
        ${data.rating ? `
          <p style="margin: 4px 0; color: #666; font-size: 13px;">
            <span style="color: #999;">⭐ 评分：</span>${data.rating} 分
          </p>
        ` : ''}
        ${!isCurrent && data.id ? `
          <p style="margin: 8px 0 0 0;">
            <a href="/scenic/${data.id}" style="color: #409eff; text-decoration: none; font-size: 13px;">查看详情 →</a>
          </p>
        ` : ''}
      ` : `
        <p style="margin: 4px 0; color: #666; font-size: 13px;">
          点击查看详情
        </p>
      `}
    </div>
  `
  
  marker.on('click', () => {
    // 显示信息窗口
    const infoWindow = new AMap.InfoWindow({
      content: infoContent,
      offset: new AMap.Pixel(0, -30)
    })
    infoWindow.open(map, marker.getPosition())
    
    if (data) {
      emit('markerClick', data)
    }
  })
  
  map.add(marker)
  markerInstances.push(marker)
}

// 监听坐标变化
watch(() => [props.latitude, props.longitude], ([lat, lng]) => {
  if (map) {
    map.setCenter([lng, lat])
  }
})

// 监听标记变化
watch(() => props.markers, (newMarkers) => {
  if (map && AMap) {
    // 清除旧标记
    markerInstances.forEach(m => map.remove(m))
    markerInstances = []
    
    // 添加新标记
    if (newMarkers.length > 0) {
      newMarkers.forEach(marker => {
        const isCurrent = marker.id === props.currentScenicId
        addMarker(marker.latitude, marker.longitude, marker.title, marker, isCurrent)
      })
    } else {
      addMarker(props.latitude, props.longitude, props.title, null, true)
    }
  }
}, { deep: true })

// 暴露方法
defineExpose({
  setCenter: (lat, lng) => {
    map?.setCenter([lng, lat])
  },
  setZoom: (zoom) => {
    map?.setZoom(zoom)
  }
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  min-height: 300px;
  border-radius: 8px;
  overflow: hidden;
}
</style>
