<template>
  <div class="project-list">
    <el-table
        :data="projects"
        border
        stripe
        style="width: 100%"
    >
      <!-- 项目名称（可点击跳转） -->
      <el-table-column
          prop="name"
          label="项目名称"
          width="200"
      >
        <template #default="scope">
          <span
              class="project-name"
              @click="goToKanban(scope.row.id)"
          >
            {{ scope.row.name }}
          </span>
        </template>
      </el-table-column>

      <!-- 项目状态 -->
      <el-table-column
          prop="status"
          label="项目状态"
          width="120"
      ></el-table-column>

      <!-- 负责人 -->
      <el-table-column
          prop="owner"
          label="负责人"
          width="120"
      ></el-table-column>

      <!-- 创建日期 -->
      <el-table-column
          prop="createdAt"
          label="创建日期"
          width="140"
      ></el-table-column>

      <!-- 计划完成时间 -->
      <el-table-column
          prop="plannedEnd"
          label="计划完成时间"
          width="160"
      ></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟项目数据
const projects = ref([
  {
    id: 1,
    name: 'AI 图像识别系统',
    status: '进行中',
    owner: '张三',
    createdAt: '2025-10-10',
    plannedEnd: '2025-12-30'
  },
  {
    id: 2,
    name: 'Web 企业管理平台',
    status: '未开始',
    owner: '李四',
    createdAt: '2025-09-01',
    plannedEnd: '2025-12-01'
  },
  {
    id: 3,
    name: '移动端项目',
    status: '已完成',
    owner: '王五',
    createdAt: '2025-05-15',
    plannedEnd: '2025-08-30'
  },
])

// 点击跳转到 Kanban
const goToKanban = (projectId) => {
  router.push(`/project/${projectId}/kanban`)
}
</script>

<style scoped>
.project-list {
  padding: 20px;
}

.project-name {
  color: #409EFF;
  cursor: pointer;
}

.project-name:hover {
  text-decoration: underline;
}
</style>
