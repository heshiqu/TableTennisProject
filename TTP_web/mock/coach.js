const Mock = require('mockjs')

// 教练mock数据
const coaches = [
  {
    id: 1,
    username: 'coach_zhang',
    realName: '张教练',
    gender: 'MALE',
    age: 35,
    phone: '13800138001',
    email: 'zhang@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: 'https://via.placeholder.com/150',
    maxStudents: 20,
    currentStudents: 12,
    experience: 8,
    awards: '国家二级运动员',
    specialties: ['正手攻球', '反手推挡', '发球技巧'],
    status: 'ACTIVE',
    createTime: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    username: 'coach_li',
    realName: '李教练',
    gender: 'FEMALE',
    age: 32,
    phone: '13800138002',
    email: 'li@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: 'https://via.placeholder.com/150',
    maxStudents: 15,
    currentStudents: 8,
    experience: 6,
    awards: '国家一级运动员',
    specialties: ['弧圈球', '削球', '快攻'],
    status: 'ACTIVE',
    createTime: '2024-01-16 09:30:00'
  },
  {
    id: 3,
    username: 'coach_wang',
    realName: '王教练',
    gender: 'MALE',
    age: 28,
    phone: '13800138003',
    email: 'wang@example.com',
    campusId: 2,
    campusName: '上海校区',
    avatar: 'https://via.placeholder.com/150',
    maxStudents: 18,
    currentStudents: 10,
    experience: 4,
    awards: '省级冠军',
    specialties: ['发球', '接发球', '战术指导'],
    status: 'ACTIVE',
    createTime: '2024-01-17 14:20:00'
  },
  {
    id: 4,
    username: 'coach_chen',
    realName: '陈教练',
    gender: 'MALE',
    age: 40,
    phone: '13800138004',
    email: 'chen@example.com',
    campusId: 1,
    campusName: '北京校区',
    avatar: 'https://via.placeholder.com/150',
    maxStudents: 25,
    currentStudents: 20,
    experience: 15,
    awards: '国家级教练',
    specialties: ['全面技术', '比赛训练', '心理辅导'],
    status: 'ACTIVE',
    createTime: '2024-01-18 11:15:00'
  }
]

module.exports = [
  // 根据校区ID获取教练列表
  {
    url: '/api/coaches/campus/\\d+',
    type: 'get',
    response: config => {
      const campusId = parseInt(config.url.match(/\\/api\\/coaches\\/campus\\/(\\d+)/)[1])
      const filteredCoaches = coaches.filter(coach => coach.campusId === campusId)
      
      return {
        code: 200,
        message: '获取成功',
        data: filteredCoaches
      }
    }
  },

  // 获取所有教练（分页）
  {
    url: '/api/users/type/COACH/page',
    type: 'get',
    response: config => {
      const { page = 0, size = 20 } = config.query
      
      const total = coaches.length
      const startIndex = parseInt(page) * parseInt(size)
      const endIndex = startIndex + parseInt(size)
      const records = coaches.slice(startIndex, endIndex)
      
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

  // 根据校区ID获取教练（分页）
  {
    url: '/api/users/coaches/campus/\\d+',
    type: 'get',
    response: config => {
      const campusId = parseInt(config.url.match(/\\/api\\/users\\/coaches\\/campus\\/(\\d+)/)[1])
      const { page = 0, size = 20 } = config.query
      
      const filteredCoaches = coaches.filter(coach => coach.campusId === campusId)
      const total = filteredCoaches.length
      const startIndex = parseInt(page) * parseInt(size)
      const endIndex = startIndex + parseInt(size)
      const records = filteredCoaches.slice(startIndex, endIndex)
      
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

  // 获取教练详情
  {
    url: '/api/coaches/\\d+',
    type: 'get',
    response: config => {
      const id = parseInt(config.url.match(/\\/api\\/coaches\\/(\\d+)/)[1])
      const coach = coaches.find(c => c.id === id)
      
      if (!coach) {
        return {
          code: 404,
          message: '教练不存在'
        }
      }
      
      return {
        code: 200,
        message: '获取成功',
        data: coach
      }
    }
  }
]