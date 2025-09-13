<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :http-request="uploadAvatar"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*">
              <img v-if="displayUser.avatar" :src="displayUser.avatar" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <h3>{{ displayUser.realName || displayUser.username }}</h3>
            <el-tag :type="getLevelType(displayUser.level)">
              {{ getLevelText(displayUser.level) }}
            </el-tag>
          </div>
          
          <div class="profile-info">
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span>用户名：{{ displayUser.username }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-user-solid"></i>
              <span>真实姓名：{{ displayUser.realName }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-male"></i>
              <span>性别：{{ displayUser.gender === 'MALE' ? '男' : '女' }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-time"></i>
              <span>年龄：{{ displayUser.age }}岁</span>
            </div>
            <div class="info-item">
              <i class="el-icon-phone"></i>
              <span>电话：{{ displayUser.phone }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-message"></i>
              <span>邮箱：{{ displayUser.email }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-star-on"></i>
              <span>当前学员：{{ displayUser.currentStudents }}/{{ displayUser.maxStudents }}人</span>
            </div>


          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <div slot="header">
            <span>个人信息</span>
          </div>
          
          <el-form 
            ref="userForm" 
            :model="userForm" 
            :rules="userRules" 
            label-width="100px"
            v-loading="loading">
            
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userForm.realName" placeholder="请输入真实姓名"></el-input>
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="userForm.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="userForm.age" :min="1" :max="100"></el-input-number>
            </el-form-item>
            
            <el-form-item label="电话" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱地址"></el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="updating">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>修改密码</span>
          </div>
          
          <el-form 
            ref="passwordForm" 
            :model="passwordForm" 
            :rules="passwordRules" 
            label-width="100px"
            v-loading="passwordLoading">
            
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input 
                v-model="passwordForm.currentPassword" 
                type="password" 
                show-password
                placeholder="请输入当前密码">
              </el-input>
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password
                placeholder="请输入8-16位新密码">
              </el-input>
              <div class="password-tips">
                密码要求：8-16位，必须包含字母、数字和特殊字符
              </div>
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password
                placeholder="请再次输入新密码">
              </el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updatePassword" :loading="passwordUpdating">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getCoachInfo } from '@/api/coach'
import { updateUserInfo } from '@/api/user'
import { getCampusList } from '@/api/system'
import { updatePassword } from '@/api/coach'
import { uploadAvatar } from '@/api/upload'

export default {
  name: 'CoachProfile',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'))
      } else if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/.test(value)) {
        callback(new Error('密码必须为8-16位，包含字母、数字和特殊字符'))
      } else {
        callback()
      }
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      loading: false,
      updating: false,
      passwordLoading: false,
      passwordUpdating: false,
      // 用于展示的用户信息
        displayUser: {
          id: '',
          username: '',
          realName: '',
          gender: '',
          age: '',
          phone: '',
          email: '',
          avatar: '',
          level: '',
          currentStudents: 0,
          maxStudents: 20
        },
        // 编辑表单数据
        userForm: {
          id: '',
          username: '',
          realName: '',
          gender: '',
          age: '',
          phone: '',
          email: '',
          avatar: '',
          currentStudents: 0,
          maxStudents: 20
        },
      campusList: [],

      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      userRules: {
          realName: [
            { required: true, message: '请输入真实姓名', trigger: 'blur' },
            { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
          ],
          gender: [
            { required: true, message: '请选择性别', trigger: 'change' }
          ],
          age: [
            { required: true, message: '请输入年龄', trigger: 'blur' },
            { type: 'number', min: 18, max: 80, message: '年龄必须在18-80岁之间', trigger: 'blur' }
          ],
          phone: [
            { required: true, message: '请输入手机号码', trigger: 'blur' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
          ],
          email: [
            { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
          ]
        },
      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    uploadAction() {
      return (process.env.VUE_APP_BASE_API || 'http://localhost:8080') + '/api/coach/upload-avatar'
    }
  },
  created() {
    this.loadUserInfo()
    this.loadCampusList()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        // 获取当前登录教练的ID
        const coachId = this.$store.state.user.user?.id
        if (!coachId) {
          this.$message.error('无法获取教练ID')
          return
        }
        
        const response = await getCoachInfo(coachId)
        const coachData = response.data
        
        // 同时更新展示数据和编辑表单数据
        const coachInfo = {
          id: coachData.id,
          username: coachData.username,
          realName: coachData.realName || '',
          gender: coachData.gender || '',
          age: coachData.age || '',
          phone: coachData.phone || '',
          email: coachData.email || '',
          campusId: coachData.campusId,
          campusName: coachData.campusName || '',
          avatar: coachData.avatar || '',
          role: coachData.userType || 'COACH',
          level: coachData.level || '',
          studentCount: coachData.studentCount || 0
        }
        
        // 展示数据
        this.displayUser = {
          id: coachData.id,
          username: coachData.username,
          realName: coachData.realName || '',
          gender: coachData.gender || '',
          age: coachData.age || '',
          phone: coachData.phone || '',
          email: coachData.email || '',
          avatar: coachData.avatar || '/uploads/avatars/default-avatar.jpg',
          level: coachData.level || '',
          awards: coachData.awards || '',
          hourlyRate: coachData.hourlyRate || 0,
          currentStudents: coachData.currentStudents || 0,
          maxStudents: coachData.maxStudents || 20
        }
        // 确保头像URL是完整的API地址格式
        if (this.displayUser.avatar) {
          const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
          if (typeof this.displayUser.avatar === 'string') {
            if (!this.displayUser.avatar.startsWith('http')) {
              // 如果是相对路径，添加API前缀
              this.displayUser.avatar = this.displayUser.avatar.startsWith('/') 
                ? baseApi + this.displayUser.avatar 
                : baseApi + '/uploads/avatars/' + this.displayUser.avatar
            }
          }
        }
        // 编辑表单数据（初始化为展示数据的副本，但不包含level字段）
        const { level, ...editableData } = this.displayUser
        this.userForm = editableData
        
        console.log('获取教练信息成功:', coachData)
      } catch (error) {
        console.error('获取教练信息失败:', error)
        this.$message.error('获取教练信息失败')
      } finally {
        this.loading = false
      }
    },
    async loadCampusList() {
      try {
        const response = await getCampusList()
        this.campusList = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
      }
    },
    async updateProfile() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return
        
        this.updating = true
        try {
          // 获取当前登录教练的ID
          const coachId = this.$store.state.user.user?.id
          if (!coachId) {
            this.$message.error('无法获取教练ID')
            return
          }
          
          const updateData = {
            realName: this.userForm.realName,
            gender: this.userForm.gender,
            age: this.userForm.age,
            phone: this.userForm.phone,
            email: this.userForm.email,
            avatar: this.userForm.avatar || '/uploads/avatars/default-avatar.jpg'
          }
          
          console.log('更新教练信息:', { coachId, updateData })
          
          const response = await updateUserInfo(coachId, updateData)
          
          if (response.code === 200) {
            this.$message.success('个人信息更新成功')
            // 使用返回的最新数据更新展示信息，同时保留教练特有属性
            const updatedData = response.data
            
            // 合并原有教练特有属性与新数据
            const updatedCoachInfo = {
              id: updatedData.id,
              username: updatedData.username,
              realName: updatedData.realName || '',
              gender: updatedData.gender || '',
              age: updatedData.age || '',
              phone: updatedData.phone || '',
              email: updatedData.email || '',
              campusId: updatedData.campusId,
              campusName: updatedData.campusName || '',
              avatar: updatedData.avatar || '',
              role: updatedData.userType || 'COACH',
              // 保留原有教练特有属性
              level: this.displayUser.level || '',
              studentCount: this.displayUser.studentCount || 0,
              currentStudents: this.displayUser.currentStudents || 0,
              maxStudents: this.displayUser.maxStudents || 20
            }
            
            // 更新展示数据
            this.displayUser = { ...updatedCoachInfo }
            
            // 更新编辑表单数据（不包含level等不可编辑字段）
            const { level, studentCount, currentStudents, maxStudents, ...editableData } = updatedCoachInfo
            this.userForm = { 
              ...editableData,
              currentStudents: updatedCoachInfo.currentStudents,
              maxStudents: updatedCoachInfo.maxStudents
            }
            
            console.log('教练信息更新成功:', updatedData)
          } else {
            this.$message.error(response.message || '更新个人信息失败')
          }
        } catch (error) {
          console.error('更新教练信息失败:', error)
          this.$message.error('更新个人信息失败：' + (error.response?.data?.message || '请检查输入信息'))
        } finally {
          this.updating = false
        }
      })
    },
    async updatePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return
        
        this.passwordUpdating = true
        try {
          // 获取当前登录教练的ID
          const coachId = this.$store.state.user.user?.id
          if (!coachId) {
            this.$message.error('无法获取教练ID')
            return
          }
          
          const response = await updatePassword(
            coachId,
            this.passwordForm.currentPassword,
            this.passwordForm.newPassword
          )
          
          if (response.code === 200) {
            this.$message.success('密码修改成功，请重新登录')
            this.resetPasswordForm()
            
            // 使用返回的最新数据更新展示信息，同时保留教练特有属性
            const updatedData = response.data
            
            // 合并原有教练特有属性与新数据
            const updatedCoachInfo = {
              id: updatedData.id,
              username: updatedData.username,
              realName: updatedData.realName || '',
              gender: updatedData.gender || '',
              age: updatedData.age || '',
              phone: updatedData.phone || '',
              email: updatedData.email || '',
              campusId: updatedData.campusId,
              campusName: updatedData.campusName || '',
              avatar: updatedData.avatar || '',
              role: updatedData.userType || 'COACH',
              // 保留原有教练特有属性
              level: this.displayUser.level || '',
              studentCount: this.displayUser.studentCount || 0,
              currentStudents: this.displayUser.currentStudents || 0,
              maxStudents: this.displayUser.maxStudents || 20
            }
            
            // 更新展示数据
            this.displayUser = { ...updatedCoachInfo }
            
            // 更新编辑表单数据（不包含level等不可编辑字段）
            const { level, studentCount, currentStudents, maxStudents, ...editableData } = updatedCoachInfo
            this.userForm = { 
              ...editableData,
              currentStudents: updatedCoachInfo.currentStudents,
              maxStudents: updatedCoachInfo.maxStudents
            }
            
            console.log('密码修改成功:', updatedData)
            
            // 延迟跳转登录页面
            setTimeout(() => {
              this.$store.dispatch('user/logout')
              this.$router.push('/login')
            }, 2000)
          } else {
            this.$message.error(response.message || '密码修改失败')
          }
        } catch (error) {
          console.error('修改密码失败:', error)
          this.$message.error('修改密码失败：' + (error.response?.data?.message || '请检查当前密码是否正确'))
        } finally {
          this.passwordUpdating = false
        }
      })
    },
    resetPasswordForm() {
      this.$refs.passwordForm.resetFields()
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    },
    // 上传头像
    uploadAvatar({ file }) {
      const loading = this.$loading({
        lock: true,
        text: '上传中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      uploadAvatar(file)
        .then(response => {
          // 处理不同响应格式，提取相对地址
          let avatarUrl = response.data || response.data?.data || response
          
          if (typeof avatarUrl === 'string') {
            if (avatarUrl.startsWith('http')) {
              // 如果是完整URL，提取相对路径部分
              const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
              if (avatarUrl.includes('/uploads/avatars/')) {
                // 提取相对路径部分（从/uploads开始）
                const relativePath = avatarUrl.substring(avatarUrl.indexOf('/uploads/avatars/'))
                this.userForm.avatar = relativePath
              } else {
                // 无法提取，使用返回的相对地址
                this.userForm.avatar = avatarUrl
              }
            } else if (avatarUrl.startsWith('/')) {
              // 已经是相对路径，直接使用
              this.userForm.avatar = avatarUrl
            } else {
              // 如果是文件名，拼接相对路径
              this.userForm.avatar = '/uploads/avatars/' + avatarUrl
            }
          } else {
            // 使用默认头像
            this.userForm.avatar = '/uploads/avatars/default-avatar.jpg'
          }
          
          // 显示完整URL用于预览
          const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
          this.displayUser.avatar = baseApi + this.userForm.avatar
          
          this.$message.success('头像上传成功，请点击保存修改按钮保存更改')
        })
        .catch(error => {
          this.$message.error('头像上传失败')
          console.error('头像上传失败:', error)
        })
        .finally(() => {
          loading.close()
        })
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
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
.app-container {
  padding: 20px;
}

.profile-card {
  text-align: center;
}

.avatar-section {
  padding: 20px 0;
}

.avatar-uploader {
  margin-bottom: 15px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 6px;
}

.profile-info {
  margin-top: 20px;
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.info-item i {
  margin-right: 10px;
  color: #409EFF;
  width: 20px;
  text-align: center;
}

.password-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>