<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>系统日志</span>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索用户、操作内容"
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        />
        <el-select v-model="operationType" placeholder="操作类型" clearable @change="handleFilter">
          <el-option label="登录" value="LOGIN" />
          <el-option label="注册" value="REGISTER" />
          <el-option label="修改信息" value="UPDATE_PROFILE" />
          <el-option label="预约课程" value="BOOK_COURSE" />
          <el-option label="取消预约" value="CANCEL_BOOKING" />
          <el-option label="充值" value="RECHARGE" />
          <el-option label="评价" value="EVALUATE" />
          <el-option label="报名比赛" value="TOURNAMENT_REGISTER" />
          <el-option label="更换教练" value="CHANGE_COACH" />
        </el-select>
        <el-select v-model="userType" placeholder="用户类型" clearable @change="handleFilter">
          <el-option label="学员" value="STUDENT" />
          <el-option label="教练" value="COACH" />
          <el-option label="校区管理员" value="CAMPUS_ADMIN" />
          <el-option label="超级管理员" value="SUPER_ADMIN" />
        </el-select>
        <el-select v-model="campusFilter" placeholder="所属校区" clearable @change="handleFilter">
          <el-option
            v-for="campus in campuses"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
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
        :data="logs"
        border
        style="width: 100%"
      >
        <el-table-column prop="operationTime" label="操作时间" width="160" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getUserTypeType(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="campusName" label="所属校区" width="150" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getOperationTypeType(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
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
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作时间">{{ selectedLog.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ selectedLog.username }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          <el-tag :type="getUserTypeType(selectedLog.userType)">
            {{ getUserTypeText(selectedLog.userType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属校区">{{ selectedLog.campusName || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeType(selectedLog.operationType)">
            {{ getOperationTypeText(selectedLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ selectedLog.ipAddress || '未知' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h4>操作内容</h4>
        <pre style="background-color: #f5f5f5; padding: 10px; border-radius: 4px; white-space: pre-wrap;">
{{ selectedLog.operationContent }}
        </pre>
      </div>
      
      <div v-if="selectedLog.details" style="margin-top: 20px;">
        <h4>详细信息</h4>
        <pre style="background-color: #f5f5f5; padding: 10px; border-radius: 4px; white-space: pre-wrap;">
{{ selectedLog.details }}
        </pre>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSystemLogs, exportSystemLogs, getCampuses } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'SystemLogs',
  components: { Pagination },
  data() {
    return {
      logs: [],
      campuses: [],
      keyword: '',
      operationType: '',
      userType: '',
      campusFilter: '',
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      selectedLog: {}
    }
  },
  created() {
    this.getList()
    this.loadCampuses()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          keyword: this.keyword,
          operationType: this.operationType,
          userType: this.userType,
          campusId: this.campusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getSystemLogs(params)
        this.logs = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取系统日志失败:', error)
        this.$message.error('获取系统日志失败')
      } finally {
        this.loading = false
      }
    },
    async loadCampuses() {
      try {
        const response = await getCampuses({ limit: 1000 })
        this.campuses = response.data.records || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleViewDetail(row) {
      this.selectedLog = row
      this.detailDialogVisible = true
    },
    async handleExport() {
      try {
        const params = {
          keyword: this.keyword,
          operationType: this.operationType,
          userType: this.userType,
          campusId: this.campusFilter,
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
        'STUDENT': 'success',
        'COACH': 'warning',
        'CAMPUS_ADMIN': 'danger',
        'SUPER_ADMIN': 'info'
      }
      return types[userType] || 'info'
    },
    getUserTypeText(userType) {
      const texts = {
        'STUDENT': '学员',
        'COACH': '教练',
        'CAMPUS_ADMIN': '校区管理员',
        'SUPER_ADMIN': '超级管理员'
      }
      return texts[userType] || userType
    },
    getOperationTypeType(operationType) {
      const types = {
        'LOGIN': 'success',
        'REGISTER': 'warning',
        'UPDATE_PROFILE': 'info',
        'BOOK_COURSE': 'primary',
        'CANCEL_BOOKING': 'danger',
        'RECHARGE': 'success',
        'EVALUATE': 'info',
        'TOURNAMENT_REGISTER': 'warning',
        'CHANGE_COACH': 'danger'
      }
      return types[operationType] || 'info'
    },
    getOperationTypeText(operationType) {
      const texts = {
        'LOGIN': '登录',
        'REGISTER': '注册',
        'UPDATE_PROFILE': '修改信息',
        'BOOK_COURSE': '预约课程',
        'CANCEL_BOOKING': '取消预约',
        'RECHARGE': '充值',
        'EVALUATE': '评价',
        'TOURNAMENT_REGISTER': '报名比赛',
        'CHANGE_COACH': '更换教练'
      }
      return texts[operationType] || operationType
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
</style>