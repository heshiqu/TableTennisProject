<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>查找教练</span>
      </div>
      
      <!-- 搜索表单 -->
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="姓名">
              <el-input v-model="searchForm.name" placeholder="请输入教练姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性别">
              <el-select v-model="searchForm.gender" placeholder="请选择性别" clearable>
                <el-option label="男" value="MALE"></el-option>
                <el-option label="女" value="FEMALE"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年龄">
              <el-input-number v-model="searchForm.age" :min="18" :max="70" clearable></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="等级">
              <el-select v-model="searchForm.level" placeholder="请选择等级" clearable>
                <el-option label="高级教练" value="SENIOR"></el-option>
                <el-option label="中级教练" value="INTERMEDIATE"></el-option>
                <el-option label="初级教练" value="JUNIOR"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="info" @click="handleBrowseAll">浏览全部</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 教练列表 -->
    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>教练列表</span>
        <el-button style="float: right;" type="text" @click="handleRefresh">刷新</el-button>
      </div>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="coach in coachList" :key="coach.id" style="margin-bottom: 20px;">
          <el-card :body-style="{ padding: '20px' }" class="coach-card">
            <div class="coach-header">
              <el-avatar :size="80" :src="getAvatarUrl(coach.avatar)" class="coach-avatar"></el-avatar>
              <div class="coach-info">
                <h4>{{ coach.realName }}</h4>
                <p>{{ coach.level | levelFilter }}</p>
                <p>{{ coach.gender | genderFilter }} | {{ coach.age }}岁</p>
              </div>
            </div>
            
            <div class="coach-stats">
              <div class="stat-item">
                <span class="stat-value">{{ coach.price || 0 }}</span>
                <span class="stat-label">元/小时</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ coach.studentCount || 0 }}</span>
                <span class="stat-label">学员</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ coach.rating || 0 }}</span>
                <span class="stat-label">评分</span>
              </div>
            </div>

            <div class="coach-description">
              <p>{{ coach.achievements || '暂无获奖经历' }}</p>
            </div>

            <div class="coach-actions">
              <el-button type="primary" size="small" @click="handleSelectCoach(coach)" 
                         :disabled="coach.studentCount >= 20 || selectedCoaches.length >= 2">
                选中教练
              </el-button>
              <el-button size="small" @click="handleViewDetail(coach)">查看详情</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="coachList.length === 0 && !loading" description="暂无教练信息"></el-empty>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[6, 12, 24]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="text-align: center; margin-top: 20px;">
      </el-pagination>
    </el-card>

    <!-- 教练详情对话框 -->
    <el-dialog title="教练详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedCoach">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-avatar :size="120" :src="getAvatarUrl(selectedCoach.avatar)"></el-avatar>
          </el-col>
          <el-col :span="16">
            <h3>{{ selectedCoach.realName }}</h3>
            <p><strong>等级：</strong>{{ selectedCoach.level | levelFilter }}</p>
            <p><strong>性别：</strong>{{ selectedCoach.gender | genderFilter }}</p>
            <p><strong>年龄：</strong>{{ selectedCoach.age }}岁</p>
            <p><strong>电话：</strong>{{ selectedCoach.phone }}</p>
            <p><strong>邮箱：</strong>{{ selectedCoach.email }}</p>
            <p><strong>收费标准：</strong>{{ selectedCoach.price }}元/小时</p>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <div>
          <h4>获奖经历</h4>
          <p>{{ selectedCoach.achievements || '暂无获奖经历' }}</p>
        </div>
        
        <div style="margin-top: 20px;">
          <h4>学员评价</h4>
          <el-rate
            v-model="selectedCoach.rating"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}">
          </el-rate>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSelectCoach(selectedCoach)" 
                   :disabled="selectedCoach.studentCount >= 20 || selectedCoaches.length >= 2">
          选中教练
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCoaches, selectCoach } from '@/api/coach'
import { getAvatarUrl } from '@/utils/avatar'
import { getMyCoaches } from '@/api/student'

export default {
  name: 'CoachSearch',
  data() {
    return {
      searchForm: {
        name: '',
        gender: '',
        age: null,
        level: ''
      },
      coachList: [],
      selectedCoach: null,
      selectedCoaches: [],
      loading: false,
      detailDialogVisible: false,
      page: {
        current: 1,
        size: 6
      },
      total: 0,
      defaultAvatar: '/uploads/avatars/default-avatar.png'
    }
  },
  filters: {
    levelFilter(level) {
      const levelMap = {
        'SENIOR': '高级教练',
        'INTERMEDIATE': '中级教练',
        'JUNIOR': '初级教练'
      }
      return levelMap[level] || level
    },
    genderFilter(gender) {
      return gender === 'MALE' ? '男' : '女'
    }
  },
  created() {
    this.loadMyCoaches()
    this.handleBrowseAll()
  },
  methods: {
    getAvatarUrl,
    async loadMyCoaches() {
      try {
        const response = await getMyCoaches()
        this.selectedCoaches = response.data || []
      } catch (error) {
        console.error('获取已选教练失败:', error)
      }
    },
    async loadCoaches() {
      this.loading = true
      try {
        const params = {
          ...this.searchForm,
          current: this.page.current,
          size: this.page.size
        }
        const response = await getAllCoaches(params)
        this.coachList = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        this.$message.error('获取教练列表失败')
        console.error('获取教练列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    async handleSearch() {
      this.page.current = 1
      await this.loadCoaches()
    },
    async handleBrowseAll() {
      this.searchForm = {
        name: '',
        gender: '',
        age: null,
        level: ''
      }
      await this.loadCoaches()
    },
    handleReset() {
      this.searchForm = {
        name: '',
        gender: '',
        age: null,
        level: ''
      }
    },
    handleRefresh() {
      this.loadCoaches()
    },
    handleViewDetail(coach) {
      this.selectedCoach = coach
      this.detailDialogVisible = true
    },
    async handleSelectCoach(coach) {
      if (this.selectedCoaches.length >= 2) {
        this.$message.warning('最多只能选择两位教练')
        return
      }
      
      if (coach.studentCount >= 20) {
        this.$message.warning('该教练学员已满')
        return
      }
      
      try {
        await this.$confirm(`确定选择 ${coach.realName} 作为您的教练吗？`, '确认选择', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await selectCoach(coach.id)
        this.$message.success('选择成功，请等待教练确认')
        this.loadCoaches()
        this.loadMyCoaches()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('选择失败')
          console.error('选择教练失败:', error)
        }
      }
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadCoaches()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadCoaches()
    }
  }
}
</script>

<style scoped>
.search-form {
  padding: 20px 0;
}

.coach-card {
  transition: all 0.3s;
}

.coach-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.coach-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.coach-avatar {
  margin-right: 15px;
}

.coach-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.coach-info p {
  margin: 3px 0;
  color: #666;
  font-size: 14px;
}

.coach-stats {
  display: flex;
  justify-content: space-around;
  margin: 15px 0;
  padding: 15px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.coach-description {
  margin: 15px 0;
  min-height: 60px;
}

.coach-description p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.coach-actions {
  display: flex;
  justify-content: space-between;
}

@media screen and (max-width: 768px) {
  .el-col-8 {
    width: 100%;
  }
}
</style>