<template>
  <div v-if="visible" class="media-viewer-modal" @click="handleClose">
    <div class="media-viewer-content" @click.stop>
      <button class="close-btn" @click="handleClose">✕</button>
      <div v-if="media" class="media-content">
        <div class="media-header">
          <h4>{{ media.name }}</h4>
          <span class="media-date">{{ media.date }}</span>
        </div>
        <div class="media-body">
          <img v-if="media.type === 'image'" :src="media.url" :alt="media.name" class="viewer-image">
          <video v-else-if="media.type === 'video'" :src="media.url" controls class="viewer-video">
            您的浏览器不支持HTML5视频播放。
          </video>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  media: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.media-viewer-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.media-viewer-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow: hidden;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.media-content {
  padding: 16px;
}

.media-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.media-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.media-date {
  font-size: 13px;
  color: #999;
}

.media-body {
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: calc(90vh - 100px);
  overflow: auto;
}

.viewer-image {
  max-width: 100%;
  max-height: calc(90vh - 100px);
  border-radius: 8px;
}

.viewer-video {
  max-width: 100%;
  max-height: calc(90vh - 100px);
  border-radius: 8px;
  background: #000;
}
</style>