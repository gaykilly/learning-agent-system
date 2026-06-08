<template>
  <section class="device-map">
    <h3>{{ title }}</h3>
    <div class="map-header">
      <span>总设备数：{{ deviceCount }}台</span>
      <div class="map-style-switch">
        <button 
          v-for="style in mapStyles" 
          :key="style.key"
          :class="{ active: currentMapStyle === style.key }"
          @click="switchMapStyle(style.key)"
        >
          {{ style.label }}
        </button>
      </div>
    </div>
    <div class="map-container">
      <div :id="containerId" class="amap-container"></div>
      <div :id="tooltipId" class="coordinate-tooltip" style="display: none;">
        <span>经度: <span :id="lngValueId">0.00000</span></span>
        <span>纬度: <span :id="latValueId">0.00000</span></span>
      </div>
      <div class="map-legend">
        <div class="legend-item">
          <div class="legend-dot blue"></div>
          <span>正常</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot orange"></div>
          <span>警告</span>
        </div>
        <div class="legend-item">
          <div class="legend-dot red"></div>
          <span>故障</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '设备分布地图'
  },
  deviceData: {
    type: Array,
    default: () => []
  },
  taskData: {
    type: Array,
    default: () => []
  },
  selectedTaskId: {
    type: String,
    default: null
  },
  center: {
    type: Array,
    default: () => [116.40039, 39.90886]
  },
  zoom: {
    type: Number,
    default: 14
  },
  canClickSelect: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['map-click'])

const containerId = ref('amap-container-' + Math.random().toString(36).substr(2, 9))
const tooltipId = ref('coordinate-tooltip-' + Math.random().toString(36).substr(2, 9))
const lngValueId = ref('lng-value-' + Math.random().toString(36).substr(2, 9))
const latValueId = ref('lat-value-' + Math.random().toString(36).substr(2, 9))

let map = null
let mapReady = false
let selectedMarker = null
let satelliteLayer = null
let defaultLayer = null
let taskMarkers = []

const currentMapStyle = ref('normal')
const mapStyles = [
  { key: 'normal', label: '普通' },
  { key: 'satellite', label: '卫星' },
  { key: 'dark', label: '深色' }
]

const deviceCount = computed(() => props.deviceData.length)

const getStatusColor = (status) => {
  const colors = {
    normal: '#4A90E2',
    warning: '#FF9800',
    error: '#F44336'
  }
  return colors[status] || '#4A90E2'
}

const createMarker = (device) => {
  const color = getStatusColor(device.status)
  const markerContent = `
    <div style="
      width: 16px;
      height: 16px;
      background: ${color};
      border-radius: 50%;
      border: 2px solid white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.3);
    "></div>
  `
  
  return new window.AMap.Marker({
    position: [device.lng, device.lat],
    content: markerContent,
    title: device.name,
    offset: new window.AMap.Pixel(-8, -8)
  })
}

const createTaskMarker = (task) => {
  const markerContent = `
    <div style="
      width: 20px;
      height: 20px;
      background: #FF6B6B;
      border-radius: 50%;
      border: 3px solid white;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
    ">
      <div style="
        width: 6px;
        height: 6px;
        background: white;
        border-radius: 50%;
      "></div>
    </div>
  `
  
  return new window.AMap.Marker({
    position: [task.lng, task.lat],
    content: markerContent,
    title: task.name || '项目位置',
    offset: new window.AMap.Pixel(-10, -10)
  })
}

const initMap = () => {
  if (!window.AMap || map) return
  
  const container = document.getElementById(containerId.value)
  if (!container) {
    setTimeout(initMap, 100)
    return
  }
  
  const rect = container.getBoundingClientRect()
  if (rect.width < 100 || rect.height < 100) {
    setTimeout(initMap, 100)
    return
  }
  
  map = new window.AMap.Map(containerId.value, {
    center: props.center,
    zoom: props.zoom,
    mapStyle: 'amap://styles/normal'
  })

    // 保存默认图层引用
    const layers = map.getLayers()
    if (layers.length > 0) {
      defaultLayer = layers[0]
    }

    if (window.AMap.Scale) map.addControl(new window.AMap.Scale())
    if (window.AMap.ToolBar) map.addControl(new window.AMap.ToolBar())

    props.deviceData.forEach(device => {
      const marker = createMarker(device)
      map.add(marker)
    })
    
    // 添加项目标记
    updateTaskMarkers()
    
    mapReady = true

    map.on('mousemove', (e) => {
      const lng = e.lnglat.getLng().toFixed(5)
      const lat = e.lnglat.getLat().toFixed(5)
      
      const lngEl = document.getElementById(lngValueId.value)
      if (lngEl) lngEl.textContent = lng
      const latEl = document.getElementById(latValueId.value)
      if (latEl) latEl.textContent = lat
      
      const tooltip = document.getElementById(tooltipId.value)
      if (tooltip) {
        tooltip.style.display = 'block'
        tooltip.style.left = `${e.pixel.x + 15}px`
        tooltip.style.top = `${e.pixel.y + 15}px`
      }
    })
    
    map.on('mouseout', () => {
      const tooltip = document.getElementById(tooltipId.value)
      if (tooltip) tooltip.style.display = 'none'
    })
    
    map.on('click', (e) => {
      if (props.canClickSelect) {
        const lng = e.lnglat.getLng()
        const lat = e.lnglat.getLat()
        emit('map-click', lng, lat, '自定义位置')
      }
    })
}

