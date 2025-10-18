<template>
  <div class="kanban-page">
    <el-card>
      <h2>Kanban 看板</h2>
      <p>这里未来可以展示任务卡片、拖拽功能等。</p>
      <p>当前项目 ID：{{ $route.params.id }}</p>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.kanban-page {
  padding: 10px;
}
</style>
