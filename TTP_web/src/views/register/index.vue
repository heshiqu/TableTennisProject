<template>
  <div class="register-container">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form" label-position="left">

      <div class="title-container">
        <h3 class="title">乒乓球培训管理系统</h3>
        <p class="subtitle">用户注册</p>
      </div>

      <el-form-item prop="userType">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-select v-model="registerForm.userType" placeholder="请选择用户类型" style="width: 85%">
          <el-option label="学员" value="STUDENT" />
          <el-option label="教练" value="COACH" />
        </el-select>
      </el-form-item>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          v-model="registerForm.username"
          placeholder="请输入用户名 (3-20位)"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="registerForm.password"
          :type="passwordType"
          placeholder="请输入密码 (8-16位，包含字母、数字、特殊字符)"
          name="password"
          tabindex="2"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="registerForm.confirmPassword"
          :type="confirmPasswordType"
          placeholder="请确认密码"
          name="confirmPassword"
          tabindex="3"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showConfirmPwd">
          <svg-icon :icon-class="confirmPasswordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="realName">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          v-model="registerForm.realName"
          placeholder="请输入真实姓名"
          name="realName"
          tabindex="4"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="gender">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-select v-model="registerForm.gender" placeholder="请选择性别" style="width: 85%">
          <el-option label="男" value="MALE" />
          <el-option label="女" value="FEMALE" />
          <el-option label="保密" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item prop="age">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input-number
          v-model="registerForm.age"
          :min="1"
          :max="120"
          placeholder="请输入年龄"
          style="width: 85%"
        />
      </el-form-item>

      <el-form-item prop="phone">
        <span class="svg-container">
          <svg-icon icon-class="phone" />
        </span>
        <el-input
          v-model="registerForm.phone"
          placeholder="请输入手机号"
          name="phone"
          tabindex="5"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="email">
        <span class="svg-container">
          <svg-icon icon-class="email" />
        </span>
        <el-input
          v-model="registerForm.email"
          placeholder="请输入邮箱地址"
          name="email"
          tabindex="6"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 头像上传 -->
      <el-form-item label="头像">
        <div class="avatar-section">
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader-enhanced"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
              accept="image/*"
            >
              <div class="avatar-display-area">
                <img v-if="registerForm.avatar" :src="getAvatarDisplayUrl(registerForm.avatar)" class="avatar-image" @error="handleImageError" />
                <div v-else class="avatar-placeholder-enhanced">
                  <div class="avatar-icon-wrapper">
                    <svg-icon icon-class="user" class="avatar-icon-large" />
                  </div>
                  <div class="avatar-upload-text">点击上传头像</div>
                </div>
              </div>
              <div class="avatar-hover-overlay">
                <svg-icon icon-class="upload" class="upload-icon" />
                <span>更换头像</span>
              </div>
            </el-upload>
            
            <div class="avatar-info">
              <div class="avatar-tip-enhanced">
                <svg-icon icon-class="info" class="info-icon" />
                <span>支持 JPG、PNG 格式，最大5MB</span>
              </div>
              <el-button 
                v-if="registerForm.avatar" 
                type="text" 
                size="mini" 
                class="remove-avatar-btn"
                @click="clearAvatar"
              >
                <svg-icon icon-class="delete" />
                清除头像
              </el-button>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 校区选择 -->
      <el-form-item prop="campusId">
        <span class="svg-container">
          <svg-icon icon-class="nested" />
        </span>
        <el-select v-model="registerForm.campusId" placeholder="请选择校区" style="width: 85%">
          <el-option
            v-for="campus in campusList"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
      </el-form-item>

      <!-- 学员特有字段 - 余额字段已移除，注册时默认余额为0 -->

      <!-- 教练特有字段 -->
      <template v-if="registerForm.userType === 'COACH'">
        <el-form-item prop="level">
          <span class="svg-container">
            <svg-icon icon-class="star" />
          </span>
          <el-select v-model="registerForm.level" placeholder="请选择教练等级" style="width: 85%">
            <el-option label="初级教练" value="JUNIOR" />
            <el-option label="中级教练" value="INTERMEDIATE" />
            <el-option label="高级教练" value="SENIOR" />
            <el-option label="国家级教练" value="NATIONAL" />
          </el-select>
        </el-form-item>

        <el-form-item prop="awards">
          <span class="svg-container">
            <svg-icon icon-class="award" />
          </span>
          <el-input
            v-model="registerForm.awards"
            type="textarea"
            :rows="3"
            placeholder="请填写获奖经历 (可选)"
            style="width: 85%"
          />
        </el-form-item>
      </template>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:20px;" @click.native.prevent="handleRegister">注册</el-button>

      <div class="login-link">
        <el-button type="text" @click="goToLogin">已有账号？立即登录</el-button>
      </div>

    </el-form>
  </div>
