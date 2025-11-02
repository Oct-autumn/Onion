import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:5000', // 你的后端地址
    timeout: 5000,
})

// 获取某项目任务列表
export const getTasks = (projectId) => api.get(`/api/projects/${projectId}/tasks`)

// 添加任务
export const addTask = (projectId, taskData) => api.post(`/api/projects/${projectId}/tasks`, taskData)

// 删除任务
export const deleteTask = (taskId) => api.delete(`/api/tasks/${taskId}`)

// 更新任务状态
export const updateTaskStatus = (taskId, newStatus) =>
    api.put(`/api/tasks/${taskId}`, { status: newStatus })
