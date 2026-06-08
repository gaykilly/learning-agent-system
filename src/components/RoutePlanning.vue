<template>
  <div class="route-content">
    <div class="route-top-bar">
      <h3>航线管理</h3>
      <button class="add-route-btn" @click="showAddRouteModal = true">+ 添加航线</button>
    </div>
    
    <!-- 添加航线弹窗 -->
    <div v-if="showAddRouteModal" class="modal-overlay" @click="closeAddRouteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h4>添加新航线</h4>
          <button class="modal-close" @click="closeAddRouteModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label>航线名称</label>
            <input v-model="newRoute.name" type="text" class="form-input" placeholder="请输入航线名称" />
          </div>
          <div class="form-row">
            <label>起点名称</label>
            <input v-model="newRoute.start" type="text" class="form-input" placeholder="如：北京" />
          </div>
          <div class="form-row">
            <label>起点坐标</label>
            <input v-model="newRoute.startCoord" type="text" class="form-input" placeholder="格式：经度,纬度" />
          </div>
          <div class="form-row">
            <label>终点名称</label>
            <input v-model="newRoute.end" type="text" class="form-input" placeholder="如：上海" />
          </div>
          <div class="form-row">
            <label>终点坐标</label>
            <input v-model="newRoute.endCoord" type="text" class="form-input" placeholder="格式：经度,纬度" />
          </div>
          <div class="form-row">
            <label>航线颜色</label>
            <input v-model="newRoute.color" type="color" class="form-input color-input" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeAddRouteModal">取消</button>
          <button class="btn-confirm" @click="addRoute">确认添加</button>
        </div>
      </div>
    </div>
    
    <div class="route-list">
      <div 
        v-for="route in routeData" 
        :key="route.id"
        :class="{ selected: selectedRoute === route.id, editing: isRouteEditing && editingRouteId === route.id }"
        @click="handleRouteClick(route.id)"
      >
        <!-- 编辑模式显示表单 -->
        <div v-if="isRouteEditing && editingRouteId === route.id" class="route-edit-form" @click.stop>
          <div class="form-row">
            <label>航线名称</label>
            <input v-model="route.name" type="text" class="form-input" />
          </div>
          <div class="form-row">
            <label>起点</label>
            <input v-model="route.start" type="text" class="form-input" />
          </div>
          <div class="form-row">
            <label>起点坐标</label>
            <input 
              :value="route.path[0] ? route.path[0][0] + ',' + route.path[0][1] : ''" 
              type="text" 
              class="form-input"
              @change="updateStartCoord($event, route)"
              placeholder="经度,纬度"
            />
          </div>
          <div class="form-row">
            <label>终点</label>
            <input v-model="route.end" type="text" class="form-input" />
          </div>
          <div class="form-row">
            <label>终点坐标</label>
            <input 
              :value="route.path.length > 1 ? route.path[route.path.length - 1][0] + ',' + route.path[route.path.length - 1][1] : ''" 
              type="text" 
              class="form-input"
              @change="updateEndCoord($event, route)"
              placeholder="经度,纬度"
            />
          </div>
          <div class="form-row">
            <label>距离</label>
            <span class="form-value">{{ calculateRouteDistance(route.path).toFixed(2) }} km</span>
          </div>
          <div class="form-row">
            <label>航点数量</label>
            <span class="form-value">{{ route.path.length }} 个</span>
          </div>
        </div>
        
        <!-- 非编辑模式显示摘要 -->
        <div v-else>
          <div class="route-header">
            <span class="route-name">{{ route.name }}</span>
            <span class="route-distance">{{ calculateRouteDistance(route.path).toFixed(2) }} km</span>
          </div>
          <div class="route-info">
            <span>{{ route.start }} → {{ route.end }}</span>
            <span class="route-time">{{ route.time }}</span>
          </div>
        </div>
        
        <div class="route-actions">
          <button @click.stop="toggleRouteEdit(route.id)" class="edit-btn">
            {{ isRouteEditing && editingRouteId === route.id ? '退出编辑' : '编辑' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  routeData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['route-click', 'route-edit', 'route-add'])

const selectedRoute = ref(null)
const isRouteEditing = ref(false)
const editingRouteId = ref(null)
const showAddRouteModal = ref(false)
const newRoute = reactive({
  name: '',
  start: '',
  startCoord: '',
  end: '',
  endCoord: '',
  color: '#4A90E2'
})

// 计算两点之间的距离（单位：公里）
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng/2) * Math.sin(dLng/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return R * c
}

