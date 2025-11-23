<template>
  <div class="kanban-page">
    <div class="kanban-row">
      <div
          v-for="column in columns"
          :key="column.key"
          class="kanban-column"
          :data-column-key="column.key"
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
            :data-column-key="column.key"
            @end = "onDragEnd"
            animation="200"
            ghost-class="drag-ghost"
            force-fallback="true"
        >
          <template #item="{ element }">
            <el-card class="task-card" :style="{ background: getTaskColor(element.status) }" :data-id="element.id">
              <div class="task-card-content">
                <h3 class="task-title">{{ element.title }}</h3>
                <p class="task-desc">{{ element.description }}</p>
                <p class="task-assigneer">Assigneer: {{ element.assigneer }}</p>
                <p class="task-workingHour">Work Hours: {{ element.workingHour }}</p>
              </div>
              <div class="task-actions">
                <el-button
                    type="primary"
                    size="mini"
                    @click="openTaskDialog(element.status, element)"
                >Edit</el-button>
                <el-button
                    type="danger"
                    size="mini"
                    @click="deleteTask(element)"
                >Delete</el-button>
              </div>
            </el-card>
          </template>
        </draggable>
      </div>
    </div>

    <!-- Task Dialog -->
    <el-dialog title="Task" width="500px" v-model="addTaskDialogVisible">
      <el-form ref="taskFormRef" :model="newTask" :rules="rules" label-width="100px">
        <el-form-item label="Task Name" prop="title">
          <el-input v-model="newTask.title" placeholder="Please enter task name" />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input type="textarea" v-model="newTask.description" placeholder="Please enter task description" :rows="3"/>
        </el-form-item>
        <el-form-item label="Assigneer" prop="assigneer">
          <el-input v-model="newTask.assigneer" placeholder="Please enter assigneer" />
        </el-form-item>
        <el-form-item label="Work Hours" prop="workingHour">
          <el-input v-model.number="newTask.workingHour" placeholder="Please enter work hours" type="number"/>
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
import { useRoute } from 'vue-router'

// 从路由获取项目ID
const route = useRoute()
const projectId = route.params.id || 1

// 定义任务状态常量
const TaskStatus = ['todo', 'inprocess', 'codereview', 'done']

// 看板列配置
const columns = reactive([
  { key: 'todo', title: 'To-Do', tasks: [] },
  { key: 'inprocess', title: 'In-Process', tasks: [] },
  { key: 'codereview', title: 'Code Review', tasks: [] },
  { key: 'done', title: 'Done', tasks: [] }
])

// 弹窗与表单相关
const addTaskDialogVisible = ref(false)
const currentColumnKey = ref(null)
const taskFormRef = ref(null)
const editingTask = ref(null)

const newTask = reactive({
  title: '',
  description: '',
  assigneer: '',
  workingHour: '',
  status: 'todo'
})

const rules = reactive({
  title: [
    { required: true, message: 'Please enter task name', trigger: 'blur' },
    { max: 50, message: 'Task name cannot exceed 50 characters', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please enter description', trigger: 'blur' }
  ],
  assigneer: [
    { required: true, message: 'Please enter assigneer', trigger: 'blur' }
  ],
  workingHour: [
    { required: true, message: 'Please enter work hours', trigger: 'blur' },
    { type: 'number', message: 'Work hours must be a number', trigger: 'change' }
  ]
})

// 打开任务弹窗
const openTaskDialog = (columnKey, taskToEdit = null) => {
  currentColumnKey.value = columnKey
  if (taskToEdit) {
    Object.assign(newTask, { ...taskToEdit })
    editingTask.value = taskToEdit
  } else {
    Object.assign(newTask, {
      title: '',
      description: '',
      assigneer: '',
      workingHour: '',
      status: columnKey
    })
    editingTask.value = null
  }
  addTaskDialogVisible.value = true
}

// 数据映射函数 - 将后端数据格式转换为前端需要的格式
const mapTaskData = (backendData) => {
  // 如果后端返回的是项目对象，从中提取tasks数组
  const backendTasks = backendData || []

  // 定义状态映射关系（根据实际情况调整）
  const statusMap = {
    'todo': 'todo',
    'inprocess': 'inprocess',
    'codereview': 'codereview',
    'done': 'done'
  }

  // 映射每个任务
  return backendTasks.map(task => ({
    id: task.taskId || task.id, // 任务ID
    title: task.taskName || task.title, // 任务名称
    description: task.taskDesc || task.description || '', // 任务描述
    assigneer: task.assignerId || task.assigneer || '', // 负责人
    workingHour: task.hours || task.workingHour || 0, // 工作小时
    // 状态映射 - 如果没有匹配的状态，默认设为'todo'
    status: task.status || 'todo'
  }))
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    const res = await request.get(`/kanban/tasks`, { params: { projectId } })
    console.info("=========task======")
    console.info('Backend response:', res) // 打印后端原始数据，用于调试

    // 进行数据映射
    const mappedTasks = mapTaskData(res)
    console.info('Mapped tasks:', mappedTasks) // 打印映射后的数据，用于调试

    // 清空现有任务
    columns.forEach(col => col.tasks = [])

    // 将映射后的任务分配到对应的列中
    mappedTasks.forEach(task => {
      if (TaskStatus.includes(task.status)) {
        const col = columns.find(c => c.key === task.status)
        if (col) col.tasks.push(task)
      }
    })

    // ElMessage.success('Tasks fetched successfully')
  } catch (err) {
    console.error('Fetch tasks error:', err)
    ElMessage.error('Failed to fetch tasks')
  }
}

