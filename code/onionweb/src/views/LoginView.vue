<template>
  <div class="login-container">
      <div class="login-wrapper">
          <!-- Left side - Image -->
          <div class="image-section">
          <div class="image-content">
              <h1 class="welcome-title">Welcome Back</h1>
          <p class="welcome-subtitle">Sign in to continue your Project Management journey</p>
          <div class="image-placeholder">
            <img src="/Onion_slogan_word.png" alt="Onion Slogan" class="welcome-image" />
          </div>
        </div>
      </div>

      <!-- Right side - Login Form -->
      <div class="form-section">
        <el-card class="login-card">
          <template #header>
            <div class="card-header">
              <h2>User Login</h2>
            </div>
          </template>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            label-width="80px"
            class="login-form"
          >
            <el-form-item label="Email" prop="email">
              <el-input
                v-model="loginForm.email"
                placeholder="Enter your email"
                type="email"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="Password" prop="password">
              <el-input
                v-model="loginForm.password"
                placeholder="Enter your password"
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
                class="login-button"
              >
                Login
              </el-button>
              <el-button @click="resetForm">
                Reset
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="register-link">
            <p>Don't have an account? <router-link to="/register">Sign up</router-link></p>
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
const loginFormRef = ref()
const loading = ref(false)

// Form data
const loginForm = reactive({
  email: '',
  password: ''
})

// Form validation rules
const rules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email format', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Password length should be 6 to 20 characters', trigger: 'blur' }
  ]
}

// Submit form
const submitForm = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 测试用户验证（用于前端测试，支持管理员与普通用户）
        const testUsers = [
          { id: 1, name: '超级管理员', email: 'admin@onion.com', password: 'admin123', role: '超级管理员' },
          { id: 2, name: '张三', email: 'zhangsan@example.com', password: 'pass123', role: '开发者' },
          { id: 3, name: '李四', email: 'lisi@example.com', password: 'pass123', role: '设计师' }
        ]
        const matched = testUsers.find(u => u.email === loginForm.email && u.password === loginForm.password)
        if (matched) {
          localStorage.setItem('token', `test-token-${matched.id}`)
          localStorage.setItem('user', JSON.stringify({ id: matched.id, name: matched.name, email: matched.email, role: matched.role }))
          ElMessage.success('登录成功！(测试用户)')
          router.push('/')
          return
        }

        // Call backend API
        const response = await fetch('/api/user/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: loginForm.email,
            password: loginForm.password
          })
        })
        
        if (response.ok) {
          ElMessage.success('Login successful!')
          router.push('/dashboard')
        } else {
          const error = await response.json()
          ElMessage.error(error.message || 'Login failed')
        }
      } catch (error) {
        ElMessage.error('Network error, please try again later')
        console.error('Login error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// Reset form
const resetForm = () => {
  if (!loginFormRef.value) return
loginFormRef.value.resetFields()
}
</script>

<style scoped>
.login-container {
min-height: 100vh;
width: 100vw; 
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
display: flex;
align-items: center;
justify-content: center;
padding: 0;
margin: 0;
}

.login-wrapper {
display: flex;
width: 100%;
min-height: 100vh;
background: white;
box-shadow: none;
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

.login-card {
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

.login-form {
padding: 20px 0;
}

.login-button {
width: 100%;
margin-bottom: 10px;
}

.register-link {
text-align: center;
margin-top: 20px;
padding-top: 20px;
border-top: 1px solid #eee;
}

.register-link a {
color: #409eff;
text-decoration: none;
}

.register-link a:hover {
text-decoration: underline;
}

.image-section, .form-section { min-width: 0; }
</style>