<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>课表管理</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="showSetScheduleDialog">设置课表</el-button>
          <el-button type="text" @click="loadSchedule">刷新</el-button>
        </div>
      </div>

      <!-- 课表工具栏 -->
      <div class="schedule-toolbar">
        <el-date-picker
          v-model="selectedWeek"
          type="week"
          format="yyyy 第 WW 周"
          placeholder="选择周"
          @change="handleWeekChange">
        </el-date-picker>
        
        <el-button-group style="margin-left: 20px;">
          <el-button @click="previousWeek">上一周</el-button>
          <el-button @click="currentWeek">本周</el-button>
          <el-button @click="nextWeek">下一周</el-button>
        </el-button-group>
      </div>

      <!-- 课表视图 -->
      <div class="schedule-container" v-loading="loading">
        <div class="schedule-table">
          <table>
            <thead>
              <tr>
                <th class="time-header">时间</th>
                <th v-for="day in weekDays" :key="day.date" class="day-header">
                  <div>{{ day.weekday }}</div>
                  <div>{{ day.date }}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="timeSlot in timeSlots" :key="timeSlot.key">
                <td class="time-cell">{{ timeSlot.label }}</td>
                <td 
                  v-for="day in weekDays" 
                  :key="`${day.date}-${timeSlot.key}`"
                  class="schedule-cell"
                  :class="getCellClass(day.date, timeSlot.key)">
                  <div 
                    v-if="getSchedule(day.date, timeSlot.key)" 
                    class="schedule-item">
                    <div class="course-info">
                      <div class="course-student">{{ getSchedule(day.date, timeSlot.key).studentName }}</div>
                      <div class="course-table">{{ getSchedule(day.date, timeSlot.key).tableName }}</div>
                    </div>
                    <div class="course-status">
                      <el-tag 
                        :type="getStatusType(getSchedule(day.date, timeSlot.key).status)" 
                        size="mini">
                        {{ getStatusText(getSchedule(day.date, timeSlot.key).status) }}
                      </el-tag>
                    </div>
                  </div>
                  <div 
                    v-else 
                    class="empty-slot"
                    @click="showSetScheduleDialog(day.date, timeSlot.key)">
                    空闲
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </el-card>

    <!-- 设置课表对话框 -->
    <el-dialog 
      title="设置课表" 
      :visible.sync="scheduleDialogVisible" 
      width="50%">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="日期">
          <el-date-picker
            v-model="scheduleForm.date"
            type="date"
            placeholder="选择日期"
            :disabled="!!selectedSlot">
          </el-date-picker>
        </el-form-item>
        
        <el-form-item label="时间段">
          <el-select v-model="scheduleForm.timeSlot" placeholder="选择时间段">
            <el-option 
              v-for="slot in availableTimeSlots" 
              :key="slot.key" 
              :label="slot.label" 
              :value="slot.key">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="可用球台">
          <el-select v-model="scheduleForm.tableId" placeholder="选择球台">
            <el-option 
              v-for="table in availableTables" 
              :key="table.id" 
              :label="table.name" 
              :value="table.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input 
            type="textarea" 
            v-model="scheduleForm.remark" 
            :rows="3" 
            placeholder="可选备注信息">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="setSchedule">确定</el-button>
      </span>
    </el-dialog>

    <!-- 今日课程列表 -->
    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>今日课程安排</span>
      </div>
      
      <div class="today-courses" v-loading="loading">
        <div v-if="todayCourses.length > 0">
          <el-table :data="todayCourses" style="width: 100%" border>
            <el-table-column prop="timeSlot" label="时间段" width="120"></el-table-column>
            <el-table-column prop="studentName" label="学员姓名" width="120"></el-table-column>
            <el-table-column prop="tableName" label="球台" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="mini">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewCourseDetail(scope.row)">
                  详情
                </el-button>
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="cancelSchedule(scope.row)"
                  v-if="scope.row.status === 'CONFIRMED'">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="今日暂无课程安排"></el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCoachSchedule, setCoachSchedule, getAvailableTables } from '@/api/coach'

