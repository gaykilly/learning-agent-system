<template>
  <div class="history-section">
    <div class="history-grid">
      <div 
        v-for="item in mediaData" 
        :key="item.id" 
        class="history-item" 
        @click="handleMediaClick(item)"
      >
        <div v-if="item.type === 'image'" class="history-thumb" :style="{ backgroundImage: `url(${item.url})` }">
          <span class="media-type">📷</span>
        </div>
        <div v-else-if="item.type === 'video'" class="history-thumb video-thumb">
          <div class="video-cover" :style="{ backgroundImage: `url(${getVideoCover(item)})` }"></div>
          <div class="video-overlay">
            <span class="media-type">🎬</span>
            <div class="play-btn">▶</div>
          </div>
        </div>
        <div class="history-info">
          <div class="history-name">{{ item.name }}</div>
          <div class="history-date">{{ item.date }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const videoCovers = ref({})

const captureVideoFrame = (videoUrl) => {
  return new Promise((resolve) => {
    if (videoCovers.value[videoUrl]) {
      resolve(videoCovers.value[videoUrl])
      return
    }

    const video = document.createElement('video')
    video.crossOrigin = 'anonymous'
    video.preload = 'metadata'
    video.src = videoUrl

    video.onloadedmetadata = () => {
      video.currentTime = 0.5
    }

    video.onseeked = () => {
      const canvas = document.createElement('canvas')
      canvas.width = video.videoWidth || 300
      canvas.height = video.videoHeight || 200
      const ctx = canvas.getContext('2d')
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      const coverUrl = canvas.toDataURL('image/jpeg', 0.8)
      videoCovers.value[videoUrl] = coverUrl
      resolve(coverUrl)
      video.remove()
    }

    video.onerror = () => {
      videoCovers.value[videoUrl] = 'https://via.placeholder.com/300x200?text=Video+Cover'
      resolve(videoCovers.value[videoUrl])
      video.remove()
    }
  })
}

const getVideoCover = (item) => {
  if (videoCovers.value[item.url]) {
    return videoCovers.value[item.url]
  }
  
  captureVideoFrame(item.url)
  return item.thumbnail || 'https://via.placeholder.com/300x200?text=Loading...'
}

defineProps({
  mediaData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['media-click'])

const handleMediaClick = (item) => {
  emit('media-click', item)
}
</script>

<style scoped>
.history-section {
  padding: 12px;
  height: 100%;
  overflow-y: auto;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.history-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.history-item:hover {
  transform: scale(1.02);
}

.history-thumb {
  position: relative;
  width: 100%;
  height: 120px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  overflow: hidden;
}

.video-thumb {
  height: 120px;
}

.video-cover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.media-type {
  font-size: 24px;
}

.video-overlay .media-type {
  font-size: 32px;
  margin-bottom: 8px;
}

.play-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #4A90E2;
  transition: transform 0.2s;
}

.video-overlay:hover .play-btn {
  transform: scale(1.1);
}

.history-info {
  padding: 8px 4px;
}

.history-name {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-date {
  font-size: 12px;
  color: #999;
}
</style>