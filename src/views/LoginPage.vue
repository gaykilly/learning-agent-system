<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">{{ isRegister ? '用户注册' : '用户登录' }}</h2>
      
      <div class="form-tabs">
        <button 
          :class="['tab-btn', { active: !isRegister }]" 
          @click="isRegister = false"
        >
          登录
        </button>
        <button 
          :class="['tab-btn', { active: isRegister }]" 
          @click="isRegister = true"
        >
          注册
        </button>
      </div>

      <form v-if="!isRegister" @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" v-model="formData.username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input id="password" v-model="formData.password" type="password" placeholder="请输入密码" required />
        </div>
        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="formData.remember" />
            <span>记住我</span>
          </label>
        </div>
        <button type="submit" class="login-btn">登录</button>
      </form>

      <form v-else @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="reg-username">用户名</label>
          <input id="reg-username" v-model="registerData.username" type="text" placeholder="请输入用户名（至少4个字符）" required />
        </div>
        <div class="form-group">
          <label for="reg-name">姓名</label>
          <input id="reg-name" v-model="registerData.name" type="text" placeholder="请输入真实姓名" required />
        </div>
        <div class="form-group">
          <label for="reg-password">密码</label>
          <input id="reg-password" v-model="registerData.password" type="password" placeholder="请输入密码（至少6个字符）" required />
        </div>
        <div class="form-group">
          <label for="reg-role">用户角色</label>
          <select id="reg-role" v-model="registerData.role" class="role-select">
            <option value="pilot">飞手</option>
          </select>
        </div>
        <button type="submit" class="login-btn">注册</button>
      </form>


    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { logLogin, logRegister } from '@/utils/logger'

const router = useRouter()
const userStore = useUserStore()
const isRegister = ref(false)

const formData = ref({
  username: '',
  password: '',
  remember: false
})

const registerData = ref({
  username: '',
  name: '',
  password: '',
  role: 'pilot'
})

const handleLogin = async () => {
  try {
    await userStore.login(formData.value)
    logLogin(formData.value.username)
    console.log('Login successful, redirecting...')
    console.log('User roles:', userStore.roles)
    if (userStore.isAdmin) {
      router.push('/dashboard')
    } else if (userStore.isPilot) {
      router.push('/live')
    } else {
      router.push('/dashboard')
    }
  } catch (error) {
    console.error('Login failed:', error)
    alert(error.message || '登录失败！')
  }
}

const handleRegister = async () => {
  try {
    await userStore.register(registerData.value)
    logRegister(registerData.value.username)
    alert('注册成功！请登录')
    isRegister.value = false
    registerData.value = {
      username: '',
      name: '',
      password: '',
      role: 'pilot'
    }
  } catch (error) {
    console.error('Register failed:', error)
    alert(error.message || '注册失败！')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: #ffffff;
  border-radius: 12px;
  padding: 62px;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  color: #333333;
  margin-bottom: 20px;
}

.form-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: none;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.tab-btn:hover {
  color: #667eea;
}

.tab-btn.active {
  color: #667eea;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #667eea;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #333333;
}

.form-group input {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px 25px;
  font-size: 20px;
  transition: all 0.3s ease;
  outline: none;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.role-select {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px 25px;
  font-size: 20px;
  transition: all 0.3s ease;
  outline: none;
  background: #fff;
}

.role-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #666666;
}

.remember-me input {
  margin-right: 8px;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}


</style>