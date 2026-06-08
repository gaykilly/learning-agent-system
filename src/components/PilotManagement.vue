<template>
  <div class="settings-card">
    <div class="card-header" @click="toggleExpand">
      <h3>用户管理设置</h3>
      <div class="header-right">
        <span class="user-count">{{ pilotUsers.length }} 个用户</span>
        <span class="expand-icon">{{ expanded ? '▼' : '▲' }}</span>
      </div>
    </div>
    
    <div class="card-content" v-show="expanded">
      <div class="table-container">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>姓名</th>
              <th>角色</th>
              <th>创建时间</th>
              <th>
                <button class="add-btn" @click.stop="showAddModal = true">添加用户</button>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in pilotUsers" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.name }}</td>
              <td><span :class="['role-tag', user.role]">{{ user.role === 'pilot' ? '飞手' : user.role }}</span></td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
                <button class="edit-btn" @click.stop="editUser(user)">编辑</button>
                <button class="delete-btn" @click.stop="deleteUser(user)">删除</button>
              </td>
            </tr>
            <tr v-if="pilotUsers.length === 0">
              <td colspan="6" class="empty-row">暂无用户</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="showAddModal || showEditModal" @click.self="closeModal">
      <div class="modal-content">
        <h4>{{ showEditModal ? '编辑用户' : '添加用户' }}</h4>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="formData.username" type="text" placeholder="请输入用户名" :disabled="showEditModal" required />
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="formData.name" type="text" placeholder="请输入姓名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="formData.password" type="password" :placeholder="showEditModal ? '不修改密码请留空' : '请输入密码（至少6位）'" :required="!showEditModal" />
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="formData.role" class="role-select">
              <option value="pilot">飞手</option>
            </select>
          </div>
          <div class="modal-footer">
            <button type="button" class="cancel-btn" @click="closeModal">取消</button>
            <button type="submit" class="submit-btn">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores'
import { logAddUser, logEditUser, logDeleteUser } from '@/utils/logger'

const userStore = useUserStore()

const expanded = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingUser = ref(null)
const usersList = ref([])

const formData = ref({
  username: '',
  name: '',
  password: '',
  role: 'pilot'
})

const loadUsers = () => {
  const users = JSON.parse(localStorage.getItem('registered_users') || '[]')
  usersList.value = users
}

const pilotUsers = computed(() => {
  // 管理员可以看到所有用户，飞手只能看到飞手用户
  if (userStore.isAdmin) {
    return usersList.value
  }
  return usersList.value.filter(u => u.role === 'pilot')
})

onMounted(() => {
  loadUsers()
  window.addEventListener('storage', loadUsers)
})

onUnmounted(() => {
  window.removeEventListener('storage', loadUsers)
})

const toggleExpand = () => {
  expanded.value = !expanded.value
}

const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const editUser = (user) => {
  editingUser.value = user
  formData.value = {
    username: user.username,
    name: user.name,
    password: '',
    role: user.role
  }
  showEditModal.value = true
}

const deleteUser = (user) => {
  if (confirm(`确定要删除用户 "${user.name}" 吗？`)) {
    const users = JSON.parse(localStorage.getItem('registered_users') || '[]')
    const updatedUsers = users.filter(u => u.id !== user.id)
    localStorage.setItem('registered_users', JSON.stringify(updatedUsers))
    loadUsers()
    logDeleteUser(user.username, userStore.userInfo?.name || '管理员')
    alert('删除成功')
  }
}

const handleSubmit = () => {
  if (!formData.value.username || !formData.value.name) {
    alert('请填写完整信息')
    return
  }
  
  if (!showEditModal.value && (!formData.value.password || formData.value.password.length < 6)) {
    alert('密码至少需要6位')
    return
  }

  let users = JSON.parse(localStorage.getItem('registered_users') || '[]')
  console.log('Existing users before:', users)
  
  if (showEditModal.value && editingUser.value) {
    const index = users.findIndex(u => u.id === editingUser.value.id)
    if (index !== -1) {
      const updatedUser = {
        ...users[index],
        username: formData.value.username,
        name: formData.value.name,
        role: formData.value.role
      }
      if (formData.value.password && formData.value.password.length >= 6) {
        updatedUser.password = formData.value.password
      }
      users[index] = updatedUser
    }
    logEditUser(formData.value.username, userStore.userInfo?.name || '管理员')
    alert('修改成功')
  } else {
    const exists = users.find(u => u.username === formData.value.username)
    if (exists) {
      alert('用户名已存在')
      return
    }
    const newUser = {
      id: Date.now(),
      username: formData.value.username,
      password: formData.value.password,
      name: formData.value.name,
      role: formData.value.role,
      roles: [formData.value.role],
      createdAt: Date.now()
    }
    users.push(newUser)
    console.log('New user added:', newUser)
    logAddUser(formData.value.username, formData.value.role, userStore.userInfo?.name || '管理员')
    alert('添加成功')
  }
  
  localStorage.setItem('registered_users', JSON.stringify(users))
  console.log('Users saved to localStorage:', JSON.parse(localStorage.getItem('registered_users')))
  loadUsers()
  
  closeModal()
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  editingUser.value = null
  formData.value = {
    username: '',
    name: '',
    password: '',
    role: 'pilot'
  }
}
</script>

<style lang="scss" scoped>
.settings-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  
  h3 {
    font-size: 16px;
    margin: 0;
    color: #333;
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-count {
      background: #f5f5f5;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      color: #666;
    }
    
    .expand-icon {
      font-size: 12px;
      color: #999;
      transition: transform 0.3s ease;
    }
  }
  
  &:hover .expand-icon {
    color: #667eea;
  }
}

.card-content {
  padding-top: 16px;
}

.toolbar {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  background: #4A90E2;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: #357ABD;
  }
}

.table-container {
  overflow-x: auto;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  
  th, td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #f0f0f0;
  }
  
  th {
    background: #fafafa;
    font-weight: 600;
    color: #666;
    font-size: 14px;
  }
  
  td {
    color: #333;
    font-size: 14px;
  }
}

.role-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  
  &.pilot {
    background: #e6f7ff;
    color: #1890ff;
  }
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 40px;
}

.edit-btn, .delete-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
  margin-right: 6px;
  transition: all 0.2s ease;
}

.edit-btn {
  background: #4A90E2;
  color: #fff;
  
  &:hover {
    background: #357ABD;
  }
}

.delete-btn {
  background: #f5f5f5;
  color: #666;
  
  &:hover {
    background: #eee;
    color: #ff4d4f;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 400px;
  max-width: 90%;
  
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px 0;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.form-group {
  margin-bottom: 16px;
  
  label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    color: #333;
    font-size: 14px;
  }
  
  input, select {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.3s ease;
    box-sizing: border-box;
    
    &:focus {
      border-color: #667eea;
    }
    
    &:disabled {
      background: #f5f5f5;
      color: #999;
    }
  }
}

.role-select {
  background: #fff;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn, .submit-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  
  &:hover {
    background: #eee;
  }
}

.submit-btn {
  background: #667eea;
  color: #fff;
  
  &:hover {
    background: #5a70d6;
  }
}
</style>