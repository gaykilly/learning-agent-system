<template>
  <div class="live-section">
    <!-- 控制按钮 -->
    <div class="control-buttons">
      <button 
        :class="{ active: isPushing, disabled: isPulling }"
        @click="togglePush"
      >
        {{ isPushing ? '⏹ 停止推流' : '▶ 启动推流' }}
      </button>
      <button 
        :class="{ active: isPulling }"
        @click="togglePull"
      >
        {{ isPulling ? '⏹ 停止拉流' : '▶ 启动拉流+识别+直播' }}
      </button>
    </div>

    <!-- 视频容器 -->
    <div class="video-wrapper">
      <div class="video-container">
        <video 
          v-show="!useBoxedStream"
          ref="videoPlayer" 
          class="video-player"
          controls 
          autoplay
          playsinline
        ></video>
        <img 
          v-show="useBoxedStream && isStreaming"
          ref="streamImage"
          class="video-player"
          :src="boxedStreamUrl"
        />
        
        <div v-if="!isStreaming" class="no-stream">
          <div class="no-stream-icon">📡</div>
          <p>{{ streamError || '等待推流中...' }}</p>
          <p class="stream-hint">请确保推流服务已启动</p>
        </div>
      </div>
    </div>

    <!-- 直播状态 -->
    <div class="stream-status" v-if="isStreaming">
      <span class="status-dot active"></span>
      <span>直播中</span>
      <span class="duration">{{ streamDuration }}</span>
    </div>

    <!-- 日志信息 -->
    <div class="log-panel" v-if="logs.length > 0">
      <div class="log-header">
        <h4>操作日志</h4>
        <span class="log-count">{{ logs.length }} 条日志</span>
      </div>
      <div class="logs">
        <div v-for="(log, index) in logs" :key="index" :class="['log-item', `log-${log.type}`]">
          <span class="log-time">[{{ log.time }}]</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const videoPlayer = ref(null)
const streamImage = ref(null)
const isPushing = ref(false)
const isPulling = ref(false)
const isStreaming = ref(false)
const streamError = ref('')
const streamDuration = ref('00:00:00')
const logs = ref([])
const useBoxedStream = ref(true)
const boxedStreamUrl = ref('http://127.0.0.1:5003/api/live/boxed/mjpeg')

let flvPlayer = null
let startTime = null
let durationTimer = null

const addLog = (message, type = 'info') => {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  logs.value.unshift({ time, message, type })
  if (logs.value.length > 50) {
    logs.value.pop()
  }
}

const togglePush = async () => {
  if (isPulling.value) return

  if (isPushing.value) {
    try {
      await fetch("http://127.0.0.1:5003/api/pull/stop", { method: "POST" })
      isPushing.value = false
      addLog('推流已停止')
    } catch (e) {
      console.error('Error stopping push:', e)
      addLog('停止推流失败')
    }
  } else {
    try {
      await fetch("http://127.0.0.1:5003/api/pull/start", { method: "POST" })
      isPushing.value = true
      addLog('推流已启动')
    } catch (e) {
      console.error('Error starting push:', e)
      streamError.value = '无法连接推流服务，请确保后端服务已启动'
      addLog('启动推流失败：后端服务未运行')
    }
  }
}

const togglePull = async () => {
  if (isPulling.value) {
    try {
      await fetch("http://127.0.0.1:5003/api/pull/stop", { method: "POST" })
    } catch (e) {
      console.error('Error stopping pull:', e)
    }
    stopLive()
    isPulling.value = false
    addLog('拉流已停止')
  } else {
    try {
      await fetch("http://127.0.0.1:5003/api/pull/start", { method: "POST" })
      isPulling.value = true
      streamError.value = ''
      addLog('拉流已启动，正在连接直播...')
      setTimeout(playLive, 1000)
    } catch (e) {
      console.error('Error starting pull:', e)
      streamError.value = '无法连接拉流服务，请确保后端服务已启动'
      addLog('启动拉流失败：后端服务未运行')
    }
  }
}

