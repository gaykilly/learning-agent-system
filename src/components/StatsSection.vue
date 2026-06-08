<template>
  <div class="stats-section">
    <div class="stats-grid">
      <div class="stats-card">
        <div class="stats-icon">📊</div>
        <div class="stats-data">
          <div class="stats-value">2,847</div>
          <div class="stats-label">数据采集量</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">📈</div>
        <div class="stats-data">
          <div class="stats-value">98.7%</div>
          <div class="stats-label">数据准确率</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">⚡</div>
        <div class="stats-data">
          <div class="stats-value">156</div>
          <div class="stats-label">设备在线</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">🔄</div>
        <div class="stats-data">
          <div class="stats-value">32</div>
          <div class="stats-label">同步任务</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 数据统计组件 - 展示关键数据指标
</script>

<style scoped>
.stats-section {
  padding: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  gap: 12px;
  background: #f8fafc;
}

.stats-icon {
  font-size: 28px;
}

.stats-data {
  flex: 1;
}

.stats-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stats-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>