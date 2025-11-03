import request from '@/utils/request'

// 获取任务列表
export function getTasksApi(projectId) {
    return request({
        url: `/kanban/tasks?projectId=${projectId}`,
        method: 'get'
    })
}

// 新增任务
export function addTaskApi(projectId, data) {
    return request({
        url: `/api/projects/${projectId}/tasks`,
        method: 'post',
        data
    })
}

// 更新任务（状态变更等）
export function updateTaskApi(taskId, data) {
    return request({
        url: `/api/tasks/${taskId}`,
        method: 'put',
        data
    })
}

// 删除任务
export function deleteTaskApi(taskId) {
    return request({
        url: `/api/tasks/${taskId}`,
        method: 'delete'
    })
}
