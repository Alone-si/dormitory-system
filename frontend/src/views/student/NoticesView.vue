<template>
  <div class="notices-view">
    <div class="page-header">
      <div class="header-left">
        <h2>宿舍通知</h2>
        <p class="subtitle">查看最新的宿舍公告和通知信息</p>
      </div>
    </div>
    
    <div v-if="notices.length > 0" class="notices-list">
      <div v-for="notice in notices" :key="notice.id" class="glass-card notice-card" @click="showDetail(notice)">
        <div class="card-header">
          <div class="title-section">
            <Pin v-if="notice.pinned" :size="16" class="pin-icon" />
            <h3>{{ notice.title }}</h3>
          </div>
          <el-tag :type="getPriorityColor(notice.priority)" size="small">
            {{ getPriorityText(notice.priority) }}
          </el-tag>
        </div>
        
        <div class="card-body">
          <div class="notice-meta">
            <el-tag size="small">{{ getTypeText(notice.type) }}</el-tag>
            <span class="date">{{ formatDate(notice.publishedAt) }}</span>
          </div>
          <div class="notice-preview">
            {{ getPreview(notice.content) }}
          </div>
        </div>
      </div>
    </div>
    
    <el-empty v-else description="暂无通知" />
    
    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="selectedNotice?.title" 
      width="600px"
      class="apple-dialog"
    >
      <div class="detail-content">
        <div class="detail-meta">
          <el-tag :type="getPriorityColor(selectedNotice?.priority || '')" size="small">
            {{ getPriorityText(selectedNotice?.priority || '') }}
          </el-tag>
          <el-tag size="small">{{ getTypeText(selectedNotice?.type || '') }}</el-tag>
          <span class="detail-date">{{ formatDate(selectedNotice?.publishedAt) }}</span>
        </div>
        <div class="detail-text">
          {{ selectedNotice?.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Pin } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { noticeApi } from '../../api/notice'
import { useUserStore } from '../../stores/user'
import type { Notice } from '../../types'

const userStore = useUserStore()
const notices = ref<Notice[]>([])
const detailVisible = ref(false)
const selectedNotice = ref<Notice | null>(null)

const showDetail = (notice: Notice) => {
  selectedNotice.value = notice
  detailVisible.value = true
}

const getPreview = (content: string) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

const loadNotices = async () => {
  try {
    const gender = userStore.userInfo?.gender
    if (gender) {
      notices.value = await noticeApi.getByTarget(gender)
    } else {
      notices.value = await noticeApi.getAll()
    }
  } catch (error) {
    ElMessage.error('加载通知失败')
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    ANNOUNCEMENT: '公告',
    MAINTENANCE: '维护通知',
    EVENT: '活动通知',
    REGULATION: '规章制度',
    EMERGENCY: '紧急通知'
  }
  return map[type] || type
}

const getPriorityText = (priority: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    NORMAL: '普通',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority] || priority
}

const getPriorityColor = (priority: string) => {
  const map: Record<string, any> = {
    LOW: 'info',
    NORMAL: '',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[priority] || ''
}

const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.header-left h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.glass-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.notices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-card {
  transition: all 0.3s ease;
  cursor: pointer;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.pin-icon {
  color: #f56c6c;
  flex-shrink: 0;
}

.title-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date {
  font-size: 13px;
  color: #999;
}

.notice-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 详情对话框样式 */
:deep(.apple-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.apple-dialog .el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.apple-dialog .el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

:deep(.apple-dialog .el-dialog__body) {
  padding: 24px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-date {
  font-size: 13px;
  color: #999;
  margin-left: auto;
}

.detail-text {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
