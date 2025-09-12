<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="学员姓名"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select v-model="listQuery.campusId" placeholder="所属校区" clearable style="width: 150px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="listQuery.gender" placeholder="性别" clearable style="width: 100px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in genderOptions" :key="item.key" :label="item.display_name" :value="item.key" />
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
        添加学员
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
      <el-table-column label="学员姓名" min-width="120px" align="center">
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
      <el-table-column label="邮箱" min-width="180px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="账户余额" width="100px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ row.balance || 0 }}</span>
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
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">
            编辑
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
        <el-form-item label="电话" prop="phone">
          <el-input v-model="temp.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="账户余额" prop="balance" v-if="dialogStatus === 'update'">
          <el-input-number v-model="temp.balance" :min="0" :precision="2" :step="0.1" placeholder="请输入账户余额" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="temp.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
  </div>
</template>

<script>
import { getStudentList, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { getCampusList } from '@/api/campus'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'StudentManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        1: 'success',
        0: 'info'
      }
      return statusMap[status]
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
        gender: undefined
      },
      campusOptions: [],
      genderOptions: [
        { key: 1, display_name: '男' },
        { key: 2, display_name: '女' }
      ],
      defaultAvatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
      temp: {
        id: undefined,
        username: '',
        realName: '',
        password: '',
        gender: 1,
        age: 18,
        campusId: undefined,
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
        phone: [
          { required: true, message: '联系电话不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadCampusList()
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
        const response = await getStudentList(queryParams)
        if (response.code === 200) {
          // 适配后端真实数据结构
          this.list = response.data.content || []
          this.total = response.data.totalElements || 0
        }
      } catch (error) {
        console.error('获取学员列表失败:', error)
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
        age: 18,
        campusId: undefined,
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
          createStudent(this.temp).then(response => {
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
          updateStudent(tempData).then(response => {
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
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['学员姓名', '用户名', '性别', '年龄', '所属校区', '电话', '邮箱', '账户余额', '注册时间', '状态']
        const filterVal = ['realName', 'username', 'gender', 'age', 'campusName', 'phone', 'email', 'balance', 'createTime', 'status']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '学员列表'
        })
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'gender') {
          return v[j] === 1 ? '男' : '女'
        } else if (j === 'status') {
          return v[j] === 1 ? '正常' : '禁用'
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>

<style lang="scss" scoped>
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