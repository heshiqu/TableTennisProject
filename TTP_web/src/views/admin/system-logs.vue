<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>系统日志</span>
      </div>

      <div class="filter-container">
        <el-input
          v-model="listQuery.keyword"
          placeholder="关键词"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-select
          v-model="listQuery.operationType"
          placeholder="操作类型"
          clearable
          style="width: 120px"
          class="filter-item"
        >
          <el-option label="登录" value="LOGIN" />
          <el-option label="创建" value="CREATE" />
          <el-option label="更新" value="UPDATE" />
          <el-option label="删除" value="DELETE" />
          <el-option label="查询" value="QUERY" />
        </el-select>
        <el-select
          v-model="listQuery.userType"
          placeholder="用户类型"
          clearable
          style="width: 120px"
          class="filter-item"
        >
          <el-option label="超级管理员" value="SUPER_ADMIN" />
          <el-option label="校区管理员" value="CAMPUS_ADMIN" />
          <el-option label="教练" value="COACH" />
          <el-option label="学员" value="STUDENT" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilter"
        />
        <el-button
          v-waves
          class="filter-item"
          type="primary"
          icon="el-icon-search"
          @click="handleFilter"
        >
          搜索
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
        v-loading="listLoading"
        :data="list"
        border
        fit
        highlight-current-row
        style="width: 100%;"
      >
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="操作时间" prop="operationTime" align="center" />
        <el-table-column label="操作人" prop="operatorName" align="center" />
        <el-table-column label="用户类型" align="center">
          <template slot-scope="{row}">
            <el-tag :type="getUserTypeType(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" align="center">
          <template slot-scope="{row}">
            <el-tag :type="getOperationTypeType(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" prop="description" align="center" />
        <el-table-column label="IP地址" prop="ipAddress" align="center" />
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="{row}">
            <el-button size="mini" @click="handleView(row)">
              详情
            </el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      title="日志详情"
      :visible.sync="detailVisible"
      width="600px"
    >
      <el-form label-width="100px">
        <el-form-item label="操作时间：">
          <span>{{ currentLog.operationTime }}</span>
        </el-form-item>
        <el-form-item label="操作人：">
          <span>{{ currentLog.operatorName }}</span>
        </el-form-item>
        <el-form-item label="用户类型：">
          <el-tag :type="getUserTypeType(currentLog.userType)">
            {{ getUserTypeText(currentLog.userType) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作类型：">
          <el-tag :type="getOperationTypeType(currentLog.operationType)">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作描述：">
          <span>{{ currentLog.description }}</span>
        </el-form-item>
        <el-form-item label="IP地址：">
          <span>{{ currentLog.ipAddress }}</span>
        </el-form-item>
        <el-form-item label="请求参数：">
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px;">{{ currentLog.requestParams || '无' }}</pre>
        </el-form-item>
        <el-form-item label="响应结果：">
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px;">{{ currentLog.responseResult || '无' }}</pre>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { getSystemLogs, exportSystemLogs } from '@/api/admin'
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'

export default {
  name: 'AdminSystemLogs',
  directives: { waves },
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        keyword: undefined,
        operationType: undefined,
        userType: undefined,
        startDate: undefined,
        endDate: undefined
      },
      dateRange: [],
      detailVisible: false,
      currentLog: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const params = {
          ...this.listQuery,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getSystemLogs(params)
        this.list = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取系统日志失败:', error)
        this.$message.error('获取系统日志失败')
      } finally {
        this.listLoading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleView(row) {
      this.currentLog = row
      this.detailVisible = true
    },
    async handleExport() {
      try {
        const params = {
          ...this.listQuery,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportSystemLogs(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    getUserTypeType(userType) {
      const types = {
        'SUPER_ADMIN': 'danger',
        'CAMPUS_ADMIN': 'warning',
        'COACH': 'success',
        'STUDENT': 'info'
      }
      return types[userType] || ''
    },
    getUserTypeText(userType) {
      const texts = {
        'SUPER_ADMIN': '超级管理员',
        'CAMPUS_ADMIN': '校区管理员',
        'COACH': '教练',
        'STUDENT': '学员'
      }
      return texts[userType] || userType
    },
    getOperationTypeType(operationType) {
      const types = {
        'LOGIN': 'success',
        'CREATE': 'primary',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'QUERY': 'info'
      }
      return types[operationType] || ''
    },
    getOperationTypeText(operationType) {
      const texts = {
        'LOGIN': '登录',
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除',
        'QUERY': '查询'
      }
      return texts[operationType] || operationType
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}

.filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-left: 10px;
}

pre {
  margin: 0;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.4;
  max-height: 200px;
  overflow-y: auto;
}
</style>