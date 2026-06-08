<template>
  <div class="project-container">
    <!-- 顶部导航栏 -->
    <TopNav />

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 左侧面板：任务列表 -->
      <div class="left-panel" :style="{ width: panelWidth + 'px' }">
        <div class="task-list-area">
          <div class="task-list-header">
            <h2>{{ isCreating ? '创建项目' : '项目列表' }}</h2>
            <button v-if="!isCreating && userStore.isAdmin" class="add-project-btn" @click="showCreatePanel">
              <span class="add-icon">+</span>
              <span>创建项目</span>
            </button>
            <button v-if="isCreating" class="cancel-btn" @click="cancelCreate">
              取消
            </button>
          </div>
          <div v-if="!isCreating" class="task-filters">
              <select v-model="taskStatusFilter" class="filter-select">
                <option value="all">全部</option>
                <option value="pending">待执行</option>
                <option value="processing">执行中</option>
                <option value="completed">已完成</option>
              </select>
              <select v-model="sortField" class="filter-select">
                <option value="time">按时间</option>
                <option value="name">按项目名称</option>
              </select>
              <select v-model="sortOrder" class="filter-select">
                <option value="desc">倒序</option>
                <option value="asc">正序</option>
              </select>
          </div>
          
          <!-- 创建项目表单 -->
          <div v-if="isCreating" class="create-project-form">
            <div class="form-group">
              <label>项目名称</label>
              <input v-model="newProject.name" type="text" placeholder="请输入项目名称" class="form-input">
            </div>
            <div class="form-group">
              <label>项目简介</label>
              <textarea v-model="newProject.description" placeholder="请输入项目简介" class="form-textarea"></textarea>
            </div>
            <div class="form-group">
              <label>项目位置</label>
              <div class="location-info">
                <span v-if="newProject.location" class="location-value">
                  {{ newProject.location }} ({{ newProject.lng.toFixed(6) }}, {{ newProject.lat.toFixed(6) }})
                </span>
                <span v-else class="location-hint">
                  请在地图上点击选择位置
                </span>
              </div>
            </div>
            <div class="form-group">
              <label>负责飞手</label>
              <select v-model="newProject.pilotId" class="form-input">
                <option value="">暂不分配</option>
                <option v-for="pilot in pilots" :key="pilot.id" :value="pilot.id">{{ pilot.name }}</option>
              </select>
            </div>
            <div class="form-actions">
              <button class="submit-btn" :disabled="!canSubmit" @click="createProject">
                创建项目
              </button>
            </div>
          </div>
          
          <!-- 编辑项目表单 -->
          <div v-if="isEditing" class="create-project-form">
            <div class="form-group">
              <label>项目名称</label>
              <input v-model="editingTask.name" type="text" placeholder="请输入项目名称" class="form-input">
            </div>
            <div class="form-group">
              <label>项目简介</label>
              <textarea v-model="editingTask.description" placeholder="请输入项目简介" class="form-textarea"></textarea>
            </div>
            <div class="form-group">
              <label>项目位置</label>
              <div class="location-info">
                <span v-if="editingTask.location" class="location-value">
                  {{ editingTask.location }} ({{ editingTask.lng.toFixed(6) }}, {{ editingTask.lat.toFixed(6) }})
                </span>
                <span v-else class="location-hint">
                  请在地图上点击选择位置
                </span>
              </div>
            </div>
            <div class="form-group">
              <label>负责飞手</label>
              <select v-model="editingTask.pilotId" class="form-input">
                <option :value="null">暂不分配</option>
                <option v-for="pilot in pilots" :key="pilot.id" :value="pilot.id">{{ pilot.name }}</option>
              </select>
            </div>
            <div class="form-actions">
              <button class="submit-btn" :disabled="!editingTask.name.trim()" @click="saveEdit">
                保存修改
              </button>
              <button class="cancel-btn" @click="cancelEdit">
                取消
              </button>
            </div>
          </div>
          
          <div v-if="!isCreating && !isEditing" class="task-cards">
            <div 
              v-for="task in filteredTasks" 
              :key="task.id" 
              class="task-card"
              :class="{ selected: selectedTask?.id === task.id }"
            >
              <div class="task-header">
                <div class="task-status" :class="task.status">
                  {{ task.status === 'pending' ? '待执行' : task.status === 'processing' ? '执行中' : '已完成' }}
                </div>
                <h3>{{ task.name }}</h3>
                <div class="task-actions">
                  <div class="more-btn" @click.stop="toggleMoreMenu(task.id)">
                    <span>⋮</span>
                  </div>
                  <div v-if="showMoreMenu === task.id" class="more-menu">
                    <div class="menu-item" @click.stop="editTask(task)">编辑</div>
                    <div class="menu-item delete" @click.stop="deleteTask(task.id)">删除</div>
                  </div>
                </div>
              </div>
              <p>{{ task.description }}</p>
              <div class="task-meta">
                <span>{{ task.time }}</span>
                <span>{{ task.location }}</span>
              </div>
              <div class="task-arrow" @click="selectTask(task)">→</div>
            </div>
          </div>
        </div>
        
        <!-- 拖拽调整宽度手柄 -->
        <div class="resize-handle" @mousedown="startResize"></div>
      </div>

      <!-- 右侧内容区：地图 -->
      <div class="right-section">
        <!-- 地图视图 -->
        <div class="map-section">
          <DeviceMap 
            ref="mapRef"
            :device-data="deviceData" 
            :task-data="tasks"
            :selected-task-id="selectedTask?.id"
            :center="[116.40039, 39.90886]" 
            :zoom="14"
            :can-click-select="isCreating"
            @map-click="handleMapClick"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import TopNav from '@/components/TopNav.vue'
