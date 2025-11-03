<template>
  <div class="project-detail-page">
    <!-- 项目基本信息 -->
    <el-card class="project-info-card">
      <div class="project-header">
        <h2 class="project-name">{{ project.name }}</h2>
        <p class="team-count">团队人数: {{ project.teamCount }} 人</p>
      </div>
    </el-card>

    <!-- 团队成员表格 -->
    <el-card class="team-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h3>团队成员</h3>
        </div>
      </template>

      <!-- 添加成员按钮 -->
      <el-button type="primary" size="small" @click="openAddMemberDialog">+ 添加成员</el-button>

      <!-- 表格 -->
      <el-table
          :data="members"
          stripe
          style="width: 100%; margin-bottom: 16px;"
          v-loading="loading"
          element-loading-text="加载中..."
      >
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="mini" @click="deleteMember(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-wrapper">
        <div class="page-size-select">
          <span>每页显示：</span>
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

    <!-- 新增成员弹窗 -->
    <el-dialog title="添加成员" v-model="addMemberDialogVisible" width="400px">
      <el-form ref="addMemberFormRef" :model="newMember" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="newMember.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-input v-model="newMember.role" placeholder="请输入角色" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newMember.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addMember">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const projectId = route.params.id

// 项目基本信息
const project = reactive({
  name: '',
  teamCount: 0
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(5)
const totalMembers = ref(0)
const members = ref([])

const loading = ref(false)

// 添加成员弹窗
const addMemberDialogVisible = ref(false)
const newMember = reactive({ name: '', role: '', email: '' })


// 打开新增成员弹窗
const openAddMemberDialog = () => {
  Object.assign(newMember, { name: '', role: '', email: '' })
  addMemberDialogVisible.value = true
}

// 获取项目基本信息
const fetchProjectInfo = async () => {
  try {
    const res = await request.get(`/api/projects/${projectId}`)
    project.name = res.data.name
    project.teamCount = res.data.teamCount
  } catch (err) {
    console.error(err)
    ElMessage.error('网络错误，获取项目基本信息失败')
  }
}

// 获取团队成员列表
const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/projects/${projectId}/team`, {
      params: { page: currentPage.value, pageSize: pageSize.value }
    })
    members.value = res.data.members
    totalMembers.value = res.data.total
  } catch (err) {
    console.error(err)
    ElMessage.error('网络错误，获取团队成员失败')
  } finally {
    loading.value = false
  }
}

// 新增成员
const addMember = async () => {
  if (!newMember.name || !newMember.role || !newMember.email) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await request.post(`/api/projects/${projectId}/team`, newMember)
    members.value.unshift(res.data.data)
    project.teamCount++
    totalMembers.value++
    addMemberDialogVisible.value = false
    ElMessage.success('添加成功')
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || '添加失败')
  }
}

// 删除成员
const deleteMember = async (memberId) => {
  try {
    await request.delete(`/api/projects/${projectId}/team/${memberId}`)
    members.value = members.value.filter(m => m.id !== memberId)
    project.teamCount--
    totalMembers.value--
    ElMessage.success('删除成功')
  } catch (err) {
    console.error(err)
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

// 页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMembers()
}

// 每页数量变化
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchMembers()
}

// 初始化
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
