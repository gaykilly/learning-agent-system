<template>
  <header class="top-nav">
    <div class="logo">logo</div>
    <nav class="main-menu">
      <template v-if="userStore.isAdmin">
        <router-link to="/dashboard" :class="{ active: activeNav === 'dashboard' }">首页</router-link>
        <router-link to="/project" :class="{ active: activeNav === 'project' }">项目</router-link>
        <router-link to="/statistics" :class="{ active: activeNav === 'statistics' }">统计</router-link>
        <router-link to="/system" :class="{ active: activeNav === 'system' }">系统</router-link>
      </template>
      <template v-else-if="userStore.isPilot">
        <router-link to="/live" :class="{ active: activeNav === 'live' }">飞手控制台</router-link>
        <router-link to="/pilot-tasks" :class="{ active: activeNav === 'pilot-tasks' }">任务</router-link>
      </template>
    </nav>
    <div class="right-section">
      <div class="search-box">
        <input type="text" placeholder="搜索...">
        <span class="search-icon">🔍</span>
      </div>
      <div class="notification">
        <span class="badge">3</span>
        <span class="icon">🔔</span>
      </div>
      <div class="user-avatar" :class="userAvatarClass" :title="userAvatarTitle" @click="handleLogout">{{ userAvatarText }}</div>
      <div class="settings">⚙️</div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeNav = computed(() => {
  const path = route.path
  if (userStore.isAdmin) {
    if (path.startsWith('/dashboard')) return 'dashboard'
    if (path.startsWith('/project')) return 'project'
    if (path.startsWith('/statistics')) return 'statistics'
    if (path.startsWith('/system')) return 'system'
    return 'dashboard'
  } else if (userStore.isPilot) {
    if (path.startsWith('/live')) return 'live'
    if (path.startsWith('/pilot-tasks')) return 'pilot-tasks'
    return 'live'
  }
  return 'dashboard'
})

const userAvatarText = computed(() => {
  if (userStore.isAdmin) return '管'
  if (userStore.isPilot) return '飞'
  return '用'
})

const userAvatarClass = computed(() => {
  if (userStore.isAdmin) return 'avatar-admin'
  if (userStore.isPilot) return 'avatar-pilot'
  return 'avatar-default'
})

const userAvatarTitle = computed(() => {
  const name = userStore.userInfo?.name || '用户'
  return `${name} - 退出登录`
})

const handleLogout = () => {
  const confirmed = window.confirm('确定要退出登录吗？')
  if (confirmed === true) {
    userStore.logout()
    sessionStorage.clear()
    try {
      document.cookie = 'Admin-Token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      document.cookie = 'Admin-Token=; Max-Age=-99999999; path=/'
    } catch (e) {
      console.error('Cookie clear error:', e)
    }
    window.location.replace('/login')
  }
}
</script>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  height: 60px;
  width: 100%;
  min-width: 0;
  max-width: none;
  background: #ffffff;
  color: #333333;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  box-sizing: border-box;
  margin: 0;
}

.logo {
  font-size: 28px;
  font-weight: bold;
  color: #4A90E2;
  font-family: 'Brush Script MT', 'Segoe Script', cursive;
  flex-shrink: 0;
  margin-right: 20px;
}

.main-menu {
  display: flex;
  gap: 10px;
  min-width: 0;
  flex: 1;
  justify-content: center;
}

.main-menu a {
  color: #333333;
  text-decoration: none;
  padding: 8px 15px;
  border-radius: 20px;
  transition: all 0.3s;
  font-weight: 500;
  flex: 1;
  text-align: center;
  min-width: 0;
}

.main-menu a:hover,
.main-menu a.active,
.main-menu a.router-link-active,
.main-menu a.router-link-exact-active {
  background: #f0f0f0;
  color: #4A90E2;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex-shrink: 0;
}

.search-box {
  position: relative;
  flex-shrink: 1;
  min-width: 0;
  flex-basis: 100px;
}

.search-box input {
  padding: 5px 22px 5px 8px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  outline: none;
  font-size: 12px;
  width: 100%;
  max-width: 120px;
  min-width: 60px;
  background: #f5f5f5;
  color: #333333;
  box-sizing: border-box;
}

.search-icon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.notification {
  position: relative;
  cursor: pointer;
}

.notification .badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: red;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #4A90E2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
}

.avatar-admin {
  background: #722ed1;
}

.avatar-pilot {
  background: #4A90E2;
}

.avatar-default {
  background: #8c8c8c;
}

.settings {
  cursor: pointer;
  font-size: 18px;
}

@media (max-width: 600px) {
  .search-box {
    display: none;
  }
  
  .main-menu {
    gap: 8px;
  }
  
  .main-menu a {
    padding: 5px 8px;
    font-size: 12px;
  }
  
  .right-section {
    gap: 8px;
  }
  
  .logo {
    font-size: 16px;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
  
  .notification .badge {
    width: 16px;
    height: 16px;
    font-size: 11px;
  }
  
  .settings {
    font-size: 16px;
  }
}
</style>
