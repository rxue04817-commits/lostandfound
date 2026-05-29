
import axios from 'axios'
import { API_ENDPOINTS } from './apiConfig'
import { setToken,setUserInfo } from '@/utils/auth'
import request from './request'

export async function login(loginData) {
  try {
    const response = await axios.post(API_ENDPOINTS.LOGIN, loginData)
    if (response.data.code === 1) {
      // 保存token到 Vuex
      setToken(response.data.data.token)
      // 保存用户信息
      setUserInfo(response.data.data.userInfoVO)
      return {
        success: true,
        data: response.data,
        userInfo: response.data.data.userInfoVO
      }
    } else {
      return { success: false, message: response.data.message }
    }
  } catch (error) {
    return { success: false, message: '登录请求失败' }
  }
}

// 用户注册
export async function register(registerData) {
  try {
    const response = await axios.post(API_ENDPOINTS.REGISTER, registerData)
    if (response.data.code === 1) {

      return { success: true, username: response.data.data }
    } else {
      return { success: false, message: response.data.msg }
    }
  } catch (error) {
    return { success: false, message: '注册请求失败' }
  }
}
export async function getUserById(id) {
  try {
    const response = await request.get(`/users/${id}`)
    console.log(response)
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return { 
      success: false, 
      message: error.response?.data?.message || '获取用户信息失败' 
    }
  }
}
// src/api/userApi.js
export async function uploadAvatar(avatarFile) {
  try {
    const formData = new FormData()
    formData.append('avatar', avatarFile)
    
    const response = await request.post('/users/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return { 
      success: false, 
      message: error.response?.data?.message || '头像上传失败' 
    }
  }
}
export async function updateUserInfo(userInfo) {
  try {
    const response = await request.put('/users/profile', userInfo)
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return { 
      success: false, 
      message: error.response?.data?.message || '更新失败' 
    }
  }
}