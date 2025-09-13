<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 账户信息卡片 -->
      <el-col :span="8">
        <el-card>
          <div slot="header">
            <span>账户信息</span>
          </div>
          <div class="account-info">
            <div class="balance-display">
              <div class="balance-label">当前余额</div>
              <div class="balance-amount">¥{{ balance.toFixed(2) }}</div>
            </div>
            <div class="account-stats">
              <div class="stat-item">
                <span class="stat-label">本月消费</span>
                <span class="stat-value">¥{{ monthlyExpense.toFixed(2) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">本月充值</span>
                <span class="stat-value">¥{{ monthlyRecharge.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" style="width: 100%; margin-bottom: 10px;" @click="goToRecharge">
              立即充值
            </el-button>
            <el-button style="width: 100%;" @click="goToCourseBook">
              预约课程
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 交易记录 -->
      <el-col :span="16">
        <el-card>
          <div slot="header">
            <span>交易记录</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="getList">刷新</el-button>
          </div>
          
          <!-- 交易记录表格 -->
          <el-table
            v-loading="listLoading"
            :data="list"
            border
            fit
            highlight-current-row
            style="width: 100%;">
            
            <el-table-column label="交易时间" align="center" width="160">
              <template slot-scope="scope">
                <span>{{ scope.row.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="交易类型" align="center" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.paymentType | paymentTypeFilter">
                  {{ scope.row.paymentType | paymentTypeText }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="金额" align="center" width="100">
              <template slot-scope="scope">
                <span :class="['amount-display', getAmountClass(scope.row)]">
                  {{ getAmountDisplay(scope.row) }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column label="支付方式" align="center" width="100">
              <template slot-scope="scope">
                {{ scope.row.paymentMethod | paymentMethodText }}
              </template>
            </el-table-column>
            
            <el-table-column label="状态" align="center" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status | statusTypeFilter">
                  {{ scope.row.status | statusText }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="订单号" align="center" min-width="200">
              <template slot-scope="scope">
                {{ scope.row.orderId }}
              </template>
            </el-table-column>
            
            <el-table-column label="备注" align="center" min-width="150">
              <template slot-scope="scope">
                {{ getRemark(scope.row) }}
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <pagination
            v-show="total>0"
            :total="total"
            :page.sync="listQuery.page"
            :limit.sync="listQuery.size"
            @pagination="getList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getBalance, getUserPayments } from '@/api/student'
import Pagination from '@/components/Pagination'

export default {
  name: 'StudentAccount',
  components: { Pagination },
  data() {
    return {
      balance: 0,
      monthlyExpense: 0,
      monthlyRecharge: 0,
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 0,
        size: 10
      }
    }
  },
  filters: {
    paymentTypeFilter(type) {
      const map = {
        'RECHARGE': 'success',
        'COURSE_FEE': 'warning',
        'REFUND': 'info',
        'CONTEST_FEE': 'danger'
      }
      return map[type] || ''
    },
    paymentTypeText(type) {
      const map = {
        'RECHARGE': '充值',
        'COURSE_FEE': '课程费用',
        'REFUND': '退款',
        'CONTEST_FEE': '比赛费用'
      }
      return map[type] || type
    },
    paymentMethodText(method) {
      const map = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝',
        'OFFLINE': '线下支付'
      }
      return map[method] || method
    },
    statusTypeFilter(status) {
      const map = {
        'PENDING': 'warning',
        'SUCCESS': 'success',
        'FAILED': 'danger'
      }
      return map[status] || ''
    },
    statusText(status) {
      const map = {
        'PENDING': '待支付',
        'SUCCESS': '已完成',
        'FAILED': '已失败'
      }
      return map[status] || status
    }
  },
  created() {
    this.getAccountBalance()
    this.getList()
  },
  methods: {
    async getAccountBalance() {
      try {
        const userId = this.$store.state.user.user?.id
        if (!userId) {
          this.$message.error('无法获取用户信息')
          return
        }

        const response = await getBalance(userId)
        if (response.code === 200) {
          this.balance = response.data || 0
        } else {
          this.$message.error('获取余额失败')
          this.balance = 0
        }
      } catch (error) {
        console.error('获取余额失败:', error)
        this.$message.error('获取余额失败')
        this.balance = 0
      }
    },
    async getList() {
      this.listLoading = true
      try {
        const userId = this.$store.state.user.user?.id
        if (!userId) {
          this.$message.error('无法获取用户信息')
          return
        }

        // 重置页码为0，确保从第一页开始获取
        this.listQuery.page = 0

        const params = {
          page: this.listQuery.page,
          size: this.listQuery.size,
          sort: 'createdAt,desc'
        }

        const response = await getUserPayments(userId, params)
        if (response.code === 200) {
          this.list = response.data.content || []
          this.total = response.data.totalElements || 0
        } else {
          this.$message.error('获取交易记录失败')
          this.list = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取交易记录失败:', error)
        this.$message.error('获取交易记录失败')
        this.list = []
        this.total = 0
      } finally {
        this.listLoading = false
      }
    },
    getRemark(row) {
      switch (row.paymentType) {
        case 'RECHARGE':
          return row.amount > 0 ? '账户充值' : '充值退款'
        case 'COURSE_FEE':
          return `课程费用 - 课程ID: ${row.relatedId || 'N/A'}`
        case 'REFUND':
          return '退款处理'
        case 'CONTEST_FEE':
          return `比赛费用 - 比赛ID: ${row.relatedId || 'N/A'}`
        default:
          return ''
      }
    },
    goToRecharge() {
      this.$router.push('/student/recharge')
    },
    goToCourseBook() {
      this.$router.push('/student/course-book')
    },
    getAmountClass(row) {
      const positiveTypes = ['RECHARGE', 'REFUND']
      const negativeTypes = ['COURSE_FEE', 'CONTEST_FEE']
      
      if (positiveTypes.includes(row.paymentType)) {
        return 'positive-amount'
      } else if (negativeTypes.includes(row.paymentType)) {
        return 'negative-amount'
      }
      return row.amount > 0 ? 'positive-amount' : 'negative-amount'
    },
    getAmountDisplay(row) {
      const positiveTypes = ['RECHARGE', 'REFUND']
      const negativeTypes = ['COURSE_FEE', 'CONTEST_FEE']
      
      if (positiveTypes.includes(row.paymentType)) {
        return `+${row.amount.toFixed(2)}`
      } else if (negativeTypes.includes(row.paymentType)) {
        return `-${Math.abs(row.amount).toFixed(2)}`
      }
      
      // 默认处理
      return row.amount > 0 ? `+${row.amount.toFixed(2)}` : `${row.amount.toFixed(2)}`
    },
  }
}
</script>

<style scoped>
.account-info {
  text-align: center;
}

.balance-display {
  margin-bottom: 30px;
}

.balance-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.account-stats {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
}

.stat-value {
  font-weight: bold;
  color: #333;
}

.quick-actions {
  text-align: center;
}

.amount-display {
  font-weight: bold;
}

.positive-amount {
  color: #67C23A;
}

.negative-amount {
  color: #F56C6C;
}

.income {
  color: #67C23A;
  font-weight: bold;
}

.expense {
  color: #F56C6C;
  font-weight: bold;
}
</style>