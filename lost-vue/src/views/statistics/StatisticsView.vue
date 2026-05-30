<template>
  <div class="statistics-container">
    <h2>数据统计</h2>
    <div class="charts-container">
      <div class="chart-item">
        <h3>失物 / 拾物发布数量</h3>
        <div ref="typeChart" style="width: 100%; height: 400px;"></div>
      </div>
      <div class="chart-item">
        <h3>状态分布</h3>
        <div ref="statusChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
    <div class="charts-container">
      <div class="chart-item full-width">
        <h3>分类分布</h3>
        <div ref="categoryChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getUserLostFoundList } from '@/api/lostApi'
import { ITEM_TYPE_LABEL, getStatusLabel } from '@/utils/itemConstants'

const typeChart = ref(null)
const statusChart = ref(null)
const categoryChart = ref(null)

const typeCount = ref({ 0: 0, 1: 0 })
const statusCount = ref({ 0: 0, 1: 0, 2: 0, 3: 0 })
const categoryCount = ref({})

const fetchStatisticsData = async () => {
  try {
    const result = await getUserLostFoundList({ page: 1, size: 9999 })
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
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const initTypeChart = () => {
  if (!typeChart.value) return
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    title: { text: '类型分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: typeCount.value[0], name: '失物' },
        { value: typeCount.value[1], name: '拾物' }
      ]
    }]
  })
}

const initStatusChart = () => {
  if (!statusChart.value) return
  const chart = echarts.init(statusChart.value)
  chart.setOption({
    title: { text: '状态分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [0, 1, 2, 3].map((s) => ({
        value: statusCount.value[s],
        name: getStatusLabel(s, 0)
      }))
    }]
  })
}

const initCategoryChart = () => {
  if (!categoryChart.value) return
  const chart = echarts.init(categoryChart.value)
  chart.setOption({
    title: { text: '分类分布', left: 'center' },
    tooltip: {},
    xAxis: {
      type: 'category',
      data: Object.keys(categoryCount.value),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: Object.values(categoryCount.value),
      itemStyle: { color: '#409eff' }
    }]
  })
}

onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.statistics-container { padding: 20px; }
.charts-container { display: flex; gap: 20px; margin-bottom: 20px; }
.chart-item { flex: 1; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
.full-width { flex: 1 1 100%; }
</style>
