<template>
  <div class="statistics-container">
    <div class="header-section">
      <h2>数据统计</h2>
      <el-radio-group v-if="showSwitch" v-model="dataType" @change="handleDataTypeChange" size="large">
        <el-radio-button label="personal">我的数据</el-radio-button>
        <el-radio-button label="platform">全平台数据</el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="charts-container">
      <div class="chart-item">
        <h3>失物 / 拾物发布数量</h3>
        <div ref="typeChart" style="width: 100%; min-height: 150px;"></div>
      </div>
      <div class="chart-item">
        <h3>状态分布</h3>
        <div ref="statusChart" style="width: 100%; min-height: 150px;"></div>
      </div>
    </div>
    <div class="charts-container">
      <div class="chart-item full-width">
        <h3>分类分布</h3>
        <div ref="categoryChart" style="width: 100%; min-height: 250px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getAllPlatformStatistics, getUserLostFoundList } from '@/api/lostApi'
import { getStatusLabel } from '@/utils/itemConstants'
import { getUserInfo } from '@/utils/auth'

const typeChart = ref(null)
const statusChart = ref(null)
const categoryChart = ref(null)

const typeCount = ref({ 0: 0, 1: 0 })
const statusCount = ref({ 0: 0, 1: 0, 2: 0, 3: 0 })
const categoryCount = ref({})

// 数据类型：personal-个人数据，platform-全平台数据
const dataType = ref('platform')

// 获取用户信息
const userInfo = getUserInfo()
const userRole = userInfo?.role || 0

// 是否显示切换按钮（管理员和超级管理员）
const showSwitch = computed(() => userRole >= 1)

// 页面加载时，根据角色设置默认值
onMounted(() => {
  // 普通用户只能看个人数据，管理员默认看全平台数据
  dataType.value = userRole >= 1 ? 'platform' : 'personal'
  fetchStatisticsData()
})

const handleDataTypeChange = () => {
  fetchStatisticsData()
}

const fetchStatisticsData = async () => {
  try {
    let result
    
    if (dataType.value === 'platform' && userRole >= 1) {
      // 管理员查看全平台数据
      result = await getAllPlatformStatistics()
    } else {
      // 普通用户或管理员查看个人数据
      result = await getUserLostFoundList({ page: 1, size: 9999 })
    }
    
    if (result.success) {
      const posts = result.data.records || []
      typeCount.value = { 0: 0, 1: 0 }
      statusCount.value = { 0: 0, 1: 0, 2: 0, 3: 0 }
      categoryCount.value = {}

      posts.forEach((post) => {
        typeCount.value[post.itemType] = (typeCount.value[post.itemType] || 0) + 1
        statusCount.value[post.status] = (statusCount.value[post.status] || 0) + 1
        const catName = post.categoryName || '其他'
        categoryCount.value[catName] = (categoryCount.value[catName] || 0) + 1
      })

      initTypeChart()
      initStatusChart()
      initCategoryChart()
    } else {
      console.error('获取统计数据失败:', result.message)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const initTypeChart = () => {
  if (!typeChart.value) return
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    title: { 
      text: '类型分布', 
      left: 'center',
      textStyle: {
        padding: [0, 0, 5, 0],
        fontSize: 10,
        fontWeight: 'bold'
      }
    },
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '55%'],
      avoidLabelOverlap: false,
      data: [
        { value: typeCount.value[0], name: '失物' },
        { value: typeCount.value[1], name: '拾物' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const initStatusChart = () => {
  if (!statusChart.value) return
  const chart = echarts.init(statusChart.value)
  chart.setOption({
    title: { 
      text: '状态分布', 
      left: 'center',
      textStyle: {
        padding: [0, 0, 5, 0],
        fontSize: 10,
        fontWeight: 'bold'
      }
    },
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '55%'],
      avoidLabelOverlap: false,
      data: [0, 1, 2, 3].map((s) => ({
        value: statusCount.value[s],
        name: getStatusLabel(s, 0)
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const initCategoryChart = () => {
  if (!categoryChart.value) return
  const chart = echarts.init(categoryChart.value)
  chart.setOption({
    title: { 
      text: '分类分布', 
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {},
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Object.keys(categoryCount.value),
      axisLabel: { 
        rotate: 30,
        interval: 'auto'
      }
    },
    yAxis: { 
      type: 'value',
      name: '数量'
    },
    series: [{
      type: 'bar',
      data: Object.values(categoryCount.value),
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      }
    }]
  })
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.statistics-container { 
  padding: 20px; 
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
}

.charts-container { 
  display: flex; 
  gap: 20px; 
  margin-bottom: 20px; 
}

.chart-item { 
  flex: 1; 
  padding: 20px; 
  border: 1px solid #ddd; 
  border-radius: 8px; 
}

.full-width { 
  flex: 1 1 100%; 
}

</style>
