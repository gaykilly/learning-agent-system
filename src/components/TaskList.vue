<template>
  <div class="task-list-container">
    <div class="task-list-header">
      <h3>任务列表</h3>
      <span class="task-count">{{ tasks.length }} 个任务</span>
    </div>
    
    <div class="task-list-area">
      <div 
        v-for="task in tasks" 
        :key="task.id" 
        :class="{ selected: selectedTask === task.id }"
        class="task-card"
        @click="handleTaskClick(task)"
      >
        <div class="task-header">
          <span class="task-id">任务{{ task.id }}</span>
          <span :class="['status-badge', task.status.toLowerCase()]">
            {{ getStatusText(task.status) }}
          </span>
        </div>
        <div class="task-title">{{ task.name }}</div>
        <div class="task-info">
          <span class="task-date">{{ task.date }}</span>
          <span class="task-device">📦 {{ task.device }}</span>
        </div>
        <div class="task-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: task.progress + '%' }"></div>
          </div>
          <span class="progress-text">{{ task.progress }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  selectedTask: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['task-click'])

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待执行',
    'processing': '执行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status.toLowerCase()] || status
}

const handleTaskClick = (task) => {
  emit('task-click', task)
}
</script>

<style scoped>
.task-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.task-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.task-list-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.task-count {
  font-size: 13px;
  color: #999;
}

.task-list-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.task-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.task-card:hover {
  border-color: #4A90E2;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.15);
}

.task-card.selected {
  background: #e8f4fd;
  border-color: #4A90E2;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-id {
  font-size: 12px;
  color: #999;
}

.status-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.processing {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.completed {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.cancelled {
  background: #fce7f3;
  color: #db2777;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.task-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4A90E2, #60a5fa);
  border-radius: 3px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 36px;
  text-align: right;
}
</style>