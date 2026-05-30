<!-- src/views/admin/AdminUserManagement.vue -->
<template>
  <div class="admin-container">
    <h2>用户管理</h2>

    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" @submit.prevent="handleSearch">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="真实姓名">
              <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="手机号">
              <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="角色">
              <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
                <el-option label="普通用户" :value="0" />
                <el-option label="管理员" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
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
      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button
              v-if="row.status === 1 && !isSelf(row.id)"
              size="small"
              type="warning"
              @click="toggleStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              @click="toggleStatus(row, 1)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.role === 0 && !isSelf(row.id)"
              size="small"
              type="primary"
              @click="toggleRole(row, 1)"
            >
              设为管理员
            </el-button>
            <el-button
              v-if="row.role === 1 && !isSelf(row.id)"
              size="small"
              type="danger"
              @click="toggleRole(row, 0)"
            >
              取消管理员
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchUsers"
        @current-change="fetchUsers"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detailData.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ detailData.role === 1 ? '管理员' : '普通用户' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ detailData.status === 1 ? '启用' : '禁用' }}
        </el-descriptions-item>
        <el-descriptions-item label="发布数量">{{ detailData.postCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailData.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo } from '@/utils/auth'
import { getUserList, getUserDetail, updateUserStatus, updateUserRole } from '@/api/adminApi'

const currentUser = ref(getUserInfo())
const loading = ref(false)
const userList = ref([])
const detailVisible = ref(false)
const detailData = ref(null)

const searchForm = ref({
  username: '',
  realName: '',
  phone: '',
  role: null,
  status: null
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const isSelf = (userId) => currentUser.value?.id === userId

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] === null) delete params[key]
    })
    const result = await getUserList(params)
    if (result.success) {
      userList.value = result.data.records || []
      pagination.value.total = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取用户列表失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchUsers()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    realName: '',
    phone: '',
    role: null,
    status: null
  }
  pagination.value.page = 1
  fetchUsers()
}

const viewDetail = async (id) => {
  const result = await getUserDetail(id)
  if (result.success) {
    detailData.value = result.data
    detailVisible.value = true
  } else {
    ElMessage.error(result.message || '获取详情失败')
  }
}

const toggleStatus = async (row, status) => {
  const action = status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.realName}」吗？`, '提示', { type: 'warning' })
    const result = await updateUserStatus(row.id, status)
    if (result.success) {
      ElMessage.success(`${action}成功`)
      fetchUsers()
    } else {
      ElMessage.error(result.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const toggleRole = async (row, role) => {
  const action = role === 1 ? '设为管理员' : '取消管理员'
  try {
    await ElMessageBox.confirm(`确定要将用户「${row.realName}」${action}吗？`, '提示', { type: 'warning' })
    const result = await updateUserRole(row.id, role)
    if (result.success) {
      ElMessage.success('操作成功')
      fetchUsers()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.search-card,
.list-card {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
