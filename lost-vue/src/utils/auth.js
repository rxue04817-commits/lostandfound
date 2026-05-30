// src/utils/auth.js

import store from '@/store'

/**
 * 从 Vuex 或 localStorage 获取 token
 */
export function getToken() {
  // 优先从 Vuex 获取 token
  const vuexToken = store.getters.getToken
  if (vuexToken) {
    return vuexToken
  }
  
  // 如果 Vuex 中没有 token，从 localStorage 获取
  const localStorageToken = localStorage.getItem('token')
  if (localStorageToken) {
    // 将 localStorage 中的 token 设置到 Vuex 中
    store.dispatch('setToken', localStorageToken)
    return localStorageToken
  }
  
  return null
}

/**
 * 设置 token 到 Vuex 和 localStorage
 */
export function setToken(token) {
  // 通过 Vuex 设置 token
  store.dispatch('setToken', token)
}

/**
 * 移除 token
 */
export function removeToken() {
  // 通过 Vuex 移除 token
  store.dispatch('removeToken')
}

/**
 * 从 Vuex 或 localStorage 获取用户信息
 */
export function getUserInfo() {
  // 优先从 Vuex 获取用户信息
  const vuexUserInfo = store.getters.getUserInfo
  if (vuexUserInfo) {
    return vuexUserInfo
  }
  
  // 如果 Vuex 中没有用户信息，从 localStorage 获取
  const localStorageUserInfoStr = localStorage.getItem('userInfo')
  if (localStorageUserInfoStr) {
    try {
      const userInfo = JSON.parse(localStorageUserInfoStr)
      // 将 localStorage 中的用户信息设置到 Vuex 中
      store.dispatch('setUserInfo', userInfo)
      return userInfo
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }
  
  return null
}

/**
 * 设置用户信息到 Vuex 和 localStorage
 */
export function setUserInfo(userInfo) {
  // 通过 Vuex 设置用户信息
  store.dispatch('setUserInfo', userInfo)
}

/**
 * 清除用户信息
 */
export function removeUserInfo() {
  // 通过 Vuex 清除用户信息
  store.dispatch('removeUserInfo')
}

/**
 * 解析JWT token获取用户信息
 */
export function parseToken(token) {
  try {
    if (!token) return null
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Token解析失败:', error)
    return null
  }
}

/**
 * 获取用户ID
 */
export function getUserId() {
  const token = getToken()
  const payload = parseToken(token)
  return payload ? payload.userId : null
}

/**
 * 获取用户角色
 */
export function getUserRole() {
  const token = getToken()
  const payload = parseToken(token)
  return payload ? payload.role : null
}
// src/utils/auth.js
export function checkPermission(role) {
  const userRole = getUserInfo()?.role
  return userRole === role
}