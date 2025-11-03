<template>
  <el-card>
    <div class="header">
      <h2>项目列表</h2>
      <el-button type="primary" @click="showAddProjectDialog = true">
        添加新项目
      </el-button>
    </div>

    <el-table
        :data="projects"
        style="width: 100%"
        stripe
        border
        v-loading="loading"
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

    <!-- Pagination section -->
    <div class="pagination-container">
      <div class="page-size-select">
        <span>每页显示：</span>
        <el-select v-model="numProjectsPerPage" size="small" style="width: 80px; margin: 0 10px;" @change="handlePageSizeChange">
          <el-option label="2" :value="2"></el-option>
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
        title="添加新项目"
        width="500px"
        :before-close="handleDialogClose"
    >
      <el-form :model="newProjectForm" :rules="formRules" ref="projectFormRef" label-width="150px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="newProjectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="newProjectForm.status" placeholder="请选择项目状态" style="width: 100%">
            <el-option label="未开始" value="未开始"></el-option>
            <el-option label="进行中" value="进行中"></el-option>
            <el-option label="已完成" value="已完成"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="newProjectForm.owner" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="计划完成时间" prop="planDate">
          <el-date-picker
              v-model="newProjectForm.planDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="submitNewProject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
  
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// Pagination variables
const currentPage = ref(1)
const numProjectsPerPage = ref(2)
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
  owner: '',
  planDate: ''
})

// Form validation rules
const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ],
  owner: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ],
  planDate: [
    { required: true, message: '请选择计划完成时间', trigger: 'change' }
  ]
}

// Local dummy test projects data
const dummyProjects = [
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
  },
  {
    id: 3,
    name: '前端管理系统2',
    status: '未开始',
    owner: '李四',
    createDate: '2025-09-15',
    planDate: '2025-12-20'
  },
  {
    id: 4,
    name: '前端管理系统3',
    status: '未开始',
    owner: '李四',
    createDate: '2025-09-15',
    planDate: '2025-12-20'
  },
  {
    id: 5,
    name: '数据分析平台',
    status: '已完成',
    owner: '王五',
    createDate: '2025-08-01',
    planDate: '2025-11-15'
  },
  {
    id: 6,
    name: '移动应用开发',
    status: '进行中',
    owner: '赵六',
    createDate: '2025-09-20',
    planDate: '2026-01-30'
  },
  {
    id: 7,
    name: '云服务部署',
    status: '进行中',
    owner: '孙七',
    createDate: '2025-10-05',
    planDate: '2025-12-15'
  },
  {
    id: 8,
    name: '安全审计系统',
    status: '未开始',
    owner: '周八',
    createDate: '2025-10-12',
    planDate: '2026-02-28'
  }
]

const fetchProjects = async () => {
  loading.value = true
  try {
    // use real API later
    /*
    const res = await request.get('/project/list', {
      params: { 
        page: currentPage.value, 
        pageSize: numProjectsPerPage.value 
      }
    })
    projects.value = res.data.projects
    totalProjects.value = res.data.total
    */
    
    // Local testing implementation
    const startIndex = (currentPage.value - 1) * numProjectsPerPage.value
    const endIndex = startIndex + numProjectsPerPage.value
    projects.value = dummyProjects.slice(startIndex, endIndex)
    totalProjects.value = dummyProjects.length
    
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
    owner: '',
    planDate: ''
  }
}

// Submit new project
const submitNewProject = () => {
  // TODO: add API call
  
  ElMessage.success('项目添加成功！')
  
  // Close dialog and reset form
  handleDialogClose()
  
  // Refresh the project list
  fetchProjects()
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
