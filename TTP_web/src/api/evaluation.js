import request from '@/utils/request'

// 评价管理API
export function createEvaluation(data) {
  return request({
    url: '/api/evaluations',
    method: 'post',
    data
  })
}

export function getEvaluationList(params) {
  return request({
    url: '/api/evaluations',
    method: 'get',
    params
  })
}

export function getEvaluationDetail(id) {
  return request({
    url: `/api/evaluations/${id}`,
    method: 'get'
  })
}

// 获取待评价的课程
export function getPendingEvaluations() {
  return request({
    url: '/api/evaluations/pending',
    method: 'get'
  })
}

// 获取学员的评价历史
export function getStudentEvaluations(studentId) {
  return request({
    url: `/api/evaluations/student/${studentId}`,
    method: 'get'
  })
}

// 获取教练的评价历史
export function getCoachEvaluations(coachId) {
  return request({
    url: `/api/evaluations/coach/${coachId}`,
    method: 'get'
  })
}

// 获取课程的评价信息
export function getCourseEvaluations(courseId) {
  return request({
    url: `/api/evaluations/course/${courseId}`,
    method: 'get'
  })
}