<template>
  <div 
    class="task-card"
    :class="{ selected: isSelected }"
    @click="handleClick"
  >
    <div class="task-status" :class="task.status">
      {{ statusText }}
    </div>
    <h3>{{ task.name }}</h3>
    <p>{{ task.description }}</p>
    <div class="task-meta">
      <span>{{ task.time }}</span>
      <span>{{ task.location }}</span>
    </div>
    <div class="task-arrow">→</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  task: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const statusText = computed(() => {
  const statusMap = {
    pending: '待执行',
    processing: '执行中',
    completed: '已完成'
  }
  return statusMap[props.task.status] || props.task.status
})

const handleClick = () => {
  emit('select', props.task)
}
</script>

<style scoped>
.task-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.task-card:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.task-card.selected {
  border-color: #4A90E2;
  background: #f8fafc;
}

.task-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 10px;
}

.task-status.pending {
  background: rgba(255, 193, 7, 0.1);
  color: #FFC107;
}

.task-status.processing {
  background: rgba(63, 81, 181, 0.1);
  color: #3F51B5;
}

.task-status.completed {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.task-card h3 {
  margin: 10px 0;
  font-size: 16px;
  color: #333;
}

.task-card p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #999;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.task-arrow {
  float: right;
  color: #ccc;
  font-size: 16px;
}
</style>
