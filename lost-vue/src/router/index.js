import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/UserLogin.vue'
import LayoutView from '../views/layout/LayoutView.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import { getToken, getUserInfo } from '@/utils/auth'
import { checkPermission } from '@/config/permission'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/layout',
    name: 'layout',
    component: LayoutView,
    redirect: '/publish',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/publish',
        component: () => import('@/views/lost/PublishView.vue'),
        meta: { title: '发布信息', requiresAuth: true }
      },
      {
        path: '/my-posts',
        component: () => import('@/views/lost/MyPostsView.vue'),
        meta: { title: '我的发布', requiresAuth: true }
      },
      {
        path: '/all-posts',
        component: () => import('@/views/lost/LostViews.vue'),
        meta: { title: '信息浏览', requiresAuth: true }
      },
      {
        path: '/lost/detail/:id',
        component: () => import('@/views/lost/LostDetailView.vue'),
        meta: { title: '物品详情', requiresAuth: true }
      },
      {
        path: '/profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      },
      {
        path: '/my-claims',
        name: 'MyClaims',
        component: () => import('@/views/lost/MyClaimsView.vue')
      },
      {
        path: '/claim-manage',
        name: 'ClaimManage',
        component: () => import('@/views/lost/ClaimManageView.vue')
      },
      {
        path: '/my-donations',
        name: 'MyDonations',
        component: () => import('@/views/lost/MyDonationsView.vue'),
        meta: { title: '我的打赏', requiresAuth: true }
      },
      {
        path: '/statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', requiresAuth: true }
      },
      {
        path: '/admin',
        name: 'Admin',
        component: AdminDashboard,
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUserManagement.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: '/admin/donation-statistics',
        name: 'DonationStatistics',
        component: () => import('@/views/admin/DonationStatistics.vue'),
        meta: { requiresAuth: true, requiresSuperAdmin: true }
      },
      {
        path: '/admin/donation-orders',
        name: 'DonationOrders',
        component: () => import('@/views/admin/DonationOrders.vue'),
        meta: { requiresAuth: true, requiresSuperAdmin: true }
      }
    ]
  },
  {
    path: '/alipay-return',
    name: 'AlipayReturn',
    component: () => import('@/views/lost/AlipayReturnView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: Login
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const userInfo = getUserInfo()

  // 如果访问登录页，且已登录则跳转到主页
  if (to.path === '/login') {
    return token ? next('/layout') : next()
  }

  // 检查是否需要认证
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!token || !userInfo) {
      console.warn('未登录用户尝试访问需要认证的页面:', to.path)
      return next('/login')
    }
  }

  // 检查角色权限（使用新的权限配置）
  if (userInfo && to.path !== '/layout') {
    const hasPermission = checkPermission(userInfo.role, to.path)
    if (!hasPermission) {
      console.warn('用户权限不足，尝试访问:', to.path, '用户角色:', userInfo.role)
      // 没有权限，重定向到默认页面
      return next('/publish')
    }
  }

  next()
})

export default router
