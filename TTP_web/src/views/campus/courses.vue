<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="课程名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select v-model="listQuery.coachId" placeholder="教练" clearable style="width: 150px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in coachOptions" :key="item.id" :label="item.realName" :value="item.id" />
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
      <el-table-column label="课程名称" min-width="150px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="教练姓名" width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.coachName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学员姓名" width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.studentName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="日期" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.courseDate | parseTime('{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.startTime }} - {{ row.endTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="球台" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.tableNumber }}</span>
        </template>
      </el-table-column>
      <el-table-column label="费用" width="80px" align="center">
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
          <el-button v-if="row.status === 0" size="mini" type="success" @click="handleConfirm(row)">
            确认
          </el-button>
          <el-button v-if="row.status === 0" size="mini" type="danger" @click="handleCancel(row)">
            取消
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

    <!-- 课程详情对话框 -->
    <el-dialog title="课程详情" :visible.sync="detailDialogVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程名称">{{ courseDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="教练姓名">{{ courseDetail.coachName }}</el-descriptions-item>
        <el-descriptions-item label="学员姓名">{{ courseDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="课程日期">{{ courseDetail.courseDate | parseTime('{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ courseDetail.startTime }} - {{ courseDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="球台号">{{ courseDetail.tableNumber }}</el-descriptions-item>
        <el-descriptions-item label="课程费用">¥{{ courseDetail.fee }}</el-descriptions-item>
        <el-descriptions-item label="课程状态">
          <el-tag :type="courseDetail.status | statusFilter">
            {{ courseDetail.status | statusNameFilter }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ courseDetail.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ courseDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCourseList, adminConfirmCourse, adminCancelCourse } from '@/api/course'
import { getCoachList } from '@/api/coach'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import Pagination from '@/components/Pagination'

export default {
  name: 'CampusCourseManage',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        0: 'warning',
        1: 'success',
        2: 'danger',
        3: 'info'
      }
      return statusMap[status]
    },
    statusNameFilter(status) {
      const statusNameMap = {
        0: '待确认',
        1: '已确认',
        2: '已取消',
        3: '已完成'
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
        coachId: undefined,
        status: undefined,
        dateRange: []
      },
      statusOptions: [
        { key: 0, display_name: '待确认' },
        { key: 1, display_name: '已确认' },
        { key: 2, display_name: '已取消' },
        { key: 3, display_name: '已完成' }
      ],
      coachOptions: [],
      detailDialogVisible: false,
      courseDetail: {},
      downloadLoading: false
    }
  },
  created() {
    this.getList()
    this.loadCoaches()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const response = await getCourseList(this.listQuery)
        if (response.code === 200) {
          this.list = response.data.records
          this.total = response.data.total
        }
      } catch (error) {
        console.error('获取课程列表失败:', error)
      }
      this.listLoading = false
    },
    async loadCoaches() {
      try {
        const response = await getCoachList({ page: 1, limit: 100 })
        if (response.code === 200) {
          this.coachOptions = response.data.records
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
      this.courseDetail = row
      this.detailDialogVisible = true
    },
    handleConfirm(row) {
      this.$confirm('确认该课程预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        adminConfirmCourse(row.id).then(response => {
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
      this.$confirm('确认取消该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        adminCancelCourse(row.id).then(response => {
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
    handleDownload() {
      this.downloadLoading = true
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '课程名称', '教练姓名', '学员姓名', '课程日期', '上课时间', '球台号', '课程费用', '课程状态', '创建时间']
        const filterVal = ['id', 'name', 'coachName', 'studentName', 'courseDate', 'startTime', 'tableNumber', 'fee', 'status', 'createTime']
        const data = this.formatJson(filterVal)
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '校区课程列表'
        })
        this.downloadLoading = false
      })
    },
    formatJson(filterVal) {
      return this.list.map(v => filterVal.map(j => {
        if (j === 'courseDate') {
          return parseTime(v[j], '{y}-{m}-{d}')
        } else if (j === 'status') {
          const statusMap = { 0: '待确认', 1: '已确认', 2: '已取消', 3: '已完成' }
          return statusMap[v[j]]
        } else if (j === 'createTime') {
          return parseTime(v[j])
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>