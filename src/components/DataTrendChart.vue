<template>
  <div class="chart-card">
    <h3>{{ title }}</h3>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: '数据趋势'
  },
  data: {
    type: Object,
    default: () => ({
      legend: ['新增数据', '处理数据', '异常数据'],
      xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
      series: [
        [1200, 1320, 1010, 1340, 1900, 2300],
        [800, 920, 810, 940, 1400, 1800],
        [100, 120, 90, 110, 150, 180]
      ]
    })
  }
})

const chartRef = ref(null)
let chart = null

const initChart = () => {
  if (!chartRef.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  chart = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '月度数据统计',
    },
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: props.data.legend,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: props.data.xAxis,
    },
    yAxis: {
      type: 'value',
    },
    series: props.data.legend.map((name, index) => ({
      name,
      type: 'line',
      data: props.data.series[index],
    })),
  }
  chart.setOption(option)
}

const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})

watch(() => props.data, initChart, { deep: true })
</script>

<style lang="scss" scoped>
.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  h3 {
    font-size: 16px;
    margin: 0 0 16px 0;
    color: #333;
  }

  .chart-container {
    height: 400px;
  }
}
</style>