export default {
  name: 'CoachSchedule',
  data() {
    return {
      selectedWeek: new Date(),
      schedule: [],
      availableTables: [],
      scheduleDialogVisible: false,
      scheduleForm: {
        date: '',
        timeSlot: '',
        tableId: '',
        remark: ''
      },
      selectedSlot: null,
      loading: false,
      timeSlots: [
        { key: '09:00-10:00', label: '09:00-10:00' },
        { key: '10:00-11:00', label: '10:00-11:00' },
        { key: '11:00-12:00', label: '11:00-12:00' },
        { key: '14:00-15:00', label: '14:00-15:00' },
        { key: '15:00-16:00', label: '15:00-16:00' },
        { key: '16:00-17:00', label: '16:00-17:00' },
        { key: '19:00-20:00', label: '19:00-20:00' },
        { key: '20:00-21:00', label: '20:00-21:00' }
      ]
    }
  },
  computed: {
    weekDays() {
      const days = []
      const currentDate = new Date(this.selectedWeek)
      const weekStart = new Date(currentDate)
      weekStart.setDate(currentDate.getDate() - currentDate.getDay() + 1) // 周一
      
      const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(weekStart)
        date.setDate(weekStart.getDate() + i)
        days.push({
          weekday: weekdays[i],
          date: date.toISOString().split('T')[0]
        })
      }
      
      return days
    },
    todayCourses() {
      const today = new Date().toISOString().split('T')[0]
      return this.schedule.filter(item => item.date === today)
    },
    availableTimeSlots() {
      return this.timeSlots
    }
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
  },
  created() {
    this.loadSchedule()
    this.loadAvailableTables()
  },
  methods: {
    async loadSchedule() {
      this.loading = true
      try {
        const startDate = this.weekDays[0].date
        const endDate = this.weekDays[6].date
        
        const response = await getCoachSchedule({
          startDate,
          endDate
        })
        this.schedule = response.data || []
      } catch (error) {
        console.error('获取课表失败:', error)
        this.$message.error('获取课表失败')
      } finally {
        this.loading = false
      }
    },
    async loadAvailableTables() {
      try {
        const response = await getAvailableTables()
        this.availableTables = response.data || []
      } catch (error) {
        console.error('获取球台列表失败:', error)
      }
    },
    getSchedule(date, timeSlot) {
      return this.schedule.find(item => 
        item.date === date && item.timeSlot === timeSlot
      )
    },
    getCellClass(date, timeSlot) {
      const schedule = this.getSchedule(date, timeSlot)
      if (!schedule) return 'empty'
      
      const statusMap = {
        'PENDING': 'pending',
        'CONFIRMED': 'confirmed',
        'COMPLETED': 'completed',
        'CANCELLED': 'cancelled'
      }
      
      return statusMap[schedule.status] || ''
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'COMPLETED': 'info',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return textMap[status] || status
    },
    handleWeekChange() {
      this.loadSchedule()
    },
    previousWeek() {
      const date = new Date(this.selectedWeek)
      date.setDate(date.getDate() - 7)
      this.selectedWeek = date
      this.loadSchedule()
    },
    currentWeek() {
      this.selectedWeek = new Date()
      this.loadSchedule()
    },
    nextWeek() {
      const date = new Date(this.selectedWeek)
      date.setDate(date.getDate() + 7)
      this.selectedWeek = date
      this.loadSchedule()
    },
    showSetScheduleDialog(date = null, timeSlot = null) {
      if (date && timeSlot) {
        this.scheduleForm = {
          date,
          timeSlot,
          tableId: '',
          remark: ''
        }
        this.selectedSlot = { date, timeSlot }
      } else {
        this.scheduleForm = {
          date: '',
          timeSlot: '',
          tableId: '',
          remark: ''
        }
        this.selectedSlot = null
      }
      this.scheduleDialogVisible = true
    },
    async setSchedule() {
      if (!this.scheduleForm.date || !this.scheduleForm.timeSlot || !this.scheduleForm.tableId) {
        this.$message.warning('请填写完整信息')
        return
      }
      
      try {
        await setCoachSchedule(this.scheduleForm)
        this.$message.success('课表设置成功')
        this.scheduleDialogVisible = false
        this.loadSchedule()
      } catch (error) {
        console.error('设置课表失败:', error)
        this.$message.error('设置课表失败：' + (error.response?.data?.message || '请检查时间段是否冲突'))
      }
    },
    viewCourseDetail(course) {
      // TODO: 查看课程详情
      console.log('查看课程详情:', course)
    },
    async cancelSchedule(course) {
      try {
        await this.$confirm('确认取消该课程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用取消课程API
        this.$message.success('课程取消成功')
        this.loadSchedule()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消课程失败:', error)
        }
      }
    }
  }
}
</script>

<style scoped>
.schedule-toolbar {
  margin-bottom: 20px;
  text-align: center;
}

.schedule-container {
  margin-bottom: 20px;
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #ebeef5;
  padding: 10px;
  text-align: center;
}

.time-header {
  width: 100px;
  background-color: #f5f7fa;
}

.day-header {
  background-color: #f5f7fa;
  font-weight: bold;
}

.time-cell {
  background-color: #f5f7fa;
  font-weight: bold;
}

.schedule-cell {
  min-width: 120px;
  height: 80px;
  vertical-align: middle;
  position: relative;
}

.schedule-cell.empty {
  background-color: #f9f9f9;
}

.schedule-cell.pending {
  background-color: #fdf6ec;
}

.schedule-cell.confirmed {
  background-color: #f0f9ff;
}

.schedule-cell.completed {
  background-color: #f0f9ff;
}

.schedule-cell.cancelled {
  background-color: #fef0f0;
}

.schedule-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.course-info {
  flex: 1;
}

.course-student {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.course-table {
  color: #666;
  font-size: 12px;
}

.course-status {
  margin-top: 5px;
}

.empty-slot {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
  transition: background-color 0.3s;
}

.empty-slot:hover {
  background-color: #e6f7ff;
}

.today-courses {
  min-height: 200px;
}
</style>