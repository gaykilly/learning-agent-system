<template>
  <div class="task-detail-container">
    <!-- 顶部导航栏 -->
    <TopNav />

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 左侧面板 -->
      <div class="left-panel" :class="{ expanded: showContent }">
        <!-- 左侧导航栏 -->
        <NavSidebar 
          :nav-items="navItems"
          :current-tab="currentTab"
          :show-content="showContent"
          @nav-click="toggleContent"
          @logout="goBackToProject"
        />

        <!-- 右侧内容面板 -->
        <transition name="slide">
          <div v-if="showContent" class="content-panel">
            <!-- 关闭按钮 -->
            <div class="content-header">
              <h3>{{ currentNavLabel }}</h3>
              <button class="close-content" @click="closeContentPanel">✕</button>
            </div>
            
            <!-- 内容区域 -->
            <div class="tab-content">
              <!-- 地图模型 -->
              <div v-if="currentTab === 'model'" class="model-section">
                <ModelSection :models="models" />
              </div>

              <!-- 任务规划 -->
              <div v-if="currentTab === 'task'" class="task-planning">
                <TaskPlanning :stats="taskStats" :tasks="recentTasks" />
              </div>

              <!-- 航线规划 -->
              <div v-if="currentTab === 'route'" class="route-planning-wrapper">
                <RoutePlanning 
                  :route-data="routeData"
                  @route-click="handleRouteClick"
                  @route-edit="handleRouteEdit"
                  @route-add="handleRouteAdd"
                />
              </div>

              <!-- 数据统计 -->
              <div v-if="currentTab === 'stats'" class="stats-section">
                <StatsSection />
              </div>

              <!-- 历史影像 -->
              <div v-if="currentTab === 'history'" class="history-wrapper">
                <HistoryMedia 
                  :media-data="historyImages"
                  @media-click="openMediaViewer"
                />
              </div>

              <!-- 直播 -->
              <div v-if="currentTab === 'live'" class="live-wrapper">
                <LiveSection :stream-url="liveStreamUrl" />
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 媒体查看器模态框 -->
      <MediaViewer 
        :visible="showMediaViewer"
        :media="currentMedia"
        @close="closeMediaViewer"
      />

      <!-- 右侧内容区：地图 -->
      <div class="right-section">
        <div class="map-section">
          <AmapContainer 
            container-id="amap-container-task"
            tooltip-id="coordinate-tooltip-task"
            lng-id="lng-value-task"
            lat-id="lat-value-task"
            @map-ready="handleMapReady"
          />
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
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TopNav from '@/components/TopNav.vue'
import NavSidebar from '@/components/NavSidebar.vue'
import AmapContainer from '@/components/AmapContainer.vue'
import RoutePlanning from '@/components/RoutePlanning.vue'
import HistoryMedia from '@/components/HistoryMedia.vue'
import MediaViewer from '@/components/MediaViewer.vue'
import ModelSection from '@/components/ModelSection.vue'
import TaskPlanning from '@/components/TaskPlanning.vue'
import StatsSection from '@/components/StatsSection.vue'
import LiveSection from '@/components/LiveSection.vue'

const router = useRouter()
const route = useRoute()

// 获取当前项目信息
const getCurrentTask = () => {
  const saved = localStorage.getItem('tasks')
  if (saved) {
    try {
      const tasks = JSON.parse(saved)
      const taskId = parseInt(route.params.id)
      return tasks.find(t => t.id === taskId)
    } catch (e) {
      console.error('Failed to parse tasks:', e)
    }
  }
  return null
}

const currentTask = getCurrentTask()

let map = null
let satelliteLayer = null
let currentRoute = null
let routeMarkers = []

// 当前选中的标签
const currentTab = ref('model')

// 是否显示内容面板
const showContent = ref(false)

// 当前导航标签的名称
const currentNavLabel = ref('')

// 侧边导航项
const navItems = [
  { key: 'model', label: '地图模型', icon: '🗺️' },
  { key: 'task', label: '任务规划', icon: '📋' },
  { key: 'route', label: '航线规划', icon: '✈️' },
  { key: 'live', label: '实时直播', icon: '📹' },
  { key: 'stats', label: '数据统计', icon: '📊' },
  { key: 'history', label: '历史影像', icon: '📷' },
]

// 直播流地址
const liveStreamUrl = ref('http://127.0.0.1/live/test.m3u8')

