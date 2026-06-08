const LOGS_KEY = 'system_logs'
const LOG_UPDATE_EVENT = 'system_log_update'

const listeners = []

export function addLogListener(callback) {
  listeners.push(callback)
  return () => {
    const index = listeners.indexOf(callback)
    if (index > -1) {
      listeners.splice(index, 1)
    }
  }
}

function emitLogUpdate() {
  listeners.forEach(callback => {
    try {
      callback()
    } catch (e) {
      console.error('Error in log listener:', e)
    }
  })
}

function getClientIP() {
  return '127.0.0.1'
}

export function addLog(action, description, type = 'info', user = '系统', module = '系统设置', params = {}) {
  const logs = getLogs()
  
  const newLog = {
    id: Date.now(),
    timestamp: Date.now(),
    type,
    action,
    user,
    description,
    module,
    ip: getClientIP(),
    params: JSON.stringify(params),
    result: type === 'error' ? '失败' : '成功',
    status: type === 'error' ? 'failed' : 'success'
  }
  
  logs.unshift(newLog)
  
  if (logs.length > 500) {
    logs.pop()
  }
  
  localStorage.setItem(LOGS_KEY, JSON.stringify(logs))
  emitLogUpdate()
  
  return newLog
}

export function getLogs() {
  try {
    const stored = localStorage.getItem(LOGS_KEY)
    return stored ? JSON.parse(stored) : getDefaultLogs()
  } catch (e) {
    console.error('Error reading logs:', e)
    return getDefaultLogs()
  }
}

export function clearLogs() {
  localStorage.setItem(LOGS_KEY, JSON.stringify([]))
  emitLogUpdate()
}

export function searchLogs(filters) {
  let logs = getLogs()
  
  if (filters.user && filters.user.trim()) {
    logs = logs.filter(log => log.user.includes(filters.user))
  }
  
  if (filters.module && filters.module !== 'all') {
    logs = logs.filter(log => log.module === filters.module)
  }
  
  if (filters.status && filters.status !== 'all') {
    logs = logs.filter(log => log.status === filters.status)
  }
  
  if (filters.startTime) {
    logs = logs.filter(log => log.timestamp >= filters.startTime)
  }
  
  if (filters.endTime) {
    logs = logs.filter(log => log.timestamp <= filters.endTime)
  }
  
  if (filters.keyword && filters.keyword.trim()) {
    const keyword = filters.keyword.toLowerCase()
    logs = logs.filter(log => 
      log.action.toLowerCase().includes(keyword) ||
      log.description.toLowerCase().includes(keyword)
    )
  }
  
  return logs.sort((a, b) => b.timestamp - a.timestamp)
}

