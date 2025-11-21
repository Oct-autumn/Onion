<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="image-section">
        <div class="image-content">
          <h1 class="welcome-title">Welcome to Onion</h1>
          <p class="welcome-subtitle">Join our community and start your journey</p>
          <div class="image-placeholder">
            <img src="/Onion_slogan_word.png" alt="Onion Slogan" class="welcome-image" />
          </div>
        </div>
      </div>

      <div class="form-section">
        <el-card class="register-card">
          <template #header>
            <div class="card-header">
              <h2>User Registration</h2>
            </div>
          </template>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="rules"
            label-width="80px"
            class="register-form"
          >
            <el-form-item label="Email" prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="Enter your email"
                type="email"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Username" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="Enter your username"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Password" prop="password">
              <el-input
                v-model="registerForm.password"
                placeholder="Enter your password"
                type="password"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Confirm Password" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                placeholder="Confirm your password"
                type="password"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                @click="submitForm"
                :loading="loading"
                class="register-button"
              >
                Register
              </el-button>
              <el-button @click="resetForm">
                Reset
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="login-link">
            <p>Already have an account? <router-link to="/login">Sign in</router-link></p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email format', trigger: 'blur' }
  ],
  username: [
    { required: true, message: 'Please enter your username', trigger: 'blur' },
    { min: 2, max: 20, message: 'Username length should be 2 to 20 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Password length should be 6 to 20 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const submitForm = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await fetch('http://localhost:8080/user/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: registerForm.email,
            username: registerForm.username,
            password: registerForm.password
          })
        })
        
        if (response.ok) {
          ElMessage.success('Registration successful!')
          router.push('/login')
        } else {
          const error = await response.json()
          ElMessage.error(error.message || 'Registration failed')
        }
      } catch (error) {
        ElMessage.error('Network error, please try again later')
        console.error('Registration error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (!registerFormRef.value) return
  registerFormRef.value.resetFields()
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  width: 100vw; 
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.register-wrapper {
  display: flex;
  width: 100%;
  min-height: 100vh;
  background: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.image-section {
  flex: 1;
  min-width: 0; 
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
}

.image-content {
  text-align: center;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: white;
}

.welcome-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  color: white;
}

.image-placeholder {
  margin-top: 2rem;
}

.welcome-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.form-section {
  flex: 1;
  background: #f8f9fa;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  min-width: 0; 
}

.register-card {
  width: 100%;
  max-width: 500px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.register-form {
  padding: 20px 0;
}

.register-button {
  width: 100%;
  margin-bottom: 10px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