// 地图模型数据
const models = ref([
  { id: 1, name: '城市三维模型 V2.0', date: '2024-01-10' },
  { id: 2, name: '地形模型 V1.5', date: '2024-01-08' },
  { id: 3, name: '建筑物模型 V3.0', date: '2024-01-05' },
])

// 任务统计
const taskStats = ref({
  total: 26,
  pending: 8,
  processing: 3,
})

// 近期任务
const recentTasks = ref([
  { id: 1, name: '设备巡检', status: 'pending', time: '09:00' },
  { id: 2, name: '数据采集', status: 'processing', time: '10:30' },
  { id: 3, name: '系统更新', status: 'pending', time: '14:00' },
])

// 航线数据
const routeData = ref([
  {
    id: 'H1',
    name: '湖北汽车学院航线',
    start: '湖北汽车学院',
    end: '逸夫图书馆',
    distance: '1 km',
    time: '8分钟',
    color: '#4CAF50',
    width: 4,
    path: [
      [110.738702, 32.65063],
      [110.7375, 32.6510],
      [110.736798, 32.651653]
    ]
  },
  {
    id: 'A1',
    name: '航线 A1',
    start: '北京',
    end: '上海',
    distance: '1318 km',
    time: '2小时15分',
    color: '#FF6B6B',
    width: 4,
    path: [
      [116.4074, 39.9042],
      [117.2009, 39.1256],
      [118.0178, 36.8065],
      [119.9869, 36.6211],
      [121.4737, 31.2304]
    ]
  },
  {
    id: 'B2',
    name: '航线 B2',
    start: '上海',
    end: '广州',
    distance: '1432 km',
    time: '2小时30分',
    color: '#4A90E2',
    width: 4,
    path: [
      [121.4737, 31.2304],
      [120.6206, 30.0538],
      [119.3062, 29.9889],
      [113.2644, 23.1291],
    ]
  }
])

// 历史影像（包含图片和视频）
const historyImages = ref([
  { id: 1, type: 'video', name: '区域A影像', date: '2024-01-14', url: '/videos/drone-1.mp4', thumbnail: 'https://via.placeholder.com/300x200?text=Video+1' },
  { id: 2, type: 'video', name: '无人机航拍视频', date: '2024-01-13', url: '/videos/drone-2.mp4', thumbnail: 'https://via.placeholder.com/300x200?text=Drone+Video' },
  { id: 3, type: 'video', name: '区域C影像', date: '2024-01-12', url: '/videos/drone-3.mp4', thumbnail: 'https://via.placeholder.com/300x200?text=Video+3' },
  { id: 4, type: 'video', name: '巡检记录视频', date: '2024-01-11', url: '/videos/drone-1.mp4', thumbnail: 'https://via.placeholder.com/300x200?text=Inspection+Video' },
])

// 媒体查看器
const showMediaViewer = ref(false)
const currentMedia = ref(null)

const openMediaViewer = (item) => {
  currentMedia.value = item
  showMediaViewer.value = true
}

const closeMediaViewer = () => {
  showMediaViewer.value = false
  currentMedia.value = null
}

// 设备数据
const deviceData = [
  { id: 1, name: '设备1', status: 'normal', lat: 39.90886, lng: 116.39739 },
  { id: 2, name: '设备2', status: 'normal', lat: 39.91086, lng: 116.40039 },
  { id: 3, name: '设备3', status: 'warning', lat: 39.90686, lng: 116.40239 },
  { id: 4, name: '设备4', status: 'normal', lat: 39.90486, lng: 116.39939 },
  { id: 5, name: '设备5', status: 'error', lat: 39.91286, lng: 116.39839 },
  { id: 6, name: '设备6', status: 'normal', lat: 39.90986, lng: 116.40339 },
  { id: 7, name: '设备7', status: 'normal', lat: 39.90786, lng: 116.40139 },
  { id: 8, name: '设备8', status: 'warning', lat: 39.91186, lng: 116.40439 },
]

const createMarker = (device) => {
  const color = device.status === 'normal' ? '#4CAF50' : device.status === 'warning' ? '#FF9800' : '#f44336'
  
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
    title: device.name,
    content: markerContent,
  })
}

