<template>
  <div class="exam-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409EFF">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalQuestions }}</div>
            <div class="stat-label">总答题数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.accuracy }}%</div>
            <div class="stat-label">正确率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.wrongCount }}</div>
            <div class="stat-label">错题数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #F56C6C">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.reviewCount }}</div>
            <div class="stat-label">待复习</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 知识点掌握度雷达图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <h3>知识点掌握度分析</h3>
          <el-select v-model="selectedCourse" placeholder="选择课程" @change="loadData">
            <el-option label="人工智能导论" value="人工智能导论" />
          </el-select>
        </div>
      </template>
      <div ref="radarChart" class="chart-container"></div>
    </el-card>

    <!-- 功能操作区 -->
    <el-row :gutter="20" class="action-row">
      <!-- 薄弱知识点 -->
      <el-col :span="12">
        <el-card class="action-card">
          <template #header>
            <h3>薄弱知识点 TOP5</h3>
          </template>
          <div class="weak-list">
            <div v-for="(item, index) in weakPoints" :key="index" class="weak-item">
              <div class="weak-rank">{{ index + 1 }}</div>
              <div class="weak-info">
                <div class="weak-name">{{ item.knowledgeName }}</div>
                <el-progress :percentage="item.masteryLevel" :color="getProgressColor(item.masteryLevel)" />
              </div>
            </div>
            <el-empty v-if="weakPoints.length === 0" description="暂无数据" />
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card class="action-card">
          <template #header>
            <h3>考前冲刺</h3>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="showMockExamDialog">
              <el-icon><Document /></el-icon>
              生成模拟试卷
            </el-button>
            <el-button type="warning" size="large" @click="showStudyPlanDialog">
              <el-icon><Calendar /></el-icon>
              生成冲刺计划
            </el-button>
            <el-button type="danger" size="large" @click="reviewWrongQuestions">
              <el-icon><Notebook /></el-icon>
              错题复习
            </el-button>
            <el-button type="success" size="large" @click="reviewToday">
              <el-icon><Clock /></el-icon>
              今日复习 ({{ stats.reviewCount }}题)
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模拟试卷对话框 -->
    <el-dialog v-model="mockExamVisible" title="生成模拟试卷" width="600px">
      <el-form :model="mockExamForm" label-width="100px">
        <el-form-item label="题目数量">
          <el-slider v-model="mockExamForm.questionCount" :min="10" :max="50" :step="5" show-stops />
        </el-form-item>
        <el-form-item label="重点覆盖">
          <el-checkbox v-model="mockExamForm.focusWeak">优先覆盖薄弱知识点</el-checkbox>
        </el-form-item>
      </el-form>
      <div v-if="mockExamContent" class="exam-preview" v-html="renderMarkdown(mockExamContent)"></div>
      <template #footer>
        <el-button @click="mockExamVisible = false">取消</el-button>
        <el-button type="primary" @click="generateMockExam" :loading="generating">
          {{ mockExamContent ? '重新生成' : '生成试卷' }}
        </el-button>
        <el-button v-if="mockExamContent" type="success" @click="downloadExam">
          下载试卷
        </el-button>
      </template>
    </el-dialog>

    <!-- 冲刺计划对话框 -->
    <el-dialog v-model="studyPlanVisible" title="考前冲刺计划" width="700px">
      <el-form :model="studyPlanForm" label-width="100px">
        <el-form-item label="距离考试">
          <el-input-number v-model="studyPlanForm.days" :min="1" :max="30" />
          <span style="margin-left: 10px">天</span>
        </el-form-item>
      </el-form>
      <div v-if="studyPlanContent" class="plan-preview" v-html="renderMarkdown(studyPlanContent)"></div>
      <template #footer>
        <el-button @click="studyPlanVisible = false">关闭</el-button>
        <el-button type="primary" @click="generateStudyPlan" :loading="generating">
          {{ studyPlanContent ? '重新生成' : '生成计划' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 错题本对话框 -->
    <el-dialog v-model="wrongQuestionVisible" title="错题本" width="800px">
      <div class="wrong-questions">
        <div v-for="(q, index) in wrongQuestions" :key="index" class="wrong-item">
          <div class="wrong-header">
            <el-tag type="danger">{{ q.questionType }}</el-tag>
            <span class="wrong-tags">{{ q.knowledgeTags }}</span>
          </div>
          <div class="wrong-question">{{ q.question }}</div>
          <div class="wrong-answers">
            <div class="your-answer">
              <span class="label">你的答案：</span>
              <span class="wrong">{{ q.studentAnswer }}</span>
            </div>
            <div class="correct-answer">
              <span class="label">正确答案：</span>
              <span class="correct">{{ q.correctAnswer }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="wrongQuestions.length === 0" description="暂无错题" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

// 使用CDN引入的全局echarts
const echarts = window.echarts
import { Edit, CircleCheck, Warning, Timer, Document, Calendar, Notebook, Clock } from '@element-plus/icons-vue'
import axios from 'axios'

const studentId = ref('student_001')
const selectedCourse = ref('人工智能导论')
const radarChart = ref(null)

// 统计数据
const stats = ref({
  totalQuestions: 0,
  correctQuestions: 0,
  accuracy: 0,
  averageMastery: 0,
  reviewCount: 0,
  wrongCount: 0
})

// 薄弱知识点
const weakPoints = ref([])

// 知识点掌握度
const masteryList = ref([])

// 模拟试卷
const mockExamVisible = ref(false)
const mockExamForm = ref({ questionCount: 20, focusWeak: true })
const mockExamContent = ref('')
const generating = ref(false)

// 冲刺计划
const studyPlanVisible = ref(false)
const studyPlanForm = ref({ days: 7 })
const studyPlanContent = ref('')

// 错题本
const wrongQuestionVisible = ref(false)
const wrongQuestions = ref([])

// 获取进度条颜色
function getProgressColor(level) {
  if (level >= 80) return '#67C23A'
  if (level >= 60) return '#E6A23C'
  return '#F56C6C'
}

// 渲染Markdown
function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

// 加载数据
async function loadData() {
  await Promise.all([
    loadStats(),
    loadWeakPoints(),
    loadMasteryList()
  ])
  renderRadarChart()
}

// 加载统计数据
async function loadStats() {
  try {
    const res = await axios.get('/api/exam/stats', {
      params: { studentId: studentId.value, courseName: selectedCourse.value }
    })
    stats.value = res.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 加载薄弱知识点
async function loadWeakPoints() {
  try {
    const res = await axios.get('/api/exam/weak-points', {
      params: { studentId: studentId.value, courseName: selectedCourse.value, limit: 5 }
    })
    weakPoints.value = res.data
  } catch (error) {
    console.error('加载薄弱知识点失败:', error)
  }
}

// 加载掌握度列表
async function loadMasteryList() {
  try {
    const res = await axios.get('/api/exam/mastery', {
      params: { studentId: studentId.value, courseName: selectedCourse.value }
    })
    masteryList.value = res.data
  } catch (error) {
    console.error('加载掌握度失败:', error)
  }
}

// 渲染雷达图
function renderRadarChart() {
  if (!radarChart.value || masteryList.value.length === 0) return

  const chart = echarts.init(radarChart.value)
  const indicator = masteryList.value.slice(0, 8).map(item => ({
    name: item.knowledgeName,
    max: 100
  }))
  const values = masteryList.value.slice(0, 8).map(item => item.masteryLevel)

  chart.setOption({
    tooltip: {},
    radar: {
      indicator: indicator,
      shape: 'circle'
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '掌握度',
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.2)'
        },
        lineStyle: {
          color: '#409EFF'
        }
      }]
    }]
  })
}

