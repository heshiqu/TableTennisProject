<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>训练记录管理</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="loadTrainingRecords">刷新</el-button>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="学员">
            <el-select v-model="filterForm.studentId" clearable placeholder="选择学员">
              <el-option 
                v-for="student in studentList" 
                :key="student.id" 
                :label="student.realName" 
                :value="student.id">
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 训练统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ trainingStats.total }}</div>
              <div class="stat-label">总训练次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ trainingStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ trainingStats.totalHours }}</div>
              <div class="stat-label">总训练时长(小时)</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">¥{{ trainingStats.totalIncome }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 训练列表 -->
      <div class="training-list" v-loading="loading">
        <div v-if="trainingRecords.length > 0">
          <el-table :data="trainingRecords" style="width: 100%" border>
            <el-table-column prop="studentName" label="学员姓名" min-width="120"></el-table-column>
            <el-table-column prop="startTime" label="训练时间" min-width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="courtNumber" label="球台" min-width="100"></el-table-column>
            <el-table-column prop="duration" label="时长(小时)" min-width="80"></el-table-column>
            <el-table-column prop="fee" label="课时费" min-width="80">
              <template slot-scope="scope">
                ¥{{ scope.row.fee }}
              </template>
            </el-table-column>
            <el-table-column label="学生评价" min-width="150">
              <template slot-scope="scope">
                <div v-if="scope.row.studentComment" style="display: flex; flex-direction: column; gap: 4px;">
                  <el-rate 
                    v-model="scope.row.studentRating" 
                    disabled 
                    text-color="#ff9900">
                  </el-rate>
                  <div class="comment-text">{{ scope.row.studentComment }}</div>
                </div>
                <span v-else>未评价</span>
              </template>
            </el-table-column>
            <el-table-column label="教练评价" min-width="150">
              <template slot-scope="scope">
                <div v-if="scope.row.coachComment" style="display: flex; flex-direction: column; gap: 4px;">
                  <el-rate 
                    v-model="scope.row.coachRating" 
                    disabled 
                    text-color="#ff9900">
                  </el-rate>
                  <div class="comment-text">{{ scope.row.coachComment }}</div>
                </div>
                <span v-else>未评价</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100" fixed="right">
              <template slot-scope="scope">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewTrainingDetail(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else-if="!loading" class="empty-container">
          <div class="empty-content">
            <i class="el-icon-document" style="font-size: 48px; color: #909399;"></i>
            <p style="color: #909399; margin-top: 16px;">暂无训练记录</p>
          </div>
        </div>
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

    <!-- 训练详情对话框 -->
    <el-dialog 
      title="训练详情" 
      :visible.sync="detailDialogVisible" 
      width="50%">
      <div v-if="selectedTraining" class="training-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学员姓名">{{ selectedTraining.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学员ID">{{ selectedTraining.studentId }}</el-descriptions-item>
          
          <el-descriptions-item label="训练时间">{{ formatDateTime(selectedTraining.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="球台">{{ selectedTraining.courtNumber }}</el-descriptions-item>
          
          <el-descriptions-item label="训练时长">{{ selectedTraining.duration }}小时</el-descriptions-item>
          <el-descriptions-item label="课时费">¥{{ selectedTraining.fee }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 学生评价（学生对教练的评价） -->
        <div v-if="selectedTraining.studentComment" class="detail-comments">
          <h4>学生评价：</h4>
          <div class="rating-display">
            <el-rate 
              v-model="selectedTraining.studentRating" 
              disabled 
              text-color="#ff9900">
            </el-rate>
          </div>
          <p>{{ selectedTraining.studentComment }}</p>
        </div>
        
        <!-- 教练评价（教练对学生的评价） -->
        <div class="detail-comments">
          <h4>教练评价：</h4>
          <div v-if="selectedTraining.coachComment">
            <!-- 已有评价，只读显示 -->
            <div class="rating-display">
              <el-rate 
                v-model="selectedTraining.coachRating" 
                disabled 
                text-color="#ff9900">
              </el-rate>
            </div>
            <p>{{ selectedTraining.coachComment }}</p>
          </div>
          <div v-else>
            <!-- 没有评价，可以编辑 -->
            <el-form :model="coachEvaluationForm" label-width="80px">
              <el-form-item label="星级评分">
                <el-rate 
                  v-model="coachEvaluationForm.rating" 
                  :max="5"
                  text-color="#ff9900">
                </el-rate>
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input
                  type="textarea"
                  v-model="coachEvaluationForm.content"
                  :rows="3"
                  placeholder="请输入对学生的评价">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitCoachEvaluation">提交评价</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div v-if="selectedTraining.cancelReason" class="detail-cancel">
          <h4>取消原因：</h4>
          <p>{{ selectedTraining.cancelReason }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCoachCompletedCourses, getCoachApprovedStudents, getCourseEvaluations, createEvaluation } from '@/api/coach'
import { mapGetters } from 'vuex'

export default {
  name: 'CoachTrainingRecords',
  data() {
    return {
      trainingRecords: [],
      studentList: [],
      selectedTraining: null,
      detailDialogVisible: false,
      loading: false,
      filterForm: {
        studentId: '',
        dateRange: []
      },
      page: {
        current: 1,
        size: 10
      },
      total: 0,
      trainingStats: {
        total: 0,
        completed: 0,
        totalHours: 0,
        totalIncome: 0
      },
      coachEvaluationForm: {
        rating: 0,
        content: ''
      }
    }
  },
  computed: {
    ...mapGetters(['userId'])
  },
  created() {
    this.loadTrainingRecords()
    this.loadStudents()
  },
  methods: {
    async loadTrainingRecords() {
      this.loading = true
      try {
        // 构建查询参数
        const params = {
          studentId: this.filterForm.studentId || undefined,
          startDate: this.filterForm.dateRange?.[0] || undefined,
          endDate: this.filterForm.dateRange?.[1] || undefined
        }
        
        const response = await getCoachCompletedCourses(this.userId, params)
        
        if (response.code === 200 && response.data) {
          // 获取所有课程记录
          let records = response.data
          
          // 为每个课程获取评价信息
          const recordsWithEvaluations = await Promise.all(
            records.map(async (record) => {
              try {
                const evalResponse = await getCourseEvaluations(record.id)
                if (evalResponse.code === 200 && evalResponse.data) {
                  const evaluations = evalResponse.data
                  
                  // 处理评价数据
                  const studentEval = evaluations.find(e => e.type === 'STUDENT_TO_COACH')
                  const coachEval = evaluations.find(e => e.type === 'COACH_TO_STUDENT')
                  
                  return {
                    ...record,
                    studentRating: studentEval?.rating || 0,
                    studentComment: studentEval?.content || '',
                    coachRating: coachEval?.rating || 0,
                    coachComment: coachEval?.content || ''
                  }
                }
              } catch (error) {
                console.error(`获取课程 ${record.id} 的评价失败:`, error)
              }
              
              // 如果没有评价数据，返回原始记录
              return {
                ...record,
                studentRating: 0,
                studentComment: '',
                coachRating: 0,
                coachComment: ''
              }
            })
          )
          
          // 使用包含评价数据的记录
          this.trainingRecords = recordsWithEvaluations
          this.total = recordsWithEvaluations.length
          
          // 计算统计信息
          this.calculateStats()
        } else {
          this.trainingRecords = []
          this.total = 0
          this.calculateStats()
        }
      } catch (error) {
        console.error('获取训练记录失败:', error)
        this.$message.error('获取训练记录失败')
      } finally {
        this.loading = false
      }
    },
    async loadStudents() {
      try {
        const response = await getCoachApprovedStudents(this.userId)
        if (response.code === 200 && response.data) {
          this.studentList = response.data.map(item => ({
            id: item.studentId,
            realName: item.studentName
          }))
        } else {
          this.studentList = []
        }
      } catch (error) {
        console.error('获取学员列表失败:', error)
        this.$message.error('获取学员列表失败')
      }
    },
    calculateStats() {
      const records = this.trainingRecords
      
      this.trainingStats = {
        total: records.length,
        completed: records.length,
        totalHours: records.reduce((sum, record) => sum + (record.duration || 0), 0),
        totalIncome: records.reduce((sum, record) => sum + (record.fee || 0), 0)
      }
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return ''
      const date = new Date(dateTimeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getStatusType(status) {
      const typeMap = {
        'COMPLETED': 'success',
        'CANCELLED': 'danger',
        'PENDING': 'warning'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'PENDING': '待确认'
      }
      return textMap[status] || status
    },
    handleFilter() {
      this.page.current = 1
      this.loadTrainingRecords()
    },
    resetFilter() {
      this.filterForm = {
        studentId: '',
        dateRange: []
      }
      this.handleFilter()
    },
    viewTrainingDetail(training) {
      this.selectedTraining = training
      this.detailDialogVisible = true
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadTrainingRecords()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadTrainingRecords()
    },
    async submitCoachEvaluation() {
      if (!this.coachEvaluationForm.rating || this.coachEvaluationForm.rating < 1 || this.coachEvaluationForm.rating > 5) {
        this.$message.warning('请选择1-5星的评分')
        return
      }
      
      if (!this.coachEvaluationForm.content || this.coachEvaluationForm.content.trim() === '') {
        this.$message.warning('请输入评价内容')
        return
      }
      
      try {
        const evaluationData = {
          courseId: this.selectedTraining.id,
          fromUserId: this.userId,
          toUserId: this.selectedTraining.studentId,
          content: this.coachEvaluationForm.content.trim(),
          rating: this.coachEvaluationForm.rating,
          type: 'COACH_TO_STUDENT'
        }
        
        const response = await createEvaluation(evaluationData)
        
        if (response.code === 200) {
          this.$message.success('评价创建成功')
          
          // 关闭对话框
          this.detailDialogVisible = false
          
          // 重置评价表单
          this.coachEvaluationForm = {
            rating: 0,
            content: ''
          }
          
          // 刷新训练记录列表
          await this.loadTrainingRecords()
        } else {
          this.$message.error(response.message || '评价创建失败')
        }
      } catch (error) {
        console.error('提交评价失败:', error)
        this.$message.error('提交评价失败，请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.training-list {
  min-height: 400px;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.empty-content {
  text-align: center;
  padding: 40px;
}

.training-detail {
  padding: 20px;
}

.detail-cancel {
  margin-top: 20px;
  padding: 15px;
  background-color: #fef0f0;
  border-left: 4px solid #f56c6c;
  border-radius: 4px;
}

.detail-cancel h4 {
  margin: 0 0 10px 0;
  color: #f56c6c;
}

.detail-cancel p {
  margin: 0;
  color: #606266;
}

.comment-text {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  line-height: 1.4;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-comments {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 4px;
}

.detail-comments h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
}

.comment-section {
  margin-bottom: 15px;
}

.comment-section h5 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.comment-section p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.5;
}

.rating-display {
  margin-bottom: 8px;
}
</style>