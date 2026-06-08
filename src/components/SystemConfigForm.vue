<template>
  <div class="settings-card">
    <h3>系统配置</h3>
    <el-form :model="config" label-width="120px">
      <el-form-item label="系统名称">
        <el-input v-model="config.name" />
      </el-form-item>
      <el-form-item label="系统描述">
        <el-textarea v-model="config.description" rows="3" />
      </el-form-item>
      <el-form-item label="自动保存">
        <el-switch v-model="config.autoSave" />
      </el-form-item>
      <el-form-item label="日志级别">
        <el-select v-model="config.logLevel">
          <el-option label="DEBUG" value="debug" />
          <el-option label="INFO" value="info" />
          <el-option label="WARN" value="warn" />
          <el-option label="ERROR" value="error" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const config = reactive({
  name: '管理系统',
  description: '数据管理与分析平台',
  autoSave: true,
  logLevel: 'info',
})

const handleSave = () => {
  ElMessage.success('系统配置保存成功')
}
</script>

<style lang="scss" scoped>
.settings-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  h3 {
    font-size: 16px;
    margin: 0 0 20px 0;
    color: #333;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-button) {
    margin-top: 8px;
  }
}
</style>