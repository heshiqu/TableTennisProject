import request from '@/utils/request'

/**
 * 上传头像
 * @param {File} file 头像文件
 * @returns {Promise} 返回头像URL
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}