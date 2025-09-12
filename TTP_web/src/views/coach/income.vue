<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>收入统计</span>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="income-card">
            <div class="income-item">
              <div class="income-label">今日收入</div>
              <div class="income-amount">¥{{ todayIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="income-card">
            <div class="income-item">
              <div class="income-label">本周收入</div>
              <div class="income-amount">¥{{ weekIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="income-card">
            <div class="income-item">
              <div class="income-label">本月收入</div>
              <div class="income-amount">¥{{ monthIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="income-card">
            <div class="income-item">
              <div class="income-label">总收入</div>
              <div class="income-amount">¥{{ totalIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>收入明细</span>
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
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="incomeDetails"
          border
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="studentName" label="学员姓名" />
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="duration" label="时长(小时)" width="100" />
          <el-table-column prop="hourlyRate" label="单价(元/小时)" width="120" />
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template slot-scope="{row}">
              <span style="color: #67C23A; font-weight: bold;">
                ¥{{ row.totalAmount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="{row}">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
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

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>月度收入统计</span>
            </div>
            <div class="chart-container">
              <canvas ref="monthlyChart" width="400" height="300"></canvas>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>收入来源分布</span>
            </div>
            <div class="chart-container">
              <canvas ref="sourceChart" width="400" height="300"></canvas>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getIncomeStats, getIncomeDetails, exportIncome } from '@/api/coach'
import Pagination from '@/components/Pagination'

export default {
  name: 'CoachIncome',
  components: { Pagination },
  data() {
    return {
      todayIncome: 0,
      weekIncome: 0,
      monthIncome: 0,
      totalIncome: 0,
      incomeDetails: [],
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      }
    }
  },
  created() {
    this.getIncomeStats()
    this.getList()
    this.initCharts()
  },
  methods: {
    async getIncomeStats() {
      try {
        const response = await getIncomeStats()
        const data = response.data
        this.todayIncome = data.todayIncome || 0
        this.weekIncome = data.weekIncome || 0
        this.monthIncome = data.monthIncome || 0
        this.totalIncome = data.totalIncome || 0
      } catch (error) {
        console.error('获取收入统计失败:', error)
        this.$message.error('获取收入统计失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getIncomeDetails(params)
        this.incomeDetails = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取收入明细失败:', error)
        this.$message.error('获取收入明细失败')
      } finally {
        this.loading = false
      }
    },
    initCharts() {
      // 模拟图表数据
      this.$nextTick(() => {
        this.drawMonthlyChart()
        this.drawSourceChart()
      })
    },
    drawMonthlyChart() {
      const canvas = this.$refs.monthlyChart
      if (canvas) {
        const ctx = canvas.getContext('2d')
        // 这里应该使用实际的图表库，如ECharts或Chart.js
        // 暂时用简单的矩形图表示
        ctx.fillStyle = '#f0f0f0'
        ctx.fillRect(0, 0, 400, 300)
        ctx.fillStyle = '#333'
        ctx.font = '14px Arial'
        ctx.fillText('月度收入图表', 150, 150)
      }
    },
    drawSourceChart() {
      const canvas = this.$refs.sourceChart
      if (canvas) {
        const ctx = canvas.getContext('2d')
        // 这里应该使用实际的图表库，如ECharts或Chart.js
        // 暂时用简单的矩形图表示
        ctx.fillStyle = '#f0f0f0'
        ctx.fillRect(0, 0, 400, 300)
        ctx.fillStyle = '#333'
        ctx.font = '14px Arial'
        ctx.fillText('收入来源分布图', 150, 150)
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getIncomeStats()
      this.getList()
    },
    async handleExport() {
      try {
        const params = {
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportIncome(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    getStatusType(status) {
      const types = {
        'COMPLETED': 'success',
        'PENDING': 'warning',
        'CANCELLED': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'COMPLETED': '已完成',
        'PENDING': '待支付',
        'CANCELLED': '已取消'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.income-card {
  text-align: center;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.income-item {
  padding: 20px 0;
}

.income-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.income-amount {
  font-size: 24px;
  font-weight: bold;
  color: #67C23A;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-container > * {
  margin-right: 10px;
  margin-bottom: 10px;
}

.chart-container {
  text-align: center;
  padding: 20px;
}
</style>