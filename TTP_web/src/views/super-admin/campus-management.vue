<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>校区管理</span>
        <el-button style="float: right;" type="primary" @click="handleCreate">新增校区</el-button>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索校区名称或地址"
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        />
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleFilter">
          <el-option label="正常运营" value="ACTIVE" />
          <el-option label="暂停运营" value="INACTIVE" />
        </el-select>
        <el-button type="primary" @click="handleFilter">查询</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="campuses"
        border
        style="width: 100%"
      >
        <el-table-column prop="name" label="校区名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="所属校区" width="120">
          <template slot-scope="{row}">
            <span>{{ row.parentName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="text" size="small" @click="handleAssignManager(row)">分配管理员</el-button>
            <el-button type="text" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 'ACTIVE' ? '暂停' : '启用' }}
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

    <!-- 新增/编辑校区对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="50%">
      <el-form ref="campusForm" :model="campusForm" :rules="rules" label-width="100px">
        <el-form-item label="校区名称" prop="name">
          <el-input v-model="campusForm.name" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="campusForm.address" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="campusForm.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="campusForm.contactPhone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="campusForm.email" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerId">
          <el-select v-model="campusForm.managerId" placeholder="选择负责人">
            <el-option
              v-for="manager in availableManagers"
              :key="manager.id"
              :label="manager.realName"
              :value="manager.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="校区简介" prop="description">
          <el-input v-model="campusForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <!-- 分配管理员对话框 -->
    <el-dialog title="分配校区管理员" :visible.sync="managerDialogVisible" width="40%">
      <el-form :model="managerForm" label-width="100px">
        <el-form-item label="选择管理员">
          <el-select v-model="managerForm.managerId" placeholder="选择管理员" style="width: 100%">
            <el-option
              v-for="manager in availableManagers"
              :key="manager.id"
              :label="manager.realName + ' (' + manager.username + ')'"
              :value="manager.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="managerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitManager">确认分配</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCampusList as getCampuses, createCampus, updateCampus, deleteCampus, getManagers as getAvailableManagers, assignCampusManager } from '@/api/campus'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusManagement',
  components: { Pagination },
  data() {
    return {
      campuses: [],
      availableManagers: [],
      keyword: '',
      statusFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      dialogVisible: false,
      managerDialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      campusForm: {
          id: null,
          name: '',
          address: '',
          contactPerson: '',
          contactPhone: '',
          email: '',
          description: '',
          managerId: null,
          parentId: null,
          parentName: ''
        },
      managerForm: {
        campusId: null,
        managerId: null
      },
      rules: {
        name: [
          { required: true, message: '请输入校区名称', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入地址', trigger: 'blur' }
        ],
        contactPerson: [
          { required: true, message: '请输入联系人', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        managerId: [
          { required: true, message: '请选择负责人', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadAvailableManagers()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          name: this.keyword
        }
        
        const response = await getCampuses(params)
        this.campuses = response.data.content || []
        this.total = response.data.totalElements || 0
      } catch (error) {
        console.error('获取校区列表失败:', error)
        this.$message.error('获取校区列表失败')
      } finally {
        this.loading = false
      }
    },
    async loadAvailableManagers() {
      try {
        const response = await getAvailableManagers()
        this.availableManagers = response.data || []
      } catch (error) {
        console.error('获取可用管理员失败:', error)
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleCreate() {
      this.isEdit = false
      this.dialogTitle = '新增校区'
      this.campusForm = {
        id: null,
        name: '',
        code: '',
        address: '',
        phone: '',
        email: '',
        managerId: null,
        description: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑校区'
      this.campusForm = {
        id: row.id,
        name: row.name,
        code: row.code,
        address: row.address,
        phone: row.phone,
        email: row.email,
        managerId: row.managerId,
        description: row.description || ''
      }
      this.dialogVisible = true
    },
    handleView(row) {
      this.$router.push(`/super-admin/campus/${row.id}`)
    },
    handleAssignManager(row) {
      this.managerForm = {
        campusId: row.id,
        managerId: row.managerId
      }
      this.managerDialogVisible = true
    },
    async handleToggleStatus(row) {
      const action = row.status === 'ACTIVE' ? '暂停' : '启用'
      try {
        await this.$confirm(`确定要${action}校区 ${row.name} 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await updateCampus({
          id: row.id,
          status: row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
        })
        this.$message.success(`${action}成功`)
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}校区失败:`, error)
          this.$message.error(`${action}失败`)
        }
      }
    },
    async submitForm() {
      try {
        await this.$refs.campusForm.validate()
        
        if (this.isEdit) {
          await updateCampus(this.campusForm)
          this.$message.success('更新成功')
        } else {
          await createCampus(this.campusForm)
          this.$message.success('创建成功')
        }
        
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        console.error('保存校区失败:', error)
        this.$message.error('保存失败')
      }
    },
    async submitManager() {
      try {
        await assignCampusManager(this.managerForm)
        this.$message.success('分配管理员成功')
        this.managerDialogVisible = false
        this.getList()
      } catch (error) {
        console.error('分配管理员失败:', error)
        this.$message.error('分配失败')
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