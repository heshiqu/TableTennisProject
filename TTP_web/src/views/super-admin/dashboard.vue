<template>
  <div class="app-container">
    <div class="dashboard-editor-container">
      <div class="clearfix">
        <pan-thumb :image="avatar" style="float: left">
          {{ name }}
        </pan-thumb>
        <div class="info-container">
          <span class="display_name" style="font-size: 20px; padding-top: 20px; display: inline-block">
            超级管理员工作台
          </span>
          <span style="font-size: 16px; padding-top: 20px; display: inline-block; margin-left: 20px; color: #666">
            欢迎回来，{{ name }}
          </span>
        </div>
      </div>
      
      <div class="data-row" style="margin-top: 20px;">
        <el-row :gutter="32">
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="data-card">
              <div class="data-header">
                <div class="data-title">校区数量</div>
                <div class="data-icon campus-icon">
                  <i class="el-icon-office-building" />
                </div>
              </div>
              <div class="data-content">
                <count-to :start-val="0" :end-val="campusCount" :duration="2600" class="data-number" />
              </div>
              <div class="data-desc">总校区数量</div>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="data-card">
              <div class="data-header">
                <div class="data-title">学员总数</div>
                <div class="data-icon student-icon">
                  <i class="el-icon-user" />
                </div>
              </div>
              <div class="data-content">
                <count-to :start-val="0" :end-val="studentCount" :duration="2600" class="data-number" />
              </div>
              <div class="data-desc">注册学员总数</div>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="data-card">
              <div class="data-header">
                <div class="data-title">教练总数</div>
                <div class="data-icon coach-icon">
                  <i class="el-icon-user-solid" />
                </div>
              </div>
              <div class="data-content">
                <count-to :start-val="0" :end-val="coachCount" :duration="2600" class="data-number" />
              </div>
              <div class="data-desc">注册教练总数</div>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="data-card">
              <div class="data-header">
                <div class="data-title">校区管理员</div>
                <div class="data-icon admin-icon">
                  <i class="el-icon-s-custom" />
                </div>
              </div>
              <div class="data-content">
                <count-to :start-val="0" :end-val="campusAdminCount" :duration="2600" class="data-number" />
              </div>
              <div class="data-desc">校区管理员数量</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-row :gutter="20" style="margin-top: 30px;">
        <el-col :span="12">
          <el-card>
            <div slot="header" class="clearfix">
              <span>系统公告</span>
              <el-button style="float: right; padding: 3px 0" type="text" @click="addNotice">发布公告</el-button>
            </div>
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-time">{{ notice.createTime }}</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <div slot="header" class="clearfix">
              <span>超级管理员快捷操作</span>
            </div>
            <div class="quick-actions">
              <el-button type="primary" icon="el-icon-office-building" @click="goToCampusManage">校区管理</el-button>
              <el-button type="success" icon="el-icon-user-solid" @click="goToCampusAdminManage">校区管理员</el-button>
              <el-button type="warning" icon="el-icon-user" @click="goToAllStudents">所有学员</el-button>
              <el-button type="info" icon="el-icon-user-solid" @click="goToAllCoaches">所有教练</el-button>
              <el-button type="danger" icon="el-icon-key" @click="goToLicenseManage">许可证管理</el-button>
              <el-button type="primary" icon="el-icon-setting" @click="goToSystemManage">系统设置</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 30px;">
        <el-col :span="24">
          <el-card>
            <div slot="header" class="clearfix">
              <span>系统操作日志</span>
              <el-button style="float: right; padding: 3px 0" type="text" @click="viewAllLogs">查看全部</el-button>
            </div>
            <el-table :data="logs" style="width: 100%">
              <el-table-column prop="operator" label="操作人" width="120" />
              <el-table-column prop="action" label="操作内容" />
              <el-table-column prop="target" label="操作对象" />
              <el-table-column prop="createTime" label="操作时间" width="180" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CountTo from 'vue-count-to'
