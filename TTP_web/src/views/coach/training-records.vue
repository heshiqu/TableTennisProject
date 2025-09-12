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
          
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" clearable placeholder="选择状态">
              <el-option label="已完成" value="COMPLETED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
            </el-select>
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
            <el-table-column prop="studentName" label="学员姓名" width="120"></el-table-column>
            <el-table-column prop="date" label="日期" width="120">
              <template slot-scope="scope">
                {{ scope.row.date | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="timeSlot" label="时间段" width="120"></el-table-column>
            <el-table-column prop="tableName" label="球台" width="100"></el-table-column>
            <el-table-column prop="duration" label="时长(小时)" width="100"></el-table-column>
            <el-table-column prop="fee" label="课时费" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.fee }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="mini">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="evaluationStatus" label="评价状态" width="100">
              <template slot-scope="scope">
                <el-tag 
                  :type="scope.row.evaluationStatus ? 'success' : 'warning'" 
                  size="mini">
                  {{ scope.row.evaluationStatus ? '已评价' : '待评价' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="viewTrainingDetail(scope.row)">
                  详情
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'COMPLETED' && !scope.row.evaluationStatus"
                  type="text" 
                  size="mini" 
                  @click="createEvaluation(scope.row)">
                  评价学员
                </el-button>
                <el-button 
                  v-if="scope.row.evaluationStatus"
                  type="text" 
                  size="mini" 
                  @click="viewEvaluation(scope.row)">
                  查看评价
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无训练记录"></el-empty>
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
          <el-descriptions-item label="联系电话">{{ selectedTraining.studentPhone }}</el-descriptions-item>
          
          <el-descriptions-item label="训练日期">{{ selectedTraining.date | formatDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ selectedTraining.timeSlot }}</el-descriptions-item>
          
          <el-descriptions-item label="球台">{{ selectedTraining.tableName }}</el-descriptions-item>
          <el-descriptions-item label="训练时长">{{ selectedTraining.duration }}小时</el-descriptions-item>
          
          <el-descriptions-item label="课时费">¥{{ selectedTraining.fee }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedTraining.status)" size="mini">
              {{ getStatusText(selectedTraining.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedTraining.cancelReason" class="detail-cancel">
          <h4>取消原因：</h4>
          <p>{{ selectedTraining.cancelReason }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 评价学员对话框 -->
    <el-dialog 
      title="评价学员" 
      :visible.sync="evaluationDialogVisible" 
      width="50%">
      <el-form :model="evaluationForm" label-width="100px" :rules="evaluationRules" ref="evaluationForm">
        <el-form-item label="学员表现" prop="coachComment">
          <el-input 
            type="textarea" 
            v-model="evaluationForm.coachComment" 
            :rows="4" 
            placeholder="请评价学员本次训练的表现">
          </el-input>
        </el-form-item>
        
        <el-form-item label="改进建议" prop="coachSuggestion">
          <el-input 
            type="textarea" 
            v-model="evaluationForm.coachSuggestion" 
            :rows="3" 
            placeholder="请给出学员的改进建议">
          </el-input>
        </el-form-item>
        
        <el-form-item label="训练标签" prop="tags">
          <el-select 
            v-model="evaluationForm.tags" 
            multiple 
            filterable 
            allow-create 
            placeholder="请选择或输入训练标签">
            <el-option label="基础扎实" value="基础扎实"></el-option>
            <el-option label="进步明显" value="进步明显"></el-option>
            <el-option label="需要加强" value="需要加强"></el-option>
            <el-option label="态度认真" value="态度认真"></el-option>
            <el-option label="技术娴熟" value="技术娴熟"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="evaluationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
      </span>
    </el-dialog>

    <!-- 查看评价对话框 -->
    <el-dialog 
      title="训练评价" 
      :visible.sync="viewEvaluationDialogVisible" 
      width="50%">
      <div v-if="selectedEvaluation" class="evaluation-view">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学员姓名">{{ selectedEvaluation.studentName }}</el-descriptions-item>
          <el-descriptions-item label="训练日期">{{ selectedEvaluation.date | formatDate }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider>教练评价</el-divider>
        <div class="evaluation-content">
          <h4>学员表现：</h4>
          <p>{{ selectedEvaluation.coachComment || '暂无评价' }}</p>
          
          <h4>改进建议：</h4>
          <p>{{ selectedEvaluation.coachSuggestion || '暂无建议' }}</p>
          
          <div v-if="selectedEvaluation.coachTags && selectedEvaluation.coachTags.length" class="evaluation-tags">
            <h4>训练标签：</h4>
            <el-tag 
              v-for="tag in selectedEvaluation.coachTags" 
              :key="tag" 
              size="small" 
              type="info">
              {{ tag }}
            </el-tag>
          </div>
        </div>
        
        <el-divider>学员反馈</el-divider>
        <div class="evaluation-content">
          <h4>训练收获：</h4>
          <p>{{ selectedEvaluation.studentComment || '暂无反馈' }}</p>
          
          <h4>训练建议：</h4>
          <p>{{ selectedEvaluation.studentSuggestion || '暂无建议' }}</p>
          
          <div v-if="selectedEvaluation.studentTags && selectedEvaluation.studentTags.length" class="evaluation-tags">
            <h4>学员标签：</h4>
            <el-tag 
              v-for="tag in selectedEvaluation.studentTags" 
              :key="tag" 
              size="small" 
              type="info">
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCoachTrainingRecords, createEvaluation } from '@/api/coach'
import { getMyStudents } from '@/api/coach'

export default {
  name: 'CoachTrainingRecords',
  data() {
    return {
      trainingRecords: [],
      studentList: [],
      selectedTraining: null,
      selectedEvaluation: null,
      detailDialogVisible: false,
      evaluationDialogVisible: false,
      viewEvaluationDialogVisible: false,
      loading: false,
      filterForm: {
        studentId: '',
        dateRange: [],
        status: ''
      },
      evaluationForm: {
        trainingId: '',
        coachComment: '',
        coachSuggestion: '',
        tags: []
      },
      evaluationRules: {
        coachComment: [
          { required: true, message: '请输入学员表现评价', trigger: 'blur' },
          { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
        ],
        coachSuggestion: [
          { max: 500, message: '长度不超过 500 个字符', trigger: 'blur' }
        ]
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
      }
    }
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
  },
  created() {
    this.loadTrainingRecords()
    this.loadStudents()
  },
  methods: {
    async loadTrainingRecords() {
      this.loading = true
      try {
        const params = {
          current: this.page.current,
          size: this.page.size,
          studentId: this.filterForm.studentId,
          startDate: this.filterForm.dateRange?.[0] || '',
          endDate: this.filterForm.dateRange?.[1] || '',
          status: this.filterForm.status
        }
        
        const response = await getCoachTrainingRecords(params)
        this.trainingRecords = response.data.records || []
        this.total = response.data.total || 0
        
        // 计算统计信息
        this.calculateStats()
      } catch (error) {
        console.error('获取训练记录失败:', error)
        this.$message.error('获取训练记录失败')
      } finally {
        this.loading = false
      }
    },
    async loadStudents() {
      try {
        const response = await getMyStudents()
        this.studentList = response.data || []
      } catch (error) {
        console.error('获取学员列表失败:', error)
      }
    },
    calculateStats() {
      const completedRecords = this.trainingRecords.filter(r => r.status === 'COMPLETED')
      
      this.trainingStats = {
        total: this.trainingRecords.length,
        completed: completedRecords.length,
        totalHours: completedRecords.reduce((sum, record) => sum + (record.duration || 0), 0),
        totalIncome: completedRecords.reduce((sum, record) => sum + (record.fee || 0), 0)
      }
    },
    getStatusType(status) {
      const typeMap = {
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
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
        dateRange: [],
        status: ''
      }
      this.handleFilter()
    },
    viewTrainingDetail(training) {
      this.selectedTraining = training
      this.detailDialogVisible = true
    },
    createEvaluation(training) {
      this.evaluationForm = {
        trainingId: training.id,
        coachComment: '',
        coachSuggestion: '',
        tags: []
      }
      this.evaluationDialogVisible = true
    },
    async submitEvaluation() {
      this.$refs.evaluationForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          await createEvaluation(this.evaluationForm)
          this.$message.success('评价提交成功')
          this.evaluationDialogVisible = false
          this.loadTrainingRecords()
        } catch (error) {
          console.error('提交评价失败:', error)
          this.$message.error('提交评价失败')
        }
      })
    },
    viewEvaluation(training) {
      this.selectedEvaluation = training
      this.viewEvaluationDialogVisible = true
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadTrainingRecords()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadTrainingRecords()
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

.training-list {
  min-height: 400px;
}

.training-detail {
  padding: 10px;
}

.detail-cancel {
  margin-top: 20px;
}

.detail-cancel h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-cancel p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.evaluation-content {
  margin-bottom: 20px;
}

.evaluation-content h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.evaluation-content p {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.6;
}

.evaluation-tags {
  margin-top: 15px;
}

.evaluation-tags h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.evaluation-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
</style>