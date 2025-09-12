<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>操作日志</span>
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
        <el-select v-model="userTypeFilter" placeholder="用户类型" clearable @change="handleFilter">
          <el-option label="学员" value="STUDENT" />
          <el-option label="教练" value="COACH" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select v-model="operationTypeFilter" placeholder="操作类型" clearable @change="handleFilter">
          <el-option label="登录" value="LOGIN" />
          <el-option label="创建" value="CREATE" />
          <el-option label="更新" value="UPDATE" />
          <el-option label="删除" value="DELETE" />
          <el-option label="查询" value="QUERY" />
          <el-option label="支付" value="PAYMENT" />
          <el-option label="预约" value="APPOINTMENT" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索用户或操作"
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button type="success" @click="exportData">导出日志</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="operationLogs"
        border
        style="width: 100%"
      >
        <el-table-column prop="createTime" label="操作时间" width="160" />
        <el-table-column prop="userName" label="操作人" width="120" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getUserTypeType(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getOperationTypeType(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作描述" />
        <el-table-column prop="ipAddress" label="IP地址" width="120" />
        <el-table-column prop="userAgent" label="设备信息" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="viewDetail(row)">查看详情</el-button>
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

    <!-- 日志详情对话框 -->
    <el-dialog title="日志详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ selectedLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ selectedLog.createTime }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ selectedLog.userName }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">{{ getUserTypeText(selectedLog.userType) }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ getOperationTypeText(selectedLog.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ selectedLog.ipAddress }}</el-descriptions-item>
        </el-descriptions>

        <div class="operation-detail">
          <h4>操作描述</h4>
          <p>{{ selectedLog.operation }}</p>
        </div>

        <div class="request-params">
          <h4>请求参数</h4>
          <pre>{{ formatJson(selectedLog.requestParams) }}</pre>
        </div>

        <div class="response-data">
          <h4>响应数据</h4>
          <pre>{{ formatJson(selectedLog.responseData) }}</pre>
        </div>

        <div class="user-agent">
          <h4>设备信息</h4>
          <p>{{ selectedLog.userAgent }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOperationLogs } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'OperationLogs',
  components: { Pagination },
  data() {
    return {
      operationLogs: [],
      dateRange: [],
      userTypeFilter: '',
      operationTypeFilter: '',
      keyword: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      selectedLog: null,
      detailDialogVisible: false
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || '',
          userType: this.userTypeFilter || '',
          operationType: this.operationTypeFilter || '',
          keyword: this.keyword || ''
        }
        
        const response = await getOperationLogs(params)
        this.operationLogs = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取操作日志失败:', error)
        this.$message.error('获取操作日志失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    getUserTypeType(userType) {
      const typeMap = {
        'STUDENT': 'primary',
        'COACH': 'success',
        'ADMIN': 'warning'
      }
      return typeMap[userType] || 'info'
    },
    getUserTypeText(userType) {
      const textMap = {
        'STUDENT': '学员',
        'COACH': '教练',
        'ADMIN': '管理员'
      }
      return textMap[userType] || userType
    },
    getOperationTypeType(operationType) {
      const typeMap = {
        'LOGIN': 'info',
        'CREATE': 'success',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'QUERY': 'primary',
        'PAYMENT': 'success',
        'APPOINTMENT': 'primary'
      }
      return typeMap[operationType] || 'info'
    },
    getOperationTypeText(operationType) {
      const textMap = {
        'LOGIN': '登录',
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除',
        'QUERY': '查询',
        'PAYMENT': '支付',
        'APPOINTMENT': '预约'
      }
      return textMap[operationType] || operationType
    },
    viewDetail(row) {
      this.selectedLog = row
      this.detailDialogVisible = true
    },
    formatJson(jsonString) {
      if (!jsonString) return '无数据'
      try {
        const obj = JSON.parse(jsonString)
        return JSON.stringify(obj, null, 2)
      } catch (e) {
        return jsonString
      }
    },
    async exportData() {
      try {
        const params = {
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || '',
          userType: this.userTypeFilter || '',
          operationType: this.operationTypeFilter || '',
          keyword: this.keyword || ''
        }
        
        const response = await this.$http({
          url: '/api/campus/logs/export',
          method: 'get',
          params,
          responseType: 'blob'
        })
        
        const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
        const fileName = `操作日志_${new Date().getTime()}.xlsx`
        
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
        console.error('导出日志失败:', error)
        this.$message.error('导出日志失败')
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

.operation-detail,
.request-params,
.response-data,
.user-agent {
  margin-top: 20px;
}

.operation-detail h4,
.request-params h4,
.response-data h4,
.user-agent h4 {
  margin-bottom: 10px;
  color: #303133;
}

.request-params pre,
.response-data pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.positive {
  color: #67C23A;
}

.negative {
  color: #F56C6C;
}
</style>