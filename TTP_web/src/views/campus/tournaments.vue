<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="比赛名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select v-model="listQuery.level" placeholder="级别" clearable style="width: 110px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 110px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-date-picker
        v-model="listQuery.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 300px;"
        class="filter-item"
        @change="handleFilter"
      />
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
      <el-button
        :loading="downloadLoading"
        style="margin-left: 10px;"
        type="info"
        icon="el-icon-download"
        @click="handleDownload"
      >
        导出
      </el-button>
    </div>

    <el-table
      :key="tableKey"
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="序号" prop="id" align="center" width="80">
        <template slot-scope="{row}">
          <span>{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column label="比赛名称" min-width="150px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="级别" width="80px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.level | levelFilter">
            {{ row.level | levelNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="比赛时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.startTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名截止" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.deadline | parseTime('{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名人数" width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.registeredCount }}/{{ row.maxParticipants }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名费" width="80px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ row.fee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" class-name="status-col" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status | statusFilter">
            {{ row.status | statusNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.status === 0" size="mini" type="success" @click="handlePublish(row)">
            发布
          </el-button>
          <el-button v-if="row.status === 1" size="mini" type="warning" @click="handleCloseRegistration(row)">
            截止报名
          </el-button>
          <el-button v-if="row.status === 2" size="mini" type="primary" @click="handleStart(row)">
            开始比赛
          </el-button>
          <el-button v-if="row.status === 3" size="mini" type="info" @click="handleEnd(row)">
            结束比赛
          </el-button>
          <el-button v-if="row.status === 4" size="mini" type="danger" @click="handleDelete(row, $index)">
            删除
          </el-button>
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

    <!-- 比赛详情对话框 -->
    <el-dialog title="比赛详情" :visible.sync="detailDialogVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="比赛名称">{{ tournamentDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="级别">
          <el-tag :type="tournamentDetail.level | levelFilter">
            {{ tournamentDetail.level | levelNameFilter }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="比赛时间">{{ tournamentDetail.startTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ tournamentDetail.deadline | parseTime('{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="报名人数">{{ tournamentDetail.registeredCount }}/{{ tournamentDetail.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="报名费">¥{{ tournamentDetail.fee }}</el-descriptions-item>
        <el-descriptions-item label="比赛状态">
          <el-tag :type="tournamentDetail.status | statusFilter">
            {{ tournamentDetail.status | statusNameFilter }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ tournamentDetail.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</el-descriptions-item>
        <el-descriptions-item label="比赛描述" :span="2">{{ tournamentDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <div v-if="tournamentDetail.participants && tournamentDetail.participants.length > 0">
        <h4 style="margin-top: 20px;">参赛学员列表</h4>
        <el-table :data="tournamentDetail.participants" border style="width: 100%">
          <el-table-column prop="studentName" label="学员姓名" width="120" />
          <el-table-column prop="studentLevel" label="学员级别" width="100" />
          <el-table-column prop="registerTime" label="报名时间" width="160">
            <template slot-scope="{row}">
              {{ row.registerTime | parseTime('{y}-{m}-{d} {h}:{i}') }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                {{ row.status === 1 ? '已确认' : '待确认' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTournamentList, publishTournament, closeRegistration, startTournament, endTournament, deleteTournament } from '@/api/tournament'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusTournamentManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    levelFilter(level) {
      const levelMap = {
        1: 'danger',
        2: 'warning',
        3: 'success'
      }
      return levelMap[level]
    },
    levelNameFilter(level) {
      const levelNameMap = {
        1: '甲级',
        2: '乙级',
        3: '丙级'
      }
      return levelNameMap[level]
    },
    statusFilter(status) {
      const statusMap = {
        0: 'info',
        1: 'primary',
        2: 'warning',
        3: 'success',
        4: 'danger'
      }
      return statusMap[status]
    },
    statusNameFilter(status) {
      const statusNameMap = {
        0: '待发布',
        1: '报名中',
        2: '报名截止',
        3: '进行中',
        4: '已结束'
      }
      return statusNameMap[status]
    }
  },
  data() {
    return {
      tableKey: 0,
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        name: undefined,
        level: undefined,
        status: undefined,
        dateRange: []
      },
      levelOptions: [
        { key: 1, display_name: '甲级' },
        { key: 2, display_name: '乙级' },
        { key: 3, display_name: '丙级' }
      ],
      statusOptions: [
        { key: 0, display_name: '待发布' },
        { key: 1, display_name: '报名中' },
        { key: 2, display_name: '报名截止' },
        { key: 3, display_name: '进行中' },
        { key: 4, display_name: '已结束' }
      ],
      detailDialogVisible: false,
      tournamentDetail: {},
      downloadLoading: false
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const response = await getTournamentList(this.listQuery)
        if (response.code === 200) {
          this.list = response.data.records
          this.total = response.data.total
        }
      } catch (error) {
        console.error('获取比赛列表失败:', error)
      }
      this.listLoading = false
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDetail(row) {
      this.tournamentDetail = row
      this.detailDialogVisible = true
    },
    handlePublish(row) {
      this.$confirm('确认发布该比赛吗？发布后学员可开始报名', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        publishTournament(row.id).then(response => {
          if (response.code === 200) {
            row.status = 1
            this.$notify({
              title: '成功',
              message: '比赛已发布',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleCloseRegistration(row) {
      this.$confirm('确认截止该比赛报名吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        closeRegistration(row.id).then(response => {
          if (response.code === 200) {
            row.status = 2
            this.$notify({
              title: '成功',
              message: '报名已截止',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleStart(row) {
      this.$confirm('确认开始该比赛吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        startTournament(row.id).then(response => {
          if (response.code === 200) {
            row.status = 3
            this.$notify({
              title: '成功',
              message: '比赛已开始',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleEnd(row) {
      this.$confirm('确认结束该比赛吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        endTournament(row.id).then(response => {
          if (response.code === 200) {
            row.status = 4
            this.$notify({
              title: '成功',
              message: '比赛已结束',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleDelete(row, index) {
      this.$confirm('确认删除该比赛吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTournament(row.id).then(response => {
          if (response.code === 200) {
            this.list.splice(index, 1)
            this.$notify({
              title: '成功',
              message: '比赛已删除',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '比赛名称', '比赛级别', '比赛日期', '报名截止', '参赛人数', '比赛状态', '创建时间']
        const filterVal = ['id', 'name', 'level', 'startDate', 'registrationDeadline', 'participantCount', 'status', 'createTime']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '校区比赛列表'
        })
        this.downloadLoading = false
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'startTime' || j === 'deadline' || j === 'createTime') {
          return parseTime(v[j])
        } else if (j === 'level') {
          const levelMap = { 1: '甲级', 2: '乙级', 3: '丙级' }
          return levelMap[v[j]]
        } else if (j === 'status') {
          const statusMap = { 0: '待发布', 1: '报名中', 2: '报名截止', 3: '进行中', 4: '已结束' }
          return statusMap[v[j]]
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>