import PanThumb from '@/components/PanThumb'
import { getDashboardData, getNotices, getLogs } from '@/api/admin'
import { getTotalCoachCount } from '@/api/coach'
import { getTotalCampusCount } from '@/api/campus'
import { getTotalStudentCount } from '@/api/user'

export default {
  name: 'SuperAdminDashboard',
  components: {
    CountTo,
    PanThumb
  },
  data() {
    return {
      campusCount: 0,
      studentCount: 0,
      coachCount: 0,
      campusAdminCount: 0,
      notices: [],
      logs: []
    }
  },
  computed: {
    ...mapGetters([
      'name',
      'avatar'
    ])
  },
  created() {
    this.loadDashboardData()
    this.loadNotices()
    this.loadLogs()
  },
  methods: {
    async loadDashboardData() {
      // 获取各项统计数据
      try {
        const count = await getTotalCampusCount()
        this.campusCount = count || 0
      } catch (error) {
        console.error('获取校区总数失败:', error)
        this.campusCount = 0
      }
      
      try {
        const count = await getTotalStudentCount()
        this.studentCount = count || 0
      } catch (error) {
        console.error('获取学员总数失败:', error)
        this.studentCount = 0
      }
      
      try {
        const count = await getTotalCoachCount()
        this.coachCount = count || 0
      } catch (error) {
        console.error('获取教练总数失败:', error)
        this.coachCount = 0
      }
      
      // 获取校区管理员数量（从系统数据获取）
      try {
        const response = await getDashboardData()
        if (response.code === 200) {
          this.campusAdminCount = response.data.campusAdminCount || 0
        }
      } catch (error) {
        console.error('获取校区管理员数量失败:', error)
        this.campusAdminCount = 0
      }
    },
    async loadNotices() {
      try {
        const response = await getNotices({ page: 1, limit: 5 })
        if (response.code === 200) {
          this.notices = response.data.records || []
        }
      } catch (error) {
        console.error('加载公告失败:', error)
      }
    },
    async loadLogs() {
      try {
        const response = await getLogs({ page: 1, limit: 10 })
        if (response.code === 200) {
          this.logs = response.data.records || []
        }
      } catch (error) {
        console.error('加载日志失败:', error)
      }
    },
    addNotice() {
      this.$router.push('/admin/notice/add')
    },
    goToCampusManage() {
      this.$router.push('/campus-manage')
    },
    goToCampusAdminManage() {
      this.$router.push('/campus-admin-manage')
    },
    goToAllStudents() {
      this.$router.push('/admin/students')
    },
    goToAllCoaches() {
      this.$router.push('/admin/coaches')
    },
    goToLicenseManage() {
      this.$router.push('/license-manage')
    },
    goToSystemManage() {
      this.$router.push('/admin/settings')
    },
    viewAllLogs() {
      this.$router.push('/system-logs')
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .data-card {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .data-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .data-title {
        font-size: 14px;
        color: #666;
      }
      
      .data-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        
        &.campus-icon {
          background: #409EFF;
        }
        
        &.student-icon {
          background: #67C23A;
        }
        
        &.coach-icon {
          background: #E6A23C;
        }
        
        &.admin-icon {
          background: #722ed1;
        }
      }
    }
    
    .data-content {
      margin-top: 10px;
      
      .data-number {
        font-size: 28px;
        font-weight: bold;
        color: #333;
      }
    }
    
    .data-desc {
      margin-top: 5px;
      font-size: 12px;
      color: #999;
    }
  }

  .notice-item {
    padding: 10px 0;
    border-bottom: 1px solid #eee;
    
    &:last-child {
      border-bottom: none;
    }
    
    .notice-title {
      font-size: 14px;
      color: #333;
      cursor: pointer;
      
      &:hover {
        color: #409EFF;
      }
    }
    
    .notice-time {
      font-size: 12px;
      color: #999;
      margin-top: 5px;
    }
  }

  .quick-actions {
    .el-button {
      margin: 5px;
    }
  }
}
</style>