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

        <!-- 拖拽任务区域 -->
        <draggable
            v-model="column.tasks"
            group="tasks"
            item-key="id"
            class="task-list"
            animation="200"
            ghost-class="drag-ghost"
            @end="onDragEnd"
        >
          <template #item="{ element }">
            <el-card class="task-card" :style="{ background: getTaskColor(element.status) }">
              <div class="task-card-content">
                <h3 class="task-title">{{ element.title }}</h3>
                <p class="task-desc">{{ element.description }}</p>
                <p class="task-assigneer">责任人: {{ element.assigneer }}</p>
                <p class="task-workingHour">工时: {{ element.workingHour }}</p>
                <el-button
                    type="danger"
                    size="small"
                    text
                    class="delete-btn"
                    @click="deleteTaskItem(element.id, column.key)"
                >
                  删除
                </el-button>
              </div>
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
          <el-input
              type="textarea"
              v-model="newTask.description"
              placeholder="请输入任务描述"
              :rows="3"
          />
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
import { reactive, ref, onMounted } from 'vue'
import draggable from 'vuedraggable'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasksApi, addTaskApi, updateTaskApi, deleteTaskApi } from '@/api/task'

const route = useRoute()
const projectId = route.params.id

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

// ✅ 页面加载时获取任务列表
onMounted(async () => {
  await loadTasks()
})

const loadTasks = async () => {
  try {
    const res = await getTasksApi(projectId)
    const tasks = res.data || []
    columns.forEach(col => {
      col.tasks = tasks.filter(task => task.status === col.key)
    })
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

// ✅ 打开新增任务弹窗
const openAddTaskDialog = (columnKey) => {
  currentColumnKey.value = columnKey
  Object.assign(newTask, { title: '', description: '', assigneer: '', workingHour: '' })
  addTaskDialogVisible.value = true
}

// ✅ 新增任务
const addTask = async () => {
  if (!newTask.title || !newTask.description || !newTask.assigneer || !newTask.workingHour) {
    ElMessage.warning('请填写完整任务信息')
    return
  }

  try {
    const { data } = await addTaskApi(projectId, {
      ...newTask,
      status: currentColumnKey.value
    })
    const column = columns.find(c => c.key === currentColumnKey.value)
    if (column) column.tasks.push(data)
    addTaskDialogVisible.value = false
    ElMessage.success('任务创建成功')
  } catch (e) {
    ElMessage.error('创建任务失败')
  }
}

// ✅ 拖拽任务后更新状态
const onDragEnd = async (event) => {
  try {
    const movedTask = event.item.__vueParentComponent.props.element
    const newColumnEl = event.to.closest('.kanban-column')
    if (!newColumnEl) return

    const newColumnKey = newColumnEl.__vnode.key
    if (movedTask.status !== newColumnKey) {
      movedTask.status = newColumnKey
      await updateTaskApi(movedTask.id, { status: newColumnKey })
    }
  } catch (e) {
    console.error('任务状态更新失败', e)
  }
}

// ✅ 删除任务
const deleteTaskItem = (id, columnKey) => {
  ElMessageBox.confirm('确认删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTaskApi(id)
      const column = columns.find(c => c.key === columnKey)
      column.tasks = column.tasks.filter(t => t.id !== id)
      ElMessage.success('删除成功')
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// ✅ 任务卡片颜色
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
  position: relative;
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
.delete-btn {
  position: absolute;
  top: 6px;
  right: 8px;
  color: #f56c6c;
}
.drag-ghost {
  opacity: 0.5;
}
</style>
