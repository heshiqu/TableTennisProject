<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>许可证管理</span>
        <el-button style="float: right;" type="primary" @click="handleGenerateLicense">生成许可证</el-button>
      </div>
      
      <div class="license-status">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-number">{{ statistics.total }}</div>
                <div class="status-label">总许可证</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-number" style="color: #67C23A;">{{ statistics.active }}</div>
                <div class="status-label">激活中</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-number" style="color: #E6A23C;">{{ statistics.expired }}</div>
                <div class="status-label">已过期</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="status-card">
              <div class="status-item">
                <div class="status-number" style="color: #F56C6C;">{{ statistics.revoked }}</div>
                <div class="status-label">已吊销</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="filter-container">
        <el-input
          v-model="keyword"
          placeholder="搜索许可证号、机构名称或联系人"
          style="width: 200px;"
          @keyup.enter.native="handleFilter"
        />
        <el-select v-model="statusFilter" placeholder="许可证状态" clearable @change="handleFilter">
          <el-option label="激活中" value="ACTIVE" />
          <el-option label="已过期" value="EXPIRED" />
          <el-option label="已吊销" value="REVOKED" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button @click="handleExport">导出</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="licenses"
        border
        style="width: 100%"
      >
        <el-table-column prop="licenseKey" label="许可证号" width="200" />
        <el-table-column prop="organizationName" label="机构名称" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="contactEmail" label="邮箱" width="180" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="到期日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'ACTIVE'" type="text" size="small" @click="handleRenew(row)">续期</el-button>
            <el-button v-if="row.status === 'ACTIVE'" type="text" size="small" @click="handleRevoke(row)">吊销</el-button>
            <el-button type="text" size="small" @click="handleSendEmail">发送邮件</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </el-card>

    <!-- 许可证详情对话框 -->
    <el-dialog title="许可证详情" :visible.sync="detailDialogVisible" width="60%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="许可证号">{{ selectedLicense.licenseKey }}</el-descriptions-item>
        <el-descriptions-item label="机构名称">{{ selectedLicense.organizationName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ selectedLicense.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedLicense.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedLicense.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ selectedLicense.startDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ selectedLicense.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedLicense.status)">
            {{ getStatusText(selectedLicense.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedLicense.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ selectedLicense.updateTime }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h4>使用说明</h4>
        <p>请将许可证号复制到系统配置文件中，系统将自动验证许可证的有效性。</p>
        <el-alert
          title="重要提醒"
          type="warning"
          description="许可证只能在一台设备上使用，请妥善保管许可证信息。"
          show-icon
        />
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyLicense">复制许可证</el-button>
      </div>
    </el-dialog>

    <!-- 生成许可证对话框 -->
    <el-dialog title="生成许可证" :visible.sync="generateDialogVisible" width="40%">
      <el-form ref="licenseForm" :model="licenseForm" :rules="rules" label-width="120px">
        <el-form-item label="机构名称" prop="organizationName">
          <el-input v-model="licenseForm.organizationName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="licenseForm.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="licenseForm.contactPhone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="licenseForm.contactEmail" />
        </el-form-item>
        <el-form-item label="使用期限" prop="duration">
          <el-select v-model="licenseForm.duration" placeholder="选择使用期限" style="width: 100%">
            <el-option label="1年" value="1" />
            <el-option label="2年" value="2" />
            <el-option label="3年" value="3" />
            <el-option label="5年" value="5" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGenerate">生成</el-button>
      </div>
    </el-dialog>

    <!-- 续期对话框 -->
    <el-dialog title="许可证续期" :visible.sync="renewDialogVisible" width="30%">
      <el-form ref="renewForm" :model="renewForm" :rules="renewRules" label-width="100px">
        <el-form-item label="机构名称">
          <span>{{ selectedLicense.organizationName }}</span>
        </el-form-item>
        <el-form-item label="当前到期">
          <span>{{ selectedLicense.endDate }}</span>
        </el-form-item>
        <el-form-item label="续期时长" prop="duration">
          <el-select v-model="renewForm.duration" placeholder="选择续期时长" style="width: 100%">
            <el-option label="1年" value="1" />
            <el-option label="2年" value="2" />
            <el-option label="3年" value="3" />
            <el-option label="5年" value="5" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="renewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRenew">确认续期</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getLicenses, generateLicense, renewLicense, revokeLicense, exportLicenses } from '@/api/super-admin'
import Pagination from '@/components/Pagination'

export default {
  name: 'LicenseManagement',
  components: { Pagination },
  data() {
    return {
      licenses: [],
      campuses: [],
      statistics: {
        total: 0,
        active: 0,
        expired: 0,
        revoked: 0
      },
      keyword: '',
      statusFilter: '',
      dateRange: [],
      loading: false,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      detailDialogVisible: false,
      generateDialogVisible: false,
      renewDialogVisible: false,
      selectedLicense: {},
      licenseForm: {
        organizationName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        duration: '1'
      },
      renewForm: {
        duration: '1'
      },
      rules: {
        organizationName: [
          { required: true, message: '请输入机构名称', trigger: 'blur' }
        ],
        contactPerson: [
          { required: true, message: '请输入联系人', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        contactEmail: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        duration: [
          { required: true, message: '请选择使用期限', trigger: 'change' }
        ]
      },
      renewRules: {
        duration: [
          { required: true, message: '请选择续期时长', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getStatistics()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.listQuery.page,
          limit: this.listQuery.limit,
          keyword: this.keyword,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        const response = await getLicenses(params)
        this.licenses = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取许可证列表失败:', error)
        this.$message.error('获取许可证列表失败')
      } finally {
        this.loading = false
      }
    },
    async getStatistics() {
      try {
        // 模拟统计数据
        this.statistics = {
          total: 156,
          active: 132,
          expired: 18,
          revoked: 6
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleGenerateLicense() {
      this.licenseForm = {
        organizationName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        duration: '1'
      }
      this.generateDialogVisible = true
    },
    handleViewDetail(row) {
      this.selectedLicense = row
      this.detailDialogVisible = true
    },
    handleRenew(row) {
      this.selectedLicense = row
      this.renewForm = {
        duration: '1'
      }
      this.renewDialogVisible = true
    },
    async handleRevoke(row) {
      try {
        await this.$confirm(`确定要吊销 ${row.organizationName} 的许可证吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await revokeLicense(row.id)
        this.$message.success('许可证已吊销')
        this.getList()
        this.getStatistics()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('吊销许可证失败:', error)
          this.$message.error('吊销许可证失败')
        }
      }
    },
    handleSendEmail() {
      this.$message.info('邮件发送功能开发中...')
    },
    async submitGenerate() {
      try {
        await this.$refs.licenseForm.validate()
        
        const response = await generateLicense(this.licenseForm)
        this.$message.success('许可证生成成功')
        this.generateDialogVisible = false
        this.getList()
        this.getStatistics()
        
        // 复制许可证号
        this.$copyText(response.data.licenseKey)
        this.$message.success('许可证号已复制到剪贴板')
      } catch (error) {
        console.error('生成许可证失败:', error)
        this.$message.error('生成许可证失败')
      }
    },
    async submitRenew() {
      try {
        await this.$refs.renewForm.validate()
        
        await renewLicense({
          licenseId: this.selectedLicense.id,
          duration: this.renewForm.duration
        })
        
        this.$message.success('续期成功')
        this.renewDialogVisible = false
        this.getList()
        this.getStatistics()
      } catch (error) {
        console.error('续期失败:', error)
        this.$message.error('续期失败')
      }
    },
    handleCopyLicense() {
      this.$copyText(this.selectedLicense.licenseKey)
      this.$message.success('许可证号已复制到剪贴板')
    },
    async handleExport() {
      try {
        const params = {
          keyword: this.keyword,
          status: this.statusFilter,
          startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
          endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
        }
        
        await exportLicenses(params)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },
    getStatusType(status) {
      const types = {
        'ACTIVE': 'success',
        'EXPIRED': 'warning',
        'REVOKED': 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'ACTIVE': '激活中',
        'EXPIRED': '已过期',
        'REVOKED': '已吊销'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.license-status {
  margin-bottom: 20px;
}

.status-card {
  text-align: center;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-item {
  padding: 20px 0;
}

.status-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-label {
  font-size: 14px;
  color: #666;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-container > * {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>