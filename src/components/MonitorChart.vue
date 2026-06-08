<template>
  <section class="monitor-chart">
    <h3>{{ title }}</h3>
    <div class="chart-container">
      <svg width="100%" height="200" viewBox="0 0 400 200">
        <defs>
          <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" :style="{ stopColor: lineColor, stopOpacity: 0.3 }" />
            <stop offset="100%" :style="{ stopColor: lineColor, stopOpacity: 0.1 }" />
          </linearGradient>
        </defs>
        <path :d="linePath" fill="none" :stroke="lineColor" stroke-width="2" />
        <path :d="areaPath" fill="url(#areaGradient)" />
        <!-- 网格线 -->
        <line x1="0" y1="200" x2="400" y2="200" stroke="#e0e0e0" />
        <line x1="0" y1="150" x2="400" y2="150" stroke="#e0e0e0" />
        <line x1="0" y1="100" x2="400" y2="100" stroke="#e0e0e0" />
        <line x1="0" y1="50" x2="400" y2="50" stroke="#e0e0e0" />
        <!-- X轴标签 -->
        <text x="10" y="20" font-size="12" fill="#999">00:00</text>
        <text x="90" y="20" font-size="12" fill="#999">06:00</text>
        <text x="190" y="20" font-size="12" fill="#999">12:00</text>
        <text x="290" y="20" font-size="12" fill="#999">18:00</text>
        <text x="390" y="20" font-size="12" fill="#999">24:00</text>
        <!-- Y轴标签 -->
        <text x="10" y="50" font-size="12" fill="#999">250</text>
        <text x="10" y="100" font-size="12" fill="#999">187</text>
        <text x="10" y="150" font-size="12" fill="#999">125</text>
        <text x="10" y="200" font-size="12" fill="#999">0</text>
      </svg>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '实时监控'
  },
  lineColor: {
    type: String,
    default: '#4A90E2'
  },
  dataPoints: {
    type: Array,
    default: () => [150, 120, 130, 100, 80, 90]
  }
})

const generatePath = (includeArea = false) => {
  const points = props.dataPoints
  if (!points || points.length === 0) return ''
  
  const scaleX = 400 / (points.length - 1)
  const scaleY = 150 / 250 // 250 is max value
  
  let path = `M0,${200 - points[0] * scaleY}`
  
  for (let i = 1; i < points.length; i++) {
    const x = i * scaleX
    const y = 200 - points[i] * scaleY
    const prevX = (i - 1) * scaleX
    const prevY = 200 - points[i - 1] * scaleY
    const cpX = (prevX + x) / 2
    path += ` Q${cpX},${prevY} ${x},${y}`
  }
  
  if (includeArea) {
    path += ' L400,200 L0,200 Z'
  }
  
  return path
}

const linePath = computed(() => generatePath(false))
const areaPath = computed(() => generatePath(true))
</script>

<style scoped>
.monitor-chart {
  width: 100%;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.monitor-chart h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 200px;
}
</style>