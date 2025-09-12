<template>
  <div class="dashboard-container">
    <div class="dashboard-text">
      <h2>学员工作台 - {{ name }}</h2>
      <p>欢迎回来，{{ name }}！今天是 {{ today }}，祝您训练愉快！</p>
    </div>

    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-money">
            <svg-icon icon-class="money" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">账户余额</div>
            <count-to :start-val="0" :end-val="balance" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">元</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="user" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">我的教练</div>
            <count-to :start-val="0" :end-val="coachCount" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">人</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-message">
            <svg-icon icon-class="message" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日课程</div>
            <count-to :start-val="0" :end-val="todayCourses" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">节</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-star">
            <svg-icon icon-class="star" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">本月取消次数</div>
            <count-to :start-val="0" :end-val="cancellationCount" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">次</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>今日课程安排</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToSchedule">查看全部</el-button>
          </div>
          <div v-if="todaySchedule.length === 0" class="empty-schedule">
            <i class="el-icon-date"></i>
            <p>今日暂无课程安排</p>
          </div>
          <div v-else>
            <div v-for="course in todaySchedule" :key="course.id" class="schedule-item">
              <div class="schedule-time">{{ course.startTime.slice(11, 16) }} - {{ course.endTime.slice(11, 16) }}</div>
              <div class="schedule-info">
                <span class="coach-name">教练：{{ course.coachName }}</span>
                <span class="table-info">状态：{{ course.status === 'CONFIRMED' ? '已确认' : course.status }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>系统通知</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToMessages">查看全部</el-button>
          </div>
          <div v-if="notifications.length === 0" class="empty-notifications">
            <i class="el-icon-bell"></i>
            <p>暂无新通知</p>
          </div>
          <div v-else>
            <div v-for="notification in notifications.slice(0, 3)" :key="notification.id" class="notification-item">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-time">{{ notification.createTime }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" icon="el-icon-search" @click="goToSearchCoach">查找教练</el-button>
            <el-button type="success" icon="el-icon-plus" @click="goToBookCourse">预约课程</el-button>
            <el-button type="warning" icon="el-icon-money" @click="goToRecharge">账户充值</el-button>
            <el-button type="info" icon="el-icon-trophy" @click="goToTournament">参加比赛</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CountTo from 'vue-count-to'
import { getBalance, getStudentCoachCount, getStudentCoursesByDateRange } from '@/api/student'
import { getCancellationStats } from '@/api/course'
import { getSystemMessages } from '@/api/system'

export default {
  name: 'StudentDashboard',
  components: {
    CountTo
  },
  data() {
    return {
      balance: 0,
      coachCount: 0,
      todayCourses: 0,
      cancellationCount: 0,
      todaySchedule: [],
      notifications: [],
      today: new Date().toLocaleDateString('zh-CN')
    }
  },
  computed: {
    ...mapGetters(['name', 'avatar', 'roles'])
  },
  created() {
    this.loadDashboardData()
  },
  methods: {
    async loadDashboardData() {
      try {
        // 等待用户数据加载完成
        await this.$store.dispatch('user/getInfo')
        
        const userId = this.$store.state.user.user?.id
        console.log('loadDashboardData - 用户ID:', userId)
        
        if (userId) {
          // 获取今天的日期范围（使用本地时间）
          const today = new Date()
          const year = today.getFullYear()
          const month = String(today.getMonth() + 1).padStart(2, '0')
          const day = String(today.getDate()).padStart(2, '0')
          
          const startStr = `${year}-${month}-${day} 00:00:00`
          const endStr = `${year}-${month}-${day} 23:59:59`
          
          console.log('今日课程查询范围:', startStr, '至', endStr)
          console.log('当前系统时间:', new Date().toLocaleString())

          // 并行获取所有数据
          const [balanceRes, statsRes, messagesRes, coachCountRes, coursesRes] = await Promise.allSettled([
            getBalance(userId),
            getCancellationStats(),
            getSystemMessages({ page: 1, size: 5 }),
            getStudentCoachCount(userId),
            this.getTodayCourses(userId, startStr, endStr)
          ])
          
          // 处理余额
          if (balanceRes.status === 'fulfilled') {
            this.balance = balanceRes.value.data || 0
          } else {
            console.error('获取余额失败:', balanceRes.reason)
            this.balance = 0
          }
          
          // 处理取消次数
          if (statsRes.status === 'fulfilled') {
            this.cancellationCount = statsRes.value.data.monthlyCancellation || 0
          } else {
            this.cancellationCount = 0
          }
          
          // 处理通知
          if (messagesRes.status === 'fulfilled') {
            this.notifications = messagesRes.value.data.records || []
          } else {
            this.notifications = []
          }
          
          // 处理教练数量
          if (coachCountRes.status === 'fulfilled') {
            this.coachCount = coachCountRes.value.data || 0
            console.log('成功获取教练数量:', this.coachCount)
          } else {
            console.error('获取教练数量失败:', coachCountRes.reason)
            this.coachCount = 0
          }

          // 处理今日课程
          if (coursesRes.status === 'fulfilled') {
            this.todaySchedule = coursesRes.value.data || []
            this.todayCourses = this.todaySchedule.length
            console.log('成功获取今日课程:', this.todaySchedule)
          } else {
            console.error('获取今日课程失败:', coursesRes.reason)
            this.todaySchedule = []
            this.todayCourses = 0
          }
          
        } else {
          console.error('无法获取用户ID')
          this.balance = 0
          this.coachCount = 0
          this.cancellationCount = 0
          this.notifications = []
          this.todaySchedule = []
          this.todayCourses = 0
        }
        
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
      }
    },

    async getTodayCourses(studentId, startDate, endDate) {
      return getStudentCoursesByDateRange(studentId, startDate, endDate)
    },

    loadCoachCount() {
      // 此方法已集成到loadDashboardData中，保留空实现
    },

    goToSearchCoach() {
      this.$router.push('/student/coach-search')
    },
    goToBookCourse() {
      this.$router.push('/student/course-book')
    },
    goToRecharge() {
      this.$router.push('/student/recharge')
    },
    goToTournament() {
      this.$router.push('/student/tournament')
    },
    goToSchedule() {
      this.$router.push('/student/schedule')
    },
    goToMessages() {
      this.$router.push('/student/messages')
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  min-height: calc(100vh - 84px);
}

.dashboard-text {
  margin-bottom: 32px;
  
  h2 {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 16px;
    color: #606266;
  }
}

.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-money {
        background: #40c9c6;
      }

      .icon-people {
        background: #36a3f7;
      }

      .icon-message {
        background: #f4516c;
      }

      .icon-star {
        background: #34bfa3;
      }
    }

    .icon-money {
      color: #40c9c6;
    }

    .icon-people {
      color: #36a3f7;
    }

    .icon-message {
      color: #f4516c;
    }

    .icon-star {
      color: #34bfa3;
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }

      .card-panel-unit {
        font-size: 16px;
        color: #606266;
        margin-left: 4px;
      }
    }
  }
}

.box-card {
  margin-bottom: 20px;
}

.empty-schedule,
.empty-notifications {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  
  i {
    font-size: 48px;
    margin-bottom: 16px;
  }
}

.schedule-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
  
  .schedule-time {
    font-weight: bold;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .schedule-info {
    display: flex;
    justify-content: space-between;
    color: #606266;
    font-size: 14px;
  }
}

.notification-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
  
  .notification-title {
    font-weight: bold;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .notification-time {
    color: #909399;
    font-size: 12px;
  }
}

.quick-actions {
  text-align: center;
  padding: 20px 0;
  
  .el-button {
    margin: 0 10px;
  }
}
</style>