</template>

<script>
import { uploadAvatar } from '@/api/upload'
import request from '@/utils/request'

export default {
  name: 'Register',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const validatePhone = (rule, value, callback) => {
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(value)) {
        callback(new Error('请输入正确的手机号格式'))
      } else {
        callback()
      }
    }

    const validateEmail = (rule, value, callback) => {
      const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
      if (value && !emailRegex.test(value)) {
        callback(new Error('请输入正确的邮箱格式'))
      } else {
        callback()
      }
    }

    const validatePassword = (rule, value, callback) => {
      const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/
      if (!passwordRegex.test(value)) {
        callback(new Error('密码必须包含字母、数字和特殊字符，长度为8-16位'))
      } else {
        callback()
      }
    }

    return {
      campusList: [
        { id: 1, name: '中心校区' },
        { id: 2, name: '东校区' },
        { id: 3, name: '西校区' },
        { id: 4, name: '南校区' },
        { id: 5, name: '北校区' }
      ],
      registerForm: {
        userType: 'STUDENT',
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        gender: 'MALE',
        age: 18,
        phone: '',
        email: '',
        campusId: '',
        level: 'JUNIOR',
        awards: '',
        avatar: ''
      },
      registerRules: {
        userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度必须在3-20个字符之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
        ],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [
          { required: true, message: '请输入年龄', trigger: 'blur' },
          { type: 'number', min: 1, max: 120, message: '年龄必须在1-120之间', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { validator: validatePhone, trigger: 'blur' }
        ],
        email: [
          { required: false, message: '请输入邮箱地址', trigger: 'blur' },
          { validator: validateEmail, trigger: 'blur' }
        ],
        campusId: [
        { required: true, message: '请选择校区', trigger: 'change' }
      ],
        level: [{ required: true, message: '请选择教练等级', trigger: 'change' }]
      },
      loading: false,
      passwordType: 'password',
      confirmPasswordType: 'password'
    }
  },
  mounted() {
    this.loadCampusList()
  },
  methods: {
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
    },
    showConfirmPwd() {
      this.confirmPasswordType = this.confirmPasswordType === 'password' ? '' : 'password'
    },
    async loadCampusList() {
      try {
        // 使用request工具发送GET请求获取校区列表
        const response = await request({
          url: '/api/campuses',
          method: 'get'
        })
        
        // 正确处理响应数据结构
        if (response && response.code === 200 && Array.isArray(response.data)) {
          this.campusList = response.data.map(campus => ({
            id: campus.id,
            name: campus.name
          }))
        } else {
          // 使用默认数据作为备用
          this.campusList = [
            { id: 1, name: '中心校区' },
            { id: 2, name: '海淀分校区' },
            { id: 3, name: '朝阳分校区' }
          ]
          
          // 在开发环境记录响应格式问题
          if (process.env.NODE_ENV === 'development') {
            console.log('校区数据响应格式不符合预期，使用默认数据', response)
          }
        }
      } catch (error) {
        // 处理网络请求异常
        if (process.env.NODE_ENV === 'development') {
          console.error('获取校区列表失败:', error)
        }
        // 使用默认校区数据作为备用
        this.campusList = [
          { id: 1, name: '中心校区' },
          { id: 2, name: '海淀分校区' },
          { id: 3, name: '朝阳分校区' }
        ]
      }
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          const registerData = {
            username: this.registerForm.username,
            password: this.registerForm.password,
            realName: this.registerForm.realName,
            gender: this.registerForm.gender,
            age: this.registerForm.age,
            phone: this.registerForm.phone,
            email: this.registerForm.email,
            campusId: this.registerForm.campusId,
            userType: this.registerForm.userType,
            avatar: this.registerForm.avatar
          }

          // 添加用户类型特有字段
          if (this.registerForm.userType === 'STUDENT') {
            registerData.initialBalance = 0 // 学生注册时初始余额固定为0
          } else if (this.registerForm.userType === 'COACH') {
            registerData.level = this.registerForm.level
            registerData.awards = this.registerForm.awards
          }

          const action = this.registerForm.userType === 'STUDENT' ? 'user/registerStudent' : 'user/registerCoach'
          
          this.$store.dispatch(action, registerData).then(response => {
            this.$message.success(response.message || '注册成功')
            this.$router.push('/login')
            this.loading = false
          }).catch(error => {
            let errorMessage = '注册失败'
            if (error.response && error.response.data) {
              errorMessage = error.response.data.message || error.response.data.error || '注册失败'
            } else if (error.message) {
              errorMessage = error.message
            }
            this.$message.error(errorMessage)
            this.loading = false
          })
        } else {
          return false
        }
      })
    },
    goToLogin() {
      this.$router.push('/login')
    },
    
    // 清除头像
    clearAvatar() {
      this.registerForm.avatar = ''
    },
    
    // 处理图片加载错误
    handleImageError() {
      console.error('头像图片加载失败，使用默认头像')
      this.registerForm.avatar = ''
    },

    // 获取头像显示URL（将相对地址转为完整URL）
    getAvatarDisplayUrl(relativeUrl) {
      if (!relativeUrl) return ''
      
      const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
      
      if (relativeUrl.startsWith('http')) {
        return relativeUrl
      } else if (relativeUrl.startsWith('/')) {
        return baseApi + relativeUrl
      } else {
        return baseApi + '/uploads/avatars/' + relativeUrl
      }
    },
    
    // 头像上传前检查
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!')
        return false
      }
      return true
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
                this.registerForm.avatar = relativePath
              } else {
                // 无法提取，使用返回的相对地址
                this.registerForm.avatar = avatarUrl
              }
            } else if (avatarUrl.startsWith('/')) {
              // 已经是相对路径，直接使用
              this.registerForm.avatar = avatarUrl
            } else {
              // 如果是文件名，拼接相对路径
              this.registerForm.avatar = '/uploads/avatars/' + avatarUrl
            }
          } else {
            const baseApi = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
      this.registerForm.avatar = baseApi + '/uploads/avatars/default-avatar.jpg'
          }
          
          this.$message.success('头像上传成功')
        })
        .catch(error => {
          this.$message.error('头像上传失败')
          console.error('头像上传失败:', error)
        })
        .finally(() => {
          loading.close()
        })
    }
  }
}</script>

