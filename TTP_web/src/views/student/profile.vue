<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="/api/upload/avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders">
              <img v-if="displayUser.avatar" :src="displayUser.avatar" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <h3>{{ displayUser.realName || displayUser.username }}</h3>
            <p>{{ displayUser.role === 'STUDENT' ? '学员' : '教练' }}</p>
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
              <i class="el-icon-location"></i>
              <span>校区：{{ displayUser.campusName }}</span>
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
            
            <el-form-item label="校区" prop="campusId">
              <el-select v-model="userForm.campusId" disabled>
                <el-option 
                  v-for="campus in campusList" 
                  :key="campus.id" 
                  :label="campus.name" 
                  :value="campus.id">
                </el-option>
              </el-select>
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
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/student'
import { getCampusList } from '@/api/system'

export default {
  name: 'StudentProfile',
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
        campusId: '',
        campusName: '',
        avatar: '',
        role: 'STUDENT'
      },
      // 用于编辑的表单数据
      userForm: {
        id: '',
        username: '',
        realName: '',
        gender: '',
        age: '',
        phone: '',
        email: '',
        campusId: '',
        campusName: '',
        avatar: '',
        role: 'STUDENT'
      },
      campusList: [],
      uploadHeaders: {
        Authorization: localStorage.getItem('token') || ''
      },
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
          { required: true, message: '请输入年龄', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
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
  created() {
    this.loadUserInfo()
    this.loadCampusList()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        // 获取当前登录用户的ID
        const userId = this.$store.state.user.user?.id
        if (!userId) {
          this.$message.error('无法获取用户ID')
          return
        }
        
        const response = await getUserInfo(userId)
        const userData = response.data
        
        // 同时更新展示数据和编辑表单数据
        const userInfo = {
          id: userData.id,
          username: userData.username,
          realName: userData.realName || '',
          gender: userData.gender || '',
          age: userData.age || '',
          phone: userData.phone || '',
          email: userData.email || '',
          campusId: userData.campusId,
          campusName: userData.campusName || '',
          avatar: userData.avatar || '',
          role: userData.userType || 'STUDENT'
        }
        
        // 展示数据
        this.displayUser = { ...userInfo }
        // 编辑表单数据（初始化为展示数据的副本）
        this.userForm = { ...userInfo }
        
        console.log('获取用户信息成功:', userData)
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.$message.error('获取用户信息失败')
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
          // 获取当前登录用户的ID
          const userId = this.$store.state.user.user?.id
          if (!userId) {
            this.$message.error('无法获取用户ID')
            return
          }
          
          const updateData = {
            realName: this.userForm.realName,
            gender: this.userForm.gender,
            age: this.userForm.age,
            phone: this.userForm.phone,
            email: this.userForm.email
          }
          
          console.log('更新用户信息:', { userId, updateData })
          
          const response = await updateUserInfo(userId, updateData)
          
          if (response.code === 200) {
            this.$message.success('个人信息更新成功')
            // 使用返回的最新数据更新展示信息
            const updatedData = response.data
            const updatedUserInfo = {
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
              role: updatedData.userType || 'STUDENT'
            }
            
            // 更新展示数据
            this.displayUser = { ...updatedUserInfo }
            // 保持编辑表单数据与展示数据同步
            this.userForm = { ...updatedUserInfo }
            
            console.log('用户信息更新成功:', updatedData)
          } else {
            this.$message.error(response.message || '更新个人信息失败')
          }
        } catch (error) {
          console.error('更新个人信息失败:', error)
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
          // 获取当前登录用户的ID
          const userId = this.$store.state.user.user?.id
          if (!userId) {
            this.$message.error('无法获取用户ID')
            return
          }
          
          console.log('修改密码请求:', { userId })
          
          const response = await updatePassword(
            userId,
            this.passwordForm.currentPassword,
            this.passwordForm.newPassword
          )
          
          if (response.code === 200) {
            this.$message.success('密码修改成功，请重新登录')
            this.resetPasswordForm()
            
            // 使用返回的最新数据更新展示信息
            const updatedData = response.data
            const updatedUserInfo = {
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
              role: updatedData.userType || 'STUDENT'
            }
            
            // 更新展示数据
            this.displayUser = { ...updatedUserInfo }
            // 更新编辑表单数据
            this.userForm = { ...updatedUserInfo }
            
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
    handleAvatarSuccess(res, file) {
      if (res.code === 200) {
        this.displayUser.avatar = res.data
        this.userForm.avatar = res.data
        this.$message.success('头像上传成功')
      } else {
        this.$message.error('头像上传失败：' + res.message)
      }
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
    }
  }
}
</script>

<style scoped>
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