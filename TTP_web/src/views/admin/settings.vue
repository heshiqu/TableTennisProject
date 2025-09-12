<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="基础设置" name="basic">
        <el-form ref="basicForm" :model="basicSettings" :rules="basicRules" label-width="120px">
          <el-form-item label="系统名称" prop="systemName">
            <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
          </el-form-item>
          <el-form-item label="系统Logo" prop="systemLogo">
            <el-upload
              class="avatar-uploader"
              action="/api/upload/logo"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
            >
              <img v-if="basicSettings.systemLogo" :src="basicSettings.systemLogo" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
          </el-form-item>
          <el-form-item label="系统描述" prop="systemDescription">
            <el-input v-model="basicSettings.systemDescription" type="textarea" :rows="3" placeholder="请输入系统描述" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="basicSettings.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="basicSettings.contactEmail" placeholder="请输入联系邮箱" />
          </el-form-item>
          <el-form-item label="联系地址" prop="contactAddress">
            <el-input v-model="basicSettings.contactAddress" placeholder="请输入联系地址" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBasicSettings">保存基础设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="支付设置" name="payment">
        <el-form ref="paymentForm" :model="paymentSettings" :rules="paymentRules" label-width="120px">
          <el-form-item label="微信支付" prop="wechatPayEnabled">
            <el-switch v-model="paymentSettings.wechatPayEnabled" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.wechatPayEnabled" label="微信AppID" prop="wechatAppId">
            <el-input v-model="paymentSettings.wechatAppId" placeholder="请输入微信AppID" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.wechatPayEnabled" label="微信商户号" prop="wechatMchId">
            <el-input v-model="paymentSettings.wechatMchId" placeholder="请输入微信商户号" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.wechatPayEnabled" label="微信密钥" prop="wechatSecretKey">
            <el-input v-model="paymentSettings.wechatSecretKey" placeholder="请输入微信密钥" show-password />
          </el-form-item>
          
          <el-form-item label="支付宝支付" prop="alipayEnabled">
            <el-switch v-model="paymentSettings.alipayEnabled" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.alipayEnabled" label="支付宝AppID" prop="alipayAppId">
            <el-input v-model="paymentSettings.alipayAppId" placeholder="请输入支付宝AppID" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.alipayEnabled" label="支付宝公钥" prop="alipayPublicKey">
            <el-input v-model="paymentSettings.alipayPublicKey" type="textarea" :rows="3" placeholder="请输入支付宝公钥" />
          </el-form-item>
          <el-form-item v-if="paymentSettings.alipayEnabled" label="支付宝私钥" prop="alipayPrivateKey">
            <el-input v-model="paymentSettings.alipayPrivateKey" type="textarea" :rows="3" placeholder="请输入支付宝私钥" show-password />
          </el-form-item>
          
          <el-form-item label="线下支付" prop="offlinePayEnabled">
            <el-switch v-model="paymentSettings.offlinePayEnabled" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="savePaymentSettings">保存支付设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notification">
        <el-form ref="notificationForm" :model="notificationSettings" label-width="120px">
          <el-form-item label="短信通知" prop="smsEnabled">
            <el-switch v-model="notificationSettings.smsEnabled" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.smsEnabled" label="短信平台" prop="smsPlatform">
            <el-select v-model="notificationSettings.smsPlatform" placeholder="请选择短信平台">
              <el-option label="阿里云" value="aliyun" />
              <el-option label="腾讯云" value="tencent" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="notificationSettings.smsEnabled" label="AccessKey" prop="smsAccessKey">
            <el-input v-model="notificationSettings.smsAccessKey" placeholder="请输入AccessKey" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.smsEnabled" label="SecretKey" prop="smsSecretKey">
            <el-input v-model="notificationSettings.smsSecretKey" placeholder="请输入SecretKey" show-password />
          </el-form-item>
          
          <el-form-item label="邮件通知" prop="emailEnabled">
            <el-switch v-model="notificationSettings.emailEnabled" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.emailEnabled" label="SMTP服务器" prop="smtpServer">
            <el-input v-model="notificationSettings.smtpServer" placeholder="请输入SMTP服务器地址" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.emailEnabled" label="SMTP端口" prop="smtpPort">
            <el-input-number v-model="notificationSettings.smtpPort" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.emailEnabled" label="邮箱账号" prop="emailUsername">
            <el-input v-model="notificationSettings.emailUsername" placeholder="请输入邮箱账号" />
          </el-form-item>
          <el-form-item v-if="notificationSettings.emailEnabled" label="邮箱密码" prop="emailPassword">
            <el-input v-model="notificationSettings.emailPassword" placeholder="请输入邮箱密码" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveNotificationSettings">保存通知设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="系统维护" name="maintenance">
        <el-form ref="maintenanceForm" :model="maintenanceSettings" label-width="120px">
          <el-form-item label="维护模式" prop="maintenanceMode">
            <el-switch v-model="maintenanceSettings.maintenanceMode" />
          </el-form-item>
          <el-form-item v-if="maintenanceSettings.maintenanceMode" label="维护提示" prop="maintenanceMessage">
            <el-input v-model="maintenanceSettings.maintenanceMessage" type="textarea" :rows="3" placeholder="请输入维护提示信息" />
          </el-form-item>
          <el-form-item label="数据备份" prop="autoBackup">
            <el-switch v-model="maintenanceSettings.autoBackup" />
          </el-form-item>
          <el-form-item v-if="maintenanceSettings.autoBackup" label="备份周期" prop="backupInterval">
            <el-select v-model="maintenanceSettings.backupInterval" placeholder="请选择备份周期">
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveMaintenanceSettings">保存维护设置</el-button>
            <el-button type="success" @click="manualBackup">立即备份</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="授权管理" name="license">
        <el-form ref="licenseForm" :model="licenseInfo" label-width="120px">
          <el-form-item label="当前状态">
            <el-tag :type="licenseInfo.valid ? 'success' : 'danger'">
              {{ licenseInfo.valid ? '已授权' : '未授权' }}
            </el-tag>
          </el-form-item>
          <el-form-item label="授权码">
            <el-input v-model="licenseInfo.licenseKey" :readonly="licenseInfo.valid" placeholder="请输入授权码" />
          </el-form-item>
          <el-form-item label="有效期至">
            <span>{{ licenseInfo.expireDate | parseTime('{y}-{m}-{d}') }}</span>
          </el-form-item>
          <el-form-item label="剩余天数">
            <span>{{ licenseInfo.daysLeft }}天</span>
          </el-form-item>
          <el-form-item>
            <el-button v-if="!licenseInfo.valid" type="primary" @click="activateLicense">激活授权</el-button>
            <el-button v-if="licenseInfo.valid && licenseInfo.daysLeft <= 30" type="warning" @click="renewLicense">续费授权</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getSystemSettings, updateSystemSettings, activateLicense, renewLicense, manualBackup } from '@/api/admin'

