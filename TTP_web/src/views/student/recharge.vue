<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 账户信息 -->
      <el-col :span="8">
        <el-card>
          <div slot="header">
            <span>账户信息</span>
          </div>
          <div class="account-info">
            <div class="balance-display">
              <div class="balance-label">当前余额</div>
              <div class="balance-amount">
                <count-to :start-val="0" :end-val="balance" :duration="1000" 
                          :decimals="2" :suffix="'元'" class="count-to" />
              </div>
            </div>
            <div class="account-stats">
              <div class="stat-item">
                <span class="stat-label">本月消费</span>
                <span class="stat-value">{{ monthlyExpense }}元</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">本月充值</span>
                <span class="stat-value">{{ monthlyRecharge }}元</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 充值记录 -->
        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>最近充值</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadRechargeHistory">刷新</el-button>
          </div>
          <div v-loading="historyLoading">
            <div v-if="recentRecharges.length > 0">
              <div v-for="item in recentRecharges" :key="item.id" class="recharge-item">
                <div class="recharge-info">
                  <div class="recharge-amount">+{{ item.amount }}元</div>
                  <div class="recharge-time">{{ item.createTime | formatDate }}</div>
                </div>
                <el-tag :type="item.status | statusType">{{ item.status | statusFilter }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无充值记录"></el-empty>
          </div>
        </el-card>
      </el-col>

      <!-- 充值区域 -->
      <el-col :span="16">
        <el-card>
          <div slot="header">
            <span>账户充值</span>
          </div>
          
          <!-- 充值金额选择 -->
          <div class="recharge-amount">
            <h4>选择充值金额</h4>
            <el-row :gutter="15">
              <el-col :span="6" v-for="amount in presetAmounts" :key="amount">
                <div 
                  class="amount-card"
                  :class="{ active: selectedAmount === amount }"
                  @click="selectAmount(amount)">
                  <div class="amount">{{ amount }}元</div>
                  <div class="description">{{ getAmountDescription(amount) }}</div>
                </div>
              </el-col>
            </el-row>
            
            <!-- 自定义金额 -->
            <div style="margin-top: 20px;">
              <el-input-number 
                v-model="customAmount" 
                :min="1" 
                :max="10000" 
                :step="100"
                controls-position="right"
                style="width: 200px; margin-right: 10px;"
                @change="selectCustomAmount">
              </el-input-number>
              <span>元</span>
            </div>
          </div>

          <!-- 支付方式 -->
          <div class="payment-method">
            <h4>选择支付方式</h4>
            <el-radio-group v-model="paymentMethod">
              <el-radio label="WECHAT">
                <img src="@/assets/wechat-pay.png" alt="微信支付" class="payment-icon">
                微信支付
              </el-radio>
              <el-radio label="ALIPAY">
                <img src="@/assets/alipay.png" alt="支付宝" class="payment-icon">
                支付宝
              </el-radio>
              <el-radio label="OFFLINE">
                <i class="el-icon-bank-card" style="font-size: 24px; margin-right: 5px;"></i>
                线下支付
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 支付确认 -->
          <div class="payment-confirm">
            <el-divider></el-divider>
            <div class="confirm-info">
              <div class="confirm-item">
                <span>充值金额：</span>
                <span class="highlight">{{ actualAmount }}元</span>
              </div>
              <div class="confirm-item">
                <span>支付方式：</span>
                <span>{{ paymentMethod | paymentMethodFilter }}</span>
              </div>
            </div>
            
            <el-button 
              type="primary" 
              size="large" 
              style="width: 100%; margin-top: 20px;" 
              @click="handleRecharge"
              :loading="recharging">
              立即充值
            </el-button>
          </div>
        </el-card>

        <!-- 充值说明 -->
        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>充值说明</span>
          </div>
          <div class="recharge-notes">
            <p>1. 充值金额将立即到账，可用于课程预约</p>
            <p>2. 支持微信、支付宝在线支付</p>
            <p>3. 线下支付请联系校区管理员</p>
            <p>4. 充值金额不可提现，仅用于系统内消费</p>
            <p>5. 如有疑问，请联系客服：400-123-4567</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 支付二维码对话框 -->
    <el-dialog title="扫码支付" :visible.sync="qrDialogVisible" width="400px">
      <div class="qr-dialog">
        <div class="qr-code">
          <img :src="qrCode" alt="支付二维码" style="width: 200px; height: 200px;">
        </div>
        <div class="qr-tip">
          <p>请使用{{ paymentMethod | paymentMethodFilter }}扫描二维码完成支付</p>
          <el-button type="text" @click="checkPaymentStatus">我已支付</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import { getBalance, createPayment, getPaymentStatus, getRechargeHistory } from '@/api/payment'

export default {
  name: 'Recharge',
  components: {
    CountTo
  },
  data() {
    return {
      balance: 0,
      monthlyExpense: 0,
      monthlyRecharge: 0,
      presetAmounts: [100, 500, 1000, 2000],
      selectedAmount: null,
      customAmount: null,
      paymentMethod: 'WECHAT',
      recharging: false,
      qrDialogVisible: false,
      qrCode: '',
      paymentId: null,
      recentRecharges: [],
      historyLoading: false
    }
  },
  computed: {
    actualAmount() {
      return this.customAmount || this.selectedAmount || 0
    }
  },
  filters: {
    formatDate(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN')
    },
    statusFilter(status) {
      const statusMap = {
        'PENDING': '待支付',
        'SUCCESS': '已完成',
        'FAILED': '已失败',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    statusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'SUCCESS': 'success',
        'FAILED': 'danger',
        'CANCELLED': 'info'
      }
      return typeMap[status] || ''
    },
    paymentMethodFilter(method) {
      const methodMap = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝',
        'OFFLINE': '线下支付'
      }
      return methodMap[method] || method
    }
  },
  created() {
    this.loadAccountInfo()
    this.loadRechargeHistory()
  },
  methods: {
    async loadAccountInfo() {
      try {
        // 从Vuex获取当前用户ID
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
        console.error('获取账户信息失败:', error)
        this.$message.error('获取账户信息失败')
        this.balance = 0
      }
    },
    async loadRechargeHistory() {
      this.historyLoading = true
      try {
        const response = await getRechargeHistory({ size: 5 })
        this.recentRecharges = response.data.records || []
      } catch (error) {
        console.error('获取充值记录失败:', error)
      } finally {
        this.historyLoading = false
      }
    },
    selectAmount(amount) {
      this.selectedAmount = amount
      this.customAmount = null
    },
    selectCustomAmount() {
      this.selectedAmount = null
    },
    getAmountDescription(amount) {
      const descriptions = {
        100: '体验充值',
        500: '基础充值',
        1000: '推荐充值',
        2000: '大额充值'
      }
      return descriptions[amount] || ''
    },
    async handleRecharge() {
      if (this.actualAmount <= 0) {
        this.$message.warning('请选择充值金额')
        return
      }

      this.recharging = true
      try {
        const paymentData = {
          amount: this.actualAmount,
          paymentMethod: this.paymentMethod,
          type: 'RECHARGE'
        }

        if (this.paymentMethod === 'OFFLINE') {
          await this.$confirm('请确认已联系校区管理员完成线下支付', '线下支付确认', {
            confirmButtonText: '已支付',
            cancelButtonText: '取消',
            type: 'info'
          })
          
          const response = await createPayment(paymentData)
          this.$message.success('线下支付申请已提交，请联系管理员确认')
          this.resetForm()
          this.loadAccountInfo()
          this.loadRechargeHistory()
        } else {
          const response = await createPayment(paymentData)
          this.paymentId = response.data.paymentId
          this.qrCode = response.data.qrCode
          this.qrDialogVisible = true
          
          // 开始轮询支付状态
          this.startPollingPaymentStatus()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '充值失败')
        }
      } finally {
        this.recharging = false
      }
    },
    startPollingPaymentStatus() {
      const checkStatus = async () => {
        try {
          const response = await getPaymentStatus(this.paymentId)
          if (response.data.status === 'SUCCESS') {
            this.$message.success('充值成功')
            this.qrDialogVisible = false
            this.resetForm()
            this.loadAccountInfo()
            this.loadRechargeHistory()
            clearInterval(this.pollingInterval)
          } else if (response.data.status === 'FAILED') {
            this.$message.error('支付失败')
            clearInterval(this.pollingInterval)
          }
        } catch (error) {
          console.error('查询支付状态失败:', error)
        }
      }
      
      // 每3秒检查一次支付状态，最多检查20次（1分钟）
      let checkCount = 0
      this.pollingInterval = setInterval(() => {
        checkCount++
        if (checkCount > 20) {
          clearInterval(this.pollingInterval)
          return
        }
        checkStatus()
      }, 3000)
    },
    async checkPaymentStatus() {
      try {
        const response = await getPaymentStatus(this.paymentId)
        if (response.data.status === 'SUCCESS') {
          this.$message.success('充值成功')
          this.qrDialogVisible = false
          this.resetForm()
          this.loadAccountInfo()
          this.loadRechargeHistory()
        } else {
          this.$message.info('请完成支付')
        }
      } catch (error) {
        this.$message.error('查询支付状态失败')
      }
    },
    resetForm() {
      this.selectedAmount = null
      this.customAmount = null
      this.paymentMethod = 'WECHAT'
      this.qrCode = ''
      this.paymentId = null
    }
  },
  beforeDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
    }
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
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 36px;
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

.recharge-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.recharge-item:last-child {
  border-bottom: none;
}

.recharge-amount {
  font-weight: bold;
  color: #67C23A;
}

.recharge-time {
  font-size: 12px;
  color: #999;
}

.recharge-amount h4 {
  margin-bottom: 20px;
}

.amount-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.amount-card:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
}

.amount-card.active {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.description {
  font-size: 12px;
  color: #666;
}

.payment-method {
  margin: 30px 0;
}

.payment-method h4 {
  margin-bottom: 20px;
}

.payment-icon {
  width: 24px;
  height: 24px;
  vertical-align: middle;
  margin-right: 5px;
}

.confirm-info {
  margin: 20px 0;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 16px;
}

.highlight {
  font-weight: bold;
  color: #F56C6C;
  font-size: 20px;
}

.recharge-notes p {
  margin: 10px 0;
  color: #666;
  font-size: 14px;
}

.qr-dialog {
  text-align: center;
}

.qr-code {
  margin-bottom: 20px;
}

.qr-tip p {
  color: #666;
  margin-bottom: 10px;
}
</style>