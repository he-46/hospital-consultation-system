import request from './index'

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    method: 'post',
    url: '/file/uploadAvatar',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
