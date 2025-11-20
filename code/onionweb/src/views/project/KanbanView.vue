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
          <el-button size="mini" type="primary" @click="openTaskDialog(column.key)">
            + Add Task
          </el-button>
        </div>

        <draggable
            v-model="column.tasks"
            group="tasks"
            item-key="id"
            class="task-list"
            animation="200"
            ghost-class="drag-ghost"
            @end="onTaskDragEnd"
        >
          <template #item="{ element }">
            <el-card class="task-card" :style="{ background: getTaskColor(element.status) }">
              <div class="task-card-content">
                <h3 class="task-title">{{ element.title }}</h3>
                <p class="task-desc">{{ element.description }}</p>
                <p class="task-assigneer">Assignee: {{ element.assigneer }}</p>
                <p class="task-workingHour">Work Hours: {{ element.workingHour }}</p>
              </div>
              <el-button
                  type="primary"
                  size="mini"
                  class="edit-task-btn"
                  @click="openTaskDialog(element.status, element)"
              >Edit</el-button>
              <el-button
                  type="danger"
                  size="mini"
                  class="delete-task-btn"
                  @click="deleteTask(element)"
              >Delete</el-button>
            </el-card>
          </template>
        </draggable>
      </div>
    </div>

    <!-- Task Dialog -->
    <el-dialog title="Task" width="500px" v-model="addTaskDialogVisible">
      <el-form ref="taskFormRef" :model="newTask" label-width="100px">
        <el-form-item label="Task Name" prop="title">
          <el-input v-model="newTask.title" placeholder="Please enter task name" />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input type="textarea" v-model="newTask.description" placeholder="Please enter task description" :rows="3"/>
        </el-form-item>
        <el-form-item label="Assignee" prop="assigneer">
          <el-input v-model="newTask.assigneer" placeholder="Please enter assignee" />
        </el-form-item>
        <el-form-item label="Work Hours" prop="workingHour">
          <el-input v-model="newTask.workingHour" placeholder="Please enter work hours" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTaskDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveTask">Confirm</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const projectId = 1 // Can be changed to get from dynamic route

const columns = reactive([
  { key: 'todo', title: 'To-Do', tasks: [] },
  { key: 'inprocess', title: 'In-Process', tasks: [] },
  { key: 'codereview', title: 'Code Review', tasks: [] },
  { key: 'done', title: 'Done', tasks: [] }
])

const addTaskDialogVisible = ref(false)
const currentColumnKey = ref(null)
const newTask = reactive({ title: '', description: '', assigneer: '', workingHour: '' })
const taskFormRef = ref(null)
const editingTask = ref(null) // Currently editing task

// Open task dialog (add or edit)
const openTaskDialog = (columnKey, taskToEdit = null) => {
  currentColumnKey.value = columnKey
  if (taskToEdit) {
    Object.assign(newTask, { ...taskToEdit })
    editingTask.value = taskToEdit
  } else {
    Object.assign(newTask, { title: '', description: '', assigneer: '', workingHour: '' })
    editingTask.value = null
  }
  addTaskDialogVisible.value = true
}

// Fetch task list
const fetchTasks = async () => {
  try {
    const res = await request.get(`/kanban/tasks`, { params: { projectId } })
    columns.forEach(c => c.tasks = [])
    res.data.forEach(task => {
      const col = columns.find(c => c.key === task.status)
      if (col) col.tasks.push(task)
    })
  } catch (err) {
    console.error(err)
    ElMessage.error('Failed to fetch tasks')
  }
}

// Save task (add or edit)
const saveTask = async () => {
  if (!newTask.title || !newTask.description || !newTask.assigneer || !newTask.workingHour) {
    ElMessage.warning('Please fill in all task information')
    return
  }

  try {
    if (editingTask.value) {
      // Edit task
      const res = await request.put(`/kanban/tasks/${editingTask.value.id}`, {
        ...newTask,
        status: currentColumnKey.value,
        projectId
      })
      Object.assign(editingTask.value, res.data)
      ElMessage.success('Task updated successfully')
    } else {
      // Add new task
      const res = await request.post(`/kanban/tasks`, {
        ...newTask,
        status: currentColumnKey.value,
        projectId
      })
      const col = columns.find(c => c.key === currentColumnKey.value)
      if (col) col.tasks.push(res.data)
      ElMessage.success('Task added successfully')
    }
    addTaskDialogVisible.value = false
  } catch (err) {
    console.error(err)
    ElMessage.error(editingTask.value ? 'Failed to update task' : 'Failed to add task')
  }
}

// Delete task
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`Are you sure you want to delete the task "${task.title}"?`, 'Delete Confirmation', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await request.delete(`/kanban/tasks/${task.id}`)
    const col = columns.find(c => c.key === task.status)
    if (col) col.tasks = col.tasks.filter(t => t.id !== task.id)
    ElMessage.success('Task deleted successfully')
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err)
      ElMessage.error('Failed to delete task')
    }
  }
}

// Drag end event
const onTaskDragEnd = async (evt) => {
  const movedTask = evt.item.__vue__.element
  const newStatus = evt.to.__vue__.column.key
  if (movedTask.status !== newStatus) {
    movedTask.status = newStatus
    try {
      await request.put(`/kanban/tasks/${movedTask.id}`, { status: newStatus })
      ElMessage.success('Task status updated successfully')
    } catch (err) {
      console.error(err)
      ElMessage.error('Failed to update task status')
      fetchTasks()
    }
  }
}

// Get task card color based on status
const getTaskColor = (status) => {
  switch (status) {
    case 'todo': return '#e6f7ff'
    case 'inprocess': return '#fffbe6'
    case 'codereview': return '#fff0f6'
    case 'done': return '#f6ffed'
    default: return '#f0f2f5'
  }
}

onMounted(() => {
  fetchTasks()
})
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
  position: relative;
  padding: 12px;
  border-radius: 10px;
  cursor: grab;
  transition: all 0.2s;
}
.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.task-card-content {
  margin-bottom: 6px;
}
.delete-task-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}
.edit-task-btn {
  position: absolute;
  top: 8px;
  right: 50px;
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