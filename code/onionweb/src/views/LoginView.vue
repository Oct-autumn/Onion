<template>
  <div class="login-container">
      <div class="login-wrapper">
          <div class="image-section">
          <div class="image-content">
              <h1 class="welcome-title">Welcome Back</h1>
          <p class="welcome-subtitle">Sign in to continue your Project Management journey</p>
          <div class="image-placeholder">
            <img src="/Onion_slogan_word.png" alt="Onion Slogan" class="welcome-image" />
          </div>
        </div>
      </div>

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
import request from '@/utils/request.js'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: ''
})

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

const submitForm = async () => { // 确保外层函数是 async
  if (!loginFormRef.value) return;

  // 注意：validate 的回调函数不能是 async！需将 await 移到 validate 外部
  const isValid = await loginFormRef.value.validate(); // 先执行校验，获取结果

  if (isValid) { // 校验通过再执行请求
    loading.value = true;
    try {
      // 直接使用封装的 request 发起请求（无需嵌套 try）
      const response = await request.post('/user/login', {
        email: loginForm.email,
        password: loginForm.password
      });

      // 登录成功：存储 Token 和用户信息
      const { authToken, userId, username, role } = response;
      localStorage.setItem('token', authToken); // 与 axios 封装中读取的 key 一致
      localStorage.setItem('userInfo', JSON.stringify({ userId, username, email: loginForm.email, role }));

      ElMessage.success('Login successful!');
      router.push('/project');
    } catch (error) {
      // 捕获请求错误（包括网络错误、后端返回的错误）
      ElMessage.error(error.message || 'Login failed');
      console.error('Login error:', error);
    } finally {
      loading.value = false; // 无论成功失败，都关闭加载
    }
  }
};

const resetForm = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.resetFields()
};
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