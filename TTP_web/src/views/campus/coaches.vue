<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="教练姓名"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select v-model="listQuery.level" placeholder="教练级别" clearable style="width: 120px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="审核状态" clearable style="width: 120px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in auditStatusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
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
      <el-table-column label="序号" prop="id" align="center" width="80">
        <template slot-scope="{row}">
          <span>{{ row.id }}</span>
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
          <span>{{ row.studentCount }}</span>
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
      <el-table-column label="审核状态" class-name="status-col" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.auditStatus | auditStatusFilter">
            {{ row.auditStatus | auditStatusNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.auditStatus === 0" size="mini" type="success" @click="handleAudit(row)">
            审核
          </el-button>
          <el-button v-if="row.auditStatus === 1" size="mini" type="warning" @click="handleUpdate(row)">
            编辑
          </el-button>
          <el-button v-if="row.auditStatus === 1" size="mini" type="danger" @click="handleDisable(row)">
            禁用
          </el-button>
          <el-button v-if="row.auditStatus === 2" size="mini" type="success" @click="handleEnable(row)">
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

    <!-- 审核对话框 -->
    <el-dialog title="教练审核" :visible.sync="auditDialogVisible" width="500px">
      <el-form ref="auditForm" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="教练姓名">
          <span>{{ auditForm.realName }}</span>
        </el-form-item>
        <el-form-item label="教练级别" prop="level">
          <el-select v-model="auditForm.level" placeholder="请选择教练级别">
            <el-option label="高级教练" :value="1" />
            <el-option label="中级教练" :value="2" />
            <el-option label="初级教练" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费标准" prop="hourlyRate">
          <el-input-number v-model="auditForm.hourlyRate" :min="0" :max="500" :step="10" />
          <span style="margin-left: 10px;">元/小时</span>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="rejectCoach">拒绝</el-button>
        <el-button type="success" @click="approveCoach">通过</el-button>
      </div>
    </el-dialog>

    <!-- 编辑教练对话框 -->
    <el-dialog title="编辑教练" :visible.sync="editDialogVisible" width="500px">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="教练级别" prop="level">
          <el-select v-model="editForm.level" placeholder="请选择教练级别">
            <el-option label="高级教练" :value="1" />
            <el-option label="中级教练" :value="2" />
            <el-option label="初级教练" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费标准" prop="hourlyRate">
          <el-input-number v-model="editForm.hourlyRate" :min="0" :max="500" :step="10" />
          <span style="margin-left: 10px;">元/小时</span>
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
  </div>
</template>

<script>
import { getCoachList, updateCoach, auditCoach } from '@/api/coach'
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
        1: '男',
        2: '女'
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
      auditStatusOptions: [
        { key: 0, display_name: '待审核' },
        { key: 1, display_name: '已通过' },
        { key: 2, display_name: '已拒绝' }
      ],
      auditDialogVisible: false,
      auditForm: {
        id: undefined,
        realName: '',
        level: 1,
        hourlyRate: 150,
        auditRemark: ''
      },
      auditRules: {
        level: [{ required: true, message: '请选择教练级别', trigger: 'change' }],
        hourlyRate: [{ required: true, message: '请输入收费标准', trigger: 'blur' }],
        auditRemark: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
      },
      editDialogVisible: false,
      editForm: {
        id: undefined,
        realName: '',
        level: 1,
        hourlyRate: 150,
        phone: '',
        email: ''
      },
      editRules: {
        realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
        level: [{ required: true, message: '请选择教练级别', trigger: 'change' }],
        hourlyRate: [{ required: true, message: '请输入收费标准', trigger: 'blur' }],
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
        const response = await getCoachList(this.listQuery)
        if (response.code === 200) {
          this.list = response.data.records
          this.total = response.data.total
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
      this.listLoading = false
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDetail(row) {
      this.$router.push(`/campus/coaches/${row.id}`)
    },
    handleAudit(row) {
      this.auditForm = {
        id: row.id,
        realName: row.realName,
        level: 2,
        hourlyRate: 150,
        auditRemark: ''
      }
      this.auditDialogVisible = true
    },
    async approveCoach() {
      this.$refs.auditForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await auditCoach({
              id: this.auditForm.id,
              action: 'approve',
              level: this.auditForm.level,
              hourlyRate: this.auditForm.hourlyRate,
              auditRemark: this.auditForm.auditRemark
            })
            if (response.code === 200) {
              this.$message.success('审核通过')
              this.auditDialogVisible = false
              this.getList()
            }
          } catch (error) {
            console.error('审核失败:', error)
          }
        }
      })
    },
    async rejectCoach() {
      this.$refs.auditForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await auditCoach({
              id: this.auditForm.id,
              action: 'reject',
              auditRemark: this.auditForm.auditRemark
            })
            if (response.code === 200) {
              this.$message.success('已拒绝')
              this.auditDialogVisible = false
              this.getList()
            }
          } catch (error) {
            console.error('拒绝失败:', error)
          }
        }
      })
    },
    handleUpdate(row) {
      this.editForm = {
        id: row.id,
        realName: row.realName,
        level: row.level,
        hourlyRate: row.hourlyRate,
        phone: row.phone,
        email: row.email
      }
      this.editDialogVisible = true
    },
    updateCoach() {
      this.$refs.editForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await updateCoach(this.editForm)
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
        updateCoach({ id: row.id, auditStatus: 3 }).then(response => {
          if (response.code === 200) {
            row.auditStatus = 3
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
        updateCoach({ id: row.id, auditStatus: 1 }).then(response => {
          if (response.code === 200) {
            row.auditStatus = 1
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