<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="学员姓名"
        style="width: 200px;"
        class="filter-item"
        @input="applyFilters"
      />
      <el-select v-model="listQuery.gender" placeholder="性别" clearable style="width: 90px" class="filter-item" @change="applyFilters">
        <el-option v-for="item in genderOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="状态" clearable class="filter-item" style="width: 110px" @change="applyFilters">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-button
        class="filter-item"
        style="margin-left: 10px;"
        type="success"
        icon="el-icon-plus"
        @click="handleCreate"
      >
        添加学员
      </el-button>
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
          <span>{{ $index + 1 }}</span>
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
      <el-table-column label="账户余额" width="100px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ Number(row.balance).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" class-name="status-col" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status | statusFilter">
            {{ row.status | statusNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">
            编辑
          </el-button>
          <el-button v-if="row.status === 'ACTIVE'" size="mini" type="warning" @click="handleModifyStatus(row, 'INACTIVE')">
            禁用
          </el-button>
          <el-button v-if="row.status === 'INACTIVE'" size="mini" type="success" @click="handleModifyStatus(row, 'ACTIVE')">
            启用
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
          <el-input v-model="temp.username" :disabled="dialogStatus==='update'" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="temp.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="temp.gender" class="filter-item" placeholder="请选择性别">
            <el-option v-for="item in genderOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="temp.age" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="temp.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="账户余额" prop="balance">
          <el-input-number v-model="temp.balance" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item v-if="dialogStatus==='update'" label="状态" prop="status">
          <el-select v-model="temp.status" class="filter-item" placeholder="请选择状态">
            <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
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

    <!-- 余额调整对话框 -->
    <el-dialog title="余额调整" :visible.sync="balanceDialogVisible" width="400px">
      <el-form ref="balanceForm" :model="balanceForm" :rules="balanceRules" label-width="100px">
        <el-form-item label="当前余额">
          <span>¥{{ Number(balanceForm.currentBalance).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="调整类型" prop="type">
          <el-select v-model="balanceForm.type" placeholder="请选择调整类型">
            <el-option label="充值" value="recharge" />
            <el-option label="扣款" value="deduct" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整金额" prop="amount">
          <el-input-number v-model="balanceForm.amount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="balanceForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="balanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="adjustBalance">确认调整</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentListByCampus, deleteStudent, adjustStudentBalance } from '@/api/student'
import { registerStudent } from '@/api/auth'
import { updateUserStatus, updateUserInfo } from '@/api/user'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusStudentManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        'INACTIVE': 'danger',
        'ACTIVE': 'success'
      }
      return statusMap[status] || 'info'
    },
    statusNameFilter(status) {
      const statusNameMap = {
        'INACTIVE': '禁用',
        'ACTIVE': '正常'
      }
      return statusNameMap[status] || '未知'
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
      originalList: [], // 保存原始数据用于过滤
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        name: undefined,
        gender: undefined,
        status: undefined
      },
      genderOptions: [
        { key: 'MALE', display_name: '男' },
        { key: 'FEMALE', display_name: '女' }
      ],
      statusOptions: [
        { key: 'ACTIVE', display_name: '正常' },
        { key: 'INACTIVE', display_name: '禁用' }
      ],
      temp: {
        id: undefined,
        username: '',
        realName: '',
        gender: 1,
        age: 18,
        phone: '',
        email: '',
        balance: 0,
        status: 1
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑学员',
        create: '添加学员'
      },
      rules: {
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
        realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
        phone: [
          { required: true, message: '电话号码不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
      },
      downloadLoading: false,
      balanceDialogVisible: false,
      balanceForm: {
        studentId: undefined,
        currentBalance: 0,
        type: 'recharge',
        amount: 0,
        remark: ''
      },
      balanceRules: {
        type: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
        amount: [
          { required: true, message: '请输入调整金额', trigger: 'blur' },
          { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
        ],
        remark: [{ required: true, message: '请输入备注信息', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        // 从vuex获取当前用户的校区ID
        const currentUser = this.$store.state.user.user
        if (!currentUser || !currentUser.campusId) {
          this.$message.error('无法获取用户校区信息')
          this.listLoading = false
          return
        }

        const campusId = currentUser.campusId
        const response = await getStudentListByCampus(campusId)
        if (response.code === 200) {
          this.originalList = response.data || []
          this.applyFilters()
        }
      } catch (error) {
        console.error('获取学员列表失败:', error)
        this.$message.error('获取学员列表失败')
      }
      this.listLoading = false
    },
    resetTemp() {
      this.temp = {
        id: undefined,
        username: '',
        realName: '',
        gender: 1,
        age: 18,
        phone: '',
        email: '',
        balance: 0,
        status: 1
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
          // 获取当前登录的校区管理员的campusId
          const currentUser = this.$store.state.user.user
          if (!currentUser || !currentUser.campusId) {
            this.$message.error('无法获取用户校区信息')
            return
          }
          
          // 构建符合要求的请求体
          const requestBody = {
            username: this.temp.username,
            password: 'jlu123456!', // 默认密码
            realName: this.temp.realName,
            gender: this.temp.gender,
            age: this.temp.age,
            phone: this.temp.phone,
            email: this.temp.email,
            userType: 'STUDENT',
            campusId: currentUser.campusId,
            level: 'SENIOR', // 默认级别
            awards: '', // 默认空
            initialBalance: this.temp.balance || 0,
            avatar: '' // 默认空
          }
          
          registerStudent(requestBody).then(response => {
            if (response.code === 200) {
              this.getList() // 重新获取列表数据
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
      this.temp = Object.assign({}, row) // copy obj
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          // 构建符合要求的请求体，不修改password、username、avatar、userType和campusId字段
          const requestBody = {
            realName: this.temp.realName,
            gender: this.temp.gender,
            age: this.temp.age,
            phone: this.temp.phone,
            email: this.temp.email,
            status: this.temp.status
          }
          
          updateUserInfo(this.temp.id, requestBody).then(response => {
            if (response.code === 200) {
              const index = this.list.findIndex(v => v.id === this.temp.id)
              this.list.splice(index, 1, response.data)
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
    handleModifyStatus(row, status) {
      const statusText = status === 'ACTIVE' ? '启用' : '禁用'
      this.$confirm(`确认${statusText}该学员吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 无论启用还是禁用，都使用updateUserStatus更新状态
        updateUserStatus(row.id, status).then(response => {
          if (response.code === 200) {
            // 只更新状态，不从列表中移除
            row.status = status
            this.$notify({
              title: '成功',
              message: `${statusText}成功`,
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleDelete(row, index) {
      this.$confirm('确认删除该学员吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteStudent(row.id).then(response => {
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
    handleDownload() {
      this.downloadLoading = true
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '用户名', '真实姓名', '性别', '年龄', '电话', '邮箱', '账户余额', '注册时间', '状态']
        const filterVal = ['id', 'username', 'realName', 'gender', 'age', 'phone', 'email', 'balance', 'createdAt', 'status']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '校区学员列表'
        })
        this.downloadLoading = false
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'gender') {
          return v[j] === 'MALE' ? '男' : (v[j] === 'FEMALE' ? '女' : '未知')
        } else if (j === 'status') {
          return v[j] === 'ACTIVE' ? '正常' : '禁用'
        } else if (j === 'createdAt') {
          return parseTime(v[j])
        } else if (j === 'balance') {
          return Number(v[j]).toFixed(2)
        } else {
          return v[j]
        }
      }))
    },
    applyFilters() {
       // 由于新API返回完整数据，在前端进行过滤
       let filteredList = this.originalList || []
       
       if (this.listQuery.name) {
         filteredList = filteredList.filter(item => 
           item.realName && item.realName.toLowerCase().includes(this.listQuery.name.toLowerCase()) ||
           item.username && item.username.toLowerCase().includes(this.listQuery.name.toLowerCase())
         )
       }
       
       if (this.listQuery.gender) {
         filteredList = filteredList.filter(item => item.gender === this.listQuery.gender)
       }
       
       if (this.listQuery.status !== undefined && this.listQuery.status !== '') {
         filteredList = filteredList.filter(item => item.status === this.listQuery.status)
       }
       
       this.list = filteredList
       this.total = filteredList.length
     },
    showBalanceDialog(row) {
      this.balanceForm = {
        studentId: row.id,
        currentBalance: row.balance,
        type: 'recharge',
        amount: 0,
        remark: ''
      }
      this.balanceDialogVisible = true
    },
    adjustBalance() {
      this.$refs.balanceForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await adjustStudentBalance(this.balanceForm)
            if (response.code === 200) {
              const index = this.list.findIndex(v => v.id === this.balanceForm.studentId)
              this.list[index].balance = response.data.newBalance
              // 同时更新原始数据
              const originalIndex = this.originalList.findIndex(v => v.id === this.balanceForm.studentId)
              if (originalIndex !== -1) {
                this.originalList[originalIndex].balance = response.data.newBalance
              }
              this.balanceDialogVisible = false
              this.$notify({
                title: '成功',
                message: '余额调整成功',
                type: 'success',
                duration: 2000
              })
            }
          } catch (error) {
            console.error('余额调整失败:', error)
          }
        }
      })
    }
  }
}
</script>