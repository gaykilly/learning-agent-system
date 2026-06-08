<template>
  <div class="settings-card">
    <div class="card-header" @click="toggleExpand">
      <h3>系统操作日志</h3>
      <div class="header-right">
        <span class="log-count">{{ filteredLogs.length }} 条日志</span>
        <span class="expand-icon">{{ expanded ? '▼' : '▲' }}</span>
      </div>
    </div>
    <div class="card-content" v-show="expanded">
      <div class="toolbar">
        <div class="search-box">
          <input v-model="searchKeyword" type="text" placeholder="搜索日志..." class="search-input" @input="handleSearch" />
        </div>
        <div class="filter-box">
          <select v-model="filterUser" class="filter-select" @change="handleSearch">
            <option value="">全部操作人</option>
            <option v-for="user in uniqueUsers" :key="user" :value="user">{{ user }}</option>
          </select>
        </div>
        <div class="filter-box">
          <select v-model="filterModule" class="filter-select" @change="handleSearch">
            <option value="all">全部模块</option>
            <option value="系统设置">系统设置</option>
            <option value="项目管理">项目管理</option>
            <option value="飞手控制台">飞手控制台</option>
            <option value="实时监控">实时监控</option>
          </select>
        </div>
        <div class="filter-box">
          <select v-model="filterStatus" class="filter-select" @change="handleSearch">
            <option value="all">全部状态</option>
            <option value="success">成功</option>
            <option value="failed">失败</option>
          </select>
        </div>
        <div class="date-range">
          <input v-model="startDate" type="date" class="date-input" @change="handleSearch" />
          <span class="date-separator">至</span>
          <input v-model="endDate" type="date" class="date-input" @change="handleSearch" />
        </div>
        <button class="export-btn" @click="handleExport">导出Excel</button>
        <button class="clear-btn" @click="handleClear">清空日志</button>
      </div>
      <div class="table-container">
        <table class="log-table">
          <thead>
            <tr>
              <th>操作时间</th>
              <th>操作人</th>
              <th>操作模块</th>
              <th>操作类型</th>
              <th>操作内容</th>
              <th>操作IP</th>
              <th>请求参数</th>
              <th>操作结果</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in filteredLogs" :key="log.id">
              <td>{{ formatTime(log.timestamp) }}</td>
              <td>{{ log.user }}</td>
              <td>{{ log.module }}</td>
              <td><span :class="['log-tag', log.type]">{{ getTypeLabel(log.type) }}</span></td>
              <td class="log-desc">{{ log.description }}</td>
              <td>{{ log.ip }}</td>
              <td class="log-params" :title="log.params">{{ truncateParams(log.params) }}</td>
              <td><span :class="['result-tag', log.status]">{{ log.result }}</span></td>
            </tr>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="8" class="empty-row">暂无日志</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getLogs, clearLogs, addLogListener, searchLogs, exportLogsToExcel } from '@/utils/logger'

const expanded = ref(false)
const searchKeyword = ref('')
const filterUser = ref('')
const filterModule = ref('all')
const filterStatus = ref('all')
const startDate = ref('')
const endDate = ref('')
const logs = ref([])

const uniqueUsers = computed(() => {
  const users = new Set(logs.value.map(log => log.user))
  return Array.from(users).sort()
})

const filteredLogs = computed(() => {
  const filters = {
    keyword: searchKeyword.value,
    user: filterUser.value,
    module: filterModule.value,
    status: filterStatus.value,
    startTime: startDate.value ? new Date(startDate.value).getTime() : null,
    endTime: endDate.value ? new Date(endDate.value).getTime() + 24 * 60 * 60 * 1000 : null
  }
  return searchLogs(filters)
})

const loadLogs = () => {
  logs.value = getLogs()
}

const toggleExpand = () => {
  expanded.value = !expanded.value
}

const handleSearch = () => {
}

const handleExport = () => {
  exportLogsToExcel(filteredLogs.value)
}

const handleClear = () => {
  if (confirm('确定要清空所有日志吗？')) {
    clearLogs()
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

const getTypeLabel = (type) => {
  const labels = {
    info: '信息',
    success: '成功',
    warning: '警告',
    error: '错误'
  }
  return labels[type] || type
}

const truncateParams = (params) => {
  if (!params) return ''
  const maxLen = 30
  return params.length > maxLen ? params.substring(0, maxLen) + '...' : params
}

let removeListener = null

onMounted(() => {
  loadLogs()
  window.addEventListener('storage', loadLogs)
  removeListener = addLogListener(loadLogs)
})

onUnmounted(() => {
  window.removeEventListener('storage', loadLogs)
  if (removeListener) {
    removeListener()
  }
})
</script>

<style lang="scss" scoped>
.settings-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #fafafa;
    border-bottom: 1px solid #eee;
    cursor: pointer;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 10px;

      .log-count {
        font-size: 14px;
        color: #666;
      }

      .expand-icon {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .card-content {
    padding: 20px;

    .toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      margin-bottom: 20px;
      align-items: center;

      .search-box {
        .search-input {
          width: 200px;
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;

          &:focus {
            outline: none;
            border-color: #4A90E2;
          }
        }
      }

      .filter-box {
        .filter-select {
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;
          min-width: 120px;

          &:focus {
            outline: none;
            border-color: #4A90E2;
          }
        }
      }

      .date-range {
        display: flex;
        align-items: center;
        gap: 8px;

        .date-input {
          padding: 8px 10px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;
        }

        .date-separator {
          color: #999;
        }
      }

      .export-btn {
        padding: 8px 16px;
        background: #4A90E2;
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: background 0.2s;

        &:hover {
          background: #357ABD;
        }
      }

      .clear-btn {
        padding: 8px 16px;
        background: #f5f5f5;
        color: #666;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #eee;
          color: #333;
        }
      }
    }

    .table-container {
      max-height: 500px;
      overflow-y: auto;
      border: 1px solid #eee;
      border-radius: 4px;

      .log-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 14px;

        thead {
          position: sticky;
          top: 0;
          background: #fafafa;
        }

        th, td {
          padding: 12px 15px;
          text-align: left;
          border-bottom: 1px solid #eee;
          white-space: nowrap;
        }

        th {
          font-weight: 600;
          color: #666;
          background: #fafafa;
        }

        tbody tr:hover {
          background: #f9f9f9;
        }

        .log-tag {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;

          &.info {
            background: #E3F2FD;
            color: #1976D2;
          }

          &.success {
            background: #E8F5E9;
            color: #388E3C;
          }

          &.warning {
            background: #FFF8E1;
            color: #F57C00;
          }

          &.error {
            background: #FFEBEE;
            color: #C62828;
          }
        }

        .result-tag {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;

          &.success {
            background: #E8F5E9;
            color: #388E3C;
          }

          &.failed {
            background: #FFEBEE;
            color: #C62828;
          }
        }

        .log-desc {
          max-width: 200px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .log-params {
          max-width: 150px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-family: monospace;
          font-size: 12px;
          color: #666;
        }

        .empty-row {
          text-align: center;
          color: #999;
          padding: 40px;
        }
      }
    }
  }
}
</style>