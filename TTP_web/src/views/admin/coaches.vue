<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ coachStatistics.total }}</div>
              <div class="stat-label">教练总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ coachStatistics.senior }}</div>
              <div class="stat-label">高级教练</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-medal"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ coachStatistics.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ coachStatistics.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="教练姓名"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select v-model="listQuery.campusId" placeholder="所属校区" clearable style="width: 150px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="listQuery.level" placeholder="教练等级" clearable style="width: 120px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="审核状态" clearable style="width: 120px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
      <el-button
        class="filter-item"
        style="margin-left: 10px;"
        type="success"
        icon="el-icon-plus"
        @click="handleCreate"
      >
        添加教练
      </el-button>
      <el-button
        class="filter-item"
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
      <el-table-column label="序号" prop="id" align="center" width="80">
        <template slot-scope="{row}">
          <span>{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column label="头像" width="80px" align="center">
        <template slot-scope="{row}">
          <img :src="row.avatar || defaultAvatar" class="user-avatar" @error="handleAvatarError">
        </template>
      </el-table-column>
      <el-table-column label="教练姓名" min-width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.realName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户名" min-width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.gender === 'MALE' ? '男' : '女' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="年龄" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.age }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属校区" min-width="150px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.campusName || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="电话" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.phone }}</span>
        </template>
      </el-table-column>

      <el-table-column label="注册时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" class-name="status-col" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">
            编辑
          </el-button>
          <el-button v-if="row.status === 0" size="mini" type="success" @click="handleAudit(row)">
            审核
          </el-button>
          <el-button v-if="row.status !== 1" size="mini" type="success" @click="handleModifyStatus(row, 1)">
            启用
          </el-button>
          <el-button v-if="row.status !== 0" size="mini" @click="handleModifyStatus(row, 0)">
            禁用
          </el-button>
          <el-button size="mini" type="danger" @click="handleDelete(row,$index)">
            删除
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="temp.username" placeholder="请输入用户名" :disabled="dialogStatus === 'update'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="temp.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogStatus === 'create'">
          <el-input v-model="temp.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="temp.gender" class="filter-item" placeholder="请选择性别">
            <el-option v-for="item in genderOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="temp.age" :min="1" :max="100" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="所属校区" prop="campusId">
          <el-select v-model="temp.campusId" class="filter-item" placeholder="请选择校区">
            <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练等级" prop="level">
          <el-select v-model="temp.level" class="filter-item" placeholder="请选择教练等级">
            <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费标准" prop="hourlyRate">
          <el-input-number v-model="temp.hourlyRate" :min="0" :precision="0" :step="10" placeholder="请输入收费标准" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="temp.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="获奖信息" prop="achievements">
          <el-input v-model="temp.achievements" type="textarea" :rows="3" placeholder="请输入获奖信息描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="temp.status">
            <el-radio :label="0">待审核</el-radio>
            <el-radio :label="1">已通过</el-radio>
            <el-radio :label="2">未通过</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">
          确认
        </el-button>
      </div>
    </el-dialog>

    <el-dialog title="审核教练" :visible.sync="auditDialogVisible">
      <el-form
        ref="auditForm"
        :model="auditForm"
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="教练姓名">
          <span>{{ auditForm.realName }}</span>
        </el-form-item>
        <el-form-item label="教练等级" prop="level">
          <el-select v-model="auditForm.level" class="filter-item" placeholder="请选择教练等级">
            <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费标准" prop="hourlyRate">
          <el-input-number v-model="auditForm.hourlyRate" :min="0" :precision="0" :step="10" placeholder="请输入收费标准" />
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">
          取消
        </el-button>
        <el-button type="danger" @click="handleAuditReject">
          拒绝
        </el-button>
        <el-button type="success" @click="handleAuditPass">
          通过
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCoachList, createCoach, updateCoach, deleteCoach, auditCoach, getTotalCoachCount } from '@/api/coach'
import { getCampusList } from '@/api/campus'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CoachManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return statusMap[status]
    },
    statusNameFilter(status) {
      const statusNameMap = {
        0: '待审核',
        1: '已通过',
        2: '未通过'
      }
      return statusNameMap[status]
    },
    levelFilter(level) {
      const levelMap = {
        1: 'danger',
        2: 'warning',
        3: 'success'
      }
      return levelMap[level]
    },
    levelNameFilter(level) {
      const levelNameMap = {
        1: '高级',
        2: '中级',
        3: '初级'
      }
      return levelNameMap[level]
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
        campusId: undefined,
        level: undefined,
        status: undefined
      },
      campusOptions: [],
      levelOptions: [
        { key: 1, display_name: '高级教练' },
        { key: 2, display_name: '中级教练' },
        { key: 3, display_name: '初级教练' }
      ],
      statusOptions: [
        { key: 0, display_name: '待审核' },
        { key: 1, display_name: '已通过' },
        { key: 2, display_name: '未通过' }
      ],
      defaultAvatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
      temp: {
        id: undefined,
        username: '',
        realName: '',
        password: '',
        gender: 1,
        age: 30,
        campusId: undefined,
        level: 3,
        hourlyRate: 80,
        phone: '',
        email: '',
        achievements: '',
        status: 0
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑教练',
        create: '添加教练'
      },
      rules: {
        username: [
          { required: true, message: '用户名不能为空', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
          { min: 8, max: 16, message: '长度在 8 到 16 个字符', trigger: 'blur' },
          { pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/, message: '密码必须包含字母、数字和特殊字符', trigger: 'blur' }
        ],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
        campusId: [{ required: true, message: '请选择所属校区', trigger: 'change' }],
        level: [{ required: true, message: '请选择教练等级', trigger: 'change' }],
        hourlyRate: [{ required: true, message: '请输入收费标准', trigger: 'blur' }],
        phone: [
          { required: true, message: '联系电话不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        achievements: [{ required: true, message: '获奖信息不能为空', trigger: 'blur' }]
      },
      auditDialogVisible: false,
      auditForm: {
        id: undefined,
        realName: '',
        level: 3,
        hourlyRate: 80,
        auditRemark: ''
      },
      coachStatistics: {
        total: 0,
        senior: 0,
        approved: 0,
        pending: 0
      }
    }
  },
  created() {
    this.getList()
    this.loadCampusList()
    this.loadCoachStatistics()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        // 将前端page(从1开始)转换为后端page(从0开始)
        const queryParams = {
          ...this.listQuery,
          page: this.listQuery.page - 1
        }
        const response = await getCoachList(queryParams)
        if (response.code === 200) {
          // 适配后端真实数据结构
          this.list = response.data.content || []
          this.total = response.data.totalElements || 0
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
      this.listLoading = false
    },
    async loadCampusList() {
      try {
        const response = await getCampusList({ page: 1, limit: 100 })
        if (response.code === 200) {
          this.campusOptions = response.data.records || []
        }
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    loadCoachStatistics() {
      // 获取教练总数
      getTotalCoachCount().then(response => {
        this.coachStatistics.total = response.data
      }).catch(error => {
        console.error('获取教练总数失败:', error)
      })
      
      // 获取高级教练数量
      getCoachList({ level: 4, page: 1, limit: 1 }).then(response => {
        this.coachStatistics.senior = response.data.total
      }).catch(error => {
        console.error('获取高级教练数量失败:', error)
      })
      
      // 获取已通过教练数量
      getCoachList({ status: 'APPROVED', page: 1, limit: 1 }).then(response => {
        this.coachStatistics.approved = response.data.total
      }).catch(error => {
        console.error('获取已通过教练数量失败:', error)
      })
      
      // 获取待审核教练数量
      getCoachList({ status: 'PENDING', page: 1, limit: 1 }).then(response => {
        this.coachStatistics.pending = response.data.total
      }).catch(error => {
        console.error('获取待审核教练数量失败:', error)
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleAvatarError(e) {
      e.target.src = this.defaultAvatar
    },
    handleModifyStatus(row, status) {
      this.$message({
        message: '操作成功',
        type: 'success'
      })
      row.status = status
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        username: '',
        realName: '',
        password: '',
        gender: 1,
        age: 30,
        campusId: undefined,
        level: 3,
        hourlyRate: 80,
        phone: '',
        email: '',
        achievements: '',
        status: 0
      }
    },
    handleCreate() {
      this.resetTemp()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          createCoach(this.temp).then(response => {
            if (response.code === 200) {
              this.list.unshift(response.data)
              this.dialogFormVisible = false
              this.$notify({
                title: '成功',
                message: '创建成功',
                type: 'success',
                duration: 2000
              })
            }
          })
        }
      })
    },
    handleUpdate(row) {
      this.temp = Object.assign({}, row)
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          const tempData = Object.assign({}, this.temp)
          delete tempData.password
          updateCoach(tempData).then(response => {
            if (response.code === 200) {
              const index = this.list.findIndex(v => v.id === this.temp.id)
              this.list.splice(index, 1, this.temp)
              this.dialogFormVisible = false
              this.$notify({
                title: '成功',
                message: '更新成功',
                type: 'success',
                duration: 2000
              })
            }
          })
        }
      })
    },
    handleDelete(row, index) {
      this.$confirm('确认删除该教练吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCoach(row.id).then(response => {
          if (response.code === 200) {
            this.list.splice(index, 1)
            this.$notify({
              title: '成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    handleAudit(row) {
      this.auditForm = {
        id: row.id,
        realName: row.realName,
        level: row.level || 3,
        hourlyRate: row.hourlyRate || 80,
        auditRemark: ''
      }
      this.auditDialogVisible = true
    },
    handleAuditPass() {
      auditCoach({
        id: this.auditForm.id,
        status: 1,
        level: this.auditForm.level,
        hourlyRate: this.auditForm.hourlyRate,
        auditRemark: this.auditForm.auditRemark
      }).then(response => {
        if (response.code === 200) {
          const index = this.list.findIndex(v => v.id === this.auditForm.id)
          this.list[index].status = 1
          this.list[index].level = this.auditForm.level
          this.list[index].hourlyRate = this.auditForm.hourlyRate
          this.auditDialogVisible = false
          this.$notify({
            title: '成功',
            message: '审核通过',
            type: 'success',
            duration: 2000
          })
        }
      })
    },
    handleAuditReject() {
      auditCoach({
        id: this.auditForm.id,
        status: 2,
        auditRemark: this.auditForm.auditRemark
      }).then(response => {
        if (response.code === 200) {
          const index = this.list.findIndex(v => v.id === this.auditForm.id)
          this.list[index].status = 2
          this.auditDialogVisible = false
          this.$notify({
            title: '成功',
            message: '审核拒绝',
            type: 'success',
            duration: 2000
          })
        }
      })
    },
    handleDownload() {
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['教练姓名', '用户名', '性别', '年龄', '所属校区', '教练等级', '收费标准', '电话', '邮箱', '学员数量', '注册时间', '审核状态']
        const filterVal = ['realName', 'username', 'gender', 'age', 'campusName', 'level', 'hourlyRate', 'phone', 'email', 'studentCount', 'createTime', 'status']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '教练列表'
        })
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'gender') {
          return v[j] === 1 ? '男' : '女'
        } else if (j === 'level') {
          const levelNameMap = { 1: '高级', 2: '中级', 3: '初级' }
          return levelNameMap[v[j]]
        } else if (j === 'status') {
          const statusNameMap = { 0: '待审核', 1: '已通过', 2: '未通过' }
          return statusNameMap[v[j]]
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>

<style lang="scss" scoped>
.statistics-row {
  margin-bottom: 20px;
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        
        i {
          font-size: 24px;
          color: white;
        }
      }
      
      .stat-info {
        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
    
    &:nth-child(1) .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &:nth-child(2) .stat-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    &:nth-child(3) .stat-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    
    &:nth-child(4) .stat-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }
}

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    display: inline-block;
    vertical-align: middle;
    margin-bottom: 10px;
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
</style>