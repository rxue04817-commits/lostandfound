<script setup>
import { ref, onMounted,computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, removeUserInfo, removeToken } from '@/utils/auth'
import {
  Plus,
  Document,
  User,
  DataAnalysis,
  UserFilled
} from '@element-plus/icons-vue'
// 用户头像URL
const userInfo = ref(null)
const router = useRouter() // 正确初始化 router
//const token = localStorage.getItem('token')
// 组件挂载时加载用户信息
onMounted(() => {
  userInfo.value = getUserInfo()
  console.log('用户信息:', userInfo.value)
})
const remove = () => {
  removeToken()  // 先移除token
  removeUserInfo()
  router.push('/login')
}
// 在 script setup 中添加
const isAdministrator = computed(() => {
  return userInfo.value && userInfo.value.role === 1
})
</script>
<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-content">
        <div class="empty-placeholder"></div>
        <h1 class="platform-title">失物招领平台</h1>
        <div class="user-info">
          <span class="welcome-text">欢迎您 {{ userInfo?.realName }}</span>
          <el-avatar :size="50" :src="userInfo?.avatar" class="user-avatar" />
          <el-button @click="remove" type="primary" size="small" class="logout-btn">退出登录</el-button>
        </div>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-row class="tac">
          <el-col :span="24">
            <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" router>
              <el-menu-item index="/publish">
                <el-icon>
                  <Plus />
                </el-icon>
                <span>发布失物</span>
              </el-menu-item>
              <el-menu-item index="/my-posts">
                <el-icon>
                  <Document />
                </el-icon>
                <span>查看我的发布</span>
              </el-menu-item>
              <el-menu-item index="/all-posts">
                <el-icon>
                  <Document />
                </el-icon>
                <span>失物信息</span>
              </el-menu-item>
              <el-menu-item index="/profile">
                <el-icon>
                  <User />
                </el-icon>
                <span>修改个人信息</span>
              </el-menu-item>
              <el-menu-item index="/statistics">
                <el-icon>
                  <DataAnalysis />
                </el-icon>
                <span>数据统计</span>
              </el-menu-item>
              <el-menu-item v-if="isAdministrator" index="/admin">
                <el-icon>
                  <DataAnalysis />
                </el-icon>
                <span>管理员审核</span>
              </el-menu-item>
              <el-menu-item v-if="isAdministrator" index="/admin/users">
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>用户管理</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-aside>
      <el-container>
        <el-main>
          <router-view></router-view>
        </el-main>
        <el-footer>
          <div class="footer-content">
            <span>失物招领平台 © 2025</span>
            <span>联系方式：support@lostfound.com</span>

          </div>
        </el-footer>
      </el-container>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 0;
  height: 80px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  height: 100%;
}

.platform-title {
  margin: 0;
  color: white;
  font-size: 32px;
  font-weight: 600;
  text-align: center;
  flex: 1;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.welcome-text {
  color: white;
  font-size: 16px;
  font-weight: 500;
}

.user-avatar {
  cursor: pointer;
  border: 2px solid white;
}

.logout-btn {
  margin-left: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.empty-placeholder {
  width: 100px;
}

.el-footer {
  background-color: #f8f9fa;
  color: #6c757d;
  text-align: center;
  padding: 16px 0;
  font-size: 14px;
  border-top: 1px solid #e9ecef;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.sidebar {
  background: linear-gradient(180deg, #4b6cb7 0%, #182848 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  height: calc(100vh - 80px);
}

.el-menu {
  border: none;
  background: transparent;
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white;
}

.el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.15) !important;
  color: white;
  border-right: 3px solid #409eff;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>