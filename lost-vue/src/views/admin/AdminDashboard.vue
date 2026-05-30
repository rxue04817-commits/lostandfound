<!-- src/views/admin/AdminDashboard.vue -->
<template>
  <div class="admin-container">
    <h2>信息审核</h2>

    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" @submit.prevent="handleSearch">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="标题">
              <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="类型">
              <el-select v-model="searchForm.itemType" placeholder="全部类型" clearable>
                <el-option label="失物" :value="0" />
                <el-option label="拾物" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分类">
              <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
                <el-option label="待审核" :value="0" />
                <el-option label="已审核" :value="1" />
                <el-option label="已完结" :value="2" />
                <el-option label="已过期" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="list-card" v-loading="loading">
      <el-table :data="lostFoundList" style="width: 100%">
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="ITEM_TYPE_TAG[row.itemType]">{{ ITEM_TYPE_LABEL[row.itemType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="160" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="date" label="日期" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status, row.itemType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" :disabled="row.status !== 0" @click="approvePost(row.id)">通过审核</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 0" @click="rejectPost(row.id)">拒绝审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="信息详情" width="600px">
      <el-form :model="detailForm" label-width="100px">
        <el-form-item label="类型">
          <el-tag :type="ITEM_TYPE_TAG[detailForm.itemType]">{{ ITEM_TYPE_LABEL[detailForm.itemType] }}</el-tag>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="detailForm.categoryName" disabled />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="detailForm.title" disabled />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="detailForm.location" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-input v-model="detailForm.date" disabled />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="detailForm.contactPhone" disabled />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="detailForm.content" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="图片" v-if="detailForm.imageUrls?.length">
          <div class="image-preview">
            <el-image
              v-for="(url, index) in detailForm.imageUrls"
              :key="index"
              :src="url"
              fit="cover"
              class="preview-image"
              :preview-src-list="detailForm.imageUrls"
            />
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
import { updateLostFoundStatus, getLostFoundDetail, getCategories } from '@/api/lostApi'
import { ITEM_TYPE_LABEL, ITEM_TYPE_TAG, getStatusLabel, getStatusTagType } from '@/utils/itemConstants'

const categories = ref([])
const searchForm = ref({ title: '', itemType: null, categoryId: null, status: null })
const pagination = ref({ page: 1, size: 10, total: 0 })
const lostFoundList = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const detailForm = ref({
  itemType: 0,
  categoryName: '',
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})

const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    Object.keys(params).forEach((k) => {
      if (params[k] === '' || params[k] === null) delete params[k]
    })
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

const handleSearch = () => {
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleReset = () => {
  searchForm.value = { title: '', itemType: null, categoryId: null, status: null }
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchLostFoundList()
}

const approvePost = async (id) => {
  ElMessageBox.confirm('确定要通过审核吗？', '提示', { type: 'warning' }).then(async () => {
    const result = await updateLostFoundStatus(id, 1)
    if (result.success) {
      ElMessage.success('审核通过成功')
      fetchLostFoundList()
    } else {
      ElMessage.error(result.message || '审核失败')
    }
  }).catch(() => {})
}

const rejectPost = async (id) => {
  ElMessageBox.confirm('确定要拒绝审核吗？', '提示', { type: 'warning' }).then(async () => {
    const result = await updateLostFoundStatus(id, 3)
    if (result.success) {
      ElMessage.success('审核拒绝成功')
      fetchLostFoundList()
    } else {
      ElMessage.error(result.message || '审核失败')
    }
  }).catch(() => {})
}

const viewDetail = async (row) => {
  const result = await getLostFoundDetail(row.id)
  if (result.success) {
    detailForm.value = {
      itemType: result.data.itemType,
      categoryName: result.data.categoryName,
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
}

onMounted(async () => {
  const catResult = await getCategories()
  if (catResult.success) categories.value = catResult.data || []
  fetchLostFoundList()
})
</script>

<style scoped>
.admin-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.list-card { margin-top: 20px; }
.image-preview { display: flex; flex-wrap: wrap; gap: 10px; }
.preview-image { width: 100px; height: 100px; border-radius: 4px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
