<template>
  <div class="pilot-container">
    <TopNav />

    <main class="main-content">
      <div class="pilot-content">
        <div class="info-panel">
          <div class="info-card">
            <h4>📡 当前状态</h4>
            <p class="status-value">{{ isLive ? '直播中' : '待机中' }}</p>
          </div>
          <div class="info-card">
            <h4>🛩️ 设备信息</h4>
            <p class="info-text">设备ID: UAC-001</p>
            <p class="info-text">型号: M300 RTK</p>
          </div>
          <div class="info-card">
            <h4>📍 当前位置</h4>
            <p class="info-text">经度: 116.39739</p>
            <p class="info-text">纬度: 39.90886</p>
          </div>
          <div class="info-card">
            <h4>✈️ 飞行状态</h4>
            <p class="status-value" :class="flightStatusClass">{{ flightStatus }}</p>
            <p class="info-text">飞行时长: {{ flightTime }}</p>
          </div>
          <div class="info-card">
            <h4>🔋 电池状态</h4>
            <p class="info-text">电量: {{ batteryLevel }}%</p>
            <p class="info-text">电压: {{ batteryVoltage }}V</p>
            <p class="info-text">温度: {{ batteryTemp }}°C</p>
          </div>
          <div class="info-card">
            <h4>🌡️ 环境数据</h4>
            <p class="info-text">温度: {{ envTemp }}°C</p>
            <p class="info-text">湿度: {{ envHumidity }}%</p>
            <p class="info-text">风速: {{ envWind }}m/s</p>
          </div>
        </div>

        <div class="live-panel">
          <h3>实时直播监控</h3>
          <LiveSection />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import TopNav from '@/components/TopNav.vue'
import LiveSection from '@/components/LiveSection.vue'

const userStore = useUserStore()
const isLive = ref(false)

const flightStatus = ref('悬停')
const flightTime = ref('00:15:32')
const batteryLevel = ref(78)
const batteryVoltage = ref(52.3)
const batteryTemp = ref(28)
const envTemp = ref(24)
const envHumidity = ref(65)
const envWind = ref(3.2)

const flightStatusClass = computed(() => {
  const status = flightStatus.value
  if (status === '飞行中') return 'status-flying'
  if (status === '悬停') return 'status-hover'
  if (status === '起飞') return 'status-takeoff'
  if (status === '降落') return 'status-landing'
  return ''
})

onMounted(() => {
  if (!userStore.isPilot && !userStore.isAdmin) {
    window.location.href = '/login'
  }
})
</script>

<style scoped>
.pilot-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  color: #333333;
}

.main-content {
  padding: 80px 20px 20px;
}

.pilot-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.live-panel {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.live-panel h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #4A90E2;
}

.info-panel {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 0;
}

.info-card h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #4A90E2;
}

.status-value {
  font-size: 24px;
  font-weight: bold;
  color: #52c41a;
  margin: 10px 0 0 0;
}

.status-flying {
  color: #1890ff;
}

.status-hover {
  color: #52c41a;
}

.status-takeoff {
  color: #faad14;
}

.status-landing {
  color: #fa8c16;
}

.info-text {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}
</style>