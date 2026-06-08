<template>
  <div class="settings-card">
    <h3>安全设置</h3>
    <el-form :model="config" label-width="120px">
      <el-form-item label="密码有效期(天)">
        <el-input-number v-model="config.passwordExpire" :min="7" :max="365" />
      </el-form-item>
      <el-form-item label="登录失败次数">
        <el-input-number v-model="config.maxLoginAttempts" :min="3" :max="20" />
      </el-form-item>
      <el-form-item label="启用双因素认证">
        <el-switch v-model="config.twoFactorAuth" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const config = reactive({
  passwordExpire: 90,
  maxLoginAttempts: 5,
  twoFactorAuth: false,
})

const handleSave = () => {
  ElMessage.success('安全设置保存成功')
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