<style lang="scss">
$bg:#283443;
$light_gray:#fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .register-container .el-input input {
    color: $cursor;
  }
}

.register-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }

  .el-select {
    .el-input {
      width: 100%;
    }
  }
}
.avatar-section {
  padding: 10px 0;
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.avatar-uploader-enhanced {
  position: relative;
}

.avatar-uploader-enhanced .el-upload {
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  width: 120px;
  height: 120px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #fafafa;
}

.avatar-uploader-enhanced .el-upload:hover {
  border-color: #409EFF;
  background: #f0f9ff;
}

.avatar-uploader-enhanced .el-upload:hover .avatar-hover-overlay {
  opacity: 1;
}

.avatar-display-area {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder-enhanced {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.avatar-icon-wrapper {
  margin-bottom: 8px;
}

.avatar-icon-large {
  font-size: 36px;
  color: #d9d9d9;
}

.avatar-upload-text {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.avatar-hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.avatar-tip-enhanced {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.info-icon {
  font-size: 14px;
  color: #409EFF;
}

.remove-avatar-btn {
  align-self: flex-start;
  color: #f56c6c;
  padding: 0;
  font-size: 12px;
}

.remove-avatar-btn:hover {
  color: #ff7875;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .avatar-upload-container {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .avatar-info {
    align-items: center;
  }
}

</style>

<style lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray:#889aa4;
$light_gray:#eee;

.register-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;
  padding: 50px 0;

  .register-form {
    position: relative;
    width: 600px;
    max-width: 100%;
    padding: 20px 35px;
    margin: 0 auto;
    overflow: hidden;
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $light_gray;
      margin: 0px auto 10px auto;
      text-align: center;
      font-weight: bold;
    }

    .subtitle {
      font-size: 16px;
      color: $light_gray;
      margin: 0px auto 30px auto;
      text-align: center;
      opacity: 0.8;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

  .login-link {
    text-align: center;
    margin-top: 10px;
  }

  @media (max-width: 768px) {
    .register-form {
      width: 100%;
      padding: 20px 20px;
    }
  }
}
</style>