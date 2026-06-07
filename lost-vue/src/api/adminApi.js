// src/api/adminApi.js
import request from './request'

export async function getAllLostFound(params) {
  try {
    const response = await request.get('/admin/lostfound', { params })
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data.data
      }
    }
    return { success: false, message: response.data.msg || '获取数据失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '获取数据失败'
    }
  }
}

export async function updateLostFoundStatus(id, status) {
  try {
    const response = await request.put(`/lostfound/${id}/status`, null, {
      params: { status }
    })
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '状态更新失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '状态更新失败'
    }
  }
}

export async function getUserList(params) {
  try {
    const response = await request.get('/admin/users', { params })
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '获取用户列表失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '获取用户列表失败'
    }
  }
}

export async function getUserDetail(id) {
  try {
    const response = await request.get(`/admin/users/${id}`)
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '获取用户详情失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '获取用户详情失败'
    }
  }
}

export async function updateUserStatus(id, status) {
  try {
    const response = await request.put(`/admin/users/${id}/status`, { status })
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '状态更新失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '状态更新失败'
    }
  }
}

export async function updateUserRole(id, role) {
  try {
    const response = await request.put(`/admin/users/${id}/role`, { role })
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '角色更新失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '角色更新失败'
    }
  }
}

export async function getStats() {
  try {
    const response = await request.get('/admin/stats')
    if (response.data.code === 1) {
      return { success: true, data: response.data.data }
    }
    return { success: false, message: response.data.msg || '获取统计数据失败' }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.msg || '获取统计数据失败'
    }
  }
}
