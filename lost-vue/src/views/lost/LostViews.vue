<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getLostFoundList, getLostFoundDetail, postComment, getCategories } from '@/api/lostApi'
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

const store = useStore()
const currentUser = computed(() => store.state.user)

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
const currentLostFound = ref(null)
const loading = ref(false)
const detailDialogVisible = ref(false)
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
    console.log(result.data)
    if (result.data.code === 1) {
      ElMessage.success('认领申请已提交，等待拾得者审核')
      claimDialogVisible.value = false
      // 刷新详情状态
      await showDetail(currentLostFound.value.id)
    } else {
      ElMessage.error(result.data.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    submittingClaim.value = false
  }
}

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

const showDetail = async (id) => {
  try {
    const result = await getLostFoundDetail(id)
    if (result.success) {
      currentLostFound.value = result.data
      commentForm.value = { content: '' }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
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

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchLostFoundList()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchLostFoundList()
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
      await showDetail(lostFoundId)
    } else {
      ElMessage.error(result.data.msg || '留言发布失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
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
            <el-button size="small" @click="showDetail(row.id)">查看详情</el-button>
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

    <el-dialog
      v-model="detailDialogVisible"
      :title="(currentLostFound?.itemType === 1 ? '拾物' : '失物') + '详情'"
      width="800px"
    >
      <div class="detail-content" v-if="currentLostFound">
        <div class="detail-header">
          <h3>
            <el-tag :type="ITEM_TYPE_TAG[currentLostFound.itemType]" size="small" style="margin-right: 8px">
              {{ ITEM_TYPE_LABEL[currentLostFound.itemType] }}
            </el-tag>
            {{ currentLostFound.title }}
          </h3>
          <p>
            分类：{{ currentLostFound.categoryName }} |
            发布人：{{ currentLostFound.userInfo?.realName }} |
            发布时间：{{ currentLostFound.createdAt }}
          </p>
        </div>

        <div class="detail-body">
          <div class="detail-item">
            <strong>详细描述：</strong>
            <p>{{ currentLostFound.content }}</p>
          </div>
          <div class="detail-item">
            <strong>{{ getLocationLabel(currentLostFound.itemType) }}：</strong>
            <p>{{ currentLostFound.location }}</p>
          </div>
          <div class="detail-item">
            <strong>{{ getDateLabel(currentLostFound.itemType) }}：</strong>
            <p>{{ currentLostFound.date }}</p>
          </div>
          <div class="detail-item">
            <strong>联系电话：</strong>
            <p>{{ currentLostFound.contactPhone }}</p>
          </div>
          <div class="detail-item" v-if="currentLostFound.images?.length">
            <strong>图片：</strong>
            <div class="images-container">
              <img
                v-for="(image, index) in currentLostFound.images"
                :key="index"
                :src="image"
                alt="图片"
                style="max-width: 200px; max-height: 200px; margin-right: 10px;"
              />
            </div>
          </div>
          <div class="detail-item">
            <strong>留言评论：</strong>
            <div class="comments-container">
              <div v-for="comment in currentLostFound.comments" :key="comment.id" class="comment-item">
                <el-card class="comment-card">
                  <div class="comment-header">
                    <el-avatar :src="comment.userInfo?.avatar" size="small" />
                    <span class="comment-user">{{ comment.userInfo?.realName }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </el-card>
              </div>
            </div>
          </div>
          <div class="detail-item">
            <strong>发表留言：</strong>
            <el-form :model="commentForm" label-width="80px">
              <el-form-item label="内容">
                <el-input v-model="commentForm.content" type="textarea" placeholder="请输入留言内容" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitComment">提交留言</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <template v-if="currentLostFound?.itemType === 1 && currentLostFound?.status === 1 && currentLostFound?.userId !== currentUser?.id">
            <el-button v-if="currentLostFound.claimStatus === 0" disabled>认领待审核</el-button>
            <el-button v-else-if="currentLostFound.claimStatus === 1" disabled type="success">认领已通过</el-button>
            <el-button v-else-if="currentLostFound.claimStatus === 3" disabled type="success">认领已完成</el-button>
            <el-button v-else type="primary" @click="showClaimDialog">申请认领</el-button>
          </template>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="claimDialogVisible" title="申请认领" width="500px">
      <el-form :model="claimForm" label-width="100px">
        <el-form-item label="认领说明" required>
          <el-input v-model="claimForm.description" type="textarea" placeholder="请详细描述物品特征以证明您是失主" rows="4" />
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
.lost-found-container { padding: 20px; }
.type-tabs { margin-bottom: 16px; }
.search-card { margin-bottom: 20px; }
.list-card { margin-top: 20px; }
.detail-content { padding: 20px; }
.detail-header { border-bottom: 1px solid #eee; padding-bottom: 20px; margin-bottom: 20px; }
.detail-item { margin-bottom: 20px; }
.images-container { display: flex; flex-wrap: wrap; }
.detail-item strong { color: #303133; margin-right: 10px; }
.el-pagination { margin-top: 20px; justify-content: flex-end; }
</style>
