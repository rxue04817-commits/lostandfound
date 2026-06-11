<template>
  <div class="donation-orders-container">
    <h2>打赏订单管理</h2>

    <el-card class="list-card">
      <div class="toolbar">
        <el-button type="success" @click="handleExport">导出Excel</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="outTradeNo" label="订单号" width="220" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="subject" label="订单标题" width="200" />
        <el-table-column prop="lostFoundTitle" label="关联物品" width="200" />
        <el-table-column prop="totalAmount" label="打赏金额(元)" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">￥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tradeStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.tradeStatus === 'TRADE_SUCCESS' || row.tradeStatus === 'TRADE_FINISHED'" type="success">已支付</el-tag>
            <el-tag v-else-if="row.tradeStatus === 'WAIT_BUYER_PAY'" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.tradeStatus === 'TRADE_CLOSED'" type="info">已取消</el-tag>
            <el-tag v-else type="info">{{ row.tradeStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import donationApi from '@/api/donationApi'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchDonations = async () => {
  loading.value = true
  try {
    const res = await donationApi.getAdminDonations({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.data.code === 1) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    } else {
      ElMessage.error(res.data.msg || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDonations()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDonations()
}

const handleExport = async () => {
  try {
    const res = await donationApi.exportDonations()
    
    // 检查响应是否为blob类型
    if (!res || !(res instanceof Blob)) {
      console.error('导出文件格式错误:', res)
      ElMessage.error('导出文件格式错误')
      return
    }
    
    // 创建下载链接（res已经是Blob，直接使用）
    const url = window.URL.createObjectURL(res)
    const link = document.createElement('a')
    link.href = url
    link.download = `打赏订单数据_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const formatTime = (timeArray) => {
  if (!timeArray || !Array.isArray(timeArray)) return ''
  const [year, month, day, hour, minute, second] = timeArray
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second || 0).padStart(2, '0')}`
}

onMounted(() => {
  fetchDonations()
})
</script>

<style scoped>
.donation-orders-container {
  padding: 20px;
  height: 600px;
}

.list-card {
  margin-top: 20px;
  height: 600px;
  display: flex;
  flex-direction: column;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}

.el-table {
  flex: 1;
  overflow: auto;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>
