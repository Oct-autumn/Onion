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
            @end="onTaskDragEnd"
        >
          <template #item="{ element }">
            <el-card class="task-card" :style="{ background: getTaskColor(element.status) }">
              <div class="task-card-content">
                <h3 class="task-title">{{ element.title }}</h3>
                <p class="task-desc">{{ element.description }}</p>
                <p class="task-assigneer">责任人: {{ element.assigneer }}</p>
                <p class="task-workingHour">工时: {{ element.workingHour }}</p>
              </div>
              <el-button
                  type="primary"
                  size="mini"
                  class="edit-task-btn"
                  @click="openTaskDialog(element.status, element)"
              >编辑</el-button>
              <el-button
                  type="danger"
                  size="mini"
                  class="delete-task-btn"
                  @click="deleteTask(element)"
              >删除</el-button>
            </el-card>
          </template>
        </draggable>
      </div>
    </div>

    <!-- 任务弹窗 -->
    <el-dialog title="任务" width="500px" v-model="addTaskDialogVisible">
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
        <el-button type="primary" @click="saveTask">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const projectId = 1 // 可以改为动态路由获取

const columns = reactive([
  { key: 'todo', title: 'To-Do', tasks: [] },
  { key: 'inprocess', title: 'In-Process', tasks: [] },
  { key: 'codereview', title: 'codeReview', tasks: [] },
  { key: 'done', title: 'Done', tasks: [] }
])

const addTaskDialogVisible = ref(false)
const currentColumnKey = ref(null)
const newTask = reactive({ title: '', description: '', assigneer: '', workingHour: '' })
const taskFormRef = ref(null)
const editingTask = ref(null) // 当前编辑的任务

// 打开任务弹窗（新增或编辑）
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

// 获取任务列表
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
    ElMessage.error('获取任务失败')
  }
}

// 保存任务（新增或编辑）
const saveTask = async () => {
  if (!newTask.title || !newTask.description || !newTask.assigneer || !newTask.workingHour) {
    ElMessage.warning('请填写完整任务信息')
    return
  }

  try {
    if (editingTask.value) {
      // 编辑任务
      const res = await request.put(`/kanban/tasks/${editingTask.value.id}`, {
        ...newTask,
        status: currentColumnKey.value,
        projectId
      })
      Object.assign(editingTask.value, res.data)
      ElMessage.success('任务更新成功')
    } else {
      // 新增任务
      const res = await request.post(`/kanban/tasks`, {
        ...newTask,
        status: currentColumnKey.value,
        projectId
      })
      const col = columns.find(c => c.key === currentColumnKey.value)
      if (col) col.tasks.push(res.data)
      ElMessage.success('任务添加成功')
    }
    addTaskDialogVisible.value = false
  } catch (err) {
    console.error(err)
    ElMessage.error(editingTask.value ? '更新任务失败' : '添加任务失败')
  }
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定删除任务 "${task.title}" 吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/kanban/tasks/${task.id}`)
    const col = columns.find(c => c.key === task.status)
    if (col) col.tasks = col.tasks.filter(t => t.id !== task.id)
    ElMessage.success('任务删除成功')
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err)
      ElMessage.error('删除任务失败')
    }
  }
}

// 拖拽结束
const onTaskDragEnd = async (evt) => {
  const movedTask = evt.item.__vue__.element
  const newStatus = evt.to.__vue__.column.key
  if (movedTask.status !== newStatus) {
    movedTask.status = newStatus
    try {
      await request.put(`/kanban/tasks/${movedTask.id}`, { status: newStatus })
      ElMessage.success('任务状态更新成功')
    } catch (err) {
      console.error(err)
      ElMessage.error('更新任务状态失败')
      fetchTasks()
    }
  }
}

// 获取任务颜色
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
