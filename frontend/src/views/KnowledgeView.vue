<template>
  <div class="knowledge-container">
    <!-- 课程选择 -->
    <el-card class="course-card">
      <template #header>
        <h3>课程知识库</h3>
      </template>

      <el-select v-model="selectedCourse" placeholder="选择课程" size="large" @change="loadKnowledge">
        <el-option v-for="course in courses" :key="course" :label="course" :value="course" />
      </el-select>
    </el-card>

    <!-- 知识点统计 -->
    <el-card v-if="selectedCourse" class="stats-card">
      <template #header>
        <h3>知识库统计</h3>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总知识点" :value="knowledgeList.length" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="章节数" :value="chapterCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均难度" :value="avgDifficulty" :precision="1" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="概念类" :value="conceptCount" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 知识点列表 -->
    <el-card v-if="selectedCourse" class="list-card">
      <template #header>
        <div class="card-header">
          <h3>知识点列表</h3>
          <el-input v-model="searchKeyword" placeholder="搜索知识点..." style="width: 300px"
            @input="filterKnowledge" clearable />
        </div>
      </template>

      <el-collapse v-model="activeChapters">
        <el-collapse-item v-for="(chapter, index) in chapters" :key="index"
          :title="'第' + chapter.no + '章：' + chapter.name" :name="chapter.no">
          <div v-for="k in chapter.knowledge" :key="k.id" class="knowledge-item">
            <div class="knowledge-header">
              <el-tag :type="getTagType(k.type)">{{ k.type }}</el-tag>
              <span class="knowledge-no">{{ k.knowledgeNo }}</span>
              <span class="knowledge-name">{{ k.knowledgeName }}</span>
              <el-rate v-model="k.difficulty" disabled :max="5" />
            </div>
            <div class="knowledge-content">{{ k.content }}</div>
            <div class="knowledge-keywords">
              <el-tag v-for="keyword in parseKeywords(k.keywords)" :key="keyword" size="small" type="info">
                {{ keyword }}
              </el-tag>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const courses = ref([])
const selectedCourse = ref('')
const knowledgeList = ref([])
const searchKeyword = ref('')
const activeChapters = ref([])

// 章节列表
const chapters = computed(() => {
  const chapterMap = new Map()
  const filtered = searchKeyword.value
    ? knowledgeList.value.filter(k =>
        k.knowledgeName.includes(searchKeyword.value) ||
        k.keywords?.includes(searchKeyword.value)
      )
    : knowledgeList.value

  filtered.forEach(k => {
    if (!chapterMap.has(k.chapterNo)) {
      chapterMap.set(k.chapterNo, {
        no: k.chapterNo,
        name: k.chapterName,
        knowledge: []
      })
    }
    chapterMap.get(k.chapterNo).knowledge.push(k)
  })

  return Array.from(chapterMap.values()).sort((a, b) => a.no - b.no)
})

// 统计数据
const chapterCount = computed(() => chapters.value.length)
const avgDifficulty = computed(() => {
  if (knowledgeList.value.length === 0) return 0
  const sum = knowledgeList.value.reduce((acc, k) => acc + (k.difficulty || 0), 0)
  return sum / knowledgeList.value.length
})
const conceptCount = computed(() =>
  knowledgeList.value.filter(k => k.type === '概念').length
)

function getTagType(type) {
  const types = { '概念': '', '算法': 'success', '应用': 'warning', '案例': 'danger' }
  return types[type] || 'info'
}

function parseKeywords(keywords) {
  if (!keywords) return []
  return keywords.split(',').map(k => k.trim())
}

async function loadCourses() {
  try {
    const res = await axios.get('/api/knowledge/courses')
    courses.value = res.data
    if (courses.value.length > 0) {
      selectedCourse.value = courses.value[0]
      loadKnowledge()
    }
  } catch (error) {
    console.error('加载课程失败:', error)
  }
}

async function loadKnowledge() {
  if (!selectedCourse.value) return
  try {
    const res = await axios.get('/api/knowledge/list', {
      params: { courseName: selectedCourse.value }
    })
    knowledgeList.value = res.data
  } catch (error) {
    console.error('加载知识点失败:', error)
  }
}

function filterKnowledge() {
  // 搜索由 computed 自动处理
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.knowledge-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-card h3,
.stats-card h3,
.list-card h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.knowledge-item {
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.knowledge-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.knowledge-no {
  font-weight: 500;
  color: #409EFF;
}

.knowledge-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.knowledge-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.knowledge-keywords {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
