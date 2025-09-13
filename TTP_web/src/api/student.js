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

// 获取学员已完成的训练记录
export function getStudentCompletedCourses(studentId) {
  return request({
    url: `/api/courses/student/${studentId}/completed`,
    method: 'get'
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

// 获取学生所在校区的教练列表
export function getCampusCoachesForStudent(campusId) {
  return request({
    url: `/api/coaches/campus/${campusId}`,
    method: 'get'
  })
}

// 获取当前学生所在校区的教练列表
export function getCurrentStudentCampusCoaches() {
  return request({
    url: '/api/coaches/campus/current',
    method: 'get'
  })
}

// 获取用户交易记录
export function getUserPayments(userId, params) {
  return request({
    url: `/api/payments/user/${userId}`,
    method: 'get',
    params
  })
}

// 获取学生已批准的教练关系
export function getStudentApprovedCoaches(studentId) {
  return request({
    url: `/api/coach-student-relations/student/${studentId}/status/APPROVED`,
    method: 'get'
  })
}

// 获取教练详细信息
export function getCoachDetail(coachId) {
  return request({
    url: `/api/coaches/${coachId}`,
    method: 'get'
  })
}

// 获取我的教练（已批准状态）
export async function getMyApprovedCoaches() {
  // 这个方法将在组件中调用，通过Vuex获取用户ID
  throw new Error('请在组件中使用getStudentApprovedCoaches和getCoachDetail组合调用')
}