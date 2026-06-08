<template>
  <div class="app-container">
    <TopNav />
    
    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 概览卡片 -->
      <section class="overview-cards">
        <OverviewCard title="在线项目" :value="8" unit="个" icon="🟢" icon-class="online" />
        <OverviewCard title="设备数量" :value="20" unit="台" icon="📱" icon-class="device" />
        <OverviewCard title="任务完成率" :value="100" unit="%" icon="✅" icon-class="complete" />
      </section>

      <!-- 状态列表 -->
      <StatusList :status-items="statusItems" />

      <!-- 实时监控图表 -->
      <MonitorChart title="实时监控" :data-points="monitorData" />

      <!-- 任务统计饼图 -->
      <section class="task-stats">
        <h3>任务统计</h3>
        <TaskStatsChart :stats="taskStats" />
      </section>

      <!-- 设备分布地图 -->
      <DeviceMap title="设备分布地图" :device-data="deviceData" :center="[116.40039, 39.90886]" :zoom="14" />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TopNav from '@/components/TopNav.vue'
import OverviewCard from '@/components/OverviewCard.vue'
import StatusList from '@/components/StatusList.vue'
import MonitorChart from '@/components/MonitorChart.vue'
import TaskStatsChart from '@/components/TaskStatsChart.vue'
import DeviceMap from '@/components/DeviceMap.vue'

const statusItems = ref([
  { status: 'online', text: '在线设备：15台' },
  { status: 'warning', text: '警告设备：2台' },
  { status: 'offline', text: '离线设备：3台' }
])

// 监控数据
const monitorData = ref([150, 120, 130, 110, 100, 90, 105, 95, 80, 90])

// 任务统计数据
const taskStats = ref({
  completed: 40,
  processing: 32,
  pending: 18,
  cancelled: 10
})

// 设备数据
const deviceData = ref([
  { id: 1, name: '设备1', status: 'normal', lat: 39.90886, lng: 116.39739 },
  { id: 2, name: '设备2', status: 'normal', lat: 39.91086, lng: 116.40039 },
  { id: 3, name: '设备3', status: 'normal', lat: 39.90686, lng: 116.40239 },
  { id: 4, name: '设备4', status: 'normal', lat: 39.90486, lng: 116.39939 },
  { id: 5, name: '设备5', status: 'normal', lat: 39.91286, lng: 116.39839 },
  { id: 6, name: '设备6', status: 'normal', lat: 39.90986, lng: 116.40339 },
  { id: 7, name: '设备7', status: 'normal', lat: 39.90786, lng: 116.40139 },
  { id: 8, name: '设备8', status: 'normal', lat: 39.91186, lng: 116.40439 },
  { id: 9, name: '设备9', status: 'normal', lat: 39.90586, lng: 116.40039 },
  { id: 10, name: '设备10', status: 'normal', lat: 39.91386, lng: 116.39939 },
  { id: 11, name: '设备11', status: 'normal', lat: 39.90886, lng: 116.40539 },
  { id: 12, name: '设备12', status: 'normal', lat: 39.90686, lng: 116.39839 },
  { id: 13, name: '设备13', status: 'normal', lat: 39.91086, lng: 116.40239 },
  { id: 14, name: '设备14', status: 'normal', lat: 39.90986, lng: 116.39939 },
  { id: 15, name: '设备15', status: 'normal', lat: 39.90786, lng: 116.40339 },
  { id: 16, name: '设备16', status: 'warning', lat: 39.90586, lng: 116.39839 },
  { id: 17, name: '设备17', status: 'warning', lat: 39.90986, lng: 116.40539 },
  { id: 18, name: '设备18', status: 'error', lat: 39.90486, lng: 116.39539 },
  { id: 19, name: '设备19', status: 'error', lat: 39.91286, lng: 116.40039 },
  { id: 20, name: '设备20', status: 'error', lat: 39.90886, lng: 116.40239 }
])
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  padding: 80px 15px 15px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 15px;
  grid-template-areas:
    "overview overview"
    "status chart"
    "task task"
    "map map";
  min-width: 0;
  overflow: hidden;
}

.overview-cards {
  grid-area: overview;
  display: flex;
  gap: 20px;
  width: 100%;
  justify-content: space-between;
  min-width: 0;
}

:deep(.status-list) {
  grid-area: status;
  width: 100%;
  min-width: 0;
}

:deep(.monitor-chart) {
  grid-area: chart;
  width: 100%;
  min-width: 0;
}

.task-stats {
  grid-area: task;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  min-width: 0;
}

:deep(.device-map) {
  grid-area: map;
  width: 100%;
  min-width: 0;
}

.task-stats h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 16px;
}

:deep(.device-map) {
  grid-area: map;
  width: 100%;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-areas:
      "overview overview overview"
      "status status chart"
      "task task task"
      "map map map";
  }
}

@media (max-width: 480px) {
  .main-content {
    grid-template-columns: 1fr;
    grid-template-areas:
      "overview"
      "status"
      "monitor"
      "task"
      "map";
  }

  .overview-cards {
    flex-direction: column;
  }
}
</style>
