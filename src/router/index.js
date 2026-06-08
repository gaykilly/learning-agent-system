import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginPage.vue'),
      meta: { title: '登录', hidden: true },
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/HomePage.vue'),
          meta: { title: '数据查询', icon: 'search', roles: ['admin'] },
        },
        {
          path: '/project',
          name: 'Project',
          component: () => import('@/views/ProjectPage.vue'),
          meta: { title: '数据核验', icon: 'check-circle', roles: ['admin'] },
        },
        {
          path: '/project/:id',
          name: 'TaskDetail',
          component: () => import('@/views/TaskDetailPage.vue'),
          meta: { title: '任务详情', icon: 'file-text', roles: ['admin'] },
        },
        {
          path: '/statistics',
          name: 'Statistics',
          component: () => import('@/views/StatisticsPage.vue'),
          meta: { title: '数据统计', icon: 'bar-chart', roles: ['admin'] },
        },
        {
          path: '/system',
          name: 'System',
          component: () => import('@/views/SystemPage.vue'),
          meta: { title: '系统设置', icon: 'settings', roles: ['admin'] },
        },
        {
          path: '/live',
          name: 'Live',
          component: () => import('@/views/PilotPage.vue'),
          meta: { title: '飞手控制台', icon: 'video', roles: ['pilot'] },
        },
        {
          path: '/pilot-tasks',
          name: 'PilotTasks',
          component: () => import('@/views/ProjectPage.vue'),
          meta: { title: '我的任务', icon: 'task', roles: ['pilot'] },
        },
        {
          path: '/pilot-task/:id',
          name: 'PilotTaskDetail',
          component: () => import('@/views/TaskDetailPage.vue'),
          meta: { title: '任务详情', icon: 'file-text', roles: ['pilot'] },
        },
      ],
    },
  ],
})

const getPageTitle = (title) => {
  return title ? `${title} - 管理系统` : '管理系统'
}

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const cookieToken = getToken()

  console.log('Router guard:', { to: to.path, cookieToken, hasRoles: userStore.hasRoles, roles: userStore.roles })

  if (to.path === '/login') {
    if (cookieToken && userStore.hasRoles) {
      console.log('Already logged in, redirecting based on role:', { isPilot: userStore.isPilot, isAdmin: userStore.isAdmin })
      if (userStore.isPilot) {
        next('/live')
      } else if (userStore.isAdmin) {
        next('/dashboard')
      } else {
        next()
      }
    } else {
      next()
    }
  } else {
    if (!cookieToken) {
      console.log('No token found in cookie, redirecting to login')
      next('/login')
      return
    }

    if (!userStore.hasRoles) {
      userStore.loadUserInfo()
      console.log('Loaded user info from localStorage:', { hasRoles: userStore.hasRoles, roles: userStore.roles })
      if (!userStore.hasRoles) {
        console.log('No roles found after loading, redirecting to login')
        next('/login')
        return
      }
    }

    const requiredRoles = to.meta.roles
    console.log('Checking permissions:', { requiredRoles, userRoles: userStore.roles })
    if (requiredRoles && requiredRoles.length > 0 && !requiredRoles.includes(userStore.roles[0])) {
      console.log('Role mismatch, redirecting to appropriate page')
      if (userStore.isPilot) {
        next('/live')
      } else {
        next('/dashboard')
      }
    } else {
      next()
    }
  }
})

router.afterEach((to) => {
  if (to.meta.title) {
    document.title = getPageTitle(to.meta.title)
  }
})

export default router