import DeviceMap from '@/components/DeviceMap.vue'
import { logCreateProject, logEditProject, logDeleteProject, logAssignTask } from '@/utils/logger'

const userStore = useUserStore()

// 路由相关
const router = useRouter()

// 任务列表相关
const selectedTask = ref(null)
const taskStatusFilter = ref('all')
const sortField = ref('time')
const sortOrder = ref('desc')

// 飞手列表
const pilots = ref([])

// 地图组件引用
const mapRef = ref(null)

// 加载飞手列表
const loadPilots = () => {
  const saved = localStorage.getItem('registered_users')
  if (saved) {
    try {
      const users = JSON.parse(saved)
      pilots.value = users.filter(u => u.role === 'pilot')
    } catch (e) {
      console.error('Failed to parse pilots:', e)
      pilots.value = getDefaultPilots()
    }
  } else {
    pilots.value = getDefaultPilots()
  }
}

const getDefaultPilots = () => {
  return [
    { id: 2, username: 'pilot001', name: '飞手小张', role: 'pilot' },
    { id: 3, username: 'pilot002', name: '飞手小李', role: 'pilot' }
  ]
}

// 从 localStorage 加载数据或使用默认数据
const loadTasks = () => {
  const saved = localStorage.getItem('tasks')
  if (saved) {
    try {
      return JSON.parse(saved)
    } catch (e) {
      console.error('Failed to parse saved tasks:', e)
    }
  }
  return [
    { id: 1, name: '设备巡检任务', status: 'pending', time: '2024-01-15 09:00', location: '北京市朝阳区', description: '对区域A的设备进行例行巡检，检查设备运行状态和数据采集情况。', lng: 116.48102, lat: 39.99037, pilotId: 2 },
    { id: 2, name: '数据采集任务', status: 'processing', time: '2024-01-15 10:30', location: '北京市海淀区', description: '采集区域B的地理数据和设备状态信息。', lng: 116.30546, lat: 39.99138, pilotId: 3 },
    { id: 3, name: '系统更新任务', status: 'pending', time: '2024-01-15 14:00', location: '北京市西城区', description: '对系统进行定期更新维护。', lng: 116.36787, lat: 39.91448, pilotId: null },
    { id: 4, name: '模型构建任务', status: 'completed', time: '2024-01-14 16:00', location: '北京市东城区', description: '完成城市三维模型的构建和优化。', lng: 116.40634, lat: 39.92295, pilotId: 2 },
    { id: 5, name: '影像处理任务', status: 'completed', time: '2024-01-14 11:00', location: '北京市丰台区', description: '处理区域C的影像数据。', lng: 116.28244, lat: 39.85862, pilotId: 3 },
    { id: 6, name: '设备维护任务', status: 'pending', time: '2024-01-16 08:00', location: '北京市石景山区', description: '对故障设备进行维护和修复。', lng: 116.18446, lat: 39.91448, pilotId: null },
    { id: 7, name: '数据同步任务', status: 'processing', time: '2024-01-15 09:30', location: '北京市通州区', description: '同步本地数据与云端数据。', lng: 116.65859, lat: 39.90697, pilotId: 2 },
    { id: 8, name: '航线规划任务', status: 'pending', time: '2024-01-16 10:00', location: '北京市顺义区', description: '规划新的飞行航线。', lng: 116.65272, lat: 40.12194, pilotId: 3 },
  ]
}

