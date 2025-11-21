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
// 导入封装的 axios 实例
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

// fetchProjects 函数看起来已经正确使用了封装的 request，这里保持不变
const fetchProjects = async () => {
  loading.value = true
  try {
    // 注意：这里假设你的后端 API 返回的数据结构是 { data: { projects: [...], total: 10 } }
    // 如果实际结构不同，请根据后端返回进行调整
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
  // 关闭弹窗时重置表单和校验状态
  if (projectFormRef.value) {
    projectFormRef.value.resetFields();
  }
  newProjectForm.value = {
    name: '',
    status: '',
    planDate: '',
  }
}

// 1. 修改 submitNewProject 函数
const submitNewProject = async () => {
  if (!projectFormRef.value) return;

  try {
    // 使用 async/await 方式进行表单校验
    await projectFormRef.value.validate();

    const projectData = {
      name: newProjectForm.value.name,
      status: newProjectForm.value.status,
      planDate: newProjectForm.value.planDate
    }

    // 2. 使用封装的 axios 实例发送 POST 请求
    // 注意：请确保后端创建项目的 API 地址是 '/api/project'，如果不是请修改
    await request.post('/project', projectData);

    // 3. 请求成功后的处理
    ElMessage.success('Project created successfully.')

    // 关闭对话框并重置表单
    handleDialogClose()

    // 刷新项目列表
    fetchProjects()

  } catch (error) {
    // 4. 统一处理错误
    // 如果是表单校验失败，error 会包含详细信息
    // 如果是 API 请求失败，错误会被 axios 拦截器捕获并 reject
    console.error('Error creating project:', error)
    // 表单校验失败的错误信息已经包含在 error 中，我们可以直接显示
    ElMessage.error(error.message || 'Failed to create project')
  }
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