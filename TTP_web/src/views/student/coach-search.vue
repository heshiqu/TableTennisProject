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
              <el-button @click="handleReset">重置</el-button>
              <el-button type="info" @click="handleBrowseAll">浏览全部</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 已选择教练提示 -->
    <el-alert
      v-if="Array.isArray(selectedCoaches) && selectedCoaches.length > 0"
      :title="`您已选择 ${selectedCoaches.length} 位教练：${selectedCoaches.filter(c => c).map(c => c.realName || c.coachName || '未知').join('、')}${selectedCoaches.length >= 2 ? '（已达上限）' : ''}`"
      :type="selectedCoaches.length >= 2 ? 'warning' : 'success'"
      :closable="false"
      style="margin-top: 20px;"
      show-icon>
    </el-alert>

    <!-- 教练列表 -->
    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>教练列表</span>
        <el-button style="float: right;" type="text" @click="handleRefresh">刷新</el-button>
      </div>
      

      
      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="coach in displayCoachList" :key="coach.id" :xs="24" :sm="12" :md="8">
          <el-card class="coach-card" shadow="hover" :class="{ 'selected-coach': isCoachSelected(coach) }">
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
              <el-button 
                type="primary" 
                size="small"
                :disabled="isCoachSelected(coach) || ((coach && coach.currentStudents) || 0) >= ((coach && coach.maxStudents) || 20) || ((selectedCoaches && selectedCoaches.length) || 0) >= 2"
                @click="handleSelectCoach(coach)">
                {{ isCoachSelected(coach) ? '已选择' : '选择教练' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <div v-if="displayCoachList.length === 0 && !loading" style="text-align: center; padding: 40px;">
        <i class="el-icon-search" style="font-size: 48px; color: #909399; margin-bottom: 10px;"></i>
        <p style="color: #909399; font-size: 14px;">暂无教练信息</p>
      </div>

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
                   :disabled="isCoachSelected(selectedCoach) || ((selectedCoach && selectedCoach.currentStudents) || 0) >= ((selectedCoach && selectedCoach.maxStudents) || 20) || ((selectedCoaches && selectedCoaches.length) || 0) >= 2">
          {{ isCoachSelected(selectedCoach) ? '已选择' : '选择教练' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAvatarUrl } from '@/utils/avatar'
import { getStudentApprovedCoaches, getCampusCoachesForStudent, selectCoach } from '@/api/student'

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
      
      console.log('=== 筛选开始 ===');
      console.log('原始教练列表:', this.coachList);
      console.log('原始列表长度:', this.coachList.length);
      console.log('搜索条件:', this.searchForm);
      
      // 严格检查搜索条件是否为空（包括null、undefined、空字符串、0等）
        const hasName = this.searchForm.name && this.searchForm.name.trim() !== '';
        const hasGender = this.searchForm.gender && this.searchForm.gender !== '';
        // 特别注意：el-input-number会将null自动设为min值18，所以必须额外检查
        const hasAge = this.searchForm.age !== null && this.searchForm.age !== undefined && this.searchForm.age !== '' && this.searchForm.age !== 0 && this.searchForm.age !== 18;
        const hasLevel = this.searchForm.level && this.searchForm.level !== '';
      
      console.log('hasName:', hasName, 'hasGender:', hasGender, 'hasAge:', hasAge, 'hasLevel:', hasLevel);
      console.log('age值:', this.searchForm.age, '类型:', typeof this.searchForm.age);
      
      // 只有当用户主动输入了搜索条件时才进行筛选
      const hasActiveFilter = hasName || hasGender || hasAge || hasLevel;
      
      if (hasActiveFilter) {
        console.log('用户输入了搜索条件，开始筛选...');
        
        // 按姓名筛选
        if (hasName) {
          console.log('按姓名筛选:', this.searchForm.name);
          filtered = filtered.filter(coach => {
            const match = coach.realName && coach.realName.toLowerCase().includes(this.searchForm.name.toLowerCase());
            console.log('教练', coach.realName, '匹配姓名:', match);
            return match;
          });
          console.log('姓名筛选后数量:', filtered.length);
        }
        
        // 按性别筛选
        if (hasGender) {
          console.log('按性别筛选:', this.searchForm.gender);
          filtered = filtered.filter(coach => {
            const match = coach.gender === this.searchForm.gender;
            console.log('教练', coach.realName, '性别:', coach.gender, '匹配性别:', match);
            return match;
          });
          console.log('性别筛选后数量:', filtered.length);
        }
        
        // 按年龄筛选 - 严格匹配
        if (hasAge) {
          console.log('按年龄筛选:', this.searchForm.age);
          const searchAge = parseInt(this.searchForm.age);
          filtered = filtered.filter(coach => {
            const match = coach.age === searchAge;
            console.log('教练', coach.realName, '年龄:', coach.age, '搜索年龄:', searchAge, '匹配年龄:', match);
            return match;
          });
          console.log('年龄筛选后数量:', filtered.length);
        }
        
        // 按等级筛选
        if (hasLevel) {
          console.log('按等级筛选:', this.searchForm.level);
          filtered = filtered.filter(coach => {
            const match = coach.level === this.searchForm.level;
            console.log('教练', coach.realName, '等级:', coach.level, '匹配等级:', match);
            return match;
          });
          console.log('等级筛选后数量:', filtered.length);
        }
      } else {
        console.log('无用户搜索条件，返回全部教练');
      }
      
      console.log('=== 筛选结束 ===');
      console.log('最终筛选结果:', filtered);
      console.log('最终数量:', filtered.length);
      
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
    this.handleBrowseAll()
    this.loadMyCoaches()
  },
  methods: {
    getAvatarUrl,
    async loadMyCoaches() {
      try {
        // 从Vuex获取当前用户ID
        const studentId = this.$store.state.user?.user?.id
        if (!studentId) {
          console.warn('无法获取用户ID')
          this.selectedCoaches = []
          return
        }
        
        const response = await getStudentApprovedCoaches(studentId)
        console.log('获取已选择教练响应:', response)
        
        if (response && response.code === 200) {
          const coachData = Array.isArray(response.data) ? response.data : []
          
          // 处理关系数据结构：{id, coachId, coachName, studentId, studentName, status, ...}
          this.selectedCoaches = coachData
            .filter(item => item && item.coachId)
            .map(item => ({
              id: item.coachId,  // 使用coachId作为教练ID
              username: '',      // 从关系数据中没有用户名
              realName: item.coachName || '未知',
              gender: 'MALE',    // 默认性别
              age: 0,            // 默认年龄
              phone: '未设置',
              email: '未设置',
              avatar: null,
              level: 'JUNIOR',   // 默认等级
              awards: '暂无获奖记录',
              hourlyRate: 0,
              currentStudents: 0,
              maxStudents: 20
            }))
          
          console.log('处理后的已选择教练:', this.selectedCoaches)
        } else {
          this.selectedCoaches = []
        }
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
          console.log('后端返回的原始数据:', response.data)
          
          // 检查数据结构
          const coachData = Array.isArray(response.data) ? response.data : response.data.content || response.data.items || []
          console.log('提取的教练数组:', coachData)
          
          // 强制使用新数组确保响应式更新
          const newCoachList = coachData.map(coach => ({
            id: coach.id || coach.userId,
            username: coach.username || coach.userName || '',
            realName: coach.realName || coach.name || coach.username || '未知',
            gender: coach.gender || 'MALE',
            age: coach.age || 0,
            phone: coach.phone || '未设置',
            email: coach.email || '未设置',
            avatar: coach.avatar,
            level: coach.level || 'JUNIOR',
            awards: coach.awards || '暂无获奖记录',
            hourlyRate: coach.hourlyRate || coach.price || 0,
            currentStudents: coach.currentStudents || coach.studentCount || 0,
            maxStudents: coach.maxStudents || coach.maxStudentCount || 20
          }))
          
          // 使用Vue.set确保响应式更新
          this.$set(this, 'coachList', newCoachList)
          console.log('处理后教练列表:', this.coachList)
          
          this.total = this.coachList.length
          console.log('最终加载的教练数量:', this.coachList.length)
        } else {
          console.warn('响应状态码不正确:', response?.code)
          this.coachList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取教练列表失败:', error)
        console.error('错误详情:', error.response ? error.response.data : error.message)
        this.$message.error('获取教练列表失败: ' + (error.response?.data?.message || error.message))
        this.coachList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async handleBrowseAll() {
      this.searchForm = {
        name: '',
        gender: '',
        age: null,
        level: ''
      }
      console.log('重置搜索表单:', this.searchForm)
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
      this.loadMyCoaches()
    },
    isCoachSelected(coach) {
      if (!coach || !coach.id || !this.selectedCoaches) return false
      return this.selectedCoaches.some(selected => selected && selected.id === coach.id)
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
        
        const studentId = this.$store.state.user?.user?.id
        if (!studentId) {
          this.$message.error('无法获取学生信息')
          return
        }
        
        const response = await selectCoach(coach.id, studentId)
        if (response.code === 200) {
          this.$message.success('申请提交成功，请等待教练确认')
          this.loadCoaches()
          this.loadMyCoaches()
        } else {
          this.$message.error(response.message || '申请提交失败')
        }
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
}.coach-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.selected-coach {
  border: 2px solid #67C23A;
  background-color: #f0f9ff;
}

.selected-coach:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.2);
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