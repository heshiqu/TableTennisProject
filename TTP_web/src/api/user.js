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
export function updateUserStatus(userId, status) {
  return request({
    url: `/api/users/${userId}/status`,
    method: 'patch',
    data: { status }
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
    return response.data
  }).catch(error => {
    console.error('获取学员总数失败:', error)
    throw error
  })
}

// 获取校区管理员分页列表
export function getCampusAdminsPage(params) {
  return request({
    url: '/api/users/admins/page',
    method: 'get',
    params
  }).then(response => {
    // 响应格式：{ code: 200, message: "获取成功", data: { records: [...], total: 0 } }
    return response.data
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

// 获取校区列表（用于下拉选择）
export function getCampuses(params) {
  return request({
    url: '/api/campuses',
    method: 'get',
    params
  }).then(response => {
    return response.data
  }).catch(error => {
    console.error('获取校区列表失败:', error)
    throw error
  })
}
