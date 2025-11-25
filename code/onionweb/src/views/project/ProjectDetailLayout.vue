<template>
  <el-container class="detail-layout">
    <!-- 顶部菜单栏 -->
    <el-header class="sub-header">
      <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          @select="handleSelect"
          background-color="#f5f5f5"
          active-text-color="#409EFF"
      >
        <el-menu-item index="kanban">Kanban</el-menu-item>
        <el-menu-item index="chart">Chart</el-menu-item>
        <el-menu-item index="info">Project Details</el-menu-item>
      </el-menu>
    </el-header>

    <!-- 内容区 -->
    <el-main class="detail-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeMenu = ref(route.name?.split('-')[1] || 'info')

watch(
    () => route.name,
    (newName) => {
      activeMenu.value = newName?.split('-')[1] || 'info'
    }
)

const handleSelect = (key) => {
  router.push(`/project/${route.params.id}/${key}`)
}
</script>

<style scoped>
.detail-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.sub-header {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.detail-main {
  padding: 20px;
  background: #fafafa;
}
</style>
