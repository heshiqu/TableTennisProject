<template>
  <div class="app-container">
    <el-card>
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
          
          <el-form-item label="性别">
            <el-select v-model="filterForm.gender" clearable placeholder="选择性别">
              <el-option label="男" value="MALE"></el-option>
              <el-option label="女" value="FEMALE"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="年龄范围">
            <el-input-number v-model="filterForm.minAge" :min="1" :max="100" placeholder="最小年龄"></el-input-number>
            <span style="margin: 0 10px;">-</span>
            <el-input-number v-model="filterForm.maxAge" :min="1" :max="100" placeholder="最大年龄"></el-input-number>
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
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ studentStats.total }}</div>
              <div class="stat-label">总学员数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ studentStats.active }}</div>
              <div class="stat-label">活跃学员</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ studentStats.male }}</div>
              <div class="stat-label">男学员</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ studentStats.female }}</div>
              <div class="stat-label">女学员</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 学员列表 -->
      <div class="student-list" v-loading="loading">
        <div v-if="students.length > 0">
          <el-table :data="students" style="width: 100%" border>
            <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
            <el-table-column prop="gender" label="性别" width="80">
              <template slot-scope="scope">
                {{ scope.row.gender === 'MALE' ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="age" label="年龄" width="80"></el-table-column>
            <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
            <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
            <el-table-column prop="balance" label="账户余额" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.balance || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" width="120">
              <template slot-scope="scope">
                {{ scope.row.joinTime | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="lastTraining" label="最近训练" width="120">
              <template slot-scope="scope">
                {{ scope.row.lastTraining || '暂无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewStudentDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewTrainingHistory(scope.row)">
                  训练记录
                </el-button>
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="sendMessage(scope.row)">
                  发送消息
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
        style="text-align: center; margin-top: 20px;">
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
              <img :src="selectedStudent.avatar || '/uploads/avatars/default-avatar.png'" alt="学员头像">
            </div>
          </el-col>
          <el-col :span="16">
            <div class="detail-info">
              <h3>{{ selectedStudent.realName }}</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label>性别：</label>
                  <span>{{ selectedStudent.gender === 'MALE' ? '男' : '女' }}</span>
                </div>
                <div class="info-item">
                  <label>年龄：</label>
                  <span>{{ selectedStudent.age }}岁</span>
                </div>
                <div class="info-item">
                  <label>电话：</label>
                  <span>{{ selectedStudent.phone }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱：</label>
                  <span>{{ selectedStudent.email || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>加入时间：</label>
                  <span>{{ selectedStudent.joinTime | formatDate }}</span>
                </div>
                <div class="info-item">
                  <label>账户余额：</label>
                  <span class="balance">¥{{ selectedStudent.balance || 0 }}</span>
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
import { getMyStudents } from '@/api/coach'

export default {
  name: 'CoachStudents',
  data() {
    return {
      students: [],
      selectedStudent: null,
      detailDialogVisible: false,
      messageDialogVisible: false,
      loading: false,
      filterForm: {
        realName: '',
        gender: '',
        minAge: null,
        maxAge: null
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
      studentStats: {
        total: 0,
        active: 0,
        male: 0,
        female: 0
      }
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
  },
  methods: {
    async loadStudents() {
      this.loading = true
      try {
        const params = {
          current: this.page.current,
          size: this.page.size,
          ...this.filterForm
        }
        
        const response = await getMyStudents(params)
        this.students = response.data.records || []
        this.total = response.data.total || 0
        
        // 计算统计信息
        this.calculateStats()
      } catch (error) {
        console.error('获取学员列表失败:', error)
        this.$message.error('获取学员列表失败')
      } finally {
        this.loading = false
      }
    },
    calculateStats() {
      this.studentStats = {
        total: this.students.length,
        active: this.students.filter(s => s.lastTraining).length,
        male: this.students.filter(s => s.gender === 'MALE').length,
        female: this.students.filter(s => s.gender === 'FEMALE').length
      }
    },
    handleFilter() {
      this.page.current = 1
      this.loadStudents()
    },
    resetFilter() {
      this.filterForm = {
        realName: '',
        gender: '',
        minAge: null,
        maxAge: null
      }
      this.handleFilter()
    },
    viewStudentDetail(student) {
      this.selectedStudent = student
      this.detailDialogVisible = true
    },
    viewTrainingHistory(student) {
      this.$router.push({
        path: '/coach/training-records',
        query: { studentId: student.id }
      })
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
</style>