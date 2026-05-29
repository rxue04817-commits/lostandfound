import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/UserLogin.vue'

import LayoutView from '../views/layout/LayoutView.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
const routes = [
  {
    path: '/',
    redirect: '/login'  // 默认重定向到登录页
  },
  {
    path: '/layout',
    name: 'layout',
    component: LayoutView,
    redirect: '/publish',
    children: [
      {
        path: '/publish',
        component: () => import('@/views/lost/PublishView.vue'),
        meta: { title: '发布失物' }
      },
      {
        path: '/my-posts',
        component: () => import('@/views/lost/MyPostsView.vue'),
        meta: { title: '我的发布' }
      },
      {
        path: '/all-posts',
        component: () => import('@/views/lost/LostViews.vue'),
        meta: { title: '我的发布' }
      },
      {
        path: '/profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: '/statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计' }
      }, {
        path: '/admin',
        name: 'Admin',
        component: AdminDashboard,
        meta: { requiresAuth: true, role: 'admin' }
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

export default router
