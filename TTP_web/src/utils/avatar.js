/**
 * 头像URL工具函数
 */

/**
 * 处理头像上传响应，返回完整的头像URL
 * @param {Object|string} response - 上传响应
 * @param {string} baseApi - API基础地址
 * @returns {string} 完整的头像URL
 */
export function processAvatarResponse(response, baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080') {
  let avatarUrl = ''
  
  // 处理不同格式的响应
  if (typeof response === 'string') {
    avatarUrl = response
  } else if (response && response.data) {
    if (typeof response.data === 'string') {
      avatarUrl = response.data
    } else if (response.data.url) {
      avatarUrl = response.data.url
    } else {
      avatarUrl = response.data
    }
  }
  
  return formatAvatarUrl(avatarUrl, baseApi)
}

/**
 * 格式化头像URL为完整路径
 * @param {string} avatarUrl - 头像URL
 * @param {string} baseApi - API基础地址
 * @returns {string} 完整的头像URL
 */
export function formatAvatarUrl(avatarUrl, baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080') {
  if (!avatarUrl || avatarUrl === '' || avatarUrl === 'null' || avatarUrl === 'undefined') {
    return baseApi + '/uploads/avatars/default-avatar.jpg'
  }
  
  // 如果已经是完整URL，直接返回
  if (avatarUrl.startsWith('http') || avatarUrl.startsWith('data:')) {
    return avatarUrl
  }
  
  // 如果已经是相对路径，添加API前缀
  if (avatarUrl.startsWith('/')) {
    return baseApi + avatarUrl
  }
  
  // 如果是文件名，拼接完整路径
  return baseApi + '/uploads/avatars/' + avatarUrl
}

/**
 * 获取完整的头像URL（向后兼容）
 * @param {string|null|undefined} avatar - 头像文件名
 * @param {string} defaultAvatar - 默认头像路径
 * @returns {string} 完整的头像URL
 */
export function getAvatarUrl(avatar, defaultAvatar = null) {
  const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
  if (!defaultAvatar) {
    defaultAvatar = baseApi + '/uploads/avatars/default-avatar.jpg'
  }
  if (!avatar || avatar === '' || avatar === 'null' || avatar === 'undefined') {
    return defaultAvatar
  }
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http') || avatar.startsWith('data:')) {
    return avatar
  }
  
  // 如果已经是相对路径，添加API前缀
  if (avatar.startsWith('/uploads/avatars/')) {
    return baseApi + avatar
  }
  
  // 如果已经是后端返回的完整路径（包含uploads/avatars），添加API前缀
  if (avatar.includes('uploads/avatars')) {
    if (avatar.startsWith('/')) {
      return baseApi + avatar
    } else {
      return baseApi + '/' + avatar
    }
  }
  
  // 添加完整前缀
  return baseApi + '/uploads/avatars/' + avatar
}

/**
 * 获取头像URL（带尺寸参数）
 * @param {string|null|undefined} avatar - 头像文件名
 * @param {number} width - 宽度
 * @param {number} height - 高度
 * @returns {string} 带尺寸参数的头像URL
 */
export function getAvatarUrlWithSize(avatar, width = 80, height = 80) {
  const baseUrl = getAvatarUrl(avatar)
  
  // 如果是默认头像或data URL，不添加尺寸参数
  if (baseUrl.includes('default-avatar.jpg') || baseUrl.startsWith('data:')) {
    return baseUrl
  }
  
  return `${baseUrl}?imageView2/1/w/${width}/h/${height}`
}

/**
 * 检查是否有有效头像
 * @param {string|null|undefined} avatar - 头像文件名
 * @returns {boolean} 是否有有效头像
 */
export function hasValidAvatar(avatar) {
  return !!(avatar && avatar !== '' && avatar !== 'null' && avatar !== 'undefined')
}