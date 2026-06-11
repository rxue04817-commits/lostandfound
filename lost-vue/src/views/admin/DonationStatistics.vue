<template>
  <div class="donation-statistics-container">
    <h2>打赏统计分析</h2>
    
    <el-card class="stats-card">
      <div class="chart-container">
        <h3>每日打赏金额趋势</h3>
        <div ref="statisticsChart" style="width: 100%; min-height: 500px;"></div>
      </div>
    </el-card>

    <el-row :gutter="20" class="summary-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="summary-item">
            <div class="summary-label">总打赏金额</div>
            <div class="summary-value">￥{{ totalAmount.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="summary-item">
            <div class="summary-label">总订单数</div>
            <div class="summary-value">{{ totalCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="summary-item">
            <div class="summary-label">平均打赏金额</div>
            <div class="summary-value">￥{{ averageAmount.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import donationApi from '@/api/donationApi'

const statisticsChart = ref(null)
const totalAmount = ref(0)
const totalCount = ref(0)
const averageAmount = ref(0)

const fetchStatistics = async () => {
  try {
    const res = await donationApi.getDonationStatistics()
    if (res.data.code === 1 && res.data.data) {
      initStatisticsChart(res.data.data)
      calculateSummary(res.data.data)
    } else {
      ElMessage.error(res.data.msg || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

const calculateSummary = (data) => {
  if (!data || !Array.isArray(data)) return
  
  let total = 0
  let count = 0
  
  data.forEach(item => {
    total += item.total || 0
    count++
  })
  
  totalAmount.value = total
  totalCount.value = count
  averageAmount.value = count > 0 ? total / count : 0
}

const initStatisticsChart = (data) => {
  if (!statisticsChart.value) return
  
  const chart = echarts.init(statisticsChart.value)
  
  const dates = data.map(item => item.date)
  const amounts = data.map(item => item.total || 0)
  
  chart.setOption({
    title: {
      text: '近30天打赏金额趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        return `${params[0].name}<br/>金额: ￥${params[0].value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: 'auto'
      }
    },
    yAxis: {
      type: 'value',
      name: '金额(元)',
      nameTextStyle: {
        padding: [0, 0, 0, 10]
      }
    },
    series: [{
      name: '打赏金额',
      type: 'line',
      data: amounts,
      smooth: true,
      itemStyle: {
        color: '#409eff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1,
            color: 'rgba(64, 158, 255, 0.1)'
          }]
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
  fetchStatistics()
})
</script>

<style scoped>
.donation-statistics-container {
  height: 600px;
}

.stats-card {
  margin-bottom: 10px;
  height: 550px;
}

.chart-container {
  padding: 10px;
  height:500px;
}

.summary-cards {
  margin-top: 20px;
  height: 100px;
}

.summary-item {
  text-align: center;
  align-items: center;
  padding: 20px 20px;
  height: 70px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
}

.summary-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}
</style>
