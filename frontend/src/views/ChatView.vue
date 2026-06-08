<template>
  <div class="chat-container">
    <!-- 智能体选择 -->
    <div class="agent-selector">
      <el-select v-model="selectedAgent" placeholder="选择智能体" size="large">
        <el-option label="辅导智能体" value="tutor" />
        <el-option label="画像智能体" value="profile" />
        <el-option label="资源智能体" value="resource" />
        <el-option label="路径智能体" value="path" />
        <el-option label="评估智能体" value="eval" />
      </el-select>
      <el-tag type="info" class="agent-desc">{{ agentDescriptions[selectedAgent] }}</el-tag>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageList">
      <div v-for="(msg, index) in messages" :key="index"
        :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
          <el-avatar :size="40" :style="{ backgroundColor: msg.role === 'user' ? '#409EFF' : '#67C23A' }">
            {{ msg.role === 'user' ? '我' : 'AI' }}
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="message-item ai-message">
        <div class="message-avatar">
          <el-avatar :size="40" style="background-color: #67C23A">AI</el-avatar>
        </div>
        <div class="message-content">
          <div class="message-text typing">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入你的问题..."
        @keydown.enter.ctrl="sendMessage"
      />
      <el-button type="primary" size="large" @click="sendMessage" :loading="loading">
        发送 (Ctrl+Enter)
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import { streamChat } from '../api'

const selectedAgent = ref('tutor')
const inputMessage = ref('')
const messages = ref([])
const loading = ref(false)
const messageList = ref(null)
const studentId = ref('student_001')

const agentDescriptions = {
  tutor: '解答学习疑问，提供个性化辅导',
  profile: '了解你并构建学习画像',
  resource: '生成学习资源（大纲、PPT、练习题等）',
  path: '规划个性化学习路径',
  eval: '评估学习效果，提供改进建议'
}

// 渲染Markdown
function renderMarkdown(text) {
  if (!text) return ''
  return marked(text)
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  })
}

// 发送消息（流式）
async function sendMessage() {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    time: new Date().toLocaleTimeString()
  })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  // 创建AI消息占位
  const aiMessage = {
    role: 'ai',
    content: '',
    time: ''
  }
  messages.value.push(aiMessage)

  try {
    // 流式接收
    const url = streamChat(message, studentId.value, selectedAgent.value)
    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
      if (event.data === '[DONE]') {
        eventSource.close()
        loading.value = false
        aiMessage.time = new Date().toLocaleTimeString()
        return
      }
      aiMessage.content += event.data
      scrollToBottom()
    }

    eventSource.onerror = () => {
      eventSource.close()
      loading.value = false
      if (!aiMessage.content) {
        aiMessage.content = '抱歉，发生了错误，请重试。'
      }
      aiMessage.time = new Date().toLocaleTimeString()
    }
  } catch (error) {
    loading.value = false
    aiMessage.content = '抱歉，发生了错误：' + error.message
    aiMessage.time = new Date().toLocaleTimeString()
  }
}

onMounted(() => {
  // 欢迎消息
  messages.value.push({
    role: 'ai',
    content: `你好！我是**学习智能体助手**，很高兴为你服务。

你可以：
- 💬 和我聊天，我会了解你的学习情况
- 📚 让我生成学习资源（教学大纲、PPT、练习题等）
- 🗺️ 让我为你规划学习路径
- 📊 让我评估你的学习效果

请告诉我你想学习什么？`,
    time: new Date().toLocaleTimeString()
  })
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.agent-selector {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 16px;
}

.agent-desc {
  font-size: 13px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-content {
  align-items: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-text {
  background: #409EFF;
  color: #fff;
}

.ai-message .message-text {
  background: #f4f4f5;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.typing {
  display: flex;
  gap: 4px;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  padding: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}

.input-area .el-input {
  flex: 1;
}

:deep(.message-text) p {
  margin: 8px 0;
}

:deep(.message-text) code {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

:deep(.message-text) pre {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

:deep(.message-text) pre code {
  background: none;
  color: inherit;
}
</style>
