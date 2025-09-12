<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>账户管理</span>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="balance-card">
            <div class="balance-info">
              <div class="balance-label">账户余额</div>
              <div class="balance-amount">¥{{ balance.toFixed(2) }}</div>
              <el-button type="primary" @click="handleRecharge">立即充值</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="info-card">
            <div class="info-item">
              <span class="info-label">今日消费：</span>
              <span class="info-value">¥{{ todayExpense.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">本月消费：</span>
              <span class="info-value">¥{{ monthExpense.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">总消费：</span>
              <span class="info-value">¥{{ totalExpense.toFixed(2) }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>交易记录</span>
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
          <el-select v-model="typeFilter" placeholder="交易类型" clearable @change="handleFilter">
            <el-option label="充值" value="RECHARGE" />
            <el-option label="消费" value="CONSUME" />
            <el-option label="退款" value="REFUND" />
          </el-select>
          <el-button type="primary" @click="handleFilter">查询</el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="transactions"
          border
          style="width: 100%"
        >
          <el-table-column prop="transactionTime" label="交易时间" width="160" />
          <el-table-column prop="transactionType" label="交易类型" width="100">
            <template slot-scope="{row}">
              <el-tag :type="getTransactionTypeType(row.transactionType)">
                {{ getTransactionTypeText(row.transactionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="交易描述" />
          <el-table-column prop="amount" label="金额" width="120">
            <template slot-scope="{row}">
              <span :class="getAmountClass(row.amount)">
                {{ row.amount > 0 ? '+' : '' }}¥{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="120">
            <template slot-scope="{row}">
              ¥{{ row.balance.toFixed(2) }}
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
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog title="账户充值" :visible.sync="rechargeDialogVisible" width="30%">
      <el-form ref="rechargeForm" :model="rechargeForm" :rules="rechargeRules" label-width="100px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="100"
            :max="5000"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="rechargeForm.paymentMethod" style="width: 100%">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="BANK" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecharge">确认充值</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAccountInfo, getTransactions, rechargeAccount } from '@/api/student'
import Pagination from '@/components/Pagination'

export default {
  name: 'StudentAccount',
  components: { Pagination },
  data() {
    return {
      balance: 0,
      todayExpense: 0,
      monthExpense: 0,
      totalExpense: 0,
      transactions: [],
      dateRange: [],
      typeFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      rechargeDialogVisible: false,
      rechargeForm: {
        amount: 100,
        paymentMethod: 'WECHAT'
      },
      rechargeRules: {
        amount: [
          { required: true, message: '请输入充值金额', trigger: 'blur' },
          { type: 'number', min: 100, max: 5000, message: '充值金额必须在 100-5000 之间', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getAccountInfo()
    this.getList()
  },
  methods: {
    async getAccountInfo() {
      try {
        const response = await getAccountInfo()
        const data = response.data
        this.balance = data.balance || 0
        this.todayExpense = data.todayExpense || 0
        this.monthExpense = data.monthExpense || 0
        this.totalExpense = data.totalExpense || 0
      } catch (error) {
        console.error('获取账户信息失败:', error)
        this.$message.error('获取账户信息失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          type: this.typeFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getTransactions(params)
        this.transactions = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取交易记录失败:', error)
        this.$message.error('获取交易记录失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleRecharge() {
      this.rechargeForm = {
        amount: 100,
        paymentMethod: 'WECHAT'
      }
      this.rechargeDialogVisible = true
    },
    async submitRecharge() {
      try {
        await this.$refs.rechargeForm.validate()
        
        await rechargeAccount({
          amount: this.rechargeForm.amount,
          paymentMethod: this.rechargeForm.paymentMethod
        })
        
        this.$message.success(`充值成功 ¥${this.rechargeForm.amount}`)
        this.rechargeDialogVisible = false
        this.getAccountInfo()
        this.getList()
      } catch (error) {
        console.error('充值失败:', error)
        this.$message.error('充值失败')
      }
    },
    getTransactionTypeType(type) {
      const types = {
        'RECHARGE': 'success',
        'CONSUME': 'danger',
        'REFUND': 'warning'
      }
      return types[type] || 'info'
    },
    getTransactionTypeText(type) {
      const texts = {
        'RECHARGE': '充值',
        'CONSUME': '消费',
        'REFUND': '退款'
      }
      return texts[type] || type
    },
    getAmountClass(amount) {
      return amount >= 0 ? 'positive-amount' : 'negative-amount'
    }
  }
}
</script>

<style scoped>
.balance-card {
  text-align: center;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.balance-info {
  padding: 20px 0;
}

.balance-label {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 36px;
  font-weight: bold;
  color: #F56C6C;
  margin-bottom: 20px;
}

.info-card {
  height: 200px;
  padding: 20px;
}

.info-item {
  margin-bottom: 15px;
  font-size: 14px;
}

.info-label {
  color: #666;
}

.info-value {
  font-weight: bold;
  color: #333;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-container > * {
  margin-right: 10px;
  margin-bottom: 10px;
}

.positive-amount {
  color: #67C23A;
  font-weight: bold;
}

.negative-amount {
  color: #F56C6C;
  font-weight: bold;
}
</style>