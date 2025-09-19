<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>财务管理</span>
      </div>
      
      <!-- 财务概览卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card>
            <div class="stat-card">
              <div class="stat-title">总收入</div>
              <div class="stat-value">¥{{ overview.totalIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-card">
              <div class="stat-title">总支出</div>
              <div class="stat-value">¥{{ overview.totalExpense.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-card">
              <div class="stat-title">净利润</div>
              <div class="stat-value" :class="{ 'positive': overview.netProfit >= 0, 'negative': overview.netProfit < 0 }">
                ¥{{ overview.netProfit.toFixed(2) }}
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-card">
              <div class="stat-title">总交易数</div>
              <div class="stat-value">{{ overview.totalTransactions }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选条件 -->
      <div class="filter-container">
        <el-select v-model="campusFilter" placeholder="所属校区" clearable @change="handleFilter">
          <el-option
            v-for="campus in campuses"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
        <el-select v-model="typeFilter" placeholder="交易类型" clearable @change="handleFilter">
          <el-option label="课程费用" value="COURSE" />
          <el-option label="预约费用" value="APPOINTMENT" />
          <el-option label="比赛费用" value="TOURNAMENT" />
          <el-option label="充值" value="RECHARGE" />
          <el-option label="退款" value="REFUND" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="交易状态" clearable @change="handleFilter">
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="处理中" value="PENDING" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <!-- 财务数据表格 -->
      <el-table
        v-loading="loading"
        :data="financeData"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="campusName" label="所属校区" width="120" />
        <el-table-column prop="studentName" label="学员姓名" width="120" />
        <el-table-column prop="coachName" label="教练姓名" width="120" />
        <el-table-column prop="type" label="交易类型" width="100">
          <template slot-scope="{row}">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="{row}">
            <span :class="{ 'positive': row.amount >= 0, 'negative': row.amount < 0 }">
              {{ row.amount >= 0 ? '+' : '' }}¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="handleProcess(row)">处理</el-button>
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

    <!-- 财务详情对话框 -->
    <el-dialog title="交易详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ selectedTransaction.id }}</el-descriptions-item>
        <el-descriptions-item label="所属校区">{{ selectedTransaction.campusName }}</el-descriptions-item>
        <el-descriptions-item label="学员姓名">{{ selectedTransaction.studentName }}</el-descriptions-item>
        <el-descriptions-item label="教练姓名">{{ selectedTransaction.coachName || '无' }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ getTypeText(selectedTransaction.type) }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">
          <span :class="{ 'positive': selectedTransaction.amount >= 0, 'negative': selectedTransaction.amount < 0 }">
            {{ selectedTransaction.amount >= 0 ? '+' : '' }}¥{{ selectedTransaction.amount.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="交易状态">
          <el-tag :type="getStatusType(selectedTransaction.status)">
            {{ getStatusText(selectedTransaction.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedTransaction.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ selectedTransaction.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="交易描述" :span="2">{{ selectedTransaction.description }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ selectedTransaction.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 处理交易对话框 -->
    <el-dialog title="处理交易" :visible.sync="processDialogVisible" width="30%">
      <el-form ref="processForm" :model="processForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.action">
            <el-radio label="APPROVE">通过</el-radio>
            <el-radio label="REJECT">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="processForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleProcessSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllFinanceStats, getAllFinanceData, processTransaction, exportFinanceData } from '@/api/super-admin'
import { getCampuses } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'FinanceManagement',
  components: { Pagination },
  data() {
    return {
      overview: {
        totalIncome: 0,
        totalExpense: 0,
        netProfit: 0,
        totalTransactions: 0
      },
      financeData: [],
      campuses: [],
      campusFilter: '',
      typeFilter: '',
      statusFilter: '',
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      processDialogVisible: false,
      selectedTransaction: {},
      processForm: {
        action: 'APPROVE',
        remark: ''
      }
    }
  },
  created() {
    this.getOverview()
    this.getList()
    this.loadCampuses()
  },
  methods: {
    async getOverview() {
      try {
        const params = {
          campusId: this.campusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getAllFinanceStats(params)
        this.overview = response.data || {
          totalIncome: 0,
          totalExpense: 0,
          netProfit: 0,
          totalTransactions: 0
        }
      } catch (error) {
        console.error('获取财务概览失败:', error)
        this.$message.error('获取财务概览失败')
      }
    },
    
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          campusId: this.campusFilter,
          type: this.typeFilter,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getAllFinanceData(params)
        this.financeData = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取财务数据失败:', error)
        this.$message.error('获取财务数据失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadCampuses() {
      try {
        const response = await getCampuses()
        this.campuses = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    
    handleFilter() {
      this.listQuery.page = 1
      this.getOverview()
      this.getList()
    },
    
    handleViewDetail(row) {
      this.selectedTransaction = row
      this.detailDialogVisible = true
    },
    
    handleProcess(row) {
      this.selectedTransaction = row
      this.processForm = {
        action: 'APPROVE',
        remark: ''
      }
      this.processDialogVisible = true
    },
    
    async handleProcessSubmit() {
      try {
        await processTransaction(this.selectedTransaction.id, this.processForm)
        this.$message.success('处理成功')
        this.processDialogVisible = false
        this.getOverview()
        this.getList()
      } catch (error) {
        console.error('处理失败:', error)
        this.$message.error('处理失败')
      }
    },
    
    async handleExport() {
      try {
        const params = {
          campusId: this.campusFilter,
          type: this.typeFilter,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportFinanceData(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    
    getTypeText(type) {
      const typeMap = {
        'COURSE': '课程费用',
        'APPOINTMENT': '预约费用',
        'TOURNAMENT': '比赛费用',
        'RECHARGE': '充值',
        'REFUND': '退款'
      }
      return typeMap[type] || type
    },
    
    getStatusType(status) {
      const statusMap = {
        'COMPLETED': 'success',
        'PENDING': 'warning',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || ''
    },
    
    getStatusText(status) {
      const statusMap = {
        'COMPLETED': '已完成',
        'PENDING': '处理中',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
}
.positive {
  color: #67C23A;
}
.negative {
  color: #F56C6C;
}
.filter-container {
  padding-bottom: 10px;
}
.filter-container .el-select,
.filter-container .el-date-picker {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>