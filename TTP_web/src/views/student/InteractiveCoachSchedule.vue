<template>
  <div class="interactive-schedule">
    <div class="schedule-header">
      <div class="coach-info">
        <h3>{{ coachName }}的课表</h3>
        <p>点击时间段选择课程时间，可连续点击扩展课程时长</p>
      </div>
      <div class="week-navigator">
        <el-button-group>
          <el-button icon="el-icon-arrow-left" @click="prevWeek" size="small">上一周</el-button>
          <el-button @click="goToCurrentWeek" size="small">本周</el-button>
          <el-button icon="el-icon-arrow-right" @click="nextWeek" size="small">下一周</el-button>
        </el-button-group>
      </div>
    </div>

    <div class="calendar-container">
      <div class="calendar-grid">
          <div class="time-header">
            <div class="time-label">时间</div>
            <div class="day-header" v-for="day in weekDays" :key="day.date">
              <div class="day-name">{{ day.name }}</div>
              <div class="day-date">{{ day.dateLabel }}</div>
            </div>
          </div>

          <div class="time-slots">
            <div class="time-row" v-for="slot in timeSlots" :key="slot">
              <div class="time-slot-label">{{ slot }}</div>
              <div 
                v-for="day in weekDays" 
                :key="`${day.date}-${slot}`"
                class="time-slot-cell"
                :class="getSlotClasses(day.date, slot)"
                @click="selectSlot(day.date, slot)">
                <div class="slot-content">
                  <div v-if="hasCourse(day.date, slot)" class="course-info">
                    <span class="course-type">{{ getCourseType(day.date, slot) }}</span>
                  </div>
                  <div v-else-if="isSelected(day.date, slot)" class="selected-indicator">
                    <i class="el-icon-check"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
    </div>

    <div class="selection-info" v-if="selectedRange.start">
      <el-alert
        type="info"
        :title="`已选择: ${formatSelectionRange()}`"
        :closable="false"
        show-icon>
      </el-alert>
    </div>

    <div class="legend">
      <div class="legend-item">
        <div class="legend-color available"></div>
        <span>可选时间</span>
      </div>
      <div class="legend-item">
        <div class="legend-color occupied"></div>
        <span>已占用</span>
      </div>
      <div class="legend-item">
        <div class="legend-color selected"></div>
        <span>已选择</span>
      </div>
    </div>
  </div>
</template>

<script>
import { getCoachCoursesByDateRange } from '@/api/course'

