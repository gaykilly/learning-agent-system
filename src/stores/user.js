import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import request from '@/utils/request'

const USER_INFO_KEY = 'user_info'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref({})
  const roles = ref([])

  const hasRoles = computed(() => roles.value.length > 0)

  const isAdmin = computed(() => roles.value.includes('admin'))
  const isPilot = computed(() => roles.value.includes('pilot'))

  function loadUserInfo() {
    try {
      const saved = localStorage.getItem(USER_INFO_KEY)
      if (saved) {
        const info = JSON.parse(saved)
        userInfo.value = info
        roles.value = info.roles || []
      }
    } catch (e) {
      console.error('Failed to load user info:', e)
    }
  }

  function saveUserInfo(info) {
    try {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
    } catch (e) {
      console.error('Failed to save user info:', e)
    }
  }

  async function login(loginData) {
    try {
      const response = await request({
        url: '/user/login',
        method: 'post',
        data: {
          userAccount: loginData.username,
          userPassword: loginData.password
        }
      })

      token.value = response.token
      setToken(token.value)

      userInfo.value = {
        id: response.userId,
        name: response.userName,
        avatar: '',
        roles: [response.roleName === '管理员' ? 'admin' : 'pilot'],
        account: response.userAccount
      }
      roles.value = [response.roleName === '管理员' ? 'admin' : 'pilot']
      
      saveUserInfo(userInfo.value)
      return Promise.resolve()
    } catch (error) {
      console.error('Login API error:', error)
      return Promise.reject(new Error(error.response?.data?.message || '登录失败'))
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    removeToken()
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem('admin_token_ls')
    sessionStorage.clear()
  }

  async function register(registerData) {
    const { username, password, name, role } = registerData
    
    if (!username || !password || !name) {
      return Promise.reject(new Error('请填写完整信息'))
    }
    
    if (username.length < 4) {
      return Promise.reject(new Error('用户名至少需要4个字符'))
    }
    
    if (password.length < 6) {
      return Promise.reject(new Error('密码至少需要6个字符'))
    }

    try {
      await request({
        url: '/user/register',
        method: 'post',
        data: {
          userAccount: username,
          userPassword: password,
          userName: name,
          userRemark: role || 'pilot'
        }
      })
      return Promise.resolve({ message: '注册成功，请登录' })
    } catch (error) {
      return Promise.reject(new Error(error.response?.data?.message || '注册失败'))
    }
  }

  function resetToken() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
  }

  return {
    token,
    userInfo,
    roles,
    hasRoles,
    isAdmin,
    isPilot,
    login,
    logout,
    register,
    resetToken,
    loadUserInfo,
  }
})