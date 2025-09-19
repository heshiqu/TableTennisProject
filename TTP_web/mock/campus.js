const Mock = require('mockjs')

const campusMocks = [
  // 获取校区学员数量
  {
    url: '/api/users/count/students/campus/[0-9]+',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: Mock.Random.integer(10, 100),
        timestamp: +new Date()
      }
    }
  },

  // 获取当前用户所在校区
  {
    url: '/api/campuses/user',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: {
          id: 1,
          name: '北京朝阳校区',
          address: '北京市朝阳区建国路88号',
          phone: '010-12345678',
          email: 'beijing@tabletennis.com',
          description: '北京朝阳校区成立于2010年，是北京市最大的乒乓球训练基地之一，拥有专业教练团队和先进设施。'
        },
        timestamp: +new Date()
      }
    }
  },

  // 获取校区仪表板数据
  {
    url: '/api/campuses/dashboard',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: {
          coachCount: Mock.Random.integer(5, 30),
          todayCourseCount: Mock.Random.integer(10, 50),
          monthlyIncome: Mock.Random.integer(10000, 100000)
        },
        timestamp: +new Date()
      }
    }
  },

  // 获取当前校区信息
  {
    url: '/api/campuses/current',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: {
          id: 1,
          name: '北京朝阳校区',
          address: '北京市朝阳区建国路88号',
          phone: '010-12345678',
          email: 'beijing@tabletennis.com',
          description: '北京朝阳校区成立于2010年，是北京市最大的乒乓球训练基地之一',
          status: 'ACTIVE',
          createdAt: '2020-01-15'
        },
        timestamp: +new Date()
      }
    }
  },

  // 更新校区信息
  {
    url: '/api/campuses/current',
    type: 'put',
    response: () => {
      return {
        code: 200,
        message: '更新成功',
        data: null,
        timestamp: +new Date()
      }
    }
  },

  // 获取校区统计信息
  {
    url: '/api/campuses/[0-9]+/stats',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: {
          totalStudents: Mock.Random.integer(50, 200),
          totalCoaches: Mock.Random.integer(5, 30),
          totalTables: Mock.Random.integer(10, 50),
          monthlyIncome: Mock.Random.integer(50000, 200000)
        },
        timestamp: +new Date()
      }
    }
  },

  // 获取校区公告
  {
    url: '/api/campuses/announcements',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: [
          {
            id: 1,
            title: '校区系统维护通知',
            content: '本周六将进行系统维护，请提前做好安排',
            createTime: '2024-01-15 10:00:00',
            creator: '管理员'
          },
          {
            id: 2,
            title: '新年活动安排',
            content: '本周日将举行新年乒乓球比赛，欢迎参加',
            createTime: '2024-01-14 09:00:00',
            creator: '管理员'
          }
        ],
        timestamp: +new Date()
      }
    }
  },

  // 创建校区公告
  {
    url: '/api/campuses/announcements',
    type: 'post',
    response: () => {
      return {
        code: 200,
        message: '公告发布成功',
        data: null,
        timestamp: +new Date()
      }
    }
  },

  // 获取待处理事项
  {
    url: '/api/campuses/pending-items',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: [
          {
            id: 1,
            type: 'coach_audit',
            title: '新教练审核',
            description: '张三教练申请入驻，需要审核',
            createTime: '2024-01-15 11:00:00'
          },
          {
            id: 2,
            type: 'course_confirm',
            title: '课程确认',
            description: '李四学员预约了明天10点的课程，需要确认',
            createTime: '2024-01-15 12:00:00'
          }
        ],
        timestamp: +new Date()
      }
    }
  },

  // 获取校区总数
  {
    url: '/api/campuses/count',
    type: 'get',
    response: () => {
      return {
        code: 200,
        message: '获取成功',
        data: Mock.Random.integer(3, 10),
        timestamp: +new Date()
      }
    }
  }
]

module.exports = campusMocks