import request from '@/utils/request'

// 学员管理API
export function getStudentList(params) {
  return request({
    url: '/api/users/type/STUDENT/page',
    method: 'get',
    params
  })
}

export function getStudentDetail(id) {
  return request({
    url: `/api/students/${id}`,
    method: 'get'
  })
}

export function updateStudent(id, data) {
  return request({
    url: `/api/students/${id}`,
    method: 'put',
    data
  })
}

export function searchStudents(params) {
  return request({
    url: '/api/students/search',
    method: 'get',
    params
  })
}

// 学员选择教练
export function selectCoach(coachId) {
  return request({
    url: '/api/students/select-coach',
    method: 'post',
    data: { coachId }
  })
}

// 获取学员的教练列表
export function getStudentCoaches(studentId) {
  return request({
    url: `/api/students/${studentId}/coaches`,
    method: 'get'
  })
}

// 更换教练
export function changeCoach(oldCoachId, newCoachId) {
  return request({
    url: '/api/students/change-coach',
    method: 'post',
    data: { oldCoachId, newCoachId }
  })
}

// 学员充值
export function rechargeBalance(amount, paymentMethod) {
  return request({
    url: '/api/students/recharge',
    method: 'post',
    data: { amount, paymentMethod }
  })
}

// 获取学员余额
export function getBalance(studentId) {
  return request({
    url: `/api/students/${studentId}/balance`,
    method: 'get'
  })
}

// 获取学员教练数量
export function getStudentCoachCount(studentId) {
  return request({
    url: `/api/coach-student-relations/student/${studentId}/coaches/count`,
    method: 'get'
  })
}

// 获取学员指定日期范围的课程
export function getStudentCoursesByDateRange(studentId, startDate, endDate) {
  return request({
    url: `/api/courses/student/${studentId}/date-range`,
    method: 'get',
    params: {
      start: startDate,
      end: endDate
    }
  })
}

// 获取用户详细信息
export function getUserInfo(userId) {
  return request({
    url: `/api/users/${userId}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(userId, data) {
  return request({
    url: `/api/users/${userId}`,
    method: 'put',
    data
  })
}

// 更新用户密码
export function updatePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/api/users/${userId}/password?oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`,
    method: 'patch'
  })
}