// 显示模拟试卷对话框
function showMockExamDialog() {
  mockExamVisible.value = true
  mockExamContent.value = ''
}

// 生成模拟试卷
async function generateMockExam() {
  generating.value = true
  try {
    const res = await axios.post('/api/exam/mock-exam', {
      studentId: studentId.value,
      courseName: selectedCourse.value,
      questionCount: mockExamForm.value.questionCount
    })
    mockExamContent.value = res.data.content
    ElMessage.success('试卷生成成功')
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
  } finally {
    generating.value = false
  }
}

// 下载试卷
function downloadExam() {
  // TODO: 实现PDF/Word下载
  ElMessage.info('下载功能开发中')
}

// 显示冲刺计划对话框
function showStudyPlanDialog() {
  studyPlanVisible.value = true
  studyPlanContent.value = ''
}

// 生成冲刺计划
async function generateStudyPlan() {
  generating.value = true
  try {
    const res = await axios.post('/api/exam/study-plan', {
      studentId: studentId.value,
      courseName: selectedCourse.value,
      days: studyPlanForm.value.days
    })
    studyPlanContent.value = res.data.plan
    ElMessage.success('计划生成成功')
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
  } finally {
    generating.value = false
  }
}

// 错题复习
async function reviewWrongQuestions() {
  try {
    const res = await axios.get('/api/exam/wrong-questions', {
      params: { studentId: studentId.value, courseName: selectedCourse.value }
    })
    wrongQuestions.value = res.data
    wrongQuestionVisible.value = true
  } catch (error) {
    ElMessage.error('加载错题失败')
  }
}

// 今日复习
async function reviewToday() {
  try {
    const res = await axios.get('/api/exam/review', {
      params: { studentId: studentId.value }
    })
    if (res.data.length === 0) {
      ElMessage.info('今日无待复习题目')
    } else {
      wrongQuestions.value = res.data
      wrongQuestionVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载复习题目失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.exam-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row .stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card h3 {
  margin: 0;
  font-size: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
}

.action-row .action-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
}

.weak-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.weak-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.weak-rank {
  width: 32px;
  height: 32px;
  background: #F56C6C;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.weak-info {
  flex: 1;
}

.weak-name {
  margin-bottom: 8px;
  font-weight: 500;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-actions .el-button {
  height: 60px;
  font-size: 16px;
}

.exam-preview,
.plan-preview {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 16px;
}

.wrong-questions {
  max-height: 500px;
  overflow-y: auto;
}

.wrong-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  border-left: 4px solid #F56C6C;
}

.wrong-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.wrong-tags {
  color: #909399;
  font-size: 13px;
}

.wrong-question {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.6;
}

.wrong-answers {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.your-answer .label,
.correct-answer .label {
  font-weight: 500;
  color: #606266;
}

.your-answer .wrong {
  color: #F56C6C;
}

.correct-answer .correct {
  color: #67C23A;
}
</style>
