<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>全部教练管理</span>
        <el-button style="float: right;" type="primary" @click="handleCreate">新增教练</el-button>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名、用户名或电话"
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        />
        <el-select v-model="campusFilter" placeholder="所属校区" clearable @change="handleFilter">
          <el-option
            v-for="campus in campuses"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
        <el-select v-model="levelFilter" placeholder="教练级别" clearable @change="handleFilter">
          <el-option label="高级教练" value="SENIOR" />
          <el-option label="中级教练" value="INTERMEDIATE" />
          <el-option label="初级教练" value="JUNIOR" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="审核状态" clearable @change="handleFilter">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已禁用" value="DISABLED" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="注册开始日期"
          end-placeholder="注册结束日期"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="coaches"
        border
        style="width: 100%"
      >
        <el-table-column label="头像" width="80">
          <template slot-scope="{row}">
            <img v-if="row.avatar" :src="row.avatar" class="avatar-image" />
            <div v-else class="avatar-placeholder">无</div>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="gender" label="性别" width="60">
          <template slot-scope="{row}">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="campusName" label="所属校区" width="150" />
        <el-table-column prop="level" label="教练级别" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="收费标准" width="100">
          <template slot-scope="{row}">
            ¥{{ row.hourlyRate }}/小时
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学员数量" width="80" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="handleApprove(row)">审核</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="text" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 'APPROVED' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" size="small" @click="handleResetPassword(row)">重置密码</el-button>
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

    <!-- 教练详情对话框 -->
    <el-dialog title="教练详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ selectedCoach.realName }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedCoach.username }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ selectedCoach.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ selectedCoach.age }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedCoach.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedCoach.email }}</el-descriptions-item>
        <el-descriptions-item label="所属校区">{{ selectedCoach.campusName }}</el-descriptions-item>
        <el-descriptions-item label="教练级别">
          <el-tag :type="getLevelType(selectedCoach.level)">
            {{ getLevelText(selectedCoach.level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收费标准">¥{{ selectedCoach.hourlyRate }}/小时</el-descriptions-item>
        <el-descriptions-item label="学员数量">{{ selectedCoach.studentCount }}人</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedCoach.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ selectedCoach.lastLoginTime || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h4>获奖经历</h4>
        <p>{{ selectedCoach.achievements || '暂无获奖经历' }}</p>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="教练审核" :visible.sync="approveDialogVisible" width="30%">
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="教练姓名">
          <span>{{ selectedCoach.realName }}</span>
        </el-form-item>
        <el-form-item label="教练级别" prop="level">
          <el-select v-model="approveForm.level" placeholder="选择教练级别" style="width: 100%">
            <el-option label="高级教练" value="SENIOR" />
            <el-option label="中级教练" value="INTERMEDIATE" />
            <el-option label="初级教练" value="JUNIOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见" prop="remark">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="danger" @click="handleReject">拒绝</el-button>
        <el-button type="primary" @click="handleApproveSubmit">通过</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCoaches, approveCoach, rejectCoach, toggleCoachStatus, resetCoachPassword, exportCoaches, getCampuses } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'AllCoaches',
  components: { Pagination },
  data() {
    return {
      coaches: [],
      campuses: [],
      keyword: '',
      campusFilter: '',
      levelFilter: '',
      statusFilter: '',
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      approveDialogVisible: false,
      selectedCoach: {},
      approveForm: {
        level: 'JUNIOR',
        remark: ''
      }
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
          campusId: this.campusFilter,
          level: this.levelFilter,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getAllCoaches(params)
        this.coaches = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取教练列表失败:', error)
        this.$message.error('获取教练列表失败')
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
    handleCreate() {
      this.$message.info('新增教练功能开发中...')
    },
    handleEdit(row) {
      this.$message.info('编辑教练功能开发中...')
    },
    handleViewDetail(row) {
      this.selectedCoach = row
      this.detailDialogVisible = true
    },
    handleApprove(row) {
      this.selectedCoach = row
      this.approveForm = {
        level: 'JUNIOR',
        remark: ''
      }
      this.approveDialogVisible = true
    },
    async handleApproveSubmit() {
      try {
        await approveCoach({
          coachId: this.selectedCoach.id,
          level: this.approveForm.level,
          remark: this.approveForm.remark
        })
        
        this.$message.success('审核通过')
        this.approveDialogVisible = false
        this.getList()
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },
    async handleReject() {
      try {
        await rejectCoach({
          coachId: this.selectedCoach.id,
          remark: this.approveForm.remark || '审核不通过'
        })
        
        this.$message.success('已拒绝')
        this.approveDialogVisible = false
        this.getList()
      } catch (error) {
        console.error('拒绝失败:', error)
        this.$message.error('拒绝失败')
      }
    },
    async handleToggleStatus(row) {
      const action = row.status === 'APPROVED' ? '禁用' : '启用'
      try {
        await this.$confirm(`确定要${action}教练 ${row.realName} 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await toggleCoachStatus(row.id)
        this.$message.success(`${action}成功`)
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}教练失败:`, error)
          this.$message.error(`${action}失败`)
        }
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm(`确定要重置 ${row.realName} 的密码吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await resetCoachPassword(row.id)
        this.$message.success('密码重置成功，初始密码为：123456')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密码失败:', error)
          this.$message.error('重置密码失败')
        }
      }
    },
    async handleExport() {
      try {
        const params = {
          keyword: this.keyword,
          campusId: this.campusFilter,
          level: this.levelFilter,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportCoaches(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    getLevelType(level) {
      const types = {
        'SENIOR': 'danger',
        'INTERMEDIATE': 'warning',
        'JUNIOR': 'success'
      }
      return types[level] || 'info'
    },
    getLevelText(level) {
      const texts = {
        'SENIOR': '高级教练',
        'INTERMEDIATE': '中级教练',
        'JUNIOR': '初级教练'
      }
      return texts[level] || level
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'info',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'DISABLED': 'warning'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        'DISABLED': '已禁用'
      }
      return texts[status] || status
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

.avatar-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}
</style>