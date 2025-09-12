<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>校区管理员管理</span>
        <el-button style="float: right;" type="primary" @click="handleCreate">新增管理员</el-button>
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
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleFilter">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="handleFilter">查询</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="administrators"
        border
        style="width: 100%"
      >
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="campusName" label="所属校区" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template slot-scope="{row}">
            <el-tag type="warning">校区管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="{row}">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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

    <!-- 新增/编辑管理员对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="50%">
      <el-form ref="adminForm" :model="adminForm" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="adminForm.realName" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="adminForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" />
        </el-form-item>
        <el-form-item label="所属校区" prop="campusId">
          <el-select v-model="adminForm.campusId" placeholder="选择校区" style="width: 100%">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="adminForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCampusAdminsPage, createCampusAdmin, updateCampusAdmin, toggleAdminStatus, resetAdminPassword, getCampuses } from '@/api/user'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusAdminManagement',
  components: { Pagination },
  data() {
    return {
      administrators: [],
      campuses: [],
      keyword: '',
      campusFilter: '',
      statusFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      adminForm: {
        id: null,
        realName: '',
        username: '',
        phone: '',
        email: '',
        campusId: null,
        password: ''
      },
      rules: {
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        campusId: [
          { required: true, message: '请选择所属校区', trigger: 'change' }
        ],
        password: [
          { required: true, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
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
          status: this.statusFilter
        }
        
        const response = await getCampusAdminsPage(params)
        this.administrators = response.records || []
        this.total = response.total || 0
      } catch (error) {
        console.error('获取管理员列表失败:', error)
        this.$message.error('获取管理员列表失败')
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
      this.isEdit = false
      this.dialogTitle = '新增校区管理员'
      this.adminForm = {
        id: null,
        realName: '',
        username: '',
        phone: '',
        email: '',
        campusId: null,
        password: '123456'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑校区管理员'
      this.adminForm = {
        id: row.id,
        realName: row.realName,
        username: row.username,
        phone: row.phone,
        email: row.email,
        campusId: row.campusId
      }
      this.dialogVisible = true
    },
    async handleToggleStatus(row) {
      const action = row.status === 'ACTIVE' ? '禁用' : '启用'
      try {
        await this.$confirm(`确定要${action}管理员 ${row.realName} 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await toggleAdminStatus(row.id)
        this.$message.success(`${action}成功`)
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}管理员失败:`, error)
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
        
        await resetAdminPassword(row.id)
        this.$message.success('密码重置成功，初始密码为：123456')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密码失败:', error)
          this.$message.error('重置密码失败')
        }
      }
    },
    async submitForm() {
      try {
        await this.$refs.adminForm.validate()
        
        if (this.isEdit) {
          await updateCampusAdmin(this.adminForm)
          this.$message.success('更新成功')
        } else {
          await createCampusAdmin(this.adminForm)
          this.$message.success('创建成功')
        }
        
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        console.error('保存管理员失败:', error)
        this.$message.error('保存失败')
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
</style>