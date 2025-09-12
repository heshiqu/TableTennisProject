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