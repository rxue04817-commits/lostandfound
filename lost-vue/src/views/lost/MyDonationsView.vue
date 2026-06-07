<template>
  <div class="my-donations-container">
    <h2>我的打赏记录</h2>
    <el-card>
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="outTradeNo" label="订单号" width="220" />
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
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <template v-if="row.tradeStatus === 'WAIT_BUYER_PAY'">
              <el-button link type="primary" @click="handleRepay(row)">去支付</el-button>
              <el-button link type="danger" @click="handleCancel(row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import donationApi from '@/api/donationApi'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchDonations = async () => {
  loading.value = true
  try {
    const res = await donationApi.getMyDonations({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    console.log(res)
    if (res.data.code === 1) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    } else {
      ElMessage.error(res.msg || '获取数据失败')
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

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '再想想',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await donationApi.cancelDonation(row.outTradeNo)
      if (res.code === 1) {
        ElMessage.success('取消成功')
        fetchDonations()
      } else {
        ElMessage.error(res.msg || '取消失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {})
}

const handleRepay = async (row) => {
  try {
    const res = await donationApi.repayDonation(row.outTradeNo)
    if (res.success && res.data) {

      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      document.forms[document.forms.length - 1].submit()
    } else {
      ElMessage.error(res.message || '发起支付失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
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
.my-donations-container {
  padding: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>