import request from '@/utils/request'
import axios from 'axios'

// 教练管理API
export function getCoachList(params) {
  return request({
    url: '/api/users/type/COACH/page',
    method: 'get',
    params
  })
}

export function getCoachDetail(id) {
  return request({
    url: `/api/coaches/${id}`,
    method: 'get'
  })
}

export function searchCoaches(params) {
  return request({
    url: '/api/coaches/search',
    method: 'get',
    params
  })
}

export function updateCoach(id, data) {
  return request({
    url: `/api/coaches/${id}`,
    method: 'put',
    data
  })
}

export function uploadCoachPhoto(data) {
  return request({
    url: '/api/coaches/photo',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 教练审核相关
export function approveCoach(id, data) {
  return request({
    url: `/api/coaches/${id}/approve`,
    method: 'post',
    data
  })
}

export function rejectCoach(id, data) {
  return request({
    url: `/api/coaches/${id}/reject`,
    method: 'post',
    data
  })
}

// 获取教练的学员列表
export function getCoachStudents(coachId) {
  return request({
    url: `/api/coaches/${coachId}/students`,
    method: 'get'
  })
}

// 获取教练学员数量
export function getCoachStudentCount(coachId) {
  return request({
    url: `/api/coach-student-relations/coach/${coachId}/students/count`,
    method: 'get'
  })
}

// 获取已批准学员列表
export function getCoachApprovedStudents(coachId) {
  return request({
    url: `/api/coach-student-relations/coach/${coachId}/status/APPROVED`,
    method: 'get'
  })
}

// 获取用户详细信息
export function getUserDetail(userId) {
  return request({
    url: `/api/users/${userId}`,
    method: 'get'
  })
}

// 获取教练总数 - 使用原生axios绕过拦截器，直接返回数字
export function getTotalCoachCount() {
  // 使用原生axios避免request.js的响应包装
  return axios({
    url: '/api/coaches/count',
    method: 'get',
    baseURL: process.env.VUE_APP_BASE_API
  }).then(response => {
    return response.data
  })
}

// 获取指定校区的教练数量
export function getCoachCountByCampus(campusId) {
  return request({
    url: `/api/coaches/count/campus/${campusId}`,
    method: 'get'
  })
}

// 获取教练个人信息
export function getCoachInfo(coachId) {
  return request({
    url: `/api/coaches/${coachId}`,
    method: 'get'
  })
}

// 更新教练个人信息
export function updateCoachInfo(coachId, data) {
  return request({
    url: `/api/coaches/${coachId}`,
    method: 'put',
    data
  })
}

// 教练修改密码
export function updatePassword(coachId, currentPassword, newPassword) {
  return request({
    url: `/api/coaches/${coachId}/password`,
    method: 'put',
    data: {
      currentPassword,
      newPassword
    }
  })
}

// 获取学生申请列表
export function getStudentApplications(coachId) {
  return request({
    url: `/api/coach-student-relations/coach/${coachId}/status/PENDING`,
    method: 'get'
  })
}

// 同意学生申请
export function approveStudentApplication(relationId, coachId) {
  return request({
    url: `/api/coach-student-relations/${relationId}/approve`,
    method: 'post',
    params: { coachId }
  })
}

// 拒绝学生申请
export function rejectStudentApplication(relationId, coachId) {
  return request({
    url: `/api/coach-student-relations/${relationId}/reject`,
    method: 'post',
    params: { coachId }
  })
}