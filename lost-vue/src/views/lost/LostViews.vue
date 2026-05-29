<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLostFoundList, getLostFoundDetail, postComment } from '@/api/lostApi'

// 搜索表单数据
const searchForm = ref({
  title: '',
  location: '',
  status: null,
  content: ''
})

// 分页数据
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 失物列表数据
const lostFoundList = ref([])
//失物详细
const currentLostFound = ref(null)

// 加载状态
const loading = ref(false)
//失物详细对话框
const detailDialogVisible = ref(false)
// 获取失物列表
const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      title: searchForm.value.title,
      location: searchForm.value.location,
      status: searchForm.value.status,
      content: searchForm.value.content
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
//查看失物详细
const showDetail = async (id) => {
  try {
    const result = await getLostFoundDetail(id)
    if (result.success == 1) {
      currentLostFound.value = result.data
      commentForm.value = currentLostFound.value.comments
      console.log("详细信息", currentLostFound.value)
      console.log('留言信息', commentForm.value)
      detailDialogVisible.value = true
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
}
// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchLostFoundList()
}
// 在<script setup>中修改 commentForm 的初始化
const commentForm = ref({
  content: ''
})
// 重置搜索
const handleReset = () => {
  searchForm.value = {
    title: '',
    location: '',
    status: null,
    content: ''
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
      // 重新加载详情以更新评论列表
      await showDetail(lostFoundId)
    } else {
      ElMessage.error(result.data.msg || '留言发布失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
}
  

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchLostFoundList()
}
// 添加状态标签映射
const getStatusLabel = (status) => {
  const statusMap = {
    '0': '待审核',
    '1': '已审核',
    '2': '已领取',
    '3': '已过期'
  }
  return statusMap[status] || '未知状态'
}
// 页面初始化
onMounted(() => {
  fetchLostFoundList()
})
</script>

<template>
  <div class="lost-found-container">
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
          <el-col :span="6">
            <el-form-item label="描述">
              <el-input v-model="searchForm.content" placeholder="请输入描述" clearable />
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

    <!-- 失物列表 -->
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
            <el-tag>
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="userInfo.realName" label="发布人" width="120">
          <template #default="{ row }">
            <span>{{ row.userInfo?.realName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </el-card>
  </div>
  <!-- 详情对话框 -->
  <!-- 详情对话框 -->
  <el-dialog v-model="detailDialogVisible" title="失物详情" width="800px">
    <div class="detail-content">
      <div class="detail-header">
        <h3>{{ currentLostFound?.title }}</h3>
        <p>发布人：{{ currentLostFound?.userInfo?.realName }} |
          发布时间：{{ currentLostFound?.createdAt }}</p>
      </div>

      <div class="detail-body">
        <div class="detail-item">
          <strong>详细描述：</strong>
          <p>{{ currentLostFound?.content }}</p>
        </div>

        <div class="detail-item">
          <strong>丢失地点：</strong>
          <p>{{ currentLostFound?.location }}</p>
        </div>

        <div class="detail-item">
          <strong>联系电话：</strong>
          <p>{{ currentLostFound?.contactPhone }}</p>
        </div>

        <div class="detail-item">
          <strong>图片：</strong>
          <div class="images-container">
           <!--  <h1>{{ currentLostFound?.images }}</h1> -->
            <img v-for="(image, index) in currentLostFound?.images" :key="index" :src="image" alt="失物图片"
              style="max-width: 200px; max-height: 200px; margin-right: 10px;">
          </div>
        </div>

        <!-- 留言评论部分 -->
        <div class="detail-item">
          <strong>留言评论：</strong>
          <div class="comments-container">
            <div v-for="(comment) in currentLostFound?.comments" :key="comment.id" class="comment-item">
              <el-card class="comment-card">
                <div class="comment-header">
                  <el-avatar :src="comment.userInfo?.avatar" size="small" />
                  <span class="comment-user">{{ comment.userInfo?.realName }}</span>
                </div>
                <div class="comment-content">
                  {{ comment.content }}
                </div>
              </el-card>
            </div>
          </div>
        </div>

        <!-- 发表留言表单部分 -->
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

    <!-- 正确放置 footer 插槽 -->
    <template #footer>
      <el-button @click="detailDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.lost-found-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-top: 20px;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.detail-body {
  padding: 20px;
}

.detail-item {
  margin-bottom: 20px;
}

.images-container {
  display: flex;
  flex-wrap: wrap;
}

.detail-item strong {
  color: #303133;
  margin-right: 10px;
}
</style>