// 计算整条航线的距离
const calculateRouteDistance = (path) => {
  if (!path || path.length < 2) return 0
  
  let totalDistance = 0
  for (let i = 0; i < path.length - 1; i++) {
    const point1 = path[i]
    const point2 = path[i + 1]
    totalDistance += calculateDistance(point1[1], point1[0], point2[1], point2[0])
  }
  return totalDistance
}

// 解析坐标字符串
const parseCoord = (coordStr) => {
  if (!coordStr || !coordStr.trim()) return null
  const parts = coordStr.split(',')
  if (parts.length !== 2) return null
  const lng = parseFloat(parts[0].trim())
  const lat = parseFloat(parts[1].trim())
  if (isNaN(lng) || isNaN(lat)) return null
  return [lng, lat]
}

// 更新起点坐标
const updateStartCoord = (event, route) => {
  const coordStr = event.target.value
  const coord = parseCoord(coordStr)
  if (coord) {
    if (route.path.length === 0) {
      route.path.push(coord)
    } else {
      const newPath = [...route.path]
      newPath[0] = coord
      route.path = newPath
    }
    emit('route-edit', { routeId: route.id, path: route.path })
  }
}

// 更新终点坐标
const updateEndCoord = (event, route) => {
  const coordStr = event.target.value
  const coord = parseCoord(coordStr)
  if (coord) {
    if (route.path.length < 2) {
      if (route.path.length === 0) {
        route.path.push([116.4, 39.9])
      }
      route.path.push(coord)
    } else {
      const newPath = [...route.path]
      newPath[route.path.length - 1] = coord
      route.path = newPath
    }
    emit('route-edit', { routeId: route.id, path: route.path })
  }
}

// 关闭添加航线弹窗
const closeAddRouteModal = () => {
  showAddRouteModal.value = false
  newRoute.name = ''
  newRoute.start = ''
  newRoute.startCoord = ''
  newRoute.end = ''
  newRoute.endCoord = ''
  newRoute.color = '#4A90E2'
}

// 添加新航线
const addRoute = () => {
  if (!newRoute.name.trim()) {
    alert('请输入航线名称')
    return
  }
  
  const routeId = 'R' + Date.now()
  const path = []
  
  const startCoord = parseCoord(newRoute.startCoord)
  if (startCoord) {
    path.push(startCoord)
  }
  
  const endCoord = parseCoord(newRoute.endCoord)
  if (endCoord) {
    path.push(endCoord)
  }
  
  const newItem = {
    id: routeId,
    name: newRoute.name,
    start: newRoute.start || '起点',
    end: newRoute.end || '终点',
    distance: path.length >= 2 ? calculateRouteDistance(path).toFixed(2) + ' km' : '0 km',
    time: '0小时',
    color: newRoute.color,
    width: 4,
    path: path
  }
  
  emit('route-add', newItem)
  closeAddRouteModal()
}

const handleRouteClick = (routeId) => {
  selectedRoute.value = routeId
  isRouteEditing.value = false
  editingRouteId.value = null
  emit('route-click', routeId)
}

const toggleRouteEdit = (routeId) => {
  isRouteEditing.value = !isRouteEditing.value
  editingRouteId.value = isRouteEditing.value ? routeId : null
  emit('route-edit', { routeId, editing: isRouteEditing.value })
}
</script>

<style scoped>
.route-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.route-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.route-top-bar h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.add-route-btn {
  padding: 6px 16px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.add-route-btn:hover {
  background: #3a7bc8;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 480px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
}

.modal-close:hover {
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-row {
  margin-bottom: 16px;
}

.form-row label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #4A90E2;
}

.color-input {
  width: 60px;
  padding: 2px;
  cursor: pointer;
}

.form-value {
  display: inline-block;
  padding: 10px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #333;
  font-size: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel {
  padding: 8px 20px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-cancel:hover {
  background: #eee;
}

.btn-confirm {
  padding: 8px 20px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-confirm:hover {
  background: #3a7bc8;
}

.route-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.route-list > div {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.route-list > div:hover {
  background: #e8f4fd;
  border-color: #4A90E2;
}

.route-list > div.selected {
  background: #e8f0fe;
  border-color: #4A90E2;
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.route-name {
  font-weight: 500;
  color: #333;
}

.route-distance {
  font-size: 13px;
  color: #4A90E2;
  font-weight: 500;
}

.route-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}

.route-time {
  color: #999;
}

.route-edit-form {
  background: white;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 10px;
}

.route-edit-form .form-row {
  margin-bottom: 12px;
}

.route-edit-form .form-row:last-child {
  margin-bottom: 0;
}

.route-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.edit-btn {
  padding: 6px 14px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.edit-btn:hover {
  background: #3a7bc8;
}
</style>