// 保存数据到 localStorage
const saveTasks = () => {
  localStorage.setItem('tasks', JSON.stringify(tasks.value))
}

// 创建项目相关
const isCreating = ref(false)
const newProject = ref({
  name: '',
  description: '',
  location: '',
  lng: 0,
  lat: 0,
  pilotId: ''
})

const canSubmit = computed(() => {
  return newProject.value.name.trim() && 
         newProject.value.location && 
         typeof newProject.value.lng === 'number' && !isNaN(newProject.value.lng) &&
         typeof newProject.value.lat === 'number' && !isNaN(newProject.value.lat)
})

const showCreatePanel = () => {
  isCreating.value = true
  newProject.value = {
    name: '',
    description: '',
    location: '',
    lng: 0,
    lat: 0,
    pilotId: ''
  }
}

const cancelCreate = () => {
  isCreating.value = false
  newProject.value = {
    name: '',
    description: '',
    location: '',
    lng: 0,
    lat: 0,
    pilotId: ''
  }
  // 清除地图上的标记
  if (mapRef.value) {
    mapRef.value.clearMarker()
  }
}

const handleMapClick = (lng, lat, location) => {
  if (isCreating.value) {
    newProject.value.lng = lng
    newProject.value.lat = lat
    newProject.value.location = location || '未知位置'
    // 在地图上显示选中位置的标记
    if (mapRef.value) {
      mapRef.value.addMarker(lng, lat)
    }
  }
}

const createProject = () => {
  if (!canSubmit.value) return
  
  const now = new Date()
  const timeStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  
  const newTask = {
    id: tasks.value.length + 1,
    name: newProject.value.name,
    status: 'pending',
    time: timeStr,
    location: newProject.value.location,
    description: newProject.value.description || '暂无简介',
    lng: newProject.value.lng,
    lat: newProject.value.lat,
    pilotId: newProject.value.pilotId ? parseInt(newProject.value.pilotId) : null
  }
  
  tasks.value.unshift(newTask)
  saveTasks()
  
  const currentUser = userStore.userInfo?.name || '管理员'
  logCreateProject(newProject.value.name, currentUser)
  
  if (newProject.value.pilotId) {
    const pilot = pilots.value.find(p => p.id === parseInt(newProject.value.pilotId))
    if (pilot) {
      logAssignTask(newProject.value.name, pilot.name, currentUser)
    }
  }
  
  cancelCreate()
}

const tasks = ref(loadTasks())