export default {
  name: 'SystemSettings',
  data() {
    return {
      activeTab: 'basic',
      basicSettings: {
        systemName: '',
        systemLogo: '',
        systemDescription: '',
        contactPhone: '',
        contactEmail: '',
        contactAddress: ''
      },
      paymentSettings: {
        wechatPayEnabled: false,
        wechatAppId: '',
        wechatMchId: '',
        wechatSecretKey: '',
        alipayEnabled: false,
        alipayAppId: '',
        alipayPublicKey: '',
        alipayPrivateKey: '',
        offlinePayEnabled: true
      },
      notificationSettings: {
        smsEnabled: false,
        smsPlatform: 'aliyun',
        smsAccessKey: '',
        smsSecretKey: '',
        emailEnabled: false,
        smtpServer: '',
        smtpPort: 587,
        emailUsername: '',
        emailPassword: ''
      },
      maintenanceSettings: {
        maintenanceMode: false,
        maintenanceMessage: '系统正在维护中，请稍后再试',
        autoBackup: true,
        backupInterval: 'daily'
      },
      licenseInfo: {
        valid: false,
        licenseKey: '',
        expireDate: '',
        daysLeft: 0
      },
      basicRules: {
        systemName: [{ required: true, message: '系统名称不能为空', trigger: 'blur' }],
        contactPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
        contactEmail: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
      },
      paymentRules: {
        wechatAppId: [{ required: true, message: '微信AppID不能为空', trigger: 'blur' }],
        wechatMchId: [{ required: true, message: '微信商户号不能为空', trigger: 'blur' }],
        wechatSecretKey: [{ required: true, message: '微信密钥不能为空', trigger: 'blur' }],
        alipayAppId: [{ required: true, message: '支付宝AppID不能为空', trigger: 'blur' }],
        alipayPublicKey: [{ required: true, message: '支付宝公钥不能为空', trigger: 'blur' }],
        alipayPrivateKey: [{ required: true, message: '支付宝私钥不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    async loadSettings() {
      try {
        const response = await getSystemSettings()
        if (response.code === 200) {
          const settings = response.data
          this.basicSettings = { ...this.basicSettings, ...settings.basic }
          this.paymentSettings = { ...this.paymentSettings, ...settings.payment }
          this.notificationSettings = { ...this.notificationSettings, ...settings.notification }
          this.maintenanceSettings = { ...this.maintenanceSettings, ...settings.maintenance }
          this.licenseInfo = { ...this.licenseInfo, ...settings.license }
        }
      } catch (error) {
        console.error('加载系统设置失败:', error)
      }
    },
    async saveBasicSettings() {
      this.$refs.basicForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await updateSystemSettings({ basic: this.basicSettings })
            if (response.code === 200) {
              this.$message.success('基础设置保存成功')
            }
          } catch (error) {
            console.error('保存基础设置失败:', error)
          }
        }
      })
    },
    async savePaymentSettings() {
      this.$refs.paymentForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await updateSystemSettings({ payment: this.paymentSettings })
            if (response.code === 200) {
              this.$message.success('支付设置保存成功')
            }
          } catch (error) {
            console.error('保存支付设置失败:', error)
          }
        }
      })
    },
    async saveNotificationSettings() {
      try {
        const response = await updateSystemSettings({ notification: this.notificationSettings })
        if (response.code === 200) {
          this.$message.success('通知设置保存成功')
        }
      } catch (error) {
        console.error('保存通知设置失败:', error)
      }
    },
    async saveMaintenanceSettings() {
      try {
        const response = await updateSystemSettings({ maintenance: this.maintenanceSettings })
        if (response.code === 200) {
          this.$message.success('维护设置保存成功')
        }
      } catch (error) {
        console.error('保存维护设置失败:', error)
      }
    },
    async activateLicense() {
      if (!this.licenseInfo.licenseKey) {
        this.$message.error('请输入授权码')
        return
      }
      try {
        const response = await activateLicense(this.licenseInfo.licenseKey)
        if (response.code === 200) {
          this.licenseInfo = { ...this.licenseInfo, ...response.data }
          this.$message.success('授权激活成功')
        }
      } catch (error) {
        console.error('激活授权失败:', error)
      }
    },
    async renewLicense() {
      try {
        const response = await renewLicense()
        if (response.code === 200) {
          this.licenseInfo = { ...this.licenseInfo, ...response.data }
          this.$message.success('授权续费成功')
        }
      } catch (error) {
        console.error('续费授权失败:', error)
      }
    },
    async manualBackup() {
      try {
        const response = await manualBackup()
        if (response.code === 200) {
          this.$message.success('数据备份成功')
        }
      } catch (error) {
        console.error('数据备份失败:', error)
      }
    },
    handleLogoSuccess(res, file) {
      this.basicSettings.systemLogo = URL.createObjectURL(file.raw)
    },
    beforeLogoUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    }
  }
}
</script>

<style lang="scss" scoped>
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}
.avatar-uploader:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>