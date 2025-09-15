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
                :class="getStatusClass(course.status)"
                @click="handleCourseClick(course)">
              <div class="course-info">
                <div class="student-name" v-if="normalizedUserType === 'coach' && showStudentName">{{ course.studentName || '待预约' }}</div>
      <div class="student-name" v-else-if="normalizedUserType === 'coach'">已预约</div>
                <div class="coach-name" v-else>{{ course.coachName || '待预约' }}</div>
                <div class="court-number">{{ course.courtNumber }}</div>
                <div class="status">{{ getStatusText(course.status) }}</div>
                <div class="cancel-btn" v-if="canCancelCourse(course)">
                  <i class="el-icon-close"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="loading" v-if="loading">
      <i class="el-icon-loading"></i> 加载中...
    </div>

    <!-- 本周已取消课程列表 -->
    <div class="cancelled-courses" v-if="cancelledCourses.length > 0 && showCancelledList">
        <h3>本周已取消课程</h3>
        <el-table :data="cancelledCourses" style="width: 100%" stripe>
            <el-table-column prop="startTime" label="日期" width="100">
              <template slot-scope="scope">
                {{ formatDate(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="时间" width="120">
              <template slot-scope="scope">
                {{ formatTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column :label="normalizedUserType === 'coach' ? '学生' : '教练'" width="100">
              <template slot-scope="scope">
                {{ normalizedUserType === 'coach' ? scope.row.studentName : scope.row.coachName }}
              </template>
            </el-table-column>
            <el-table-column prop="courtNumber" label="球台" width="80"></el-table-column>
            <el-table-column prop="cancelByUserName" label="取消人" width="100">
              <template slot-scope="scope">
                <span :class="getCancelUserClass(scope.row)">{{ scope.row.cancelByUserName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="cancelReason" label="取消原因" min-width="200" show-overflow-tooltip>
              <template slot-scope="scope">
                <span style="color: #f56c6c; font-weight: 500;">{{ scope.row.cancelReason }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="cancelTime" label="取消时间" width="140">
              <template slot-scope="scope">
                {{ formatDate(scope.row.cancelTime) }} {{ formatTime(scope.row.cancelTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="mini" effect="dark">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
      </div>
  </div>
</template>

<script>
import { getStudentCoursesByDateRange, getCoachCoursesByDateRange, cancelCourse as apiCancelCourse, getStudentMonthlyCancelCount } from '@/api/course'
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
    },
    showCancelledList: {
      type: Boolean,
      default: true
    },
    showStudentName: {
      type: Boolean,
      default: true
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      courses: [],
      cancelledCourses: [], // 已取消课程列表
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
      ],
      monthlyCancelCount: 0, // 本月已取消次数
      maxMonthlyCancels: 3   // 每月最大取消次数
    }
  },
  computed: {
    ...mapGetters(['userId', 'user']),
    effectiveUserId() {
      return this.customUserId || this.userId
    },
    normalizedUserType() {
      return this.userType ? this.userType.toLowerCase() : ''
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
    
    async getStudentCancelCount() {
      if (this.normalizedUserType !== 'student') return
      
      try {
        const response = await getStudentMonthlyCancelCount(this.effectiveUserId)
        if (response.code === 200) {
          this.monthlyCancelCount = response.data || 0
        }
      } catch (error) {
        console.error('获取取消次数失败:', error)
      }
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
        
        // 如果是学生，先获取本月取消次数
        if (this.normalizedUserType === 'student') {
          await this.getStudentCancelCount()
        }
        
        let response
        if (this.normalizedUserType === 'student') {
          response = await getStudentCoursesByDateRange(this.effectiveUserId, start, end)
        } else {
          response = await getCoachCoursesByDateRange(this.effectiveUserId, start, end)
        }
        
        if (response.code === 200) {
          // 分离正常课程和已取消课程
          const allCourses = response.data || []
          this.courses = allCourses.filter(course => course.status !== 'CANCELLED')
          
          // 已取消课程按取消时间由近及远排序
          this.cancelledCourses = allCourses
            .filter(course => course.status === 'CANCELLED')
            .sort((a, b) => new Date(b.cancelTime) - new Date(a.cancelTime))
          
          console.log('获取到课程数量:', allCourses.length, '正常课程:', this.courses.length, '已取消课程:', this.cancelledCourses.length)
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程失败')
      } finally {
        this.loading = false
      }
    },
    
    canCancelCourse(course) {
        // 只读模式下不能取消
        if (this.readOnly) return false;
        
        // 已取消的课程不能再次取消
        if (course.status === 'CANCELLED') return false;
        
        // 教练只能取消已确认的课程，且必须是自己的课程
        if (this.normalizedUserType === 'coach' && course.status === 'CONFIRMED') {
          return course.coachId === this.effectiveUserId;
        }
        
        // 学生可以取消待确认和已确认的课程（只能取消自己的课程）
        if (this.normalizedUserType === 'student' && (course.status === 'PENDING' || course.status === 'CONFIRMED')) {
          return course.studentId === this.effectiveUserId;
        }
        
        return false;
      },
    
    async handleCourseClick(course) {
      if (!this.canCancelCourse(course)) return
      
      try {
        let cancelReason = ''
        
        // 根据课程状态和用户类型确定取消原因
        if (this.normalizedUserType === 'student' && course.status === 'PENDING') {
          // 学生取消待确认课程，不需要填写原因
          cancelReason = '待确认取消'
          
          const confirmResult = await this.$confirm('确定要取消这个待确认课程吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          
          if (confirmResult === 'confirm') {
            await this.cancelCourse(course.id, cancelReason)
            this.$message.success('课程取消成功')
            await this.loadCourses()
          }
        } else if (this.normalizedUserType === 'student' && course.status === 'CONFIRMED') {
          // 学生取消已确认课程，检查取消次数限制
          const remainingCancels = this.maxMonthlyCancels - this.monthlyCancelCount
          
          if (remainingCancels <= 0) {
            this.$message.warning(`您本月已取消${this.monthlyCancelCount}次课程，已达到最大取消次数限制(${this.maxMonthlyCancels}次)，无法继续取消课程`)
            return
          }
          
          const { value: reason } = await this.$prompt(
            `请输入取消原因（本月剩余取消次数：${remainingCancels}次）`, 
            '取消已确认课程', 
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              inputPattern: /\S+/,
              inputErrorMessage: '取消原因不能为空'
            }
          )
          
          if (reason) {
            await this.cancelCourse(course.id, reason)
            this.$message.success('课程取消成功')
            await this.getStudentCancelCount() // 重新获取取消次数
            await this.loadCourses()
          }
        } else {
          // 教练取消已确认课程，需要填写原因
          const { value: reason } = await this.$prompt('请输入取消原因', '取消课程', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPattern: /\S+/,
            inputErrorMessage: '取消原因不能为空'
          })
          
          if (reason) {
            await this.cancelCourse(course.id, reason)
            this.$message.success('课程取消成功')
            await this.loadCourses()
          }
        }
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          console.error('取消课程失败:', error)
          this.$message.error('取消课程失败')
        }
      }
    },
    
    async cancelCourse(courseId, reason) {
          try {
            const response = await apiCancelCourse(courseId, reason, this.effectiveUserId);
            return response.data;
          } catch (error) {
            throw error;
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
        // 过滤掉已取消的课程
        if (course.status === 'CANCELLED') return false
        
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
    },
    getStatusTagType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'COMPLETED': 'info',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || ''
    },
    getCancelUserClass(course) {
      if (!course.cancelByUserId) return ''
      
      // 根据取消人身份设置不同样式
      if (course.cancelByUserId === course.coachId) {
        return 'cancel-by-coach'  // 教练取消
      } else if (course.cancelByUserId === course.studentId) {
        return 'cancel-by-student'  // 学生取消
      } else {
        return 'cancel-by-system'  // 系统或其他用户取消
      }
    },
    formatDate(datetime) {
      if (!datetime) return ''
      const date = new Date(datetime)
      return date.toLocaleDateString('zh-CN')
    },
    formatTime(datetime) {
      if (!datetime) return ''
      const date = new Date(datetime)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
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

.cancel-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.1);
}

.cancel-btn i {
  font-size: 10px;
  color: white;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.cancelled-courses {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cancelled-courses h3 {
  margin-bottom: 16px;
  color: #f56c6c;
  font-size: 18px;
  font-weight: 500;
}

.cancel-by-coach {
  color: #409eff;
  font-weight: 600;
}

.cancel-by-student {
  color: #67c23a;
  font-weight: 600;
}

.cancel-by-system {
  color: #909399;
  font-weight: 600;
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