<template>
  <div class="dashboard-container">
    <div class="dashboard-text">
      <h2>教练工作台 - {{ name }}</h2>
      <p>欢迎回来，{{ name }}！今天是 {{ today }}，祝您工作顺利！</p>
    </div>

    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="user" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">我的学员</div>
            <count-to :start-val="0" :end-val="studentCount" :duration="3200" class="card-panel-num" />
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
            <div class="card-panel-text">待确认预约</div>
            <count-to :start-val="0" :end-val="pendingBookings" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">个</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-money">
            <svg-icon icon-class="money" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">本月收入</div>
            <count-to :start-val="0" :end-val="monthlyIncome" :duration="3200" class="card-panel-num" />
            <span class="card-panel-unit">元</span>
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
              <div class="schedule-time">{{ course.startTime }} - {{ course.endTime }}</div>
              <div class="schedule-info">
                <span class="student-name">学员：{{ course.studentName }}</span>
                <span class="table-info">球台：{{ course.tableNumber }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>待处理预约</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToBookings">查看全部</el-button>
          </div>
          <div v-if="pendingBookingsList.length === 0" class="empty-bookings">
            <i class="el-icon-bell"></i>
            <p>暂无待处理预约</p>
          </div>
          <div v-else>
            <div v-for="booking in pendingBookingsList.slice(0, 3)" :key="booking.id" class="booking-item">
              <div class="booking-time">{{ booking.date }} {{ booking.timeSlot }}</div>
              <div class="booking-info">
                <span class="student-name">学员：{{ booking.studentName }}</span>
                <div class="booking-actions">
                  <el-button size="mini" type="success" @click="confirmBooking(booking.id)">接受</el-button>
                  <el-button size="mini" type="danger" @click="rejectBooking(booking.id)">拒绝</el-button>
                </div>
              </div>
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
            <el-button type="primary" icon="el-icon-user" @click="goToStudents">我的学员</el-button>
            <el-button type="success" icon="el-icon-time" @click="goToSetSchedule">设置课表</el-button>
            <el-button type="warning" icon="el-icon-document" @click="goToEvaluations">学员评价</el-button>
            <el-button type="info" icon="el-icon-s-data" @click="goToStatistics">数据统计</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CountTo from 'vue-count-to'
import { getCoachStudentCount } from '@/api/coach'
import { getCoachCourses, getCancellationStats } from '@/api/course'
import { getSystemMessages } from '@/api/system'

export default {
  name: 'CoachDashboard',
  components: {
    CountTo
  },
  data() {
    return {
      studentCount: 0,
      todayCourses: 0,
      pendingBookings: 0,
      monthlyIncome: 0,
      todaySchedule: [],
      pendingBookingsList: [],
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
        // 获取学员数量
        const coachId = this.$store.state.user.user?.id
        if (coachId) {
          const countRes = await getCoachStudentCount(coachId)
          this.studentCount = countRes.data || 0
        }

        // 获取今日课程
        this.loadTodaySchedule()

        // 获取待处理预约
        this.loadPendingBookings()

        // 获取系统通知
        const messagesRes = await getSystemMessages({ page: 1, size: 5 })
        this.notifications = messagesRes.data.records || []

        // 计算本月收入
        this.calculateMonthlyIncome()
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
      }
    },

    loadTodaySchedule() {
      // 模拟今日课程数据
      this.todaySchedule = [
        {
          id: 1,
          startTime: '09:00',
          endTime: '10:30',
          studentName: '小明',
          tableNumber: 'A1'
        },
        {
          id: 2,
          startTime: '14:00',
          endTime: '15:30',
          studentName: '小红',
          tableNumber: 'B2'
        }
      ]
      this.todayCourses = this.todaySchedule.length
    },

    loadPendingBookings() {
      // 模拟待处理预约数据
      this.pendingBookingsList = [
        {
          id: 1,
          date: '2024-01-15',
          timeSlot: '10:00-11:30',
          studentName: '小刚'
        }
      ]
      this.pendingBookings = this.pendingBookingsList.length
    },

    calculateMonthlyIncome() {
      // 模拟计算本月收入
      this.monthlyIncome = 12500
    },

    confirmBooking(bookingId) {
      this.$message.success('预约已确认')
      this.loadPendingBookings()
    },

    rejectBooking(bookingId) {
      this.$prompt('请输入拒绝原因', '拒绝预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{2,100}$/,
        inputErrorMessage: '请输入2-100个字符的拒绝原因'
      }).then(({ value }) => {
        this.$message.success('预约已拒绝')
        this.loadPendingBookings()
      })
    },

    goToStudents() {
      this.$router.push('/coach/students')
    },
    goToSetSchedule() {
      this.$router.push('/coach/schedule')
    },
    goToEvaluations() {
      this.$router.push('/coach/evaluations')
    },
    goToStatistics() {
      this.$router.push('/coach/statistics')
    },
    goToBookings() {
      this.$router.push('/coach/bookings')
    },
    goToSchedule() {
      this.$router.push('/coach/schedule')
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

      .icon-people {
        background: #36a3f7;
      }

      .icon-message {
        background: #f4516c;
      }

      .icon-star {
        background: #34bfa3;
      }

      .icon-money {
        background: #40c9c6;
      }
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

    .icon-money {
      color: #40c9c6;
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
.empty-bookings {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  
  i {
    font-size: 48px;
    margin-bottom: 16px;
  }
}

.schedule-item,
.booking-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
  
  .schedule-time,
  .booking-time {
    font-weight: bold;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .schedule-info,
  .booking-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #606266;
    font-size: 14px;
    
    .booking-actions {
      display: flex;
      gap: 8px;
    }
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