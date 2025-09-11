import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

export function registerStudent(data) {
  return request({
    url: '/api/auth/register/student',
    method: 'post',
    data
  })
}

export function registerCoach(data) {
  return request({
    url: '/api/auth/register/coach',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/api/auth/me',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

export function refreshToken() {
  return request({
    url: '/api/auth/refresh-token',
    method: 'post'
  })
}

export function validateToken() {
  return request({
    url: '/api/auth/validate-token',
    method: 'get'
  })
}

export function checkAuthority(authority) {
  return request({
    url: '/api/auth/check-authority',
    method: 'get',
    params: { authority }
  })
}

export function checkRole() {
  return request({
    url: '/api/auth/check-role',
    method: 'get'
  })
}