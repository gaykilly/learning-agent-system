<template>
  <div class="map-container">
    <div :id="containerId" class="amap-container"></div>
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
    <div :id="tooltipId" class="coordinate-tooltip" style="display: none;">
      <span>经度: <span :id="lngId">0.00000</span></span>
      <span>纬度: <span :id="latId">0.00000</span></span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  containerId: {
    type: String,
    default: 'amap-container'
  },
  tooltipId: {
    type: String,
    default: 'coordinate-tooltip'
  },
  lngId: {
    type: String,
    default: 'lng-value'
  },
  latId: {
    type: String,
    default: 'lat-value'
  }
})

const emit = defineEmits(['mapReady'])

const currentMapStyle = ref('normal')
const mapStyles = [
  { key: 'normal', label: '普通' },
  { key: 'satellite', label: '卫星' },
  { key: 'dark', label: '深色' }
]

let map = null
let satelliteLayer = null
let defaultLayer = null
let initAttempts = 0
const maxAttempts = 50

const initMap = () => {
  initAttempts++
  
  if (!window.AMap) {
    if (initAttempts < maxAttempts) {
      setTimeout(initMap, 200)
    } else {
      console.error('高德地图API加载失败，请检查网络连接或API密钥')
    }
    return
  }
  
  nextTick(() => {
    const container = document.getElementById(props.containerId)
    if (!container) {
      if (initAttempts < maxAttempts) {
        setTimeout(initMap, 100)
      } else {
        console.error(`地图容器元素不存在: ${props.containerId}`)
      }
      return
    }
    
    const rect = container.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) {
      if (initAttempts < maxAttempts) {
        setTimeout(initMap, 100)
      } else {
        console.error(`地图容器尺寸为0，请检查CSS样式: ${props.containerId}`)
      }
      return
    }
    
    try {
      map = new window.AMap.Map(props.containerId, {
        zoom: 13,
        center: [116.39739, 39.90886],
        mapStyle: 'amap://styles/normal',
        viewMode: '2D'
      })
      
      // 保存默认图层引用
      const layers = map.getLayers()
      if (layers.length > 0) {
        defaultLayer = layers[0]
      }
      
      if (window.AMap.Scale) {
        map.addControl(new window.AMap.Scale())
      }
      if (window.AMap.ToolBar) {
        map.addControl(new window.AMap.ToolBar())
      }
      
      map.on('mousemove', (e) => {
        const lng = e.lnglat.getLng().toFixed(5)
        const lat = e.lnglat.getLat().toFixed(5)
        const lngEl = document.getElementById(props.lngId)
        if (lngEl) lngEl.textContent = lng
        const latEl = document.getElementById(props.latId)
        if (latEl) latEl.textContent = lat
        const tooltip = document.getElementById(props.tooltipId)
        if (tooltip) {
          tooltip.style.display = 'block'
          tooltip.style.left = (e.pixel.x + 10) + 'px'
          tooltip.style.top = (e.pixel.y - 40) + 'px'
        }
      })
      
      map.on('mouseout', () => {
        const tooltip = document.getElementById(props.tooltipId)
        if (tooltip) {
          tooltip.style.display = 'none'
        }
      })
      
      emit('mapReady', map)
      console.log('地图初始化成功')
      
    } catch (error) {
      console.error('地图初始化失败:', error)
      if (initAttempts < maxAttempts) {
        setTimeout(initMap, 200)
      }
    }
  })
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

onMounted(() => {
  initMap()
})

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
})

defineExpose({
  getMap: () => map,
  switchMapStyle
})
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 300px;
}

.amap-container {
  width: 100%;
  height: 100%;
  min-height: 300px;
  background-color: #e8e8e8;
}

.map-style-switch {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

.map-style-switch button {
  margin-left: 5px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.map-style-switch button:hover {
  background: rgba(255, 255, 255, 1);
}

.map-style-switch button.active {
  background: #4A90E2;
  color: white;
}

.coordinate-tooltip {
  position: absolute;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 100;
  pointer-events: none;
}

.coordinate-tooltip span {
  margin-right: 10px;
}
</style>