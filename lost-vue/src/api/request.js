// src/api/request.js
import axios from 'axios'

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
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器（可选）
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
    if (error.response?.status === 401) {
      // token 过期或无效，可以跳转到登录页
      localStorage.removeItem('token')
      // 可以添加路由跳转逻辑
    }
    return Promise.reject(error)
  }
)

export default request