const handleMapReady = (amap) => {
  map = amap
  
  deviceData.forEach(device => {
    const marker = createMarker(device)
    map.add(marker)
  })
  
  // 如果有当前项目，跳转到项目位置
  if (currentTask && currentTask.lng && currentTask.lat) {
    console.log('Setting map center to task location:', currentTask.lng, currentTask.lat)
    map.setCenter([currentTask.lng, currentTask.lat])
    map.setZoom(16)
    
    // 在项目位置添加标记
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
    
    const taskMarker = new window.AMap.Marker({
      position: [currentTask.lng, currentTask.lat],
      content: markerContent,
      title: currentTask.name || '项目位置',
      offset: new window.AMap.Pixel(-12, -12)
    })
    
    map.add(taskMarker)
  }
  
  map.on('click', handleMapClickForAddPoint)
}

const showRouteOnMap = (routeId) => {
  if (!map || !window.AMap) return

  if (currentRoute) {
    map.remove(currentRoute)
    currentRoute = null
  }

  if (routeMarkers.length > 0) {
    map.remove(routeMarkers)
    routeMarkers = []
  }

  const route = routeData.value.find(r => r.id === routeId)
  if (!route || !route.path || route.path.length < 2) return

  const lnglatPath = route.path.map(point => {
    if (point instanceof window.AMap.LngLat) {
      return point
    } else if (Array.isArray(point) && point.length === 2) {
      return new window.AMap.LngLat(point[0], point[1])
    }
    return null
  }).filter(Boolean)

  if (lnglatPath.length < 2) return

  currentRoute = new window.AMap.Polyline({
    path: lnglatPath,
    strokeColor: route.color,
    strokeWeight: route.width,
    strokeOpacity: 0.8,
    strokeStyle: 'solid',
    lineJoin: 'round'
  })

  map.add(currentRoute)
  map.setFitView([currentRoute])

  routeMarkers = lnglatPath.map((point, index) => {
    const isStart = index === 0
    const isEnd = index === lnglatPath.length - 1
    const color = route.color || '#FF6B6B'
    
    let marker
    if (isStart || isEnd) {
      marker = new window.AMap.Marker({
        position: point,
        title: isStart ? route.start : route.end,
        draggable: true,
        cursor: 'move',
        content: `
          <div style="
            width: 20px;
            height: 20px;
            background: ${isStart ? '#4CAF50' : '#F44336'};
            border-radius: 50%;
            border: 3px solid white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            justify-content: center;
          ">
            <span style="color: white; font-size: 12px; font-weight: bold;">${index + 1}</span>
          </div>
        `,
        offset: new window.AMap.Pixel(-10, -10)
      })
    } else {
      marker = new window.AMap.Marker({
        position: point,
        draggable: true,
        cursor: 'move',
        content: `
          <div style="
            width: 16px;
            height: 16px;
            background: ${color};
            border-radius: 50%;
            border: 3px solid white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            justify-content: center;
          ">
            <span style="color: white; font-size: 10px; font-weight: bold;">${index + 1}</span>
          </div>
        `,
        offset: new window.AMap.Pixel(-8, -8)
      })
    }
    
    marker.on('dragend', () => {
      updateRoutePath(routeId)
    })
    
    marker.on('click', () => {
      const markerIndex = routeMarkers.indexOf(marker)
      if (markerIndex !== -1 && route.path.length > 2) {
        deleteWaypoint(routeId, markerIndex)
      }
    })
    
    return marker
  })
  
  map.add(routeMarkers)
  
  currentRoute.routeId = routeId
}

const handleRouteClick = (routeId) => {
  showRouteOnMap(routeId)
}

const handleRouteEdit = (data) => {
  if (!map) {
    console.warn('地图实例未初始化，无法编辑航线')
    return
  }
  
  if (data.editing) {
    editingRouteId = data.routeId
    map.on('click', handleMapClickForAddPoint)
    map.setCursor('crosshair')
  } else {
    editingRouteId = null
    map.off('click', handleMapClickForAddPoint)
    map.setCursor('default')
  }
  showRouteOnMap(data.routeId)
}

const handleRouteAdd = (newRoute) => {
  routeData.value.push(newRoute)
  if (newRoute.path.length >= 2 && map) {
    showRouteOnMap(newRoute.id)
  }
}

let editingRouteId = null

