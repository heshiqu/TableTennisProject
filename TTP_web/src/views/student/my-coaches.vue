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
              <div class="coach-avatar">
                <img :src="coach.avatar || defaultAvatar" :alt="coach.realName" />
              </div>
              <div class="coach-info">
                <h3>{{ coach.realName }}</h3>
                <p class="coach-level">{{ getCoachLevelText(coach.level) }}</p>
                <p class="coach-price">¥{{ coach.price }}/小时</p>
                <p class="coach-achievement">{{ coach.achievement || '暂无介绍' }}</p>
              </div>
              <div class="coach-actions">
                <el-button size="small" type="primary" @click="viewCoachDetail(coach)">查看详情</el-button>
                <el-button size="small" type="danger" @click="removeCoach(coach)">移除教练</el-button>
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
          <img :src="selectedCoach.avatar || defaultAvatar" :alt="selectedCoach.realName" class="detail-avatar" />
          <div class="detail-info">
            <h3>{{ selectedCoach.realName }}</h3>
            <p><strong>级别：</strong>{{ getCoachLevelText(selectedCoach.level) }}</p>
            <p><strong>收费：</strong>¥{{ selectedCoach.price }}/小时</p>
            <p><strong>联系方式：</strong>{{ selectedCoach.phone }}</p>
            <p><strong>邮箱：</strong>{{ selectedCoach.email }}</p>
          </div>
        </div>
        <div class="detail-content">
          <h4>个人简介</h4>
          <p>{{ selectedCoach.description || '暂无个人简介' }}</p>
          <h4>获奖经历</h4>
          <p>{{ selectedCoach.achievement || '暂无获奖经历' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyCoaches, removeCoach } from '@/api/student'
import defaultAvatar from '@/assets/default-avatar.png'

export default {
  name: 'MyCoaches',
  data() {
    return {
      coaches: [],
      selectedCoach: null,
      detailDialogVisible: false,
      defaultAvatar,
      loading: false
    }
  },
  created() {
    this.loadMyCoaches()
  },
  methods: {
    async loadMyCoaches() {
      this.loading = true
      try {
        const response = await getMyCoaches()
        this.coaches = response.data || []
      } catch (error) {
        console.error('获取我的教练失败:', error)
        this.$message.error('获取教练信息失败')
      } finally {
        this.loading = false
      }
    },
    getCoachLevelText(level) {
      const levelMap = {
        'SENIOR': '高级教练',
        'MIDDLE': '中级教练',
        'JUNIOR': '初级教练'
      }
      return levelMap[level] || level
    },
    viewCoachDetail(coach) {
      this.selectedCoach = coach
      this.detailDialogVisible = true
    },
    async removeCoach(coach) {
      try {
        await this.$confirm(`确定要移除教练 ${coach.realName} 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await removeCoach(coach.id)
        this.$message.success('移除成功')
        this.loadMyCoaches()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除教练失败:', error)
          this.$message.error('移除失败')
        }
      }
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
</style>