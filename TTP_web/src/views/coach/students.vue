<template>
  <div class="app-container">
    <!-- 学生申请列表 -->
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>学生申请</span>
      </div>
      
      <div v-loading="loadingApplications">
        <div v-if="applications.length > 0">
          <el-table :data="applications" style="width: 100%" border>
            <el-table-column prop="realName" label="学生姓名" min-width="100" />
            <el-table-column prop="gender" label="性别" width="60" align="center">
              <template slot-scope="scope">
                {{ scope.row.gender === 'MALE' ? '男' : scope.row.gender === 'FEMALE' ? '女' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="age" label="年龄" width="60" align="center" />
            <el-table-column prop="campusName" label="校区" min-width="100" />
            <el-table-column prop="phone" label="电话号" min-width="120" />
            <el-table-column prop="email" label="邮箱号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="applyTime" label="申请时间" min-width="140">
              <template slot-scope="scope">
                {{ scope.row.applyTime | formatDate }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button 
                  type="success" 
                  size="mini" 
                  @click="handleApproveApplication(scope.row.relationId)">
                  同意
                </el-button>
                <el-button 
                  type="danger" 
                  size="mini" 
                  @click="handleRejectApplication(scope.row.relationId)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无学生申请"></el-empty>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>我的学员管理</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="loadStudents">刷新</el-button>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="学员姓名">
            <el-input 
              v-model="filterForm.realName" 
              placeholder="输入学员姓名" 
              clearable>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 学员统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ allStudents.length }}</div>
              <div class="stat-label">总学员数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ allStudents.filter(s => s.gender === 'MALE').length }}</div>
              <div class="stat-label">男学员</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number">{{ allStudents.filter(s => s.gender === 'FEMALE').length }}</div>
              <div class="stat-label">女学员</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 学员列表 -->
      <div class="student-list" v-loading="loading">
        <div v-if="students.length > 0">
          <el-table 
            :data="students" 
            style="width: 100%" 
            border
            :max-height="600"
            stripe
            size="medium">
            <el-table-column prop="realName" label="学员姓名" min-width="100" />
            <el-table-column prop="gender" label="性别" width="70">
              <template slot-scope="scope">
                {{ scope.row.gender === 'MALE' ? '男' : scope.row.gender === 'FEMALE' ? '女' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="age" label="年龄" width="70" />
            <el-table-column prop="phone" label="电话号" min-width="120" />
            <el-table-column prop="email" label="邮箱号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="applyTime" label="申请时间" min-width="140">
              <template slot-scope="scope">
                {{ scope.row.applyTime | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="approveTime" label="批准时间" min-width="140">
              <template slot-scope="scope">
                {{ scope.row.approveTime | formatDate }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewTrainingHistory(scope.row)">
                  训练记录
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无学员"></el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[10, 20, 50]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="text-align: center; margin-top: 10px; margin-bottom: 5px;">
      </el-pagination>
    </el-card>

    <!-- 学员详情对话框 -->
    <el-dialog 
      title="学员详情" 
      :visible.sync="detailDialogVisible" 
      width="60%">
      <div v-if="selectedStudent" class="student-detail">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="detail-avatar">
              <img :src="selectedStudent.avatar || (baseUrl + '/uploads/avatars/default-avatar.jpg')" alt="学员头像">
            </div>
          </el-col>
          <el-col :span="16">
            <div class="detail-info">
              <h3>{{ selectedStudent.realName }}</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label>学员ID：</label>
                  <span>{{ selectedStudent.id }}</span>
                </div>
                <div class="info-item">
                  <label>性别：</label>
                  <span>{{ selectedStudent.gender === 'MALE' ? '男' : selectedStudent.gender === 'FEMALE' ? '女' : '-' }}</span>
                </div>
                <div class="info-item">
                  <label>年龄：</label>
                  <span>{{ selectedStudent.age || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>电话号：</label>
                  <span>{{ selectedStudent.phone || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱号：</label>
                  <span>{{ selectedStudent.email || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>申请时间：</label>
                  <span>{{ selectedStudent.applyTime | formatDate }}</span>
                </div>
                <div class="info-item">
                  <label>批准时间：</label>
                  <span>{{ selectedStudent.approveTime | formatDate }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <div class="detail-stats">
          <h4>训练统计</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedStudent.totalTrainings || 0 }}</div>
                <div class="stat-label">总训练次数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedStudent.totalHours || 0 }}</div>
                <div class="stat-label">总训练时长(小时)</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ selectedStudent.averageScore || 0 }}</div>
                <div class="stat-label">平均评分</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 训练记录对话框 -->
    <el-dialog 
      title="训练记录" 
      :visible.sync="trainingDialogVisible" 
      width="70%">
      <div v-loading="trainingLoading">
        <div v-if="trainingRecords.length > 0">
          <el-table :data="trainingRecords" style="width: 100%" border>
            <el-table-column prop="courtNumber" label="场地号" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(小时)" width="80" align="center" />
            <el-table-column prop="fee" label="费用(元)" width="80" align="center" />
            <el-table-column prop="createdAt" label="创建时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无训练记录"></el-empty>
      </div>
    </el-dialog>

    <!-- 发送消息对话框 -->
    <el-dialog 
      title="发送消息" 
      :visible.sync="messageDialogVisible" 
      width="50%">
      <el-form :model="messageForm" label-width="80px">
        <el-form-item label="接收学员">
          <el-input v-model="messageForm.studentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input 
            type="textarea" 
            v-model="messageForm.content" 
            :rows="4" 
            placeholder="请输入消息内容">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessageConfirm">发送</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script>
import { getCoachApprovedStudents, getUserDetail, getStudentApplications, approveStudentApplication, rejectStudentApplication } from '@/api/coach'
import { getStudentCompletedCourses } from '@/api/student'

export default {
  name: 'CoachStudents',
  data() {
    return {
      students: [],
      allStudents: [],
      selectedStudent: null,
      detailDialogVisible: false,
      messageDialogVisible: false,
      trainingDialogVisible: false,
      trainingRecords: [],
      trainingLoading: false,
      loading: false,
      filterForm: {
        realName: ''
      },
      messageForm: {
        studentId: '',
        studentName: '',
        content: ''
      },
      page: {
        current: 1,
        size: 10
      },
      total: 0,
      baseUrl: process.env.VUE_APP_BASE_API || 'http://localhost:8080',
      // 学生申请相关
      applications: [],
      loadingApplications: false
    }
  },
  filters: {
    formatDate(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN')
    }
  },
  created() {
    this.loadStudents()
    this.loadApplications()
  },
  methods: {
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    async loadStudents() {
      this.loading = true
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练ID')
          return
        }
        
        const response = await getCoachApprovedStudents(coachId)
        // 新API返回格式：{ code: 200, message: "获取成功", data: [...], timestamp: ... }
        const studentRelations = response.data || []
        
        // 获取每个学员的详细信息
        const studentsWithDetails = await Promise.all(
          studentRelations.map(async (relation) => {
            try {
              const userDetailResponse = await getUserDetail(relation.studentId)
              const userDetail = userDetailResponse.data || {}
              
              return {
                id: relation.studentId,
                realName: userDetail.realName || relation.studentName,
                gender: userDetail.gender,
                age: userDetail.age,
                phone: userDetail.phone,
                email: userDetail.email,
                avatar: userDetail.avatar,
                campusName: userDetail.campusName,
                status: relation.status,
                applyTime: relation.applyTime,
                approveTime: relation.approveTime
              }
            } catch (error) {
              console.error(`获取学员 ${relation.studentId} 详情失败:`, error)
              // 如果获取详情失败，使用基础信息
              return {
                id: relation.studentId,
                realName: relation.studentName,
                gender: null,
                age: null,
                phone: null,
                email: null,
                avatar: null,
                campusName: null,
                status: relation.status,
                applyTime: relation.applyTime,
                approveTime: relation.approveTime
              }
            }
          })
        )
        
        this.allStudents = studentsWithDetails
        
        // 应用筛选
        this.applyFilter()
        
      } catch (error) {
        console.error('获取学员列表失败:', error)
        this.$message.error('获取学员列表失败')
      } finally {
        this.loading = false
      }
    },

    async loadApplications() {
      this.loadingApplications = true
      try {
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练ID')
          return
        }
        
        const response = await getStudentApplications(coachId)
        const applicationRelations = response.data || []
        
        // 获取每个申请学生的详细信息
        const applicationsWithDetails = await Promise.all(
          applicationRelations.map(async (relation) => {
            try {
              const userDetailResponse = await getUserDetail(relation.studentId)
              const userDetail = userDetailResponse.data || {}
              
              return {
                id: relation.studentId,
                relationId: relation.id,
                realName: userDetail.realName || relation.studentName,
                gender: userDetail.gender,
                age: userDetail.age,
                phone: userDetail.phone,
                email: userDetail.email,
                avatar: userDetail.avatar,
                campusName: userDetail.campusName,
                status: relation.status,
                applyTime: relation.applyTime,
                approveTime: relation.approveTime
              }
            } catch (error) {
              console.error(`获取申请学生 ${relation.studentId} 详情失败:`, error)
              // 如果获取详情失败，使用基础信息
              return {
                id: relation.studentId,
                relationId: relation.id,
                realName: relation.studentName,
                gender: null,
                age: null,
                phone: null,
                email: null,
                avatar: null,
                campusName: null,
                status: relation.status,
                applyTime: relation.applyTime,
                approveTime: relation.approveTime
              }
            }
          })
        )
        
        this.applications = applicationsWithDetails
        
      } catch (error) {
        console.error('获取学生申请失败:', error)
        this.$message.error('获取学生申请失败')
      } finally {
        this.loadingApplications = false
      }
    },
    applyFilter() {
      let filtered = [...this.allStudents]
      
      // 按姓名筛选
      if (this.filterForm.realName) {
        filtered = filtered.filter(s => 
          s.realName.toLowerCase().includes(this.filterForm.realName.toLowerCase())
        )
      }
      
      this.students = filtered
      this.total = this.students.length
    },
    handleFilter() {
      this.page.current = 1
      this.applyFilter()
    },
    resetFilter() {
      this.filterForm = {
        realName: ''
      }
      this.applyFilter()
    },

    // 处理学生申请
    async handleApproveApplication(relationId) {
      try {
        await this.$confirm('确认同意该学生的申请吗？', '提示', {
          type: 'warning'
        })
        
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练ID')
          return
        }
        
        await approveStudentApplication(relationId, coachId)
        this.$message.success('申请已同意')
        
        // 重新加载申请列表和学员列表
        this.loadApplications()
        this.loadStudents()
        
      } catch (error) {
        if (error !== 'cancel') {
          console.error('同意申请失败:', error)
          this.$message.error('同意申请失败')
        }
      }
    },

    async handleRejectApplication(relationId) {
      try {
        await this.$confirm('确认拒绝该学生的申请吗？', '提示', {
          type: 'warning'
        })
        
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练ID')
          return
        }
        
        await rejectStudentApplication(relationId, coachId)
        this.$message.success('申请已拒绝')
        
        // 重新加载申请列表和学员列表
        this.loadApplications()
        this.loadStudents()
        
      } catch (error) {
        if (error !== 'cancel') {
          console.error('拒绝申请失败:', error)
          this.$message.error('拒绝申请失败')
        }
      }
    },
    viewStudentDetail(student) {
      this.selectedStudent = student
      this.detailDialogVisible = true
    },
    async viewTrainingHistory(student) {
      console.log('=== 训练记录按钮被点击 ===')
      console.log('学员信息:', student)
      
      try {
        this.selectedStudent = student
        this.trainingDialogVisible = true
        console.log('对话框可见性已设置为true:', this.trainingDialogVisible)
        
        this.trainingLoading = true
        console.log('开始加载训练记录...')
        
        const response = await getStudentCompletedCourses(student.id)
        console.log('API响应:', response)
        
        this.trainingRecords = response.data || []
        console.log('训练记录已加载:', this.trainingRecords.length, '条')
        
      } catch (error) {
        console.error('获取训练记录失败:', error)
        this.$message.error('获取训练记录失败：' + error.message)
        this.trainingRecords = []
        this.trainingDialogVisible = false
      } finally {
        this.trainingLoading = false
        console.log('加载完成，loading状态:', this.trainingLoading)
      }
    },
    sendMessage(student) {
      this.messageForm = {
        studentId: student.id,
        studentName: student.realName,
        content: ''
      }
      this.messageDialogVisible = true
    },
    async sendMessageConfirm() {
      if (!this.messageForm.content.trim()) {
        this.$message.warning('请输入消息内容')
        return
      }
      
      try {
        // TODO: 调用发送消息API
        this.$message.success('消息发送成功')
        this.messageDialogVisible = false
      } catch (error) {
        console.error('发送消息失败:', error)
        this.$message.error('发送消息失败')
      }
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadStudents()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadStudents()
    }
  }
}
</script>

<style scoped>
.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.student-list {
  min-height: 400px;
  width: 100%;
  overflow-x: auto;
}

.student-list .el-table {
  min-width: 800px;
}

.student-detail {
  padding: 20px;
}

.detail-avatar {
  text-align: center;
}

.detail-avatar img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

.detail-info h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: bold;
  color: #666;
  margin-right: 10px;
}

.balance {
  color: #67C23A;
  font-weight: bold;
}

.detail-stats {
  margin-top: 20px;
}

.detail-stats h4 {
  margin: 0 0 20px 0;
  color: #333;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .app-container {
    padding: 15px;
  }
}

@media (max-width: 992px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-avatar {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
  
  .filter-section {
    padding: 10px;
  }
  
  .stats-section .el-col {
    margin-bottom: 10px;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .student-list .el-table {
    min-width: 600px;
  }
  
  .el-table {
    font-size: 14px;
  }
}

@media (max-width: 576px) {
  .app-container {
    padding: 5px;
  }
  
  .filter-section {
    padding: 8px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .el-table {
    font-size: 12px;
  }
}
</style>