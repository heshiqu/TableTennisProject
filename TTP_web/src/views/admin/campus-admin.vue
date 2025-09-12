<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>校区管理员管理</span>
        <el-button type="primary" style="float: right;" @click="handleAdd">
          新增管理员
        </el-button>
      </div>

      <div class="filter-container">
        <el-input
          v-model="listQuery.name"
          placeholder="姓名"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-input
          v-model="listQuery.username"
          placeholder="用户名"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-select
          v-model="listQuery.campusId"
          placeholder="所属校区"
          clearable
          style="width: 200px"
          class="filter-item"
        >
          <el-option
            v-for="item in campusOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="listQuery.status"
          placeholder="状态"
          clearable
          style="width: 120px"
          class="filter-item"
        >
          <el-option label="启用" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button
          v-waves
          class="filter-item"
          type="primary"
          icon="el-icon-search"
          @click="handleFilter"
        >
          搜索
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
        <el-table-column label="姓名" prop="realName" align="center" />
        <el-table-column label="用户名" prop="username" align="center" />
        <el-table-column label="性别" align="center" width="60">
          <template slot-scope="{row}">
            <span>{{ row.gender === 'MALE' ? '男' : '女' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="年龄" prop="age" align="center" width="60" />
        <el-table-column label="电话" prop="phone" align="center" />
        <el-table-column label="邮箱" prop="email" align="center" />
        <el-table-column label="所属校区" prop="campusName" align="center" />
        <el-table-column label="操作" align="center" width="250">
          <template slot-scope="{row}">
            <el-button type="primary" size="mini" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button size="mini" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              size="mini"
              type="warning"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              size="mini"
              type="success"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button size="mini" type="danger" @click="handleResetPassword(row)">
              重置密码
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
    >
      <el-form
        ref="adminForm"
        :model="adminForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="adminForm.name" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="adminForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" />
        </el-form-item>
        <el-form-item label="所属校区" prop="campusId">
          <el-select v-model="adminForm.campusId" placeholder="请选择校区" style="width: 100%">
            <el-option
              v-for="item in campusOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="管理员详情"
      :visible.sync="detailVisible"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="姓名：">
          <span>{{ currentAdmin.realName }}</span>
        </el-form-item>
        <el-form-item label="用户名：">
          <span>{{ currentAdmin.username }}</span>
        </el-form-item>
        <el-form-item label="性别：">
          <span>{{ currentAdmin.gender === 'MALE' ? '男' : '女' }}</span>
        </el-form-item>
        <el-form-item label="年龄：">
          <span>{{ currentAdmin.age }}</span>
        </el-form-item>
        <el-form-item label="电话：">
          <span>{{ currentAdmin.phone }}</span>
        </el-form-item>
        <el-form-item label="邮箱：">
          <span>{{ currentAdmin.email }}</span>
        </el-form-item>
        <el-form-item label="所属校区：">
          <span>{{ currentAdmin.campusName }}</span>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { getCampusAdminList, addCampusAdmin, updateCampusAdmin, enableCampusAdmin, disableCampusAdmin, resetCampusAdminPassword } from '@/api/admin'
import { getCampusList } from '@/api/campus'
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusAdmin',
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
        name: undefined,
        username: undefined,
        campusId: undefined,
        status: undefined
      },
      campusOptions: [],
      dialogVisible: false,
      detailVisible: false,
      dialogTitle: '',
      adminForm: {
        id: undefined,
        name: '',
        username: '',
        phone: '',
        email: '',
        campusId: undefined
      },
      currentAdmin: {},
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
        email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
        campusId: [{ required: true, message: '请选择校区', trigger: 'change' }]
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.adminForm.id
    }
  },
  created() {
    this.getList()
    this.getCampusList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const response = await getCampusAdminList(this.listQuery)
        // 适配后端真实数据结构
        this.list = response.data.content || []
        this.total = response.data.totalElements || 0
      } catch (error) {
        // 只在开发环境打印详细错误，生产环境不显示
        if (process.env.NODE_ENV === 'development') {
          console.error('获取校区管理员列表失败:', error)
        }
        this.$message.error('获取校区管理员列表失败')
      } finally {
        this.listLoading = false
      }
    },
    async getCampusList() {
      try {
        const response = await getCampusList()
        this.campusOptions = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
        this.$message.error('获取校区列表失败')
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleAdd() {
      this.dialogTitle = '新增管理员'
      this.adminForm = {
        id: undefined,
        name: '',
        username: '',
        phone: '',
        email: '',
        campusId: undefined
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑管理员'
      this.adminForm = {
        id: row.id,
        name: row.realName,
        username: row.username,
        phone: row.phone,
        email: row.email,
        campusId: row.campusId
      }
      this.dialogVisible = true
    },
    handleView(row) {
      this.currentAdmin = row
      this.detailVisible = true
    },
    async handleDisable(row) {
      try {
        await this.$confirm('确认禁用该管理员？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await disableCampusAdmin(row.id)
        this.$message.success('禁用成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('禁用管理员失败:', error)
          this.$message.error('禁用管理员失败')
        }
      }
    },
    async handleEnable(row) {
      try {
        await this.$confirm('确认启用该管理员？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await enableCampusAdmin(row.id)
        this.$message.success('启用成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('启用管理员失败:', error)
          this.$message.error('启用管理员失败')
        }
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm('确认重置该管理员密码？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await resetCampusAdminPassword(row.id)
        this.$message.success('密码重置成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密码失败:', error)
          this.$message.error('重置密码失败')
        }
      }
    },
    handleSubmit() {
      this.$refs.adminForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEdit) {
              await updateCampusAdmin(this.adminForm)
              this.$message.success('更新成功')
            } else {
              await addCampusAdmin(this.adminForm)
              this.$message.success('新增成功')
            }
            this.dialogVisible = false
            this.getList()
          } catch (error) {
            console.error('提交失败:', error)
            this.$message.error('提交失败')
          }
        }
      })
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
</style>