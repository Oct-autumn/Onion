<template>
  <div class="user-view">
    <!-- 顶部中间的新增用户按钮（仅管理员可见） -->
    <div v-if="isAdmin" class="add-user-top">
      <el-button type="primary" @click="showAddUserDialog">新增用户</el-button>
    </div>

    <!-- 团队成员列表（公司员工总览，仅姓名与邮箱） -->
    <el-table :data="paginatedMembers" style="width: 100%">
      <el-table-column prop="name" label="成员姓名" width="200">
        <template #default="{ row }">
          <div class="member-info">
            <el-avatar :size="32" :src="row.avatar" class="member-avatar">
              {{ row.name.charAt(0) }}
            </el-avatar>
            <span class="member-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      
      <!-- 仅保留邮箱列，不展示角色/状态/操作列 -->
      <el-table-column prop="email" label="邮箱" />
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="teamMembers.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="addUserDialogVisible" title="新增用户" width="480px">
      <el-form :model="newUser" :rules="newUserRules" ref="newUserForm" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="newUser.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon
          title="新用户默认密码为 123456（仅用于本地测试）" />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addUser">确定新增</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据（用户页不包含新增弹窗）

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 当前用户与权限
const currentUser = ref(null)
try {
  const userStr = localStorage.getItem('user')
  currentUser.value = userStr ? JSON.parse(userStr) : null
} catch (_) {
  currentUser.value = null
}

const isAdmin = computed(() => {
  const role = currentUser.value?.role
  return role === '超级管理员' || role === 'admin' || role === '管理员'
})

// 新增用户对话框
const addUserDialogVisible = ref(false)
const newUserForm = ref(null)
const newUser = ref({ name: '', email: '' })
const newUserRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// 团队成员数据（仅姓名与邮箱）
const teamMembers = ref([
  { id: 2, name: '张三', email: 'zhangsan@example.com' },
  { id: 3, name: '李四', email: 'lisi@example.com' },
  { id: 9, name: '王五', email: 'wangwu@example.com' },
  { id: 10, name: '赵六', email: 'zhaoliu@example.com' },
  { id: 11, name: '钱七', email: 'qianqi@example.com' },
  { id: 12, name: '孙八', email: 'sunba@example.com' },
  { id: 13, name: '周九', email: 'zhoujiu@example.com' },
  { id: 14, name: '吴十', email: 'wushi@example.com' }
])

// 新增/邀请相关移除（用户页仅浏览+管理员删除）

// 计算属性：分页后的成员列表
const paginatedMembers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return teamMembers.value.slice(start, end)
})

// 角色渲染已移除

// 分页事件处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 显示新增用户对话框
const showAddUserDialog = () => {
  if (!isAdmin.value) {
    ElMessage.error('仅管理员可进行此操作')
    return
  }
  newUser.value = { name: '', email: '' }
  addUserDialogVisible.value = true
}

// 新增用户（默认密码 123456，仅前端演示）
const addUser = async () => {
  if (!isAdmin.value) {
    ElMessage.error('仅管理员可进行此操作')
    return
  }
  if (!newUserForm.value) return
  try {
    await newUserForm.value.validate()
    const exists = teamMembers.value.some(u => u.email === newUser.value.email)
    if (exists) {
      ElMessage.error('该邮箱已存在')
      return
    }
    teamMembers.value.unshift({
      id: Date.now(),
      name: newUser.value.name.trim(),
      email: newUser.value.email.trim()
    })
    addUserDialogVisible.value = false
    ElMessage.success('用户已创建（默认密码：123456）')
  } catch (e) {
    // 校验失败
  }
}

// 删除成员
const removeMember = (member) => {
  if (!isAdmin.value) {
    ElMessage.error('仅管理员可进行此操作')
    return
  }
  if (member.role === '项目负责人') {
    ElMessage.warning('不能删除项目负责人')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除团队成员「${member.name}」吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    const index = teamMembers.value.findIndex(m => m.id === member.id)
    if (index > -1) {
      teamMembers.value.splice(index, 1)
      ElMessage.success('成员已删除')
      
      // 如果删除后当前页没有数据，回到上一页
      const totalPages = Math.ceil(teamMembers.value.length / pageSize.value)
      if (currentPage.value > totalPages && totalPages > 0) {
        currentPage.value = totalPages
      }
    }
  }).catch(() => {
    // 用户取消删除
  })
}
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.add-user-top {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 12px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar {
  flex-shrink: 0;
}

.member-name {
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

/* 按钮样式 */
:deep(.el-button--small) {
  padding: 5px 10px;
  font-size: 12px;
}

/* 标签样式 */
:deep(.el-tag) {
  font-weight: 500;
}
</style>