<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getLostFoundList, getCategories } from '@/api/lostApi'
import { useStore } from 'vuex'
import {
  ITEM_TYPE_LABEL,
  ITEM_TYPE_TAG,
  getStatusLabel,
  getStatusTagType
} from '@/utils/itemConstants'

const store = useStore()
const router = useRouter()
computed(() => store.state.user);
const activeTypeTab = ref('')
const categories = ref([])

const searchForm = ref({
  title: '',
  location: '',
  status: null,
  content: '',
  categoryId: null
})

const pagination = ref({ page: 1, size: 10, total: 0 })
const lostFoundList = ref([])
const loading = ref(false)

const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      title: searchForm.value.title,
      location: searchForm.value.location,
      status: searchForm.value.status || 1,
      content: searchForm.value.content,
      categoryId: searchForm.value.categoryId
    }
    if (activeTypeTab.value !== '') {
      params.itemType = Number(activeTypeTab.value)
    }

    const result = await getLostFoundList(params)
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

const onTypeTabChange = () => {
  pagination.value.page = 1
  fetchLostFoundList()
}
const handleSearch = () => {
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleReset = () => {
  searchForm.value = { title: '', location: '', status: null, content: '', categoryId: null }
  pagination.value.page = 1
  fetchLostFoundList()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/lost/detail/${id}`)
}

// 图片加载错误处理
const handleImageError = (e) => {
  e.target.style.display = 'none'
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

onMounted(async () => {
  const catResult = await getCategories()
  if (catResult.success) categories.value = catResult.data || []
  fetchLostFoundList()
})
</script>

<template>
  <div class="lost-found-container">
    <el-tabs v-model="activeTypeTab" @tab-change="onTypeTabChange" class="type-tabs">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="失物" name="0" />
      <el-tab-pane label="拾物" name="1" />
    </el-tabs>

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
            <el-form-item label="分类">
              <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="描述">
              <el-input v-model="searchForm.content" placeholder="请输入描述" clearable />
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
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <div class="thumbnail-container">
              <img 
                v-if="row.images && row.images.length > 0" 
                :src="row.images[0]" 
                alt="物品图片"
                class="thumbnail-image"
                @error="handleImageError"
              />
              <div v-else class="thumbnail-placeholder">
                <el-icon :size="24"><Picture /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="ITEM_TYPE_TAG[row.itemType]">{{ ITEM_TYPE_LABEL[row.itemType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status, row.itemType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布人" width="100">
          <template #default="{ row }">{{ row.userInfo?.realName }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goToDetail(row.id)">查看详情</el-button>
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
  </div>
</template>

<style scoped>
.lost-found-container { padding: 20px; }
.type-tabs { margin-bottom: 16px; }
.search-card { margin-bottom: 20px; }
.list-card { margin-top: 20px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }

.thumbnail-container {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  overflow: hidden;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.thumbnail-image:hover {
  transform: scale(1.1);
}

.thumbnail-placeholder {
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
</style>
