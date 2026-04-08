<template>
  <div class="notices-view">
    <div class="view-header">
      <h2>宿舍通知</h2>
      <button class="create-btn" @click="handleCreate">
        <Plus :size="18" />
        <span>发布通知</span>
      </button>
    </div>
    
    <div class="notices-grid" v-loading="loading">
      <div v-for="notice in notices" :key="notice.id" class="notice-card" :class="{ 'is-pinned': notice.pinned }">
        <div class="card-header">
          <div class="header-main">
            <div class="title-row">
              <span v-if="notice.pinned" class="pin-badge" title="置顶">
                <Pin :size="12" fill="currentColor" />
              </span>
              <h3 class="notice-title" :title="notice.title">{{ notice.title }}</h3>
            </div>
            <div class="tags-row">
              <div class="custom-tag" :class="`tag-${getTypeColor(notice.type)}`">{{ getTypeText(notice.type) }}</div>
              <div class="custom-tag" :class="`tag-${getPriorityColor(notice.priority)}`">{{ getPriorityText(notice.priority) }}</div>
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <p class="notice-content">{{ notice.content }}</p>
        </div>
        
        <div class="card-footer">
          <div class="meta-info">
            <div class="meta-item">
              <Users :size="14" />
              <span class="custom-tag" :class="`tag-${getTargetColor(notice.target)}`">{{ getTargetText(notice.target) }}</span>
            </div>
            <div class="meta-item">
              <Clock :size="14" />
              <span>{{ formatDate(notice.publishedAt) }}</span>
            </div>
            <div v-if="notice.publisher" class="meta-item publisher-item">
              <User :size="14" />
              <span class="publisher-name">{{ notice.publisher.name }}</span>
            </div>
          </div>
          
          <div class="actions">
            <button class="icon-btn edit-btn" @click="handleEdit(notice)" title="编辑">
              <Edit2 :size="16" />
            </button>
            <button class="icon-btn delete-btn" @click="handleDelete(notice)" title="删除">
              <Trash2 :size="16" />
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && notices.length === 0" description="暂无通知" />
    
    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px" class="apple-form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="公告" value="ANNOUNCEMENT" />
            <el-option label="维护通知" value="MAINTENANCE" />
            <el-option label="活动通知" value="EVENT" />
            <el-option label="规章制度" value="REGULATION" />
            <el-option label="紧急通知" value="EMERGENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标对象">
          <el-select v-model="form.target" style="width: 100%">
            <el-option label="全体" value="ALL" />
            <el-option label="男生" value="MALE" />
            <el-option label="女生" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button class="cancel-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="confirm-btn" type="primary" @click="confirmSave" :loading="saving">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Pin, Clock, Users, Edit2, Trash2, User } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi } from '../../api/notice'
import { useUserStore } from '../../stores/user'
import type { Notice } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const notices = ref<Notice[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  title: '',
  content: '',
  type: 'ANNOUNCEMENT' as any,
  priority: 'NORMAL' as any,
  target: 'ALL' as any,
  pinned: false
})

const dialogTitle = computed(() => editingId.value ? '编辑通知' : '发布通知')

const loadNotices = async () => {
  loading.value = true
  try {
    notices.value = await noticeApi.getAll()
  } catch (error) {
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingId.value = null
  form.value = {
    title: '',
    content: '',
    type: 'ANNOUNCEMENT',
    priority: 'NORMAL',
    target: 'ALL',
    pinned: false
  }
  dialogVisible.value = true
}

const handleEdit = (notice: Notice) => {
  editingId.value = notice.id
  form.value = {
    title: notice.title,
    content: notice.content,
    type: notice.type,
    priority: notice.priority,
    target: notice.target || 'ALL',
    pinned: notice.pinned || false
  }
  dialogVisible.value = true
}

const confirmSave = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  saving.value = true
  try {
    const data = {
      ...form.value,
      publisher: { id: userStore.userInfo!.id } as any
    }
    
    if (editingId.value) {
      await noticeApi.update(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await noticeApi.create(data)
      ElMessage.success('发布成功')
    }
    
    dialogVisible.value = false
    loadNotices()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (notice: Notice) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', { 
      type: 'warning',
      customClass: 'apple-message-box'
    })
    await noticeApi.delete(notice.id)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    ANNOUNCEMENT: 'announcement',
    MAINTENANCE: 'maintenance',
    EVENT: 'event',
    REGULATION: 'regulation',
    EMERGENCY: 'emergency'
  }
  return map[type] || ''
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
    LOW: 'low',
    NORMAL: 'normal',
    HIGH: 'high',
    URGENT: 'urgent'
  }
  return map[priority] || ''
}

const getTargetText = (target?: string) => {
  if (!target) return '全体'
  const map: Record<string, string> = {
    ALL: '全体',
    MALE: '男生',
    FEMALE: '女生'
  }
  return map[target] || target
}

