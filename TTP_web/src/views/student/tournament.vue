<template>
  <div class="app-container">
    <!-- 比赛列表 -->
    <el-card>
      <div slot="header">
        <span>比赛报名</span>
        <el-button style="float: right;" type="primary" @click="loadTournaments">刷新</el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="可报名比赛" name="available">
          <el-row :gutter="20" v-loading="loading">
            <el-col :span="8" v-for="tournament in availableTournaments" :key="tournament.id" style="margin-bottom: 20px;">
              <el-card :body-style="{ padding: '20px' }" class="tournament-card">
                <div class="tournament-header">
                  <h4>{{ tournament.name }}</h4>
                  <el-tag :type="tournament.level | levelType">{{ tournament.level | levelFilter }}</el-tag>
                </div>
                
                <div class="tournament-info">
                  <p><i class="el-icon-date"></i> {{ tournament.date | formatDate }}</p>
                  <p><i class="el-icon-location"></i> {{ tournament.location }}</p>
                  <p><i class="el-icon-user"></i> 已报名：{{ tournament.registeredCount }}/{{ tournament.maxParticipants }}</p>
                  <p><i class="el-icon-money"></i> 报名费：{{ tournament.fee }}元</p>
                </div>

                <div class="tournament-description">
                  <p>{{ tournament.description || '暂无描述' }}</p>
                </div>

                <div class="tournament-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handleRegister(tournament)"
                    :disabled="tournament.registeredCount >= tournament.maxParticipants || tournament.registered">
                    {{ tournament.registered ? '已报名' : '立即报名' }}
                  </el-button>
                  <el-button size="small" @click="handleViewDetail(tournament)">查看详情</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="availableTournaments.length === 0 && !loading" description="暂无可报名比赛"></el-empty>
        </el-tab-pane>

        <el-tab-pane label="我的比赛" name="my">
          <el-table :data="myTournaments" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="比赛名称"></el-table-column>
            <el-table-column prop="level" label="组别" width="100">
              <template slot-scope="scope">
                {{ scope.row.level | levelFilter }}
              </template>
            </el-table-column>
            <el-table-column prop="date" label="比赛时间" width="120">
              <template slot-scope="scope">
                {{ scope.row.date | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="location" label="比赛地点" width="150"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status | statusType">{{ scope.row.status | statusFilter }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleViewDetail(scope.row)">详情</el-button>
                <el-button 
                  size="mini" 
                  type="danger" 
                  @click="handleCancelRegister(scope.row)"
                  :disabled="scope.row.status !== 'REGISTERED'">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="myTournaments.length === 0 && !loading" description="暂无参与比赛"></el-empty>
        </el-tab-pane>

        <el-tab-pane label="比赛结果" name="results">
          <el-table :data="tournamentResults" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="比赛名称"></el-table-column>
            <el-table-column prop="level" label="组别" width="100">
              <template slot-scope="scope">
                {{ scope.row.level | levelFilter }}
              </template>
            </el-table-column>
            <el-table-column prop="date" label="比赛时间" width="120">
              <template slot-scope="scope">
                {{ scope.row.date | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.rank | rankType">第{{ scope.row.rank }}名</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleViewResult(scope.row)">查看赛程</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="tournamentResults.length === 0 && !loading" description="暂无比赛结果"></el-empty>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 比赛详情对话框 -->
    <el-dialog title="比赛详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedTournament">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="比赛名称">{{ selectedTournament.name }}</el-descriptions-item>
          <el-descriptions-item label="比赛组别">{{ selectedTournament.level | levelFilter }}</el-descriptions-item>
          <el-descriptions-item label="比赛时间">{{ selectedTournament.date | formatDate }} {{ selectedTournament.time }}</el-descriptions-item>
          <el-descriptions-item label="比赛地点">{{ selectedTournament.location }}</el-descriptions-item>
          <el-descriptions-item label="报名费用">{{ selectedTournament.fee }}元</el-descriptions-item>
          <el-descriptions-item label="最大人数">{{ selectedTournament.maxParticipants }}人</el-descriptions-item>
          <el-descriptions-item label="已报名人数">{{ selectedTournament.registeredCount }}人</el-descriptions-item>
          <el-descriptions-item label="报名状态">
            <el-tag :type="selectedTournament.registered ? 'success' : 'info'">
              {{ selectedTournament.registered ? '已报名' : '未报名' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider></el-divider>
        
        <div>
          <h4>比赛描述</h4>
          <p>{{ selectedTournament.description || '暂无描述' }}</p>
        </div>
        
        <div style="margin-top: 20px;">
          <h4>比赛规则</h4>
          <p>{{ selectedTournament.rules || '暂无规则说明' }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleRegister(selectedTournament)"
          :disabled="selectedTournament.registeredCount >= selectedTournament.maxParticipants || selectedTournament.registered"
          v-if="activeTab === 'available'">
          {{ selectedTournament.registered ? '已报名' : '立即报名' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 赛程对话框 -->
    <el-dialog title="比赛赛程" :visible.sync="scheduleDialogVisible" width="70%">
      <div v-if="selectedTournament && selectedTournament.schedule">
        <el-table :data="selectedTournament.schedule" style="width: 100%">
          <el-table-column prop="round" label="轮次" width="80"></el-table-column>
          <el-table-column prop="time" label="时间" width="120"></el-table-column>
          <el-table-column prop="table" label="球台" width="80"></el-table-column>
          <el-table-column label="对阵">
            <template slot-scope="scope">
              <div v-if="scope.row.player1 && scope.row.player2">
                {{ scope.row.player1 }} vs {{ scope.row.player2 }}
              </div>
              <div v-else>
                <span style="color: #999;">待定</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="结果">
            <template slot-scope="scope">
              <span v-if="scope.row.result">{{ scope.row.result }}</span>
              <span v-else style="color: #999;">未开始</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAvailableTournaments, getMyTournaments, getTournamentResults, registerTournament, cancelTournamentRegistration } from '@/api/tournament'

export default {
  name: 'Tournament',
  data() {
    return {
      activeTab: 'available',
      availableTournaments: [],
      myTournaments: [],
      tournamentResults: [],
      selectedTournament: null,
      detailDialogVisible: false,
      scheduleDialogVisible: false,
      loading: false
    }
  },
  filters: {
    levelFilter(level) {
      const levelMap = {
        'A': '甲组',
        'B': '乙组',
        'C': '丙组'
      }
      return levelMap[level] || level
    },
    levelType(level) {
      const typeMap = {
        'A': 'danger',
        'B': 'warning',
        'C': 'success'
      }
      return typeMap[level] || ''
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    statusFilter(status) {
      const statusMap = {
        'REGISTERED': '已报名',
        'IN_PROGRESS': '进行中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    statusType(status) {
      const typeMap = {
        'REGISTERED': 'success',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'info',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || ''
    },
    rankType(rank) {
      if (rank === 1) return 'danger'
      if (rank <= 3) return 'warning'
      return 'info'
    }
  },
  created() {
    this.loadTournaments()
  },
  methods: {
    async loadTournaments() {
      this.loading = true
      try {
        if (this.activeTab === 'available') {
          const response = await getAvailableTournaments()
          this.availableTournaments = response.data || []
        } else if (this.activeTab === 'my') {
          const response = await getMyTournaments()
          this.myTournaments = response.data || []
        } else if (this.activeTab === 'results') {
          const response = await getTournamentResults()
          this.tournamentResults = response.data || []
        }
      } catch (error) {
        this.$message.error('获取比赛信息失败')
        console.error('获取比赛信息失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleTabChange() {
      this.loadTournaments()
    },
    async handleRegister(tournament) {
      if (tournament.registeredCount >= tournament.maxParticipants) {
        this.$message.warning('比赛人数已满')
        return
      }

      try {
        await this.$confirm(`确认报名参加 ${tournament.name} 吗？需要支付 ${tournament.fee} 元报名费。`, '确认报名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await registerTournament(tournament.id, tournament.level)
        this.$message.success('报名成功')
        this.loadTournaments()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '报名失败')
        }
      }
    },
    async handleCancelRegister(tournament) {
      try {
        await this.$confirm('确认取消报名吗？报名费将退还到您的账户。', '确认取消', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await cancelTournamentRegistration(tournament.id)
        this.$message.success('取消报名成功')
        this.loadTournaments()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('取消报名失败')
        }
      }
    },
    handleViewDetail(tournament) {
      this.selectedTournament = tournament
      this.detailDialogVisible = true
    },
    handleViewResult(tournament) {
      this.selectedTournament = tournament
      this.scheduleDialogVisible = true
    }
  }
}
</script>

<style scoped>
.tournament-card {
  transition: all 0.3s;
}

.tournament-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.tournament-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.tournament-header h4 {
  margin: 0;
  color: #333;
}

.tournament-info {
  margin: 15px 0;
}

.tournament-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.tournament-info i {
  margin-right: 5px;
  color: #409EFF;
}

.tournament-description {
  margin: 15px 0;
  min-height: 40px;
}

.tournament-description p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.tournament-actions {
  display: flex;
  justify-content: space-between;
}

@media screen and (max-width: 768px) {
  .el-col-8 {
    width: 100%;
  }
}
</style>