const handleMapClickForAddPoint = (e) => {
  if (!editingRouteId || !map) return
  
  const route = routeData.value.find(r => r.id === editingRouteId)
  if (!route) return
  
  const newPoint = [e.lnglat.getLng(), e.lnglat.getLat()]
  route.path = [...route.path, newPoint]
  
  const color = route.color || '#FF6B6B'
  
  const marker = new window.AMap.Marker({
    position: e.lnglat,
    draggable: true,
    cursor: 'move',
    content: `
      <div style="
        width: 16px;
        height: 16px;
        background: ${color};
        border-radius: 50%;
        border: 3px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <span style="color: white; font-size: 10px; font-weight: bold;">${routeMarkers.length + 1}</span>
      </div>
    `,
    offset: new window.AMap.Pixel(-8, -8)
  })

  marker.on('dragend', () => {
    updateRoutePath(editingRouteId)
  })

  marker.on('click', () => {
    const markerIndex = routeMarkers.indexOf(marker)
    if (markerIndex !== -1) {
      deleteWaypoint(editingRouteId, markerIndex)
    }
  })

  routeMarkers.push(marker)
  map.add(marker)
  
  if (currentRoute) {
    const lnglatPath = route.path.map(point => {
      if (point instanceof window.AMap.LngLat) {
        return point
      } else if (Array.isArray(point) && point.length === 2) {
        return new window.AMap.LngLat(point[0], point[1])
      }
      return null
    }).filter(Boolean)
    currentRoute.setPath(lnglatPath)
  }
  
  routeMarkers.forEach((m, i) => {
    m.setContent(`
      <div style="
        width: 16px;
        height: 16px;
        background: ${color};
        border-radius: 50%;
        border: 3px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <span style="color: white; font-size: 10px; font-weight: bold;">${i + 1}</span>
      </div>
    `)
  })
}

const updateRoutePath = (routeId) => {
  const route = routeData.value.find(r => r.id === routeId)
  if (!route || !currentRoute) return

  const newPath = routeMarkers.map(marker => marker.getPosition())
  route.path = newPath.map(pos => [pos.getLng(), pos.getLat()])
  currentRoute.setPath(newPath)
}

const deleteWaypoint = (routeId, index) => {
  const route = routeData.value.find(r => r.id === routeId)
  if (!route || route.path.length === 0 || !map) return

  map.remove(routeMarkers[index])
  routeMarkers.splice(index, 1)
  
  const newPath = route.path.filter((_, i) => i !== index)
  route.path = newPath

  if (route.path.length >= 2 && currentRoute) {
    const lnglatPath = route.path.map(point => {
      if (point instanceof window.AMap.LngLat) {
        return point
      } else if (Array.isArray(point) && point.length === 2) {
        return new window.AMap.LngLat(point[0], point[1])
      }
      return null
    }).filter(Boolean)
    currentRoute.setPath(lnglatPath)
  } else if (currentRoute) {
    map.remove(currentRoute)
    currentRoute = null
  }

  routeMarkers.forEach((marker, i) => {
    const color = route.color || '#FF6B6B'
    marker.setContent(`
      <div style="
        width: 16px;
        height: 16px;
        background: ${color};
        border-radius: 50%;
        border: 3px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <span style="color: white; font-size: 10px; font-weight: bold;">${i + 1}</span>
      </div>
    `)
  })
}

// 切换内容面板显示
const toggleContent = (key) => {
  const item = navItems.find(item => item.key === key)
  if (item) {
    currentNavLabel.value = item.label
  }
  
  if (currentTab.value === key && showContent.value) {
    showContent.value = false
  } else {
    currentTab.value = key
    showContent.value = true
  }
}

// 关闭内容面板
const closeContentPanel = () => {
  showContent.value = false
}

// 返回项目页面
const goBackToProject = () => {
  router.push('/project')
}

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped>
.task-detail-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.main-content {
  flex: 1;
  display: flex;
  height: calc(100vh - 80px);
  padding-top: 80px;
}

.left-panel {
  width: 80px;
  background: white;
  display: flex;
  flex-direction: row;
  border-right: 1px solid #e2e8f0;
  transition: width 0.3s ease;
}

.left-panel.expanded {
  width: 60%;
}

.content-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e2e8f0;
  overflow: hidden;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.content-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.close-content {
  background: none;
  border: none;
  font-size: 16px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-content:hover {
  background: #e2e8f0;
  color: #666;
}

/* 滑动动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

/* 右侧内容区 */
.main-content .right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.map-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.blue { background-color: #4CAF50; }
.legend-dot.orange { background-color: #FF9800; }
.legend-dot.red { background-color: #f44336; }

/* 包装器样式 */
.route-planning-wrapper,
.history-wrapper {
  height: 100%;
}
</style>