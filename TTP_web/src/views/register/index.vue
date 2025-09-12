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
        awards: ''
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
        const { getCampusList } = require('@/api/campus')
        const response = await getCampusList()
        
        // 检查响应数据结构，兼容不同格式
        let campusData = []
        if (response && response.data) {
          campusData = Array.isArray(response.data) ? response.data : response
        } else if (Array.isArray(response)) {
          campusData = response
        }
        
        if (campusData.length > 0) {
          this.campusList = campusData.map(campus => ({
            id: campus.id,
            name: campus.name
          }))
        } else {
          // 静默使用默认数据，不显示错误
          this.campusList = [
            { id: 1, name: '中心校区' },
            { id: 2, name: '海淀分校区' },
            { id: 3, name: '朝阳分校区' }
          ]
        }
      } catch (error) {
        // 静默处理错误，只在开发环境打印
        if (process.env.NODE_ENV === 'development') {
          console.log('校区数据加载提示:', '使用默认校区数据')
        }
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
            userType: this.registerForm.userType
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
    }
  }
}
</script>

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