const filteredTasks = computed(() => {
  let result = tasks.value
  if (userStore.isPilot) {
    result = result.filter(task => task.pilotId === userStore.userInfo.id)
  }
  if (taskStatusFilter.value !== 'all') {
    result = result.filter(task => task.status === taskStatusFilter.value)
  }
  
  result = [...result].sort((a, b) => {
    let comparison = 0
    if (sortField.value === 'time') {
      comparison = new Date(a.time).getTime() - new Date(b.time).getTime()
    } else if (sortField.value === 'name') {
      comparison = a.name.localeCompare(b.name)
    }
    
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
  
  return result
})

const selectTask = (task) => {
  selectedTask.value = task
  if (userStore.isPilot) {
    router.push(`/pilot-task/${task.id}`)
  } else {
    router.push(`/project/${task.id}`)
  }
}

// 更多菜单相关
const showMoreMenu = ref(null)

const toggleMoreMenu = (taskId) => {
  if (showMoreMenu.value === taskId) {
    showMoreMenu.value = null
  } else {
    showMoreMenu.value = taskId
  }
}

// 点击其他地方关闭菜单
const closeMoreMenu = (e) => {
  const target = e.target
  if (!target.closest('.task-actions')) {
    showMoreMenu.value = null
  }
}

// 编辑任务
const isEditing = ref(false)
const editingTask = ref(null)

const editTask = (task) => {
  editingTask.value = { ...task }
  isEditing.value = true
  showMoreMenu.value = null
}

const saveEdit = () => {
  const index = tasks.value.findIndex(t => t.id === editingTask.value.id)
  if (index !== -1) {
    tasks.value[index] = { ...editingTask.value }
    saveTasks()
    
    const currentUser = userStore.userInfo?.name || '管理员'
    logEditProject(editingTask.value.name, currentUser)
    
    const originalTask = tasks.value.find(t => t.id === editingTask.value.id)
    if (originalTask && originalTask.pilotId !== editingTask.value.pilotId) {
      const pilot = pilots.value.find(p => p.id === editingTask.value.pilotId)
      if (pilot) {
        logAssignTask(editingTask.value.name, pilot.name, currentUser)
      }
    }
  }
  cancelEdit()
}

const cancelEdit = () => {
  isEditing.value = false
  editingTask.value = null
}

// 删除任务
const deleteTask = async (taskId) => {
  showMoreMenu.value = null
  
  const task = tasks.value.find(t => t.id === taskId)
  const taskName = task?.name || '未知项目'
  
  const confirmed = await new Promise((resolve) => {
    if (window.confirm('确定要删除这个项目吗？')) {
      resolve(true)
    } else {
      resolve(false)
    }
  })
  
  if (confirmed) {
    tasks.value = tasks.value.filter(t => t.id !== taskId)
    saveTasks()
    if (selectedTask.value?.id === taskId) {
      selectedTask.value = null
    }
    
    const currentUser = userStore.userInfo?.name || '管理员'
    logDeleteProject(taskName, currentUser)
  }
}

// 拖拽调整宽度相关
const panelWidth = ref(420)
const isResizing = ref(false)
const minWidth = 300
const maxWidth = 600

const startResize = (e) => {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

const onResize = (e) => {
  if (!isResizing.value) return
  const mainContent = document.querySelector('.main-content')
  if (!mainContent) return
  
  const containerRect = mainContent.getBoundingClientRect()
  const newWidth = e.clientX - containerRect.left
  
  if (newWidth >= minWidth && newWidth <= maxWidth) {
    panelWidth.value = newWidth
  }
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

// 设备数据
const deviceData = ref([
  { id: 1, name: '设备1', status: 'normal', lat: 39.90886, lng: 116.39739 },
  { id: 2, name: '设备2', status: 'normal', lat: 39.91086, lng: 116.40039 },
  { id: 3, name: '设备3', status: 'warning', lat: 39.90686, lng: 116.40239 },
  { id: 4, name: '设备4', status: 'normal', lat: 39.90486, lng: 116.39939 },
  { id: 5, name: '设备5', status: 'error', lat: 39.91286, lng: 116.39839 },
  { id: 6, name: '设备6', status: 'normal', lat: 39.90986, lng: 116.40339 },
  { id: 7, name: '设备7', status: 'normal', lat: 39.90786, lng: 116.40139 },
  { id: 8, name: '设备8', status: 'warning', lat: 39.91186, lng: 116.40439 },
])

onMounted(() => {
  document.addEventListener('click', closeMoreMenu)
  
  // 加载飞手列表
  loadPilots()
  
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      tasks.value = loadTasks()
      loadPilots()
    }
  })
  window.addEventListener('storage', (e) => {
    if (e.key === 'tasks') {
      tasks.value = loadTasks()
    }
    if (e.key === 'registered_users') {
      loadPilots()
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  document.removeEventListener('click', closeMoreMenu)
})
</script>

<style scoped>
.project-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 任务列表区域 */
.task-list-area {
  width: 100%;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.task-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-list-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.add-project-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.add-project-btn:hover {
  background: #3a7bc8;
}

.add-icon {
  font-size: 16px;
  font-weight: bold;
}

.cancel-btn {
  padding: 6px 12px;
  background: #f0f0f0;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

/* 任务列表页面的筛选器 */
.task-list-area .task-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  min-width: 0;
}

.task-list-area .filter-select {
  padding: 6px 20px 6px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 6px center;
  background-size: 12px;
  min-width: 0;
  flex-shrink: 1;
}

.task-list-area .filter-select:hover {
  border-color: #4A90E2;
}

.task-list-area .filter-select:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

/* 创建项目表单 */
.create-project-form {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.create-project-form .form-group {
  margin-bottom: 15px;
  flex-shrink: 0;
}

.create-project-form .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.create-project-form .form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.3s;
}

.create-project-form .form-input:focus {
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.create-project-form .form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.3s;
  resize: vertical;
  min-height: 80px;
}

.create-project-form .form-textarea:focus {
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.create-project-form .location-info {
  padding: 10px;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 12px;
}

.create-project-form .location-value {
  color: #4A90E2;
}

.create-project-form .location-hint {
  color: #999;
}

.create-project-form .form-actions {
  display: flex;
  gap: 10px;
}

.create-project-form .submit-btn {
  flex: 1;
  padding: 10px;
  background: #4A90E2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.3s;
}

.create-project-form .submit-btn:hover:not(:disabled) {
  background: #3a7bc8;
}

.create-project-form .submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.task-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f8fafc;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  border: 1px solid #e2e8f0;
}

.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #4A90E2;
}

.stat-text {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.task-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  min-width: 0;
  pointer-events: auto;
  z-index: 1;
}

.task-card:hover {
  background: #e8f4fd;
  border-color: #4A90E2;
}

/* 任务卡片中的状态标签 */
.task-card .task-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.task-card .task-status.pending {
  background: #FFF3E0;
  color: #FF9800;
}

.task-card .task-status.processing {
  background: #E3F2FD;
  color: #1976D2;
}

.task-card .task-status.completed {
  background: #E8F5E9;
  color: #4CAF50;
}

.task-card .task-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.task-card h3 {
  margin: 0;
  font-size: 14px;
  color: #333;
  white-space: normal;
  word-wrap: break-word;
  flex: 1;
}

.task-card p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  white-space: normal;
  word-wrap: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 任务卡片中的元信息 */
.task-card .task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 11px;
  color: #999;
}

.task-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  color: #ddd;
  transition: all 0.3s;
  cursor: pointer;
}

.task-card:hover .task-arrow {
  color: #4A90E2;
  transform: translateY(-50%) translateX(5px);
}

/* 更多操作按钮 */
.task-card .task-actions {
  position: relative;
  flex-shrink: 0;
}

.task-card .more-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 18px;
  color: #999;
}

.task-card .more-btn:hover {
  background: #e2e8f0;
  color: #666;
}

/* 更多菜单 */
.task-card .more-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 5px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 100px;
  z-index: 100;
}

