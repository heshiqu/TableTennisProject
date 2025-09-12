<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>许可证管理</span>
        <el-button type="primary" style="float: right;" @click="handleGenerate">
          生成许可证
        </el-button>
      </div>

      <div class="filter-container">
        <el-input
          v-model="listQuery.licenseKey"
          placeholder="许可证密钥"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-select
          v-model="listQuery.status"
          placeholder="状态"
          clearable
          style="width: 120px"
          class="filter-item"
        >
          <el-option label="启用" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
        <el-button
          v-waves
          class="filter-item"
          type="primary"
          icon="el-icon-search"
          @click="handleFilter"
        >
          搜索
        </el-button>
      </div>

      <el-table
        v-loading="listLoading"
        :data="list"
        border
        fit
        highlight-current-row
        style="width: 100%;"
      >
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="许可证密钥" prop="licenseKey" align="center" />
        <el-table-column label="校区" prop="campusName" align="center" />
        <el-table-column label="有效期" align="center">
          <template slot-scope="{row}">
            <span>{{ row.validFrom }} - {{ row.validTo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" align="center" />
        <el-table-column label="操作" align="center" width="250">
          <template slot-scope="{row}">
            <el-button size="mini" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              size="mini"
              type="warning"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              size="mini"
              type="success"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button size="mini" type="primary" @click="handleRenew(row)">
              续期
            </el-button>
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

    <!-- 生成许可证对话框 -->
    <el-dialog
      title="生成许可证"
      :visible.sync="generateVisible"
      width="500px"
    >
      <el-form
        ref="licenseForm"
        :model="licenseForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="选择校区" prop="campusId">
          <el-select v-model="licenseForm.campusId" placeholder="请选择校区" style="width: 100%">
            <el-option
              v-for="item in campusOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期(月)" prop="durationMonths">
          <el-input-number
            v-model="licenseForm.durationMonths"
            :min="1"
            :max="120"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 续期对话框 -->
    <el-dialog
      title="续期许可证"
      :visible.sync="renewVisible"
      width="400px"
    >
      <el-form
        ref="renewForm"
        :model="renewForm"
        :rules="renewRules"
        label-width="100px"
      >
        <el-form-item label="许可证">
          <span>{{ currentLicense.licenseKey }}</span>
        </el-form-item>
        <el-form-item label="续期(月)" prop="durationMonths">
          <el-input-number
            v-model="renewForm.durationMonths"
            :min="1"
            :max="120"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRenewSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="许可证详情"
      :visible.sync="detailVisible"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="许可证密钥：">
          <span>{{ currentLicense.licenseKey }}</span>
        </el-form-item>
        <el-form-item label="所属校区：">
          <span>{{ currentLicense.campusName }}</span>
        </el-form-item>
        <el-form-item label="有效期：">
          <span>{{ currentLicense.validFrom }} - {{ currentLicense.validTo }}</span>
        </el-form-item>
        <el-form-item label="状态：">
          <el-tag :type="getStatusType(currentLicense.status)">
            {{ getStatusText(currentLicense.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建时间：">
          <span>{{ currentLicense.createTime }}</span>
        </el-form-item>
        <el-form-item label="备注：">
          <span>{{ currentLicense.remark || '无' }}</span>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { getLicenseList, generateLicense, renewLicense, enableLicense, disableLicense } from '@/api/admin'
import { getCampusList } from '@/api/campus'
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'

export default {
  name: 'AdminLicense',
  directives: { waves },
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        licenseKey: undefined,
        status: undefined
      },
      campusOptions: [],
      generateVisible: false,
      renewVisible: false,
      detailVisible: false,
      licenseForm: {
        campusId: undefined,
        durationMonths: 12
      },
      renewForm: {
        durationMonths: 12
      },
      currentLicense: {},
      rules: {
        campusId: [{ required: true, message: '请选择校区', trigger: 'change' }],
        durationMonths: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
      },
      renewRules: {
        durationMonths: [{ required: true, message: '请输入续期时长', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.getCampusList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const response = await getLicenseList(this.listQuery)
        this.list = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取许可证列表失败:', error)
        this.$message.error('获取许可证列表失败')
      } finally {
        this.listLoading = false
      }
    },
    async getCampusList() {
      try {
        const response = await getCampusList()
        this.campusOptions = response.data || []
      } catch (error) {
        console.error('获取校区列表失败:', error)
        this.$message.error('获取校区列表失败')
      }
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleGenerate() {
      this.licenseForm = {
        campusId: undefined,
        durationMonths: 12
      }
      this.generateVisible = true
    },
    handleView(row) {
      this.currentLicense = row
      this.detailVisible = true
    },
    handleRenew(row) {
      this.currentLicense = row
      this.renewForm.durationMonths = 12
      this.renewVisible = true
    },
    async handleDisable(row) {
      try {
        await this.$confirm('确认禁用该许可证？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await disableLicense(row.id)
        this.$message.success('禁用成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('禁用许可证失败:', error)
          this.$message.error('禁用许可证失败')
        }
      }
    },
    async handleEnable(row) {
      try {
        await this.$confirm('确认启用该许可证？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await enableLicense(row.id)
        this.$message.success('启用成功')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('启用许可证失败:', error)
          this.$message.error('启用许可证失败')
        }
      }
    },
    handleGenerateSubmit() {
      this.$refs.licenseForm.validate(async (valid) => {
        if (valid) {
          try {
            await generateLicense(this.licenseForm)
            this.$message.success('生成成功')
            this.generateVisible = false
            this.getList()
          } catch (error) {
            console.error('生成许可证失败:', error)
            this.$message.error('生成许可证失败')
          }
        }
      })
    },
    handleRenewSubmit() {
      this.$refs.renewForm.validate(async (valid) => {
        if (valid) {
          try {
            await renewLicense(this.currentLicense.id, this.renewForm.durationMonths)
            this.$message.success('续期成功')
            this.renewVisible = false
            this.getList()
          } catch (error) {
            console.error('续期失败:', error)
            this.$message.error('续期失败')
          }
        }
      })
    },
    getStatusType(status) {
      const types = {
        'ACTIVE': 'success',
        'DISABLED': 'danger',
        'EXPIRED': 'warning'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'ACTIVE': '启用',
        'DISABLED': '禁用',
        'EXPIRED': '已过期'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}

.filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-left: 10px;
}
</style>