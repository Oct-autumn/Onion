<!-- /src/views/RegisterView.vue -->
<template>
    <div class="register-container">
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
              placeholder="Enter your password again"
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
          <p>Already have an account？<router-link to="/login">Login</router-link></p>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
  const registerFormRef = ref()
  const loading = ref(false)
  
  // Form data
  const registerForm = reactive({
    email: '',
    username: '',
    password: '',
    confirmPassword: ''
  })
  
  // Form validation rules
  const rules = {
    email: [
      { required: true, message: 'Enter your email', trigger: 'blur' },
      { type: 'email', message: 'Enter your email', trigger: 'blur' }
    ],
    username: [
      { required: true, message: 'Enter your username', trigger: 'blur' },
      { min: 2, max: 20, message: 'Username length should be between 2 and 20 characters', trigger: 'blur' }
    ],
    password: [
      { required: true, message: 'Enter your password', trigger: 'blur' },
      { min: 6, max: 20, message: 'Password length should be between 6 and 20 characters', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: 'Confirm your password', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== registerForm.password) {
            callback(new Error('The two passwords do not match'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
  
  // Submit form
  const submitForm = async () => {
    if (!registerFormRef.value) return
    
    await registerFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          // Here call the backend API
          const response = await fetch('/api/user/register', {
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
  
  // Reset form
  const resetForm = () => {
    if (!registerFormRef.value) return
    registerFormRef.value.resetFields()
  }
  </script>
  
  <style scoped>
  .register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px;
  }
  
  .register-card {
    width: 100%;
    max-width: 500px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border-radius: 12px;
  }
  
  .card-header {
    text-align: center;
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