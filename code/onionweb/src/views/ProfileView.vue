<template>
  <div class="profile-container">
    <div class="profile-wrapper">
      <!-- Left side - User Avatar & Basic Info -->
      <div class="avatar-section">
        <div class="avatar-content">
          <div class="user-avatar">
            <el-avatar :size="120" :src="userInfo.avatar || '/default-avatar.png'">
              {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
            </el-avatar>
          </div>
          <h1 class="user-name">{{ userInfo.username || 'User' }}</h1>
          <p class="user-email">{{ userInfo.email || 'user@example.com' }}</p>
          <p class="user-role">{{ userInfo.role || 'Member' }}</p>
        </div>
      </div>

      <!-- Right side - Profile Form -->
      <div class="form-section">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <h2>User Profile</h2>
              <el-button 
                type="primary" 
                @click="toggleEditMode"
                :loading="loading"
              >
                {{ isEditMode ? 'Cancel' : 'Edit Profile' }}
              </el-button>
            </div>
          </template>
          
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="rules"
            label-width="120px"
            class="profile-form"
            :disabled="!isEditMode"
          >
            <el-form-item label="Username" prop="username">
              <el-input
                v-model="profileForm.username"
                placeholder="Enter your username"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Email" prop="email">
              <el-input
                v-model="profileForm.email"
                placeholder="Enter your email"
                type="email"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Full Name" prop="fullName">
              <el-input
                v-model="profileForm.fullName"
                placeholder="Enter your full name"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Phone" prop="phone">
              <el-input
                v-model="profileForm.phone"
                placeholder="Enter your phone number"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Bio" prop="bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="3"
                placeholder="Tell us about yourself"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item v-if="isEditMode">
              <el-button
                type="primary"
                @click="updateProfile"
                :loading="loading"
                class="update-button"
              >
                Update Profile
              </el-button>
              <el-button @click="resetForm">
                Reset
              </el-button>
            </el-form-item>
          </el-form>
          
          <!-- Change Password Section -->
          <el-divider content-position="left">Change Password</el-divider>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="Current Password" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                placeholder="Enter current password"
                type="password"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="New Password" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                placeholder="Enter new password"
                type="password"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Confirm Password" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                placeholder="Confirm new password"
                type="password"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="warning"
                @click="changePassword"
                :loading="passwordLoading"
                class="password-button"
              >
                Change Password
              </el-button>
              <el-button @click="resetPasswordForm">
                Clear
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="logout-section">
            <el-button
              type="danger"
              @click="logout"
              class="logout-button"
            >
              Logout
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const profileFormRef = ref()
const passwordFormRef = ref()
const loading = ref(false)
const passwordLoading = ref(false)
const isEditMode = ref(false)

// User info (mock data - replace with actual user data)
const userInfo = reactive({
  id: 1,
  username: 'admin',
  email: 'admin@onion.com',
  fullName: 'Administrator',
  phone: '+1234567890',
  bio: 'System administrator with full access to all features.',
  role: 'Admin',
  avatar: null,
  createdAt: '2024-01-01',
  lastLogin: '2024-12-28'
})

// Profile form data
const profileForm = reactive({
  username: '',
  email: '',
  fullName: '',
  phone: '',
  bio: ''
})

// Password form data
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Profile validation rules
const rules = {
  username: [
    { required: true, message: 'Please enter your username', trigger: 'blur' },
    { min: 2, max: 20, message: 'Username length should be 2 to 20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email format', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: 'Please enter your full name', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^[\+]?[1-9][\d]{0,15}$/, message: 'Please enter a valid phone number', trigger: 'blur' }
  ]
}

// Password validation rules
const passwordRules = {
  currentPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Password length should be 6 to 20 characters', trigger: 'blur' }
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

// Initialize form with user data
const initializeForm = () => {
  profileForm.username = userInfo.username
  profileForm.email = userInfo.email
  profileForm.fullName = userInfo.fullName
  profileForm.phone = userInfo.phone
  profileForm.bio = userInfo.bio
}

// Toggle edit mode
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    initializeForm() // Reset form when canceling
  }
}

// Update profile
const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // Call backend API
        const response = await fetch('/api/user/profile', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username: profileForm.username,
            email: profileForm.email,
            fullName: profileForm.fullName,
            phone: profileForm.phone,
            bio: profileForm.bio
          })
        })
        
        if (response.ok) {
          // Update local user info
          userInfo.username = profileForm.username
          userInfo.email = profileForm.email
          userInfo.fullName = profileForm.fullName
          userInfo.phone = profileForm.phone
          userInfo.bio = profileForm.bio
          
          ElMessage.success('Profile updated successfully!')
          isEditMode.value = false
        } else {
          const error = await response.json()
          ElMessage.error(error.message || 'Update failed')
        }
      } catch (error) {
        ElMessage.error('Network error, please try again later')
        console.error('Profile update error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// Change password
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        // Call backend API
        const response = await fetch('/api/user/change-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          })
        })
        
        if (response.ok) {
          ElMessage.success('Password changed successfully!')
          resetPasswordForm()
        } else {
          const error = await response.json()
          ElMessage.error(error.message || 'Password change failed')
        }
      } catch (error) {
        ElMessage.error('Network error, please try again later')
        console.error('Password change error:', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// Reset profile form
const resetForm = () => {
  if (!profileFormRef.value) return
  profileFormRef.value.resetFields()
  initializeForm()
}

// Reset password form
const resetPasswordForm = () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.resetFields()
}

// Logout
const logout = async () => {
  try {
    await ElMessageBox.confirm('Are you sure you want to logout?', 'Logout', {
      confirmButtonText: 'Yes',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    
    // Call logout API
    await fetch('/api/user/logout', { method: 'POST' })
    
    ElMessage.success('Logged out successfully!')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Logout error:', error)
    }
  }
}

// Initialize on mount
onMounted(() => {
  initializeForm()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.profile-wrapper {
  display: flex;
  width: 100%;
  max-width: 1200px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-height: 600px;
}

.avatar-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
}

.avatar-content {
  text-align: center;
}

.user-avatar {
  margin-bottom: 20px;
}

.user-name {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 10px;
  color: white;
}

.user-email {
  font-size: 1.1rem;
  margin-bottom: 5px;
  opacity: 0.9;
  color: white;
}

.user-role {
  font-size: 1rem;
  opacity: 0.8;
  color: white;
  background: rgba(255, 255, 255, 0.2);
  padding: 5px 15px;
  border-radius: 20px;
  display: inline-block;
}

.form-section {
  flex: 1;
  background: #f8f9fa;
  padding: 40px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 100vh;
}

.profile-card {
  width: 100%;
  max-width: 600px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.profile-form {
  padding: 20px 0;
}

.password-form {
  padding: 20px 0;
}

.update-button, .password-button {
  width: 100%;
  margin-bottom: 10px;
}

.logout-section {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.logout-button {
  width: 100%;
}

/* Responsive design */
@media (max-width: 768px) {
  .profile-wrapper {
    flex-direction: column;
    max-width: 100%;
  }
  
  .avatar-section {
    flex: none;
    min-height: 300px;
  }
  
  .user-name {
    font-size: 1.5rem;
  }
  
  .form-section {
    padding: 20px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
