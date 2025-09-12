import request from '@/utils/request'
import axios from 'axios'

// 校区管理API
export function getCampusList(params) {
  // 将前端的limit转换为Spring Data的size参数
  const springParams = {
    page: params.page - 1, // Spring Data页码从0开始
    size: params.limit,
    name: params.name
  }
  return request({
    url: '/api/campuses/page',
    method: 'get',
    params: springParams
  })
}

export function getCampusDetail(id) {
  return request({
    url: `/api/campuses/${id}`,
    method: 'get'
  })
}

export function createCampus(data) {
  return request({
    url: '/api/campuses',
    method: 'post',
    data
  })
}

export function updateCampus(id, data) {
  return request({
    url: `/api/campuses/${id}`,
    method: 'put',
    data
  })
}

export function deleteCampus(id) {
  return request({
    url: `/api/campuses/${id}`,
    method: 'delete'
  })
}

// 获取当前用户所在校区
export function getUserCampus() {
  return request({
    url: '/api/campuses/user',
    method: 'get'
  })
}

// 获取校区负责人列表
export function getManagers(params) {
  return request({
    url: '/api/campuses/managers',
    method: 'get',
    params
  })
}

// 获取校区统计信息
export function getCampusStats(id) {
  return request({
    url: `/api/campuses/${id}/stats`,
    method: 'get'
  })
}

// 获取校区总数
export function getTotalCampusCount() {
  return axios({
    url: '/api/campuses/count',
    method: 'get',
    baseURL: process.env.VUE_APP_BASE_API
  }).then(response => {
    return response.data
  }).catch(error => {
    console.error('获取校区总数失败:', error)
    throw error
  })
}

// 分配校区管理员
export function assignCampusManager(data) {
  return request({
    url: '/api/campuses/assign-manager',
    method: 'post',
    data
  })
}