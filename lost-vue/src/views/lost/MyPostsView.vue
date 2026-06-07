<template>
  <div class="my-posts-container">
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
      <el-table :data="myPostsList" style="width: 100%">
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
            <el-tag>{{ getStatusLabel(row.status, row.itemType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="editPost(row)" :disabled="row.status === 2 || row.status === 3">修改</el-button>
            <el-button size="small" type="danger" @click="deletePost(row.id)">删除</el-button>
            <el-button
              v-if="row.itemType === 0"
              size="small"
              type="primary"
              @click="updatePostStatus(row)"
              :disabled="row.status !== 1"
            >
              设为已找回
            </el-button>
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

    <el-dialog v-model="editDialogVisible" title="编辑信息" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="类型">
          <el-tag :type="ITEM_TYPE_TAG[editForm.itemType]">{{ ITEM_TYPE_LABEL[editForm.itemType] }}</el-tag>
          <span class="tip-text">（类型不可修改）</span>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item :label="getLocationLabel(editForm.itemType)">
          <el-input v-model="editForm.location" />
        </el-form-item>
        <el-form-item :label="getDateLabel(editForm.itemType)">
          <el-date-picker v-model="editForm.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="editForm.content" type="textarea" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-preview" v-if="editForm.imageUrls?.length">
            <div v-for="(url, index) in editForm.imageUrls" :key="'url-' + index" class="image-item">
              <el-image :src="url" fit="cover" class="preview-image" />
              <el-button type="danger" size="small" @click="removeImageUrl(index)" circle>X</el-button>
            </div>
          </div>
          <div class="image-preview" v-if="imageFiles.length > 0">
            <div v-for="(file, index) in imageFiles" :key="'file-' + index" class="image-item">
              <img :src="windowURL.createObjectURL(file)" class="preview-image" />
              <el-button type="danger" size="small" @click="removeImageFile(index)" circle>X</el-button>
            </div>
          </div>
          <div class="image-upload-area">
            <input type="file" multiple accept="image/*" @change="handleImageUpload" style="display: none" ref="fileInput" />
            <el-button @click="$refs.fileInput.click()">选择图片</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updatePost">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="donationDialogVisible" title="恭喜找回失物！" width="400px" :show-close="false" :close-on-click-modal="false" :close-on-press-escape="false">
      <div style="text-align: center;">
        <p>感谢使用本平台！您的支持是我们持续运营的动力。</p>
        <div style="margin: 20px 0;">
          <el-radio-group v-model="donationAmount" @change="handleAmountChange">
            <el-radio-button :value="6.6">6.6元</el-radio-button>
            <el-radio-button :value="8.8">8.8元</el-radio-button>
            <el-radio-button :value="18.8">18.8元</el-radio-button>
            <el-radio-button :value="0">自定义</el-radio-button>
          </el-radio-group>
        </div>
        <div v-if="isCustomAmount" style="margin-bottom: 20px;">
          <el-input-number v-model="donationAmount" :min="0.1" :precision="2" :step="1" placeholder="请输入金额" />
        </div>
      </div>
      <template #footer>
        <el-button @click="skipDonation">下次一定</el-button>
        <el-button type="primary" @click="handleDonationSubmit">感谢平台(支付宝支付)</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserLostFoundList,
  deleteLostFound,
  updateLostFound,
  uploadImages,
  updateLostFoundStatus,
  getCategories
} from '@/api/lostApi'
import {
  ITEM_TYPE_LABEL,
  ITEM_TYPE_TAG,
  getStatusLabel,
  getLocationLabel,
  getDateLabel
} from '@/utils/itemConstants'

import donationApi from '@/api/donationApi'

const categories = ref([])
const searchForm = ref({ title: '', itemType: null, categoryId: null, status: null })
const pagination = ref({ page: 1, size: 10, total: 0 })
const myPostsList = ref([])
const loading = ref(false)
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  itemType: 0,
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})
const imageFiles = ref([])
const windowURL = URL

const donationDialogVisible = ref(false)
const donationAmount = ref(6.6)
const isCustomAmount = ref(false)
const currentFoundItem = ref(null)

