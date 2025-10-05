<!-- /src/views/ProjectsListView.vue -->
<template>
  <div class="projects-container">
    <div class="projects-wrapper">
      <!-- Left side - Image -->
      <div class="image-section">
        <div class="image-content">
          <h1 class="welcome-title">Your Projects</h1>
          <p class="welcome-subtitle">List of all your projects</p>
          <div class="image-placeholder">
            <img src="/Onion_slogan_word.png" alt="Onion Slogan" class="welcome-image" />
          </div>
        </div>
      </div>

      <!-- Right side - Main content -->
      <div class="table-section">
        <el-card class="projects-card">
          <template #header>
            <div class="card-header">
              <h2>All Projects <span>({{ totalProjects }})</span></h2>
				<el-button 
				  type="primary" 
				  @click="createProject" 
				  size="Large"
				  :icon="Plus"
				  round>
				  New Project
				</el-button>
            </div>
          </template>

          <!-- Projects Table -->
          <el-table
            :data="projects"
            style="width: 100%"
            border
            v-loading="loading"
            empty-text="No projects found."
          >
  		
            <el-table-column prop="id" label="Project ID" min-width="100">
              <template #default="{ row }">
				<router-link :to="`/project/${row.id}`" class="project-link">
                  {{ row.id }}
                </router-link>
              </template>
            </el-table-column>
		
            <el-table-column prop="name" label="Project Name" min-width="150">
              <template #default="{ row }">
                <router-link :to="`/project/${row.id}`" class="project-link">
                  {{ row.name }}
                </router-link>
              </template>
            </el-table-column>

            <el-table-column prop="description" label="Description" min-width="200">
              <template #default="{ row }">
                <span v-if="row.description" class="description-text">
                  {{ row.description.length > 128 ? row.description.slice(0, 128) + '...' : row.description }}
                </span>
                <span v-else class="text-muted">No description</span>
              </template>
            </el-table-column>

            <el-table-column prop="createdAt" label="Created On" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
			
            <el-table-column prop="updatedAt" label="Updated On" width="180">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>			


          </el-table>


        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

// Mock data for testing
const projects = ref([]) 
const mockProjects = ([
	{
	  "id": 156,
	  "name": "Test project 1",
	  "description": "Project description 1.",
	  "createdAt": "2025-03-05T11:00:00Z",
	  "updatedAt": "2025-04-05T10:00:00Z"
	},
	{
	  "id": 130,
	  "name": "Test project 2",
	  "description": "Project description 2. Long text test: Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
	  "createdAt": "2025-04-05T10:00:00Z",
	  "updatedAt": "2025-07-05T10:00:00Z"
	},
	{
	  "id": 110,
	  "name": "Test project 3",
	  "description": "",
	  "createdAt": "2025-04-05T10:00:00Z",
	  "updatedAt": "2025-06-11T01:00:00Z"
	},
	{
	  "id": 50,
	  "name": "",
	  "description": "Project description 4 with empty title",
	  "createdAt": "2025-07-05T10:00:00Z",
	  "updatedAt": "2025-09-08T01:00:00Z"
	}	
])

const totalProjects = ref(0)
const currentPage = ref(1)
const loading = ref(false)

// Fetching projects from backend
const fetchProjects = async () => {
  loading.value = true
  
  // remove next 2 lines after testing
  projects.value = mockProjects
  totalProjects.value = projects.value.length
  
  try {
    // Replace this with actual API call
    const response = await fetch('/api/projects', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const data = await response.json()
      projects.value = data.projects || []
      totalProjects.value = data.total || 0
    } else {
      const error = await response.json()
      ElMessage.error(error.message || 'Failed to load projects')
    }
  } catch (error) {
    ElMessage.error('Network error, please try again later')
    console.error('Fetch projects error:', error)
  } finally {
    loading.value = false
  }  
}

// Format date
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}


// Create new project
const createProject = () => {
  router.push('/project/new')
}

// Load projects when page load
onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.projects-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.projects-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  background: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.image-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
}

.image-content {
  text-align: center;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: white;
}

.welcome-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  color: white;
}

.image-placeholder {
  margin-top: 2rem;
}

.welcome-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.table-section {
  flex: 1;
  background: #f8f9fa;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.projects-card {
  width: 100%;
  max-width: 1200px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.project-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.project-link:hover {

}

.description-text {
  color: #555;
}

.text-muted {
  color: #aaa;
  font-style: italic;
}

.danger-btn {
  color: #f56c6c;
}

/* Mobile device */
@media (max-width: 768px) {
  .projects-wrapper {
    flex-direction: column;
  }

  .image-section,
  .table-section {
    flex: none;
    height: auto;
    padding: 20px;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .welcome-subtitle {
    font-size: 1rem;
  }

  .projects-card {
    margin: 0 10px;
  }
}
</style>
