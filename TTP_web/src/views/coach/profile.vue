<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>个人信息</span>
        <el-button style="float: right;" type="primary" @click="handleEdit">编辑信息</el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="avatar-card">
            <div class="avatar-container">
              <img v-if="coachInfo.avatar" :src="coachInfo.avatar" class="avatar" />
              <div v-else class="avatar-placeholder">暂无头像</div>
              <el-upload
                class="avatar-uploader"
                action="/api/coach/upload-avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button type="text">更换头像</el-button>
              </el-upload>
            </div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">{{ coachInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ coachInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ coachInfo.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ coachInfo.age }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ coachInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ coachInfo.email }}</el-descriptions-item>
              <el-descriptions-item label="所属校区">{{ coachInfo.campusName }}</el-descriptions-item>
              <el-descriptions-item label="教练级别">
                <el-tag :type="getLevelType(coachInfo.level)">
                  {{ getLevelText(coachInfo.level) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="收费标准">¥{{ coachInfo.hourlyRate }}/小时</el-descriptions-item>
              <el-descriptions-item label="学员数量">{{ coachInfo.studentCount }}人</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ coachInfo.createTime }}</el-descriptions-item>
              <el-descriptions-item label="最后登录">{{ coachInfo.lastLoginTime || '从未登录' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>个人简介</span>
        </div>
        <p>{{ coachInfo.description || '暂无个人简介' }}</p>
      </el-card>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>获奖经历</span>
        </div>
        <p>{{ coachInfo.achievements || '暂无获奖经历' }}</p>
      </el-card>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>教学特色</span>
        </div>
        <p>{{ coachInfo.specialties || '暂无教学特色介绍' }}</p>
      </el-card>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog title="编辑个人信息" :visible.sync="editDialogVisible" width="50%">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="收费标准" prop="hourlyRate">
          <el-input-number
            v-model="editForm.hourlyRate"
            :min="50"
            :max="500"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="个人简介" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="获奖经历" prop="achievements">
          <el-input v-model="editForm.achievements" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="教学特色" prop="specialties">
          <el-input v-model="editForm.specialties" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCoachProfile, updateCoachProfile } from '@/api/coach'

export default {
  name: 'CoachProfile',
  data() {
    return {
      coachInfo: {
        realName: '',
        username: '',
        gender: '',
        age: 0,
        phone: '',
        email: '',
        campusName: '',
        level: '',
        hourlyRate: 0,
        studentCount: 0,
        createTime: '',
        lastLoginTime: '',
        avatar: '',
        description: '',
        achievements: '',
        specialties: ''
      },
      editDialogVisible: false,
      editForm: {
        realName: '',
        phone: '',
        email: '',
        hourlyRate: 0,
        description: '',
        achievements: '',
        specialties: ''
      },
      rules: {
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        hourlyRate: [
          { required: true, message: '请输入收费标准', trigger: 'blur' },
          { type: 'number', min: 50, max: 500, message: '收费标准必须在 50-500 之间', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getProfile()
  },
  methods: {
    async getProfile() {
      try {
        const response = await getCoachProfile()
        this.coachInfo = response.data
        this.editForm = {
          realName: this.coachInfo.realName,
          phone: this.coachInfo.phone,
          email: this.coachInfo.email,
          hourlyRate: this.coachInfo.hourlyRate,
          description: this.coachInfo.description || '',
          achievements: this.coachInfo.achievements || '',
          specialties: this.coachInfo.specialties || ''
        }
      } catch (error) {
        console.error('获取教练信息失败:', error)
        this.$message.error('获取教练信息失败')
      }
    },
    handleEdit() {
      this.editDialogVisible = true
    },
    async submitEdit() {
      try {
        await this.$refs.editForm.validate()
        
        await updateCoachProfile(this.editForm)
        this.$message.success('更新成功')
        this.editDialogVisible = false
        this.getProfile()
      } catch (error) {
        console.error('更新教练信息失败:', error)
        this.$message.error('更新失败')
      }
    },
    handleAvatarSuccess(response) {
      this.coachInfo.avatar = response.data.url
      this.$message.success('头像上传成功')
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('头像只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('头像大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    },
    getLevelType(level) {
      const types = {
        'SENIOR': 'danger',
        'INTERMEDIATE': 'warning',
        'JUNIOR': 'success'
      }
      return types[level] || 'info'
    },
    getLevelText(level) {
      const texts = {
        'SENIOR': '高级教练',
        'INTERMEDIATE': '中级教练',
        'JUNIOR': '初级教练'
      }
      return texts[level] || level
    }
  }
}
</script>

<style scoped>
.avatar-card {
  text-align: center;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-container {
  padding: 20px 0;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 15px;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 14px;
  color: #999;
}

.avatar-uploader {
  margin-top: 10px;
}
</style>