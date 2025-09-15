<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>预约审核</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="loadPendingCourses">刷新</el-button>
        </div>
      </div>

      <!-- 待审核课程列表 -->
      <div class="appointment-list" v-loading="loading">
        <div v-if="appointments.length > 0">
          <el-table :data="appointments" style="width: 100%" border>
            <el-table-column prop="studentName" label="学员姓名" min-width="100"></el-table-column>
            <el-table-column prop="courtNumber" label="球台" min-width="80"></el-table-column>
            <el-table-column prop="startTime" label="开始时间" min-width="150">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" min-width="150">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长" min-width="70">
              <template slot-scope="scope">
                {{ scope.row.duration }}h
              </template>
            </el-table-column>
            <el-table-column prop="fee" label="费用" min-width="80">
              <template slot-scope="scope">
                ¥{{ scope.row.fee }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="80">
              <template slot-scope="scope">
                <el-tag type="warning" size="mini">待审核</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="预约时间" min-width="150">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="160">
              <template slot-scope="scope">
                <el-button 
                  type="success" 
                  size="mini" 
                  @click="confirmCourse(scope.row)">
                  同意
                </el-button>
                <el-button 
                  type="danger" 
                  size="mini" 
                  @click="rejectCourse(scope.row)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else>
          <div slot="description">
            <h3>暂无待审核的预约课程</h3>
            <p>当前没有学员提交的课程预约需要审核。</p>
            <p>当学员预约您的课程后，新的预约将显示在这里。</p>
          </div>
        </el-empty>
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog 
      title="拒绝预约" 
      :visible.sync="rejectDialogVisible" 
      width="30%">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input 
            type="textarea" 
            v-model="rejectForm.reason" 
            :rows="3" 
            placeholder="请输入拒绝原因">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getCoachPendingCourses, coachConfirmCourse, coachRejectCourse } from '@/api/coach'

export default {
  name: 'CoachAppointments',
  data() {
    return {
      appointments: [],
      rejectDialogVisible: false,
      rejectForm: {
        courseId: '',
        reason: ''
      },
      loading: false
    }
  },
  created() {
    this.loadPendingCourses()
  },
  methods: {
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN') + ' ' + 
             date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    async loadPendingCourses() {
      this.loading = true
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练信息')
          return
        }
        
        const response = await getCoachPendingCourses(coachId)
        this.appointments = response.data || []
      } catch (error) {
        console.error('获取待审核课程失败:', error)
        this.$message.error('获取待审核课程失败')
      } finally {
        this.loading = false
      }
    },
    async confirmCourse(course) {
      try {
        await this.$confirm('确认接受该课程预约吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        })
        
        await coachConfirmCourse(course.id)
        this.$message.success('课程预约已确认')
        this.loadPendingCourses()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('确认课程失败:', error)
          this.$message.error('确认课程失败')
        }
      }
    },
    rejectCourse(course) {
      this.rejectForm = {
        courseId: course.id,
        reason: ''
      }
      this.rejectDialogVisible = true
    },
    async confirmReject() {
      if (!this.rejectForm.reason.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练信息')
          return
        }
        
        await coachRejectCourse(this.rejectForm.courseId, coachId, this.rejectForm.reason)
        
        this.$message.success('课程预约已拒绝')
        this.rejectDialogVisible = false
        this.loadPendingCourses()
      } catch (error) {
        console.error('拒绝课程失败:', error)
        this.$message.error('拒绝课程失败')
      }
    }
  }
}
</script>

<style scoped>
.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.appointment-list {
  min-height: 400px;
}

.appointment-detail {
  padding: 10px;
}

.detail-note,
.detail-reason {
  margin-top: 20px;
}

.detail-note h4,
.detail-reason h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-note p,
.detail-reason p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}
</style>