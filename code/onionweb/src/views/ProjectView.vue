<template>
  <el-card>
    <div class="header">
      <h2>Project Overview</h2>
      <el-button type="primary" @click="showAddProjectDialog = true">
        Create New Project
      </el-button>
    </div>

    <el-table
        :data="projects"
        style="width: 100%; margin: 0 auto;"
        stripe
        border
        v-loading="loading"
        @cell-click="handleCellClick"
    >
      <el-table-column
          prop="name"
          label="Project Name"
      >
        <template #default="{ row }">
          <el-link type="primary" @click="goToProject(row.projectId)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status">
        <template #default="scope">
          <el-select
              v-if="scope.row.projectId === editingRowId && scope.column.label === 'Status'"
              v-model="scope.row.status"
              @change="handleSave(scope.row)"
              style="width: 100%;"
              placeholder="Select status"
          >
            <el-option label="Not Started" value="Not Started"></el-option>
            <el-option label="In Progress" value="In Progress"></el-option>
            <el-option label="Completed" value="Completed"></el-option>
          </el-select>
          <span
              v-else
              :style="{
          color: {
                'Not Started': '#f56c6c',
                'In Progress': '#409eff',
                'QA': '#e6a23c',
                'Completed': '#67c23a'
              }[scope.row.status] || 'inherit',
          fontWeight: '500'
        }"
          >
        {{ scope.row.status }}
      </span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="Project Owner" />
      <el-table-column prop="createTime" label="Creation Date">
      </el-table-column>
      <el-table-column prop="expectedCompletion" label="Expected Completion Date">
      </el-table-column>
    </el-table>

    <!-- Pagination section -->
    <div class="pagination-container">
      <div class="page-size-select">
        <span>Items per page:</span>
        <el-select v-model="numProjectsPerPage" size="small" style="width: 80px; margin: 0 10px;" @change="handlePageSizeChange">
          <el-option label="5" :value="5"></el-option>
          <el-option label="10" :value="10"></el-option>
          <el-option label="20" :value="20"></el-option>
        </el-select>
      </div>

      <el-pagination
          background
          layout="prev, pager, next, ->, total"
          :current-page="currentPage"
          :page-size="numProjectsPerPage"
          :total="totalProjects"
          @current-change="handlePageChange"
      />
    </div>

    <!-- Add Project Dialog -->
    <el-dialog
        v-model="showAddProjectDialog"
        title="Create New Project"
        width="550px"
        :before-close="handleDialogClose"
    >
      <el-form :model="newProjectForm" :rules="formRules" ref="projectFormRef" label-width="250px">
        <el-form-item label="Project Name" prop="name">
          <el-input v-model="newProjectForm.name" placeholder="Enter project name" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="newProjectForm.status" placeholder="Select project status" style="width: 100%">
            <el-option label="Not Started" value="Not Started"></el-option>
            <el-option label="In Progress" value="In Progress"></el-option>
            <el-option label="Completed" value="Completed"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Expected Completion Date" prop="planDate">
          <el-date-picker
              v-model="newProjectForm.planDate"
              type="date"
              placeholder="Select date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">Cancel</el-button>
          <el-button type="primary" @click="submitNewProject">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js";

const projectFormRef = ref()
const router = useRouter()

// Pagination variables
const currentPage = ref(1)
const numProjectsPerPage = ref(5)
const totalProjects = ref(0)
const loading = ref(false)

// Reactive array for projects
const projects = ref([])

// Dialog control
const showAddProjectDialog = ref(false)

const editingRowId = ref(null)

// Form data
const newProjectForm = ref({
  name: '',
  status: '',
  planDate: '',
})

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Please enter the project name', trigger: 'blur' }
  ],
  status: [
    { required: true, message: 'Please select the project status', trigger: 'change' }
  ],
  planDate: [
    { required: true, message: 'Please select the expected completion date', trigger: 'change' }
  ]
}



