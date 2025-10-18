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
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-header>


    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.path)

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
</style>
