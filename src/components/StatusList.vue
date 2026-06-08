<template>
  <section class="status-list">
    <h3>{{ title }}</h3>
    <ul>
      <li v-for="item in statusItems" :key="item.status">
        <span class="status-dot" :class="item.status"></span>
        <span class="status-text">{{ item.text }}</span>
      </li>
    </ul>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '设备状态'
  },
  statusItems: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.status-list {
  background: white;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.status-list h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
}

.status-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.status-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-list li:last-child {
  border-bottom: none;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.online {
  background: #4CAF50;
}

.status-dot.warning {
  background: #FF9800;
}

.status-dot.offline {
  background: #f44336;
}

.status-text {
  font-size: 14px;
  color: #666;
}
</style>
