<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>比赛管理</span>
        <el-button type="primary" style="float: right;" @click="handleCreate">创建比赛</el-button>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索比赛名称"
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
        <el-select v-model="statusFilter" placeholder="比赛状态" clearable @change="handleFilter">
          <el-option label="未开始" value="UPCOMING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="比赛类型" clearable @change="handleFilter">
          <el-option label="校内比赛" value="INTERNAL" />
          <el-option label="校际比赛" value="EXTERNAL" />
          <el-option label="友谊赛" value="FRIENDLY" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tournaments"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="比赛名称" width="150" />
        <el-table-column prop="campusName" label="所属校区" width="120" />
        <el-table-column prop="type" label="比赛类型" width="100">
          <template slot-scope="{row}">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="maxParticipants" label="最大参赛人数" width="100" />
        <el-table-column prop="currentParticipants" label="已报名人数" width="100" />
        <el-table-column prop="registrationFee" label="报名费用" width="100">
          <template slot-scope="{row}">
            ¥{{ row.registrationFee }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleManageParticipants(row)">管理参赛者</el-button>
            <el-button v-if="row.status === 'UPCOMING'" type="text" size="small" @click="handleCancel(row)">取消</el-button>
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

    <!-- 比赛详情对话框 -->
    <el-dialog title="比赛详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="比赛名称">{{ selectedTournament.name }}</el-descriptions-item>
        <el-descriptions-item label="所属校区">{{ selectedTournament.campusName }}</el-descriptions-item>
        <el-descriptions-item label="比赛类型">{{ getTypeText(selectedTournament.type) }}</el-descriptions-item>
        <el-descriptions-item label="比赛时间">{{ selectedTournament.startDate }} 至 {{ selectedTournament.endDate }}</el-descriptions-item>
        <el-descriptions-item label="最大参赛人数">{{ selectedTournament.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="已报名人数">{{ selectedTournament.currentParticipants }}</el-descriptions-item>
        <el-descriptions-item label="报名费用">¥{{ selectedTournament.registrationFee }}</el-descriptions-item>
        <el-descriptions-item label="比赛状态">
          <el-tag :type="getStatusType(selectedTournament.status)">
            {{ getStatusText(selectedTournament.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="比赛地点">{{ selectedTournament.location || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedTournament.createTime }}</el-descriptions-item>
        <el-descriptions-item label="比赛描述" :span="2">{{ selectedTournament.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="比赛规则" :span="2">{{ selectedTournament.rules || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 创建/编辑比赛对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="createDialogVisible" width="50%">
      <el-form ref="tournamentForm" :model="tournamentForm" :rules="rules" label-width="100px">
        <el-form-item label="比赛名称" prop="name">
          <el-input v-model="tournamentForm.name" />
        </el-form-item>
        <el-form-item label="所属校区" prop="campusId">
          <el-select v-model="tournamentForm.campusId" placeholder="请选择校区" style="width: 100%">
            <el-option
              v-for="campus in campuses"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="比赛类型" prop="type">
          <el-select v-model="tournamentForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="校内比赛" value="INTERNAL" />
            <el-option label="校际比赛" value="EXTERNAL" />
            <el-option label="友谊赛" value="FRIENDLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="比赛时间" prop="dateRange">
          <el-date-picker
            v-model="tournamentForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大参赛人数" prop="maxParticipants">
          <el-input-number v-model="tournamentForm.maxParticipants" :min="1" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报名费用" prop="registrationFee">
          <el-input-number v-model="tournamentForm.registrationFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="比赛地点" prop="location">
          <el-input v-model="tournamentForm.location" />
        </el-form-item>
        <el-form-item label="比赛描述" prop="description">
          <el-input v-model="tournamentForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="比赛规则" prop="rules">
          <el-input v-model="tournamentForm.rules" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 管理参赛者对话框 -->
    <el-dialog title="管理参赛者" :visible.sync="participantsDialogVisible" width="70%">
      <div class="participants-header">
        <span>比赛：{{ selectedTournament.name }}</span>
        <span>已报名：{{ participants.length }}/{{ selectedTournament.maxParticipants }}</span>
      </div>
      
      <el-table
        v-loading="participantsLoading"
        :data="participants"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentName" label="学员姓名" width="120" />
        <el-table-column prop="studentPhone" label="联系电话" width="120" />
        <el-table-column prop="registrationTime" label="报名时间" width="160" />
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="row.paymentStatus === 'PAID' ? 'success' : 'danger'">
              {{ row.paymentStatus === 'PAID' ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleRemoveParticipant(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="participantsDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllTournaments, createTournament, updateTournament, cancelTournament, getTournamentParticipants, removeParticipant } from '@/api/super-admin'
import { getCampuses } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'TournamentManagement',
  components: { Pagination },
  data() {
    return {
      tournaments: [],
      campuses: [],
      keyword: '',
      campusFilter: '',
      statusFilter: '',
      typeFilter: '',
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      createDialogVisible: false,
      participantsDialogVisible: false,
      participantsLoading: false,
      selectedTournament: {},
      participants: [],
      tournamentForm: {
        name: '',
        campusId: '',
        type: 'INTERNAL',
        dateRange: [],
        maxParticipants: 50,
        registrationFee: 0,
        location: '',
        description: '',
        rules: ''
      },
      rules: {
        name: [{ required: true, message: '请输入比赛名称', trigger: 'blur' }],
        campusId: [{ required: true, message: '请选择所属校区', trigger: 'change' }],
        type: [{ required: true, message: '请选择比赛类型', trigger: 'change' }],
        dateRange: [{ required: true, message: '请选择比赛时间', trigger: 'change' }],
        maxParticipants: [{ required: true, message: '请输入最大参赛人数', trigger: 'blur' }],
        registrationFee: [{ required: true, message: '请输入报名费用', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.tournamentForm.id ? '编辑比赛' : '创建比赛'
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
          status: this.statusFilter,
          type: this.typeFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getAllTournaments(params)
        this.tournaments = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取比赛列表失败:', error)
        this.$message.error('获取比赛列表失败')
      } finally {
        this.loading = false
      }
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
      this.listQuery.page = 1
      this.getList()
    },
    
    handleCreate() {
      this.tournamentForm = {
        name: '',
        campusId: '',
        type: 'INTERNAL',
        dateRange: [],
        maxParticipants: 50,
        registrationFee: 0,
        location: '',
        description: '',
        rules: ''
      }
      this.createDialogVisible = true
    },
    
    handleEdit(row) {
      this.tournamentForm = {
        ...row,
        dateRange: [row.startDate, row.endDate]
      }
      this.createDialogVisible = true
    },
    
    handleViewDetail(row) {
      this.selectedTournament = row
      this.detailDialogVisible = true
    },
    
    async handleManageParticipants(row) {
      this.selectedTournament = row
      this.participantsDialogVisible = true
      await this.loadParticipants(row.id)
    },
    
    async loadParticipants(tournamentId) {
      this.participantsLoading = true
      try {
        const response = await getTournamentParticipants(tournamentId)
        this.participants = response.data || []
      } catch (error) {
        console.error('获取参赛者列表失败:', error)
        this.$message.error('获取参赛者列表失败')
      } finally {
        this.participantsLoading = false
      }
    },
    
    async handleSubmit() {
      try {
        await this.$refs.tournamentForm.validate()
        
        const formData = {
          ...this.tournamentForm,
          startDate: this.tournamentForm.dateRange[0],
          endDate: this.tournamentForm.dateRange[1]
        }
        delete formData.dateRange
        
        if (this.tournamentForm.id) {
          await updateTournament(this.tournamentForm.id, formData)
          this.$message.success('比赛更新成功')
        } else {
          await createTournament(formData)
          this.$message.success('比赛创建成功')
        }
        
        this.createDialogVisible = false
        this.getList()
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败')
      }
    },
    
    async handleCancel(row) {
      try {
        await this.$confirm('取消该比赛？此操作不可恢复！', '警告', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await cancelTournament(row.id)
        this.$message.success('比赛已取消')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('取消失败')
        }
      }
    },
    
    async handleRemoveParticipant(row) {
      try {
        await this.$confirm('移除该参赛者？', '提示', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await removeParticipant(this.selectedTournament.id, row.id)
        this.$message.success('参赛者已移除')
        await this.loadParticipants(this.selectedTournament.id)
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('移除失败')
        }
      }
    },
    
    handleExport() {
      // 导出逻辑
      this.$message.info('导出功能开发中...')
    },
    
    getTypeText(type) {
      const typeMap = {
        'INTERNAL': '校内比赛',
        'EXTERNAL': '校际比赛',
        'FRIENDLY': '友谊赛'
      }
      return typeMap[type] || type
    },
    
    getStatusType(status) {
      const statusMap = {
        'UPCOMING': 'info',
        'ONGOING': 'success',
        'COMPLETED': 'info',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || ''
    },
    
    getStatusText(status) {
      const statusMap = {
        'UPCOMING': '未开始',
        'ONGOING': '进行中',
        'COMPLETED': '已结束',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-container .el-input,
.filter-container .el-select,
.filter-container .el-date-picker {
  margin-right: 10px;
  margin-bottom: 10px;
}
.participants-header {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  font-weight: bold;
}
</style>