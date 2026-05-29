// src/store/index.js
import { createStore } from 'vuex'

export default createStore({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: null
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    REMOVE_TOKEN(state) {
      state.token = ''
      localStorage.removeItem('token')
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    REMOVE_USER_INFO(state) {
      state.userInfo = null
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    setToken({ commit }, token) {
      commit('SET_TOKEN', token)
    },
    removeToken({ commit }) {
      commit('REMOVE_TOKEN')
    },
    setUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
    },
    removeUserInfo({ commit }) {
      commit('REMOVE_USER_INFO')
    }
  },
  getters: {
    getToken: state => state.token,
    getUserInfo: state => state.userInfo
  }
})