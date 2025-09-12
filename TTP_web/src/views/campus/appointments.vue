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
          @change="handleFilter"
        />
        <el-select
          v-model="listQuery.status"
          placeholder="状态"
          clearable
          style="width: 120px"
          class="filter-item"
        >
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select
          v-model="listQuery.coachId"
          placeholder="选择教练"
          clearable
          style="width: 150px"
          class="filter-item"
        >
          <el-option
            v-for="item in coachOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-button
          v-waves
          class="filter-item"
          type="primary"
          icon="el-icon-search"
          @click="handleFilter"
        >
          搜索
        </el-button>
        <el-button
          class="filter-item"
          type="info"
          icon="el-icon-download"
          @click="handleExport"
        >
          导出
        </el-button>
      </div>

      <el-table
        v-loading="listLoading"
        :data="list"
        border
        fit
        highlight-current-row
        style="width: 100%;"
      >
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="预约时间" prop="appointmentTime" align="center" />
        <el-table-column label="学员" prop="studentName" align="center" />
        <el-table-column label="教练" prop="coachName" align="center" />
        <el-table-column label="课程类型" prop="courseType" align="center" />
        <el-table-column label="时长(小时)" prop="duration" align="center" width="100" />
        <el-table-column label="费用(元)" align="center" width="100">
          <template slot-scope="{row}">
            <span>¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" align="center" />
        <el-table-column label="操作" align="center" width="200">
          <template slot-scope="{row}">
            <el-button size="mini" @click="handleView(row)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="mini"
              type="success"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="mini"
              type="warning"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      title="预约详情"
      :visible.sync="detailVisible"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="预约时间：">
          <span>{{ currentAppointment.appointmentTime }}</span>
        </el-form-item>
        <el-form-item label="学员：">
          <span>{{ currentAppointment.studentName }}</span>
        </el-form-item>
        <el-form-item label="教练：">
          <span>{{ currentAppointment.coachName }}</span>
        </el-form-item>
        <el-form-item label="课程类型：">
          <span>{{ currentAppointment.courseType }}</span>
        </el-form-item>
        <el-form-item label="时长：">
          <span>{{ currentAppointment.duration }}小时</span>
        </el-form-item>
        <el-form-item label="费用：">
          <span>¥{{ currentAppointment.amount.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="状态：">
          <el-tag :type="getStatusType(currentAppointment.status)">
            {{ getStatusText(currentAppointment.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注：">
          <span>{{ currentAppointment.remark || '无' }}</span>
        </el-form-item>
        <el-form-item label="创建时间：">
          <span>{{ currentAppointment.createTime }}</span>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { getAppointmentList, confirmAppointment, cancelAppointment, exportAppointments } from '@/api/campus'
import { getCoachList } from '@/api/coach'
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusAppointments',
  directives: { waves },
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        startDate: undefined,
        endDate: undefined,
        status: undefined,
        coachId: undefined
      },
      dateRange: [],
      coachOptions: [],
      detailVisible: false,
      currentAppointment: {}
    }
  },
  created() {
    this.getList()
    this.getCoachList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const params = {
          ...this.listQuery,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getAppointmentList(params)
        this.list = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取预约列表失败:', error)
        this.$message.error('获取预约列表失败')
      } finally {
        this.listLoading = false
      }
    },
    async getCoachList() {
      try {
        const response = await getCoachList()
        this.coachOptions = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
        this.$message.error('获取教练列表失败')
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleView(row) {
      this.currentAppointment = row
      this.detailVisible = true
    },
    async handleConfirm(row) {
      try {
        await this.$confirm('确认该预约？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await confirmAppointment(row.id)
        this.$message.success('确认成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('确认预约失败:', error)
          this.$message.error('确认预约失败')
        }
      }
    },
    async handleCancel(row) {
      try {
        await this.$confirm('确认取消该预约？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await cancelAppointment(row.id)
        this.$message.success('取消成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消预约失败:', error)
          this.$message.error('取消预约失败')
        }
      }
    },
    async handleExport() {
      try {
        const params = {
          ...this.listQuery,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportAppointments(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
        'CONFIRMED': 'primary',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}

.filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-left: 10px;
}
</style>