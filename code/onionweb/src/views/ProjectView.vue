<template>
  <el-card>
    <div class="header">
      <h2>Projects Overview</h2>
      <el-button type="primary" @click="showAddProjectDialog = true">
        New Project
      </el-button>
    </div>

    <el-table
        :data="projects"
        style="width: 100%; margin: 0 auto;"
        stripe
        border
        v-loading="loading"
    >
      <el-table-column
          prop="name"
          label="Project Title"
      >
        <template #default="{ row }">
          <el-link type="primary" @click="goToProject(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" />
      <el-table-column prop="owner" label="Project Owner" />
      <el-table-column prop="createDate" label="Create date" />
      <el-table-column prop="planDate" label="Expected Completion Date"/>
    </el-table>

    <!-- Pagination section -->
    <div class="pagination-container">
      <div class="page-size-select">
        <span>每页显示：</span>
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
        <el-form-item label="Project Title" prop="name">
          <el-input v-model="newProjectForm.name" placeholder="New project" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="newProjectForm.status" placeholder="Please select" style="width: 100%">
            <el-option label="未开始" value="未开始"></el-option>
            <el-option label="进行中" value="进行中"></el-option>
            <el-option label="已完成" value="已完成"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Expected Completion Date" prop="planDate">
          <el-date-picker
              v-model="newProjectForm.planDate"
              type="date"
              placeholder="Pick a date"
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
import { ElMessage, ElMessageBox } from 'element-plus'

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

// Form data
const newProjectForm = ref({
  name: '',
  status: '',
  planDate: '',
})

// Form validation rules
const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ],
  planDate: [
    { required: true, message: '请选择计划完成时间', trigger: 'change' }
  ]
}

// Local dummy test projects data
const dummyProjects = {
  data: [
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
  ]
}

const fetchProjects = async () => {
  loading.value = true
  try {

    const res = await request.get('/project/list', {
      params: { 
        page: currentPage.value, 
        pageSize: numProjectsPerPage.value 
      }
    })
	
    projects.value = res.data.projects
    totalProjects.value = res.data.total
    
  } catch (err) {
    console.error(err)
    ElMessage.error('网络错误，获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchProjects() // Fetch projects when page changes
}

// 每页数量变化
const handlePageSizeChange = (size) => {
  currentPage.value = 1 // Reset to first page when page size changes
  fetchProjects() // Fetch projects with new page size
}

const goToProject = (id) => {
  router.push(`/project/${id}/kanban`)
}

// Handle dialog close
const handleDialogClose = () => {
  showAddProjectDialog.value = false
  // Reset form
  newProjectForm.value = {
    name: '',
    status: '',
    planDate: '',
  }
}

// Submit new project
const submitNewProject = async () => {
  // Validate form first
  projectFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const projectData = {
          name: newProjectForm.name,
          status: newProjectForm.status,
          planDate: newProjectForm.planDate
        }

        // Make API call to create new project
        const response = await fetch('/api/project', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(projectData)
        })

        if (response.ok) {
          ElMessage.success('Project created successfully.')
          
          // Close dialog and reset form
          handleDialogClose()
          
          // Refresh the project list
          fetchProjects()
        } else {
          const errorData = await response.json()
          console.error('Error creating project:', errorData)
          ElMessage.error('Failed to create project')
        }
      } catch (error) {
        console.error('Network error creating project:', error)
        ElMessage.error('Failed to create project')
      }
    } else {
      ElMessage.error('Please fill in all required fields')
      return false
    }
  })
}

// Fetch projects when component mounts
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
