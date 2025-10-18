<template>
    <div class="profile-view">
      <!-- 用户基本信息卡片 -->
      <el-card class="profile-header-card">
        <div class="profile-header">
          <div class="user-avatar-section">
            <el-avatar :size="80" class="profile-avatar">
              {{ currentUser?.name?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="user-info-section">
            <h2 class="user-name">{{ currentUser?.name || '用户' }}</h2>
            <p class="user-email">{{ currentUser?.email || 'user@example.com' }}</p>
            <el-tag :type="getRoleType(currentUser?.role)" size="large">
              {{ currentUser?.role || '成员' }}
            </el-tag>
          </div>
          <div class="action-section">
            <el-button type="primary" @click="toggleEditMode">
              {{ isEditMode ? '取消编辑' : '编辑资料' }}
            </el-button>
          </div>
        </div>
      </el-card>
  
      <!-- 个人资料表单 -->
      <el-card class="profile-form-card">
        <template #header>
          <div class="card-header">
            <h3>个人资料</h3>
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
              <el-form-item label="姓名" prop="name">
                <el-input
                  v-model="profileForm.name"
                  placeholder="请输入姓名"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  placeholder="请输入邮箱"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="角色" prop="role">
                <el-select v-model="profileForm.role" placeholder="请选择角色" style="width: 100%">
                  <el-option label="开发者" value="开发者" />
                  <el-option label="测试员" value="测试员" />
                  <el-option label="设计师" value="设计师" />
                  <el-option label="产品经理" value="产品经理" />
                  <el-option label="项目经理" value="项目经理" />
                  <el-option label="超级管理员" value="超级管理员" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部门" prop="department">
                <el-input
                  v-model="profileForm.department"
                  placeholder="请输入部门"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入电话号码"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="入职日期" prop="joinDate">
                <el-date-picker
                  v-model="profileForm.joinDate"
                  type="date"
                  placeholder="选择入职日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="profileForm.bio"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
  
          <el-form-item v-if="isEditMode">
            <el-button type="primary" @click="saveProfile" :loading="loading">
              保存更改
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
  
      <!-- 账户设置 -->
      <el-card class="account-settings-card">
        <template #header>
          <div class="card-header">
            <h3>账户设置</h3>
          </div>
        </template>
        
        <div class="settings-list">
          <div class="setting-item">
            <div class="setting-info">
              <h4>修改密码</h4>
              <p>定期更新密码以确保账户安全</p>
            </div>
            <el-button type="primary" text @click="showChangePasswordDialog">
              修改密码
            </el-button>
          </div>
          
          <div class="setting-item">
            <div class="setting-info">
              <h4>账户状态</h4>
              <p>当前账户状态正常</p>
            </div>
            <el-tag type="success">正常</el-tag>
          </div>
        </div>
      </el-card>
  
      <!-- 修改密码弹窗 -->
      <el-dialog v-model="changePasswordDialogVisible" title="修改密码" width="500px">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="changePasswordDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="changePassword">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  
  // 响应式数据
  const isEditMode = ref(false)
  const loading = ref(false)
  const changePasswordDialogVisible = ref(false)
  const profileFormRef = ref(null)
  const passwordFormRef = ref(null)
  
  // 当前用户信息
  const currentUser = ref(null)
  
  // 个人资料表单
  const profileForm = reactive({
    name: '',
    email: '',
    role: '',
    department: '',
    phone: '',
    joinDate: '',
    bio: ''
  })
  
  // 密码修改表单
  const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  
  // 表单验证规则
  const rules = {
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    role: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ]
  }
  
  const passwordRules = {
    currentPassword: [
      { required: true, message: '请输入当前密码', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== passwordForm.newPassword) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
  
  // 获取角色标签类型
  const getRoleType = (role) => {
    const roleTypes = {
      '项目负责人': 'danger',
      '项目经理': 'warning',
      '开发者': 'primary',
      '测试员': 'success',
      '设计师': 'info',
      '产品经理': '',
      '超级管理员': 'danger'
    }
    return roleTypes[role] || ''
  }
  
  // 获取当前用户信息
  onMounted(() => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      currentUser.value = JSON.parse(userStr)
      // 初始化表单数据
      Object.assign(profileForm, {
        name: currentUser.value.name || '',
        email: currentUser.value.email || '',
        role: currentUser.value.role || '',
        department: '技术部',
        phone: '138****8888',
        joinDate: '2024-01-01',
        bio: '这是一个热爱技术的开发者，专注于前端开发和用户体验设计。'
      })
    }
  })
  
  // 切换编辑模式
  const toggleEditMode = () => {
    isEditMode.value = !isEditMode.value
    if (!isEditMode.value) {
      resetForm()
    }
  }
  
  // 保存个人资料
  const saveProfile = async () => {
    if (!profileFormRef.value) return
    
    try {
      await profileFormRef.value.validate()
      loading.value = true
      
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 更新本地存储
      const updatedUser = { ...currentUser.value, ...profileForm }
      localStorage.setItem('user', JSON.stringify(updatedUser))
      currentUser.value = updatedUser
      
      ElMessage.success('个人资料更新成功')
      isEditMode.value = false
    } catch (error) {
      console.log('表单验证失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  // 重置表单
  const resetForm = () => {
    if (profileFormRef.value) {
      profileFormRef.value.resetFields()
    }
  }
  
  // 显示修改密码弹窗
  const showChangePasswordDialog = () => {
    changePasswordDialogVisible.value = true
    // 重置密码表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  }
  
  // 修改密码
  const changePassword = async () => {
    if (!passwordFormRef.value) return
    
    try {
      await passwordFormRef.value.validate()
      
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success('密码修改成功')
      changePasswordDialogVisible.value = false
    } catch (error) {
      console.log('表单验证失败:', error)
    }
  }
  </script>
  
  <style scoped>
  .profile-view {
    padding: 20px;
  }
  
  /* 头部卡片样式 */
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
  
  /* 卡片头部样式 */
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
  
  /* 表单样式 */
  .profile-form {
    max-width: 800px;
  }
  
  /* 设置列表样式 */
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
  
  /* 弹窗样式 */
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
  
  /* 卡片间距 */
  .profile-form-card,
  .account-settings-card {
    margin-bottom: 20px;
  }
  
  /* 响应式设计 */
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