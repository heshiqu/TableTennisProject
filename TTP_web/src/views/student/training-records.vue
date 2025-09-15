<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>训练记录</span>
      </div>
      
      <div class="filter-container">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="handleFilter"
        />
        <el-select v-model="coachFilter" placeholder="选择教练" clearable @change="handleFilter">
          <el-option
            v-for="coach in myCoaches"
            :key="coach.id"
            :label="coach.realName"
            :value="coach.id"
          />
        </el-select>
        <el-button type="primary" @click="handleFilter">查询</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="trainingRecords"
        border
        style="width: 100%"
      >
        <el-table-column prop="date" label="训练日期" min-width="100" />
        <el-table-column prop="time" label="训练时间" min-width="120" />
        <el-table-column prop="coachName" label="教练姓名" min-width="100" />
        <el-table-column label="训练时长" min-width="80">
          <template slot-scope="{row}">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="tableNumber" label="球台号" min-width="70" />
        <el-table-column label="课时费" min-width="80">
          <template slot-scope="{row}">
            ¥{{ row.fee }}
          </template>
        </el-table-column>
        <el-table-column label="我的评价" min-width="150" show-overflow-tooltip>
          <template slot-scope="{row}">
            <div v-if="row.studentEvaluation">
              <el-rate 
                :value="row.studentRating" 
                disabled 
                size="mini"
                text-color="#ff9900"
                score-template="{value}分">
              </el-rate>
              <span class="evaluation-text">{{ row.studentEvaluation }}</span>
            </div>
            <span v-else class="no-evaluation">暂无评价</span>
          </template>
        </el-table-column>
        <el-table-column label="教练评价" min-width="150" show-overflow-tooltip>
          <template slot-scope="{row}">
            <div v-if="row.coachEvaluation">
              <el-rate 
                :value="row.coachRating" 
                disabled 
                size="mini"
                text-color="#ff9900"
                score-template="{value}分">
              </el-rate>
              <span class="evaluation-text">{{ row.coachEvaluation }}</span>
            </div>
            <span v-else class="no-evaluation">暂无评价</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="80" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="viewDetail(row)">查看详情</el-button>
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

    <!-- 训练记录详情对话框 -->
    <el-dialog title="训练详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedRecord" class="training-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="训练日期">{{ selectedRecord.date }}</el-descriptions-item>
          <el-descriptions-item label="训练时间">{{ selectedRecord.time }}</el-descriptions-item>
          <el-descriptions-item label="教练姓名">{{ selectedRecord.coachName }}</el-descriptions-item>
          <el-descriptions-item label="训练时长">{{ selectedRecord.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="球台号">{{ selectedRecord.tableNumber }}</el-descriptions-item>
          <el-descriptions-item label="课时费">¥{{ selectedRecord.fee }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 我的评价区域 -->
        <div class="evaluation-section">
          <h4>我的评价</h4>
          <div v-if="selectedRecord.studentEvaluation">
            <div class="evaluation-display">
              <span class="rating-label">评分：</span>
              <el-rate 
                :value="selectedRecord.studentRating" 
                disabled 
                show-score 
                text-color="#ff9900"
                score-template="{value}分">
              </el-rate>
            </div>
            <div class="evaluation-content">
              <p>{{ selectedRecord.studentEvaluation }}</p>
            </div>
          </div>
          <div v-else-if="selectedRecord.status === 'COMPLETED'">
            <div class="evaluation-form">
              <div class="rating-section">
                <span class="rating-label">评分：</span>
                <el-rate 
                  v-model="studentRatingForm.rating" 
                  :max="5"
                  :allow-half="false"
                  text-color="#ff9900">
                </el-rate>
              </div>
              <el-input
                v-model="studentRatingForm.content"
                type="textarea"
                :rows="3"
                placeholder="请对本次训练进行评价..."
                maxlength="500"
                show-word-limit>
              </el-input>
              <el-button 
                type="primary" 
                size="small" 
                @click="submitStudentEvaluation" 
                style="margin-top: 10px;"
                :disabled="!studentRatingForm.rating || !studentRatingForm.content">
                提交评价
              </el-button>
            </div>
          </div>
          <p v-else>暂无评价</p>
        </div>
        
        <!-- 教练评价区域 -->
        <div class="evaluation-section">
          <h4>教练评价</h4>
          <div v-if="selectedRecord.coachEvaluation">
            <div class="evaluation-display">
              <span class="rating-label">评分：</span>
              <el-rate 
                :value="selectedRecord.coachRating" 
                disabled 
                show-score 
                text-color="#ff9900"
                score-template="{value}分">
              </el-rate>
            </div>
            <div class="evaluation-content">
              <p>{{ selectedRecord.coachEvaluation }}</p>
            </div>
          </div>
          <p v-else>暂无评价</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentCompletedCourses } from '@/api/student'
import { getMyCoaches } from '@/api/student'
import { getCourseEvaluations } from '@/api/evaluation'
import { createEvaluation } from '@/api/evaluation'
import Pagination from '@/components/Pagination'

export default {
  name: 'TrainingRecords',
  components: { Pagination },
  data() {
    return {
      trainingRecords: [],
      myCoaches: [],
      dateRange: [],
      coachFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      selectedRecord: null,
      detailDialogVisible: false,
      studentRatingForm: {
        rating: 0,
        content: ''
      }
    }
  },
  created() {
    this.loadMyCoaches()
    this.getList()
  },
  methods: {
    async loadMyCoaches() {
      try {
        const userId = this.$store.state.user.user?.id
        if (!userId) return
        
        const response = await getMyCoaches()
        this.myCoaches = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    async getList() {
      this.loading = true
      try {
        const userId = this.$store.state.user.user?.id
        if (!userId) {
          this.$message.error('请先登录')
          return
        }
        
        const response = await getStudentCompletedCourses(userId)
        
        // 根据新的API响应格式处理数据
        if (response.code === 200) {
          let allRecords = (response.data || []).map(course => ({
            id: course.id,
            date: new Date(course.startTime).toLocaleDateString('zh-CN'),
            time: `${new Date(course.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })} - ${new Date(course.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`,
            coachName: course.coachName,
            coachId: course.coachId,
            duration: course.duration * 60, // 转换为分钟
            tableNumber: course.courtNumber,
            fee: course.fee,
            status: course.status,
            startTime: course.startTime,
            endTime: course.endTime,
            studentEvaluation: '',
            coachEvaluation: ''
          }))
          
          // 为每条记录获取评价信息
          for (let record of allRecords) {
            try {
              const evaluationResponse = await getCourseEvaluations(record.id)
              if (evaluationResponse.code === 200 && evaluationResponse.data) {
                const evaluations = evaluationResponse.data
                
                // 查找学生对教练的评价
                const studentEval = evaluations.find(e => e.type === 'STUDENT_TO_COACH')
                if (studentEval) {
                  record.studentEvaluation = studentEval.content
                  record.studentRating = studentEval.rating
                }
                
                // 查找教练对学生的评价
                const coachEval = evaluations.find(e => e.type === 'COACH_TO_STUDENT')
                if (coachEval) {
                  record.coachEvaluation = coachEval.content
                  record.coachRating = coachEval.rating
                }
              }
            } catch (error) {
              console.error(`获取课程 ${record.id} 评价失败:`, error)
            }
          }
          
          // 本地筛选逻辑
          if (this.dateRange && this.dateRange.length === 2) {
            const startDate = new Date(this.dateRange[0])
            const endDate = new Date(this.dateRange[1])
            endDate.setHours(23, 59, 59, 999)
            
            allRecords = allRecords.filter(record => {
              const recordDate = new Date(record.startTime)
              return recordDate >= startDate && recordDate <= endDate
            })
          }
          
          if (this.coachFilter) {
            allRecords = allRecords.filter(record => 
              record.coachName === this.myCoaches.find(coach => coach.id === this.coachFilter)?.realName
            )
          }
          
          this.trainingRecords = allRecords
          this.total = this.trainingRecords.length
        } else {
          this.trainingRecords = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取训练记录失败:', error)
        this.$message.error('获取训练记录失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      // 由于新API不支持分页和筛选，这里只做本地筛选
      this.listQuery.page = 1
      this.getList()
    },
    viewDetail(row) {
      this.selectedRecord = row
      this.detailDialogVisible = true
      // 重置评价表单
      this.studentRatingForm = {
        rating: 0,
        content: ''
      }
    },
    async submitStudentEvaluation() {
      if (!this.studentRatingForm.rating || !this.studentRatingForm.content) {
        this.$message.warning('请填写评分和评价内容')
        return
      }

      try {
        const requestBody = {
          courseId: this.selectedRecord.id,
          fromUserId: this.$store.state.user.user?.id,
          toUserId: this.selectedRecord.coachId,
          content: this.studentRatingForm.content,
          rating: this.studentRatingForm.rating,
          type: 'STUDENT_TO_COACH'
        }

        const response = await createEvaluation(requestBody)
        
        if (response.code === 200) {
          this.$message.success('评价提交成功')
          this.detailDialogVisible = false
          // 刷新训练记录
          this.getList()
        } else {
          this.$message.error(response.message || '评价提交失败')
        }
      } catch (error) {
        console.error('提交评价失败:', error)
        this.$message.error('评价提交失败，请稍后重试')
      }
    }
  }
}</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.filter-container > * {
  margin-right: 10px;
  margin-bottom: 10px;
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

.detail-cancel {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.detail-cancel h4 {
  color: #f56c6c;
  margin-bottom: 10px;
}

.evaluation-section {
  margin-top: 20px;
}

.evaluation-section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.evaluation-section p {
  color: #606266;
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.evaluation-item {
  margin-bottom: 15px;
}

.evaluation-item h4 {
  margin-bottom: 10px;
  color: #303133;
}

.evaluation-item p {
  margin: 5px 0;
  line-height: 1.6;
  color: #606266;
}

.evaluation-text {
  color: #606266;
  font-size: 14px;
}

.no-evaluation {
  color: #909399;
  font-size: 14px;
  font-style: italic;
}

.evaluation-display {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.rating-label {
  margin-right: 10px;
  color: #333;
  font-weight: 500;
}

.evaluation-content {
  margin-top: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.evaluation-form {
  margin-top: 10px;
}

.rating-section {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

/* 评价星级样式 */
.el-rate {
  margin-bottom: 4px;
}

.el-rate__icon {
  font-size: 12px;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
  
  .filter-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-container .el-date-editor {
    margin-right: 0;
    width: 100% !important;
  }
  
  .filter-container .el-select {
    width: 100% !important;
  }
  
  .filter-container .el-button {
    width: 100%;
  }
}

/* 表格响应式样式 */
@media screen and (max-width: 1200px) {
  .el-table {
    font-size: 13px;
  }
}

@media screen and (max-width: 768px) {
  .el-table {
    font-size: 12px;
  }
}
</style>