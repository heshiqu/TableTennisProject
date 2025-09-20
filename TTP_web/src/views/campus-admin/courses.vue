<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="按教练姓名，学生姓名查询"
        style="width: 200px;"
        class="filter-item"
        @input="handleFilter"
      />
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 110px" class="filter-item" @change="handleFilter">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.display_name" :value="item.key" />
      </el-select>
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
      <el-table-column label="序号" align="center" width="80">
        <template slot-scope="{$index}">
          <span>{{ $index + 1 }}</span>
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
          <span>{{ row.courseDate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.startTime }} - {{ row.endTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="球台" width="80px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.courtNumber }}</span>
        </template>
      </el-table-column>
      <el-table-column label="课时" width="60px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.duration }}小时</span>
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
          <el-button v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" size="mini" type="danger" @click="handleCancel(row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 课程详情对话框 -->
    <el-dialog title="课程详情" :visible.sync="detailDialogVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="教练姓名">{{ courseDetail.coachName }}</el-descriptions-item>
        <el-descriptions-item label="学员姓名">{{ courseDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="课程日期">{{ courseDetail.courseDate }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ courseDetail.startTime }} - {{ courseDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="球台号">{{ courseDetail.courtNumber }}</el-descriptions-item>
        <el-descriptions-item label="课时">{{ courseDetail.duration }}小时</el-descriptions-item>
        <el-descriptions-item label="课程费用">¥{{ courseDetail.fee }}</el-descriptions-item>
        <el-descriptions-item label="课程状态">
          <el-tag :type="courseDetail.status | statusFilter">
            {{ courseDetail.status | statusNameFilter }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ courseDetail.createdAt | parseTime('{y}-{m}-{d} {h}:{i}') }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" v-if="courseDetail.status === 'CANCELLED'" :span="2">{{ courseDetail.cancelReason || '无' }}</el-descriptions-item>
        <el-descriptions-item label="取消人" v-if="courseDetail.status === 'CANCELLED'" :span="2">{{ courseDetail.cancelByUserName || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCampusCourses, adminConfirmCourse, adminCancelCourse } from '@/api/course'
import waves from '@/directive/waves'
import { parseTime } from '@/utils'
import request from '@/utils/request'

// 添加防抖函数
function debounce(fn, delay) {
  let timer = null
  return function() {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(context, args)
    }, delay)
  }
}

export default {
  name: 'CampusCourseManage',
  components: {},
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'CANCELLED': 'danger',
        'COMPLETED': 'info'
      }
      return statusMap[status]
    },
    statusNameFilter(status) {
      const statusNameMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成'
      }
      return statusNameMap[status]
    }
  },
  data() {
    return {
      tableKey: 0,
      list: null, // 用于显示的筛选后的数据
      allCourses: null, // 存储从API获取的完整原始数据
      total: 0,
      listLoading: true,
      listQuery: {
        name: undefined,
        status: undefined
      },
      statusOptions: [
        { key: 'PENDING', display_name: '待确认' },
        { key: 'CONFIRMED', display_name: '已确认' },
        { key: 'CANCELLED', display_name: '已取消' },
        { key: 'COMPLETED', display_name: '已完成' }
      ],
      detailDialogVisible: false,
      courseDetail: {},
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
        // 从用户信息中获取当前校区管理员的campusId
        const userInfo = this.$store.getters.userInfo || { campusId: 2 } // 默认值为2，实际应该从store获取
        const campusId = userInfo.campusId
        
        // 获取完整数据，不传递筛选参数
        const response = await getCampusCourses(campusId)
        if (response.code === 200) {
          // 由于API直接返回数组，需要进行格式化处理
          this.allCourses = response.data.map(course => {
            // 格式化日期时间
            const startDate = new Date(course.startTime)
            const formattedDate = startDate.toLocaleDateString('zh-CN')
            const formattedStartTime = startDate.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
            const endDate = new Date(course.endTime)
            const formattedEndTime = endDate.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
            
            return {
              ...course,
              courseDate: formattedDate,
              startTime: formattedStartTime,
              endTime: formattedEndTime,
              createTime: new Date(course.createdAt).toLocaleString('zh-CN')
            }
          })
          
          // 初始化时显示全部数据
          this.applyFilter()
        }
      } catch (error) {
        console.error('获取课程列表失败:', error)
      }
      this.listLoading = false
    },
    // 在前端进行筛选
    applyFilter() {
      if (!this.allCourses) {
        this.list = null
        this.total = 0
        return
      }
      
      let filteredList = [...this.allCourses]
      
      // 按姓名筛选（教练姓名或学生姓名）
      if (this.listQuery.name) {
        const searchName = this.listQuery.name.toLowerCase()
        filteredList = filteredList.filter(item => 
          (item.coachName && item.coachName.toLowerCase().includes(searchName)) ||
          (item.studentName && item.studentName.toLowerCase().includes(searchName))
        )
      }
      
      // 按状态筛选
      if (this.listQuery.status) {
        filteredList = filteredList.filter(item => item.status === this.listQuery.status)
      }
      
      this.list = filteredList
      this.total = filteredList.length
    },
    // 使用防抖优化handleFilter方法
    handleFilter: debounce(function() {
      this.applyFilter()
    }, 300),
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
        request({
          url: `/api/courses/${row.id}/confirm`,
          method: 'post'
        }).then(response => {
          if (response.code === 200) {
            // 刷新列表以更新状态
            this.getList()
            this.$notify({
              title: '成功',
              message: '课程已确认',
              type: 'success',
              duration: 2000
            })
          } else {
            // 显示失败信息
            this.$notify({
              title: '失败',
              message: response.message || '课程确认失败',
              type: 'error',
              duration: 3000
            })
          }
        }).catch(error => {
          // 处理网络请求异常，尝试从error对象中提取message
          let errorMessage = '网络错误，请稍后重试'
          if (error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message
          } else if (error.message) {
            errorMessage = error.message
          }
          this.$notify({
            title: '失败',
            message: errorMessage,
            type: 'error',
            duration: 3000
          })
          console.error('确认课程失败:', error)
        })
      })
    },
    handleCancel(row) {
      this.$confirm('确认取消该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 获取当前登录管理员的id
        const userInfo = this.$store.getters.userInfo || { id: 2 } // 默认值为2，实际应该从store获取
        const cancelledBy = userInfo.id
        
        // 发送取消请求
        request({
          url: `/api/courses/${row.id}/cancel`,
          method: 'post',
          params: {
            reason: '管理员取消',
            cancelledBy: cancelledBy
          }
        }).then(response => {
          if (response.code === 200) {
            // 刷新列表以更新状态
            this.getList()
            this.$notify({
              title: '成功',
              message: '课程已取消',
              type: 'success',
              duration: 2000
            })
          } else {
            // 显示失败信息
            this.$notify({
              title: '失败',
              message: response.message || '课程取消失败',
              type: 'error',
              duration: 3000
            })
          }
        }).catch(error => {
          // 处理网络请求异常，尝试从error对象中提取message
          let errorMessage = '网络错误，请稍后重试'
          if (error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message
          } else if (error.message) {
            errorMessage = error.message
          }
          this.$notify({
            title: '失败',
            message: errorMessage,
            type: 'error',
            duration: 3000
          })
          console.error('取消课程失败:', error)
        })
      })
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/utils/export2excel').then(excel => {
        const tHeader = ['ID', '教练姓名', '学员姓名', '课程日期', '上课时间', '球台号', '课时', '课程费用', '课程状态', '创建时间']
        const filterVal = ['id', 'coachName', 'studentName', 'startTime', 'startTime', 'courtNumber', 'duration', 'fee', 'status', 'createdAt']
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
      // 使用allCourses而非list，确保导出所有数据
      return (this.allCourses || []).map(v => filterVal.map(j => {
        if (j === 'startTime' && filterVal.indexOf(j) === 3) {
          return v.courseDate
        } else if (j === 'startTime' && filterVal.indexOf(j) === 4) {
          return `${v.startTime} - ${v.endTime}`
        } else if (j === 'status') {
          const statusMap = { 'PENDING': '待确认', 'CONFIRMED': '已确认', 'CANCELLED': '已取消', 'COMPLETED': '已完成' }
          return statusMap[v[j]]
        } else if (j === 'createdAt') {
          return v.createTime
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>