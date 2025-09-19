import request from '@/utils/request'

// 课程管理API
export function getCourseSchedule(params) {
  return request({
    url: '/api/courses/schedule',
    method: 'get',
    params
  })
}

export function getCoachSchedule(coachId, date) {
  return request({
    url: `/api/courses/coach-schedule/${coachId}`,
    method: 'get',
    params: { date }
  })
}

export function bookCourse(data) {
  return request({
    url: '/api/courses/book',
    method: 'post',
    data
  })
}

export function cancelCourse(courseId, reason, cancelledBy) {
  return request({
    url: `/api/courses/${courseId}/cancel`,
    method: 'post',
    params: { 
      reason: reason,
      cancelledBy: cancelledBy
    }
  })
}

export function confirmCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/confirm`,
    method: 'post'
  })
}

export function rejectCourse(courseId, reason) {
  return request({
    url: `/api/courses/${courseId}/reject`,
    method: 'post',
    data: { reason }
  })
}

// 获取可用的球台
export function getAvailableTables(date, timeSlot) {
  return request({
    url: '/api/courses/available-tables',
    method: 'get',
    params: { date, timeSlot }
  })
}

// 获取学员的课程列表
export function getStudentCourses(params) {
  return request({
    url: '/api/courses/student',
    method: 'get',
    params
  })
}

// 获取教练的课程列表
export function getCoachCourses(params) {
  return request({
    url: '/api/courses/coach',
    method: 'get',
    params
  })
}

// 获取取消次数统计
export function getCancellationStats() {
  return request({
    url: '/api/courses/cancellation-stats',
    method: 'get'
  })
}

// 获取课程列表（管理员/校区管理员）
export function getCourseList(params) {
  // 将前端page(从1开始)转换为后端page(从0开始)
  const queryParams = {
    ...params,
    page: params.page - 1,
    size: params.limit
  }
  return request({
    url: '/api/courses/page',
    method: 'get',
    params: queryParams
  })
}

// 获取学生的课程列表（按日期范围）
export function getStudentCoursesByDateRange(studentId, start, end) {
  // 确保时间格式为 yyyy-MM-dd HH:mm:ss
  const formatDate = (date) => {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
  
  return request({
    url: `/api/courses/student/${studentId}/date-range`,
    method: 'get',
    params: { 
      start: formatDate(start), 
      end: formatDate(end) 
    }
  })
}

// 获取教练的课程列表（按日期范围）
export function getCoachCoursesByDateRange(coachId, start, end) {
  // 确保时间格式为 yyyy-MM-dd HH:mm:ss
  const formatDate = (date) => {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
  
  return request({
    url: `/api/courses/coach/${coachId}/date-range`,
    method: 'get',
    params: { 
      start: formatDate(start), 
      end: formatDate(end) 
    }
  })
}

// 校区管理员确认课程（管理员权限）
export function adminConfirmCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/admin-confirm`,
    method: 'post'
  })
}

// 校区管理员取消课程（管理员权限）
export function adminCancelCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/admin-cancel`,
    method: 'post'
  })
}

// 获取教练的待确认预约
export function getCoachPendingBookings(coachId, status) {
  return request({
    url: `/api/courses/coach/${coachId}/status/${status}`,
    method: 'get'
  })
}

// 获取教练本月收入
export function getCoachMonthlyIncome(coachId) {
  return request({
    url: `/api/courses/coach/${coachId}/monthly-income`,
    method: 'get'
  })
}

// 获取学生本月取消次数
export function getStudentMonthlyCancelCount(studentId) {
  return request({
    url: `/api/students/${studentId}/cancel-count`,
    method: 'get'
  })
}

// 获取校区今日已确认课程数量
export function getCampusTodayConfirmedCount(campusId) {
  return request({
    url: `/api/courses/campus/${campusId}/today/confirmed/count`,
    method: 'get'
  })
}

// 获取校区管理员的课程列表
export function getCampusCourses(campusId, params) {
  // 复制参数，但移除分页相关参数
  const queryParams = { ...params }
  if (queryParams.page || queryParams.limit) {
    delete queryParams.page
    delete queryParams.limit
  }
  
  return request({
    url: `/api/courses/campus/${campusId}`,
    method: 'get',
    params: queryParams
  })
}