<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的教练</span>
      </div>
      
      <div v-if="coaches.length === 0" class="empty-container">
        <el-empty description="您还没有选择教练" />
        <el-button type="primary" @click="goToCoachSearch">去查找教练</el-button>
      </div>
      
      <div v-else class="coach-list">
        <el-row :gutter="20">
          <el-col v-for="coach in coaches" :key="coach.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="coach-card" shadow="hover">
              <div class="coach-info">
                <el-avatar :size="60" :src="getAvatarUrl(coach.avatar)" class="coach-avatar" />
                <div class="coach-details">
                  <h3>{{ coach.realName || coach.username }}</h3>
                  <p class="coach-level">{{ formatCoachLevel(coach.level) }}</p>
                  <p class="coach-contact">
                    <i class="el-icon-phone" /> {{ coach.phone || '暂无电话' }}
                  </p>
                  <p class="coach-contact">
                    <i class="el-icon-message" /> {{ coach.email || '暂无邮箱' }}
                  </p>
                  <el-button type="primary" size="small" @click="viewCoachDetail(coach)">
                    详细信息
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 教练详情对话框 -->
    <el-dialog title="教练详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedCoach" class="coach-detail">
        <div class="detail-header">
          <img :src="getAvatarUrl(selectedCoach.avatar)" :alt="selectedCoach.realName" class="detail-avatar" />
          <div class="detail-info">
            <h3>{{ selectedCoach.realName }}</h3>
            <p><strong>用户名：</strong>{{ selectedCoach.username }}</p>
            <p><strong>级别：</strong>{{ formatCoachLevel(selectedCoach.level) }}</p>
            <p><strong>性别：</strong>{{ selectedCoach.gender === 'MALE' ? '男' : selectedCoach.gender === 'FEMALE' ? '女' : '未知' }}</p>
            <p><strong>年龄：</strong>{{ selectedCoach.age }}岁</p>
            <p><strong>电话：</strong>{{ selectedCoach.phone }}</p>
            <p><strong>邮箱：</strong>{{ selectedCoach.email }}</p>
            <p><strong>收费：</strong>¥{{ selectedCoach.hourlyRate }}/小时</p>
            <p><strong>学员：</strong>{{ selectedCoach.currentStudents }}/{{ selectedCoach.maxStudents }}人</p>
          </div>
        </div>
        <div class="detail-content">
          <h4>个人简介</h4>
          <p>{{ selectedCoach.awards || '暂无个人简介' }}</p>
        </div>
        <div class="detail-actions" style="text-align: center; margin-top: 20px;">
          <el-button type="primary" @click="viewCoachSchedule">
            <i class="el-icon-date"></i> 查看课表
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 教练课表对话框 -->
    <CoachScheduleDialog
      :visible.sync="scheduleDialogVisible"
      :coach-id="selectedCoach ? selectedCoach.id : null"
      :coach-name="selectedCoach ? (selectedCoach.realName || selectedCoach.username) : '教练'"
    />
  </div>
</template>

<script>
import { getStudentApprovedCoaches, getCoachDetail } from '@/api/student'
import { getAvatarUrl } from '@/utils/avatar'
import CoachScheduleDialog from './CoachScheduleDialog.vue'

export default {
  components: {
    CoachScheduleDialog
  },
  name: 'MyCoaches',
  data() {
    return {
      coaches: [],
      selectedCoach: null,
      detailDialogVisible: false,
      scheduleDialogVisible: false,
      loading: false
    }
  },
  created() {
    this.loadMyCoaches()
  },
  computed: {
    formatCoachLevel() {
      return (level) => {
        const levelMap = {
          'SENIOR': '高级',
          'INTERMEDIATE': '中高',
          'JUNIOR': '初级'
        }
        return levelMap[level] || level
      }
    }
  },

  methods: {
    async loadMyCoaches() {
      this.loading = true
      try {
        const userId = this.$store.state.user.user?.id
        if (!userId) {
          this.$message.error('无法获取用户信息')
          return
        }

        // 获取已批准的教练关系
        const relationResponse = await getStudentApprovedCoaches(userId)
        if (relationResponse.code === 200 && relationResponse.data) {
          // 获取每个教练的详细信息
          const coachPromises = relationResponse.data.map(async (relation) => {
            const coachResponse = await getCoachDetail(relation.coachId)
            if (coachResponse.code === 200) {
              return {
                ...coachResponse.data,
                relationId: relation.id,
                approveTime: relation.approveTime,
                applyTime: relation.applyTime
              }
            }
            return null
          })

          const coaches = await Promise.all(coachPromises)
          this.coaches = coaches.filter(coach => coach !== null)
        } else {
          this.coaches = []
        }
      } catch (error) {
        console.error('获取我的教练失败:', error)
        this.$message.error('获取教练信息失败')
        this.coaches = []
      } finally {
        this.loading = false
      }
    },
    getAvatarUrl(avatar) {
      return getAvatarUrl(avatar)
    },
    viewCoachDetail(coach) {
      this.selectedCoach = coach
      this.detailDialogVisible = true
    },
    viewCoachSchedule() {
      this.detailDialogVisible = false
      this.scheduleDialogVisible = true
    },
    goToCoachSearch() {
      this.$router.push('/student/coach-search')
    }
  }
}
</script>

<style scoped>
.empty-container {
  text-align: center;
  padding: 40px 0;
}

.coach-list {
  margin-top: 20px;
}

.coach-card {
  margin-bottom: 20px;
  text-align: center;
}

.coach-avatar img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.coach-info h3 {
  margin: 10px 0 5px;
  color: #303133;
}

.coach-level {
  color: #409EFF;
  font-weight: bold;
}

.coach-price {
  color: #F56C6C;
  font-size: 16px;
  font-weight: bold;
}

.coach-achievement {
  color: #909399;
  font-size: 14px;
  margin: 10px 0;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.coach-actions {
  margin-top: 15px;
}

.coach-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.detail-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  margin-right: 20px;
}

.detail-info h3 {
  margin: 0 0 15px;
  color: #303133;
}

.detail-content h4 {
  margin: 15px 0 10px;
  color: #303133;
}

.detail-content p {
  color: #606266;
  line-height: 1.6;
}

.coach-contact {
  color: #606266;
  font-size: 14px;
  margin: 5px 0;
}

.coach-contact i {
  margin-right: 5px;
  color: #909399;
}
</style>