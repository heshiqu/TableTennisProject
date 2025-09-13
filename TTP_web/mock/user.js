
const tokens = {
  admin: {
    token: 'admin-token'
  },
  student: {
    token: 'student-token'
  },
  coach: {
    token: 'coach-token'
  }
}

const users = {
  'admin-token': {
    id: 1,
    username: 'admin',
    realName: '管理员',
    gender: 'MALE',
    age: 30,
    phone: '13800138000',
    email: 'admin@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: '5a4717f8-9803-4cb4-9f60-ddb143fa099a.jpg',
    role: 'ADMIN',
    userType: 'ADMIN'
  },
  'student-token': {
    id: 2,
    username: 'student',
    realName: '学生用户',
    gender: 'FEMALE',
    age: 20,
    phone: '13800138001',
    email: 'student@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: '', // 空字符串，将使用默认头像
    role: 'STUDENT',
    userType: 'STUDENT'
  },
  'coach-token': {
    id: 3,
    username: 'coach',
    realName: '教练用户',
    gender: 'MALE',
    age: 35,
    phone: '13800138002',
    email: 'coach@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: null, // null值，将使用默认头像
    role: 'COACH',
    userType: 'COACH'
  }
}

// 校区mock数据
const campuses = [
  { id: 1, name: '北京校区', address: '北京市朝阳区' },
  { id: 2, name: '上海校区', address: '上海市浦东新区' },
  { id: 3, name: '广州校区', address: '广州市天河区' }
]

// 校区管理员mock数据
const campusAdmins = [
  {
    id: 1,
    realName: '张三',
    username: 'admin_zhang',
    phone: '13800138001',
    email: 'zhang@example.com',
    campusId: 1,
    campusName: '北京校区',
    status: 'ACTIVE',
    createTime: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    realName: '李四',
    username: 'admin_li',
    phone: '13800138002',
    email: 'li@example.com',
    campusId: 2,
    campusName: '上海校区',
    status: 'ACTIVE',
    createTime: '2024-01-16 09:30:00'
  },
  {
    id: 3,
    realName: '王五',
    username: 'admin_wang',
    phone: '13800138003',
    email: 'wang@example.com',
    campusId: 1,
    campusName: '北京校区',
    status: 'INACTIVE',
    createTime: '2024-01-17 14:20:00'
  }
]

module.exports = [
  // user login
  {
    url: '/api/auth/login',
    type: 'post',
    response: config => {
      const { username } = config.body
      const token = tokens[username]

      // mock error
      if (!token) {
        return {
          code: 60204,
          message: '用户名或密码错误'
        }
      }

      return {
        code: 200,
        data: {
          token: token,
          userType: users[token].userType
        }
      }
    }
  },

  // get current user info
  {
    url: '/api/auth/me',
    type: 'get',
    response: config => {
      const token = config.headers.authorization?.replace('Bearer ', '')
      const info = users[token]

      // mock error
      if (!info) {
        return {
          code: 50008,
          message: '获取用户信息失败，请重新登录'
        }
      }

      return {
        code: 200,
        data: info
      }
    }
  },

  // 头像文件服务 - 直接返回文件
  {
    url: '/api/uploads/avatars/.*',
    type: 'get',
    response: () => {
      // 返回默认头像的base64数据
      return {
        code: 200,
        data: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='
      }
    }
  },

  // 头像上传接口
  {
    url: '/api/upload/avatar',
    type: 'post',
    response: () => {
      // 模拟上传成功，返回相对路径
      const filename = 'mock-avatar-' + Date.now() + '.jpg'
      return {
        code: 200,
        message: '头像上传成功',
        data: '/uploads/avatars/' + filename
      }
    }
  },"explanation":"简化用户信息响应，添加头像文件服务mock"}

  // get user role
  {
    url: '/api/auth/check-role',
    type: 'get',
    response: config => {
      const token = config.headers.authorization?.replace('Bearer ', '')
      const info = users[token]

      if (!info) {
        return {
          code: 50008,
          message: '获取用户角色失败'
        }
      }

      return {
        code: 200,
        data: info.role
      }
    }
  },

  // user logout
  {
    url: '/vue-admin-template/user/logout',
    type: 'post',
    response: _ => {
      return {
        code: 20000,
        data: 'success'
      }
    }
  },
  
  // 获取校区管理员分页列表
  {
    url: '/api/users/admins/page',
    type: 'get',
    response: config => {
      const { page = 0, size = 20, name, username, campusId, status } = config.query
      
      let filteredData = campusAdmins
      
      // 按姓名筛选
      if (name) {
        filteredData = filteredData.filter(admin => 
          admin.realName.includes(name)
        )
      }
      
      // 按用户名筛选
      if (username) {
        filteredData = filteredData.filter(admin => 
          admin.username.includes(username)
        )
      }
      
      // 按校区筛选
      if (campusId) {
        filteredData = filteredData.filter(admin => admin.campusId === parseInt(campusId))
      }
      
      // 按状态筛选
      if (status) {
        filteredData = filteredData.filter(admin => admin.status === status)
      }
      
      const total = filteredData.length
      const startIndex = parseInt(page) * parseInt(size)
      const endIndex = startIndex + parseInt(size)
      const records = filteredData.slice(startIndex, endIndex)
      
      return {
        code: 200,
        message: '获取成功',
        data: {
          records,
          total,
          page: parseInt(page),
          size: parseInt(size)
        }
      }
    }
  },
  
  // 创建校区管理员
  {
    url: '/api/users/admins',
    type: 'post',
    response: _ => {
      return {
        code: 200,
        message: '创建成功',
        data: null
      }
    }
  },
  
  // 更新校区管理员
  {
    url: '/api/users/admins/[0-9]+',
    type: 'put',
    response: _ => {
      return {
        code: 200,
        message: '更新成功',
        data: null
      }
    }
  },
  
  // 切换管理员状态
  {
    url: '/api/users/admins/[0-9]+/status',
    type: 'patch',
    response: _ => {
      return {
        code: 200,
        message: '状态更新成功',
        data: null
      }
    }
  },
  
  // 重置管理员密码
  {
    url: '/api/users/admins/[0-9]+/password/reset',
    type: 'post',
    response: _ => {
      return {
        code: 200,
        message: '密码重置成功',
        data: null
      }
    }
  },
  
  // 获取校区列表
  {
    url: '/api/campuses',
    type: 'get',
    response: _ => {
      return {
        code: 200,
        message: '获取成功',
        data: {
          records: campuses,
          total: campuses.length
        }
      }
    }
  }
]
