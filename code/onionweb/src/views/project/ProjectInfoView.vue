<template>
  <div class="project-detail-page">
    <el-card class="project-info-card">
      <!-- 使用 div 包裹头部内容，方便进行 Flexbox 布局 -->
      <div class="project-header">
        <div class="project-main-info">
          <h2 class="project-name">{{ project.name }}</h2>
          <p class="team-count">
            <i class="el-icon-user"></i>
            Team Size: {{ project.teamCount }} people
          </p>
        </div>
        <div class="project-meta-info">
        <span class="expected-completion">
          <i class="el-icon-calendar"></i>
          Expected Completion: {{ project.expectedCompletion }}
        </span>
        </div>
      </div>
    </el-card>

    <!-- Team Members Table -->
    <el-card class="team-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h3>Team Members</h3>
        </div>
      </template>

      <!-- Add Member Button -->
      <el-button type="primary" size="small" @click="openAddMemberDialog">+ Add Member</el-button>

      <!-- Table -->
      <el-table
          :data="members"
          stripe
          style="width: 100%; margin-bottom: 16px;"
          v-loading="loading"
          element-loading-text="Loading..."
          @cell-click="handleCellClick"
      >
        <el-table-column prop="name" label="Name" />
        <el-table-column prop="role" label="Role">
          <template #default="scope">
            <span :style="{
              color: {
                'Manager': '#409eff',
                'Developer': '#67c23a',
                'QA': '#e6a23c',
                'DevOps': '#f56c6c'
              }[scope.row.role] || 'inherit',
              fontWeight: '500'
            }">
              {{ scope.row.role }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="Status">
          <template #default="scope">
            <el-select
                v-if="scope.row.userId === editingRowId && scope.column.label === 'Status'"
                v-model="scope.row.status"
                @change="handleSave(scope.row)"
                style="width: 100%;"
                placeholder="Select status"
            >
              <el-option label="Working" value="Working"></el-option>
              <el-option label="Leave" value="Leave"></el-option>
            </el-select>
            <span
                v-else
                :style="{
          color: scope.row.status === 'Working' ? '#67c23a' : '#f56c6c',
          fontWeight: '500'
        }"
            >
        {{ scope.row.status }}
      </span>
          </template>
        </el-table-column>

        <el-table-column prop="workingHour" label="Working Hours">
          <template #default="scope">
            <el-input
                v-if="scope.row.userId === editingRowId && scope.column.label === 'Working Hours'"
                v-model="scope.row.workingHour"
                @blur="handleSave(scope.row)"
                @keyup.enter="handleSave(scope.row)"
                type="number"
                placeholder="Enter working hours"
            />
            <span v-else>
        {{ scope.row.workingHour }}
      </span>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="mini" @click="deleteMember(row.userId)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <div class="page-size-select">
          <span>Show per page:</span>
          <el-select v-model="pageSize" size="small" style="width: 80px; margin: 0 10px;" @change="handlePageSizeChange">
            <el-option label="5" :value="5"></el-option>
            <el-option label="10" :value="10"></el-option>
            <el-option label="20" :value="20"></el-option>
          </el-select>
        </div>

        <el-pagination
            background
            layout="prev, pager, next, ->, total"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="totalMembers"
            @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- Add Member Dialog -->
    <el-dialog title="Add Member" v-model="addMemberDialogVisible" width="400px">
      <el-form ref="addMemberFormRef" :model="newMember" label-width="80px">
        <el-form-item label="Name" prop="name">
          <el-input v-model="newMember.name" placeholder="Please enter name" />
        </el-form-item>
        <el-form-item label="UserId" prop="userId">
          <el-input v-model="newMember.userId" placeholder="Please enter userId" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="newMember.status" placeholder="Please select status">
            <el-option label="Working" value="Working"></el-option>
            <el-option label="Leave" value="Leave"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Working Hours" prop="workingHour">
          <el-input v-model="newMember.workingHour" placeholder="Please enter working hours" />
        </el-form-item>
        <el-form-item label="Role" prop="role" >
          <el-select v-model="newMember.role" placeholder="Please select role">
            <el-option label="Manager" value="Manager"></el-option>
            <el-option label="Developer" value="Developer"></el-option>
            <el-option label="QA" value="QA"></el-option>
            <el-option label="DevOps" value="DevOps"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="addMember">Confirm</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const projectId = route.params.id

const editingRowId = ref(null)

// Project basic information
const project = reactive({
  name: '',
  expectedCompletion: '',
  teamCount: 0
})

// Pagination parameters
const currentPage = ref(1)
const pageSize = ref(5)
const totalMembers = ref(0)
const members = ref([])

const loading = ref(false)

// Add member dialog visibility
const addMemberDialogVisible = ref(false)
const newMember = reactive({ name: '', userId: '', status: '', workingHour: '', role: '' })

// Open the add member dialog
const openAddMemberDialog = () => {
  Object.assign(newMember, { name: '', status: '', workingHour: '', role: '' })
  addMemberDialogVisible.value = true
}


// 根据角色返回对应的 CSS 类名
const getRoleClass = (role) => {
  switch (role) {
    case 'Manager':
      return 'role-manager';
    case 'Developer':
      return 'role-developer';
    case 'QA':
      return 'role-qa';
    case 'DevOps':
      return 'role-devops';
    default:
      return '';
  }
};