// 保存任务
const saveTask = async () => {
  if (!taskFormRef.value) return
  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const taskData = {
        ...newTask,
        status: currentColumnKey.value,
        projectId
      }

      // 注意：保存时可能需要将数据转换回后端需要的格式
      const backendTaskData = {
        taskId: taskData.id, // 如果是编辑操作
        title: taskData.title,
        description: taskData.description,
        assigneer: taskData.assigneer,
        workingHour: taskData.workingHour,
        status: taskData.status,
        projectId: projectId
        // 其他后端需要的字段...
      }

      if (editingTask.value) {
        const res = await request.put(`/kanban/tasks/${editingTask.value.id}`, backendTaskData)
        if (res.status === 200)
          ElMessage.success('Task updated successfully')
          await fetchTasks()
        // const updatedTask = mapTaskData({backendTaskData})
        // Object.assign(editingTask.value, updatedTask)

      } else {
        const res = await request.post(`/kanban/tasks/add`, backendTaskData)
        // const newTask = mapTaskData(backendTaskData)
        console.error("=========task======")
        console.info("currentColumnKey.value is: "  + currentColumnKey.value)
        console.error("=========task======")
        // const col = columns.find(c => c.key === currentColumnKey.value)
        // if (col) col.tasks.push(newTask)
        if (res.status === 200)
          ElMessage.success('Task updated successfully')
          await fetchTasks()
      }
      addTaskDialogVisible.value = false
    } catch (err) {
      console.error('Save task error:', err)
      ElMessage.error(editingTask.value ? 'Failed to update task' : 'Failed to add task')
    }
  })
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
        `Are you sure you want to delete the task "${task.title}"?`,
        'Delete Confirmation',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
    )

    await request.delete(`/task/delete/${task.id}`)
    const col = columns.find(c => c.key === task.status)
    if (col) col.tasks = col.tasks.filter(t => t.id !== task.id)

    ElMessage.success('Task deleted successfully')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Delete task error:', err)
      ElMessage.error('Failed to delete task')
    }
  }
}

//拖拽结束
const onDragEnd = async (event) => {
  console.info(" ======== 11 end ========")
  console.info("event is ======= ")
  console.info(event)

  // 1. 直接从 event.to 获取目标列的 data-column-key（最精准，因为 event.to 就是目标列的 .task-list DOM）
  const toColKey = event.to.dataset.columnKey
  // 从 event.from 获取源列的 data-column-key（可选，用于日志或判断）
  const fromColKey = event.from.dataset.columnKey

  console.log("Target column data-column-key:", toColKey)
  console.log("Source column data-column-key:", fromColKey)

  const taskId = event.item.__draggable_context.element.id
  console.info('task id is ===' + taskId)
  if (!taskId) {
    console.error("Failed to get task ID from dragged element")
    ElMessage.error('Failed to identify the task')
    return
  }
  console.info("======== end  ======= ")

  try {
    // 更新任务状态
    console.info("======== 开始更新  ======= ")
    // 你可以在此添加请求后端的代码来同步任务的状态
    await request.put(`/kanban/tasks/`+ taskId, { status: toColKey })
    // ElMessage.success('Task status updated successfully')
    await fetchTasks();
  } catch (err) {
    // 如果更新失败，恢复任务原状态
    ElMessage.error('Failed to update task status. Reverting change')
  }
}

//拖拽事件
const onDragChange = async (event) => {
  console.info("=======11  drag=======")
  console.info("event is = " + event)
  console.info(event)
  if (event.added) {
    console.error("this is added")
  }
  console.info("oldtask ====== " + event.added.element.status)
  console.info(event.added.element)

  console.info("==============oldtask ======")

  const movedTask = event.item
  const targetColumnKey = event.from.dataset.columnKey

  console.info("movedTask = " + movedTask)
  console.info("targetColumnKey ==== " + targetColumnKey)
  if (movedTask.status !== targetColumnKey) {
    try {
      // 更新任务状态
      movedTask.status = targetColumnKey
      // 你可以在此添加请求后端的代码来同步任务的状态
      await request.put(`/kanban/tasks/${movedTask.id}`, { status: targetColumnKey })
      ElMessage.success('Task status updated successfully')
    } catch (err) {
      // 如果更新失败，恢复任务原状态
      movedTask.status = event.oldIndex
      ElMessage.error('Failed to update task status. Reverting change...')
    }
  }
}

// 根据状态获取任务卡片颜色
const getTaskColor = (status) => {
  switch (status) {
    case 'todo': return '#e6f7ff'     // 淡蓝
    case 'inprocess': return '#fffbe6' // 淡黄
    case 'codereview': return '#fff0f6' // 淡粉
    case 'done': return '#f6ffed'      // 淡绿
    default: return '#f0f2f5'
  }
}

// 组件挂载时获取任务列表
onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
/* 样式部分保持不变 */
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
  overflow-x: auto;
  padding-bottom: 10px;
}

.kanban-column {
  flex: 1;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.column-title {
  font-weight: 600;
  font-size: 16px;
  color: #1f2d3d;
}

.task-list {
  flex: 1;
  min-height: 50px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  padding-right: 4px;
}

.task-card {
  position: relative;
  padding: 12px;
  border-radius: 8px;
  cursor: grab;
  transition: all 0.2s ease;
  border: 1px solid #f5f5f5;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.task-card-content {
  margin-bottom: 12px;
}

.task-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 6px;
  color: #1f2d3d;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-assigneer, .task-workingHour {
  font-size: 12px;
  color: #909399;
  margin: 3px 0;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 8px;
}

.edit-task-btn, .delete-task-btn {
  position: static;
  top: auto;
  right: auto;
  padding: 4px 10px;
  font-size: 12px;
}

.drag-ghost {
  opacity: 0.5;
  background: #f0f0f0;
}

.task-list::-webkit-scrollbar {
  width: 4px;
}

.task-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

.task-list::-webkit-scrollbar-track {
  background: transparent;
}
</style>