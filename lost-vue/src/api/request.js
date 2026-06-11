// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // 设置基础URL
  timeout: 10000 // 设置超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 检查是否需要跳过认证
    if (!config.noAuth) {
      // 从本地存储获取 token
      const token = localStorage.getItem('token')
      if (token) {
        // 根据后端期望设置头部名称
        config.headers['token'] = token
      } else {
        // 如果没有token且不是公开接口，可以选择阻止请求或继续（由后端返回401）
        // 这里选择继续，让后端返回401后统一处理
        console.warn('请求未携带token:', config.url)
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是blob类型（如文件下载），直接返回原始响应数据
    if (response.config.responseType === 'blob') {
      return response.data
    }
    return response
  },
  error => {
    // 统一处理错误
    if (error.response) {
      const status = error.response.status
      
      if (status === 401) {
        // Token 过期或无效
        console.warn('Token无效或已过期，跳转到登录页')
        
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        
        // 显示提示消息（避免重复提示）
        if (!window.isRedirectingToLogin) {
          window.isRedirectingToLogin = true
          ElMessage.warning('登录已过期，请重新登录')
          
          // 延迟跳转，避免在请求回调中立即跳转导致的问题
          setTimeout(() => {
            window.isRedirectingToLogin = false
            // 如果当前不在登录页，则跳转
            if (router.currentRoute.value.path !== '/login') {
              router.push('/login')
            }
          }, 500)
        }
      } else if (status === 403) {
        ElMessage.error('没有权限访问该资源')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      console.error('请求失败:', error.message)
    }
    
    return Promise.reject(error)
  }
)

export default request