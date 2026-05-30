<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>学习画像</h3>
          <el-button type="primary" @click="refreshProfile">刷新画像</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生ID">{{ profile.studentId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.studentName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile.major || '待设置' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ profile.grade || '待设置' }}</el-descriptions-item>
        <el-descriptions-item label="知识基础">
          <el-tag :type="getLevelType(profile.knowledgeLevel)">{{ profile.knowledgeLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认知风格">{{ profile.cognitiveStyle }}</el-descriptions-item>
        <el-descriptions-item label="学习偏好">{{ profile.learningPreference }}</el-descriptions-item>
        <el-descriptions-item label="易错点">{{ profile.weakPoints }}</el-descriptions-item>
        <el-descriptions-item label="学习目标" :span="2">{{ profile.learningGoal }}</el-descriptions-item>
        <el-descriptions-item label="学习进度" :span="2">
          <el-progress :percentage="getProgress()" :stroke-width="20" :text-inside="true" />
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 画像维度可视化 -->
    <el-card class="dimension-card">
      <template #header>
        <h3>画像维度分析</h3>
      </template>

      <div class="dimension-grid">
        <div v-for="(dim, index) in dimensions" :key="index" class="dimension-item">
          <div class="dimension-icon" :style="{ backgroundColor: dim.color }">
            {{ dim.icon }}
          </div>
          <div class="dimension-info">
            <h4>{{ dim.name }}</h4>
            <p>{{ dim.value }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 通过对话更新画像 -->
    <el-card class="update-card">
      <template #header>
        <h3>对话更新画像</h3>
      </template>

      <p class="tip">通过自然对话，让智能体了解你并自动更新画像</p>

      <div class="chat-update">
        <el-input
          v-model="chatMessage"
          type="textarea"
          :rows="3"
          placeholder="例如：我是计算机专业大三的学生，正在学习人工智能..."
        />
        <el-button type="primary" @click="updateFromChat" :loading="updating">
          发送并更新画像
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '../api'

const studentId = ref('student_001')
const profile = ref({
  studentId: '',
  studentName: '',
  major: '',
  grade: '',
  knowledgeLevel: '待评估',
  cognitiveStyle: '待分析',
  learningPreference: '待了解',
  weakPoints: '待发现',
  learningGoal: '待设定',
  learningProgress: '刚开始'
})
const chatMessage = ref('')
const updating = ref(false)

const dimensions = computed(() => [
  { name: '知识基础', value: profile.value.knowledgeLevel, icon: '📚', color: '#409EFF' },
  { name: '认知风格', value: profile.value.cognitiveStyle, icon: '🧠', color: '#67C23A' },
  { name: '学习偏好', value: profile.value.learningPreference, icon: '💡', color: '#E6A23C' },
  { name: '易错点', value: profile.value.weakPoints, icon: '⚠️', color: '#F56C6C' },
  { name: '学习目标', value: profile.value.learningGoal, icon: '🎯', color: '#909399' },
  { name: '学习进度', value: profile.value.learningProgress, icon: '📈', color: '#b37feb' }
])

function getLevelType(level) {
  if (level === '高级') return 'success'
  if (level === '中级') return 'warning'
  if (level === '初级') return 'info'
  return ''
}

function getProgress() {
  const progress = profile.value.learningProgress
  if (progress === '刚开始') return 10
  if (progress === '初级') return 30
  if (progress === '中级') return 60
  if (progress === '高级') return 90
  return 50
}

async function refreshProfile() {
  try {
    const res = await getProfile(studentId.value)
    profile.value = res.data
    ElMessage.success('画像已刷新')
  } catch (error) {
    ElMessage.error('获取画像失败：' + error.message)
  }
}

async function updateFromChat() {
  if (!chatMessage.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  updating.value = true
  try {
    const res = await updateProfile(studentId.value, chatMessage.value)
    profile.value = res.data
    chatMessage.value = ''
    ElMessage.success('画像已更新')
  } catch (error) {
    ElMessage.error('更新失败：' + error.message)
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  refreshProfile()
})
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.dimension-card {
  background: #fff;
}

.dimension-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.dimension-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.dimension-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #606266;
}

.dimension-info p {
  margin: 0;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.update-card {
  background: #fff;
}

.update-card h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.tip {
  color: #909399;
  margin-bottom: 16px;
}

.chat-update {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-update .el-button {
  align-self: flex-end;
}
</style>
