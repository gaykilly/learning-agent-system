import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia, { useUserStore } from './stores'
import { setupDirectives } from './directive'
import 'nprogress/nprogress.css'
import './style/index.scss'

const app = createApp(App)

app.use(pinia)

const userStore = useUserStore()
userStore.loadUserInfo()

app.use(router)
setupDirectives(app)

app.mount('#app')
