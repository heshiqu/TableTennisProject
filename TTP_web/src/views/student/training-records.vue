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
        <el-table-column prop="date" label="训练日期" width="120" />
        <el-table-column prop="time" label="训练时间" width="120" />
        <el-table-column prop="coachName" label="教练姓名" width="100" />
        <el-table-column prop="duration" label="训练时长" width="80">
          <template slot-scope="{row}">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="tableNumber" label="球台号" width="80" />
        <el-table-column prop="fee" label="课时费" width="80">
          <template slot-scope="{row}">
            ¥{{ row.fee }}
          </template>
        </el-table-column>
        <el-table-column prop="studentEvaluation" label="我的评价" />
        <el-table-column prop="coachEvaluation" label="教练评价" />
        <el-table-column label="操作" width="100" fixed="right">
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
        
        <div class="evaluation-section">
          <h4>我的评价</h4>
          <p>{{ selectedRecord.studentEvaluation || '暂无评价' }}</p>
        </div>
        
        <div class="evaluation-section">
          <h4>教练评价</h4>
          <p>{{ selectedRecord.coachEvaluation || '暂无评价' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTrainingRecords } from '@/api/student'
import { getMyCoaches } from '@/api/student'
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
      detailDialogVisible: false
    }
  },
  created() {
    this.loadMyCoaches()
    this.getList()
  },
  methods: {
    async loadMyCoaches() {
      try {
        const response = await getMyCoaches()
        this.myCoaches = response.data || []
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          startDate: this.dateRange?.[0] || '',
          endDate: this.dateRange?.[1] || '',
          coachId: this.coachFilter || ''
        }
        
        const response = await getTrainingRecords(params)
        this.trainingRecords = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取训练记录失败:', error)
        this.$message.error('获取训练记录失败')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    viewDetail(row) {
      this.selectedRecord = row
      this.detailDialogVisible = true
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
</style>