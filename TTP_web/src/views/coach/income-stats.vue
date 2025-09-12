<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>收入统计</span>
      </div>
      
      <div class="filter-container">
        <el-date-picker
          v-model="dateRange"
          type="monthrange"
          range-separator="至"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
          value-format="yyyy-MM"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button type="success" @click="exportData">导出数据</el-button>
      </div>

      <div class="stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-title">本月收入</div>
              <div class="stat-value">¥{{ stats.monthIncome }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-title">本月课时</div>
              <div class="stat-value">{{ stats.monthHours }}小时</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-title">平均单价</div>
              <div class="stat-value">¥{{ stats.averagePrice }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-title">学员数量</div>
              <div class="stat-value">{{ stats.studentCount }}人</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="收入明细" name="details">
          <el-table
            v-loading="loading"
            :data="incomeDetails"
            border
            style="width: 100%"
          >
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="time" label="时间" width="120" />
            <el-table-column prop="studentName" label="学员姓名" />
            <el-table-column prop="duration" label="训练时长" width="100">
              <template slot-scope="{row}">
                {{ row.duration }}分钟
              </template>
            </el-table-column>
            <el-table-column prop="price" label="课时单价" width="100">
              <template slot-scope="{row}">
                ¥{{ row.price }}/小时
              </template>
            </el-table-column>
            <el-table-column prop="totalFee" label="课时费" width="100">
              <template slot-scope="{row}">
                ¥{{ row.totalFee }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="{row}">
                <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
                  {{ row.status === 'COMPLETED' ? '已完成' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
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
                ¥{{ row.totalIncome }}
              </template>
            </el-table-column>
            <el-table-column prop="totalHours" label="总课时" width="100">
              <template slot-scope="{row}">
                {{ row.totalHours }}小时
              </template>
            </el-table-column>
            <el-table-column prop="studentCount" label="学员数量" width="100" />
            <el-table-column prop="averagePrice" label="平均单价" width="100">
              <template slot-scope="{row}">
                ¥{{ row.averagePrice }}
              </template>
            </el-table-column>
            <el-table-column prop="courseCount" label="课程次数" width="100" />
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
import { getIncomeStats, getIncomeDetails, getMonthlyStats } from '@/api/coach'
import Pagination from '@/components/Pagination'

export default {
  name: 'IncomeStats',
  components: { Pagination },
  data() {
    return {
      activeTab: 'details',
      dateRange: [],
      stats: {
        monthIncome: 0,
        monthHours: 0,
        averagePrice: 0,
        studentCount: 0
      },
      incomeDetails: [],
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
        const response = await getIncomeStats()
        this.stats = response.data || {}
      } catch (error) {
        console.error('获取统计信息失败:', error)
        this.$message.error('获取统计信息失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || ''
        }

        if (this.activeTab === 'details') {
          const response = await getIncomeDetails(params)
          this.incomeDetails = response.data.records || []
          this.total = response.data.total || 0
        } else {
          const response = await getMonthlyStats(params)
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
    async exportData() {
      try {
        const params = {
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || ''
        }
        
        const response = await this.$http({
          url: '/api/coach/income/export',
          method: 'get',
          params,
          responseType: 'blob'
        })
        
        const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
        const fileName = `收入统计_${new Date().getTime()}.xlsx`
        
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

.stats-overview {
  margin-bottom: 30px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: bold;
}
</style>