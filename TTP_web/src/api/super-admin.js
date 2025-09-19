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

// 获取系统日志（超级管理员专用）
export function getSystemLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 导出系统日志
export function exportSystemLogs(params) {
  return request({
    url: '/admin/logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取所有校区
export function getCampuses(params) {
  return request({
    url: '/api/campuses',
    method: 'get',
    params
  })
}

// 获取所有学员（超级管理员）
export function getAllStudents(params) {
  return request({
    url: '/api/students/all',
    method: 'get',
    params
  })
}

// 获取所有教练（超级管理员）
export function getAllCoaches(params) {
  return request({
    url: '/api/coaches/all',
    method: 'get',
    params: params || undefined
  })
}

// 校区管理员相关API
export function getCampusAdminsPage(params) {
  const queryParams = {
    page: params.page - 1,
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

export function createCampusAdmin(data) {
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

export function toggleAdminStatus(adminId) {
  return request({
    url: `/api/users/admins/${adminId}/status`,
    method: 'put'
  })
}

export function resetAdminPassword(adminId) {
  return request({
    url: `/api/users/admins/${adminId}/password/reset`,
    method: 'put'
  })
}

// 课程管理相关API（超级管理员）
export function getAllCourses() {
  return request({
    url: '/api/courses/all',
    method: 'get'
  })
}

export function adminConfirmCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/admin-confirm`,
    method: 'put'
  })
}

export function adminCancelCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/admin-cancel`,
    method: 'put'
  })
}

// 比赛管理相关API（超级管理员）
export function getAllTournaments(params) {
  const queryParams = {
    page: params.page - 1,
    size: params.limit,
    name: params.name,
    campusId: params.campusId,
    status: params.status,
    startDate: params.startDate,
    endDate: params.endDate
  }
  return request({
    url: '/api/tournaments/all',
    method: 'get',
    params: queryParams
  })
}

export function createTournament(data) {
  return request({
    url: '/api/tournaments',
    method: 'post',
    data
  })
}

export function updateTournament(id, data) {
  return request({
    url: `/api/tournaments/${id}`,
    method: 'put',
    data
  })
}

export function deleteTournament(id) {
  return request({
    url: `/api/tournaments/${id}`,
    method: 'delete'
  })
}

export function adminCancelTournament(tournamentId) {
  return request({
    url: `/api/tournaments/${tournamentId}/admin-cancel`,
    method: 'put'
  })
}

// 财务管理相关API（超级管理员）
export function getAllFinanceStats(params) {
  const queryParams = {
    campusId: params.campusId,
    startDate: params.startDate,
    endDate: params.endDate,
    type: params.type
  }
  return request({
    url: '/api/finance/all/stats',
    method: 'get',
    params: queryParams
  })
}

export function getAllFinanceDetails(params) {
  const queryParams = {
    page: params.page - 1,
    size: params.limit,
    campusId: params.campusId,
    startDate: params.startDate,
    endDate: params.endDate,
    type: params.type,
    keyword: params.keyword
  }
  return request({
    url: '/api/finance/all/details',
    method: 'get',
    params: queryParams
  })
}

export function exportFinanceData(params) {
  return request({
    url: '/api/finance/all/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 教练管理相关API
export function rejectCoach(data) {
  return request({
    url: '/api/coaches/reject',
    method: 'post',
    data
  })
}

export function resetCoachPassword(coachId) {
  return request({
    url: `/api/coaches/${coachId}/password/reset`,
    method: 'put'
  })
}

export function toggleCoachStatus(coachId) {
  return request({
    url: `/api/coaches/${coachId}/status/toggle`,
    method: 'put'
  })
}

// 学员管理相关API
export function rechargeStudent(data) {
  return request({
    url: '/api/students/recharge',
    method: 'post',
    data
  })
}

export function resetStudentPassword(studentId) {
  return request({
    url: `/api/students/${studentId}/password/reset`,
    method: 'put'
  })
}

// 比赛管理相关API
export function getTournamentParticipants(tournamentId) {
  return request({
    url: `/api/tournaments/${tournamentId}/participants`,
    method: 'get'
  })
}

export function removeParticipant(tournamentId, participantId) {
  return request({
    url: `/api/tournaments/${tournamentId}/participants/${participantId}`,
    method: 'delete'
  })
}

// 系统管理相关API
export function processTransaction(data) {
  return request({
    url: '/api/finance/transactions',
    method: 'post',
    data
  })
}

// 许可证管理相关API
export function getLicenses(params) {
  return request({
    url: '/api/licenses',
    method: 'get',
    params
  })
}

export function renewLicense(licenseId) {
  return request({
    url: `/api/licenses/${licenseId}/renew`,
    method: 'put'
  })
}

export function revokeLicense(licenseId) {
  return request({
    url: `/api/licenses/${licenseId}/revoke`,
    method: 'put'
  })
}