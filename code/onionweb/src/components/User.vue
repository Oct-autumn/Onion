<template>
  <div class="user-view">
    <div v-if="isAdmin" class="add-user-top">
      <el-button type="primary" @click="showAddUserDialog">Add User</el-button>
    </div>

    <el-table :data="users" style="width: 100%" :loading="loading">
      <el-table-column prop="name" label="Name" width="220">
        <template #default="{ row }">
          <div class="member-info">
            <el-avatar :size="32" :src="row.avatar" class="member-avatar">
              {{ row.name.charAt(0) }}
            </el-avatar>
            <span class="member-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="email" label="Email" />
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="addUserDialogVisible" title="Add User" width="480px">
      <el-form :model="newUser" :rules="newUserRules" ref="newUserForm" label-width="90px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="newUser.username" placeholder="Enter username" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="newUser.email" placeholder="Enter email" />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="New users receive the default password 123456"
        />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="addUser">Create</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)

const currentUser = ref(null)
try {
  const userStr = localStorage.getItem('user')
  currentUser.value = userStr ? JSON.parse(userStr) : null
} catch (_) {
  currentUser.value = null
}

const isAdmin = computed(() => currentUser.value?.role === 'Admin')

const addUserDialogVisible = ref(false)
const newUserForm = ref(null)
const newUser = ref({ username: '', email: '' })
const newUserRules = {
  username: [
    { required: true, message: 'Please enter a username', trigger: 'blur' },
    { min: 2, max: 20, message: 'Username must be 2-20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter an email', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
  ]
}

const users = ref([])

const mapUser = (item) => {
  const name = item.name || item.username || item.email?.split('@')[0] || 'Unknown'
  return {
    id: item.id ?? item.user_id ?? Date.now(),
    name,
    email: item.email || '',
  }
}

const fetchUsers = async () => {
  if (!isAdmin.value) return
  loading.value = true
  try {
    const response = await fetch(`/api/user/list?page=${currentPage.value}&size=${pageSize}`)
    let result = null
    try {
      result = await response.json()
    } catch (_) {
      result = null
    }
    if (!response.ok) {
      throw new Error(result?.message || 'Failed to load users')
    }
    const list = Array.isArray(result?.data) ? result.data : Array.isArray(result) ? result : []
    users.value = list.map(mapUser)
    total.value = result?.total ?? list.length ?? 0
  } catch (error) {
    console.error('Fetch users error:', error)
    ElMessage.error(error.message || 'Failed to load users')
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

onMounted(() => {
  if (isAdmin.value) {
    fetchUsers()
  }
})

watch(isAdmin, (val) => {
  if (val) {
    fetchUsers()
  } else {
    users.value = []
    total.value = 0
  }
})

const showAddUserDialog = () => {
  if (!isAdmin.value) {
    ElMessage.error('Only administrators can perform this action')
    return
  }
  newUser.value = { username: '', email: '' }
  addUserDialogVisible.value = true
}

const addUser = async () => {
  if (!isAdmin.value) {
    ElMessage.error('Only administrators can perform this action')
    return
  }
  if (!newUserForm.value) return
  try {
    await newUserForm.value.validate()
    const exists = users.value.some(u => u.email === newUser.value.email)
    if (exists) {
      ElMessage.error('Email already exists')
      return
    }
    
    const payload = {
      email: newUser.value.email.trim(),
      username: newUser.value.username.trim(),
      password: '123456',
    }

    const response = await fetch('/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    let result = null
    try {
      result = await response.json()
    } catch (_) {
      result = null
    }

    if (!response.ok) {
      throw new Error(result?.message || 'Failed to create user')
    }

    currentPage.value = 1
    await fetchUsers()
    addUserDialogVisible.value = false
    ElMessage.success('User created (default password: 123456)')
  } catch (e) {
    console.error('Add user failed:', e)
    ElMessage.error(e.message || 'Failed to create user')
  }
}
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.add-user-top {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 12px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar {
  flex-shrink: 0;
}

.member-name {
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-button--small) {
  padding: 5px 10px;
  font-size: 12px;
}

:deep(.el-tag) {
  font-weight: 500;
}
</style>