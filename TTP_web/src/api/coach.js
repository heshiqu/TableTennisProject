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