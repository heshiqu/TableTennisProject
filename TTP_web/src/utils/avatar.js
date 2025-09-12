/**
 * 头像URL工具函数
 */

/**
 * 获取完整的头像URL
 * @param {string|null|undefined} avatar - 头像文件名
 * @param {string} defaultAvatar - 默认头像路径
 * @returns {string} 完整的头像URL
 */
export function getAvatarUrl(avatar, defaultAvatar = '/uploads/avatars/default-avatar.png') {
  if (!avatar || avatar === '' || avatar === 'null' || avatar === 'undefined') {
    return defaultAvatar
  }
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http') || avatar.startsWith('data:')) {
    return avatar
  }
  
  // 如果已经是相对路径，直接返回
  if (avatar.startsWith('/uploads/avatars/')) {
    return avatar
  }
  
  // 如果已经是后端返回的完整路径（包含uploads/avatars），直接返回
  if (avatar.includes('uploads/avatars')) {
    return `/${avatar}`
  }
  
  // 添加前缀
  return `/uploads/avatars/${avatar}`
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
  if (baseUrl.includes('default-avatar.png') || baseUrl.startsWith('data:')) {
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