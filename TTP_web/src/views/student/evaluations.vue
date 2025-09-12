<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>训练评价记录</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="loadEvaluations">刷新</el-button>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="教练">
            <el-select v-model="filterForm.coachId" clearable placeholder="选择教练">
              <el-option 
                v-for="coach in coachList" 
                :key="coach.id" 
                :label="coach.realName" 
                :value="coach.id">
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

      <!-- 评价列表 -->
      <div class="evaluation-list" v-loading="loading">
        <div v-if="evaluations.length > 0">
          <div 
            v-for="evaluation in evaluations" 
            :key="evaluation.id" 
            class="evaluation-card">
            <div class="evaluation-header">
              <div class="evaluation-info">
                <h4>{{ evaluation.coachName }}</h4>
                <span class="evaluation-date">{{ evaluation.createTime | formatDate }}</span>
                <el-tag size="mini" :type="getScoreType(evaluation.score)">
                  评分：{{ evaluation.score }}分
                </el-tag>
              </div>
              <div class="evaluation-actions">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="showEvaluationDetail(evaluation)">
                  查看详情
                </el-button>
              </div>
            </div>
            
            <div class="evaluation-content">
              <div class="evaluation-section">
                <h5>学员评价：</h5>
                <p>{{ evaluation.studentComment || '暂无评价' }}</p>
              </div>
              
              <div class="evaluation-section">
                <h5>教练反馈：</h5>
                <p>{{ evaluation.coachComment || '暂无反馈' }}</p>
              </div>
              
              <div class="evaluation-tags" v-if="evaluation.tags && evaluation.tags.length">
                <el-tag 
                  v-for="tag in evaluation.tags" 
                  :key="tag" 
                  size="mini" 
                  type="info">
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价记录"></el-empty>
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

    <!-- 评价详情对话框 -->
    <el-dialog 
      title="评价详情" 
      :visible.sync="detailDialogVisible" 
      width="60%">
      <div v-if="selectedEvaluation" class="evaluation-detail">
        <div class="detail-header">
          <h3>{{ selectedEvaluation.coachName }}</h3>
          <div class="detail-meta">
            <span>{{ selectedEvaluation.createTime | formatDate }}</span>
            <el-tag :type="getScoreType(selectedEvaluation.score)">
              评分：{{ selectedEvaluation.score }}分
            </el-tag>
          </div>
        </div>
        
        <el-divider>学员评价</el-divider>
        <div class="detail-section">
          <h4>训练收获：</h4>
          <p>{{ selectedEvaluation.studentComment || '暂无评价' }}</p>
          
          <h4>训练建议：</h4>
          <p>{{ selectedEvaluation.studentSuggestion || '暂无建议' }}</p>
        </div>
        
        <el-divider>教练反馈</el-divider>
        <div class="detail-section">
          <h4>表现评价：</h4>
          <p>{{ selectedEvaluation.coachComment || '暂无反馈' }}</p>
          
          <h4>改进建议：</h4>
          <p>{{ selectedEvaluation.coachSuggestion || '暂无建议' }}</p>
        </div>
        
        <div class="detail-tags" v-if="selectedEvaluation.tags && selectedEvaluation.tags.length">
          <h4>标签：</h4>
          <el-tag 
            v-for="tag in selectedEvaluation.tags" 
            :key="tag" 
            size="small" 
            type="info">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStudentEvaluations } from '@/api/evaluation'
import { getMyCoaches } from '@/api/student'

export default {
  name: 'StudentEvaluations',
  data() {
    return {
      evaluations: [],
      coachList: [],
      selectedEvaluation: null,
      detailDialogVisible: false,
      loading: false,
      filterForm: {
        coachId: '',
        dateRange: []
      },
      page: {
        current: 1,
        size: 10
      },
      total: 0
    }
  },
  filters: {
    formatDate(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  },
  created() {
    this.loadEvaluations()
    this.loadCoaches()
  },
  methods: {
    async loadEvaluations() {
      this.loading = true
      try {
        const params = {
          current: this.page.current,
          size: this.page.size,
          coachId: this.filterForm.coachId,
          startDate: this.filterForm.dateRange?.[0] || '',
          endDate: this.filterForm.dateRange?.[1] || ''
        }
        
        const response = await getStudentEvaluations(params)
        this.evaluations = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取评价记录失败:', error)
        this.$message.error('获取评价记录失败')
      } finally {
        this.loading = false
      }
    },
    async loadCoaches() {
      try {
        const response = await getMyCoaches()
        this.coachList = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    getScoreType(score) {
      if (score >= 4) return 'success'
      if (score >= 3) return 'warning'
      return 'danger'
    },
    handleFilter() {
      this.page.current = 1
      this.loadEvaluations()
    },
    resetFilter() {
      this.filterForm = {
        coachId: '',
        dateRange: []
      }
      this.handleFilter()
    },
    showEvaluationDetail(evaluation) {
      this.selectedEvaluation = evaluation
      this.detailDialogVisible = true
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadEvaluations()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadEvaluations()
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

.evaluation-list {
  min-height: 400px;
}

.evaluation-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}

.evaluation-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.evaluation-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.evaluation-date {
  color: #999;
  font-size: 14px;
  margin-right: 10px;
}

.evaluation-content {
  color: #666;
  line-height: 1.6;
}

.evaluation-section {
  margin-bottom: 15px;
}

.evaluation-section h5 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 14px;
}

.evaluation-section p {
  margin: 0;
  font-size: 14px;
}

.evaluation-tags {
  margin-top: 15px;
}

.evaluation-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.evaluation-detail {
  padding: 10px;
}

.detail-header {
  text-align: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-meta {
  color: #999;
  font-size: 14px;
}

.detail-meta .el-tag {
  margin-left: 10px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.detail-section p {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.6;
}

.detail-tags {
  margin-top: 20px;
}

.detail-tags h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
</style>