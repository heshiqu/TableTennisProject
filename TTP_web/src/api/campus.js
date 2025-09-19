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

// 获取当前校区信息
export function getCampusInfo() {
  return request({
    url: '/api/campuses/current',
    method: 'get'
  })
}

// 更新校区信息
export function updateCampusInfo(data) {
  return request({
    url: '/api/campuses/current',
    method: 'put',
    data
  })
}

// 获取校区总数
export function getTotalCampusCount() {
  return axios({
    url: '/api/campuses/count',
    method: 'get',
    baseURL: process.env.VUE_APP_BASE_API
  }).then(response => {
    // 响应格式：{ code: 200, message: "获取成功", data: 3 }
    if (response.data && response.data.code === 200) {
      return response.data.data
    }
    return 0
  }).catch(error => {
    console.error('获取校区总数失败:', error)
    return 0
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

// 获取校区仪表板数据
export function getCampusDashboard() {
  return request({
    url: '/api/campuses/dashboard',
    method: 'get'
  })
}

// 获取校区公告
export function getCampusAnnouncements() {
  return request({
    url: '/api/campuses/announcements',
    method: 'get'
  })
}

// 创建校区公告
export function createCampusAnnouncement(data) {
  return request({
    url: '/api/campuses/announcements',
    method: 'post',
    data
  })
}

// 获取待处理事项
export function getCampusPendingItems() {
  return request({
    url: '/api/campuses/pending-items',
    method: 'get'
  })
}

// 获取校区学员数量
export function getCampusStudentCount(campusId) {
  return request({
    url: `/api/users/count/students/campus/${campusId}`,
    method: 'get'
  })
}