// Fetch project basic information
const fetchProjectInfo = async () => {
  try {
    const res = await request.get(`/project/info/${projectId}`)
    console.info("=========  项目详情 ========")
    console.info(res)
    project.name = res.name
    project.expectedCompletion = res.expectedCompletion
    // project.teamCount = res.teamCount
  } catch (err) {
    console.error(err)
    ElMessage.error('Network error, failed to fetch project information')
  }
}

// Fetch team members list
const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await request.get(`/project/info/${projectId}/team`, {
      params: { page: currentPage.value, pageSize: pageSize.value }
    })
    members.value = res.members
    totalMembers.value = res.total
    project.teamCount = res.total
  } catch (err) {
    console.error(err)
    ElMessage.error('Network error, failed to fetch team members')
  } finally {
    loading.value = false
  }
}

// Add a new member
const addMember = async () => {
  if (!newMember.name || !newMember.status || !newMember.workingHour || !newMember.role|| !newMember.userId) {
    ElMessage.warning('Please fill in all information')
    return
  }
  try {
    const res = await request.post(`/project/info/${projectId}/team`, newMember)
    addMemberDialogVisible.value = false
    await fetchMembers()
    // members.value.unshift(res.members)
    // project.teamCount++
    // totalMembers.value++

    ElMessage.success('Member added successfully')
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || 'Failed to add member')
  }
}

// Delete a member
const deleteMember = async (memberId) => {
  try {
    await request.delete(`/project/info/${projectId}/team/${memberId}`)
    members.value = members.value.filter(m => m.id !== memberId)
    project.teamCount--
    totalMembers.value--
    ElMessage.success('Member deleted successfully')
    await fetchMembers()
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || 'Failed to delete member')
  }
}

// Handle page change
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMembers()
}

// Handle page size change
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchMembers()
}

// Initialize
onMounted(async () => {
  await fetchProjectInfo()
  await fetchMembers()
})


// 开始编辑
const handleEdit = (row) => {
  console.info("=====handleEdit ==")
  console.info("row ==== ")
  console.info(row.id)
  editingRowId.value = row.userId
}

// 点击单元格时进入编辑状态
const handleCellClick = (row, column) => {
  console.info("=====handleCellClick ==")
  console.log('点击可编辑列，userId：', row.userId, '列的 fixed 值：', column.label)
  if (column.label === 'Status' || column.label === 'Working Hours') {
    console.log('点击可编辑列，userId：', row.userId, '列的 fixed 值：', column.fixed)
    editingRowId.value = row.userId
  }
  console.info("=====editingRowId ==" + editingRowId)

}

// 保存编辑
const handleSave = async (row) => {
  try {
    console.info("====handleSave=====")
    await request.put(`/project/info/${projectId}/team`, {
      userId: row.userId, // 必须包含 userId，以便后端识别是更新操作
      name: row.name,
      status: row.status,
      workingHour: row.workingHour,
      role: row.role
    })
    ElMessage.success('Member updated successfully')
    editingRowId.value = null // 退出编辑状态
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || 'Failed to update member')
    // 如果更新失败，可以回滚数据
  }
  await fetchMembers()
}
</script>

<style scoped>
.project-detail-page {
  padding: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.team-card {
  padding-bottom: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
}

.page-size-select {
  display: flex;
  align-items: center;
}

/* 卡片整体样式 */
.project-info-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease-in-out;
  overflow: hidden; /* 确保内部元素不会超出圆角 */
  padding: 20px;
}

/* 卡片悬停效果 */
.project-info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

/* 头部内容区域布局 */
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

/* 左侧主要信息区域 */
.project-main-info {
  display: flex;
  flex-direction: column;
  gap: 8px; /* 元素之间的间距 */
}

/* 项目名称样式 */
.project-name {
  font-size: 1.5rem;
  font-weight: 600;
  color: #212529;
  margin: 0;
  line-height: 1.3;
}

/* 团队人数样式 */
.team-count {
  font-size: 0.95rem;
  color: #495057;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.team-count i.el-icon-user {
  color: #86909c;
  font-size: 1rem;
}

/* 右侧元信息区域 */
.project-meta-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* 预计完成日期样式 */
.expected-completion {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border-radius: 20px; /* 胶囊形状 */
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.expected-completion i.el-icon-calendar {
  font-size: 1rem;
}

/* 响应式调整：在小屏幕上垂直排列 */
@media (max-width: 768px) {
  .project-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .project-meta-info {
    align-items: flex-start;
    width: 100%;
  }

  .expected-completion {
    width: 100%;
    justify-content: center;
  }

  /* 角色颜色样式 */
  :deep(.role-manager) {
    color: #409eff; /* 蓝色 - Manager */
    font-weight: 500;
  }

  :deep(.role-developer) {
    color: #67c23a; /* 绿色 - Developer */
    font-weight: 500;
  }

  :deep(.role-qa) {
    color: #e6a23c; /* 橙色 - QA */
    font-weight: 500;
  }

  :deep(.role-devops) {
    color: #f56c6c; /* 红色 - DevOps */
    font-weight: 500;
  }
}
</style>