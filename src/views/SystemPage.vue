<template>
  <div class="system-page">
    <TopNav />
    <div class="settings-content">
      <SystemConfigForm />
      <SecurityConfigForm />
      <PilotManagement />
      <LogManagement />
    </div>
  </div>
</template>

<script setup>
import TopNav from '@/components/TopNav.vue'
import SystemConfigForm from '@/components/SystemConfigForm.vue'
import SecurityConfigForm from '@/components/SecurityConfigForm.vue'
import PilotManagement from '@/components/PilotManagement.vue'
import LogManagement from '@/components/LogManagement.vue'
</script>

<style lang="scss" scoped>
.system-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 80px 20px 20px;

  .settings-content {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
}
</style>