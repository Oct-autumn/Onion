<template>
  <div class="user-view">
    <!-- 团队成员列表 -->
    <el-table :data="teamMembers" style="width: 100%">
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
      
      <el-table-column prop="role" label="角色" width="150">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)" size="small">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="email" label="邮箱" />
      
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === '已加入' ? 'success' : 'warning'" size="small">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button 
            type="danger" 
            size="small" 
            text 
            @click="removeMember(row)"
            :disabled="row.role === '项目负责人'"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加成员按钮 -->
    <div class="add-member-section">
      <el-button type="primary" @click="showAddMemberDialog">
        <el-icon><Plus /></el-icon>
        添加团队成员
      </el-button>
    </div>

    <!-- 添加成员弹窗 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加团队成员" width="500px">
      <el-form :model="newMember" :rules="memberRules" ref="memberForm" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="newMember.name" placeholder="请输入成员姓名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newMember.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="newMember.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="开发者" value="开发者" />
            <el-option label="测试员" value="测试员" />
            <el-option label="设计师" value="设计师" />
            <el-option label="产品经理" value="产品经理" />
            <el-option label="项目经理" value="项目经理" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addMemberDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addMember">发送邀请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 响应式数据
const addMemberDialogVisible = ref(false)
const memberForm = ref(null)

// 团队成员数据
const teamMembers = ref([
  {
    id: 1,
    name: '张三',
    email: 'zhangsan@example.com',
    role: '项目负责人',
    status: '已加入',
    avatar: ''
  },
  {
    id: 2,
    name: '李四',
    email: 'lisi@example.com',
    role: '开发者',
    status: '已加入',
    avatar: ''
  },
  {
    id: 3,
    name: '王五',
    email: 'wangwu@example.com',
    role: '测试员',
    status: '待接受',
    avatar: ''
  }
])

// 新成员表单数据
const newMember = ref({
  name: '',
  email: '',
  role: ''
})

// 表单验证规则
const memberRules = {
  name: [
    { required: true, message: '请输入成员姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色标签类型
const getRoleType = (role) => {
  const roleTypes = {
    '项目负责人': 'danger',
    '项目经理': 'warning',
    '开发者': 'primary',
    '测试员': 'success',
    '设计师': 'info',
    '产品经理': ''
  }
  return roleTypes[role] || ''
}

// 显示添加成员弹窗
const showAddMemberDialog = () => {
  newMember.value = { name: '', email: '', role: '' }
  addMemberDialogVisible.value = true
}

// 添加成员
const addMember = async () => {
  if (!memberForm.value) return
  
  try {
    await memberForm.value.validate()
    
    // 检查邮箱是否已存在
    const existingMember = teamMembers.value.find(member => member.email === newMember.value.email)
    if (existingMember) {
      ElMessage.error('该邮箱已存在于团队成员中')
      return
    }
    
    // 添加新成员
    const member = {
      id: Date.now(),
      name: newMember.value.name,
      email: newMember.value.email,
      role: newMember.value.role,
      status: '待接受',
      avatar: ''
    }
    
    teamMembers.value.push(member)
    addMemberDialogVisible.value = false
    
    ElMessage.success(`邀请已发送给 ${member.name} (${member.email})`)
    
    // 这里可以添加实际的API调用
    // await sendInvitation(member)
    
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}

// 删除成员
const removeMember = (member) => {
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

.add-member-section {
  margin-top: 20px;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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