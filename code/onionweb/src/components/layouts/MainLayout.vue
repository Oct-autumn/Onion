<template>
  <el-container class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          background-color="#f5f5f5"
          text-color="#606266"
          active-text-color="#409EFF"
          @select="handleSelect"
          class="menu"
      >
        <el-menu-item index="/project">
          <el-icon><Message /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>

    <!-- 用户信息区域 -->
    <div class="user-info">
        <el-dropdown @command="handleUserCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" class="user-avatar">
              {{ currentUser?.name?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="user-name">{{ currentUser?.name || '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>


    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, User, UserFilled, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.path)
const currentUser = ref(null)
const isAdmin = ref(false)

// 获取当前用户信息
onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
    const role = currentUser.value?.role
    isAdmin.value = role === '超级管理员' || role === 'admin' || role === '管理员'
  }
})

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      currentUser.value = null
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {
      // 用户取消退出
    })
  }
}

// 点击菜单切换路由
const handleSelect = (key) => {
  router.push(key)
}

// 监听路由变化，自动更新菜单高亮
watch(
    () => route.path,
    (newPath) => {
      activeMenu.value = newPath
    }
)
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0; /* 去掉内边距 */
}

.menu {
  flex: 1;
  width: 100%;
  border-bottom: none; /* 可选，去掉底部线条 */
  justify-content: flex-start; /* 菜单从左对齐 */
}


.main-content {
  flex: 1;
  padding: 24px;
  background: #fafafa;
}

.user-info {
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f0f0f0;
}

.user-avatar {
  margin-right: 8px;
  background-color: #409EFF;
  color: white;
}

.user-name {
  margin-right: 8px;
  font-weight: 500;
  color: #606266;
}
</style>
