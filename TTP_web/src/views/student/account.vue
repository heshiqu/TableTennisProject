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
            <el-button type="primary" style="width: 100%; margin-bottom: 10px;" @click="showRechargeDialog">
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

    <!-- 充值对话框 -->
    <el-dialog title="账户充值" :visible.sync="rechargeDialogVisible" width="400px">
      <el-form ref="rechargeForm" :model="rechargeForm" :rules="rechargeRules" label-width="100px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="100" 
            :precision="0" 
            :step="100" 
            placeholder="请输入充值金额"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="WECHAT">微信支付</el-radio>
            <el-radio label="ALIPAY">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecharge">提交</el-button>
      </div>
    </el-dialog>

    <!-- 支付确认对话框 -->
    <el-dialog title="支付确认" :visible.sync="paymentConfirmVisible" width="400px">
      <div class="payment-confirm-content" v-if="currentPayment">
        <div class="payment-info">
          <p>订单金额: <span class="amount">¥{{ currentPayment.amount }}</span></p>
          <p>订单号: <span class="order-id">{{ currentPayment.orderId }}</span></p>
          <p>支付方式: <span class="payment-method">{{ paymentMethodText(currentPayment.paymentMethod) }}</span></p>
        </div>
        <div class="payment-tips">
          <p>请您使用手机扫描二维码或打开相应支付软件完成支付</p>
          <p>支付完成后请点击下方按钮确认</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handlePaymentFailure">支付失败</el-button>
        <el-button type="primary" @click="handlePaymentSuccess">支付成功</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBalance, getUserPayments } from '@/api/student'
import { createPaymentOrder, confirmPaymentSuccess, confirmPaymentFailure } from '@/api/payment'
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
      },
      // 充值相关
      rechargeDialogVisible: false,
      rechargeForm: {
        amount: 100,
        paymentMethod: 'WECHAT'
      },
      rechargeRules: {
        amount: [
          { required: true, message: '请输入充值金额', trigger: 'blur' },
          { type: 'number', min: 100, message: '充值金额不能少于100元', trigger: 'blur' }
        ],
        paymentMethod: [
          { required: true, message: '请选择支付方式', trigger: 'change' }
        ]
      },
      // 支付确认相关
      paymentConfirmVisible: false,
      currentPayment: null
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
    // 显示充值对话框
      showRechargeDialog() {
        this.rechargeForm = {
          amount: 100,
          paymentMethod: 'WECHAT'
        }
        this.rechargeDialogVisible = true
      },
      
      // 提交充值请求
      async submitRecharge() {
        try {
          await this.$refs.rechargeForm.validate()
          
          const userId = this.$store.state.user.user?.id
          if (!userId) {
            this.$message.error('无法获取用户信息')
            return
          }
          
          // 创建支付订单
          const response = await createPaymentOrder({
            userId: userId,
            amount: this.rechargeForm.amount,
            paymentType: 'RECHARGE',
            paymentMethod: this.rechargeForm.paymentMethod
          })
          
          if (response.code === 200) {
            this.currentPayment = response.data
            this.rechargeDialogVisible = false
            this.paymentConfirmVisible = true
          } else {
            this.$message.error('创建订单失败: ' + response.message)
          }
        } catch (error) {
          console.error('充值失败:', error)
          this.$message.error('充值失败，请稍后重试')
        }
      },
      
      // 处理支付成功
      async handlePaymentSuccess() {
        try {
          if (!this.currentPayment?.orderId) {
            this.$message.error('订单信息错误')
            return
          }
          
          const response = await confirmPaymentSuccess(this.currentPayment.orderId)
          
          if (response.code === 200) {
            this.$message.success('充值成功！')
            this.paymentConfirmVisible = false
            // 刷新余额和交易记录
            this.getAccountBalance()
            this.getList()
          } else {
            this.$message.error('支付处理失败: ' + response.message)
          }
        } catch (error) {
          console.error('支付成功处理失败:', error)
          this.$message.error('支付成功处理失败，请稍后重试')
        }
      },
      
      // 处理支付失败
      async handlePaymentFailure() {
        try {
          if (!this.currentPayment?.orderId) {
            this.$message.error('订单信息错误')
            return
          }
          
          const response = await confirmPaymentFailure(this.currentPayment.orderId)
          
          if (response.code === 200) {
            this.$message.info('已取消支付')
            this.paymentConfirmVisible = false
            // 刷新余额和交易记录
            this.getAccountBalance()
            this.getList()
          } else {
            this.$message.error('支付处理失败: ' + response.message)
          }
        } catch (error) {
          console.error('支付失败处理失败:', error)
          this.$message.error('支付失败处理失败，请稍后重试')
        }
      },
      
      // 获取支付方式文本
      paymentMethodText(method) {
        const map = {
          'WECHAT': '微信支付',
          'ALIPAY': '支付宝',
          'OFFLINE': '线下支付'
        }
        return map[method] || method
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

/* 支付确认对话框样式 */
.payment-confirm-content {
  padding: 20px 0;
}

.payment-info {
  margin-bottom: 20px;
}

.payment-info p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.payment-info .amount {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.payment-info .order-id {
  color: #606266;
  font-family: monospace;
}

.payment-info .payment-method {
  color: #409EFF;
  font-weight: bold;
}

.payment-tips {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.payment-tips p {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.payment-tips p:last-child {
  margin-bottom: 0;
}
</style>