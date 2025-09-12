
const tokens = {
  admin: {
    token: 'admin-token'
  },
  editor: {
    token: 'editor-token'
  }
}

const users = {
  'admin-token': {
    roles: ['admin'],
    introduction: 'I am a super administrator',
    avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
    name: 'Super Admin'
  },
  'editor-token': {
    roles: ['editor'],
    introduction: 'I am an editor',
    avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
    name: 'Normal Editor'
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
    url: '/vue-admin-template/user/login',
    type: 'post',
    response: config => {
      const { username } = config.body
      const token = tokens[username]

      // mock error
      if (!token) {
        return {
          code: 60204,
          message: 'Account and password are incorrect.'
        }
      }

      return {
        code: 20000,
        data: token
      }
    }
  },

  // get user info
  {
    url: '/vue-admin-template/user/info\.*',
    type: 'get',
    response: config => {
      const { token } = config.query
      const info = users[token]

      // mock error
      if (!info) {
        return {
          code: 50008,
          message: 'Login failed, unable to get user details.'
        }
      }

      return {
        code: 20000,
        data: info
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
