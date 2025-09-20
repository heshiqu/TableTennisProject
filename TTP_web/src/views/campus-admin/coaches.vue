<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="教练姓名"
        style="width: 200px;"
        class="filter-item"
        @input="applyFilters"
      />
      <el-select v-model="listQuery.level" placeholder="教练级别" clearable style="width: 120px" class="filter-item" @change="applyFilters">
        <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 100px" class="filter-item" @change="applyFilters">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>

      <el-button
        :loading="downloadLoading"
        style="margin-left: 10px;"
        type="info"
        icon="el-icon-download"
        @click="handleDownload"
      >
        导出
      </el-button>
    </div>

    <el-table
      :key="tableKey"
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="序号" type="index" align="center" width="80">
        <template slot-scope="{$index}">
          <span>{{ (listQuery.page - 1) * listQuery.limit + $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="头像" width="80px" align="center">
        <template slot-scope="{row}">
          <el-avatar :src="row.avatar" :size="40" />
        </template>
      </el-table-column>
      <el-table-column label="姓名" min-width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.realName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户名" min-width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="60px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.gender | genderFilter }}</span>
        </template>
      </el-table-column>
      <el-table-column label="年龄" width="60px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.age }}</span>
        </template>
      </el-table-column>
      <el-table-column label="教练级别" width="100px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.level | levelFilter">
            {{ row.level | levelNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收费标准" width="100px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ row.hourlyRate }}/小时</span>
        </template>
      </el-table-column>
      <el-table-column label="学员数量" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.currentStudents }}/{{ row.maxStudents }}</span>
        </template>
      </el-table-column>
      <el-table-column label="电话" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="150px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="获奖信息" min-width="200px" align="center">
        <template slot-scope="{row}">
          <el-tooltip :content="row.awards" placement="top" :disabled="!row.awards">
            <span class="text-ellipsis">{{ row.awards || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'">
            {{ row.status === 'ACTIVE' ? '活跃' : row.status === 'PENDING' ? '待审核' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="350" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button size="mini" type="warning" @click="handleUpdate(row)">
              编辑
            </el-button>
          
          <!-- 待审核状态下显示通过和不通过按钮 -->
          <template v-if="row.status === 'PENDING'">
            <el-button size="mini" type="success" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button size="mini" type="danger" @click="handleReject(row)">
              不通过
            </el-button>
          </template>
          
          <!-- 非待审核且非禁用状态下显示禁用按钮 -->
          <el-button size="mini" type="danger" v-else-if="row.status !== 'INACTIVE'" @click="handleDisable(row)">
            禁用
          </el-button>
          
          <!-- 禁用状态下显示启用按钮 -->
          <el-button size="mini" type="success" v-else @click="handleEnable(row)">
            启用
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



    <!-- 编辑教练对话框 -->
    <el-dialog title="编辑教练" :visible.sync="editDialogVisible" width="500px">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateCoach">保存</el-button>
      </div>
    </el-dialog>

    <!-- 教练详情对话框 -->
    <el-dialog title="教练详情" :visible.sync="detailDialogVisible" width="600px">
      <div class="detail-content">
        <div class="detail-row">
          <div class="detail-label">头像</div>
          <div class="detail-value">
            <el-avatar :src="coachDetail.avatar" size="80" />
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-label">姓名</div>
          <div class="detail-value">{{ coachDetail.realName }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">用户名</div>
          <div class="detail-value">{{ coachDetail.username }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">性别</div>
          <div class="detail-value">{{ coachDetail.gender === 'MALE' ? '男' : '女' }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">年龄</div>
          <div class="detail-value">{{ coachDetail.age }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">电话</div>
          <div class="detail-value">{{ coachDetail.phone }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">邮箱</div>
          <div class="detail-value">{{ coachDetail.email }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">校区</div>
          <div class="detail-value">{{ coachDetail.campusName }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">状态</div>
          <div class="detail-value">
            <el-tag :type="coachDetail.status === 'ACTIVE' ? 'success' : coachDetail.status === 'PENDING' ? 'warning' : 'danger'">
              {{ coachDetail.status === 'ACTIVE' ? '活跃' : coachDetail.status === 'PENDING' ? '待审核' : '禁用' }}
            </el-tag>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-label">注册时间</div>
          <div class="detail-value">{{ coachDetail.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">更新时间</div>
          <div class="detail-value">{{ coachDetail.updatedAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">教练级别</div>
          <div class="detail-value">
            <el-tag :type="coachDetail.level | levelFilter">
              {{ coachDetail.level | levelNameFilter }}
            </el-tag>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-label">收费标准</div>
          <div class="detail-value">¥{{ coachDetail.hourlyRate }}/小时</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">学员数量</div>
          <div class="detail-value">{{ coachDetail.currentStudents }}/{{ coachDetail.maxStudents }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">获奖信息</div>
          <div class="detail-value">{{ coachDetail.awards }}</div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.text-ellipsis {
  display: inline-block;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-content {
  padding: 20px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-label {
  width: 100px;
  color: #606266;
  font-weight: 500;
}

.detail-value {
  flex: 1;
  color: #303133;
  word-break: break-word;
}
</style>

<script>
import request from '@/utils/request'
import { getCoachesByCampus, auditCoach, getCoachDetail } from '@/api/coach'
import { updateUserInfo } from '@/api/user'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusCoachManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    levelFilter(level) {
      const levelMap = {
        'SENIOR': 'danger',
        'INTERMEDIATE': 'warning',
        'JUNIOR': 'success'
      }
      return levelMap[level] || 'info'
    },
    levelNameFilter(level) {
      const levelNameMap = {
        'SENIOR': '高级',
        'INTERMEDIATE': '中级',
        'JUNIOR': '初级'
      }
      return levelNameMap[level] || level
    },
    auditStatusFilter(status) {
      const statusMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return statusMap[status]
    },
    auditStatusNameFilter(status) {
      const statusNameMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return statusNameMap[status]
    },
    genderFilter(gender) {
      const genderMap = {
        'MALE': '男',
        'FEMALE': '女'
      }
      return genderMap[gender] || '未知'
    }
  },
  data() {
    return {
      tableKey: 0,
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        name: undefined,
        level: undefined,
        status: undefined
      },
      levelOptions: [
        { key: 1, display_name: '高级教练' },
        { key: 2, display_name: '中级教练' },
        { key: 3, display_name: '初级教练' }
      ],
      statusOptions: [
        { key: 'ACTIVE', display_name: '活跃' },
        { key: 'PENDING', display_name: '待审核' },
        { key: 'INACTIVE', display_name: '禁用' }
      ],

      editDialogVisible: false,
      editForm: {
        id: undefined,
        realName: '',
        phone: '',
        email: ''
      },
      detailDialogVisible: false,
      coachDetail: {},
      editRules: {
        realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
        phone: [
          { required: true, message: '电话号码不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
      },
      downloadLoading: false
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const campusId = this.$store.state.user.user?.campusId
        if (!campusId) {
          this.$message.error('获取校区信息失败')
          this.listLoading = false
          return
        }
        
        const response = await getCoachesByCampus(campusId)
        if (response.code === 200) {
          // 根据筛选条件过滤数据
          let filteredList = response.data || []
          
          if (this.listQuery.name) {
            filteredList = filteredList.filter(coach => 
              coach.realName && coach.realName.toLowerCase().includes(this.listQuery.name.toLowerCase())
            )
          }
          
          if (this.listQuery.level) {
            const levelMap = { 1: 'SENIOR', 2: 'INTERMEDIATE', 3: 'JUNIOR' }
            const targetLevel = levelMap[this.listQuery.level]
            filteredList = filteredList.filter(coach => coach.level === targetLevel)
          }
          
          if (this.listQuery.status) {
            filteredList = filteredList.filter(coach => coach.status === this.listQuery.status)
          }
          
          // 前端分页处理
          this.total = filteredList.length
          const start = (this.listQuery.page - 1) * this.listQuery.limit
          const end = start + this.listQuery.limit
          this.list = filteredList.slice(start, end)
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
      this.listLoading = false
    },
    applyFilters() {
      this.listQuery.page = 1
      this.getList()
    },
    async handleDetail(row) {
      try {
        this.listLoading = true
        const response = await getCoachDetail(row.id)
        if (response.code === 200) {
          this.coachDetail = response.data
          this.detailDialogVisible = true
        }
      } catch (error) {
        console.error('获取教练详情失败:', error)
        this.$message.error('获取教练详情失败')
      } finally {
        this.listLoading = false
      }
    },
    handleUpdate(row) {
      this.editForm = {
        id: row.id,
        realName: row.realName,
        phone: row.phone,
        email: row.email
      }
      this.editDialogVisible = true
    },
    updateCoach() {
      this.$refs.editForm.validate(async(valid) => {
        if (valid) {
          try {
            // 构建符合要求的请求体，只包含需要修改的字段
            const requestBody = {
              realName: this.editForm.realName,
              phone: this.editForm.phone,
              email: this.editForm.email
            }
            
            const response = await updateUserInfo(this.editForm.id, requestBody)
            if (response.code === 200) {
              this.$message.success('更新成功')
              this.editDialogVisible = false
              this.getList()
            }
          } catch (error) {
            console.error('更新失败:', error)
          }
        }
      })
    },
    handleDisable(row) {
      this.$confirm('确认禁用该教练吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request({
          url: `/api/users/${row.id}`,
          method: 'delete'
        }).then(response => {
          if (response.code === 200) {
            // 从列表中移除该教练
            this.getList()
            this.$notify({
              title: '成功',
              message: '禁用成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleEnable(row) {
      this.$confirm('确认启用该教练吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request({
          url: `/api/users/${row.id}/status`,
          method: 'patch',
          params: { status: 'ACTIVE' }
        }).then(response => {
          if (response.code === 200) {
            // 刷新列表以更新状态
            this.getList()
            this.$notify({
              title: '成功',
              message: '启用成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    
    // 处理教练审核通过
    handleApprove(row) {
      this.$confirm('确认通过该教练的审核吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        request({
          url: `/api/users/${row.id}/status`,
          method: 'patch',
          params: { status: 'ACTIVE' }
        }).then(response => {
          if (response.code === 200) {
            // 刷新列表以更新状态
            this.getList()
            this.$notify({
              title: '成功',
              message: '审核通过成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    
    // 处理教练审核不通过
    handleReject(row) {
      this.$confirm('确认不通过该教练的审核吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(() => {
        request({
          url: `/api/users/${row.id}/status`,
          method: 'patch',
          params: { status: 'INACTIVE' }
        }).then(response => {
          if (response.code === 200) {
            // 刷新列表以更新状态
            this.getList()
            this.$notify({
              title: '成功',
              message: '审核不通过成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '姓名', '用户名', '性别', '年龄', '教练级别', '收费标准', '学员数量', '电话', '邮箱', '审核状态', '注册时间']
        const filterVal = ['id', 'realName', 'username', 'gender', 'age', 'level', 'hourlyRate', 'studentCount', 'phone', 'email', 'auditStatus', 'createTime']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '校区教练列表'
        })
        this.downloadLoading = false
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'gender') {
          return v[j] === 1 ? '男' : '女'
        } else if (j === 'level') {
          const levelMap = { 1: '高级', 2: '中级', 3: '初级' }
          return levelMap[v[j]]
        } else if (j === 'auditStatus') {
          const statusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已禁用' }
          return statusMap[v[j]]
        } else if (j === 'createTime') {
          return parseTime(v[j])
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>