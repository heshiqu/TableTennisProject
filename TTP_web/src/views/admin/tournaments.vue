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
      <el-select v-model="listQuery.level" placeholder="比赛级别" clearable style="width: 120px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
      <el-select v-model="listQuery.status" placeholder="比赛状态" clearable style="width: 120px" class="filter-item" @change="handleFilter">
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
        class="filter-item"
        style="margin-left: 10px;"
        type="success"
        icon="el-icon-plus"
        @click="handleCreate"
      >
        创建比赛
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
      <el-table-column label="比赛名称" min-width="200px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="比赛级别" width="100px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.level | levelFilter">
            {{ row.level | levelNameFilter }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.startTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.endTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名费用" width="100px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ row.entryFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名人数" width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.currentParticipants }}/{{ row.maxParticipants }}</span>
        </template>
      </el-table-column>
      <el-table-column label="比赛地点" min-width="200px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.location }}</span>
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
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.status === 0" size="mini" type="success" @click="handlePublish(row)">
            发布
          </el-button>
          <el-button v-if="row.status === 1" size="mini" type="warning" @click="handleEndRegistration(row)">
            截止报名
          </el-button>
          <el-button v-if="row.status === 2" size="mini" type="primary" @click="handleStartTournament(row)">
            开始比赛
          </el-button>
          <el-button v-if="row.status === 3" size="mini" type="success" @click="handleEndTournament(row)">
            结束比赛
          </el-button>
          <el-button size="mini" type="danger" @click="handleDelete(row,$index)">
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

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 500px; margin-left:50px;"
      >
        <el-form-item label="比赛名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入比赛名称" />
        </el-form-item>
        <el-form-item label="比赛级别" prop="level">
          <el-select v-model="temp.level" class="filter-item" placeholder="请选择比赛级别">
            <el-option v-for="item in levelOptions" :key="item.key" :label="item.display_name" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="temp.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="temp.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="报名费用" prop="entryFee">
          <el-input-number v-model="temp.entryFee" :min="0" :precision="0" :step="10" placeholder="请输入报名费用" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="temp.maxParticipants" :min="1" :max="100" placeholder="请输入最大参赛人数" />
        </el-form-item>
        <el-form-item label="比赛地点" prop="location">
          <el-input v-model="temp.location" placeholder="请输入比赛地点" />
        </el-form-item>
        <el-form-item label="比赛描述" prop="description">
          <el-input v-model="temp.description" type="textarea" :rows="4" placeholder="请输入比赛描述" />
        </el-form-item>
        <el-form-item label="比赛规则" prop="rules">
          <el-input v-model="temp.rules" type="textarea" :rows="4" placeholder="请输入比赛规则" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">
          确认
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTournamentList, createTournament, updateTournament, deleteTournament, publishTournament, endRegistration, startTournament, endTournament } from '@/api/tournament'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'TournamentManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        0: 'info',
        1: 'success',
        2: 'warning',
        3: 'primary',
        4: 'success'
      }
      return statusMap[status]
    },
    statusNameFilter(status) {
      const statusNameMap = {
        0: '未发布',
        1: '报名中',
        2: '报名截止',
        3: '进行中',
        4: '已结束'
      }
      return statusNameMap[status]
    },
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
        1: '甲组',
        2: '乙组',
        3: '丙组'
      }
      return levelNameMap[level]
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
        { key: 1, display_name: '甲组' },
        { key: 2, display_name: '乙组' },
        { key: 3, display_name: '丙组' }
      ],
      statusOptions: [
        { key: 0, display_name: '未发布' },
        { key: 1, display_name: '报名中' },
        { key: 2, display_name: '报名截止' },
        { key: 3, display_name: '进行中' },
        { key: 4, display_name: '已结束' }
      ],
      temp: {
        id: undefined,
        name: '',
        level: 1,
        startTime: '',
        endTime: '',
        entryFee: 30,
        maxParticipants: 16,
        location: '',
        description: '',
        rules: '',
        status: 0
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑比赛',
        create: '创建比赛'
      },
      rules: {
        name: [{ required: true, message: '比赛名称不能为空', trigger: 'blur' }],
        level: [{ required: true, message: '请选择比赛级别', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        entryFee: [{ required: true, message: '请输入报名费用', trigger: 'blur' }],
        maxParticipants: [{ required: true, message: '请输入最大参赛人数', trigger: 'blur' }],
        location: [{ required: true, message: '比赛地点不能为空', trigger: 'blur' }],
        description: [{ required: true, message: '比赛描述不能为空', trigger: 'blur' }],
        rules: [{ required: true, message: '比赛规则不能为空', trigger: 'blur' }]
      }
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
    resetTemp() {
      this.temp = {
        id: undefined,
        name: '',
        level: 1,
        startTime: '',
        endTime: '',
        entryFee: 30,
        maxParticipants: 16,
        location: '',
        description: '',
        rules: '',
        status: 0
      }
    },
    handleCreate() {
      this.resetTemp()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          createTournament(this.temp).then(response => {
            if (response.code === 200) {
              this.list.unshift(response.data)
              this.dialogFormVisible = false
              this.$notify({
                title: '成功',
                message: '创建成功',
                type: 'success',
                duration: 2000
              })
            }
          })
        }
      })
    },
    handleDetail(row) {
      this.$router.push(`/admin/tournaments/${row.id}`)
    },
    handlePublish(row) {
      this.$confirm('确认发布该比赛吗？发布后学员即可报名', '提示', {
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
    handleEndRegistration(row) {
      this.$confirm('确认截止该比赛的报名吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        endRegistration(row.id).then(response => {
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
    handleStartTournament(row) {
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
    handleEndTournament(row) {
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
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.filter-container {
  padding-bottom: 10px;
  .filter-item {
    display: inline-block;
    vertical-align: middle;
    margin-bottom: 10px;
  }
}
</style>