<template>
  <div class="resource-container">
    <!-- 资源类型选择 -->
    <el-card class="type-card">
      <template #header>
        <h3>选择资源类型</h3>
      </template>

      <div class="type-grid">
        <div
          v-for="type in resourceTypes"
          :key="type.value"
          :class="['type-item', selectedType === type.value ? 'active' : '']"
          @click="selectedType = type.value"
        >
          <div class="type-icon">{{ type.icon }}</div>
          <div class="type-name">{{ type.label }}</div>
          <div class="type-desc">{{ type.desc }}</div>
        </div>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card">
      <template #header>
        <h3>输入知识点</h3>
      </template>

      <el-input
        v-model="inputText"
        type="textarea"
        :rows="4"
        :placeholder="getPlaceholder()"
      />

      <div class="input-actions">
        <el-button type="primary" size="large" @click="generate" :loading="loading">
          生成资源
        </el-button>
        <el-button size="large" @click="clear">清空</el-button>
      </div>
    </el-card>

    <!-- 生成结果 -->
    <el-card v-if="result" class="result-card">
      <template #header>
        <div class="result-header">
          <h3>生成结果</h3>
          <div class="result-actions">
            <el-button type="primary" size="small" @click="copyResult">复制内容</el-button>
            <el-button size="small" @click="downloadWord">下载Word</el-button>
          </div>
        </div>
      </template>

      <div class="result-content" v-html="renderMarkdown(result)"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { streamResource } from '../api'

const selectedType = ref('syllabus')
const inputText = ref('')
const result = ref('')
const loading = ref(false)
const studentId = ref('student_001')

const resourceTypes = [
  { value: 'syllabus', label: '教学大纲', icon: '📋', desc: '生成完整的课程教学大纲' },
  { value: 'ppt', label: 'PPT大纲', icon: '📊', desc: '生成PPT课件大纲' },
  { value: 'exercise', label: '练习题', icon: '✏️', desc: '生成配套练习题' },
  { value: 'mindmap', label: '思维导图', icon: '🗺️', desc: '生成知识点思维导图' },
  { value: 'review', label: '复习提纲', icon: '📝', desc: '生成期末复习提纲' }
]

function getPlaceholder() {
  const placeholders = {
    syllabus: '请输入课程名称或知识点，例如：机器学习基础',
    ppt: '请输入知识点，例如：神经网络原理',
    exercise: '请输入知识点，例如：Python函数定义',
    mindmap: '请输入知识点，例如：数据结构与算法',
    review: '请输入课程或知识点，例如：线性代数'
  }
  return placeholders[selectedType.value]
}

function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

async function generate() {
  if (!inputText.value.trim()) {
    ElMessage.warning('请输入知识点')
    return
  }

  loading.value = true
  result.value = ''

  try {
    const url = streamResource(selectedType.value, inputText.value, studentId.value)
    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
      if (event.data === '[DONE]') {
        eventSource.close()
        loading.value = false
        return
      }
      result.value += event.data
    }

    eventSource.onerror = () => {
      eventSource.close()
      loading.value = false
      if (!result.value) {
        ElMessage.error('生成失败，请重试')
      }
    }
  } catch (error) {
    loading.value = false
    ElMessage.error('生成失败：' + error.message)
  }
}

function clear() {
  inputText.value = ''
  result.value = ''
}

function copyResult() {
  navigator.clipboard.writeText(result.value)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

function downloadWord() {
  // TODO: 实现Word下载
  ElMessage.info('Word下载功能开发中')
}
</script>

<style scoped>
.resource-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.type-card h3,
.input-card h3,
.result-card h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.type-item {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.type-item:hover {
  background: #ecf5ff;
  border-color: #409EFF;
}

.type-item.active {
  background: #ecf5ff;
  border-color: #409EFF;
}

.type-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.type-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 12px;
  color: #909399;
}

.input-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.result-content {
  line-height: 1.8;
  color: #303133;
}

:deep(.result-content) h1,
:deep(.result-content) h2,
:deep(.result-content) h3 {
  margin: 16px 0 8px 0;
  color: #409EFF;
}

:deep(.result-content) p {
  margin: 8px 0;
}

:deep(.result-content) ul,
:deep(.result-content) ol {
  padding-left: 24px;
}

:deep(.result-content) code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

:deep(.result-content) pre {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

:deep(.result-content) pre code {
  background: none;
  color: inherit;
}
</style>
