<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>全部学员管理</span>
        <el-button style="float: right;" type="primary" @click="handleCreate">新增学员</el-button>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名、用户名或电话"
          style="width: 200px;"
          @input="handleFilter"
          clearable
        />
        <el-select v-model="campusFilter" placeholder="所属校区" clearable @change="handleFilter">
          <el-option
            v-for="campus in campuses"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
        <el-select v-model="genderFilter" placeholder="性别" clearable @change="handleFilter">
          <el-option label="男" value="MALE" />
          <el-option label="女" value="FEMALE" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleFilter">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button @click="resetFilter">重置</el-button>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="students"
        border
        style="width: 100%"
      >
        <el-table-column label="头像" width="60">
          <template slot-scope="{row}">
            <img v-if="row.avatar" :src="getAvatarUrl(row.avatar)" style="width: 40px; height: 40px; border-radius: 50%;" />
            <div v-else style="width: 40px; height: 40px; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
              <i class="el-icon-user" style="font-size: 20px; color: #999;"></i>
            </div>
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
        <el-table-column prop="balance" label="账户余额" width="100">
          <template slot-scope="{row}">
            ¥{{ (row.balance || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template slot-scope="{row}">
            {{ row.createdAt | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="text" size="small" @click="handleRecharge(row)">充值</el-button>
            <el-button type="text" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>


    </el-card>

    <!-- 学员详情对话框 -->
    <el-dialog title="学员详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="头像">
          <img v-if="selectedStudent.avatar" :src="getAvatarUrl(selectedStudent.avatar)" style="width: 60px; height: 60px; border-radius: 50%;" />
          <div v-else style="width: 60px; height: 60px; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
            <i class="el-icon-user" style="font-size: 30px; color: #999;"></i>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">{{ selectedStudent.realName }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedStudent.username }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ selectedStudent.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ selectedStudent.age }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedStudent.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedStudent.email }}</el-descriptions-item>
        <el-descriptions-item label="所属校区">{{ selectedStudent.campusName }}</el-descriptions-item>
        <el-descriptions-item label="账户余额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ (selectedStudent.balance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedStudent.createdAt | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ selectedStudent.lastLoginTime | formatDate || '从未登录' }}</el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog title="账户充值" :visible.sync="rechargeDialogVisible" width="30%">
      <el-form ref="rechargeForm" :model="rechargeForm" :rules="rechargeRules" label-width="100px">
        <el-form-item label="学员姓名">
          <span>{{ selectedStudent.realName }}</span>
        </el-form-item>
        <el-form-item label="当前余额">
          <span>¥{{ (selectedStudent.balance || 0).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="10000"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="rechargeForm.remark" type="textarea" :rows="3" placeholder="请输入充值备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecharge">确认充值</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllStudents, updateStudent, rechargeStudent, resetStudentPassword, exportStudents } from '@/api/super-admin'
import { getCampuses } from '@/api/super-admin'
import { getAvatarUrl } from '@/utils/avatar'

export default {
  name: 'AllStudents',
  components: {},
  data() {
    return {
      students: [],
      allStudents: [], // 存储所有学生数据
      campuses: [],
      keyword: '',
      campusFilter: '',
      genderFilter: '',
      statusFilter: '',

      loading: false,
      total: 0,
      detailDialogVisible: false,
      rechargeDialogVisible: false,
      selectedStudent: {},
      rechargeForm: {
        amount: 100,
        remark: ''
      },
      rechargeRules: {
        amount: [
          { required: true, message: '请输入充值金额', trigger: 'blur' },
          { type: 'number', min: 1, max: 10000, message: '充值金额必须在 1-10000 之间', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadCampuses()
  },
  methods: {
    getAvatarUrl,
    async getList() {
      this.loading = true
      try {
        // 首次加载获取所有学生，不传任何筛选参数
        const response = await getAllStudents({})
        this.allStudents = response.data || []
        this.students = [...this.allStudents] // 显示所有学生
        this.total = this.allStudents.length
      } catch (error) {
        console.error('获取学员列表失败:', error)
        this.$message.error('获取学员列表失败')
      } finally {
        this.loading = false
      }
    },
    
    applyFilter() {
      // 根据筛选条件过滤学生
      let filteredStudents = [...this.allStudents]
      
      if (this.campusFilter) {
        filteredStudents = filteredStudents.filter(student => 
          student.campusId === this.campusFilter
        )
      }
      
      if (this.genderFilter) {
        filteredStudents = filteredStudents.filter(student => 
          student.gender === this.genderFilter
        )
      }
      
      if (this.statusFilter) {
        filteredStudents = filteredStudents.filter(student => 
          student.status === this.statusFilter
        )
      }
      
      if (this.keyword) {
        const keyword = this.keyword.toLowerCase()
        filteredStudents = filteredStudents.filter(student => 
          student.realName.toLowerCase().includes(keyword) ||
          student.username.toLowerCase().includes(keyword) ||
          student.phone.includes(this.keyword)
        )
      }

      
      this.students = filteredStudents
      this.total = filteredStudents.length
    },
    async loadCampuses() {
      try {
        const response = await getCampuses()
        this.campuses = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    handleFilter() {
      this.applyFilter()
    },
    
    resetFilter() {
      this.keyword = ''
      this.campusFilter = ''
      this.genderFilter = ''
      this.statusFilter = ''
      this.students = [...this.allStudents]
      this.total = this.allStudents.length
    },
    handleCreate() {
      this.$message.info('新增学员功能开发中...')
    },
    handleEdit(row) {
      this.$message.info('编辑学员功能开发中...')
    },
    handleViewDetail(row) {
      this.selectedStudent = row
      this.detailDialogVisible = true
    },
    handleRecharge(row) {
      this.selectedStudent = row
      this.rechargeForm = {
        amount: 100,
        remark: ''
      }
      this.rechargeDialogVisible = true
    },
    async submitRecharge() {
      try {
        await this.$refs.rechargeForm.validate()
        
        await rechargeStudent({
          studentId: this.selectedStudent.id,
          amount: this.rechargeForm.amount,
          remark: this.rechargeForm.remark
        })
        
        this.$message.success(`成功为 ${this.selectedStudent.realName} 充值 ¥${this.rechargeForm.amount}`)
        this.rechargeDialogVisible = false
        this.getList()
      } catch (error) {
        console.error('充值失败:', error)
        this.$message.error('充值失败')
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm(`确定要重置 ${row.realName} 的密码吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await resetStudentPassword(row.id)
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
          gender: this.genderFilter,
          status: this.statusFilter
        }
        
        await exportStudents(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
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