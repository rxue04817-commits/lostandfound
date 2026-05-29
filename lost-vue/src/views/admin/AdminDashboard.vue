<!-- src/views/admin/AdminDashboard.vue -->
<template>
  <div class="admin-container">
    <h2>管理员后台</h2>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" @submit.prevent="handleSearch">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="标题">
              <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="地点">
              <el-input v-model="searchForm.location" placeholder="请输入地点" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态">
                <el-option label="全部" value="" />
                <el-option label="待审核" value="0" />
                <el-option label="已审核" value="1" />
                <el-option label="已领取" value="2" />
                <el-option label="已过期" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 审核列表 -->
    <el-card class="list-card" v-loading="loading">
      <el-table :data="lostFoundList" style="width: 100%">
        <el-table-column prop="title" label="标题" width="200">
          <template #default="{ row }">
            <span>{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="location" label="地点" width="150">
          <template #default="{ row }">
            <span>{{ row.location }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="date" label="丢失日期" width="120">
          <template #default="{ row }">
            <span>{{ row.date }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small"  :disabled="row.status !== 0" @click="approvePost(row.id)">
              通过审核
            </el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 0"
              @click="rejectPost(row.id)">
              拒绝审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </el-card>
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="失物详情" width="600px">
      <el-form :model="detailForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="detailForm.title" disabled />
        </el-form-item>

        <el-form-item label="丢失地点">
          <el-input v-model="detailForm.location" disabled />
        </el-form-item>

        <el-form-item label="丢失日期">
          <el-date-picker v-model="detailForm.date" type="date" value-format="YYYY-MM-DD" disabled />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input v-model="detailForm.contactPhone" disabled />
        </el-form-item>

        <el-form-item label="详细描述">
          <el-input v-model="detailForm.content" type="textarea" disabled />
        </el-form-item>

        <el-form-item label="图片">
          <!-- 图片预览区域 -->
          <div class="image-preview" v-if="detailForm.imageUrls && detailForm.imageUrls.length > 0">
            <div v-for="(url, index) in detailForm.imageUrls" :key="'url-' + index" class="image-item">
              <el-image :src="url" fit="cover" class="preview-image" :preview-src-list="detailForm.imageUrls"
                :initial-index="index" lazy />
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllLostFound } from '@/api/adminApi'
import { updateLostFoundStatus, getLostFoundDetail } from '@/api/lostApi'
// 搜索表单数据
const searchForm = ref({
  title: '',
  location: '',
  status: null
})

// 分页数据
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 失物列表数据
const lostFoundList = ref([])
const loading = ref(false)

// 获取失物列表
const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      title: searchForm.value.title,
      location: searchForm.value.location,
      status: searchForm.value.status
    }

    const result = await getAllLostFound(params)
    if (result.success) {
      lostFoundList.value = result.data.records
      pagination.value.total = result.data.total
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchLostFoundList()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    title: '',
    location: '',
    status: null
  }
  pagination.value.page = 1
  fetchLostFoundList()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchLostFoundList()
}

// 通过审核
const approvePost = async (id) => {
  ElMessageBox.confirm('确定要通过此失物信息的审核吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端API更新状态
      const result = await updateLostFoundStatus(id, 1) // 1表示已审核
      if (result.success) {
        ElMessage.success('审核通过成功')
        fetchLostFoundList()
      } else {
        ElMessage.error(result.message || '审核失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 拒绝审核
const rejectPost = async (id) => {
  ElMessageBox.confirm('确定要拒绝此失物信息的审核吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端API更新状态
      const result = await updateLostFoundStatus(id, 3) // 3表示已过期
      if (result.success) {
        ElMessage.success('审核拒绝成功')
        fetchLostFoundList()
      } else {
        ElMessage.error(result.message || '审核失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 添加状态标签映射
const getStatusLabel = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已审核',
    2: '已领取',
    3: '已过期'
  }
  return statusMap[status] || '未知状态'
}

const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 页面初始化
onMounted(() => {
  fetchLostFoundList()
})
const detailDialogVisible = ref(false)
const detailForm = ref({
  id: null,
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})
const viewDetail = async (row) => {
  try {
    const result = await getLostFoundDetail(row.id)
    if (result.success) {
      detailForm.value = {
        id: row.id,
        title: result.data.title,
        content: result.data.content,
        location: result.data.location,
        date: result.data.date,
        contactPhone: result.data.contactPhone,
        imageUrls: result.data.images || []
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(result.message || '获取详情失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
}

</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-top: 20px;
}
</style>