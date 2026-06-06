<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import claimApi from '@/api/claimApi'

const claims = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, size: 10, total: 0 })

const fetchClaims = async () => {
  loading.value = true
  try {
    const res = await claimApi.getReceivedClaims({ page: pagination.value.page, pageSize: pagination.value.size })
    if (res.data.code === 1) {
      claims.value = res.data.data.records
      pagination.value.total = res.data.data.total
    } else {
      ElMessage.error(res.data.msg || '获取收到的认领失败')
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

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定通过该认领申请吗？通过后其他待审核申请将被自动拒绝。', '提示', { type: 'warning' })
    const res = await claimApi.approveClaim(id)
    if (res.data.code === 1) {
      ElMessage.success('已通过')
      fetchClaims()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('网络请求异常')
  }
}

const handleReject = async (id) => {
  try {
    const { value: rejectReason } = await ElMessageBox.prompt('请输入拒绝原因（选填）', '拒绝认领', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.*/,
      inputErrorMessage: ''
    })
    
    const res = await claimApi.rejectClaim(id, { rejectReason: rejectReason || '不符合认领条件' })
    if (res.data.code === 1) {
      ElMessage.success('已拒绝')
      fetchClaims()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('网络请求异常')
  }
}

const handleComplete = async (id) => {
  try {
    await ElMessageBox.confirm('物品已归还给该失主了吗？确认后状态将变更为已归还。', '提示', { type: 'success' })
    const res = await claimApi.completeClaim(id)
    if (res.data.code === 1) {
      ElMessage.success('已确认归还')
      fetchClaims()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('网络请求异常')
  }
}

onMounted(() => {
  fetchClaims()
})
</script>

<template>
  <div class="claim-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>认领管理 (收到的申请)</span>
        </div>
      </template>

      <el-table :data="claims" style="width: 100%" v-loading="loading">
        <el-table-column prop="lostFoundTitle" label="拾物标题" width="200" />
        <el-table-column prop="claimerName" label="申请人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="description" label="认领说明" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getClaimStatusTag(row.status)">
              {{ getClaimStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row.id)">拒绝</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button type="primary" size="small" @click="handleComplete(row.id)">确认归还</el-button>
            </template>
          </template>
        </el-table-column>
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
.claim-manage-container {
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