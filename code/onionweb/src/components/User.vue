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

      <el-table-column prop="role" label="Role" width="180">
        <template #default="{ row }">
          <!-- If user is Admin, show as read-only tag -->
          <el-tag v-if="row.role === 1" type="danger" size="small">
            {{ roleLabelMap[row.role] }}
          </el-tag>
          <!-- For non-admin users, show editable select -->
          <el-select 
            v-else
            v-model="row.role" 
            placeholder="Select role" 
            size="small"
            @change="(val) => updateUserRole(row, val)"
          >
            <el-option
                v-for="role in nonAdminRoleOptions"
                :key="role.value"
                :label="role.label"
                :value="role.value"
            />
          </el-select>
        </template>
      </el-table-column>
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
// 1. 确保已导入封装的 axios 实例
import request from "@/utils/request.js";

const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)

const currentUser = ref(null)
try {
  // 注意：这里的 'user' key 要和你登录时存储的一致，之前我们用的是 'userInfo'
  // 如果登录时是 localStorage.setItem('userInfo', ...)，这里也要对应修改
  const userStr = localStorage.getItem('userInfo')
  currentUser.value = userStr ? JSON.parse(userStr) : null
} catch (_) {
  currentUser.value = null
}

const isAdmin = computed(() => currentUser.value?.role === 1)

// Role options (excluding Admin role for updating other users)
const nonAdminRoleOptions = [
  { label: 'Normal User', value: 0 },
  { label: 'Project Manager', value: 2 },
  { label: 'Developer', value: 3 },
  { label: 'Tester', value: 4 },
  { label: 'Designer', value: 5 },
  { label: 'Product Manager', value: 6 },
]

const roleLabelMap = {
  0: 'Normal User',
  1: 'Admin',
  2: 'Project Manager',
  3: 'Developer',
  4: 'Tester',
  5: 'Designer',
  6: 'Product Manager',
}

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
    role: item.role ?? 0, // Include role information
  }
}

// 2. 修改 fetchUsers 函数
const fetchUsers = async () => {
  if (!isAdmin.value) return
  loading.value = true
  try {
    // 使用 request.get 替代 fetch
    // 注意：get 请求的参数可以直接放在第二个参数的 params 对象里
    const result = await request.get('/user/list', {
      params: {
        page: currentPage.value,
        pagenum: pageSize
      }
    });

    // 由于我们在 axios 响应拦截器中已经 return response.data，
    // 所以这里的 result 直接就是后端返回的数据体
    const list = Array.isArray(result?.data) ? result.data : Array.isArray(result) ? result : []
    users.value = list.map(mapUser)
    total.value = result?.total ?? list.length ?? 0
  } catch (error) {
    // 错误处理被大大简化，因为 axios 拦截器已经处理了网络错误和 4xx/5xx 状态码
    console.error('Fetch users error:', error)
    // error 对象是我们在响应拦截器中 reject 的内容，它包含了 message
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

// 3. 修改 addUser 函数
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

    // 使用 request.post 替代 fetch
    // post 的第二个参数直接是请求体 payload
    await request.post('/user/register', payload);

    // 如果请求成功，axios 会返回响应数据，我们这里不需要处理，直接执行后续操作
    currentPage.value = 1
    await fetchUsers()
    addUserDialogVisible.value = false
    ElMessage.success('User created (default password: 123456)')
  } catch (error) {
    // 同样，这里的错误处理也被简化了
    console.error('Add user failed:', error)
    ElMessage.error(error.message || 'Failed to create user')
  }
}

const updateUserRole = async (user, newRole) => {
  if (!isAdmin.value) {
    ElMessage.error('Only administrators can perform this action')
    // Revert the change
    await fetchUsers()
    return
  }

  // Prevent setting role to Admin (value 1)
  if (newRole === 1) {
    ElMessage.error('Cannot set user role to Admin')
    // Revert the change
    await fetchUsers()
    return
  }

  try {
    const payload = {
      user_id: user.id,
      username: user.name,
      email: user.email,
      role: newRole
    }

    await request.post('/user/update', payload)
    ElMessage.success(`User role updated to ${roleLabelMap[newRole]}`)
    // Refresh the user list to ensure data consistency
    await fetchUsers()
  } catch (error) {
    console.error('Update user role failed:', error)
    ElMessage.error(error.message || 'Failed to update user role')
    // Revert the change on error
    await fetchUsers()
  }
}
</script>

<style scoped>
/* ... (样式部分保持不变) ... */
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