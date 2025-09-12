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
            <span>我的教练</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="8" v-for="coach in myCoaches" :key="coach.id">
              <el-card :body-style="{ padding: '15px' }" 
                       :class="{ 'selected-card': selectedCoach && selectedCoach.id === coach.id }"
                       @click.native="selectCoach(coach)">
                <div class="coach-item">
                  <el-avatar :size="60" :src="coach.avatar || defaultAvatar"></el-avatar>
                  <div class="coach-info">
                    <h4>{{ coach.realName }}</h4>
                    <p>{{ coach.level | levelFilter }}</p>
                    <p>{{ coach.price }}元/小时</p>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
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
            <span>选择日期</span>
          </div>
          <el-calendar v-model="selectedDate" :range="calendarRange">
            <template slot="dateCell" slot-scope="{date, data}">
              <div :class="{ 'available-date': isDateAvailable(date), 'unavailable-date': !isDateAvailable(date) }">
                <span>{{ data.day.split('-').slice(2).join('-') }}</span>
                <div v-if="getAvailableSlots(date).length > 0" class="slot-count">
                  {{ getAvailableSlots(date).length }}个时段
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>

        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>选择时间段</span>
            <span style="float: right; color: #999;">{{ selectedDate | formatDate }}</span>
          </div>
          <el-row :gutter="10">
            <el-col :span="6" v-for="slot in availableSlots" :key="slot.id">
              <el-button 
                :type="selectedSlot && selectedSlot.id === slot.id ? 'primary' : 'default'"
                @click="selectSlot(slot)"
                :disabled="!slot.available"
                class="time-slot-btn">
                {{ slot.startTime }} - {{ slot.endTime }}
              </el-button>
            </el-col>
          </el-row>
          <el-empty v-if="availableSlots.length === 0" description="该日期无可选时段"></el-empty>
        </el-card>

        <div style="text-align: center; margin-top: 20px;">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep" :disabled="!selectedSlot">下一步</el-button>
        </div>
      </div>

      <!-- 步骤3：选择球台 -->
      <div v-if="activeStep === 2">
        <el-card>
          <div slot="header">
            <span>选择球台</span>
            <span style="float: right; color: #999;">
              {{ selectedDate | formatDate }} {{ selectedSlot.startTime }} - {{ selectedSlot.endTime }}
            </span>
          </div>
          <el-row :gutter="20">
            <el-col :span="6" v-for="table in availableTables" :key="table.id">
              <el-card 
                :body-style="{ padding: '15px', textAlign: 'center' }"
                :class="{ 'selected-card': selectedTable && selectedTable.id === table.id }"
                @click.native="selectTable(table)">
                <div class="table-item">
                  <div class="table-number">{{ table.name }}</div>
                  <div class="table-status" :class="table.status">{{ table.status | statusFilter }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div style="text-align: center; margin: 20px 0;">
            <el-button type="info" @click="autoAssignTable">系统自动分配</el-button>
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
          <el-descriptions :column="2" border>
            <el-descriptions-item label="教练">{{ selectedCoach.realName }}</el-descriptions-item>
            <el-descriptions-item label="等级">{{ selectedCoach.level | levelFilter }}</el-descriptions-item>
            <el-descriptions-item label="日期">{{ selectedDate | formatDate }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ selectedSlot.startTime }} - {{ selectedSlot.endTime }}</el-descriptions-item>
            <el-descriptions-item label="球台">{{ selectedTable.name }}</el-descriptions-item>
            <el-descriptions-item label="费用">{{ calculateFee() }}元</el-descriptions-item>
          </el-descriptions>
          
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
import { getMyCoaches } from '@/api/student'
import { getCoachSchedule, getAvailableTables, bookCourse } from '@/api/course'

export default {
  name: 'CourseBook',
  data() {
    return {
      activeStep: 0,
      myCoaches: [],
      selectedCoach: null,
      selectedDate: new Date(),
      selectedSlot: null,
      selectedTable: null,
      availableSlots: [],
      availableTables: [],
      defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
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
    },
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
    async loadMyCoaches() {
      try {
        const response = await getMyCoaches()
        this.myCoaches = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    selectCoach(coach) {
      this.selectedCoach = coach
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
      if (!this.selectedSlot) return
      
      try {
        const dateStr = this.formatDateForAPI(this.selectedDate)
        const response = await getAvailableTables(dateStr, this.selectedSlot.startTime, this.selectedSlot.endTime)
        this.availableTables = response.data || []
      } catch (error) {
        console.error('获取可用球台失败:', error)
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
      this.selectedTable = table
    },
    autoAssignTable() {
      const availableTable = this.availableTables.find(table => table.status === 'AVAILABLE')
      if (availableTable) {
        this.selectedTable = availableTable
        this.$message.success('已为您自动分配球台')
      } else {
        this.$message.warning('暂无可用球台')
      }
    },
    calculateFee() {
      if (!this.selectedCoach || !this.selectedSlot) return 0
      const duration = this.calculateDuration(this.selectedSlot.startTime, this.selectedSlot.endTime)
      return this.selectedCoach.price * duration
    },
    calculateDuration(startTime, endTime) {
      const start = new Date(`2000/01/01 ${startTime}`)
      const end = new Date(`2000/01/01 ${endTime}`)
      return (end - start) / (1000 * 60 * 60)
    },
    formatDateForAPI(date) {
      return new Date(date).toISOString().split('T')[0]
    },
    nextStep() {
      if (this.activeStep === 1) {
        this.loadAvailableSlots()
      }
      this.activeStep++
    },
    prevStep() {
      this.activeStep--
    },
    async confirmBooking() {
      try {
        await this.$confirm('确认预约该课程吗？', '确认预约', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const bookingData = {
          coachId: this.selectedCoach.id,
          date: this.formatDateForAPI(this.selectedDate),
          startTime: this.selectedSlot.startTime,
          endTime: this.selectedSlot.endTime,
          tableId: this.selectedTable.id
        }

        await bookCourse(bookingData)
        this.$message.success('预约成功，请等待教练确认')
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
    }
  },
  watch: {
    selectedDate() {
      if (this.activeStep === 1) {
        this.loadAvailableSlots()
      }
    }
  }
}
</script>

<style scoped>
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
}

.table-number {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.table-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
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
</style>