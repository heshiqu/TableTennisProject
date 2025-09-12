<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>系统日志</span>
      </div>

      <div class="filter-container">
        <el-input
          v-model="listQuery.keyword"
          placeholder="搜索关键词"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-select
          v-model="listQuery.actionType"
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
          <el-option label="学员" value="STUDENT" />
          <el-option label="教练" value="COACH" />
          <el-option label="校区管理员" value="CAMPUS_ADMIN" />
          <el-option label="超级管理员" value="SUPER_ADMIN" />
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
        :data="logs"
        border
        fit
        highlight-current-row
        style="width: 100%;"
      >
        <el-table-column label="ID" prop="id" align="center" width="80" />
        <el-table-column label="操作时间" width="160" align="center">
          <template slot-scope="{row}">
            <span>{{ row.createdAt | parseTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" width="120" align="center">
          <template slot-scope="{row}">
            <span>{{ row.userName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户类型" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag :type="getUserTypeTag(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag :type="getActionTypeTag(row.actionType)">
              {{ getActionTypeText(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" min-width="200" align="left">
          <template slot-scope="{row}">
            <span>{{ row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column label="IP地址" width="120" align="center">
          <template slot-scope="{row}">
            <span>{{ row.ipAddress }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
          <template slot-scope="{row}">
            <el-button type="primary" size="mini" @click="handleView(row)">
              查看
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

    <!-- 日志详情对话框 -->
    <el-dialog
      title="日志详情"
      :visible.sync="dialogVisible"
      width="600px"
    >
      <el-form
        v-if="currentLog"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="ID:">
          <span>{{ currentLog.id }}</span>
        </el-form-item>
        <el-form-item label="操作时间:">
          <span>{{ currentLog.createdAt | parseTime }}</span>
        </el-form-item>
        <el-form-item label="操作人:">
          <span>{{ currentLog.userName }}</span>
        </el-form-item>
        <el-form-item label="用户类型:">
          <el-tag :type="getUserTypeTag(currentLog.userType)">
            {{ getUserTypeText(currentLog.userType) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作类型:">
          <el-tag :type="getActionTypeTag(currentLog.actionType)">
            {{ getActionTypeText(currentLog.actionType) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作描述:">
          <span>{{ currentLog.description }}</span>
        </el-form-item>
        <el-form-item label="IP地址:">
          <span>{{ currentLog.ipAddress }}</span>
        </el-form-item>
        <el-form-item label="用户代理:">
          <span>{{ currentLog.userAgent }}</span>
        </el-form-item>
        <el-form-item label="请求参数:">
          <pre>{{ formatRequestData(currentLog.requestData) }}</pre>
        </el-form-item>
        <el-form-item label="响应数据:">
          <pre>{{ formatResponseData(currentLog.responseData) }}</pre>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSystemLogs, exportSystemLogs } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusSystemLogs',
  components: { Pagination },
  data() {
    return {
      logs: [],
      total: 0,
      loading: false,
      listQuery: {
        page: 1,
        limit: 20,
        keyword: undefined,
        actionType: undefined,
        userType: undefined
      },
      dateRange: [],
      dialogVisible: false,
      currentLog: null
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
          ...this.listQuery,
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
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleView(row) {
      this.currentLog = row
      this.dialogVisible = true
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
    getUserTypeText(type) {
      const typeMap = {
        STUDENT: '学员',
        COACH: '教练',
        CAMPUS_ADMIN: '校区管理员',
        SUPER_ADMIN: '超级管理员'
      }
      return typeMap[type] || type
    },
    getUserTypeTag(type) {
      const tagMap = {
        STUDENT: 'info',
        COACH: 'success',
        CAMPUS_ADMIN: 'warning',
        SUPER_ADMIN: 'danger'
      }
      return tagMap[type] || ''
    },
    getActionTypeText(type) {
      const typeMap = {
        LOGIN: '登录',
        CREATE: '创建',
        UPDATE: '更新',
        DELETE: '删除',
        QUERY: '查询'
      }
      return typeMap[type] || type
    },
    getActionTypeTag(type) {
      const tagMap = {
        LOGIN: 'primary',
        CREATE: 'success',
        UPDATE: 'warning',
        DELETE: 'danger',
        QUERY: 'info'
      }
      return tagMap[type] || ''
    },
    formatRequestData(data) {
      try {
        return JSON.stringify(JSON.parse(data), null, 2)
      } catch {
        return data || '无'
      }
    },
    formatResponseData(data) {
      try {
        return JSON.stringify(JSON.parse(data), null, 2)
      } catch {
        return data || '无'
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

pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}
</style>