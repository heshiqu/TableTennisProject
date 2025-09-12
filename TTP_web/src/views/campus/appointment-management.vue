<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预约管理</span>
      </div>
      
      <div class="filter-container">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="handleFilter"
        />
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleFilter">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select v-model="coachFilter" placeholder="教练" clearable @change="handleFilter">
          <el-option
            v-for="coach in coaches"
            :key="coach.id"
            :label="coach.realName"
            :value="coach.id"
          />
        </el-select>
        <el-button type="primary" @click="handleFilter">查询</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="appointments"
        border
        style="width: 100%"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="time" label="时间" width="120" />
        <el-table-column prop="studentName" label="学员姓名" />
        <el-table-column prop="coachName" label="教练姓名" />
        <el-table-column prop="duration" label="训练时长" width="100">
          <template slot-scope="{row}">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="tableNumber" label="球台号" width="80" />
        <el-table-column prop="fee" label="课时费" width="100">
          <template slot-scope="{row}">
            ¥{{ row.fee }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="cancelAppointment(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedAppointment" class="appointment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约编号">{{ selectedAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedAppointment.createTime }}</el-descriptions-item>
          <el-descriptions-item label="学员姓名">{{ selectedAppointment.studentName }}</el-descriptions-item>
          <el-descriptions-item label="教练姓名">{{ selectedAppointment.coachName }}</el-descriptions-item>
          <el-descriptions-item label="训练日期">{{ selectedAppointment.date }}</el-descriptions-item>
          <el-descriptions-item label="训练时间">{{ selectedAppointment.time }}</el-descriptions-item>
          <el-descriptions-item label="训练时长">{{ selectedAppointment.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="球台号">{{ selectedAppointment.tableNumber }}</el-descriptions-item>
          <el-descriptions-item label="课时费">¥{{ selectedAppointment.fee }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(selectedAppointment.status) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedAppointment.cancelInfo" class="cancel-info">
          <h4>取消信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="取消人">{{ selectedAppointment.cancelInfo.cancelBy }}</el-descriptions-item>
            <el-descriptions-item label="取消时间">{{ selectedAppointment.cancelInfo.cancelTime }}</el-descriptions-item>
            <el-descriptions-item label="取消原因">{{ selectedAppointment.cancelInfo.reason }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCampusAppointments, getCampusCoaches, cancelAppointment } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'AppointmentManagement',
  components: { Pagination },
  data() {
    return {
      appointments: [],
      coaches: [],
      dateRange: [],
      statusFilter: '',
      coachFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      selectedAppointment: null,
      detailDialogVisible: false
    }
  },
  created() {
    this.loadCoaches()
    this.getList()
  },
  methods: {
    async loadCoaches() {
      try {
        const response = await getCampusCoaches()
        this.coaches = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || '',
          status: this.statusFilter || '',
          coachId: this.coachFilter || ''
        }
        
        const response = await getCampusAppointments(params)
        this.appointments = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取预约列表失败:', error)
        this.$message.error('获取预约列表失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
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
    viewDetail(row) {
      this.selectedAppointment = row
      this.detailDialogVisible = true
    },
    async cancelAppointment(row) {
      try {
        await this.$confirm(`确定要取消学员 ${row.studentName} 的预约吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const reason = await this.$prompt('请输入取消原因', '取消预约', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^.{2,}$/,
          inputErrorMessage: '请输入至少2个字符'
        })
        
        await cancelAppointment(row.id, reason.value)
        this.$message.success('取消成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消预约失败:', error)
          this.$message.error('取消失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.filter-container > * {
  margin-right: 10px;
  margin-bottom: 10px;
}

.cancel-info {
  margin-top: 20px;
}

.cancel-info h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>