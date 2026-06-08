import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000,
})

service.interceptors.request.use(
  (config) => {
    try {
      const userStore = useUserStore()
      const token = userStore.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch (e) {
      console.log('获取 token 失败:', e)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000,
      })
      if (res.code === 401) {
        try {
          const userStore = useUserStore()
          userStore.logout()
        } catch (e) {
          console.log('登出失败:', e)
        }
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res.data
    }
  },
  (error) => {
    ElMessage({
      message: error.message || '网络错误',
      type: 'error',
      duration: 5 * 1000,
    })
    return Promise.reject(error)
  }
)

export default service