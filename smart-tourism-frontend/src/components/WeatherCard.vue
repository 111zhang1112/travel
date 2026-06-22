<template>
  <div class="weather-card" v-if="weatherData">
    <div class="weather-header">
      <div class="weather-icon">
        <span>{{ getWeatherIcon(todayWeather.dayweather) }}</span>
      </div>
      <div class="weather-info">
        <div class="weather-temp">
          <span class="temp-high">{{ todayWeather.daytemp }}°</span>
          <span class="temp-divider">/</span>
          <span class="temp-low">{{ todayWeather.nighttemp }}°</span>
        </div>
        <div class="weather-desc">{{ todayWeather.dayweather }}</div>
        <div class="weather-location">
          <el-icon><Location /></el-icon>
          {{ weatherData.city }}
        </div>
      </div>
    </div>
    
    <div class="weather-details">
      <div class="detail-item">
        <el-icon><Sunny /></el-icon>
        <span>{{ todayWeather.daywind }}风 {{ todayWeather.daypower }}级</span>
      </div>
      <div class="detail-item">
        <el-icon><Calendar /></el-icon>
        <span>{{ todayWeather.week }}</span>
      </div>
    </div>
    
    <!-- 未来几天天气 -->
    <div class="weather-forecast" v-if="weatherData.forecasts && weatherData.forecasts.length > 1">
      <div class="forecast-title">未来天气</div>
      <div class="forecast-list">
        <div 
          class="forecast-item" 
          v-for="(forecast, index) in weatherData.forecasts.slice(1, 4)" 
          :key="index"
        >
          <div class="forecast-date">{{ formatDate(forecast.date) }}</div>
          <div class="forecast-icon">{{ getWeatherIcon(forecast.dayweather) }}</div>
          <div class="forecast-weather">{{ forecast.dayweather }}</div>
          <div class="forecast-temp">
            <span class="temp-high">{{ forecast.daytemp }}°</span>
            <span class="temp-low">{{ forecast.nighttemp }}°</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="weather-tips" v-if="!weatherData.fallback">
      <el-icon><InfoFilled /></el-icon>
      <span>数据更新时间：{{ formatTime(weatherData.reporttime) }}</span>
    </div>
  </div>
  
  <div class="weather-card weather-loading" v-else-if="loading">
    <el-skeleton :rows="3" animated />
  </div>
  
  <div class="weather-card weather-error" v-else-if="error">
    <el-empty description="天气信息加载失败" :image-size="60" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Location, Sunny, Calendar, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  latitude: {
    type: Number,
    required: true
  },
  longitude: {
    type: Number,
    required: true
  }
})

const weatherData = ref(null)
const loading = ref(true)
const error = ref(false)

const todayWeather = computed(() => {
  if (!weatherData.value || !weatherData.value.forecasts || weatherData.value.forecasts.length === 0) {
    return {}
  }
  return weatherData.value.forecasts[0]
})

onMounted(async () => {
  await fetchWeather()
})

const fetchWeather = async () => {
  try {
    loading.value = true
    error.value = false
    
    console.log('正在获取天气信息...', {
      latitude: props.latitude,
      longitude: props.longitude
    })
    
    const response = await fetch(
      `/api/weather/location?latitude=${props.latitude}&longitude=${props.longitude}`
    )
    
    console.log('天气API响应状态:', response.status)
    
    if (!response.ok) {
      console.error('天气API请求失败:', response.status, response.statusText)
      error.value = true
      return
    }
    
    const result = await response.json()
    console.log('天气API返回数据:', result)
    
    if (result.code === 200) {
      weatherData.value = result.data
      console.log('天气数据加载成功:', weatherData.value)
    } else {
      console.error('天气API返回错误:', result)
      error.value = true
    }
  } catch (e) {
    console.error('获取天气失败，详细错误:', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

const getWeatherIcon = (weather) => {
  const iconMap = {
    '晴': '☀️',
    '多云': '⛅',
    '阴': '☁️',
    '小雨': '🌦️',
    '中雨': '🌧️',
    '大雨': '⛈️',
    '暴雨': '⛈️',
    '雷阵雨': '⛈️',
    '雨夹雪': '🌨️',
    '小雪': '❄️',
    '中雪': '❄️',
    '大雪': '❄️',
    '暴雪': '❄️',
    '雾': '🌫️',
    '霾': '😷'
  }
  
  for (const key in iconMap) {
    if (weather && weather.includes(key)) {
      return iconMap[key]
    }
  }
  
  return '🌤️'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}
</script>

<style lang="scss" scoped>
.weather-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  color: white;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.weather-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.weather-icon {
  font-size: 64px;
  line-height: 1;
}

.weather-info {
  flex: 1;
}

.weather-temp {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
  
  .temp-high {
    color: #fff;
  }
  
  .temp-divider {
    margin: 0 8px;
    opacity: 0.6;
  }
  
  .temp-low {
    opacity: 0.8;
    font-size: 28px;
  }
}

.weather-desc {
  font-size: 18px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  opacity: 0.8;
}

.weather-details {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
}

.weather-forecast {
  margin-top: 20px;
}

.forecast-title {
  font-size: 14px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.forecast-list {
  display: flex;
  gap: 12px;
}

.forecast-item {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 8px;
  text-align: center;
  backdrop-filter: blur(10px);
}

.forecast-date {
  font-size: 12px;
  margin-bottom: 8px;
  opacity: 0.8;
}

.forecast-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.forecast-weather {
  font-size: 13px;
  margin-bottom: 8px;
}

.forecast-temp {
  font-size: 14px;
  
  .temp-high {
    font-weight: bold;
    margin-right: 4px;
  }
  
  .temp-low {
    opacity: 0.7;
    font-size: 12px;
  }
}

.weather-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 12px;
  opacity: 0.7;
}

.weather-loading,
.weather-error {
  background: #f5f5f5;
  color: #666;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
