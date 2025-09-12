import request from '@/utils/request'

// 系统管理API
export function getSystemLogs(params) {
  return request({
    url: '/api/system/logs',
    method: 'get',
    params
  })
}

export function getSystemStats() {
  return request({
    url: '/api/system/stats',
    method: 'get'
  })
}

// 获取系统消息
export function getSystemMessages(params) {
  return request({
    url: '/api/system/messages',
    method: 'get',
    params
  })
}

// 标记消息已读
export function markMessageRead(messageId) {
  return request({
    url: `/api/system/messages/${messageId}/read`,
    method: 'post'
  })
}

// 获取未读消息数量
export function getUnreadMessageCount() {
  return request({
    url: '/api/system/messages/unread-count',
    method: 'get'
  })
}

// 系统配置相关
export function getSystemConfig() {
  return request({
    url: '/api/system/config',
    method: 'get'
  })
}

export function updateSystemConfig(data) {
  return request({
    url: '/api/system/config',
    method: 'put',
    data
  })
}