<template>
  <section class="task-stats">
    <h3>{{ title }}</h3>
    <div class="pie-chart-container">
      <svg class="pie-chart" viewBox="0 0 200 200">
        <circle cx="100" cy="100" r="70" fill="none" stroke="#E0E0E0" stroke-width="20" />
        <circle 
          cx="100" cy="100" r="70" fill="none" 
          :stroke="colors.completed" stroke-width="20" 
          :stroke-dasharray="`${completedPercent * 4.4} 440`" stroke-dashoffset="0" 
        />
        <circle 
          cx="100" cy="100" r="70" fill="none" 
          :stroke="colors.processing" stroke-width="20" 
          :stroke-dasharray="`${processingPercent * 4.4} 440`" 
          :stroke-dashoffset="`-${completedPercent * 4.4}`" 
        />
        <circle 
          cx="100" cy="100" r="70" fill="none" 
          :stroke="colors.pending" stroke-width="20" 
          :stroke-dasharray="`${pendingPercent * 4.4} 440`" 
          :stroke-dashoffset="`-${(completedPercent + processingPercent) * 4.4}`" 
        />
        <circle 
          cx="100" cy="100" r="70" fill="none" 
          :stroke="colors.cancelled" stroke-width="20" 
          :stroke-dasharray="`${cancelledPercent * 4.4} 440`" 
          :stroke-dashoffset="`-${(completedPercent + processingPercent + pendingPercent) * 4.4}`" 
        />
        <text x="100" y="105" text-anchor="middle" font-size="24" font-weight="bold" fill="#333">{{ totalPercent }}%</text>
      </svg>
      
      <div class="legend">
        <div class="legend-item" v-for="(item, key) in legendItems" :key="key">
          <div class="legend-color" :class="key"></div>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '任务统计'
  },
  stats: {
    type: Object,
    default: () => ({
      completed: 40,
      processing: 32,
      pending: 16,
      cancelled: 12
    })
  },
  colors: {
    type: Object,
    default: () => ({
      completed: '#4CAF50',
      processing: '#4A90E2',
      pending: '#FF9800',
      cancelled: '#F44336'
    })
  }
})

const total = computed(() => {
  return props.stats.completed + props.stats.processing + props.stats.pending + props.stats.cancelled
})

const completedPercent = computed(() => (props.stats.completed / total.value) * 100)
const processingPercent = computed(() => (props.stats.processing / total.value) * 100)
const pendingPercent = computed(() => (props.stats.pending / total.value) * 100)
const cancelledPercent = computed(() => (props.stats.cancelled / total.value) * 100)
const totalPercent = computed(() => Math.round(completedPercent.value))

const legendItems = computed(() => ({
  completed: { label: `已完成 (${props.stats.completed})` },
  processing: { label: `进行中 (${props.stats.processing})` },
  pending: { label: `待处理 (${props.stats.pending})` },
  cancelled: { label: `已取消 (${props.stats.cancelled})` }
}))
</script>

<style scoped>
.task-stats {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.task-stats h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 16px;
}

.pie-chart-container {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 15px;
  justify-content: center;
}

.pie-chart {
  width: 150px;
  height: 150px;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-color.completed { background: #4CAF50; }
.legend-color.processing { background: #4A90E2; }
.legend-color.pending { background: #FF9800; }
.legend-color.cancelled { background: #F44336; }
</style>