import request from '@/utils/request'

// 支付管理API
export function createPayment(data) {
  return request({
    url: '/api/payments/create',
    method: 'post',
    data
  })
}

export function getPaymentStatus(orderId) {
  return request({
    url: `/api/payments/status/${orderId}`,
    method: 'get'
  })
}

export function getPaymentHistory(params) {
  return request({
    url: '/api/payments/history',
    method: 'get',
    params
  })
}

// 获取支付二维码
export function getPaymentQRCode(orderId, paymentMethod) {
  return request({
    url: `/api/payments/qrcode/${orderId}`,
    method: 'get',
    params: { paymentMethod }
  })
}

// 线下支付确认
export function confirmOfflinePayment(data) {
  return request({
    url: '/api/payments/offline-confirm',
    method: 'post',
    data
  })
}

// 退款申请
export function applyRefund(courseId) {
  return request({
    url: `/api/payments/refund/${courseId}`,
    method: 'post'
  })
}

// 创建支付订单（根据用户需求的接口）
export function createPaymentOrder(data) {
  return request({
    url: '/api/payments',
    method: 'post',
    data
  })
}

// 确认支付成功
export function confirmPaymentSuccess(orderId) {
  return request({
    url: `/api/payments/${orderId}/success`,
    method: 'post'
  })
}

// 确认支付失败
export function confirmPaymentFailure(orderId) {
  return request({
    url: `/api/payments/${orderId}/failure`,
    method: 'post'
  })
}