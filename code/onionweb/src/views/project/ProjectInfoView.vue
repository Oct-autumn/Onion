<template>
  <div class="project-detail-page">
    <!-- Project Basic Information -->
    <el-card class="project-info-card">
      <div class="project-header">
        <h2 class="project-name">{{ project.name }}</h2>
        <p class="team-count">Team Size: {{ project.teamCount }} people</p>
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
      >
        <el-table-column prop="name" label="Name" />
        <el-table-column prop="status" label="Status" />
        <el-table-column prop="workingHour" label="Working Hours" />
        <el-table-column prop="role" label="Role" />
        <el-table-column label="Actions" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="mini" @click="deleteMember(row.id)">Delete</el-button>
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
        <el-form-item label="Status" prop="status">
          <el-input v-model="newMember.status" placeholder="Please enter status" />
        </el-form-item>
        <el-form-item label="Working Hours" prop="workingHour">
          <el-input v-model="newMember.workingHour" placeholder="Please enter working hours" />
        </el-form-item>
        <el-form-item label="Role" prop="role">
          <el-input v-model="newMember.role" placeholder="Please enter role" />
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

// Project basic information
const project = reactive({
  name: '',
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
const newMember = reactive({ name: '', status: '', workingHour: '', role: '' })

// Open the add member dialog
const openAddMemberDialog = () => {
  Object.assign(newMember, { name: '', status: '', workingHour: '', role: '' })
  addMemberDialogVisible.value = true
}

// Fetch project basic information
const fetchProjectInfo = async () => {
  try {
    const res = await request.get(`/project/info/${projectId}`)
    project.name = res.data.name
    project.teamCount = res.data.teamCount
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
    members.value = res.data.members
    totalMembers.value = res.data.total
  } catch (err) {
    console.error(err)
    ElMessage.error('Network error, failed to fetch team members')
  } finally {
    loading.value = false
  }
}

// Add a new member
const addMember = async () => {
  if (!newMember.name || !newMember.status || !newMember.workingHour || !newMember.role) {
    ElMessage.warning('Please fill in all information')
    return
  }
  try {
    const res = await request.post(`/project/info/${projectId}/team`, newMember)
    members.value.unshift(res.data.data)
    project.teamCount++
    totalMembers.value++
    addMemberDialogVisible.value = false
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
</script>

<style scoped>
.project-detail-page {
  padding: 20px;
}

.project-info-card {
  padding: 20px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-name {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.team-count {
  font-size: 16px;
  color: #606266;
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
</style>