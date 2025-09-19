<template>
  <div class="dashboard-container">
    <div class="dashboard-text">
      欢迎校区管理员：<span>{{ name }}</span>
    </div>

    <!-- 校区信息展示 - 移至最顶部 -->
    <el-card v-if="campusInfo" class="campus-info-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <i class="el-icon-office-building"></i>
        <span>校区信息</span>
      </div>
      <div class="campus-info-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>校区名称：</label>
              <span>{{ campusInfo.name }}</span>
            </div>
            <div class="info-item">
              <label>校区地址：</label>
              <span>{{ campusInfo.address }}</span>
            </div>
            <div class="info-item">
              <label>联系人：</label>
              <span>{{ campusInfo.contactPerson }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>联系电话：</label>
              <span>{{ campusInfo.contactPhone }}</span>
            </div>
            <div class="info-item">
              <label>邮箱：</label>
              <span>{{ campusInfo.email }}</span>
            </div>
            <div v-if="campusInfo.parentName" class="info-item">
              <label>所属校区：</label>
              <span>{{ campusInfo.parentName }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>校区统计</span>
          </div>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper icon-people">
              <svg-icon icon-class="peoples" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">校区学员</div>
              <count-to
                :start-val="0"
                :end-val="campusStats.studentCount"
                :duration="2600"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>教练统计</span>
          </div>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper icon-message">
              <svg-icon icon-class="peoples" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">校区教练</div>
              <count-to
                :start-val="0"
                :end-val="campusStats.coachCount"
                :duration="3000"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>今日课程</span>
          </div>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper icon-money">
              <svg-icon icon-class="education" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">今日课程</div>
              <count-to
                :start-val="0"
                :end-val="campusStats.todayCourseCount"
                :duration="3200"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>收入统计</span>
          </div>
          <div class="card-panel">
            <div class="card-panel-icon-wrapper icon-shopping">
              <svg-icon icon-class="money" class-name="card-panel-icon" />
            </div>
            <div class="card-panel-description">
              <div class="card-panel-text">本月收入</div>
              <count-to
                :start-val="0"
                :end-val="campusStats.monthlyIncome"
                :duration="3600"
                prefix="¥"
                class="card-panel-num"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>校区公告</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="showAddAnnouncement = true">
              发布公告
            </el-button>
          </div>
          <el-table :data="announcements" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="createTime" label="发布时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>待处理事项</span>
          </div>
          <el-table :data="pendingItems" style="width: 100%">
            <el-table-column prop="type" label="类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.type | typeFilter">
                  {{ scope.row.type | typeNameFilter }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="createTime" label="时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.createTime | parseTime('{y}-{m}-{d} {h}:{i}') }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handlePendingItem(scope.row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发布公告对话框 -->
    <el-dialog title="发布公告" :visible.sync="showAddAnnouncement" width="500px">
      <el-form ref="announcementForm" :model="announcementForm" :rules="announcementRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="announcementForm.content" type="textarea" :rows="4" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAddAnnouncement = false">取消</el-button>
        <el-button type="primary" @click="addAnnouncement">发布</el-button>
      </div>
    </el-dialog>

    <!-- 快捷导航按钮 -->
    <el-row :gutter="20" style="margin-top: 30px;">
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>快捷导航</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-button type="primary" icon="el-icon-user" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-students')">
                学员管理
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="success" icon="el-icon-s-custom" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-coaches')">
                教练管理
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="warning" icon="el-icon-date" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-courses')">
                课程管理
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="info" icon="el-icon-s-order" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-appointments')">
                预约管理
              </el-button>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-button type="danger" icon="el-icon-trophy" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-tournaments')">
                赛事管理
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="primary" icon="el-icon-money" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-finance')">
                财务管理
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="success" icon="el-icon-s-home" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-info')">
                校区信息
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="warning" icon="el-icon-s-operation" style="width: 100%; margin-bottom: 10px;" @click="$router.push('/campus-admin-system-logs')">
                系统日志
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CountTo from 'vue-count-to'
import { 
  getCampusDashboard, 
  getCampusAnnouncements, 
  getCampusPendingItems, 
  createCampusAnnouncement,
  getCampusStudentCount,
  getCampusDetail
} from '@/api/campus'
import { getCoachCountByCampus } from '@/api/coach'
import { getCampusTodayConfirmedCount } from '@/api/course'

export default {
  name: 'CampusDashboard',
  components: {
    CountTo
  },
  data() {
    return {
      campusStats: {
        studentCount: 0,
        coachCount: 0,
        todayCourseCount: 0,
        monthlyIncome: 0
      },
      campusInfo: null,
      announcements: [],
      pendingItems: [],
      showAddAnnouncement: false,
      announcementForm: {
        title: '',
        content: ''
      },
      announcementRules: {
        title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['name', 'roles'])
  },
  created() {
    this.loadDashboardData()
  },
  
  mounted() {
    // 确保用户信息已加载
    if (!this.$store.state.user.user) {
      console.log('用户信息未加载，尝试重新获取...')
      this.$store.dispatch('user/getInfo').then(() => {
        console.log('用户信息已重新获取:', this.$store.state.user.user)
        this.loadDashboardData()
      }).catch(error => {
        console.error('重新获取用户信息失败:', error)
      })
    }
  },
  methods: {
    async loadDashboardData() {
      try {
        console.log('开始加载校区仪表板数据...')
        
        // 从vuex获取当前用户的校区ID
        const currentUser = this.$store.state.user.user
        console.log('当前用户信息:', currentUser)
        
        if (!currentUser) {
          console.warn('用户未登录或用户信息为空')
          this.campusStats.studentCount = 0
          return
        }
        
        if (!currentUser.campusId) {
          console.error('用户缺少campusId字段，用户对象包含:', Object.keys(currentUser))
          this.campusStats.studentCount = 0
          return
        }
        
        const campusId = currentUser.campusId
        console.log('使用用户校区ID:', campusId)
        
        // 获取校区学员数量
        try {
          console.log('开始获取校区学员数量，校区ID:', campusId)
          const studentCountRes = await getCampusStudentCount(campusId)
          console.log('获取校区学员数量响应:', studentCountRes)
          if (studentCountRes && studentCountRes.code === 200) {
            this.campusStats.studentCount = studentCountRes.data || 0
            console.log('设置学员数量为:', this.campusStats.studentCount)
          }
        } catch (error) {
          console.warn('获取校区学员数量失败:', error)
          // 使用默认值
          this.campusStats.studentCount = 0
        }

        // 获取校区教练数量
        try {
          console.log('开始获取校区教练数量，校区ID:', campusId)
          const coachCountRes = await getCoachCountByCampus(campusId)
          console.log('获取校区教练数量响应:', coachCountRes)
          if (coachCountRes && coachCountRes.code === 200) {
            this.campusStats.coachCount = coachCountRes.data || 0
            console.log('设置教练数量为:', this.campusStats.coachCount)
          }
        } catch (error) {
          console.warn('获取校区教练数量失败:', error)
          // 使用默认值
          this.campusStats.coachCount = 0
        }

        // 获取校区今日已确认课程数量
        try {
          console.log('开始获取校区今日已确认课程数量，校区ID:', campusId)
          const todayCourseCountRes = await getCampusTodayConfirmedCount(campusId)
          console.log('获取校区今日课程数量响应:', todayCourseCountRes)
          if (todayCourseCountRes && todayCourseCountRes.code === 200) {
            this.campusStats.todayCourseCount = todayCourseCountRes.data || 0
            console.log('设置今日课程数量为:', this.campusStats.todayCourseCount)
          }
        } catch (error) {
          console.warn('获取校区今日课程数量失败:', error)
          // 使用默认值
          this.campusStats.todayCourseCount = 0
        }
        
        // 获取校区详细信息
        try {
          console.log('开始获取校区详细信息，校区ID:', campusId)
          const campusDetailRes = await getCampusDetail(campusId)
          console.log('获取校区详细信息响应:', campusDetailRes)
          if (campusDetailRes && campusDetailRes.code === 200) {
            this.campusInfo = campusDetailRes.data
            console.log('设置校区信息:', this.campusInfo)
          }
        } catch (error) {
          console.warn('获取校区详细信息失败:', error)
          this.campusInfo = null
        }

        // 获取其他统计数据
        try {
          const [statsRes, announcementsRes, pendingRes] = await Promise.all([
            getCampusDashboard(),
            getCampusAnnouncements(),
            getCampusPendingItems()
          ])
          
          if (statsRes && statsRes.code === 200) {
            this.campusStats = {
              ...this.campusStats,
              ...statsRes.data
            }
          }
          if (announcementsRes && announcementsRes.code === 200) {
            this.announcements = announcementsRes.data || []
          }
          if (pendingRes && pendingRes.code === 200) {
            this.pendingItems = pendingRes.data || []
          }
        } catch (error) {
          console.warn('获取校区统计数据失败:', error)
        }
      } catch (error) {
        console.error('加载校区仪表板数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      }
    },
    async addAnnouncement() {
      this.$refs.announcementForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await createCampusAnnouncement(this.announcementForm)
            if (response.code === 200) {
              this.$message.success('公告发布成功')
              this.showAddAnnouncement = false
              this.announcementForm = { title: '', content: '' }
              this.loadDashboardData()
            }
          } catch (error) {
            console.error('发布公告失败:', error)
          }
        }
      })
    },
    handlePendingItem(item) {
      // 根据待处理事项类型跳转到相应页面
      switch (item.type) {
        case 'coach_audit':
          this.$router.push('/campus/coaches')
          break
        case 'course_confirm':
          this.$router.push('/campus/courses')
          break
        case 'refund_request':
          this.$router.push('/campus/payments')
          break
        default:
          this.$message.info('暂无处理页面')
      }
    }
  },
  filters: {
    typeFilter(type) {
      const typeMap = {
        'coach_audit': 'warning',
        'course_confirm': 'primary',
        'refund_request': 'danger'
      }
      return typeMap[type] || 'info'
    },
    typeNameFilter(type) {
      const nameMap = {
        'coach_audit': '教练审核',
        'course_confirm': '课程确认',
        'refund_request': '退款申请'
      }
      return nameMap[type] || '其他'
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .dashboard-text {
    font-size: 20px;
    line-height: 28px;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 16px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, 0.05);
    border-color: rgba(0, 0, 0, 0.05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }
      .icon-people {
        background: #40c9c6;
      }
      .icon-message {
        background: #36a3f7;
      }
      .icon-money {
        background: #f4516c;
      }
      .icon-shopping {
        background: #34bfa3;
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3;
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }

  .box-card {
    margin-bottom: 20px;
  }

  .campus-info-card {
    margin-top: 30px;
  }

  .campus-info-content {
    padding: 10px 0;
  }

  .info-item {
    margin-bottom: 15px;
    line-height: 1.6;
  }

  .info-item label {
    font-weight: bold;
    color: #606266;
    display: inline-block;
    width: 80px;
    margin-right: 10px;
  }

  .info-item span {
    color: #303133;
  }
}

@media (max-width: 550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }

  .info-item label {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style>