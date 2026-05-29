// src/api/adminApi.js
import request from './request'

export async function getAllLostFound(params) {
  try {
    const response = await request.get('/admin/lostfound', { params })

    if (response.data.code === 1)
    {
        return {
        success: true,
        data: response.data.data
      }
    }
      
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.message || '获取数据失败'
    }
  }
}

export async function updateLostFoundStatus(id, status) {
  try {
    const response = await request.put(`/admin/lostfound/${id}/status`, { status })
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.message || '状态更新失败'
    }
  }
}