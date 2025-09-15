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
          <div v-else class="schedule-container">
            <div class="schedule-scroll">
              <div v-for="course in todaySchedule" :key="course.id" class="schedule-item">
                <div class="schedule-time">{{ course.startTime }} - {{ course.endTime }}</div>
                <div class="schedule-info">
                  <span class="student-name">学员：{{ course.studentName }}</span>
                  <span class="table-info">球台：{{ course.tableNumber }}</span>
                  <span class="status-info" :class="'status-' + course.status.toLowerCase()">
                    {{ getStatusText(course.status) }}
                  </span>
                </div>
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
          <div class="bookings-container">
            <div v-if="pendingBookingsList.length === 0" class="empty-bookings">
              <i class="el-icon-document"></i>
              <p>暂无待处理预约</p>
            </div>
            <div v-else class="bookings-scroll">
              <div v-for="booking in pendingBookingsList" :key="booking.id" class="booking-item">
                <div class="booking-time">{{ booking.date }} {{ booking.timeSlot }}</div>
                <div class="booking-info">
                  <span class="student-name">学员：{{ booking.studentName }}</span>
                  <span class="table-info">球台：{{ booking.courtNumber }}</span>
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
import { getCoachCoursesByDateRange, getCoachPendingBookings, getCoachMonthlyIncome } from '@/api/course'
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
      console.log('开始加载教练仪表盘数据...')
      try {
        // 获取学员数量
        const coachId = this.$store.state.user.user?.id
        console.log('当前教练ID:', coachId)
        
        if (coachId) {
          const countRes = await getCoachStudentCount(coachId)
          this.studentCount = countRes.data || 0
        }

        // 获取今日课程
        this.loadTodaySchedule()

        // 获取待处理预约
        this.loadPendingBookings()

        // 获取本月收入
        console.log('即将调用loadMonthlyIncome...')
        await this.loadMonthlyIncome()
        console.log('loadMonthlyIncome调用完成')
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
      }
    },

    loadTodaySchedule() {
      // 获取今日课程数据
      const today = new Date()
      const start = new Date(today)
      start.setHours(0, 0, 0, 0)
      const end = new Date(today)
      end.setHours(23, 59, 59, 999)
      
      const coachId = this.$store.state.user.user?.id
      if (!coachId) {
        console.error('无法获取教练ID')
        return
      }

      getCoachCoursesByDateRange(coachId, start, end)
        .then(response => {
          if (response.code === 200 && response.data) {
            this.todaySchedule = response.data
              .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
              .map(course => ({
                id: course.id,
                startTime: course.startTime.slice(11, 16),
                endTime: course.endTime.slice(11, 16),
                studentName: course.studentName,
                tableNumber: course.courtNumber,
                status: course.status
              }))
            this.todayCourses = this.todaySchedule.length
          } else {
            this.todaySchedule = []
            this.todayCourses = 0
          }
        })
        .catch(error => {
          console.error('获取今日课程失败:', error)
          this.todaySchedule = []
          this.todayCourses = 0
        })
    },

    async loadPendingBookings() {
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          console.error('无法获取教练ID')
          return
        }

        const response = await getCoachPendingBookings(coachId, 'PENDING')
        if (response.code === 200 && response.data) {
          this.pendingBookingsList = response.data.map(booking => ({
            id: booking.id,
            date: booking.startTime.slice(0, 10),
            timeSlot: `${booking.startTime.slice(11, 16)}-${booking.endTime.slice(11, 16)}`,
            studentName: booking.studentName,
            courtNumber: booking.courtNumber,
            startTime: booking.startTime,
            endTime: booking.endTime
          }))
          this.pendingBookings = this.pendingBookingsList.length
        } else {
          this.pendingBookingsList = []
          this.pendingBookings = 0
        }
      } catch (error) {
        console.error('获取待确认预约失败:', error)
        this.pendingBookingsList = []
        this.pendingBookings = 0
      }
    },

    async loadMonthlyIncome() {
      console.log('开始加载教练本月收入...')
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          console.error('无法获取教练ID')
          return
        }

        console.log('正在请求本月收入，教练ID:', coachId)
        const response = await getCoachMonthlyIncome(coachId)
        console.log('本月收入API响应:', response)
        
        if (response.code === 200 && response.data !== undefined) {
          this.monthlyIncome = response.data
          console.log('本月收入设置成功:', this.monthlyIncome)
        } else {
          this.monthlyIncome = 0
          console.log('本月收入数据无效，设置为0')
        }
      } catch (error) {
        console.error('获取本月收入失败:', error)
        this.monthlyIncome = 0
      }
    },

    goToStudents() {
      this.$router.push('/coach-students')
    },
    goToSetSchedule() {
      this.$router.push('/coach-schedule')
    },
    goToEvaluations() {
      this.$router.push('/coach-training-records')
    },
    goToStatistics() {
      this.$router.push('/coach-training-records')
    },
    goToBookings() {
      this.$router.push('/coach-appointments')
    },
    goToSchedule() {
      this.$router.push('/coach-schedule')
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-text {
  margin-bottom: 20px;
}

.dashboard-text h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.dashboard-text p {
  font-size: 14px;
  color: #666;
}

.panel-group {
  margin-top: 18px;
}

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
  border-radius: 8px;
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-panel:hover .card-panel-icon-wrapper {
  color: #fff;
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
  margin-left: 0px;
  flex: 1;
  text-align: right;
}

.card-panel-text {
  line-height: 18px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
  margin-bottom: 12px;
}

.card-panel-num {
  font-size: 20px;
  color: #666;
}

.card-panel-unit {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}

.icon-people {
  color: #40c9c6;
}

.icon-message {
  color: #36a3f7;
}

.icon-star {
  color: #f4516c;
}

.icon-money {
  color: #34bfa3;
}

.box-card {
  margin-bottom: 20px;
}

.schedule-container,
.bookings-container {
  max-height: 180px; /* 显示3个项目的高度 */
  overflow: hidden;
}

.schedule-scroll,
.bookings-scroll {
  max-height: 180px;
  overflow-y: auto;
  padding-right: 5px; /* 为滚动条预留空间 */
}

.schedule-scroll::-webkit-scrollbar,
.bookings-scroll::-webkit-scrollbar {
  width: 6px;
}

.schedule-scroll::-webkit-scrollbar-track,
.bookings-scroll::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.schedule-scroll::-webkit-scrollbar-thumb,
.bookings-scroll::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.schedule-scroll::-webkit-scrollbar-thumb:hover,
.bookings-scroll::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.schedule-item,
.booking-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.schedule-item:last-child,
.booking-item:last-child {
  border-bottom: none;
}

.schedule-time,
.booking-time {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.schedule-info,
.booking-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.student-name,
.table-info {
  color: #666;
  font-size: 14px;
}

.status-info {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}

.status-confirmed {
  background-color: #f0f9ff;
  color: #409eff;
  border: 1px solid #b3d8ff;
}

.status-completed {
  background-color: #f0f9ff;
  color: #67c23a;
  border: 1px solid #b3d8ff;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fab6b6;
}

.empty-schedule,
.empty-bookings {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-schedule i,
.empty-bookings i {
  font-size: 48px;
  margin-bottom: 10px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  margin: 0;
}

@media (max-width: 768px) {
  .card-panel {
    height: auto;
    padding: 15px;
  }
  
  .card-panel-icon-wrapper {
    display: none;
  }
  
  .card-panel-description {
    text-align: center;
  }
  
  .schedule-container,
  .bookings-container {
    max-height: none; /* 移动端不限制高度 */
  }
  
  .schedule-scroll,
  .bookings-scroll {
    max-height: none;
    overflow-y: visible;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .quick-actions .el-button {
    width: 100%;
  }
}
</style>