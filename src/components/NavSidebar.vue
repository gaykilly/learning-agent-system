<template>
  <div class="nav-sidebar">
    <div 
      v-for="item in navItems" 
      :key="item.key"
      :class="{ active: currentTab === item.key }"
      @click="handleNavClick(item.key)"
    >
      <span class="nav-icon">{{ item.icon }}</span>
      <span class="nav-text">{{ item.label }}</span>
      <span v-if="currentTab === item.key && showContent" class="nav-arrow">◀</span>
      <span v-else-if="currentTab === item.key" class="nav-arrow">▶</span>
    </div>
    
    <!-- 退出按钮 -->
    <div class="nav-item logout-btn" @click="handleLogout">
      <span class="nav-icon">🚪</span>
      <span class="nav-text">{{ logoutLabel }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  navItems: {
    type: Array,
    default: () => [
      { key: 'model', label: '地图模型', icon: '🗺️' },
      { key: 'task', label: '任务规划', icon: '📋' },
      { key: 'route', label: '航线规划', icon: '✈️' },
      { key: 'stats', label: '数据统计', icon: '📊' },
      { key: 'history', label: '历史影像', icon: '📷' },
    ]
  },
  currentTab: {
    type: String,
    default: 'model'
  },
  showContent: {
    type: Boolean,
    default: false
  },
  logoutLabel: {
    type: String,
    default: '退出项目'
  }
})

const emit = defineEmits(['nav-click', 'logout'])

const handleNavClick = (key) => {
  emit('nav-click', key)
}

const handleLogout = () => {
  emit('logout')
}
</script>

<style scoped>
.nav-sidebar {
  width: 80px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  padding: 10px 0;
  flex-shrink: 0;
  justify-content: flex-start;
  height: 100%;
  border-right: 1px solid #e2e8f0;
}

.nav-sidebar div {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
  position: relative;
}

.nav-sidebar div:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-sidebar div.active {
  background: #dbeafe;
  color: #1d4ed8;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 11px;
  text-align: center;
}

.nav-arrow {
  position: absolute;
  right: 4px;
  font-size: 10px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.logout-btn {
  margin-top: auto;
}

.logout-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}
</style>