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
        <el-col v-for="coach in displayCoachList" :key="coach.id" :xs="24" :sm="12" :md="8">
          <el-card class="coach-card" shadow="hover">
            <div class="coach-header">
              <el-avatar class="coach-avatar" :size="60" :src="getAvatarUrl(coach.avatar)"></el-avatar>
              <div class="coach-info">
                <h4>{{ coach.realName || '未知' }}</h4>
                <p>{{ coach.level | levelFilter }}</p>
                <p>{{ coach.gender | genderFilter }} · {{ coach.age || 0 }}岁</p>
              </div>
            </div>
            
            <div class="coach-stats">
              <div class="stat-item">
                <span class="stat-value">¥{{ coach.hourlyRate || 0 }}</span>
                <span class="stat-label">每小时</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ coach.currentStudents || 0 }}/{{ coach.maxStudents || 20 }}</span>
                <span class="stat-label">学员</span>
              </div>
            </div>
            
            <div class="coach-description">
              <p><strong>获奖：</strong>{{ coach.awards || '暂无获奖记录' }}</p>
              <p><strong>邮箱：</strong>{{ coach.email || '未设置' }}</p>
            </div>
            
            <div class="coach-actions">
              <el-button type="text" @click="handleViewDetail(coach)">查看详情</el-button>
              <el-button type="primary" size="small" 
                         :disabled="(coach.currentStudents || 0) >= (coach.maxStudents || 20) || (selectedCoaches || []).length >= 2"
                         @click="handleSelectCoach(coach)">
                选择教练
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="displayCoachList.length === 0 && !loading" description="暂无教练信息"></el-empty>

      <!-- 分页 - 移除，因为新API返回完整列表 -->
      <!-- <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[6, 12, 24]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="text-align: center; margin-top: 20px;">
      </el-pagination> -->
    </el-card>

    <!-- 教练详情对话框 -->
    <el-dialog title="教练详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedCoach">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-avatar :size="120" :src="getAvatarUrl(selectedCoach.avatar)"></el-avatar>
          </el-col>
          <el-col :span="16">
            <h3>{{ selectedCoach.realName || '未知' }}</h3>
            <p><strong>用户名：</strong>{{ selectedCoach.username || '未设置' }}</p>
            <p><strong>等级：</strong>{{ selectedCoach.level | levelFilter }}</p>
            <p><strong>性别：</strong>{{ selectedCoach.gender | genderFilter }}</p>
            <p><strong>年龄：</strong>{{ selectedCoach.age || 0 }}岁</p>
            <p><strong>电话：</strong>{{ selectedCoach.phone || '未设置' }}</p>
            <p><strong>邮箱：</strong>{{ selectedCoach.email || '未设置' }}</p>
            <p><strong>收费：</strong>¥{{ selectedCoach.hourlyRate || 0 }}/小时</p>
            <p><strong>学员：</strong>{{ selectedCoach.currentStudents || 0 }}/{{ selectedCoach.maxStudents || 20 }}人</p>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <el-row>
          <el-col :span="24">
            <h4>获奖经历</h4>
            <p>{{ selectedCoach.awards || '暂无获奖经历' }}</p>
          </el-col>
        </el-row>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSelectCoach(selectedCoach)" 
                   :disabled="(selectedCoach.currentStudents || 0) >= (selectedCoach.maxStudents || 20) || (selectedCoaches || []).length >= 2">
          选择教练
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAvatarUrl } from '@/utils/avatar'
import { getMyCoaches, getCampusCoachesForStudent, selectCoach } from '@/api/student'

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
      defaultAvatar: (process.env.VUE_APP_BASE_API || 'http://localhost:8080') + '/uploads/avatars/default-avatar.jpg'
    }
  },
  computed: {
    displayCoachList() {
      let filtered = this.coachList;
      
      // 按姓名过滤
      if (this.searchForm.name) {
        filtered = filtered.filter(coach => 
          coach.realName && coach.realName.toLowerCase().includes(this.searchForm.name.toLowerCase())
        );
      }
      
      // 按性别过滤
      if (this.searchForm.gender) {
        filtered = filtered.filter(coach => coach.gender === this.searchForm.gender);
      }
      
      // 按年龄过滤
      if (this.searchForm.age) {
        filtered = filtered.filter(coach => coach.age === this.searchForm.age);
      }
      
      // 按等级过滤
      if (this.searchForm.level) {
        filtered = filtered.filter(coach => coach.level === this.searchForm.level);
      }
      
      return filtered;
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
        this.selectedCoaches = response?.data || []
      } catch (error) {
        console.error('获取已选教练失败:', error)
        this.selectedCoaches = []
      }
    },
    async loadCoaches() {
      this.loading = true
      try {
        // 从Vuex获取当前用户的校区ID
        const campusId = this.$store.state.user.user?.campusId
        console.log('当前用户校区ID:', campusId)
        
        if (!campusId) {
          this.$message.error('无法获取用户校区信息')
          this.coachList = []
          this.total = 0
          return
        }

        // 使用具体的校区ID获取教练列表
        const response = await getCampusCoachesForStudent(campusId)
        console.log('教练列表原始响应:', response)
        
        if (response && response.code === 200) {
          if (!response.data) {
            console.warn('响应中没有data字段')
            this.coachList = []
            this.total = 0
            return
          }
          
          // 使用返回的教练数据，添加默认值处理
          this.coachList = response.data.map(coach => ({
            id: coach.id,
            username: coach.username || '',
            realName: coach.realName || '未知',
            gender: coach.gender || 'MALE',
            age: coach.age || 0,
            phone: coach.phone || '未设置',
            email: coach.email || '未设置',
            avatar: coach.avatar,
            level: coach.level || 'JUNIOR',
            awards: coach.awards || '暂无获奖记录',
            hourlyRate: coach.hourlyRate || 0,
            currentStudents: coach.currentStudents || 0,
            maxStudents: coach.maxStudents || 20
          }))
          
          this.total = this.coachList.length
          console.log('最终加载的教练数量:', this.coachList.length)
        } else {
          console.warn('响应状态码不正确:', response?.code)
          this.coachList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
        this.$message.error('获取教练列表失败: ' + error.message)
        this.coachList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    async handleSearch() {
      // 搜索功能现在使用前端过滤
      this.page.current = 1
      console.log('执行搜索，搜索条件:', this.searchForm)
      console.log('搜索结果数量:', this.displayCoachList.length)
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
      if ((this.selectedCoaches || []).length >= 2) {
        this.$message.warning('最多只能选择两位教练')
        return
      }
      
      if ((coach.currentStudents || 0) >= (coach.maxStudents || 20)) {
        this.$message.warning('该教练学员已满')
        return
      }
      
      try {
        await this.$confirm(`确定选择 ${coach.realName || '未知'} 作为您的教练吗？`, '确认选择', {
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