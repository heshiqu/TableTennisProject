import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: '/api/auth/me',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 获取用户列表（管理员）
export function getUserList(params) {
  return request({
    url: '/api/users',
    method: 'get',
    params
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/api/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 更新用户信息
export function updateUserInfo(id, data) {
  return request({
    url: `/api/users/${id}`,
    method: 'put',
    data
  })
}

// 更新用户余额
export function updateUserBalance(userId, amount, type) {
  return request({
    url: `/api/users/${userId}/balance`,
    method: 'post',
    data: { amount, type }
  })
}

// 获取学员总数
export function getTotalStudentCount() {
  return request({
    url: '/api/users/count/students',
    method: 'get'
  }).then(response => {
    // 响应格式：{ code: 200, message: "获取成功", data: 6 }
    if (response && response.code === 200) {
      return response.data
    }
    return 0
  }).catch(error => {
    console.error('获取学员总数失败:', error)
    return 0
  })
}

// 获取校区管理员分页列表
export function getCampusAdminsPage(params) {
  return request({
    url: '/api/users/admins/page',
    method: 'get',
    params
  }).then(response => {
    // 响应格式：{ code: 200, message: "获取成功", data: { content: [...], totalElements: 0 } }
    if (response && response.code === 200) {
      return response.data
    }
    throw new Error('获取校区管理员列表失败')
  }).catch(error => {
    console.error('获取校区管理员列表失败:', error)
    throw error
  })
}

// 创建校区管理员
export function createCampusAdmin(data) {
  return request({
    url: '/api/users/admins',
    method: 'post',
    data
  })
}

// 更新校区管理员
export function updateCampusAdmin(data) {
  return request({
    url: `/api/users/admins/${data.id}`,
    method: 'put',
    data
  })
}

// 切换管理员状态
export function toggleAdminStatus(adminId) {
  return request({
    url: `/api/users/admins/${adminId}/status`,
    method: 'patch'
  })
}

// 重置管理员密码
export function resetAdminPassword(adminId) {
  return request({
    url: `/api/users/admins/${adminId}/password/reset`,
    method: 'post'
  })
}

// 更新用户密码（学生和教练通用）
export function updatePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/api/users/${userId}/password?oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`,
    method: 'patch'
  })
}

// 获取校区列表（用于下拉选择）
export function getCampuses(params) {
  return request({
    url: '/api/campuses',
    method: 'get',
    params
  }).then(response => {
    // 兼容新的API格式
    if (response && response.code === 200) {
      return response.data
    }
    return response
  }).catch(error => {
    console.error('获取校区列表失败:', error)
    throw error
  })
}
