<template>
  <div class = "charts-container">
	<div ref="taskPieChartRef" class="chart"></div>
    <div ref="burndownChartRef" class="chart" style="display: none;"></div>
    <div ref="contributionChartRef" class="chart"></div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import {onMounted, ref} from 'vue'

// Chart references
const burndownChartRef = ref(null)
const contributionChartRef = ref(null)
const taskPieChartRef = ref(null)

// Chart instances
let burndownChartInstance = null
let contributionChartInstance = null
let taskPieChartRefInstance = null

const initCharts = () => {

  // TODO: Fetch Data from API (dummy data for now)
  const chartData = {
	taskPie: {
	  xAxis: ['To-Do', 'In-Process', 'Code Review', 'Completed'],
	  yAxis: [5, 3, 4, 7]
	},
    burndown: {
      xAxis: ['Sprint 1', 'Sprint 2', 'Sprint 3', 'Sprint 4', 'Sprint 5'],
      yAxis: [500, 430, 280, 200, 120]
    },
    contribution: {
      xAxis: ['Alice', 'Bob', 'Charlie', 'Diana', 'Eve', 'Frank'],
      yAxis: [12, 18, 15, 5, 8, 11]
    }
  }

  // Pie Chart
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
		  radius: '70%',
		  data: chartData.taskPie.xAxis.map((name, index) => ({
			name: name,
			value: chartData.taskPie.yAxis[index]
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
      data: chartData.burndown.xAxis,
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
        data: chartData.burndown.yAxis,
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
      data: chartData.contribution.xAxis,
    },
    yAxis: {
      type: 'value',
      name: "Hours",
      nameLocation: 'middle',  // Position of axis name        
      nameGap: 30  // Distance from axis
    },         
    series: [{
      data: chartData.contribution.yAxis,
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