export default {
  name: 'InteractiveCoachSchedule',
  props: {
    coachId: {
      type: [String, Number],
      required: true
    },
    coachName: {
      type: String,
      required: true
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
      ],
      selectedRange: {
        start: null,
        end: null
      },

    }
  },
  computed: {
    weekDays() {
      const days = []
      const start = new Date(this.currentWeekStart)
      const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(start)
        date.setDate(start.getDate() + i)
        days.push({
          date: date.toISOString().split('T')[0],
          name: dayNames[date.getDay()],
          dateLabel: `${date.getMonth() + 1}/${date.getDate()}`
        })
      }
      return days
    }
  },
  async created() {
    this.setCurrentWeek()
    await this.loadCourses()
  },
  methods: {
    setCurrentWeek() {
      const today = new Date()
      const dayOfWeek = today.getDay()
      const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      this.currentWeekStart = new Date(today.setDate(diff))
    },
    async loadCourses() {
      if (!this.coachId) return
      
      this.loading = true
      try {
        const start = new Date(this.currentWeekStart)
        const end = new Date(start)
        end.setDate(start.getDate() + 6)
        
        start.setHours(0, 0, 0, 0)
        end.setHours(23, 59, 59, 999)
        
        const response = await getCoachCoursesByDateRange(this.coachId, start, end)
        if (response && response.code === 200) {
          this.courses = response.data || []
        } else {
          this.courses = []
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程失败')
        this.courses = []
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
    hasCourse(date, slot) {
      if (!this.courses || !Array.isArray(this.courses)) return false
      const [startTime] = slot.split('-')
      
      return this.courses.some(course => {
        if (!course || !course.startTime || !course.endTime) return false
        
        // 排除已取消的课程，已取消的课程不应占用时间段
        if (course.status === 'CANCELLED') {
          return false
        }
        
        try {
          const courseDate = new Date(course.startTime).toISOString().split('T')[0]
          const courseStartTime = new Date(course.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
          const courseEndTime = new Date(course.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
          
          // 检查这个时间段是否被课程覆盖
          const slotStartTime = startTime
          const slotEndTime = slot.split('-')[1]
          
          // 课程时间段与格子时间段重叠
          return courseDate === date && 
                 ((courseStartTime <= slotStartTime && courseEndTime > slotStartTime) ||
                  (courseStartTime >= slotStartTime && courseStartTime < slotEndTime))
        } catch (error) {
          return false
        }
      })
    },
    getCourseType(date, slot) {
      if (!this.courses || !Array.isArray(this.courses)) return ''
      const [startTime] = slot.split('-')
      
      const course = this.courses.find(course => {
        if (!course || !course.startTime || !course.endTime) return false
        
        // 排除已取消的课程
        if (course.status === 'CANCELLED') {
          return false
        }
        
        try {
          const courseDate = new Date(course.startTime).toISOString().split('T')[0]
          const courseStartTime = new Date(course.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
          const courseEndTime = new Date(course.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
          
          // 检查这个时间段是否被课程覆盖
          const slotStartTime = startTime
          const slotEndTime = slot.split('-')[1]
          
          return courseDate === date && 
                 ((courseStartTime <= slotStartTime && courseEndTime > slotStartTime) ||
                  (courseStartTime >= slotStartTime && courseStartTime < slotEndTime))
        } catch (error) {
          return false
        }
      })
      
      if (course) {
        const courseStart = new Date(course.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        const courseEnd = new Date(course.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        return `已预约 (${courseStart}-${courseEnd})`
      }
      return ''
    },
    isPastTime(date, slot) {
      // 检查是否为过去时间（使用本地时间）
      const [startTime] = slot.split('-')
      const [startHour, startMinute] = startTime.split(':').map(Number)
      
      // 获取当前本地时间
      const now = new Date()
      
      // 构建时间段对应的本地时间
      const [year, month, day] = date.split('-').map(Number)
      const slotTime = new Date(year, month - 1, day, startHour, startMinute)
      
      // 获取当前本地日期（用于日期比较）
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      
      // 构建时间段对应的本地日期
      const slotDate = new Date(year, month - 1, day)
      
      // 如果日期早于今天，则设为过去
      if (slotDate < today) {
        return true
      }
      
      // 如果是今天，判断具体时间
      if (slotDate.getTime() === today.getTime()) {
        const nowHour = now.getHours()
        const nowMinute = now.getMinutes()
        return (startHour < nowHour) || (startHour === nowHour && startMinute <= nowMinute)
      }
      
      return false
    },
    
    isSlotAvailable(date, slot) {
      // 检查是否有课程占用
      if (this.hasCourse(date, slot)) {
        return false
      }
      
      // 检查是否为过去时间
      if (this.isPastTime(date, slot)) {
        return false
      }
      
      return true
    },
    isSelected(date, slot) {
      if (!this.selectedRange.start) return false
      
      const slotIndex = this.timeSlots.indexOf(slot)
      const dateIndex = this.weekDays.findIndex(day => day.date === date)
      
      const start = this.selectedRange.start
      const end = this.selectedRange.end || this.selectedRange.start
      
      const startDateIndex = this.weekDays.findIndex(day => day.date === start.date)
      const endDateIndex = this.weekDays.findIndex(day => day.date === end.date)
      const startSlotIndex = this.timeSlots.indexOf(start.slot)
      const endSlotIndex = this.timeSlots.indexOf(end.slot)
      
      // 检查是否在连续选择范围内
      if (dateIndex === startDateIndex && dateIndex === endDateIndex) {
        // 同一天内
        return slotIndex >= Math.min(startSlotIndex, endSlotIndex) && 
               slotIndex <= Math.max(startSlotIndex, endSlotIndex)
      } else if (dateIndex === startDateIndex) {
        // 开始日期
        return slotIndex >= startSlotIndex
      } else if (dateIndex === endDateIndex) {
        // 结束日期
        return slotIndex <= endSlotIndex
      } else if (dateIndex > Math.min(startDateIndex, endDateIndex) && 
                 dateIndex < Math.max(startDateIndex, endDateIndex)) {
        // 中间日期
        return true
      }
      
      return false
    },
    getSlotClasses(date, slot) {
      const classes = []
      
      if (!this.isSlotAvailable(date, slot)) {
        if (this.isPastTime(date, slot)) {
          classes.push('past')
        } else {
          classes.push('occupied')
        }
      } else {
        classes.push('available')
      }
      
      if (this.isSelected(date, slot)) {
        classes.push('selected')
        // 判断当前格子是否为选择范围的起点或终点，添加额外标记
        if (this.selectedRange.start && this.selectedRange.end) {
          const isStart = date === this.selectedRange.start.date && slot === this.selectedRange.start.slot
          const isEnd = date === this.selectedRange.end.date && slot === this.selectedRange.end.slot
          if (isStart) classes.push('range-start')
          if (isEnd) classes.push('range-end')
        }
      }
      
      return classes
    },
    selectSlot(date, slot) {
      if (this.isPastTime(date, slot)) {
        this.$message.warning('该时间段已过去')
        return
      }
      
      if (!this.isSlotAvailable(date, slot)) {
        this.$message.warning('该时间段已被占用')
        return
      }

      const clickedSlot = { date, slot }
      
      if (!this.selectedRange.start) {
        // 第一次点击，选择单个时间段
        this.selectedRange.start = clickedSlot
        this.selectedRange.end = clickedSlot
        this.emitSelection()
      } else {
        // 已有选择，检查是否可以扩展为连续时间段
        const currentSelection = {
          start: this.selectedRange.start,
          end: this.selectedRange.end || this.selectedRange.start
        }
        
        // 计算新的选择范围
        let newStart, newEnd
        
        if (this.isBefore(clickedSlot, currentSelection.start)) {
          // 点击在开始之前，向前扩展
          newStart = clickedSlot
          newEnd = currentSelection.end
        } else if (this.isAfter(clickedSlot, currentSelection.end)) {
          // 点击在结束之后，向后扩展
          newStart = currentSelection.start
          newEnd = clickedSlot
        } else {
          // 点击在范围内，重新开始选择
          this.selectedRange.start = clickedSlot
          this.selectedRange.end = clickedSlot
          this.emitSelection()
          return
        }
        
        // 检查新范围是否连续且可用
        if (this.isRangeAvailable(newStart, newEnd)) {
          this.selectedRange.start = newStart
          this.selectedRange.end = newEnd
          this.emitSelection()
        } else {
          // 如果不能连续，重新开始选择
          this.selectedRange.start = clickedSlot
          this.selectedRange.end = clickedSlot
          this.emitSelection()
        }
      }
    },
    
    isBefore(slot1, slot2) {
      const date1Index = this.weekDays.findIndex(day => day.date === slot1.date)
      const date2Index = this.weekDays.findIndex(day => day.date === slot2.date)
      const slot1Index = this.timeSlots.indexOf(slot1.slot)
      const slot2Index = this.timeSlots.indexOf(slot2.slot)
      
      if (date1Index < date2Index) return true
      if (date1Index > date2Index) return false
      return slot1Index < slot2Index
    },
    isAfter(start, end) {
      const startDateIndex = this.weekDays.findIndex(day => day.date === start.date)
      const endDateIndex = this.weekDays.findIndex(day => day.date === end.date)
      const startSlotIndex = this.timeSlots.indexOf(start.slot)
      const endSlotIndex = this.timeSlots.indexOf(end.slot)
      
      if (startDateIndex < endDateIndex) return false
      if (startDateIndex > endDateIndex) return true
      return startSlotIndex > endSlotIndex
    },
    isRangeAvailable(start, end) {
      const startDateIndex = this.weekDays.findIndex(day => day.date === start.date)
      const endDateIndex = this.weekDays.findIndex(day => day.date === end.date)
      const startSlotIndex = this.timeSlots.indexOf(start.slot)
      const endSlotIndex = this.timeSlots.indexOf(end.slot)
      
      for (let dateIdx = startDateIndex; dateIdx <= endDateIndex; dateIdx++) {
        const date = this.weekDays[dateIdx].date
        const startIdx = dateIdx === startDateIndex ? startSlotIndex : 0
        const endIdx = dateIdx === endDateIndex ? endSlotIndex : this.timeSlots.length - 1
        
        for (let slotIdx = startIdx; slotIdx <= endIdx; slotIdx++) {
          const slot = this.timeSlots[slotIdx]
          if (!this.isSlotAvailable(date, slot)) {
            return false
          }
        }
      }
      
      return true
    },
    formatSelectionRange() {
      if (!this.selectedRange.start) return ''
      
      const start = this.selectedRange.start
      const end = this.selectedRange.end || this.selectedRange.start
      
      const formatTime = (slot) => {
        return slot.split('-')[0] // 只显示开始时间
      }
      
      if (start.date === end.date && start.slot === end.slot) {
        // 单个时间段
        return `${start.date} ${start.slot}`
      } else {
        // 连续时间段
        return `${start.date} ${formatTime(start.slot)} - ${end.date} ${end.slot.split('-')[1]}`
      }
    },
    emitSelection() {
      if (!this.selectedRange.start || !this.selectedRange.end) return
      
      const start = this.selectedRange.start
      const end = this.selectedRange.end
      
      this.$emit('time-selected', {
        startDate: start.date,
        startTime: start.slot.split('-')[0],
        endDate: end.date,
        endTime: end.slot.split('-')[1]
      })
    }
  }
}
</script>

<style scoped>
.interactive-schedule {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.coach-info h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.coach-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.calendar-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}

.time-header {
  display: contents;
}

.time-label {
  padding: 12px;
  text-align: center;
  font-weight: 500;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  background: #f5f7fa;
  font-size: 14px;
  color: #333;
}

.day-header {
  padding: 12px;
  text-align: center;
  font-weight: 500;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  background: #f5f7fa;
}

.day-name {
  font-size: 14px;
  color: #333;
}

.day-date {
  font-size: 12px;
  color: #666;
}

.time-slots {
  display: contents;
}

.time-row {
  display: contents;
}

.time-slot-label {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  font-size: 12px;
  color: #666;
  background: #f5f7fa;
  font-weight: 500;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-slot-cell {
  height: 60px;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

@media (max-width: 768px) {
  .calendar-grid {
    min-width: 700px;
  }
  
  .time-label,
  .time-slot-label {
    padding: 8px;
    font-size: 11px;
  }
  
  .day-header {
    padding: 8px;
    font-size: 12px;
  }
}

.time-slot-cell.available {
  background: #f0f9ff;
}

.time-slot-cell.available:hover {
  background: #d9ecff;
}

.time-slot-cell.occupied {
  background: #fef0f0;
  cursor: not-allowed;
}

.time-slot-cell.past {
  background: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.6;
}

.time-slot-cell.selected {
  background: #409eff;
  color: white;
}

.time-slot-cell.selected:hover {
  background: #66b1ff;
}

.time-slot-cell.range-start {
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}

.time-slot-cell.range-end {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}

.slot-content {
  text-align: center;
}

.course-info {
  font-size: 12px;
}

.course-type {
  color: #f56c6c;
  font-weight: 500;
}

.selected-indicator {
  font-size: 16px;
}

.selection-info {
  margin: 20px 0;
}

.legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.legend-color.available {
  background: #f0f9ff;
  border: 1px solid #d9ecff;
}

.legend-color.occupied {
  background: #fef0f0;
  border: 1px solid #fab6b6;
}

.legend-color.selected {
  background: #409eff;
}

@media (max-width: 768px) {
  .schedule-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .calendar-grid {
    min-width: 600px;
  }
}
</style>