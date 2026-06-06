import request from './request'

export default {
  submitClaim(data) {
    return request.post('/claims', data)
  },
  getMyClaims(params) {
    return request.get('/claims/my', { params })
  },
  getReceivedClaims(params) {
    return request.get('/claims/received', { params })
  },
  getClaimById(id) {
    return request.get(`/claims/${id}`)
  },
  approveClaim(id) {
    return request.put(`/claims/${id}/approve`)
  },
  rejectClaim(id, data) {
    return request.put(`/claims/${id}/reject`, data)
  },
  completeClaim(id) {
    return request.put(`/claims/${id}/complete`)
  }
}