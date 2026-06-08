import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

// 导入页面组件
import ChatView from './views/ChatView.vue'
import ProfileView from './views/ProfileView.vue'
import ResourceView from './views/ResourceView.vue'
import PathView from './views/PathView.vue'
import KnowledgeView from './views/KnowledgeView.vue'
import ExamView from './views/ExamView.vue'

// 配置路由
const routes = [
  { path: '/', redirect: '/chat' },
  { path: '/chat', component: ChatView, name: 'Chat' },
  { path: '/profile', component: ProfileView, name: 'Profile' },
  { path: '/resource', component: ResourceView, name: 'Resource' },
  { path: '/path', component: PathView, name: 'Path' },
  { path: '/knowledge', component: KnowledgeView, name: 'Knowledge' },
  { path: '/exam', component: ExamView, name: 'Exam' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
