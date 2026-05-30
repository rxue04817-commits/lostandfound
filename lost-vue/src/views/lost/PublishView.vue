<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import { uploadImages, publishLostFound, getCategories } from '@/api/lostApi'
import { ElMessage } from 'element-plus'
import { ITEM_TYPE, getLocationLabel, getDateLabel, getContentPlaceholder } from '@/utils/itemConstants'

const userInfo = ref(null)
const categories = ref([])
const itemType = ref(ITEM_TYPE.LOST)

const form = ref({
  itemType: ITEM_TYPE.LOST,
  categoryId: null,
  title: '',
  content: '',
  location: '',
  date: '',
  contactPhone: '',
  imageUrls: []
})

const imageFiles = ref([])
const isSubmitting = ref(false)
const windowURL = URL

const locationLabel = computed(() => getLocationLabel(itemType.value))
const dateLabel = computed(() => getDateLabel(itemType.value))
const contentPlaceholder = computed(() => getContentPlaceholder(itemType.value))

const onTypeChange = (type) => {
  itemType.value = type
  form.value.itemType = type
}

onMounted(async () => {
  userInfo.value = getUserInfo()
  const result = await getCategories()
  if (result.success) {
    categories.value = result.data || []
  }
})

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  imageFiles.value = [...imageFiles.value, ...files]
}

const removeImage = (index) => {
  imageFiles.value.splice(index, 1)
}

const resetForm = () => {
  form.value = {
    itemType: itemType.value,
    categoryId: null,
    title: '',
    content: '',
    location: '',
    date: '',
    contactPhone: '',
    imageUrls: []
  }
  imageFiles.value = []
}

const submitForm = async () => {
  isSubmitting.value = true
  try {
    form.value.itemType = itemType.value

    if (imageFiles.value.length > 0) {
      const uploadResult = await uploadImages(imageFiles.value)
      if (!uploadResult.success) {
        ElMessage.error(uploadResult.message || '图片上传失败')
        return
      }
      form.value.imageUrls = Array.isArray(uploadResult.data) ? uploadResult.data : []
    } else {
      form.value.imageUrls = []
    }

    const result = await publishLostFound(form.value)
    if (result.success) {
      ElMessage.success(result.message || '发布成功，等待审核')
      resetForm()
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
    <h2>发布信息</h2>

    <el-radio-group v-model="itemType" @change="onTypeChange" class="type-switch">
      <el-radio-button :value="0">我丢了东西</el-radio-button>
      <el-radio-button :value="1">我捡到了东西</el-radio-button>
    </el-radio-group>

    <el-form :model="form" label-width="100px" class="publish-form">
      <el-form-item label="物品分类" required>
        <el-select v-model="form.categoryId" placeholder="请选择物品分类" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标题" required>
        <el-input
          v-model="form.title"
          :placeholder="itemType === 0 ? '如：丢失黑色钱包' : '如：拾到学生证'"
        />
      </el-form-item>

      <el-form-item label="详细描述" required>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          :placeholder="contentPlaceholder"
        />
      </el-form-item>

      <el-form-item :label="locationLabel" required>
        <el-input v-model="form.location" :placeholder="'请输入' + locationLabel" />
      </el-form-item>

      <el-form-item :label="dateLabel" required>
        <el-date-picker
          v-model="form.date"
          type="date"
          :placeholder="'请选择' + dateLabel"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="联系电话" required>
        <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
      </el-form-item>

      <el-form-item label="图片上传">
        <div class="image-upload-area">
          <input
            type="file"
            multiple
            accept="image/*"
            @change="handleImageUpload"
            style="display: none"
            ref="fileInput"
          />
          <el-button @click="$refs.fileInput.click()">选择图片</el-button>
          <div class="image-preview" v-if="imageFiles.length > 0">
            <div v-for="(file, index) in imageFiles" :key="index" class="image-item">
              <img :src="windowURL.createObjectURL(file)" :alt="file.name" class="preview-image" />
              <el-button type="danger" size="small" @click="removeImage(index)" circle>X</el-button>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          @click="submitForm"
          :loading="isSubmitting"
          :disabled="!form.title || !form.content || !form.location || !form.date || !form.contactPhone || !form.categoryId"
        >
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

.type-switch {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.publish-form {
  margin-top: 8px;
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
  margin-right: 10px;
  margin-bottom: 25px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}
</style>
