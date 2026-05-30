import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// 对话API - 流式输出
export function streamChat(message, studentId = 'default', agentType = 'tutor') {
  const params = new URLSearchParams({ message, studentId, agentType })
  return `/api/chat/stream?${params.toString()}`
}

// 对话API - 非流式
export function sendMessage(message, studentId = 'default', agentType = 'tutor') {
  return api.post('/chat/send', { message, studentId, agentType })
}

// 获取智能体列表
export function getAgents() {
  return api.get('/chat/agents')
}

// 画像API
export function getProfile(studentId) {
  return api.get('/profile/get', { params: { studentId } })
}

export function updateProfile(studentId, message) {
  return api.post('/profile/update', { studentId, message })
}

export function getProfileContext(studentId) {
  return api.get('/profile/context', { params: { studentId } })
}

// 资源生成API - 流式
export function streamResource(type, input, studentId = 'default') {
  const params = new URLSearchParams({ input, studentId })
  return `/api/resource/${type}/stream?${params.toString()}`
}

// 资源生成API - 非流式
export function generateResource(type, input, studentId = 'default') {
  return api.post(`/resource/${type}`, { input, studentId })
}

// 学习路径API
export function generatePath(studentId, courseName) {
  return api.post('/path/generate', { studentId, courseName })
}

export function getPaths(studentId) {
  return api.get('/path/list', { params: { studentId } })
}

export function getActivePaths(studentId) {
  return api.get('/path/active', { params: { studentId } })
}

export default api
