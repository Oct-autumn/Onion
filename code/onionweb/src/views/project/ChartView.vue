<template>
  <div class = "charts-container">
	<div ref="taskPieChartRef" class="chart"></div>
    <div ref="burndownChartRef" class="chart" style="display: none;"></div>
    <div ref="contributionChartRef" class="chart"></div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const projectId = route.params.id


// Chart references
const burndownChartRef = ref(null)
const contributionChartRef = ref(null)
const taskPieChartRef = ref(null)

// Chart instances
let burndownChartInstance = null
let contributionChartInstance = null
let taskPieChartRefInstance = null

// Chart data
let taskStatuses = []
let taskCount = []
let membersName = []
let membersWorkingHour = []

const dummyMembersRes = {
  data: [
	{name: 'Alice', workingHour: '10'},
	{name: 'Bob', workingHour: '5',},
	{name: 'Charlie', workingHour: '15',},
  ]
}

const dummyTaskRes = {
  data: [
    { id: 1, title: 'Task 1', status: 'todo' },
    { id: 2, title: 'Task 2', status: 'in-progress' },
    { id: 3, title: 'Task 3', status: 'todo' },
    { id: 4, title: 'Task 4', status: 'code review' },
    { id: 5, title: 'Task 5', status: 'in-progress' },
    { id: 6, title: 'Task 6', status: 'todo' },
    { id: 7, title: 'Task 7', status: 'completed' },
  ]
};


const fetchMembers = async () => {
  try {
    const res = await request.get(`/project/info/${projectId}/team`)
    //const res = dummyMembersRes
    
    membersName = res.data.map(member => member.name)  // Added .data
    membersWorkingHour = res.data.map(member => member.workingHour)  // Added .data
  } catch (err) {
    console.error(err)
    ElMessage.error('网络错误，获取团队成员失败')
  } 
}

const fetchProjectTasks = async () => {
  try {
    const res = await request.get(`/kanban/tasks`, { params: { projectId } })
	//const res = dummyTaskRes;
	
	// Group tasks by status and count them
	const statusCountMap = res.data.reduce((acc, task) => {
	  acc[task.status] = (acc[task.status] || 0) + 1;
	  return acc;
	}, {});

	// Create two arrays: one for statuses and one for counts
	taskStatuses = Object.keys(statusCountMap);
	taskCount = Object.values(statusCountMap);
  } catch (err) {
    console.error(err)
    ElMessage.error('获取任务失败')
  }
}

const initCharts = () => {
  fetchProjectTasks()
  fetchMembers()

  // Pie Chart for task distribution
  taskPieChartRefInstance = echarts.init(taskPieChartRef.value)
	taskPieChartRefInstance.setOption({
	  title: {
		text: 'Task Distribution'
	  },
	  tooltip: {
		trigger: 'item'
	  },
	  series: [
		{
		  name: 'Tasks',
		  type: 'pie',
		  radius: '60%',
		  data: taskStatuses.map((name, index) => ({
			name: name,
			value: taskCount[index]
		  })),
		}
	  ]
	});

  // Burndown Chart
  burndownChartInstance = echarts.init(burndownChartRef.value)
  burndownChartInstance.setOption({
    title: {
      text: 'Burndown Chart'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: ['Sprint 1', 'Sprint 2', 'Sprint 3', 'Sprint 4', 'Sprint 5']
    },
    yAxis: {
      type: 'value',
      name: "Credits",
      nameLocation: 'middle',  // Position of axis name        
      nameGap: 30  // Distance from axis        
    },
    series: [
      {
        name: 'Credits',
        type: 'line',
        stack: 'Total',
        data: [500, 430, 280, 200, 120]
      },
    ]
  });

  // Bar Chart
  contributionChartInstance = echarts.init(contributionChartRef.value)
  contributionChartInstance.setOption({
    title: {
      text: 'Members Contribution'
    },
    xAxis: {
      type: 'category',
      data: membersName
    },
    yAxis: {
      type: 'value',
      name: "Hours",
      nameLocation: 'middle',  // Position of axis name        
      nameGap: 30  // Distance from axis
    },         
    series: [{
      data: membersWorkingHour,
      type: 'bar',
      name: 'Values'
    }]
  });

}

// Handle window resize
const handleResize = () => {
  taskPieChartRefInstance?.resize()
  burndownChartInstance?.resize()
  contributionChartInstance?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

</script>

<style scoped>
	.charts-container {
	  display: flex;
	  gap: 20px;
	  justify-content: center;
	  align-items: flex-start;
	  padding: 20px;
	  flex-wrap: wrap; 
	}
  .chart {
    width: 600px;
    height: 550px;
    /* border: 1px solid #e0e0e0; */
    border-radius: 8px;
  }
</style>

