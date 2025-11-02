<template>
  <div class="project-detail-page">
    <!-- 项目基本信息 -->
    <el-card class="project-info-card">
      <div class="project-header">
        <h2 class="project-name">{{ project.name }}</h2>
        <p class="team-count">团队人数: {{ project.team.length }} 人</p>
      </div>
    </el-card>

    <!-- 团队成员表格 -->
    <el-card class="team-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h3>团队成员</h3>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
          ref="teamTable"
          :data="paginatedTeam"
          stripe
          style="width: 100%; margin-bottom: 16px;"
          v-loading="isLoading"
          element-loading-text="加载中..."
      >
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="email" label="邮箱" />
      </el-table>

      <!-- 分页器放在表格内部，卡片底部 -->
      <div class="pagination-wrapper">
        <div class="page-size-select">
          <span>每页显示：</span>
          <el-select v-model="pageSize" size="small" style="width: 80px; margin: 0 10px;">
            <el-option label="5" :value="5"></el-option>
            <el-option label="10" :value="10"></el-option>
            <el-option label="20" :value="20"></el-option>
          </el-select>
        </div>

        <el-pagination
            background
            layout="prev, pager, next, ->, total"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            :total="project.team.length"
            :disabled="project.team.length === 0"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>


<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const project = reactive({
  name: '',
  team: []
})

const currentPage = ref(1)
const pageSize = ref(5)
const teamTable = ref(null)
const isLoading = ref(false)

// 计算分页后的数据
const paginatedTeam = computed(() => {
  // 处理边界情况
  if (!project.team || project.team.length === 0) {
    return []
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value

  // 确保不会索引超出数组长度
  return project.team.slice(start, Math.min(end, project.team.length))
})

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  scrollToTop()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置为第一页
  scrollToTop()
}

// 滚动表格到顶部
const scrollToTop = () => {
  if (teamTable.value) {
    const tableBody = teamTable.value.$el.querySelector('.el-table__body-wrapper')
    if (tableBody) {
      tableBody.scrollTop = 0
    }
  }
}

// 监听分页参数变化，确保页码有效
watch([currentPage, pageSize], () => {
  // 计算总页数
  const totalPages = Math.ceil(project.team.length / pageSize.value) || 1

  // 确保当前页码在有效范围内
  if (currentPage.value > totalPages) {
    currentPage.value = totalPages
  } else if (currentPage.value < 1) {
    currentPage.value = 1
  }
}, { immediate: true })

onMounted(() => {
  isLoading.value = true

  // 模拟API请求延迟
  setTimeout(() => {
    const projectId = route.params.id
    project.name = '示例项目 A'
    project.team = [
      { name: '张三', role: '项目经理', email: 'zhangsan@example.com' },
      { name: '李四', role: '开发者', email: 'lisi@example.com' },
      { name: '王五', role: '测试员', email: 'wangwu@example.com' },
      { name: '赵六', role: '开发者', email: 'zhaoliu@example.com' },
      { name: '钱七', role: '设计师', email: 'qianqi@example.com' },
      { name: '孙八', role: '开发者', email: 'sunba@example.com' },
      { name: '周九', role: '产品经理', email: 'zhoujiu@example.com' }
    ]
    isLoading.value = false
  }, 500)
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

/* 响应式处理 */
@media (max-width: 768px) {
  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .page-size-select {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