const showDonationDialog = (row) => {
  currentFoundItem.value = row
  donationAmount.value = 6.6
  isCustomAmount.value = false
  donationDialogVisible.value = true
}

const handleAmountChange = (val) => {
  if (val === 0) {
    isCustomAmount.value = true
    donationAmount.value = 0 // 切换到自定义时清空金额
  } else {
    isCustomAmount.value = false
  }
}

const handleDonationSubmit = async () => {
  if (isCustomAmount.value && (!donationAmount.value || donationAmount.value <= 0)) {
    ElMessage.warning('请输入有效的打赏金额')
    return
  }
  const amountToPay = isCustomAmount.value ? donationAmount.value : (donationAmount.value === 0 ? 0.1 : donationAmount.value);
  if(amountToPay <= 0) {
      ElMessage.warning('请输入有效的打赏金额')
      return
  }
  
  try {
    const res = await donationApi.pay({
      lostFoundId: currentFoundItem.value.id,
      amount: amountToPay
    })
    console.log("Payment ", res)
    if (res.success && res.data) {
      // 支付宝返回的是HTML表单，我们可以将其写入一个临时div并提交
      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      document.forms[document.forms.length - 1].submit()
    } else {
      ElMessage.error(res.message || '发起支付失败')
    }
  } catch (error) {
    console.error('Payment error:', error)
    ElMessage.error('网络请求异常')
  }
}

const skipDonation = () => {
  donationDialogVisible.value = false
  fetchMyPostsList()
}

const fetchMyPostsList = async () => {
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

const handleSearch = () => {
  pagination.value.page = 1
  fetchMyPostsList()
}

const handleReset = () => {
  searchForm.value = { title: '', itemType: null, categoryId: null, status: null }
  pagination.value.page = 1
  fetchMyPostsList()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchMyPostsList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchMyPostsList()
}

const deletePost = (id) => {
  ElMessageBox.confirm('确定要删除这条信息吗？', '提示', { type: 'warning' }).then(async () => {
    const result = await deleteLostFound(id)
    if (result.success) {
      ElMessage.success('删除成功')
      fetchMyPostsList()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  }).catch(() => {})
}

const editPost = (row) => {
  editForm.value = {
    id: row.id,
    itemType: row.itemType,
    title: row.title,
    content: row.content,
    location: row.location,
    date: row.date,
    contactPhone: row.contactPhone,
    imageUrls: row.images?.length ? [...row.images] : []
  }
  imageFiles.value = []
  editDialogVisible.value = true
}

const handleImageUpload = (event) => {
  imageFiles.value = [...imageFiles.value, ...Array.from(event.target.files)]
}

const removeImageFile = (index) => imageFiles.value.splice(index, 1)
const removeImageUrl = (index) => editForm.value.imageUrls.splice(index, 1)

const updatePost = async () => {
  try {
    if (imageFiles.value.length > 0) {
      const uploadResult = await uploadImages(imageFiles.value)
      if (!uploadResult.success) {
        ElMessage.error(uploadResult.message || '图片上传失败')
        return
      }
      editForm.value.imageUrls = [...editForm.value.imageUrls, ...(uploadResult.data || [])]
    }
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

const updatePostStatus = async (row) => {
  if (row.status !== 1) return
  ElMessageBox.confirm('确定要将此物品标记为已找回吗？', '提示', { type: 'warning' }).then(async () => {
    const result = await updateLostFoundStatus(row.id, 2)
    if (result.success) {
      ElMessage.success('状态更新成功')
      showDonationDialog(row)
    } else {
      ElMessage.error(result.message || '状态更新失败')
    }
  }).catch(() => {})
}

onMounted(async () => {
  const catResult = await getCategories()
  if (catResult.success) categories.value = catResult.data || []
  fetchMyPostsList()
})
</script>

<style scoped>
.my-posts-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.list-card { margin-top: 20px; }
.tip-text { margin-left: 8px; color: #909399; font-size: 12px; }
.image-upload-area { margin-top: 10px; }
.image-preview { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
.image-item { position: relative; width: 150px; height: 150px; }
.preview-image { width: 100%; height: 100%; object-fit: cover; border-radius: 4px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
