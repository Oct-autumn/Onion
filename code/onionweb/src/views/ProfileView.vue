<template>
  <div class="profile-view">
    <el-card class="profile-header-card">
      <div class="profile-header">
        <div class="user-avatar-section">
          <el-avatar :size="80" class="profile-avatar">
            {{ currentUser?.name?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <div class="user-info-section">
          <h2 class="user-name">{{ currentUser?.name || 'User' }}</h2>
          <p class="user-email">{{ currentUser?.email || 'user@example.com' }}</p>
          <el-tag :type="getRoleType(currentUser?.role)" size="large">
            {{ currentUser?.role || 'Member' }}
          </el-tag>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="toggleEditMode">
            {{ isEditMode ? 'Cancel' : 'Edit Profile' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="profile-form-card">
      <template #header>
        <div class="card-header">
          <h3>Profile</h3>
        </div>
      </template>

      <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="rules"
          label-width="100px"
          class="profile-form"
          :disabled="!isEditMode"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Name" prop="name">
              <el-input
                  v-model="profileForm.name"
                  placeholder="Enter your name"
                  clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email" prop="email">
              <el-input
                  v-model="profileForm.email"
                  placeholder="Enter your email"
                  clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Role" prop="role">
              <el-select v-model="profileForm.role" placeholder="Select role" style="width: 100%">
                <el-option
                    v-for="role in selectableRoles"
                    :key="role"
                    :label="role"
                    :value="role"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="isEditMode">
          <el-button type="primary" @click="saveProfile" :loading="loading">
            Save Changes
          </el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="account-settings-card">
      <template #header>
        <div class="card-header">
          <h3>Account Settings</h3>
        </div>
      </template>

      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <h4>Change Password</h4>
            <p>Update your password regularly to keep your account secure</p>
          </div>
          <el-button type="primary" text @click="showChangePasswordDialog">
            Change Password
          </el-button>
        </div>

        <div class="setting-item">
          <div class="setting-info">
            <h4>Account Status</h4>
            <p>Your account is active</p>
          </div>
          <el-tag type="success">Active</el-tag>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="changePasswordDialogVisible" title="Change Password" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="Current Password" prop="currentPassword">
          <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              placeholder="Enter current password"
              show-password
          />
        </el-form-item>
        <el-form-item label="New Password" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="Enter new password"
              show-password
          />
        </el-form-item>
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="Confirm new password"
              show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
          <span class="dialog-footer">
            <el-button @click="changePasswordDialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="changePassword">Confirm</el-button>
          </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
// 1. 导入封装的 axios 实例
import request from "@/utils/request.js";

const isEditMode = ref(false)
const loading = ref(false)
const changePasswordDialogVisible = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const currentUser = ref(null)
const roleOptions = [
  'Admin',
  'Project Manager',
  'Developer',
  'Tester',
  'Designer',
  'Product Manager',
]
const isAdmin = computed(() => currentUser.value?.role === 'Admin')
const selectableRoles = computed(() =>
    isAdmin.value ? roleOptions : roleOptions.filter(role => role !== 'Admin')
)
const roleCodeMap = {
  'Admin': 1,
  'Project Manager': 2,
  'Developer': 3,
  'Tester': 4,
  'Designer': 5,
  'Product Manager': 6,
}

const profileForm = reactive({
  name: '',
  email: '',
  role: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  name: [
    { required: true, message: 'Please enter your name', trigger: 'blur' },
    { min: 2, max: 10, message: 'Name must be 2 to 10 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
  ],
  role: [
    { required: true, message: 'Please select a role', trigger: 'change' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm new password', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const getRoleType = (role) => {
  const roleTypes = {
    'Admin': 'danger',
    'Project Manager': 'warning',
    'Developer': 'primary',
    'Tester': 'success',
    'Designer': 'info',
    'Product Manager': ''
  }
  return roleTypes[role] || ''
}

onMounted(() => {
  // 注意：这里也需要和登录时存储的 key 保持一致，应为 'userInfo'
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    const parsed = JSON.parse(userStr)
    currentUser.value = { ...parsed }
    Object.assign(profileForm, {
      name: currentUser.value.name || '',
      email: currentUser.value.email || '',
      role: currentUser.value.role || ''
    })
  }
})

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    resetForm()
  }
}

// 2. 修改 saveProfile 函数
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    loading.value = true

    if (!isAdmin.value && profileForm.role === 'Admin') {
      ElMessage.error('You are not allowed to set the role to Admin')
      loading.value = false
      return
    }

    if (!currentUser.value?.id) {
      ElMessage.error('Cannot determine current user information')
      loading.value = false
      return
    }

    const mappedRole = roleCodeMap[profileForm.role]
    if (mappedRole === undefined) {
      ElMessage.error('Unsupported role selected')
      loading.value = false
      return
    }

    const payload = {
      username: profileForm.name.trim(),
      email: profileForm.email.trim(),
      role: mappedRole,
      user_id: currentUser.value.id,
    }

    // 使用封装的 axios 实例发送 POST 请求
    await request.post('/user/update', payload);

    // 请求成功后更新本地存储和当前用户信息
    const updatedUser = {
      ...currentUser.value,
      name: profileForm.name.trim(),
      email: profileForm.email.trim(),
      role: profileForm.role,
    }
    // 同样，存储的 key 应为 'userInfo'
    localStorage.setItem('userInfo', JSON.stringify(updatedUser))
    currentUser.value = updatedUser

    ElMessage.success('Profile updated successfully')
    isEditMode.value = false
  } catch (error) {
    // 统一的错误处理
    console.error('Profile update failed:', error)
    ElMessage.error(error.message || 'Failed to update profile')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

const showChangePasswordDialog = () => {
  changePasswordDialogVisible.value = true
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  // 重置表单校验状态
  passwordFormRef.value?.clearValidate();
}

// 3. 修改 changePassword 函数
const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    if (!currentUser.value?.id) {
      ElMessage.error('Cannot determine current user information')
      return
    }

    const payload = {
      password: passwordForm.newPassword,
      old_password: passwordForm.currentPassword,
      user_id: currentUser.value.id,
    }

    // 使用封装的 axios 实例发送 POST 请求
    await request.post('/user/change_pwd', payload);

    ElMessage.success('Password updated successfully')
    changePasswordDialogVisible.value = false
  } catch (error) {
    // 统一的错误处理
    console.error('Password change failed:', error)
    ElMessage.error(error.message || 'Failed to update password')
  }
}
</script>

<style scoped>
.profile-view {
  padding: 20px;
}

.profile-header-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar-section {
  flex-shrink: 0;
}

.profile-avatar {
  background-color: #409EFF;
  color: white;
  font-size: 24px;
  font-weight: bold;
}

.user-info-section {
  flex: 1;
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.user-email {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.action-section {
  flex-shrink: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.profile-form {
  max-width: 800px;
}

.settings-list {
  max-width: 600px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.setting-info p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.profile-form-card,
.account-settings-card {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .user-info-section {
    text-align: center;
  }
}
</style>