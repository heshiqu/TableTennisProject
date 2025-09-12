<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>待处理预约</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="loadAppointments">刷新</el-button>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" clearable placeholder="选择状态">
              <el-option label="待确认" value="PENDING"></el-option>
              <el-option label="已确认" value="CONFIRMED"></el-option>
              <el-option label="已拒绝" value="REJECTED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 预约统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ appointmentStats.pending }}</div>
              <div class="stat-label">待确认</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ appointmentStats.confirmed }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ appointmentStats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ appointmentStats.cancelled }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 预约列表 -->
      <div class="appointment-list" v-loading="loading">
        <div v-if="appointments.length > 0">
          <el-table :data="appointments" style="width: 100%" border>
            <el-table-column prop="studentName" label="学员姓名" width="120"></el-table-column>
            <el-table-column prop="studentPhone" label="联系电话" width="120"></el-table-column>
            <el-table-column prop="date" label="日期" width="120">
              <template slot-scope="scope">
                {{ scope.row.date | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="timeSlot" label="时间段" width="120"></el-table-column>
            <el-table-column prop="tableName" label="球台" width="100"></el-table-column>
            <el-table-column prop="fee" label="课时费" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.fee }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="mini">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="预约时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.createTime | formatDateTime }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <template v-if="scope.row.status === 'PENDING'">
                  <el-button 
                    type="success" 
                    size="mini" 
                    @click="confirmAppointment(scope.row)">
                    接受
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="mini" 
                    @click="rejectAppointment(scope.row)">
                    拒绝
                  </el-button>
                </template>
                <template v-else>
                  <el-button 
                    type="text" 
                    size="mini" 
                    @click="viewAppointmentDetail(scope.row)">
                    查看详情
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无预约"></el-empty>
      </div>

      <!-- 分页 -->
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

    <!-- 预约详情对话框 -->
    <el-dialog 
      title="预约详情" 
      :visible.sync="detailDialogVisible" 
      width="50%">
      <div v-if="selectedAppointment" class="appointment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学员姓名">{{ selectedAppointment.studentName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedAppointment.studentPhone }}</el-descriptions-item>
          <el-descriptions-item label="学员邮箱">{{ selectedAppointment.studentEmail || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="学员年龄">{{ selectedAppointment.studentAge }}岁</el-descriptions-item>
          
          <el-descriptions-item label="预约日期">{{ selectedAppointment.date | formatDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ selectedAppointment.timeSlot }}</el-descriptions-item>
          <el-descriptions-item label="球台">{{ selectedAppointment.tableName }}</el-descriptions-item>
          <el-descriptions-item label="课时费">¥{{ selectedAppointment.fee }}</el-descriptions-item>
          
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedAppointment.status)" size="mini">
              {{ getStatusText(selectedAppointment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ selectedAppointment.createTime | formatDateTime }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedAppointment.studentNote" class="detail-note">
          <h4>学员备注：</h4>
          <p>{{ selectedAppointment.studentNote }}</p>
        </div>
        
        <div v-if="selectedAppointment.rejectReason" class="detail-reason">
          <h4>拒绝原因：</h4>
          <p>{{ selectedAppointment.rejectReason }}</p>
        </div>
      </div>
    </el-dialog>

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
import { getCoachAppointments, confirmAppointment, rejectAppointment } from '@/api/coach'

export default {
  name: 'CoachAppointments',
  data() {
    return {
      appointments: [],
      selectedAppointment: null,
      detailDialogVisible: false,
      rejectDialogVisible: false,
      loading: false,
      filterForm: {
        status: '',
        dateRange: []
      },
      rejectForm: {
        appointmentId: '',
        reason: ''
      },
      page: {
        current: 1,
        size: 10
      },
      total: 0,
      appointmentStats: {
        pending: 0,
        confirmed: 0,
        rejected: 0,
        cancelled: 0
      }
    }
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  },
  created() {
    this.loadAppointments()
  },
  methods: {
    async loadAppointments() {
      this.loading = true
      try {
        const params = {
          current: this.page.current,
          size: this.page.size,
          status: this.filterForm.status,
          startDate: this.filterForm.dateRange?.[0] || '',
          endDate: this.filterForm.dateRange?.[1] || ''
        }
        
        const response = await getCoachAppointments(params)
        this.appointments = response.data.records || []
        this.total = response.data.total || 0
        
        // 计算统计信息
        this.calculateStats()
      } catch (error) {
        console.error('获取预约列表失败:', error)
        this.$message.error('获取预约列表失败')
      } finally {
        this.loading = false
      }
    },
    calculateStats() {
      this.appointmentStats = {
        pending: this.appointments.filter(a => a.status === 'PENDING').length,
        confirmed: this.appointments.filter(a => a.status === 'CONFIRMED').length,
        rejected: this.appointments.filter(a => a.status === 'REJECTED').length,
        cancelled: this.appointments.filter(a => a.status === 'CANCELLED').length
      }
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'REJECTED': 'danger',
        'CANCELLED': 'info'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消'
      }
      return textMap[status] || status
    },
    handleFilter() {
      this.page.current = 1
      this.loadAppointments()
    },
    resetFilter() {
      this.filterForm = {
        status: '',
        dateRange: []
      }
      this.handleFilter()
    },
    viewAppointmentDetail(appointment) {
      this.selectedAppointment = appointment
      this.detailDialogVisible = true
    },
    async confirmAppointment(appointment) {
      try {
        await this.$confirm('确认接受该预约吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        })
        
        await confirmAppointment(appointment.id)
        this.$message.success('预约已确认')
        this.loadAppointments()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('确认预约失败:', error)
          this.$message.error('确认预约失败')
        }
      }
    },
    rejectAppointment(appointment) {
      this.rejectForm = {
        appointmentId: appointment.id,
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
        await rejectAppointment({
          appointmentId: this.rejectForm.appointmentId,
          reason: this.rejectForm.reason
        })
        
        this.$message.success('预约已拒绝')
        this.rejectDialogVisible = false
        this.loadAppointments()
      } catch (error) {
        console.error('拒绝预约失败:', error)
        this.$message.error('拒绝预约失败')
      }
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadAppointments()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadAppointments()
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