// 点击单元格时进入编辑状态
const handleCellClick = (row, column) => {
  console.info("=====handleCellClick ==")
  console.log('点击可编辑列，userId：', row.projectId, '列的 fixed 值：', column.label)
  if (column.label === 'Status') {
    console.log('点击可编辑列，userId：', row.projectId, '列的 fixed 值：', column.fixed)
    editingRowId.value = row.projectId
  }
  console.info("=====editingRowId ==" + editingRowId)

}

// 保存编辑
const handleSave = async (row) => {
  try {
    console.info("====handleSave=====")

    await request.put('/project/update', {
        projectId: row.projectId,
        status: row.status, // 使用新值

    });
    ElMessage.success('Status updated successfully');
    editingRowId.value = null // 退出编辑状态
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || 'Failed to update member')
    // 如果更新失败，可以回滚数据
  }
  await fetchProjects();
}

/**
 * Format date string (YYYYMMDD -> YYYY-MM-DD)
 * @param {string} dateStr - Original date string
 * @returns {string} Formatted date string
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const year = dateStr.substring(0, 4)
  const month = dateStr.substring(4, 6)
  const day = dateStr.substring(6, 8)
  return `${year}-${month}-${day}`
}

/**
 * Fetch the list of projects
 */
const fetchProjects = async () => {
  loading.value = true
  try {
    const res = await request.get('/project/list', {
      params: {
        page: currentPage.value,
        pageSize: numProjectsPerPage.value
      }
    })
    console.info("==========")
    console.info(res)
    const projectList = res || []
    console.info(projectList)
    const processedProjects = projectList.map(project => ({
      projectId: project.projectId,
      name: project.name,
      status: project.status || 'Not Started',
      userName: project.userName,
      createTime: project.createTime,
      expectedCompletion: project.expectedCompletion,
    }))

    console.info("processedProjects = ")
    console.info(processedProjects)

    projects.value = processedProjects
    totalProjects.value = res.total || projectList.length

  } catch (err) {
    console.error(err)
    ElMessage.error('Network error, failed to fetch project list')
  } finally {
    loading.value = false
  }
}

// Page change
const handlePageChange = (page) => {
  currentPage.value = page
  fetchProjects()
}

// Page size change
const handlePageSizeChange = (size) => {
  currentPage.value = 1
  numProjectsPerPage.value = size
  fetchProjects()
}

// Go to project details page
const goToProject = (projectId) => {
  router.push(`/project/${projectId}/kanban`)
}

// Handle dialog close
const handleDialogClose = () => {
  showAddProjectDialog.value = false
  if (projectFormRef.value) {
    projectFormRef.value.resetFields()
  }
  newProjectForm.value = {
    name: '',
    status: '',
    planDate: '',
  }
}

// Submit new project
const submitNewProject = async () => {
  if (!projectFormRef.value) return

  try {
    await projectFormRef.value.validate()

    const projectData = {
      name: newProjectForm.value.name,
      status: newProjectForm.value.status,
      expectedCompletion: newProjectForm.value.planDate,
      // Add other fields as needed by backend
    }

    await request.post('/project/create', projectData)

    ElMessage.success('Project created successfully')
    handleDialogClose()
    fetchProjects()

  } catch (error) {
    console.error('Failed to create project:', error)
    ElMessage.error(error.message || 'Failed to create project')
  }
}

/**
 * 修改项目状态
 */
const handleStatusChange = async (row) => {
  // 添加加载状态，防止重复提交
  row.updating = true;
  try {
    await request.get('/project/update', {
      params: {
        projectId: row.projectId,
        status: newStatus, // 使用新值
      }
    });
    ElMessage.success('Status updated successfully');
  } catch (err) {
    console.error(err);
    ElMessage.error('Failed to update status');
    // 失败时回滚状态
    row.status = row.status;
  } finally {
    // 移除加载状态
    row.updating = false;
  }
};

// Fetch project list on component mount
onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.page-size-select {
  display: flex;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>