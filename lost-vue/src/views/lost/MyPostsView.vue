<template>
  <div class="my-posts-container">
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

    <!-- 我的发布列表 -->
    <el-card class="list-card" v-loading="loading">
      <el-table :data="myPostsList" style="width: 100%">
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
            <el-tag>
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="editPost(row)">修改</el-button>
            <el-button size="small" type="danger" @click="deletePost(row.id)">删除</el-button>
            <!-- <span>{{ row.status }}</span>
            <span>{{ typeof row.status }}</span> -->
            <el-button size="small" type="primary" @click="updatePostStatus(row.id, row.status)"
              :disabled="row.status !== 1">
              设为已领取
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑失物信息" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>

        <el-form-item label="丢失地点">
          <el-input v-model="editForm.location" />
        </el-form-item>

        <el-form-item label="丢失日期">
          <el-date-picker v-model="editForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" />
        </el-form-item>

        <el-form-item label="详细描述">
          <el-input v-model="editForm.content" type="textarea" />
        </el-form-item>

        <el-form-item label="图片">
          <!-- 已上传图片预览区域 -->
          <div class="image-preview" v-if="editForm.imageUrls && editForm.imageUrls.length > 0">
            <div v-for="(url, index) in editForm.imageUrls" :key="'url-' + index" class="image-item">
              <el-image :src="url" fit="cover" class="preview-image" :preview-src-list="editForm.imageUrls"
                :initial-index="index" lazy />
              <el-button type="danger" size="small" @click="removeImageUrl(index)" circle>X</el-button>
            </div>
          </div>

          <!-- 新图片预览区域 -->
          <div class="image-preview" v-if="imageFiles.length > 0">
            <div v-for="(file, index) in imageFiles" :key="'file-' + index" class="image-item">
              <img :src="windowURL.createObjectURL(file)" :alt="file.name" class="preview-image">
              <el-button type="danger" size="small" @click="removeImageFile(index)" circle>
                X
              </el-button>
            </div>
          </div>

          <!-- 图片上传控件 -->
          <div class="image-upload-area">
            <input type="file" multiple accept="image/*" @change="handleImageUpload" style="display: none"
              ref="fileInput">
            <el-button @click="$refs.fileInput.click()">选择图片</el-button>
            <div class="el-upload__tip">可以选择多张图片进行上传</div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updatePost">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserLostFoundList, deleteLostFound, updateLostFound, uploadImages, updateLostFoundStatus } from '@/api/lostApi'

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

// 我的发布列表数据
const myPostsList = ref([])
const loading = ref(false)

// 编辑表单数据
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})

// 图片文件引用
const imageFiles = ref([])
const windowURL = URL

// 获取我的发布列表
const fetchMyPostsList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      title: searchForm.value.title,
      location: searchForm.value.location,
      status: searchForm.value.status
    }
    console.log(params)
    const result = await getUserLostFoundList(params)

    if (result.success) {
      myPostsList.value = result.data.records
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
  fetchMyPostsList()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    title: '',
    location: '',
    status: null
  }
  pagination.value.page = 1
  fetchMyPostsList()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchMyPostsList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchMyPostsList()
}

// 删除失物信息
const deletePost = (id) => {
  ElMessageBox.confirm('确定要删除这条失物信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const result = await deleteLostFound(id)
      if (result.success) {
        ElMessage.success('删除成功')
        fetchMyPostsList()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {
    // 用户取消删除
  })
}
/* const updataStatus =async (id)=>{
    const result = await updateLostFoundStatus(id,2)
    if (result.success){
        
    }
} */
// 编辑失物信息
const editPost = (row) => {
  editForm.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    location: row.location,
    date: row.date,
    contactPhone: row.contactPhone,
    imageUrls: row.images && row.images.length > 0 ? [...row.images] : []
  }
  imageFiles.value = [] // 清空本地文件列表
  editDialogVisible.value = true
}

// 处理图片上传
const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  imageFiles.value = [...imageFiles.value, ...files]
}

// 移除图片文件
const removeImageFile = (index) => {
  imageFiles.value.splice(index, 1)
}

// 移除已上传的图片URL
const removeImageUrl = (index) => {
  editForm.value.imageUrls.splice(index, 1)
}

// 更新失物信息
const updatePost = async () => {
  try {
    // 先上传新选择的图片
    if (imageFiles.value.length > 0) {
      const uploadResult = await uploadImages(imageFiles.value)
      if (!uploadResult.success) {
        ElMessage.error(uploadResult.message || '图片上传失败')
        return
      }

      // 将新上传的图片URL添加到表单中
      const newUrls = Array.isArray(uploadResult.data) ? uploadResult.data : []
      editForm.value.imageUrls = [...editForm.value.imageUrls, ...newUrls]
    }

    // 准备提交的数据
    const postData = {
      title: editForm.value.title,
      content: editForm.value.content,
      location: editForm.value.location,
      date: editForm.value.date,
      contactPhone: editForm.value.contactPhone,
      imageUrls: editForm.value.imageUrls
    }

    const result = await updateLostFound(editForm.value.id, postData)
    if (result.success) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchMyPostsList()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
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
// 修改失物状态
const updatePostStatus = async (id, currentStatus) => {
  console.log('当前状态值:', currentStatus)
  console.log('状态类型:', typeof currentStatus)
  // 只有已审核状态才能修改为已领取
  if (currentStatus !== 1) {
    ElMessage.warning('只有已审核的物品才能修改为已领取状态')
    return
  }

  ElMessageBox.confirm('确定要将此物品标记为已领取吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 准备更新数据，只更新状态字段

      const result = await updateLostFoundStatus(id, 2)
      if (result.success) {
        ElMessage.success('状态更新成功')
        fetchMyPostsList()
      } else {
        ElMessage.error(result.message || '状态更新失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {
    // 用户取消操作
  })
}
// 页面初始化
onMounted(() => {
  fetchMyPostsList()
})
</script>

<style scoped>
.my-posts-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-top: 20px;
}

.image-upload-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-item {
  position: relative;
  width: 150px;
  height: 150px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}
</style>