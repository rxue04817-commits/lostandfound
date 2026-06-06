<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import claimApi from '@/api/claimApi'

const claims = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, size: 10, total: 0 })

const fetchClaims = async () => {
  loading.value = true
  try {
    const res = await claimApi.getMyClaims({ page: pagination.value.page, pageSize: pagination.value.size })
    console.log(res)
    if (res.data.code === 1) {
      claims.value = res.data.data.records
      pagination.value.total = res.data.data.total
    } else {
      ElMessage.error(res.msg || '获取我的认领失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchClaims()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchClaims()
}

const getClaimStatusLabel = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已完成' }
  return map[status] || '未知'
}

const getClaimStatusTag = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || ''
}

onMounted(() => {
  fetchClaims()
})
</script>

<template>
  <div class="my-claims-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的认领</span>
        </div>
      </template>

      <el-table :data="claims" style="width: 100%" v-loading="loading">
        <el-table-column prop="lostFoundTitle" label="拾物标题" width="200" />
        <el-table-column prop="description" label="我的认领说明" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getClaimStatusTag(row.status)">
              {{ getClaimStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<style scoped>
.my-claims-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>