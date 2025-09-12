<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="listQuery.coachId" placeholder="教练" clearable style="width: 150px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in coachOptions" :key="item.id" :label="item.realName" :value="item.id" />
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
      <el-table-column label="教练" min-width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.coachName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学员" min-width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.studentName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="场地" min-width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.courtNumber || '-' }}</span>
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
      <el-table-column label="时长" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.duration || 0 }}小时</span>
        </template>
      </el-table-column>
      <el-table-column label="费用" width="100px" align="center">
        <template slot-scope="{row}">
          <span>¥{{ row.fee || 0 }}</span>
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
          <span>{{ row.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.status === 'PENDING'" size="mini" type="success" @click="handleConfirm(row)">
            确认
          </el-button>
          <el-button v-if="row.status === 'PENDING'" size="mini" type="danger" @click="handleCancel(row)">
            取消
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
  </div>
</template>

<script>
import { getCourseList, confirmCourse, cancelCourse, deleteCourse } from '@/api/course'
import { getCoachList } from '@/api/coach'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CourseManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        'PENDING': 'info',
        'CONFIRMED': 'success',
        'CANCELLED': 'warning',
        'COMPLETED': 'danger',
        'EVALUATED': 'success'
      }
      return statusMap[status] || 'info'
    },
    statusNameFilter(status) {
      const statusNameMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成',
        'EVALUATED': '已评价'
      }
      return statusNameMap[status] || status
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
        coachId: undefined,
        dateRange: []
      },
      coachOptions: []
    }
  },
  created() {
    this.getList()
    this.loadCoachList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const response = await getCourseList(this.listQuery)
        if (response.code === 200) {
          this.list = response.data.content
          this.total = response.data.totalElements
        }
      } catch (error) {
        console.error('获取课程列表失败:', error)
      }
      this.listLoading = false
    },

    async loadCoachList() {
      try {
        const response = await getCoachList({ page: 1, limit: 100, status: 1 })
        if (response.code === 200) {
          this.coachOptions = response.data.records || []
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDetail(row) {
      this.$router.push(`/admin/courses/${row.id}`)
    },
    handleConfirm(row) {
      this.$confirm('确认该课程预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        confirmCourse(row.id).then(response => {
          if (response.code === 200) {
            row.status = 1
            this.$notify({
              title: '成功',
              message: '课程已确认',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleCancel(row) {
      this.$confirm('取消该课程预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelCourse(row.id).then(response => {
          if (response.code === 200) {
            row.status = 2
            this.$notify({
              title: '成功',
              message: '课程已取消',
              type: 'success',
              duration: 2000
            })
          }
        })
      })
    },
    handleDelete(row, index) {
      this.$confirm('确认删除该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourse(row.id).then(response => {
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
    },
    handleDownload() {
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '教练', '学员', '场地', '开始时间', '结束时间', '时长(小时)', '费用', '状态', '创建时间']
        const filterVal = ['id', 'coachName', 'studentName', 'courtNumber', 'startTime', 'endTime', 'duration', 'fee', 'status', 'createdAt']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '课程列表'
        })
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'status') {
          const statusNameMap = { 
            'PENDING': '待确认', 
            'CONFIRMED': '已确认', 
            'CANCELLED': '已取消', 
            'COMPLETED': '已完成', 
            'EVALUATED': '已评价' 
          }
          return statusNameMap[v[j]] || v[j]
        } else if (j === 'startTime' || j === 'endTime' || j === 'createdAt') {
          return parseTime(v[j], '{y}-{m}-{d} {h}:{i}')
        } else {
          return v[j]
        }
      }))
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