.task-card .more-menu .menu-item {
  padding: 8px 16px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.task-card .more-menu .menu-item:hover {
  background: #f1f5f9;
}

.task-card .more-menu .menu-item.delete {
  color: #ef4444;
}

.task-card .more-menu .menu-item.delete:hover {
  background: #fef2f2;
}

/* 主内容区 */
.main-content {
  position: relative;
  padding: 80px 0 0;
  min-height: 100vh;
  display: flex;
  height: calc(100vh - 80px);
  min-width: 600px;
}

/* 左侧面板 */
.main-content .left-panel {
  flex: 1;
  min-width: 280px;
  max-width: 450px;
  background-color: #f1f5f9;
  transition: all 0.3s ease;
  padding: 15px;
  border-right: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
}

/* 拖拽调整宽度手柄 */
.resize-handle {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 6px;
  cursor: col-resize;
  background-color: transparent;
  transition: background-color 0.2s ease;
  z-index: 10;
}

.resize-handle:hover {
  background-color: #4A90E2;
}

.resize-handle:active {
  background-color: #3a7bc8;
}

.left-panel {
  position: relative;
  overflow: hidden;
}

/* 右侧内容区 */
.main-content .right-section {
  flex: 2;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  height: 100%;
  background-color: #ffffff;
}

/* 地图视图 */
.map-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>