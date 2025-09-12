<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的比赛</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="已报名比赛" name="registered">
          <el-table
            v-loading="loading"
            :data="registeredTournaments"
            border
            style="width: 100%"
          >
            <el-table-column prop="name" label="比赛名称" />
            <el-table-column prop="group" label="参赛组别" width="100" />
            <el-table-column prop="date" label="比赛日期" width="120" />
            <el-table-column prop="time" label="比赛时间" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="{row}">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column label="操作" width="120" fixed="right">
              <template slot-scope="{row}">
                <el-button type="text" size="small" @click="viewTournamentDetail(row)">查看详情</el-button>
                <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="cancelRegistration(row)">取消报名</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="历史比赛" name="history">
          <el-table
            v-loading="loading"
            :data="historyTournaments"
            border
            style="width: 100%"
          >
            <el-table-column prop="name" label="比赛名称" />
            <el-table-column prop="group" label="参赛组别" width="100" />
            <el-table-column prop="date" label="比赛日期" width="120" />
            <el-table-column prop="rank" label="最终排名" width="100" />
            <el-table-column prop="matches" label="比赛场次" width="100" />
            <el-table-column prop="wins" label="胜场" width="80" />
            <el-table-column prop="losses" label="负场" width="80" />
            <el-table-column label="操作" width="100" fixed="right">
              <template slot-scope="{row}">
                <el-button type="text" size="small" @click="viewTournamentDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

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
      <div v-if="selectedTournament" class="tournament-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="比赛名称">{{ selectedTournament.name }}</el-descriptions-item>
          <el-descriptions-item label="参赛组别">{{ selectedTournament.group }}</el-descriptions-item>
          <el-descriptions-item label="比赛日期">{{ selectedTournament.date }}</el-descriptions-item>
          <el-descriptions-item label="比赛时间">{{ selectedTournament.time }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(selectedTournament.status) }}</el-descriptions-item>
          <el-descriptions-item label="报名费">¥{{ selectedTournament.fee }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedTournament.matches" class="matches-section">
          <h4>比赛安排</h4>
          <el-table :data="selectedTournament.matches" border style="width: 100%">
            <el-table-column prop="round" label="轮次" width="80" />
            <el-table-column prop="opponent" label="对手" />
            <el-table-column prop="table" label="球台" width="80" />
            <el-table-column prop="time" label="时间" width="120" />
            <el-table-column prop="result" label="结果" width="100">
              <template slot-scope="{row}">
                <el-tag :type="row.result === 'WIN' ? 'success' : 'danger'">
                  {{ row.result === 'WIN' ? '胜' : '负' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyTournaments, cancelTournamentRegistration } from '@/api/student'
import Pagination from '@/components/Pagination'

export default {
  name: 'MyTournaments',
  components: { Pagination },
  data() {
    return {
      activeTab: 'registered',
      registeredTournaments: [],
      historyTournaments: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      selectedTournament: null,
      detailDialogVisible: false
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const response = await getMyTournaments({
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          status: this.activeTab === 'registered' ? 'PENDING,ONGOING' : 'COMPLETED,CANCELLED'
        })
        
        if (this.activeTab === 'registered') {
          this.registeredTournaments = response.data.records || []
        } else {
          this.historyTournaments = response.data.records || []
        }
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取比赛记录失败:', error)
        this.$message.error('获取比赛记录失败')
      } finally {
        this.loading = false
      }
    },
    handleTabChange() {
      this.listQuery.page = 1
      this.getList()
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'info',
        'ONGOING': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'PENDING': '待开始',
        'ONGOING': '进行中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return textMap[status] || status
    },
    viewTournamentDetail(tournament) {
      this.selectedTournament = tournament
      this.detailDialogVisible = true
    },
    async cancelRegistration(tournament) {
      try {
        await this.$confirm(`确定要取消报名 ${tournament.name} 吗？报名费将退回您的账户。`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await cancelTournamentRegistration(tournament.id)
        this.$message.success('取消报名成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消报名失败:', error)
          this.$message.error('取消报名失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.matches-section {
  margin-top: 20px;
}

.matches-section h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>