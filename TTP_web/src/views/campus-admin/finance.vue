<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>财务管理</span>
      </div>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card class="finance-card">
            <div class="finance-item">
              <div class="finance-label">今日收入</div>
              <div class="finance-amount">¥{{ todayIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="finance-card">
            <div class="finance-item">
              <div class="finance-label">本周收入</div>
              <div class="finance-amount">¥{{ weekIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="finance-card">
            <div class="finance-item">
              <div class="finance-label">本月收入</div>
              <div class="finance-amount">¥{{ monthIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="finance-card">
            <div class="finance-item">
              <div class="finance-label">总收入</div>
              <div class="finance-amount">¥{{ totalIncome.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-bottom: 20px;">
        <div slot="header">
          <span>财务明细</span>
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
            v-model="listQuery.type"
            placeholder="交易类型"
            clearable
            style="width: 120px"
            class="filter-item"
          >
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
          <el-button
            v-waves
            class="filter-item"
            type="primary"
            icon="el-icon-search"
            @click="handleFilter"
          >
            查询
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
          v-loading="loading"
          :data="financeDetails"
          border
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="type" label="类型" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'">
                {{ row.type === 'INCOME' ? '收入' : '支出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template slot-scope="{row}">
              <span :style="{color: row.type === 'INCOME' ? '#67C23A' : '#F56C6C'}">
                {{ row.type === 'INCOME' ? '+' : '-' }}¥{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="120">
            <template slot-scope="{row}">
              <span>¥{{ row.balance.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="120" />
        </el-table>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="listQuery.page"
          :limit.sync="listQuery.limit"
          @pagination="getList"
        />
      </el-card>

      <el-row :gutter="20">
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
import { getFinanceStats, getFinanceDetails, exportFinance } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusFinance',
  components: { Pagination },
  data() {
    return {
      todayIncome: 0,
      weekIncome: 0,
      monthIncome: 0,
      totalIncome: 0,
      financeDetails: [],
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20,
        type: undefined
      }
    }
  },
  created() {
    this.getFinanceStats()
    this.getList()
    this.initCharts()
  },
  methods: {
    async getFinanceStats() {
      try {
        const response = await getFinanceStats()
        const data = response.data
        this.todayIncome = data.todayIncome || 0
        this.weekIncome = data.weekIncome || 0
        this.monthIncome = data.monthIncome || 0
        this.totalIncome = data.totalIncome || 0
      } catch (error) {
        console.error('获取财务统计失败:', error)
        this.$message.error('获取财务统计失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          ...this.listQuery,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getFinanceDetails(params)
        this.financeDetails = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取财务明细失败:', error)
        this.$message.error('获取财务明细失败')
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
        ctx.fillText('月度财务图表', 150, 150)
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
      this.getFinanceStats()
      this.getList()
    },
    async handleExport() {
      try {
        const params = {
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null,
          type: this.listQuery.type
        }
        
        await exportFinance(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    }
  }
}
</script>

<style scoped>
.finance-card {
  text-align: center;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.finance-item {
  padding: 20px 0;
}

.finance-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.finance-amount {
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