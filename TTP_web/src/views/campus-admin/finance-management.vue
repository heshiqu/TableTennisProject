<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>财务管理</span>
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
        <el-select v-model="typeFilter" placeholder="交易类型" clearable @change="handleFilter">
          <el-option label="课程收入" value="COURSE_INCOME" />
          <el-option label="比赛收入" value="TOURNAMENT_INCOME" />
          <el-option label="退款支出" value="REFUND" />
          <el-option label="其他收入" value="OTHER_INCOME" />
          <el-option label="其他支出" value="OTHER_EXPENSE" />
        </el-select>
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button type="success" @click="exportData">导出数据</el-button>
      </div>

      <div class="finance-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card income">
              <div class="stat-title">本月收入</div>
              <div class="stat-value">¥{{ financeStats.monthlyIncome }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card expense">
              <div class="stat-title">本月支出</div>
              <div class="stat-value">¥{{ financeStats.monthlyExpense }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card profit">
              <div class="stat-title">本月利润</div>
              <div class="stat-value">¥{{ financeStats.monthlyProfit }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card balance">
              <div class="stat-title">账户余额</div>
              <div class="stat-value">¥{{ financeStats.totalBalance }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="交易明细" name="transactions">
          <el-table
            v-loading="loading"
            :data="transactions"
            border
            style="width: 100%"
          >
            <el-table-column prop="date" label="交易日期" width="120" />
            <el-table-column prop="type" label="交易类型" width="120">
              <template slot-scope="{row}">
                <el-tag :type="getTypeType(row.type)">
                  {{ getTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="交易描述" />
            <el-table-column prop="amount" label="金额" width="120">
              <template slot-scope="{row}">
                <span :class="row.amount >= 0 ? 'positive' : 'negative'">
                  {{ row.amount >= 0 ? '+' : '' }}¥{{ Math.abs(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="120">
              <template slot-scope="{row}">
                ¥{{ row.balance }}
              </template>
            </el-table-column>
            <el-table-column prop="relatedUser" label="相关用户" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="月度统计" name="monthly">
          <el-table
            v-loading="loading"
            :data="monthlyStats"
            border
            style="width: 100%"
          >
            <el-table-column prop="month" label="月份" width="120" />
            <el-table-column prop="totalIncome" label="总收入" width="120">
              <template slot-scope="{row}">
                <span class="positive">¥{{ row.totalIncome }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalExpense" label="总支出" width="120">
              <template slot-scope="{row}">
                <span class="negative">¥{{ row.totalExpense }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="netProfit" label="净利润" width="120">
              <template slot-scope="{row}">
                <span :class="row.netProfit >= 0 ? 'positive' : 'negative'">
                  ¥{{ row.netProfit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="courseIncome" label="课程收入" width="120">
              <template slot-scope="{row}">
                ¥{{ row.courseIncome }}
              </template>
            </el-table-column>
            <el-table-column prop="tournamentIncome" label="比赛收入" width="120">
              <template slot-scope="{row}">
                ¥{{ row.tournamentIncome }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script>
import { getFinanceStats, getTransactions, getMonthlyFinanceStats } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'FinanceManagement',
  components: { Pagination },
  data() {
    return {
      activeTab: 'transactions',
      dateRange: [],
      typeFilter: '',
      financeStats: {
        monthlyIncome: 0,
        monthlyExpense: 0,
        monthlyProfit: 0,
        totalBalance: 0
      },
      transactions: [],
      monthlyStats: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      }
    }
  },
  created() {
    this.getStats()
    this.getList()
  },
  methods: {
    async getStats() {
      try {
        const response = await getFinanceStats()
        this.financeStats = response.data || {}
      } catch (error) {
        console.error('获取财务统计失败:', error)
        this.$message.error('获取财务统计失败')
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
          type: this.typeFilter || ''
        }

        if (this.activeTab === 'transactions') {
          const response = await getTransactions(params)
          this.transactions = response.data.records || []
          this.total = response.data.total || 0
        } else {
          const response = await getMonthlyFinanceStats(params)
          this.monthlyStats = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleTabChange() {
      this.listQuery.page = 1
      this.getList()
    },
    getTypeType(type) {
      const typeMap = {
        'COURSE_INCOME': 'success',
        'TOURNAMENT_INCOME': 'success',
        'REFUND': 'danger',
        'OTHER_INCOME': 'info',
        'OTHER_EXPENSE': 'danger'
      }
      return typeMap[type] || 'info'
    },
    getTypeText(type) {
      const textMap = {
        'COURSE_INCOME': '课程收入',
        'TOURNAMENT_INCOME': '比赛收入',
        'REFUND': '退款支出',
        'OTHER_INCOME': '其他收入',
        'OTHER_EXPENSE': '其他支出'
      }
      return textMap[type] || type
    },
    async exportData() {
      try {
        const params = {
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || '',
          type: this.typeFilter || ''
        }
        
        const response = await this.$http({
          url: '/api/campus/finance/export',
          method: 'get',
          params,
          responseType: 'blob'
        })
        
        const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
        const fileName = `财务数据_${new Date().getTime()}.xlsx`
        
        if ('download' in document.createElement('a')) {
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.style.display = 'none'
          link.href = url
          link.setAttribute('download', fileName)
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
        } else {
          navigator.msSaveBlob(blob, fileName)
        }
      } catch (error) {
        console.error('导出数据失败:', error)
        this.$message.error('导出数据失败')
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

.finance-overview {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  color: white;
}

.stat-card.income {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.stat-card.expense {
  background: linear-gradient(135deg, #F56C6C, #F78989);
}

.stat-card.profit {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.stat-card.balance {
  background: linear-gradient(135deg, #909399, #A6A9AD);
}

.stat-title {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.9;
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
</style>