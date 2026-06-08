<template>
  <div class="model-section">
    <div class="model-list">
      <div class="model-item" v-for="model in models" :key="model.id">
        <div class="model-icon">🗺️</div>
        <div class="model-info">
          <div class="model-name">{{ model.name }}</div>
          <div class="model-date">{{ model.date }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  models: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.model-section {
  padding: 8px;
}

.model-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.model-item:hover {
  background: #f8fafc;
}

.model-icon {
  font-size: 24px;
}

.model-info {
  flex: 1;
}

.model-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.model-date {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>