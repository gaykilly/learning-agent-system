<template>
  <div class="card" :class="{ [cardClass]: cardClass }">
    <div class="card-content">
      <div class="card-title">{{ title }}</div>
      <div class="card-value">{{ value }}</div>
      <div class="card-unit">{{ unit }}</div>
    </div>
    <div class="card-icon" :class="iconClass">{{ icon }}</div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: '📊'
  },
  iconClass: {
    type: String,
    default: ''
  },
  cardClass: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
  width: 30%;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.card-unit {
  font-size: 14px;
  color: #999;
  margin-left: 5px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.card-icon.online {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.card-icon.device {
  background: rgba(63, 81, 181, 0.1);
  color: #3F51B5;
}

.card-icon.complete {
  background: rgba(255, 193, 7, 0.1);
  color: #FFC107;
}

.card-icon.warning {
  background: rgba(255, 152, 0, 0.1);
  color: #FF9800;
}
</style>