const updateTaskMarkers = () => {
  if (!map) return
  
  // 移除所有旧的任务标记
  taskMarkers.forEach(marker => {
    map.remove(marker)
  })
  taskMarkers = []
  
  // 添加新的任务标记（排除当前选中的项目）
  try {
    props.taskData.forEach(task => {
      // 跳过当前选中的项目（如果传入了 selectedTaskId）
      if (props.selectedTaskId && task.id === props.selectedTaskId) {
        return
      }
      if (task && typeof task.lng === 'number' && typeof task.lat === 'number') {
        const taskMarker = createTaskMarker(task)
        map.add(taskMarker)
        taskMarkers.push(taskMarker)
      }
    })
  } catch (e) {
    console.error('Error adding task markers:', e)
  }
}

const switchMapStyle = (style) => {
  currentMapStyle.value = style
  if (!map) return
  
  if (style === 'satellite') {
    // 切换到卫星地图
    // 隐藏默认图层
    if (defaultLayer) {
      defaultLayer.hide()
    }
    // 创建并显示卫星图层
    if (!satelliteLayer && window.AMap.TileLayer) {
      satelliteLayer = new window.AMap.TileLayer.Satellite()
      map.add(satelliteLayer)
    }
    if (satelliteLayer) {
      satelliteLayer.show()
    }
  } else {
    // 切换到普通或深色地图
    // 隐藏卫星图层
    if (satelliteLayer) {
      satelliteLayer.hide()
    }
    // 显示默认图层
    if (defaultLayer) {
      defaultLayer.show()
    }
    // 设置地图样式
    const styleMap = {
      normal: 'amap://styles/normal',
      dark: 'amap://styles/dark'
    }
    map.setMapStyle(styleMap[style] || 'amap://styles/normal')
  }
}

let resizeTimer = null

const handleResize = () => {
  if (map) {
    map.resize()
  }
}

const setMapCenter = (lng, lat) => {
  if (map && mapReady) {
    map.setCenter([lng, lat])
    map.setZoom(16)
    addMarker(lng, lat)
  } else if (!mapReady) {
    setTimeout(() => {
      setMapCenter(lng, lat)
    }, 300)
  }
}

const addMarker = (lng, lat) => {
  // 移除之前的标记
  if (selectedMarker) {
    map.remove(selectedMarker)
    selectedMarker = null
  }
  
  // 创建新标记
  const markerContent = `
    <div style="
      width: 24px;
      height: 24px;
      background: #4A90E2;
      border-radius: 50%;
      border: 3px solid white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
    ">
      <div style="
        width: 8px;
        height: 8px;
        background: white;
        border-radius: 50%;
      "></div>
    </div>
  `
  
  selectedMarker = new window.AMap.Marker({
    position: [lng, lat],
    content: markerContent,
    title: '项目位置',
    offset: new window.AMap.Pixel(-12, -12)
  })
  
  map.add(selectedMarker)
}

const clearMarker = () => {
  if (selectedMarker) {
    map.remove(selectedMarker)
    selectedMarker = null
  }
}

defineExpose({
  setMapCenter,
  clearMarker,
  addMarker
})

onMounted(() => {
  console.log('DeviceMap mounted, window.AMap:', !!window.AMap)
  
  if (window.AMap) {
    initMap()
  } else {
    let attempts = 0
    const checkTimer = setInterval(() => {
      attempts++
      console.log('Checking AMap, attempt:', attempts)
      if (window.AMap) {
        clearInterval(checkTimer)
        initMap()
      } else if (attempts > 30) {
        clearInterval(checkTimer)
        console.error('AMap API failed to load after 30 attempts')
      }
    }, 100)
  }
  
  window.addEventListener('resize', () => {
    if (resizeTimer) clearTimeout(resizeTimer)
    resizeTimer = setTimeout(handleResize, 200)
  })
})

// 监听任务数据变化，更新地图标记
watch(() => props.taskData, () => {
  updateTaskMarkers()
}, { deep: true })

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
  if (satelliteLayer) {
    satelliteLayer = null
  }
  if (defaultLayer) {
    defaultLayer = null
  }
  if (resizeTimer) clearTimeout(resizeTimer)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.device-map {
  width: 100%;
  height: 100%;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.device-map h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 16px;
}

.map-header {
  margin-bottom: 16px;
  font-size: 14px;
  color: #999999;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-style-switch {
  display: flex;
  gap: 8px;
}

.map-style-switch button {
  padding: 4px 12px;
  font-size: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background-color: #ffffff;
  color: #666666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.map-style-switch button:hover {
  border-color: #4A90E2;
  color: #4A90E2;
}

.map-style-switch button.active {
  background-color: #4A90E2;
  border-color: #4A90E2;
  color: #ffffff;
}

.map-container {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 500px;
  height: calc(100vh - 200px);
  border-radius: 8px;
  overflow: hidden;
}

.amap-container {
  width: 100%;
  height: 100%;
  background-color: #f4f4f4;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  left: 10px;
  display: flex;
  gap: 15px;
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.blue { background: #4A90E2; }
.legend-dot.orange { background: #FF9800; }
.legend-dot.red { background: #F44336; }

.coordinate-tooltip {
  position: absolute;
  padding: 6px 10px;
  background: rgba(50, 50, 50, 0.9);
  color: white;
  font-size: 12px;
  border-radius: 4px;
  pointer-events: none;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 2px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.coordinate-tooltip span span {
  color: #4CAF50;
  font-weight: bold;
}
</style>