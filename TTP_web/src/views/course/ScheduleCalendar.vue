<template>
  <div class="schedule-calendar">
    <div class="calendar-header" v-if="showHeader">
      <el-button-group>
        <el-button icon="el-icon-arrow-left" @click="prevWeek">上一周</el-button>
        <el-button @click="goToCurrentWeek">本周</el-button>
        <el-button icon="el-icon-arrow-right" @click="nextWeek">下一周</el-button>
      </el-button-group>
      <div class="week-range">
        {{ currentWeekRange }}
      </div>
    </div>

    <div class="calendar-body">
      <div class="time-slots">
        <div class="time-slot-header">
          <div class="time-label">时间</div>
          <div class="day-label" v-for="day in weekDays" :key="day.date">
            {{ day.label }}
          </div>
        </div>
        
        <div class="time-slot-row" v-for="timeSlot in timeSlots" :key="timeSlot">
          <div class="time-label">{{ timeSlot }}</div>
          <div class="day-slot" v-for="day in weekDays" :key="day.date + timeSlot">
            <div 
              v-for="course in getCoursesForSlot(day.date, timeSlot)" 
              :key="course.id"
              class="course-item"
              :class="getStatusClass(course.status)">
              <div class="course-info">
                <div class="coach-name">{{ course.coachName || course.studentName }}</div>
                <div class="court-number">{{ course.courtNumber }}</div>
                <div class="status">{{ getStatusText(course.status) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="loading" v-if="loading">
      <i class="el-icon-loading"></i> 加载中...
    </div>
  </div>
</template>

<script>
import { getStudentCoursesByDateRange, getCoachCoursesByDateRange } from '@/api/course'
import { mapGetters } from 'vuex'

export default {
  name: 'ScheduleCalendar',
  props: {
    userType: {
      type: String,
      required: true,
      validator: value => ['student', 'coach'].includes(value)
    },
    customUserId: {
      type: [String, Number],
      default: null
    },
    showHeader: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      courses: [],
      loading: false,
      currentWeekStart: null,
      timeSlots: [
        '08:00-09:00',
        '09:00-10:00',
        '10:00-11:00',
        '11:00-12:00',
        '14:00-15:00',
        '15:00-16:00',
        '16:00-17:00',
        '17:00-18:00',
        '19:00-20:00',
        '20:00-21:00'
      ]
    }
  },
  computed: {
    ...mapGetters(['userId', 'user']),
    effectiveUserId() {
      return this.customUserId || this.userId
    },
    weekDays() {
      const days = []
      const start = new Date(this.currentWeekStart)
      const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(start)
        date.setDate(start.getDate() + i)
        days.push({
          date: date.toISOString().split('T')[0],
          label: `${dayNames[date.getDay()]} ${date.getMonth() + 1}/${date.getDate()}`
        })
      }
      return days
    },
    currentWeekRange() {
      if (!this.currentWeekStart) return ''
      const start = new Date(this.currentWeekStart)
      const end = new Date(start)
      end.setDate(start.getDate() + 6)
      return `${start.getFullYear()}年${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
    }
  },
  async created() {
    this.setCurrentWeek()
    
    // 如果不是使用customUserId，确保当前用户信息已加载
    if (!this.customUserId) {
      if (!this.userId) {
        await this.$store.dispatch('user/getInfo')
      }
    }
    
    console.log('用户类型:', this.userType)
    console.log('用户ID:', this.effectiveUserId)
    if (!this.customUserId) {
      console.log('用户信息:', this.user)
    }
    
    if (this.effectiveUserId) {
      this.loadCourses()
    } else {
      console.error('无法获取用户ID')
      this.$message.error('无法获取用户信息，请重新登录')
    }
  },
  methods: {
    setCurrentWeek() {
      const today = new Date()
      const dayOfWeek = today.getDay()
      const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      this.currentWeekStart = new Date(today.setDate(diff))
    },
    async loadCourses() {
      if (!this.effectiveUserId) {
        console.error('用户ID未定义')
        this.$message.error('请先登录')
        return
      }

      this.loading = true
      try {
        const start = new Date(this.currentWeekStart)
        const end = new Date(start)
        end.setDate(start.getDate() + 6)
        
        // 设置时间为当天开始和结束
        start.setHours(0, 0, 0, 0)
        end.setHours(23, 59, 59, 999)
        
        let response
        if (this.userType === 'student') {
          response = await getStudentCoursesByDateRange(this.effectiveUserId, start, end)
        } else {
          response = await getCoachCoursesByDateRange(this.effectiveUserId, start, end)
        }
        
        if (response.code === 200) {
          this.courses = response.data
          console.log('获取到课程数量:', this.courses.length)
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程失败')
      } finally {
        this.loading = false
      }
    },
    prevWeek() {
      const newStart = new Date(this.currentWeekStart)
      newStart.setDate(newStart.getDate() - 7)
      this.currentWeekStart = newStart
      this.loadCourses()
    },
    nextWeek() {
      const newStart = new Date(this.currentWeekStart)
      newStart.setDate(newStart.getDate() + 7)
      this.currentWeekStart = newStart
      this.loadCourses()
    },
    goToCurrentWeek() {
      this.setCurrentWeek()
      this.loadCourses()
    },
    getCoursesForSlot(date, timeSlot) {
      const [slotStartStr, slotEndStr] = timeSlot.split('-')
      const [slotStartHour, slotStartMinute] = slotStartStr.split(':').map(Number)
      const [slotEndHour, slotEndMinute] = slotEndStr.split(':').map(Number)
      
      const slotStartMinutes = slotStartHour * 60 + slotStartMinute
      const slotEndMinutes = slotEndHour * 60 + slotEndMinute
      
      return this.courses.filter(course => {
        const courseStart = new Date(course.startTime)
        const courseEnd = new Date(course.endTime)
        const courseDate = courseStart.toISOString().split('T')[0]
        
        if (courseDate !== date) return false
        
        const courseStartMinutes = courseStart.getHours() * 60 + courseStart.getMinutes()
        const courseEndMinutes = courseEnd.getHours() * 60 + courseEnd.getMinutes()
        
        // 检查课程时间段与时间槽是否重叠
        return courseStartMinutes < slotEndMinutes && courseEndMinutes > slotStartMinutes
      })
    },
    getStatusClass(status) {
      const statusMap = {
        'PENDING': 'status-pending',
        'CONFIRMED': 'status-confirmed',
        'COMPLETED': 'status-completed',
        'CANCELLED': 'status-cancelled'
      }
      return statusMap[status] || ''
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.schedule-calendar {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.week-range {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.calendar-body {
  overflow-x: auto;
}

.time-slots {
  min-width: 800px;
}

.time-slot-header {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  border-bottom: 1px solid #eee;
  background: #f5f7fa;
}

.time-label,
.day-label {
  padding: 12px;
  text-align: center;
  font-weight: 500;
  border-right: 1px solid #eee;
}

.time-slot-row {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  min-height: 80px;
  border-bottom: 1px solid #eee;
}

.day-slot {
  border-right: 1px solid #eee;
  position: relative;
  min-height: 80px;
  padding: 4px;
}

.course-item {
  margin: 2px;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.4;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.coach-name,
.student-name {
  font-weight: 500;
  color: #333;
}

.court-number {
  color: #666;
}

.status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 2px;
  display: inline-block;
}

.status-pending {
  background-color: #fdf6ec;
  border: 1px solid #f5dab1;
}

.status-pending .status {
  background-color: #e6a23c;
  color: white;
}

.status-confirmed {
  background-color: #f0f9ff;
  border: 1px solid #b3d8ff;
}

.status-confirmed .status {
  background-color: #409eff;
  color: white;
}

.status-completed {
  background-color: #f0f9ff;
  border: 1px solid #b3d8ff;
}

.status-completed .status {
  background-color: #67c23a;
  color: white;
}

.status-cancelled {
  background-color: #fef0f0;
  border: 1px solid #fab6b6;
}

.status-cancelled .status {
  background-color: #f56c6c;
  color: white;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 768px) {
  .calendar-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .time-slots {
    min-width: 600px;
  }
}
</style>