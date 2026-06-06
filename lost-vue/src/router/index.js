import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/UserLogin.vue'
import LayoutView from '../views/layout/LayoutView.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import { getToken, getUserInfo } from '@/utils/auth'

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
      }
    ]
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

  if (to.path === '/login') {
    return token ? next('/layout') : next()
  }

  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!token) {
      return next('/login')
    }
  }

  if (to.matched.some((record) => record.meta.requiresAdmin)) {
    if (!userInfo || userInfo.role !== 1) {
      return next('/publish')
    }
  }

  next()
})

export default router
