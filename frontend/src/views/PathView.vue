<template>
  <div class="path-container">
    <!-- 生成学习路径 -->
    <el-card class="generate-card">
      <template #header>
        <h3>生成学习路径</h3>
      </template>

      <el-form :inline="true" class="path-form">
        <el-form-item label="课程名称">
          <el-input v-model="courseName" placeholder="例如：人工智能导论" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="generatePath" :loading="generating">
            生成路径
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成结果 -->
    <el-card v-if="currentPath" class="result-card">
      <template #header>
        <div class="card-header">
          <h3>{{ currentPath.title }}</h3>
          <el-tag type="success">进行中</el-tag>
        </div>
      </template>

      <p class="path-desc">{{ currentPath.description }}</p>

      <!-- 学习步骤 -->
      <div class="steps-container">
        <el-steps :active="currentStep" direction="vertical" :space="100">
          <el-step
            v-for="(step, index) in parsedSteps"
            :key="index"
            :title="step.title"
            :description="step.description"
          />
        </el-steps>
      </div>

      <!-- 推荐资源 -->
      <div class="resources-section">
        <h4>推荐学习资源</h4>
        <div class="resource-grid">
          <div v-for="(res, index) in parsedResources" :key="index" class="resource-item">
            <el-tag :type="getTagType(res.type)">{{ res.type }}</el-tag>
            <span class="resource-name">{{ res.name }}</span>
            <span class="resource-reason">{{ res.reason }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 历史路径 -->
    <el-card class="history-card">
      <template #header>
        <h3>学习路径历史</h3>
      </template>

      <el-table :data="paths" style="width: 100%">
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="title" label="路径标题" />
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.progress" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status === 'active' ? '进行中' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="viewPath(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { generatePath as apiGeneratePath, getPaths } from '../api'

const studentId = ref('student_001')
const courseName = ref('')
const generating = ref(false)
const currentPath = ref(null)
const currentStep = ref(0)
const paths = ref([])

const parsedSteps = computed(() => {
  if (!currentPath.value?.steps) return []
  try {
    return JSON.parse(currentPath.value.steps)
  } catch {
    return []
  }
})

const parsedResources = computed(() => {
  if (!currentPath.value?.recommendedResources) return []
  try {
    return JSON.parse(currentPath.value.recommendedResources)
  } catch {
    return []
  }
})

function getTagType(type) {
  const types = {
    '文档': '',
    '视频': 'success',
    '练习': 'warning',
    '案例': 'danger',
    '项目': 'info'
  }
  return types[type] || ''
}

async function generatePath() {
  if (!courseName.value.trim()) {
    ElMessage.warning('请输入课程名称')
    return
  }

  generating.value = true
  try {
    const res = await apiGeneratePath(studentId.value, courseName.value)
    currentPath.value = res.data
    ElMessage.success('学习路径已生成')
    loadPaths()
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
  } finally {
    generating.value = false
  }
}

async function loadPaths() {
  try {
    const res = await getPaths(studentId.value)
    paths.value = res.data
  } catch (error) {
    console.error('加载路径失败:', error)
  }
}

function viewPath(path) {
  currentPath.value = path
}

onMounted(() => {
  loadPaths()
})
</script>

<style scoped>
.path-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.generate-card h3,
.result-card h3,
.history-card h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.path-form {
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.path-desc {
  color: #606266;
  margin-bottom: 24px;
  line-height: 1.6;
}

.steps-container {
  margin-bottom: 32px;
}

.resources-section h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.resource-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.resource-name {
  font-weight: 500;
  color: #303133;
}

.resource-reason {
  color: #909399;
  font-size: 13px;
}
</style>
