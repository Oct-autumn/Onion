<template>
  <div class="kanban-page">
    <div class="kanban-row">
      <div
          v-for="column in columns"
          :key="column.key"
          class="kanban-column"
      >
        <div class="column-header">
          <span class="column-title">{{ column.title }}</span>
          <el-button size="mini" type="primary" @click="openAddTaskDialog(column.key)">
            + 新增任务
          </el-button>
        </div>

        <draggable
            v-model="column.tasks"
            group="tasks"
            item-key="id"
            class="task-list"
            animation="200"
            ghost-class="drag-ghost"
        >
          <template #item="{ element }">
            <el-card class="task-card" :style="{ background: getTaskColor(element.status) }">
              <h3 class="task-title">{{ element.title }}</h3>
              <p class="task-desc">{{ element.description }}</p>
              <p class="task-assigneer">责任人: {{ element.assigneer }}</p>
              <p class="task-workingHour">工时: {{ element.workingHour }}</p>
            </el-card>
          </template>
        </draggable>
      </div>
    </div>

    <!-- 新增任务弹窗 -->
    <el-dialog title="新增任务" width="500px" v-model="addTaskDialogVisible">
      <el-form ref="taskFormRef" :model="newTask" label-width="100px">
        <el-form-item label="任务名称" prop="title">
          <el-input v-model="newTask.title" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input type="textarea" v-model="newTask.description" placeholder="请输入任务描述" :rows="3"/>
        </el-form-item>
        <el-form-item label="责任人" prop="assigneer">
          <el-input v-model="newTask.assigneer" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="工时" prop="workingHour">
          <el-input v-model="newTask.workingHour" placeholder="请输入完成工时" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTaskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addTask">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import draggable from 'vuedraggable'

const columns = reactive([
  { key: 'backlog', title: 'Backlog', tasks: [] },
  { key: 'todo', title: 'To-Do', tasks: [] },
  { key: 'inprocess', title: 'In-Process', tasks: [] },
  { key: 'done', title: 'Done', tasks: [] }
])

const addTaskDialogVisible = ref(false)
const currentColumnKey = ref(null)
const newTask = reactive({ title: '', description: '', assigneer: '', workingHour: '' })
const taskFormRef = ref(null)

const openAddTaskDialog = (columnKey) => {
  currentColumnKey.value = columnKey
  Object.assign(newTask, { title: '', description: '', assigneer: '', workingHour: '' })
  addTaskDialogVisible.value = true
}

const addTask = () => {
  if (!newTask.title || !newTask.description || !newTask.assigneer || !newTask.workingHour) return
  const column = columns.find(c => c.key === currentColumnKey.value)
  if (column) {
    column.tasks.push({
      ...newTask,
      id: Date.now() + Math.random(),
      status: column.key
    })
  }
  addTaskDialogVisible.value = false
}

const getTaskColor = (status) => {
  switch (status) {
    case 'backlog': return '#fef6f6'
    case 'todo': return '#e6f7ff'
    case 'inprocess': return '#fffbe6'
    case 'done': return '#f6ffed'
    default: return '#f0f2f5'
  }
}
</script>

<style scoped>
.kanban-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 10px;
  background: #f0f2f5;
}
.kanban-row {
  display: flex;
  flex: 1;
  gap: 12px;
}
.kanban-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
}
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.column-title {
  font-weight: bold;
  font-size: 18px;
  color: #1f2d3d;
}
.task-list {
  flex: 1;
  min-height: 50px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}
.task-card {
  padding: 12px;
  border-radius: 10px;
  cursor: grab;
  transition: all 0.2s;
}
.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.task-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 4px;
}
.task-desc, .task-assigneer, .task-workingHour {
  font-size: 14px;
  color: #606266;
  margin: 2px 0;
}
.drag-ghost {
  opacity: 0.5;
}
</style>
