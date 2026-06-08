<template>
  <div class="stat-card">
    <div class="stat-icon" :class="iconClass">
      <component :is="iconComponent" />
    </div>
    <div class="stat-info">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  iconComponent: {
    type: Object,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  label: {
    type: String,
    required: true
  },
  iconClass: {
    type: String,
    default: 'blue'
  }
})
</script>

<style lang="scss" scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;

    &.blue {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.green {
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    }

    &.orange {
      background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%);
    }

    &.purple {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      color: #666;
    }
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #333;
    }

    .stat-label {
      font-size: 14px;
      color: #999;
      margin-top: 4px;
    }
  }
}
</style>