const getTargetColor = (target?: string) => {
  if (!target) return 'all'
  const map: Record<string, string> = {
    ALL: 'all',
    MALE: 'male',
    FEMALE: 'female'
  }
  return map[target] || 'all'
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.notices-view {
  padding: 24px;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.view-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.create-btn:active {
  transform: scale(0.96);
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.icon-btn:active {
  transform: scale(0.96);
}

.edit-btn {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.delete-btn {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.notices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.notice-card {
  background: white;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-height: 200px;
}

.notice-card.is-pinned {
  border-color: #dbeafe;
  background: #f8fafc;
}

.notice-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 12px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.pin-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #ef4444;
  color: white;
  border-radius: 4px;
}

.notice-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.tags-row {
  display: flex;
  gap: 8px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 8px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 通知类型颜色 */
.tag-announcement {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-maintenance {
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
  color: white;
}

.tag-event {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-regulation {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  color: white;
}

.tag-emergency {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

/* 优先级颜色 */
.tag-low {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: white;
}

.tag-normal {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-high {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.tag-urgent {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

/* 目标对象颜色 */
.tag-all {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-male {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-female {
  background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%);
  color: white;
}

/* 默认颜色 */
.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.tag-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-info {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.tag- {
  background: linear-gradient(135deg, #e5e7eb 0%, #d1d5db 100%);
  color: #4b5563;
}

.card-body {
  flex: 1;
  margin-bottom: 12px;
}

.notice-content {
  margin: 0;
  font-size: 11px;
  color: #4b5563;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 18px;
  border-top: 1px solid #e5e7eb;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: #9ca3af;
}

.actions {
  display: flex;
  gap: 8px;
}

.mr-1 {
  margin-right: 4px;
}
</style>


<style>
/* Apple风格对话框 - 通知发布 */
.notices-view .el-dialog {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15) !important;
  padding: 0 !important;
}

.notices-view .el-dialog .el-dialog__header {
  padding: 18px 24px !important;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  border-bottom: none !important;
  margin: 0 !important;
  border-radius: 16px 16px 0 0 !important;
}

.notices-view .el-dialog__title {
  font-size: 17px !important;
  font-weight: 600 !important;
  color: white !important;
  letter-spacing: -0.2px !important;
}

.notices-view .el-dialog__headerbtn {
  top: 16px !important;
  right: 20px !important;
  width: 28px !important;
  height: 28px !important;
}

.notices-view .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  opacity: 0.95 !important;
}

.notices-view .el-dialog__headerbtn:hover .el-dialog__close {
  opacity: 1 !important;
}

.notices-view .el-dialog__body {
  padding: 24px !important;
  background: white !important;
}

.notices-view .el-dialog__footer {
  padding: 0 24px 20px !important;
  background: white !important;
  border-top: none !important;
  margin: 0 !important;
}

/* 表单样式 */
.notices-view .apple-form .el-form-item {
  margin-bottom: 16px !important;
}

.notices-view .apple-form .el-form-item__label {
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #1f2937 !important;
  padding-right: 12px !important;
}

/* 输入框样式 */
.notices-view .el-dialog .el-input__wrapper {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.2s ease !important;
  background: #f9fafb !important;
  box-shadow: none !important;
  padding: 8px 12px !important;
}

.notices-view .el-dialog .el-input__wrapper:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

.notices-view .el-dialog .el-input__wrapper.is-focus {
  border-color: #f59e0b !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1) !important;
}

.notices-view .el-dialog .el-input__inner {
  font-size: 14px !important;
  color: #374151 !important;
}

/* 文本域样式 */
.notices-view .el-dialog .el-textarea__inner {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  padding: 10px 12px !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
  transition: all 0.2s ease !important;
  background: #f9fafb !important;
  box-shadow: none !important;
  color: #374151 !important;
}

.notices-view .el-dialog .el-textarea__inner:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

.notices-view .el-dialog .el-textarea__inner:focus {
  border-color: #f59e0b !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1) !important;
  outline: none !important;
}

/* 选择器样式 */
.notices-view .el-dialog .el-select .el-input__wrapper {
  border-radius: 8px !important;
}

/* 开关样式 */
.notices-view .el-dialog .el-switch.is-checked .el-switch__core {
  background-color: #f59e0b !important;
  border-color: #f59e0b !important;
}

/* 按钮容器 */
.dialog-footer-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 对话框按钮 */
.notices-view .el-dialog__footer .el-button {
  padding: 9px 24px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
  border: none !important;
  min-width: 80px !important;
}

.notices-view .el-dialog__footer .cancel-btn {
  background: #f3f4f6 !important;
  color: #6b7280 !important;
  border: 1px solid #e5e7eb !important;
}

.notices-view .el-dialog__footer .cancel-btn:hover {
  background: #e5e7eb !important;
  color: #374151 !important;
}

.notices-view .el-dialog__footer .cancel-btn:active {
  transform: scale(0.98) !important;
}

.notices-view .el-dialog__footer .confirm-btn {
  background: #f59e0b !important;
  color: white !important;
}

.notices-view .el-dialog__footer .confirm-btn:hover {
  background: #d97706 !important;
}

.notices-view .el-dialog__footer .confirm-btn:active {
  transform: scale(0.98) !important;
}

/* 发布人信息样式 */
.publisher-item {
  color: #3b82f6 !important;
}

.publisher-name {
  font-weight: 600;
  color: #3b82f6;
}

/* 加载状态 */
.notices-view .el-dialog .el-button.is-loading {
  opacity: 0.7 !important;
}
</style>
