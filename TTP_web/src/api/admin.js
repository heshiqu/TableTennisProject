import request from '@/utils/request'

// 获取仪表盘数据
export function getDashboardData() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

// 获取系统公告
export function getNotices(params) {
  return request({
    url: '/admin/notices',
    method: 'get',
    params
  })
}

// 创建系统公告
export function createNotice(data) {
  return request({
    url: '/admin/notices',
    method: 'post',
    data
  })
}

// 更新系统公告
export function updateNotice(id, data) {
  return request({
    url: `/admin/notices/${id}`,
    method: 'put',
    data
  })
}

// 删除系统公告
export function deleteNotice(id) {
  return request({
    url: `/admin/notices/${id}`,
    method: 'delete'
  })
}

// 获取操作日志
export function getLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 获取系统设置
export function getSettings() {
  return request({
    url: '/admin/settings',
    method: 'get'
  })
}

// 更新系统设置
export function updateSettings(data) {
  return request({
    url: '/admin/settings',
    method: 'put',
    data
  })
}

// 获取系统统计
export function getStatistics(params) {
  return request({
    url: '/admin/statistics',
    method: 'get',
    params
  })
}

// 获取收入统计
export function getRevenueStats(params) {
  return request({
    url: '/admin/revenue-stats',
    method: 'get',
    params
  })
}

// 获取课程统计
export function getCourseStats(params) {
  return request({
    url: '/admin/course-stats',
    method: 'get',
    params
  })
}

// 获取用户增长统计
export function getUserGrowthStats(params) {
  return request({
    url: '/admin/user-growth-stats',
    method: 'get',
    params
  })
}

// 校区管理员相关API
export function getCampusAdminList(params) {
  // 使用新的API端点获取校区管理员列表
  const queryParams = {
    page: params.page - 1, // 后端页码从0开始
    size: params.limit,
    name: params.name,
    username: params.username,
    campusId: params.campusId,
    status: params.status
  }
  return request({
    url: '/api/users/type/CAMPUS_ADMIN/page',
    method: 'get',
    params: queryParams
  })
}

export function addCampusAdmin(data) {
  return request({
    url: '/api/users/admins',
    method: 'post',
    data
  })
}

export function updateCampusAdmin(data) {
  return request({
    url: `/api/users/admins/${data.id}`,
    method: 'put',
    data
  })
}

export function enableCampusAdmin(id) {
  return request({
    url: `/api/users/admins/${id}/enable`,
    method: 'put'
  })
}

export function disableCampusAdmin(id) {
  return request({
    url: `/api/users/admins/${id}/disable`,
    method: 'put'
  })
}

export function resetCampusAdminPassword(id) {
  return request({
    url: `/api/users/admins/${id}/reset-password`,
    method: 'put'
  })
}