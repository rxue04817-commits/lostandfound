<template>
  <div class="statistics-container">
    <h2>数据统计</h2>
    
    <!-- 图表容器 -->
    <div class="charts-container">
      <!-- 发布数量统计 -->
      <div class="chart-item">
        <h3>发布数量统计</h3>
        <div ref="publishChart" style="width: 100%; height: 400px;"></div>
      </div>

      <!-- 状态分布统计 -->
      <div class="chart-item">
        <h3>状态分布统计</h3>
        <div ref="statusChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getUserLostFoundList } from '@/api/lostApi'

// 统计数据
const publishCount = ref(0)
const statusCount = ref({
  0: 0, // 待审核
  1: 0, // 已审核
  2: 0, // 已领取
  3: 0  // 已过期
})

// 图表实例
const publishChart = ref(null)
const statusChart = ref(null)

// 获取统计数据
const fetchStatisticsData = async () => {
  try {
    const result = await getUserLostFoundList({ page: 1, size: 9999 })
    if (result.success) {
      const posts = result.data.records
      
      // 统计发布总数
      publishCount.value = posts.length
      
      // 统计各状态数量
      posts.forEach(post => {
        statusCount.value[post.status] = (statusCount.value[post.status] || 0) + 1
      })
      
      // 初始化图表
      initPublishChart()
      initStatusChart()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化发布数量图表
const initPublishChart = () => {
  if (!publishChart.value) return
  
  const chart = echarts.init(publishChart.value)
  const option = {
    title: {
      text: '发布数量统计',
      left: 'center'
    },
    tooltip: {},
    legend: {
      data: ['发布数量']
    },
    xAxis: {
      data: ['我的发布']
    },
    yAxis: {},
    series: [{
      name: '发布数量',
      type: 'bar',
      data: [publishCount.value]
    }]
  }
  
  chart.setOption(option)
}

// 初始化状态分布图表
const initStatusChart = () => {
  if (!statusChart.value) return
  
  const chart = echarts.init(statusChart.value)
  const statusLabels = ['待审核', '已审核', '已领取', '已过期']
  const statusValues = Object.values(statusCount.value)
  
  const option = {
    title: {
      text: '状态分布统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '状态分布',
      type: 'pie',
      radius: '50%',
      data: statusLabels.map((label, index) => ({
        value: statusValues[index],
        name: label
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  chart.setOption(option)
}

// 页面初始化
onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.charts-container {
  display: flex;
  gap: 20px;
}

.chart-item {
  flex: 1;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
</style>