const playLive = () => {
  if (!isPulling.value) return
  
  if (useBoxedStream.value) {
    addLog('正在连接带框直播流...')
    isStreaming.value = true
    streamError.value = ''
    startTime = Date.now()
    startDurationTimer()
    
    if (streamImage.value) {
      streamImage.value.src = ''
      setTimeout(() => {
        streamImage.value.src = 'http://127.0.0.1:5003/api/live/boxed/mjpeg?t=' + Date.now()
      }, 100)
    }
    return
  }
  
  if (!videoPlayer.value) return
  
  if (flvPlayer) {
    try {
      flvPlayer.destroy()
    } catch (e) {
      console.warn('Error destroying flv player:', e)
    }
    flvPlayer = null
  }
  
  isStreaming.value = false
  streamError.value = ''
  
  if (window.flvjs && window.flvjs.isSupported()) {
    flvPlayer = window.flvjs.createPlayer({
      type: "flv",
      url: "http://127.0.0.1:5003/api/live/flv",
      isLive: true,
      hasAudio: false,
      hasVideo: true,
      enableWorker: true,
      lazyLoad: false
    })
    
    flvPlayer.attachMediaElement(videoPlayer.value)
    
    flvPlayer.on(window.flvjs.Events.LOADING_COMPLETE, () => {
      isStreaming.value = true
      streamError.value = ''
      startTime = Date.now()
      startDurationTimer()
      addLog('直播流连接成功')
    })
    
    flvPlayer.on(window.flvjs.Events.ERROR, (err) => {
      console.error('FLV error:', err)
      isStreaming.value = false
      
      const currentFlvPlayer = flvPlayer
      try {
        if (currentFlvPlayer) {
          currentFlvPlayer.destroy()
        }
      } catch (e) {
        console.warn('Error destroying flv player on error:', e)
      }
      flvPlayer = null
      
      if (err && err.type === 'networkError') {
        streamError.value = '网络连接失败，请检查推流服务'
      } else {
        streamError.value = '直播流加载失败'
      }
      addLog('直播流连接失败')
      
      if (isPulling.value) {
        setTimeout(() => {
          if (isPulling.value && !isStreaming.value) {
            addLog('尝试重新连接直播流...')
            playLive()
          }
        }, 3000)
      }
    })
    
    flvPlayer.on(window.flvjs.Events.STATISTICS_INFO, (stats) => {
      if (stats && stats.speed > 0) {
        isStreaming.value = true
        streamError.value = ''
      }
    })
    
    try {
      flvPlayer.load()
    } catch (e) {
      console.error('Error loading flv player:', e)
      streamError.value = '播放器加载失败'
      addLog('播放器加载失败')
    }
  } else {
    streamError.value = '当前浏览器不支持 FLV 播放'
    addLog('浏览器不支持 FLV 格式')
  }
}

const stopLive = () => {
  if (flvPlayer) {
    flvPlayer.destroy()
    flvPlayer = null
  }
  if (videoPlayer.value) {
    videoPlayer.value.pause()
    videoPlayer.value.src = ''
  }
  isStreaming.value = false
  streamDuration.value = '00:00:00'
  startTime = null
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
  boxedStreamUrl.value = 'http://127.0.0.1:5003/api/live/boxed/mjpeg'
}

const startDurationTimer = () => {
  if (durationTimer) clearInterval(durationTimer)
  durationTimer = setInterval(() => {
    if (startTime) {
      const elapsed = Math.floor((Date.now() - startTime) / 1000)
      const hours = Math.floor(elapsed / 3600).toString().padStart(2, '0')
      const minutes = Math.floor((elapsed % 3600) / 60).toString().padStart(2, '0')
      const seconds = (elapsed % 60).toString().padStart(2, '0')
      streamDuration.value = `${hours}:${minutes}:${seconds}`
    }
  }, 1000)
}

onUnmounted(() => {
  stopLive()
  if (isPushing.value) {
    fetch("http://127.0.0.1:5003/api/pull/stop", { method: "POST" })
  }
  if (isPulling.value) {
    fetch("http://127.0.0.1:5003/api/pull/stop", { method: "POST" })
  }
})
</script>

<style scoped>
.live-section {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
}

.control-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  width: 100%;
  max-width: 600px;
  justify-content: center;
}

.control-buttons button {
  padding: 10px 24px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background-color: #fff;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  max-width: 200px;
}

.control-buttons button:hover:not(.disabled) {
  border-color: #4A90E2;
  color: #4A90E2;
}

.control-buttons button.active {
  background-color: #4A90E2;
  border-color: #4A90E2;
  color: #ffffff;
}

.control-buttons button.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.video-wrapper {
  width: 100%;
  max-width: 900px;
  display: flex;
  justify-content: center;
  margin: 0 auto;
}

.video-container {
  width: 100%;
  max-width: 900px;
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #333;
  box-sizing: border-box;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.no-stream {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #333;
}

.no-stream-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.no-stream p {
  margin: 5px 0;
  font-size: 16px;
}

.stream-hint {
  font-size: 14px !important;
  color: #999 !important;
}

.stream-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-top: 16px;
  font-size: 14px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #999;
}

.status-dot.active {
  background-color: #f5222d;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.duration {
  margin-left: auto;
  font-family: monospace;
  color: #666;
}

.log-panel {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.log-panel h4 {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.log-count {
  font-size: 12px;
  color: #999;
  background-color: #e8e8e8;
  padding: 2px 8px;
  border-radius: 10px;
}

.logs {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-item {
  display: flex;
  gap: 10px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.log-item.log-info {
  background-color: #e6f7ff;
}

.log-item.log-success {
  background-color: #f6ffed;
}

.log-item.log-error {
  background-color: #fff2f0;
}

.log-time {
  color: #999;
  font-family: monospace;
  white-space: nowrap;
  font-size: 11px;
}

.log-message {
  color: #666;
}

.log-item.log-error .log-message {
  color: #f5222d;
}

.log-item.log-success .log-message {
  color: #52c41a;
}
</style>