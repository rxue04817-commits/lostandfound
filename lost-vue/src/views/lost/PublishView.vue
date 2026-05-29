<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import { uploadImages, publishLostFound } from '@/api/lostApi'
import { ElMessage } from 'element-plus'

const userInfo = ref(null)
const form = ref({
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})

const imageFiles = ref([])
const isSubmitting = ref(false)

// 显式定义 URL 对象引用
const windowURL = URL

onMounted(() => {
  userInfo.value = getUserInfo()
  console.log('发布页面:', userInfo.value)
})

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  imageFiles.value = [...imageFiles.value, ...files]
}

const removeImage = (index) => {
  imageFiles.value.splice(index, 1)
}

const submitForm = async () => {
  isSubmitting.value = true
  
  try {
    // 先上传图片
    if (imageFiles.value.length > 0) {
      const imageData = new FormData()
      imageFiles.value.forEach(file => {
        imageData.append('images', file)
      })
      
      const uploadResult = await uploadImages(imageFiles.value)
      if (!uploadResult.success) {
        ElMessage.error(uploadResult.message || '图片上传失败')
        return
      }
      
    
      form.value.imageUrls = Array.isArray(uploadResult.data) ? uploadResult.data : []

    }
    
    // 发布失物信息
    const result = await publishLostFound(form.value)
    if (result.success) {
      ElMessage.success(result.message || '发布成功')
      // 清空表单
      form.value = {
        title: '',
        content: '',
        location: '',
        date: '',
        contactPhone: '',
        imageUrls: []
      }
      imageFiles.value = []
    } else {
      ElMessage.error(result.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('发布过程中出现错误')
    console.error(error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="publish-container">
    <h2>发布失物招领信息</h2>
    <el-form :model="form" label-width="100px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入物品名称"></el-input>
      </el-form-item>
      
      <el-form-item label="详细描述">
        <el-input 
          v-model="form.content" 
          type="textarea" 
          placeholder="请详细描述物品特征及丢失情况">
        </el-input>
      </el-form-item>
      
      <el-form-item label="丢失地点">
        <el-input v-model="form.location" placeholder="请输入丢失地点"></el-input>
      </el-form-item>
      
      <el-form-item label="丢失日期">
        <el-date-picker
          v-model="form.date"
          type="date"
          placeholder="请选择丢失日期"
          value-format="YYYY-MM-DD">
        </el-date-picker>
      </el-form-item>
      
      <el-form-item label="联系电话">
        <el-input v-model="form.contactPhone" placeholder="请输入联系电话"></el-input>
      </el-form-item>
      
      <el-form-item label="图片上传">
        <div class="image-upload-area">
          <input 
            type="file" 
            multiple 
            accept="image/*" 
            @change="handleImageUpload"
            style="display: none"
            ref="fileInput">
          <el-button @click="$refs.fileInput.click()">选择图片</el-button>
          
          <div class="image-preview" v-if="imageFiles.length > 0">
            <div 
              v-for="(file, index) in imageFiles" 
              :key="index"
              class="image-item">
              <img 
                :src="windowURL.createObjectURL(file)" 
                :alt="file.name"
                class="preview-image">
              <el-button 
                type="danger" 
                size="small" 
                @click="removeImage(index)"
                circle>
                X
              </el-button>
            </div>
          </div>
        </div>
      </el-form-item>
      
      <el-form-item>
        <el-button 
          type="primary" 
          @click="submitForm" 
          :loading="isSubmitting"
          :disabled="!form.title || !form.content || !form.location || !form.date || !form.contactPhone">
          {{ isSubmitting ? '发布中...' : '发布' }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
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