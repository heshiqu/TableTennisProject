<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>系统消息</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="markAllRead">全部已读</el-button>
          <el-button type="text" @click="loadMessages">刷新</el-button>
        </div>
      </div>

      <!-- 消息筛选 -->
      <div class="message-filter">
        <el-radio-group v-model="messageType" @change="handleTypeChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
          <el-radio-button label="course">课程</el-radio-button>
          <el-radio-button label="tournament">比赛</el-radio-button>
          <el-radio-button label="system">系统</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" v-loading="loading">
        <div v-if="messages.length > 0">
          <div 
            v-for="message in messages" 
            :key="message.id" 
            class="message-item"
            :class="{ 'unread': !message.read }"
            @click="handleMessageDetail(message)">
            <div class="message-icon">
              <i :class="getMessageIcon(message.type)"></i>
            </div>
            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ message.createTime | formatTime }}</div>
            </div>
            <div class="message-actions">
              <el-button 
                type="text" 
                size="mini" 
                @click.stop="markAsRead(message)"
                v-if="!message.read">
                标为已读
              </el-button>
              <el-button 
                type="text" 
                size="mini" 
                @click.stop="deleteMessage(message)"
                class="delete-btn">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无消息"></el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page.current"
        :page-sizes="[10, 20, 50]"
        :page-size="page.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="text-align: center; margin-top: 20px;">
      </el-pagination>
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog title="消息详情" :visible.sync="detailDialogVisible" width="50%">
      <div v-if="selectedMessage">
        <div class="message-detail">
          <div class="detail-header">
            <h3>{{ selectedMessage.title }}</h3>
            <div class="detail-time">{{ selectedMessage.createTime | formatTime }}</div>
          </div>
          <div class="detail-content">
            {{ selectedMessage.content }}
          </div>
          <div v-if="selectedMessage.extraData" class="detail-extra">
            <el-divider></el-divider>
            <h4>相关信息</h4>
            <div v-html="formatExtraData(selectedMessage.extraData)"></div>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="markAsRead(selectedMessage)" v-if="!selectedMessage.read">标记已读</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMessages, markMessageRead, markAllMessagesRead, deleteMessage } from '@/api/system'

export default {
  name: 'StudentMessages',
  data() {
    return {
      messages: [],
      selectedMessage: null,
      messageType: 'all',
      detailDialogVisible: false,
      loading: false,
      page: {
        current: 1,
        size: 20
      },
      total: 0,
      pollingInterval: null
    }
  },
  filters: {
    formatTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60 * 1000) {
        return '刚刚'
      } else if (diff < 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 1000)) + '分钟前'
      } else if (diff < 24 * 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
      } else {
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
    }
  },
  created() {
    this.loadMessages()
    this.startPolling()
  },
  beforeDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
    }
  },
  methods: {
    async loadMessages() {
      this.loading = true
      try {
        const params = {
          type: this.messageType === 'all' ? '' : this.messageType.toUpperCase(),
          current: this.page.current,
          size: this.page.size
        }
        
        const response = await getMessages(params)
        this.messages = response.data.records || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取消息失败:', error)
      } finally {
        this.loading = false
      }
    },
    getMessageIcon(type) {
      const iconMap = {
        'COURSE': 'el-icon-time',
        'TOURNAMENT': 'el-icon-trophy',
        'SYSTEM': 'el-icon-message',
        'PAYMENT': 'el-icon-money',
        'COACH': 'el-icon-user'
      }
      return iconMap[type] || 'el-icon-message'
    },
    handleTypeChange() {
      this.page.current = 1
      this.loadMessages()
    },
    handleMessageDetail(message) {
      this.selectedMessage = message
      this.detailDialogVisible = true
      
      if (!message.read) {
        this.markAsRead(message)
      }
    },
    async markAsRead(message) {
      try {
        await markMessageRead(message.id)
        message.read = true
        this.$message.success('已标记为已读')
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },
    async markAllRead() {
      try {
        await this.$confirm('确认将所有消息标记为已读吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        await markAllMessagesRead()
        this.messages.forEach(msg => msg.read = true)
        this.$message.success('全部标记为已读')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('标记全部已读失败:', error)
        }
      }
    },
    async deleteMessage(message) {
      try {
        await this.$confirm('确认删除该消息吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteMessage(message.id)
        this.loadMessages()
        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除消息失败:', error)
        }
      }
    },
    formatExtraData(extraData) {
      if (typeof extraData === 'string') {
        try {
          const data = JSON.parse(extraData)
          return this.renderExtraData(data)
        } catch (e) {
          return extraData
        }
      }
      return this.renderExtraData(extraData)
    },
    renderExtraData(data) {
      let html = '<div>'
      
      if (data.courseInfo) {
        html += `<p><strong>课程信息：</strong>${data.courseInfo}</p>`
      }
      
      if (data.coachInfo) {
        html += `<p><strong>教练信息：</strong>${data.coachInfo}</p>`
      }
      
      if (data.tournamentInfo) {
        html += `<p><strong>比赛信息：</strong>${data.tournamentInfo}</p>`
      }
      
      if (data.actionUrl) {
        html += `<p><a href="${data.actionUrl}" target="_blank" class="el-link el-link--primary">查看详情</a></p>`
      }
      
      html += '</div>'
      return html
    },
    handleSizeChange(val) {
      this.page.size = val
      this.loadMessages()
    },
    handleCurrentChange(val) {
      this.page.current = val
      this.loadMessages()
    },
    startPolling() {
      // 每30秒轮询一次新消息
      this.pollingInterval = setInterval(() => {
        this.loadMessages()
      }, 30000)
    }
  }
}
</script>

<style scoped>
.message-filter {
  margin-bottom: 20px;
  text-align: center;
}

.message-list {
  min-height: 400px;
}

.message-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: #f5f5f5;
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.message-icon i {
  color: white;
  font-size: 18px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.message-item.unread .message-title {
  color: #409EFF;
}

.message-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  color: #999;
  font-size: 12px;
}

.message-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.delete-btn {
  color: #F56C6C;
}

.message-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.detail-header h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-time {
  color: #999;
  font-size: 14px;
}

.detail-content {
  line-height: 1.6;
  color: #666;
  margin-bottom: 20px;
}

.detail-extra {
  margin-top: 20px;
}

.detail-extra h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.detail-extra p {
  margin: 10px 0;
  color: #666;
}
</style>