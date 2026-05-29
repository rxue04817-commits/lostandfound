// src/api/lostApi.js
import request from './request'

export async function getLostFoundList(params) {
  try {
    const response = await request.get('/lostfound', { params })
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data.data
      }
    } else {
      return { success: false, message: response.data.msg }
    }
  } catch (error) {
    return { success: false, message: '获取数据失败' }
  }
}

export async function getLostFoundDetail(id) {
  try {
    const response = await request.get(`/lostfound/${id}`)
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data.data
      }
    } else {
      return { success: false, message: response.data.msg }
    }
  } catch (error) {
    return { success: false, message: '获取详情失败' }
  }
}

export const postComment = (lostFoundId, content) => {
  return request.post('/comments', { content }, {
    params: {
      lostFoundId: lostFoundId
    }
  })
}

export async function uploadImages(images) {
  try {
    const formData = new FormData()
    images.forEach(image => {
      formData.append('images', image)
    })

    const response = await request.post('/lostfound/upload-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    return {
      success: true,
      data: response.data.data
    }
  } catch (error) {
    return {
      success: false,
      message: '图片上传失败'
    }
  }
}

export async function publishLostFound(data) {
  try {
    const response = await request.post('/lostfound', data)
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data.data,
        message: response.data.msg
      }
    } else {
      return {
        success: false,
        message: response.data.msg
      }
    }
  } catch (error) {
    return {
      success: false,
      message: '发布失败'
    }
  }
}
// 删除失物信息接口
export async function deleteLostFound(id) {
  try {
    const response = await request.delete(`/lostfound/delete/${id}`)
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return {
      success: false,
      message: '删除失败'
    }
  }
}

// 更新失物信息接口
export async function updateLostFound(id, data) {
  try {
    const response = await request.put(`/lostfound/${id}`, data)
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return {
      success: false,
      message: '更新失败'
    }
  }
}
// 查询自己发布的失物信息列表接口
export async function getUserLostFoundList(params) {
  try {
    const response = await request.get('/lostfound/user', { params })
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data.data
      }
    } else {
      return {
        success: false,
        message: response.data.msg
      }
    }
  } catch (error) {
    return {
      success: false,
      message: '获取数据失败'
    }
  }
}

// 更新失物状态接口
export async function updateLostFoundStatus(id, status) {
  try {
    const response = await request.put(`/lostfound/${id}/status`, { status }, {
      params: { status }
    })
    if (response.data.code === 1) {
      return {
        success: true,
        data: response.data
      }
    }
  } catch (error) {
    return {
      success: false,
      message: '状态更新失败'
    }
  }
}