<template>
  <div class="task-planning">
    <div class="planning-card">
      <h4>任务概览</h4>
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总任务</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待执行</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.processing }}</div>
          <div class="stat-label">执行中</div>
        </div>
      </div>
    </div>
    <div class="planning-card">
      <h4>近期任务</h4>
      <ul class="mini-task-list">
        <li v-for="task in tasks" :key="task.id">
          <span :class="['task-dot', task.status]"></span>
          <span>{{ task.name }}</span>
          <span class="task-time">{{ task.time }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    default: () => ({ total: 0, pending: 0, processing: 0 })
  },
  tasks: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.task-planning {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px;
}

.planning-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.planning-card h4 {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 16px 0;
}

.stats-row {
  display: flex;
  gap: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #4A90E2;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.mini-task-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.mini-task-list li {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.mini-task-list li:last-child {
  border-bottom: none;
}

.task-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 12px;
}

.task-dot.pending { background-color: #FF9800; }
.task-dot.processing { background-color: #4A90E2; }
.task-dot.completed { background-color: #4CAF50; }

.task-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}
</style>