import axios from 'axios'

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios({
    method: 'post',
    url: '/api/file/uploadAvatar',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
    }
  })
}
