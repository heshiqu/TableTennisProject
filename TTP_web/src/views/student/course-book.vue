<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>预约课程</span>
      </div>
      
      <!-- 步骤条 -->
      <el-steps :active="activeStep" finish-status="success" style="margin-bottom: 30px;">
        <el-step title="选择教练"></el-step>
        <el-step title="选择时间"></el-step>
        <el-step title="选择球台"></el-step>
        <el-step title="确认预约"></el-step>
      </el-steps>

      <!-- 步骤1：选择教练 -->
      <div v-if="activeStep === 0">
        <el-card>
          <div slot="header">
            <span>选择我的教练</span>
          </div>
          <el-radio-group v-model="selectedCoach" class="coach-radio-group">
            <el-radio 
              v-for="coach in myCoaches" 
              :key="coach.id" 
              :label="coach"
              class="coach-radio-item">
              <div class="coach-name-display">
                {{ coach.realName || coach.username }}
              </div>
            </el-radio>
          </el-radio-group>
          <el-empty v-if="myCoaches.length === 0" description="暂无教练，请先选择教练"></el-empty>
        </el-card>
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="primary" @click="nextStep" :disabled="!selectedCoach">下一步</el-button>
        </div>
      </div>

      <!-- 步骤2：选择时间 -->
      <div v-if="activeStep === 1">
        <el-card>
          <div slot="header">
            <span>选择时间</span>
          </div>
          <interactive-coach-schedule
            :coach-id="selectedCoach.id"
            :coach-name="selectedCoach.realName"
            @time-selected="handleTimeSelected" />
        </el-card>

        <div style="text-align: center; margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep" :disabled="!selectedTimeRange">下一步</el-button>
        </div>
      </div>

      <!-- 步骤3：选择球台 -->
      <div v-if="activeStep === 2">
        <el-card>
          <div slot="header">
            <span>选择球台</span>
            <span style="float: right; color: #999;">
              {{ selectedTimeRange.startDate }} {{ selectedTimeRange.startTime }} - {{ selectedTimeRange.endTime }}
            </span>
          </div>
          <div v-loading="loadingTables" element-loading-text="正在检查球台可用性...">
            <el-row :gutter="20">
              <el-col :span="6" v-for="table in availableTables" :key="table.id">
                <el-card 
                  :body-style="{ padding: '15px', textAlign: 'center', minHeight: '120px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }"
                  :class="[{ 'selected-card': selectedTable && selectedTable.id === table.id }, 
                           { 'disabled-card': table.status !== 'AVAILABLE' }]"
                  @click.native="selectTable(table)">
                  <div class="table-item">
                    <div class="table-number">球台编号：{{ table.courtNumber || table.name }}</div>
                    <div class="table-status" :class="table.status">
                      <span :class="getStatusClass(table.status)">{{ table.status | statusFilter }}</span>
                      <div v-if="table.status === 'OCCUPIED' && table.availableInTimeRange === false" 
                           class="status-hint">
                        时段占用
                      </div>
                      <div v-else-if="table.status === 'MAINTENANCE'" 
                           class="status-hint">
                        维护中
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-if="availableTables.length === 0" description="暂无可用的球台"></el-empty>
            <div style="text-align: center; margin: 20px 0;">
              <el-button type="info" @click="autoAssignTable" :disabled="!hasAvailableTable()">系统自动分配</el-button>
            </div>
          </div>
        </el-card>

        <div style="text-align: center; margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep" :disabled="!selectedTable">下一步</el-button>
        </div>
      </div>

      <!-- 步骤4：确认预约 -->
      <div v-if="activeStep === 3">
        <el-card>
          <div slot="header">
            <span>确认预约信息</span>
          </div>
          <div style="background: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
              <h3 style="margin: 0; color: #333;">预约确认</h3>
              <div style="font-size: 24px; font-weight: bold; color: #ff6b6b;">
                ¥{{ calculateFee() }}
              </div>
            </div>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
              <div style="background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <div style="font-size: 12px; color: #666; margin-bottom: 5px;">教练名称</div>
                <div style="font-size: 16px; font-weight: 600; color: #333;">{{ selectedCoach.realName }}</div>
              </div>
              
              <div style="background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <div style="font-size: 12px; color: #666; margin-bottom: 5px;">球台编号</div>
                <div style="font-size: 16px; font-weight: 600; color: #333;">{{ selectedTable.courtNumber || selectedTable.name }}</div>
              </div>
              
              <div style="background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <div style="font-size: 12px; color: #666; margin-bottom: 5px;">课程日期</div>
                <div style="font-size: 16px; font-weight: 600; color: #333;">{{ selectedTimeRange.startDate }}</div>
              </div>
              
              <div style="background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <div style="font-size: 12px; color: #666; margin-bottom: 5px;">课程时段</div>
                <div style="font-size: 16px; font-weight: 600; color: #333;">{{ selectedTimeRange.startTime }} - {{ selectedTimeRange.endTime }}</div>
              </div>
            </div>
          </div>
          
          <el-alert
            title="预约说明"
            type="info"
            description="预约成功后，需等待教练确认。教练确认后，费用将从您的账户中扣除。如需取消，请提前24小时操作。"
            :closable="false"
            style="margin-top: 20px;">
          </el-alert>
        </el-card>

        <div style="text-align: center; margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="confirmBooking">确认预约</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getStudentApprovedCoaches, getCoachDetail } from '@/api/student'
