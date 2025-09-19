<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>课程管理</span>
      </div>
      
      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="按校区，教练姓名，学生姓名查询"
          style="width: 200px;"
          @input="handleFilter"
        />
        <el-select v-model="campusFilter" placeholder="所属校区" clearable @change="handleFilter">
          <el-option
            v-for="campus in campuses"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>

        <el-select v-model="statusFilter" placeholder="课程状态" clearable @change="handleFilter">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已取消" value="CANCELLED" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <el-table
          v-loading="loading"
          :data="courses"
          border
          style="width: 100%"
          fit
        >
          <el-table-column type="index" label="序号" min-width="80" :index="indexMethod" />
          <el-table-column prop="campusName" label="所属校区" min-width="120" />
          <el-table-column prop="coachName" label="教练姓名" min-width="100" />
          <el-table-column prop="studentName" label="学员姓名" min-width="100" />
          <el-table-column prop="courseDate" label="上课日期" min-width="120" />
          <el-table-column prop="startTime" label="开始时间" min-width="100" />
          <el-table-column prop="endTime" label="结束时间" min-width="100" />
          <el-table-column prop="tableNumber" label="球台号" min-width="80" />
          <el-table-column prop="fee" label="费用" min-width="80">
            <template slot-scope="{row}">
              ¥{{ row.fee }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template slot-scope="{row}">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" min-width="200">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status === 'PENDING'" type="text" size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog title="课程详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="所属校区">{{ selectedCourse.campusName }}</el-descriptions-item>
        <el-descriptions-item label="教练姓名">{{ selectedCourse.coachName }}</el-descriptions-item>
        <el-descriptions-item label="学员姓名">{{ selectedCourse.studentName }}</el-descriptions-item>
        <el-descriptions-item label="上课日期">{{ selectedCourse.courseDate }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ selectedCourse.startTime }} - {{ selectedCourse.endTime }}</el-descriptions-item>
        <el-descriptions-item label="球台号">{{ selectedCourse.tableNumber }}</el-descriptions-item>
        <el-descriptions-item label="课程费用">¥{{ selectedCourse.fee }}</el-descriptions-item>
        <el-descriptions-item label="课程状态">
          <el-tag :type="getStatusType(selectedCourse.status)">
            {{ getStatusText(selectedCourse.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedCourse.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ selectedCourse.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCourses, adminConfirmCourse, adminCancelCourse } from '@/api/super-admin'
import { getCampuses } from '@/api/super-admin'
import { getAllCoaches } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'CourseManagement',
  components: { Pagination },
  data() {
    return {
      courses: [],
      campuses: [],
      keyword: '',
      campusFilter: '',
      statusFilter: '',
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      selectedCourse: {}
    }
  },
  created() {
    this.getList()
    this.loadCampuses()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const response = await getAllCourses()
        // 转换数据格式以匹配模板需求
        let allCourses = response.data.map(course => {
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
            tableNumber: course.courtNumber,
            createTime: new Date(course.createdAt).toLocaleString('zh-CN')
          }
        })
        
        // 前端筛选逻辑
        if (this.keyword) {
          allCourses = allCourses.filter(course => 
            (course.campusName && course.campusName.includes(this.keyword)) ||
            (course.studentName && course.studentName.includes(this.keyword)) ||
            (course.coachName && course.coachName.includes(this.keyword))
          )
        }
        
        if (this.campusFilter) {
          allCourses = allCourses.filter(course => course.campusId === this.campusFilter)
        }
        
        if (this.statusFilter) {
          allCourses = allCourses.filter(course => course.status === this.statusFilter)
        }
        

        
        this.courses = allCourses
        this.total = this.courses.length
      } catch (error) {
        console.error('获取课程列表失败:', error)
        this.$message.error('获取课程列表失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadCampuses() {
      try {
        const response = await getCampuses()
        this.campuses = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    
    handleViewDetail(row) {
      // 确保详情对话框显示正确的数据格式
      this.selectedCourse = {
        ...row,
        // 这些字段在之前的getList中已经格式化过
        remark: row.cancelReason || '无'
      }
      this.detailDialogVisible = true
    },
    
    async handleConfirm(row) {
      try {
        await this.$confirm('确认该课程？', '提示', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await adminConfirmCourse(row.id)
        this.$message.success('课程已确认')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('确认失败')
        }
      }
    },
    
    async handleCancel(row) {
      try {
        await this.$confirm('取消该课程？', '提示', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await adminCancelCourse(row.id)
        this.$message.success('课程已取消')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('取消失败')
        }
      }
    },
    
    handleExport() {
      // 导出逻辑
      this.$message.info('导出功能开发中...')
    },
    
    getStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'success',
        'CANCELLED': 'danger',
        'COMPLETED': 'info'
      }
      return statusMap[status] || ''
    },
    
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成'
      }
      return statusMap[status] || status
    },
    
    // 确保序号从1开始递增
    indexMethod(index) {
      return (this.listQuery.page - 1) * this.listQuery.limit + index + 1
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-container .el-input,
.filter-container .el-select,
.filter-container .el-date-picker {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>