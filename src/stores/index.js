import { createPinia } from 'pinia'
import { useUserStore } from './user'
import { useAppStore } from './app'

const pinia = createPinia()

export { useUserStore, useAppStore }
export default pinia