export function exportLogsToExcel(logs) {
  const headers = [
    { key: 'timestamp', label: '操作时间' },
    { key: 'user', label: '操作人' },
    { key: 'module', label: '操作模块' },
    { key: 'action', label: '操作类型' },
    { key: 'description', label: '操作内容' },
    { key: 'ip', label: '操作IP' },
    { key: 'params', label: '请求参数' },
    { key: 'result', label: '操作结果' }
  ]
  
  let csv = headers.map(h => h.label).join('\t') + '\n'
  
  logs.forEach(log => {
    const row = headers.map(h => {
      let value = log[h.key]
      if (h.key === 'timestamp') {
        value = formatDateTime(log.timestamp)
      }
      return `"${String(value).replace(/"/g, '""')}"`
    })
    csv += row.join('\t') + '\n'
  })
  
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `系统操作日志_${formatDate(Date.now())}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function formatDateTime(timestamp) {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

function formatDate(timestamp) {
  const date = new Date(timestamp)
  return `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`
}

function getDefaultLogs() {
  const now = Date.now()
  return [
    { id: 1, timestamp: now - 1000 * 60 * 5, type: 'success', action: '登录', user: '管理员', description: '用户成功登录系统', module: '系统设置', ip: '127.0.0.1', params: '{"username":"admin"}', result: '成功', status: 'success' },
    { id: 2, timestamp: now - 1000 * 60 * 15, type: 'info', action: '创建项目', user: '管理员', description: '创建新项目：无人机巡检任务', module: '项目管理', ip: '127.0.0.1', params: '{"name":"无人机巡检任务","location":"北京市朝阳区"}', result: '成功', status: 'success' },
    { id: 3, timestamp: now - 1000 * 60 * 30, type: 'info', action: '任务分配', user: '管理员', description: '将任务分配给飞手小张', module: '项目管理', ip: '127.0.0.1', params: '{"taskId":1,"pilotId":2}', result: '成功', status: 'success' },
    { id: 4, timestamp: now - 1000 * 60 * 45, type: 'success', action: '注册', user: '系统', description: '新用户注册成功：testuser', module: '系统设置', ip: '127.0.0.1', params: '{"username":"testuser","role":"pilot"}', result: '成功', status: 'success' },
    { id: 5, timestamp: now - 1000 * 60 * 60, type: 'error', action: '登录失败', user: '未知', description: '用户名或密码错误', module: '系统设置', ip: '127.0.0.1', params: '{"username":"wronguser"}', result: '失败', status: 'failed' },
    { id: 6, timestamp: now - 1000 * 60 * 90, type: 'info', action: '修改配置', user: '管理员', description: '更新系统配置参数', module: '系统设置', ip: '127.0.0.1', params: '{"setting":"value"}', result: '成功', status: 'success' },
    { id: 7, timestamp: now - 1000 * 60 * 120, type: 'success', action: '退出登录', user: '飞手小张', description: '用户安全退出系统', module: '系统设置', ip: '127.0.0.1', params: '{}', result: '成功', status: 'success' },
    { id: 8, timestamp: now - 1000 * 60 * 180, type: 'info', action: '启动直播', user: '飞手小李', description: '开始实时直播推流', module: '飞手控制台', ip: '127.0.0.1', params: '{"channel":"live1"}', result: '成功', status: 'success' },
  ]
}

export function logLogin(username, module = '系统设置') {
  addLog('登录', `用户 "${username}" 成功登录系统`, 'success', username, module, { username })
}

export function logLogout(username, module = '系统设置') {
  addLog('退出登录', `用户 "${username}" 退出系统`, 'success', username, module, { username })
}

export function logRegister(username, module = '系统设置') {
  addLog('注册', `新用户 "${username}" 注册成功`, 'success', username, module, { username })
}

export function logCreateProject(projectName, username, module = '项目管理') {
  addLog('创建项目', `创建项目：${projectName}`, 'info', username, module, { projectName })
}

export function logEditProject(projectName, username, module = '项目管理') {
  addLog('编辑项目', `修改项目：${projectName}`, 'info', username, module, { projectName })
}

export function logDeleteProject(projectName, username, module = '项目管理') {
  addLog('删除项目', `删除项目：${projectName}`, 'warning', username, module, { projectName })
}

export function logAssignTask(taskName, pilotName, username, module = '项目管理') {
  addLog('任务分配', `将任务 "${taskName}" 分配给 ${pilotName}`, 'info', username, module, { taskName, pilotName })
}

export function logAddUser(username, role, operator, module = '系统设置') {
  addLog('添加用户', `创建${role === 'pilot' ? '飞手' : role}用户：${username}`, 'info', operator, module, { username, role })
}

export function logEditUser(username, operator, module = '系统设置') {
  addLog('编辑用户', `修改用户信息：${username}`, 'info', operator, module, { username })
}

export function logDeleteUser(username, operator, module = '系统设置') {
  addLog('删除用户', `删除用户：${username}`, 'warning', operator, module, { username })
}

export function logStartLive(username, module = '飞手控制台') {
  addLog('启动直播', '开始实时视频直播', 'info', username, module, {})
}

export function logStopLive(username, module = '飞手控制台') {
  addLog('停止直播', '停止视频直播', 'info', username, module, {})
}