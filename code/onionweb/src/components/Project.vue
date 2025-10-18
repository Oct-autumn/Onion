<template>
  <div class="project-view">
    <!-- 项目列表 -->
    <el-table :data="projects" style="width: 100%">
      <el-table-column prop="name" label="项目名称">
        <template #default="{ row }">
          <el-link type="primary" @click="openProject(row)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="项目状态" />
      <el-table-column prop="owner" label="负责人" />
      <el-table-column prop="created" label="创建日期" />
      <el-table-column prop="deadline" label="计划完成时间" />
    </el-table>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" width="90%" top="5vh" :title="selectedProject?.name">
      <el-tabs v-model="activeTab">
        <!-- 项目详情页 -->
        <el-tab-pane label="项目详情" name="details">
          <div class="project-details">
            <p><b>项目名称：</b>{{ selectedProject?.name }}</p>
            <p><b>状态：</b>{{ selectedProject?.status }}</p>
            <p><b>负责人：</b>{{ selectedProject?.owner }}</p>
            <p><b>计划完成时间：</b>{{ selectedProject?.deadline }}</p>
          </div>
        </el-tab-pane>

        <!-- Kanban 页面 -->
        <el-tab-pane label="Kanban" name="kanban">
          <div class="kanban-board">
            <div
                v-for="(column, key) in kanban"
                :key="key"
                class="kanban-column"
            >
              <div class="kanban-header">
                <h3>{{ column.title }}</h3>
                <el-button size="small" type="primary" text @click="addTask(key)">
                  + 添加任务
                </el-button>
              </div>

              <draggable
                  v-model="column.tasks"
                  group="tasks"
                  animation="200"
                  class="task-list"
              >
                <template #item="{ element, index }">
                  <div
                      class="task-card"
                      @contextmenu.prevent="removeTask(key, index)"
                  >
                    {{ element }}
                  </div>
                </template>
              </draggable>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import draggable from 'vuedraggable'
import { ElMessageBox, ElMessage } from 'element-plus'

const dialogVisible = ref(false)
const activeTab = ref('details')
const selectedProject = ref(null)

const projects = ref([
  { name: 'AI平台搭建', status: '进行中', owner: '张三', created: '2025-09-01', deadline: '2025-12-30' },
  { name: '前端重构', status: '已完成', owner: '李四', created: '2025-07-12', deadline: '2025-09-15' },
])

// Kanban 数据
const kanban = ref({
  todo: { title: 'To-Do', tasks: ['撰写需求文档', '设计数据库结构'] },
  progress: { title: 'In Progress', tasks: ['前端开发'] },
  review: { title: 'Code Review', tasks: ['测试接口'] },
  done: { title: 'Done', tasks: ['项目初始化'] },
})

// 打开项目详情
const openProject = (project) => {
  selectedProject.value = project
  dialogVisible.value = true
  activeTab.value = 'details'
}

// 添加任务
const addTask = async (columnKey) => {
  const { value } = await ElMessageBox.prompt('请输入任务名称', '添加任务', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：前端页面开发',
  }).catch(() => ({ value: null }))
  if (value && value.trim()) {
    kanban.value[columnKey].tasks.push(value.trim())
    ElMessage.success('任务已添加')
  }
}

// 删除任务（右键）
const removeTask = (columnKey, index) => {
  const taskName = kanban.value[columnKey].tasks[index]
  ElMessageBox.confirm(`确定要删除任务「${taskName}」吗？`, '删除任务', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(() => {
        kanban.value[columnKey].tasks.splice(index, 1)
        ElMessage.success('任务已删除')
      })
      .catch(() => {})
}
</script>

<style scoped>
.project-view {
  padding: 20px;
}

.kanban-board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.kanban-column {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  min-height: 400px;
}

.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-list {
  flex: 1;
  min-height: 200px;
}

.task-card {
  background: white;
  margin: 8px 0;
  padding: 10px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: grab;
  transition: transform 0.2s;
}

.task-card:hover {
  transform: scale(1.02);
}
</style>
