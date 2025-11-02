<template>
  <el-card>
    <h2>项目列表</h2>
    <el-table
        :data="projects"
        style="width: 100%"
        stripe
        border
    >
      <el-table-column
          prop="name"
          label="项目名称"
          width="200"
      >
        <template #default="{ row }">
          <el-link type="primary" @click="goToProject(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="项目状态" width="120"/>
      <el-table-column prop="owner" label="负责人" width="120"/>
      <el-table-column prop="createDate" label="创建日期" width="150"/>
      <el-table-column prop="planDate" label="计划完成时间" width="150"/>
    </el-table>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { reactive } from 'vue'

const router = useRouter()

const projects = reactive([
  {
    id: 1,
    name: 'AI 图像识别系统',
    status: '进行中',
    owner: '张三',
    createDate: '2025-10-10',
    planDate: '2025-12-30'
  },
  {
    id: 2,
    name: '前端管理系统',
    status: '未开始',
    owner: '李四',
    createDate: '2025-09-15',
    planDate: '2025-12-20'
  }
])

const goToProject = (id) => {
  router.push(`/project/${id}/kanban`)
}
</script>
