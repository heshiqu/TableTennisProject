<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>我的课表</span>
        <div style="float: right;">
          <el-date-picker
            v-model="selectedWeek"
            type="week"
            format="yyyy 第 WW 周"
            placeholder="选择周"
            @change="handleWeekChange"
            style="margin-right: 10px;">
          </el-date-picker>
          <el-button type="primary" @click="loadSchedule">刷新</el-button>
        </div>
      </div>

      <!-- 周视图 -->
      <div class="week-view" v-loading="loading">
        <div class="week-header">
          <div class="time-slot"></div>
          <div class="day-header" v-for="day in weekDays" :key="day.date">
            <div class="day-name">{{ day.name }}</div>
            <div class="day-date">{{ day.date | formatDay }}</div>
          </div>
        </div>

        <div class="week-body">
          <div class="time-slots">
            <div class="time-slot" v-for="time in timeSlots" :key="time">
              {{ time }}
            </div>
          </div>

          <div class="day-columns">
            <div class="day-column" v-for="day in weekDays" :key="day.date">
              <div class="day-cell" v-for="time in timeSlots" :key="time">
                <div 
                  v-if="getCourseAt(day.date, time)" 
                  class="course-item"
                  :class="getCourseStatus(getCourseAt(day.date, time))"
                  @click="handleCourseDetail(getCourseAt(day.date, time))">
                  <div class="course-time">{{ time }}</div>
                  <div class="course-coach">{{ getCourseAt(day.date, time).coachName }}</div>
                  <div class="course-table">{{ getCourseAt(day.date, time).tableName }}</div>
                  <div class="course-status">{{ getCourseAt(day.date, time).status | statusFilter }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 课程列表 -->
      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>课程列表</span>
          <el-radio-group v-model="listType" @change="handleListTypeChange" style="float: right;">
            <el-radio-button label="upcoming">即将开始</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>

        <el-table :data="courseList" style="width: 100%" v-loading="loading">
          <el-table-column prop="date" label="日期" width="120">
            <template slot-scope="scope">
              {{ scope.row.date | formatDate }}
            </template>
          </el-table-column>
          <el-table-column prop="time" label="时间" width="120">
            <template slot-scope="scope">
              {{ scope.row.startTime }} - {{ scope.row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="coachName" label="教练" width="120"></el-table-column>
          <el-table-column prop="tableName" label="球台" width="100"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status | statusType">{{ scope.row.status | statusFilter }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="mini" @click="handleCourseDetail(scope.row)">详情</el-button>
              <el-button 
                size="mini" 
                type="danger" 
                @click="handleCancelCourse(scope.row)"
                :disabled="!canCancel(scope.row)">
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="page.current"
          :page-sizes="[10, 20, 50]"
          :page-size="page.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="text-align: center; margin-top: 20px;">
        </el-pagination>
      </el-card>
    </el-card>

    <!-- 已取消课程列表 -->
    <el-card v-if="cancelledCourses.length > 0" style="margin-top: 20px;">
      <div slot="header">
        <span>本周已取消课程</span>
        <el-tag type="danger" style="margin-left: 10px;">{{ cancelledCourses.length }} 节</el-tag>
      </div>
      
      <el-table :data="cancelledCourses" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120">
          <template slot-scope="scope">
            {{ new Date(scope.row.startTime).toLocaleDateString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.startTime | formatTime }} - {{ scope.row.endTime | formatTime }}
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" width="120"></el-table-column>
        <el-table-column prop="tableName" label="球台" width="100"></el-table-column>
        <el-table-column prop="fee" label="费用" width="80">
          <template slot-scope="scope">
            {{ scope.row.fee }}元
          </template>
        </el-table-column>
        <el-table-column prop="cancelReason" label="取消原因" width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.cancelReason">{{ scope.row.cancelReason }}</span>
            <span v-else>未填写原因</span>
          </template>
        </el-table-column>
        <el-table-column prop="cancelByUserName" label="取消人" width="100">
          <template slot-scope="scope">
            {{ scope.row.cancelByUserName || '系统' }}
          </template>
        </el-table-column>
        <el-table-column prop="cancelTime" label="取消时间" width="150">
          <template slot-scope="scope">
            {{ new Date(scope.row.cancelTime).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleCourseDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog title="课程详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedCourse">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日期">{{ selectedCourse.date | formatDate }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ selectedCourse.startTime }} - {{ selectedCourse.endTime }}</el-descriptions-item>
          <el-descriptions-item label="教练">{{ selectedCourse.coachName }}</el-descriptions-item>
          <el-descriptions-item label="球台">{{ selectedCourse.tableName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedCourse.status | statusType">{{ selectedCourse.status | statusFilter }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="费用">{{ selectedCourse.fee }}元</el-descriptions-item>
        </el-descriptions>

        <el-divider></el-divider>

        <div v-if="selectedCourse.status === 'CONFIRMED'">
          <h4>上课提醒</h4>
          <el-alert
            title="课程即将开始"
            type="info"
            :description="getCourseReminder(selectedCourse)"
            :closable="false"
            show-icon>
          </el-alert>
        </div>

        <div v-if="selectedCourse.status === 'COMPLETED'">
          <h4>课程评价</h4>
          <div v-if="selectedCourse.evaluation">
            <p><strong>学员评价：</strong>{{ selectedCourse.evaluation.studentComment }}</p>
            <p><strong>教练评价：</strong>{{ selectedCourse.evaluation.coachComment }}</p>
            <p><strong>评分：</strong>
              <el-rate v-model="selectedCourse.evaluation.rating" disabled></el-rate>
            </p>
          </div>
          <div v-else>
            <p>暂无评价</p>
            <el-button type="primary" size="small" @click="handleEvaluate(selectedCourse)">去评价</el-button>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          type="danger" 
          @click="handleCancelCourse(selectedCourse)"
          :disabled="!canCancel(selectedCourse)"
          v-if="selectedCourse && ['PENDING', 'CONFIRMED'].includes(selectedCourse.status)">
          取消课程
        </el-button>
      </span>
    </el-dialog>

    <!-- 取消课程对话框 -->
    <el-dialog title="取消课程" :visible.sync="cancelDialogVisible" width="30%">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-select v-model="cancelForm.reason" placeholder="请选择取消原因">
            <el-option label="临时有事" value="BUSY"></el-option>
            <el-option label="身体不适" value="HEALTH"></el-option>
            <el-option label="其他原因" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" v-if="cancelForm.reason === 'OTHER'">
          <el-input type="textarea" v-model="cancelForm.description" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancelCourse">确认取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentSchedule, getStudentCourses, cancelCourse } from '@/api/course'

export default {
  name: 'StudentSchedule',
  data() {
    return {
      selectedWeek: new Date(),
      weekDays: [],
      timeSlots: [
        '08:00', '09:00', '10:00', '11:00',
        '14:00', '15:00', '16:00', '17:00',
        '19:00', '20:00', '21:00'
      ],
      schedule: [],
      courseList: [],
      cancelledCourses: [], // 已取消课程列表
      listType: 'upcoming',
      selectedCourse: null,
      detailDialogVisible: false,
      cancelDialogVisible: false,
      cancelForm: {
        reason: '',
        description: ''
      },
      loading: false,
      page: {
        current: 1,
        size: 10
      },
      total: 0
    }
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    formatDay(date) {
      if (!date) return ''
      return new Date(date).getDate()
    },
    formatTime(datetime) {
      if (!datetime) return ''
      const date = new Date(datetime)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    statusFilter(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || status
    },
    statusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'COMPLETED': 'info',
        'CANCELLED': 'danger',
        'REJECTED': 'danger'
      }
      return typeMap[status] || ''
    }
  },
  created() {
    this.initWeekDays()
    this.loadSchedule()
    this.loadCourseList()
  },
  methods: {
    initWeekDays() {
      const today = new Date(this.selectedWeek)
      const startOfWeek = new Date(today)
      startOfWeek.setDate(today.getDate() - today.getDay())
      
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      this.weekDays = []
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(startOfWeek)
        date.setDate(startOfWeek.getDate() + i)
        this.weekDays.push({
          name: days[i],
          date: date
        })
      }
    },
    async loadSchedule() {
      this.loading = true
      try {
        const startDate = this.weekDays[0]?.date
        const endDate = this.weekDays[6]?.date
        
        if (startDate && endDate) {
          const response = await getStudentSchedule({
            startDate: startDate.toISOString().split('T')[0],
            endDate: endDate.toISOString().split('T')[0]
          })
          const allCourses = response.data || []
          // 过滤掉已取消的课程，只显示非取消状态的课程在课表中
          this.schedule = allCourses.filter(course => course.status !== 'CANCELLED')
          // 单独保存已取消的课程用于下方展示
          this.cancelledCourses = allCourses.filter(course => course.status === 'CANCELLED')
        }
      } catch (error) {
        console.error('获取课表失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadCourseList() {
      this.loading = true
      try {
        const response = await getStudentCourses({
          status: this.listType.toUpperCase(),
          current: this.page.current,
          size: this.page.size
        })
        this.courseList = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取课程列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    getCourseAt(date, time) {
      const dateStr = date.toISOString().split('T')[0]
      
      return this.schedule.find(course => {
        if (course.date !== dateStr) return false
        
        // 处理连续时间段的课程
        if (course.startTime && course.endTime) {
          const [timeHour, timeMinute] = time.split(':').map(Number)
          const timeInMinutes = timeHour * 60 + timeMinute
          
          const courseStart = new Date(course.startTime)
          const courseEnd = new Date(course.endTime)
          
          const courseStartInMinutes = courseStart.getHours() * 60 + courseStart.getMinutes()
          const courseEndInMinutes = courseEnd.getHours() * 60 + courseEnd.getMinutes()
          
          // 检查时间段是否重叠
          return timeInMinutes >= courseStartInMinutes && timeInMinutes < courseEndInMinutes
        }
        
        // 兼容旧的时间格式
        return course.startTime === time
      })
    },
    getCourseStatus(course) {
      return course.status.toLowerCase()
    },
    getCourseReminder(course) {
      const courseDate = new Date(course.date + ' ' + course.startTime)
      const now = new Date()
      const diffHours = (courseDate - now) / (1000 * 60 * 60)
      
      if (diffHours < 1) {
        return `课程将在 ${Math.floor(diffHours * 60)} 分钟后开始，请提前到达球馆`
      } else {
        return `课程将在 ${Math.floor(diffHours)} 小时后开始`
      }
    },
    handleWeekChange() {
      this.initWeekDays()
      this.loadSchedule()
    },
    handleListTypeChange() {
      this.page.current = 1
      this.loadCourseList()
    },
    handleCourseDetail(course) {
      this.selectedCourse = course
      this.detailDialogVisible = true
    },
    handleCancelCourse(course) {
      if (!this.canCancel(course)) {
        this.$message.warning('该课程无法取消')
        return
      }
      this.selectedCourse = course
      this.cancelDialogVisible = true
    },
    canCancel(course) {
      if (!['PENDING', 'CONFIRMED'].includes(course.status)) {
        return false
      }
      
      const courseDate = new Date(course.date + ' ' + course.startTime)
      const now = new Date()
      const diffHours = (courseDate - now) / (1000 * 60 * 60)
      
      return diffHours >= 24
    },
    async confirmCancelCourse() {
      if (!this.selectedCourse) return
      
      try {
        await cancelCourse(this.selectedCourse.id, this.cancelForm)
        this.$message.success('取消课程申请已提交')
        this.cancelDialogVisible = false
        this.loadSchedule()
        this.loadCourseList()
      } catch (error) {
        this.$message.error(error.message || '取消失败')
      }
    },
    handleEvaluate(course) {
      this.$router.push({
        name: 'CourseEvaluation',
        query: { courseId: course.id }
      })
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadCourseList()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadCourseList()
    }
  }
}
</script>

<style scoped>
.week-view {
  border: 1px solid #eee;
  border-radius: 4px;
}

.week-header {
  display: flex;
  background-color: #f5f5f5;
  border-bottom: 1px solid #eee;
}

.time-slot {
  width: 80px;
  border-right: 1px solid #eee;
}

.day-header {
  flex: 1;
  text-align: center;
  padding: 10px;
  border-right: 1px solid #eee;
}

.day-header:last-child {
  border-right: none;
}

.day-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.day-date {
  font-size: 12px;
  color: #666;
}

.week-body {
  display: flex;
}

.time-slots {
  width: 80px;
  border-right: 1px solid #eee;
}

.time-slots .time-slot {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #eee;
  font-size: 12px;
  color: #666;
}

.day-columns {
  display: flex;
  flex: 1;
}

.day-column {
  flex: 1;
  border-right: 1px solid #eee;
}

.day-column:last-child {
  border-right: none;
}

.day-cell {
  height: 60px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.course-item {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  border-radius: 4px;
  padding: 4px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s;
}

.course-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.course-item.pending {
  background-color: #fdf6ec;
  border: 1px solid #e6a23c;
  color: #e6a23c;
}

.course-item.confirmed {
  background-color: #f0f9ff;
  border: 1px solid #409eff;
  color: #409eff;
}

.course-item.completed {
  background-color: #f0f9ff;
  border: 1px solid #67c23a;
  color: #67c23a;
}

.course-item.cancelled {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
  color: #f56c6c;
}

.course-time {
  font-weight: bold;
  margin-bottom: 2px;
}

.course-coach {
  margin-bottom: 2px;
}

.course-table {
  font-size: 10px;
  opacity: 0.8;
}

.course-status {
  font-size: 10px;
  margin-top: 2px;
}

@media screen and (max-width: 768px) {
  .week-view {
    overflow-x: auto;
  }
  
  .week-header,
  .week-body {
    min-width: 600px;
  }
}
</style>