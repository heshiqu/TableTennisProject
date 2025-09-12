<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>校区信息</span>
        <el-button style="float: right;" type="primary" @click="handleEdit">编辑信息</el-button>
      </div>
      
      <div v-if="campusInfo" class="campus-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="校区名称">{{ campusInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="校区代码">{{ campusInfo.code }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ campusInfo.address }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ campusInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ campusInfo.email }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ campusInfo.managerName }}</el-descriptions-item>
          <el-descriptions-item label="成立时间">{{ campusInfo.establishedDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="campusInfo.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ campusInfo.status === 'ACTIVE' ? '正常运营' : '暂停运营' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="campus-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">总学员数</div>
                <div class="stat-value">{{ campusStats.totalStudents }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">总教练数</div>
                <div class="stat-value">{{ campusStats.totalCoaches }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">球台总数</div>
                <div class="stat-value">{{ campusStats.totalTables }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">本月收入</div>
                <div class="stat-value">¥{{ campusStats.monthlyIncome }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="campus-description">
          <h4>校区简介</h4>
          <p>{{ campusInfo.description || '暂无简介' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 编辑校区信息对话框 -->
    <el-dialog title="编辑校区信息" :visible.sync="editDialogVisible" width="50%">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="校区名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="校区简介" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" />
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
import { getCampusInfo, updateCampusInfo, getCampusStats } from '@/api/campus'

export default {
  name: 'CampusInfo',
  data() {
    return {
      campusInfo: null,
      campusStats: {
        totalStudents: 0,
        totalCoaches: 0,
        totalTables: 0,
        monthlyIncome: 0
      },
      editForm: {
        name: '',
        address: '',
        phone: '',
        email: '',
        description: ''
      },
      editDialogVisible: false,
      rules: {
        name: [
          { required: true, message: '请输入校区名称', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入地址', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadCampusInfo()
    this.loadCampusStats()
  },
  methods: {
    async loadCampusInfo() {
      try {
        const response = await getCampusInfo()
        this.campusInfo = response.data || {}
      } catch (error) {
        console.error('获取校区信息失败:', error)
        this.$message.error('获取校区信息失败')
      }
    },
    async loadCampusStats() {
      try {
        const response = await getCampusStats()
        this.campusStats = response.data || {}
      } catch (error) {
        console.error('获取校区统计失败:', error)
        this.$message.error('获取校区统计失败')
      }
    },
    handleEdit() {
      this.editForm = {
        name: this.campusInfo.name,
        address: this.campusInfo.address,
        phone: this.campusInfo.phone,
        email: this.campusInfo.email,
        description: this.campusInfo.description || ''
      }
      this.editDialogVisible = true
    },
    async submitEdit() {
      try {
        await this.$refs.editForm.validate()
        await updateCampusInfo(this.editForm)
        this.$message.success('更新成功')
        this.editDialogVisible = false
        this.loadCampusInfo()
      } catch (error) {
        console.error('更新校区信息失败:', error)
        this.$message.error('更新失败')
      }
    }
  }
}
</script>

<style scoped>
.campus-info {
  padding: 20px;
}

.campus-stats {
  margin: 30px 0;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: bold;
}

.campus-description {
  margin-top: 30px;
}

.campus-description h4 {
  margin-bottom: 15px;
  color: #303133;
}

.campus-description p {
  color: #606266;
  line-height: 1.6;
}
</style>