import request from './request'

export default {
  async pay(data) {
    try {
      const response = await request.post('/donation/pay', data)
      console.log("response"+response)
      if (response.data.code === 1) {
        return {
          success: true,
          data: response.data.data
        }
      } else {
        return { success: false, message: response.data.msg }
      }
    } catch (error) {
      return { success: false, message: '发起支付失败' }
    }
  },

  getMyDonations(params) {
    return request.get('/donation/my', { params })
  },

  cancelDonation(outTradeNo) {
    return request.put(`/donation/cancel/${outTradeNo}`)
  },

  async repayDonation(outTradeNo) {
    try {
      const response = await request.post(`/donation/repay/${outTradeNo}`)
      if (response.data.code === 1) {
        return { success: true, data: response.data.data }
      } else {
        return { success: false, message: response.data.msg }
      }
    } catch (error) {
      return { success: false, message: '发起支付失败' }
    }
  },

  // 管理员接口
  getAdminDonations(params) {
    return request.get('/admin/donation/page', { params })
  },

  getDonationStatistics() {
    return request.get('/admin/donation/statistics')
  },

  exportDonations() {
    return request.get('/admin/donation/export', { responseType: 'blob' })
  }
}