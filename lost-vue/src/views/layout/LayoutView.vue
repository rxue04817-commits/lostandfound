<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, removeUserInfo, removeToken } from '@/utils/auth'
import { getUserLostFoundList } from '@/api/lostApi'
import { filterMenusByRole } from '@/config/permission'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 用户头像URL
const userInfo = ref(null)
const router = useRouter() // 正确初始化 router
const hasFoundItem = ref(false)
const visibleMenus = ref([])

//组件挂载时加载用户信息
onMounted(async () => {
  userInfo.value = getUserInfo()
  
  // 如果没有用户信息，说明未登录，不应该发起请求
  if (!userInfo.value) {
    console.warn('未检测到用户信息，请检查登录状态')
    return
  }
  
  try {
    const res = await getUserLostFoundList({ itemType: 1, size: 1 })
    if (res.success && res.data.total > 0) {
      hasFoundItem.value = true
    }
  } catch (error) {
    // 401错误会被拦截器处理，这里只需要记录日志
    console.error('获取用户拾物列表失败:', error)
  }
  
  // 根据用户角色过滤菜单
  visibleMenus.value = filterMenusByRole(userInfo.value.role, {
    hasFoundItem: hasFoundItem.value
  })
})

const remove = () => {
  removeToken()  // 先移除token
  removeUserInfo()
  router.push('/login')
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return ElementPlusIconsVue[iconName]
}

// 当前激活的菜单
const activeMenu = computed(() => {
  return router.currentRoute.value.path
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
              <el-menu-item 
                v-for="menu in visibleMenus" 
                :key="menu.path" 
                :index="menu.path"
              >
                <el-icon>
                  <component :is="getIconComponent(menu.icon)" />
                </el-icon>
                <span>{{ menu.title }}</span>
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
            <span>失物招领平台 © 2026</span>
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
  background: var(--gradient-primary);
  box-shadow: var(--shadow-dark);
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
  letter-spacing: 2px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: var(--radius-round);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
}

.welcome-text {
  color: white;
  font-size: 16px;
  font-weight: 500;
}

.user-avatar {
  cursor: pointer;
  border: 2px solid white;
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.logout-btn {
  margin-left: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.empty-placeholder {
  width: 100px;
}

.el-footer {
  background-color: var(--bg-white);
  color: var(--text-secondary);
  text-align: center;
  padding: 16px 0;
  font-size: 14px;
  border-top: 1px solid var(--border-lighter);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.sidebar {
  background: var(--gradient-sidebar);
  box-shadow: var(--shadow-base);
  height: calc(100vh - 80px);
  overflow-y: auto;
}

.el-menu {
  border: none;
  background: transparent;
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  border-radius: var(--radius-base);
  margin: 4px 8px;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white;
  transform: translateX(4px);
}

.el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.15) !important;
  color: white;
  border-right: 3px solid var(--primary-color);
  font-weight: 600;
}

.el-main {
  background-color: var(--bg-page);
  padding: 20px;
  overflow-y: auto;
}
</style>