import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebar = ref({
    opened: true,
    withoutAnimation: false,
  })
  const device = ref('desktop')
  const size = ref('medium')

  const isSidebarOpened = computed(() => sidebar.value.opened)

  function toggleSidebar() {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = false
  }

  function closeSidebar(withoutAnimation = false) {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
  }

  function openSidebar(withoutAnimation = false) {
    sidebar.value.opened = true
    sidebar.value.withoutAnimation = withoutAnimation
  }

  function toggleDevice(deviceType) {
    device.value = deviceType
  }

  function setSize(sizeType) {
    size.value = sizeType
  }

  return {
    sidebar,
    device,
    size,
    isSidebarOpened,
    toggleSidebar,
    closeSidebar,
    openSidebar,
    toggleDevice,
    setSize,
  }
})
