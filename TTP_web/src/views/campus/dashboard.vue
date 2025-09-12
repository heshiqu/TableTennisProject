<template>
  <div class="dashboard-container">
    <div class="dashboard-text">
      欢迎校区管理员：<span>{{ name }}</span>
    </div>

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
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CountTo from 'vue-count-to'
import { getCampusDashboard, getCampusAnnouncements, createCampusAnnouncement, getCampusPendingItems } from '@/api/campus'

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
  methods: {
    async loadDashboardData() {
      try {
        const [statsRes, announcementsRes, pendingRes] = await Promise.all([
          getCampusDashboard(),
          getCampusAnnouncements(),
          getCampusPendingItems()
        ])
        
        if (statsRes.code === 200) {
          this.campusStats = statsRes.data
        }
        if (announcementsRes.code === 200) {
          this.announcements = announcementsRes.data
        }
        if (pendingRes.code === 200) {
          this.pendingItems = pendingRes.data
        }
      } catch (error) {
        console.error('加载校区仪表板数据失败:', error)
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
}
</style>