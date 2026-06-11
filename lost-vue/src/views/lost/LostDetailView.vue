<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLostFoundDetail, postComment } from '@/api/lostApi'
import claimApi from '@/api/claimApi'
import { useStore } from 'vuex'
import {
  ITEM_TYPE_LABEL,
  ITEM_TYPE_TAG,
  getStatusLabel,
  getStatusTagType,
  getLocationLabel,
  getDateLabel
} from '@/utils/itemConstants'

const route = useRoute()
const router = useRouter()
const store = useStore()
const currentUser = computed(() => store.state.user)

const currentLostFound = ref(null)
const loading = ref(false)
const commentForm = ref({ content: '' })

const claimDialogVisible = ref(false)
const claimForm = ref({ description: '', contactPhone: '' })
const submittingClaim = ref(false)

const showClaimDialog = () => {
  claimForm.value = { description: '', contactPhone: currentUser.value?.phone || '' }
  claimDialogVisible.value = true
}

const submitClaim = async () => {
  if (!claimForm.value.description.trim() || !claimForm.value.contactPhone.trim()) {
    ElMessage.warning('请填写认领说明和联系电话')
    return
  }
  submittingClaim.value = true
  try {
    const data = {
      lostFoundId: currentLostFound.value.id,
      description: claimForm.value.description,
      contactPhone: claimForm.value.contactPhone
    }
    const result = await claimApi.submitClaim(data)
    if (result.data.code === 1) {
      ElMessage.success('认领申请已提交，等待拾得者审核')
      claimDialogVisible.value = false
      // 刷新详情状态
      await fetchDetail()
    } else {
      ElMessage.error(result.data.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    submittingClaim.value = false
  }
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const result = await getLostFoundDetail(id)
    if (result.success) {
      currentLostFound.value = result.data
      commentForm.value = { content: '' }
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  try {
    const lostFoundId = currentLostFound.value.id
    const result = await postComment(lostFoundId, commentForm.value.content)
    if (result.data.code === 1) {
      ElMessage.success('留言发布成功')
      commentForm.value.content = ''
      await fetchDetail()
    } else {
      ElMessage.error(result.data.msg || '留言发布失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="detail-container" v-loading="loading">
    <div class="header-bar">
      <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
      <h2>{{ currentLostFound?.title }}</h2>
    </div>

    <el-card class="detail-card" v-if="currentLostFound">
      <div class="detail-content">
        <div class="detail-header">
          <div class="header-left">
            <el-tag :type="ITEM_TYPE_TAG[currentLostFound.itemType]" size="large">
              {{ ITEM_TYPE_LABEL[currentLostFound.itemType] }}
            </el-tag>
            <el-tag :type="getStatusTagType(currentLostFound.status)" size="large" style="margin-left: 10px">
              {{ getStatusLabel(currentLostFound.status, currentLostFound.itemType) }}
            </el-tag>
          </div>
          <div class="header-right">
            <span>发布人：{{ currentLostFound.userInfo?.realName }}</span>
            <span style="margin-left: 20px">发布时间：{{ currentLostFound.createdAt }}</span>
          </div>
        </div>

        <div class="detail-body">
          <div class="info-section">
            <h3>基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="分类">{{ currentLostFound.categoryName }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentLostFound.contactPhone }}</el-descriptions-item>
              <el-descriptions-item label="地点" :span="2">
                {{ getLocationLabel(currentLostFound.itemType) }}：{{ currentLostFound.location }}
              </el-descriptions-item>
              <el-descriptions-item label="日期" :span="2">
                {{ getDateLabel(currentLostFound.itemType) }}：{{ currentLostFound.date }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="info-section">
            <h3>详细描述</h3>
            <div class="description-content">{{ currentLostFound.content }}</div>
          </div>

          <div class="info-section" v-if="currentLostFound.images?.length">
            <h3>物品图片</h3>
            <div class="images-gallery">
              <el-image
                v-for="(image, index) in currentLostFound.images"
                :key="index"
                :src="image"
                :preview-src-list="currentLostFound.images"
                :initial-index="index"
                fit="cover"
                class="gallery-image"
              />
            </div>
          </div>

          <div class="info-section">
            <h3>留言评论 ({{ currentLostFound.comments?.length || 0 }})</h3>
            <div class="comments-container">
              <div v-if="!currentLostFound.comments || currentLostFound.comments.length === 0" class="empty-comments">
                暂无留言
              </div>
              <div v-else v-for="comment in currentLostFound.comments" :key="comment.id" class="comment-item">
                <el-card class="comment-card" shadow="hover">
                  <div class="comment-header">
                    <el-avatar :src="comment.userInfo?.avatar" size="small" />
                    <span class="comment-user">{{ comment.userInfo?.realName }}</span>
                    <span class="comment-time">{{ comment.createdAt }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </el-card>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h3>发表留言</h3>
            <el-form :model="commentForm">
              <el-form-item>
                <el-input 
                  v-model="commentForm.content" 
                  type="textarea" 
                  placeholder="请输入留言内容" 
                  :rows="4"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitComment">提交留言</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div class="detail-footer">
          <template v-if="currentLostFound?.itemType === 1 && currentLostFound?.status === 1 && currentLostFound?.userId !== currentUser?.id">
            <el-button v-if="currentLostFound.claimStatus === 0" disabled size="large">认领待审核</el-button>
            <el-button v-else-if="currentLostFound.claimStatus === 1" disabled type="success" size="large">认领已通过</el-button>
            <el-button v-else-if="currentLostFound.claimStatus === 3" disabled type="success" size="large">认领已完成</el-button>
            <el-button v-else type="primary" size="large" @click="showClaimDialog">申请认领</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="claimDialogVisible" title="申请认领" width="500px">
      <el-form :model="claimForm" label-width="100px">
        <el-form-item label="认领说明" required>
          <el-input 
            v-model="claimForm.description" 
            type="textarea" 
            placeholder="请详细描述物品特征以证明您是失主" 
            :rows="4" 
          />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="claimForm.contactPhone" placeholder="请输入您的联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="claimDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitClaim" :loading="submittingClaim">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-container {
  padding: 20px;
  height: 600px;
}

.header-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 10px;
}

.header-bar h2 {
  margin: 0;
  flex: 1;
}

.detail-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.detail-content {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  margin-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;

}

.header-left {
  display: flex;
  gap: 10px;
}

.header-right {
  color: #606266;
  font-size: 14px;
}

.detail-body {
  padding: 10px 0;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.description-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #606266;
}

.images-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.gallery-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.gallery-image:hover {
  transform: scale(1.05);
}

.comments-container {
  max-height: 500px;
  overflow-y: auto;
}

.empty-comments {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.comment-item {
  margin-bottom: 15px;
}

.comment-card {
  transition: all 0.3s ease;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-user {
  font-weight: bold;
  color: #303133;
}

.comment-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
}

.detail-footer {
  display: flex;
  justify-content: center;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
  margin-top: 30px;
}
</style>
