<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, setUserInfo } from '@/utils/auth'
import { updateUserInfo, uploadAvatar } from '@/api/userApi'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  password:''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const dataLoading = ref(false)
const currentUser = ref(null)
const isEditing = ref(false)
const isChangingPassword = ref(false)
const fileInput = ref(null)
const originalUserInfo = ref({})

onMounted(async () => {
  await loadUserProfile()
})

const loadUserProfile = async () => {
  dataLoading.value = true
  try {
    const user = getUserInfo()
    if (user) {
      currentUser.value = user
      userInfo.value.realName = user.realName || ''
      userInfo.value.phone = user.phone || ''
      userInfo.value.email = user.email || ''
      userInfo.value.avatar = user.avatar || ''
      userInfo.value.password = user.password || ''
      // 保存原始数据副本
      originalUserInfo.value = { ...userInfo.value }
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    ElMessage.error('获取用户信息时出现错误')
    console.error(error)
  } finally {
    dataLoading.value = false
  }
}

const handleSubmit = async () => {
  // 简单验证
  if (!userInfo.value.realName.trim()) {
    ElMessage.error('请输入真实姓名')
    return
  }

  if (userInfo.value.phone && !/^1[3-9]\d{9}$/.test(userInfo.value.phone)) {
    ElMessage.error('请输入正确的手机号格式')
    return
  }

  if (userInfo.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userInfo.value.email)) {
    ElMessage.error('请输入正确的邮箱格式')
    return
  }

  loading.value = true
  try {
    // 如果正在修改密码，则合并密码信息
  /*   let userData = { ...userInfo.value } */
    if (isChangingPassword.value) {
      if (!passwordForm.value.newPassword) {
        ElMessage.error('请输入新密码')
        loading.value = false
        return
      }
      
      if (passwordForm.value.newPassword.length < 6) {
        ElMessage.error('新密码长度不能少于6位')
        loading.value = false
        return
      }
      
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        ElMessage.error('两次输入的新密码不一致')
        loading.value = false
        return
      }
      
      if (passwordForm.value.oldPassword === passwordForm.value.newPassword) {
        ElMessage.error('新密码不能与旧密码相同')
        loading.value = false
        return
      }
      
     /*  userData = {
        ...userData,
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      } */
      userInfo.value.password=passwordForm.value.newPassword
    }
    console.log("修改的个人信息",userInfo.value)
    const result = await updateUserInfo(userInfo.value)
    if (result.success) {
      ElMessage.success(isChangingPassword.value ? '密码修改成功' : '个人信息更新成功')
      
      // 更新本地存储的用户信息
      if (currentUser.value) {
        const updatedUser = {
          ...currentUser.value,
          realName: userInfo.value.realName,
          phone: userInfo.value.phone,
          email: userInfo.value.email,
          avatar: userInfo.value.avatar
        }
        
        // 如果修改了密码，也更新本地存储中的密码
        if (isChangingPassword.value) {
          updatedUser.password = passwordForm.value.newPassword
        }
        
        setUserInfo(updatedUser)
        currentUser.value = updatedUser
        originalUserInfo.value = { ...userInfo.value } // 更新原始数据副本
      }
      
      isEditing.value = false
      isChangingPassword.value = false
      
      // 清空密码表单
      passwordForm.value.oldPassword = ''
      passwordForm.value.newPassword = ''
      passwordForm.value.confirmPassword = ''
    } else {
      ElMessage.error(result.message || (isChangingPassword.value ? '密码修改失败' : '更新失败'))
    }
  } catch (error) {
    ElMessage.error(isChangingPassword.value ? '密码修改过程中出现错误' : '更新过程中出现错误')
    console.error(error)
  } finally {
    loading.value = false
  }
}


const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  isChangingPassword.value = false
  // 恢复原始数据
  userInfo.value = { ...originalUserInfo.value }
  // 清空密码表单
  passwordForm.value.oldPassword = ''
  passwordForm.value.newPassword = ''
  passwordForm.value.confirmPassword = ''
}

const startChangePassword = () => {
  isChangingPassword.value = true
}
// 头像上传相关函数
const triggerFileSelect = () => {
  fileInput.value.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 检查文件大小 (限制为2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  try {
    const result = await uploadAvatar(file)
    if (result.success) {
      ElMessage.success('头像上传成功')
      // 更新头像URL
      userInfo.value.avatar = result.data.data
    } else {
      ElMessage.error(result.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传过程中出现错误')
    console.error(error)
  }
}
</script>

<template>
  <div class="user-profile-container">
    <h2>个人信息</h2>

    <!-- 添加加载指示器 -->
    <div v-if="dataLoading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="circle" style="width: 80px; height: 80px;" />
          <br><br>
          <el-skeleton-item variant="p" style="width: 100%; height: 40px;" />
          <br>
          <el-skeleton-item variant="p" style="width: 100%; height: 40px;" />
          <br>
          <el-skeleton-item variant="p" style="width: 100%; height: 40px;" />
        </template>
      </el-skeleton>
    </div>

    <el-form v-else :model="userInfo" label-width="100px" @submit.prevent="handleSubmit">
      <el-form-item label="头像">
        <div class="avatar-container">
          <el-avatar :size="80"
            :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          <el-button @click="triggerFileSelect" style="margin-top: 10px;">
            上传头像
          </el-button>
          <input ref="fileInput" type="file" accept="image/*" @change="handleAvatarUpload" style="display: none" />
        </div>
      </el-form-item>

      <el-form-item label="真实姓名">
        <el-input v-model="userInfo.realName" placeholder="请输入真实姓名" :disabled="!isEditing">
        </el-input>
      </el-form-item>

      <el-form-item label="手机号">
        <el-input v-model="userInfo.phone" placeholder="请输入手机号" :disabled="!isEditing">
        </el-input>
      </el-form-item>

      <el-form-item label="邮箱">
        <el-input v-model="userInfo.email" placeholder="请输入邮箱地址" :disabled="!isEditing">
        </el-input>
      </el-form-item>

      <!-- 密码修改表单项 -->
      <template v-if="isChangingPassword">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码(至少6位)" show-password />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </template>

      <el-form-item>
        <div v-if="!isEditing && !isChangingPassword">
          <el-button @click="startEdit">编辑信息</el-button>
          <el-button @click="startChangePassword" style="margin-left: 10px;">修改密码</el-button>
        </div>
        <div v-else>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存修改
          </el-button>
          <el-button @click="cancelEdit" style="margin-left: 10px;">
            取消
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.user-profile-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.loading-container {
  padding: 20px;
}
</style>