import { getCoachSchedule, getAvailableTables, bookCourse } from '@/api/course'
import request from '@/utils/request'
import InteractiveCoachSchedule from './InteractiveCoachSchedule.vue'

export default {
  components: {
    InteractiveCoachSchedule
  },
  name: 'CourseBook',
  data() {
    return {
      activeStep: 0,
      myCoaches: [],
      selectedCoach: null,
      selectedDate: new Date(),
      selectedSlot: null,
      selectedTable: null,
      selectedTimeRange: null,
      availableSlots: [],
      availableTables: [],
      loadingTables: false,
      defaultAvatar: (process.env.VUE_APP_BASE_API || 'http://localhost:8080') + '/uploads/avatars/default-avatar.jpg',
      calendarRange: [new Date(), new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)]
    }
  },
  filters: {
    levelFilter(level) {
      const levelMap = {
        'SENIOR': '高级教练',
        'INTERMEDIATE': '中级教练',
        'JUNIOR': '初级教练'
      }
      return levelMap[level] || level
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
  },
  filters: {
    statusFilter(status) {
      const statusMap = {
        'AVAILABLE': '可用',
        'OCCUPIED': '占用',
        'MAINTENANCE': '维修'
      }
      return statusMap[status] || status
    }
  },
  created() {
    this.loadMyCoaches()
  },
  methods: {
    // 通过校区ID获取球台数据
    async getCourtsByCampus(campusId) {
      try {
        const response = await request({
          url: `/api/courts/campus/${campusId}`,
          method: 'get'
        })
        return response.data || []
      } catch (error) {
        console.error('获取球台数据失败:', error)
        this.$message.error('获取球台数据失败')
        return []
      }
    },

    // 检查球台在指定时间段是否可用
    async checkCourtAvailability(courtId, startTime, endTime) {
      try {
        // 将时间格式转换为ISO 8601格式（2025-09-16T20:00:00）
        const formatDateTime = (dateTimeStr) => {
          const [date, time] = dateTimeStr.split(' ')
          return `${date}T${time}:00`
        }
        
        const response = await request({
          url: '/api/courses/check-court-time-conflict',
          method: 'get',
          params: {
            courtId: courtId,
            startTime: formatDateTime(startTime),
            endTime: formatDateTime(endTime)
          }
        })
        // 返回false表示球台空闲，true表示球台被占用
        return !response.data
      } catch (error) {
        console.error('检查球台可用性失败:', error)
        return false // 默认返回不可用，避免错误选择
      }
    },
    
    async loadMyCoaches() {
      try {
        const studentId = this.$store.state.user.user?.id
        if (!studentId) {
          this.$message.error('无法获取学生信息')
          return
        }
        
        // 获取已批准的教练关系
        const response = await getStudentApprovedCoaches(studentId)
        const coachRelations = response.data || []
        
        // 获取教练详细信息
        const coachPromises = coachRelations.map(async relation => {
          try {
            const coachResponse = await getCoachDetail(relation.coachId)
            return coachResponse.data
          } catch (error) {
            console.error(`获取教练 ${relation.coachId} 详情失败:`, error)
            return null
          }
        })
        
        const coaches = await Promise.all(coachPromises)
        this.myCoaches = coaches.filter(coach => coach !== null)
        
      } catch (error) {
        console.error('获取教练列表失败:', error)
        this.$message.error('获取教练列表失败')
      }
    },
    async loadAvailableSlots() {
      if (!this.selectedCoach || !this.selectedDate) return
      
      try {
        const dateStr = this.formatDateForAPI(this.selectedDate)
        const response = await getCoachSchedule(this.selectedCoach.id, dateStr)
        this.availableSlots = response.data || []
      } catch (error) {
        console.error('获取可用时段失败:', error)
      }
    },
    async loadAvailableTables() {
      if (!this.selectedTimeRange) return
      
      this.loadingTables = true
      try {
        // 获取当前用户的校区ID
        const campusId = this.$store.state.user.user?.campusId
        if (!campusId) {
          this.$message.error('无法获取用户校区信息')
          return
        }
        
        // 通过校区ID获取球台数据
        const courts = await this.getCourtsByCampus(campusId)
        
        // 检查每个球台在选定时间段的可用性
        const startDateTime = `${this.selectedTimeRange.startDate} ${this.selectedTimeRange.startTime}`
        const endDateTime = `${this.selectedTimeRange.startDate} ${this.selectedTimeRange.endTime}`
        
        // 并行检查所有球台的可用性
        const courtsWithAvailability = await Promise.all(
          courts.map(async (court) => {
            // 跳过维修状态的球台
            if (court.status === 'MAINTENANCE') {
              return court
            }
            
            // 检查球台在选定时间段是否可用
            const isAvailable = await this.checkCourtAvailability(
              court.id,
              startDateTime,
              endDateTime
            )
            
            return {
              ...court,
              // 如果球台在选定时间段被占用，则标记为占用状态
              status: isAvailable ? court.status : 'OCCUPIED',
              // 添加可用性标记，用于前端显示
              availableInTimeRange: isAvailable
            }
          })
        )
        
        this.availableTables = courtsWithAvailability || []
        console.log('获取到的球台数据（含可用性检查）:', this.availableTables)
      } catch (error) {
        console.error('获取球台数据失败:', error)
        this.$message.error('获取球台数据失败')
      } finally {
        this.loadingTables = false
      }
    },
    isDateAvailable(date) {
      // 这里应该根据教练的日程判断是否可用
      return true
    },
    getAvailableSlots(date) {
      // 这里应该返回该日期的可用时段
      return this.availableSlots.filter(slot => 
        new Date(slot.date).toDateString() === date.toDateString()
      )
    },
    selectSlot(slot) {
      this.selectedSlot = slot
      this.selectedTable = null
      this.loadAvailableTables()
    },
    selectTable(table) {
      if (table.status === 'AVAILABLE') {
        this.selectedTable = table
      } else {
        this.$message.warning(`球台 ${table.courtNumber || table.name} 当前${this.$options.filters.statusFilter(table.status)}，无法选择`)
      }
    },
    autoAssignTable() {
      // 获取所有可用的球台
      const availableTables = this.availableTables.filter(table => table.status === 'AVAILABLE')
      
      if (availableTables.length > 0) {
        // 随机选择一个可用的球台
        const randomIndex = Math.floor(Math.random() * availableTables.length)
        const selectedTable = availableTables[randomIndex]
        
        this.selectedTable = selectedTable
        this.$message.success(`已为您自动分配球台: ${selectedTable.courtNumber || selectedTable.name}`)
      } else {
        this.$message.warning('暂无可用球台')
      }
    },
    
    hasAvailableTable() {
      return this.availableTables.some(table => table.status === 'AVAILABLE')
    },
    
    getStatusClass(status) {
      return {
        'status-available': status === 'AVAILABLE',
        'status-occupied': status === 'OCCUPIED',
        'status-maintenance': status === 'MAINTENANCE'
      }
    },
    calculateFee() {
      console.log('教练数据:', this.selectedCoach)
      console.log('时间数据:', this.selectedTimeRange)
      
      if (!this.selectedCoach || !this.selectedTimeRange || 
          !this.selectedTimeRange.startTime || !this.selectedTimeRange.endTime) {
        console.log('数据不完整，返回0')
        return 0
      }
      
      // 根据代码分析，教练价格字段应该是hourlyRate
      const priceField = this.selectedCoach.hourlyRate || 100
      
      console.log('价格字段hourlyRate:', priceField)
      
      try {
        const duration = this.calculateDuration(this.selectedTimeRange.startTime, this.selectedTimeRange.endTime)
        const price = parseFloat(priceField) || 100
        const total = Math.round(price * duration * 100) / 100
        console.log('计算结果:', {duration, price, total})
        return total
      } catch (error) {
        console.error('计算费用出错:', error)
        return 100
      }
    },
    calculateDuration(startTime, endTime) {
      // 解析时间格式为小时和分钟
      const [startHour, startMinute] = startTime.split(':').map(Number)
      const [endHour, endMinute] = endTime.split(':').map(Number)
      
      // 计算总分钟数差
      const startTotalMinutes = startHour * 60 + startMinute
      const endTotalMinutes = endHour * 60 + endMinute
      
      // 计算小时数
      const duration = (endTotalMinutes - startTotalMinutes) / 60
      
      return duration > 0 ? duration : 0
    },
    formatDateForAPI(date) {
      return new Date(date).toISOString().split('T')[0]
    },
    formatDateTime(dateTimeStr) {
      const date = new Date(dateTimeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    handleTimeSelected(timeRange) {
      this.selectedTimeRange = timeRange
      this.selectedTable = null
      // 不在选择时间后立即加载球台，等到进入选择球台步骤时再加载
    },
    nextStep() {
      this.activeStep++
      // 当进入选择球台步骤时加载球台数据
      if (this.activeStep === 2) {
        this.loadAvailableTables()
      }
    },
    prevStep() {
      this.activeStep--
    },
    async confirmBooking() {
      try {
        await this.$confirm(
          `确认预约 ${this.selectedDate.toLocaleDateString('zh-CN')} ${this.selectedTimeRange.startTime} - ${this.selectedTimeRange.endTime} 的课程吗？`, 
          '确认预约', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        // 构建UTC时间字符串，直接使用用户选择的本地时间作为UTC时间
        const startDate = new Date(this.selectedTimeRange.startDate)
        const [startHour, startMinute] = this.selectedTimeRange.startTime.split(':').map(Number)
        const [endHour, endMinute] = this.selectedTimeRange.endTime.split(':').map(Number)
        
        // 创建UTC时间的Date对象（将本地时间视为UTC时间）
        const startDateTime = new Date(Date.UTC(
          startDate.getFullYear(), 
          startDate.getMonth(), 
          startDate.getDate(), 
          startHour, 
          startMinute
        )).toISOString()
        const endDateTime = new Date(Date.UTC(
          startDate.getFullYear(), 
          startDate.getMonth(), 
          startDate.getDate(), 
          endHour, 
          endMinute
        )).toISOString()

        const bookingData = {
          coachId: this.selectedCoach.id,
          studentId: this.$store.state.user.user?.id,
          courtId: this.selectedTable.id,
          startTime: startDateTime,
          endTime: endDateTime
        }

        // 使用新的API格式直接发送到 /api/courses
        const response = await request({
          url: '/api/courses',
          method: 'post',
          data: bookingData
        })

        if (response.code === 200) {
          this.$message.success('课程预约成功！')
          
          // 显示详细的预约信息
          const courseInfo = response.data
          this.$notify({
            title: '预约成功',
            message: `
              <div>
                <p><strong>教练：</strong>${courseInfo.coachName}</p>
                <p><strong>球台：</strong>${courseInfo.courtNumber}</p>
                <p><strong>时间：</strong>${this.formatDateTime(courseInfo.startTime)} - ${this.formatDateTime(courseInfo.endTime)}</p>
                <p><strong>费用：</strong>¥${courseInfo.fee}</p>
                <p><strong>状态：</strong>等待教练确认</p>
              </div>
            `,
            type: 'success',
            dangerouslyUseHTMLString: true,
            duration: 5000
          })
        } else {
          throw new Error(response.message || '预约失败')
        }

        this.resetBooking()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '预约失败')
        }
      }
    },
    resetBooking() {
      this.activeStep = 0
      this.selectedCoach = null
      this.selectedDate = new Date()
      this.selectedSlot = null
      this.selectedTable = null
      this.selectedTimeRange = null
    }
  },

}
</script>

<style scoped>
.coach-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coach-radio-item {
  display: block;
  margin-right: 0;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  transition: all 0.3s;
}

.coach-radio-item:hover {
  border-color: #409EFF;
}

.coach-radio-item.is-checked {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.coach-name-display {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.selected-card {
  border: 2px solid #409EFF;
}

.coach-item {
  text-align: center;
  cursor: pointer;
}

.coach-info h4 {
  margin: 10px 0 5px 0;
}

.coach-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.available-date {
  background-color: #f0f9ff;
  border-radius: 4px;
  padding: 4px;
}

.unavailable-date {
  opacity: 0.5;
}

.slot-count {
  font-size: 12px;
  color: #409EFF;
  margin-top: 2px;
}

.time-slot-btn {
  width: 100%;
  margin-bottom: 10px;
}

.table-item {
  cursor: pointer;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.table-number {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  line-height: 1.2;
}

.table-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 4px;
}

.status-hint {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  line-height: 1.2;
}

.table-status.AVAILABLE {
  background-color: #67C23A;
  color: white;
}

.table-status.OCCUPIED {
  background-color: #F56C6C;
  color: white;
}

.table-status.MAINTENANCE {
  background-color: #E6A23C;
  color: white;
}

.status-available {
  color: #67C23A;
  font-weight: bold;
}

.status-occupied {
  color: #F56C6C;
  font-weight: bold;
}

.status-maintenance {
  color: #E6A23C;
  font-weight: bold;
}

.el-card[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 统一卡片高度 */
.el-card {
  height: 100%;
  transition: all 0.3s ease;
}

.el-card__body {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>