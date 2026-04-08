<template>
  <div class="repairs-view">
    <div class="view-header">
      <h2>报修处理</h2>
      <div class="filter-buttons">
        <button 
          v-for="filter in filters" 
          :key="filter.value"
          class="filter-btn"
          :class="{ active: statusFilter === filter.value }"
          @click="changeFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>
    
    <div class="repairs-grid" v-loading="loading">
      <div v-for="repair in repairs" :key="repair.id" class="repair-card">
        <div class="card-header">
          <div class="header-top">
            <div class="custom-tag" :class="`tag-${getTypeColor(repair.type)}`">
              {{ getTypeText(repair.type) }}
            </div>
            <div class="custom-tag" :class="`tag-${getStatusColor(repair.status)}`">
              {{ getStatusText(repair.status) }}
            </div>
          </div>
          <div class="time-info">
            <Clock :size="14" />
            <span>{{ formatDate(repair.createdAt) }}</span>
          </div>
        </div>
        
        <div class="card-body">
          <div class="description-box">
            <p class="description">{{ repair.description }}</p>
          </div>
          
          <div class="location-info">
            <MapPin :size="14" />
            <span>{{ repair.student.room?.building.name }} - {{ repair.student.room?.roomNumber }}</span>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="student-info">
            <div class="avatar-placeholder">
              <img v-if="repair.student.avatar" :src="repair.student.avatar" :alt="repair.student.name" class="student-avatar-image" />
              <span v-else class="student-avatar-text">{{ repair.student.name.charAt(0) }}</span>
            </div>
            <span class="student-name">{{ repair.student.name }}</span>
          </div>
          
          <div class="actions">
            <button 
              v-if="repair.status === 'PENDING'"
              class="action-btn primary-btn"
              @click="handleProcess(repair)"
            >
              处理
            </button>
            <button 
              v-if="repair.status === 'IN_PROGRESS'"
              class="action-btn success-btn"
              @click="handleComplete(repair)"
            >
              完成
            </button>
            <button class="action-btn detail-btn" @click="showDetail(repair)">详情</button>
          </div>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && repairs.length === 0" description="暂无报修记录" />
    
    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="报修详情" width="360px">
      <div v-if="currentRepair" class="detail-dialog-content">
        <!-- 状态和类型 -->
        <div class="detail-header">
          <div class="header-item">
            <span class="header-label">报修类型</span>
            <div class="custom-tag" :class="`tag-${getTypeColor(currentRepair.type)}`">
              {{ getTypeText(currentRepair.type) }}
            </div>
          </div>
          <div class="header-item">
            <span class="header-label">当前状态</span>
            <div class="custom-tag" :class="`tag-${getStatusColor(currentRepair.status)}`">
              {{ getStatusText(currentRepair.status) }}
            </div>
          </div>
        </div>

        <!-- 问题描述 -->
        <div class="detail-section">
          <div class="section-title">问题描述</div>
          <div class="section-content description-text">{{ currentRepair.description }}</div>
        </div>

        <!-- 报修信息 -->
        <div class="detail-section">
          <div class="section-title">报修信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">提交人</span>
              <span class="info-value">{{ currentRepair.student.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学号</span>
              <span class="info-value">{{ currentRepair.student.studentId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">宿舍位置</span>
              <span class="info-value">{{ currentRepair.student.room?.building.name }} - {{ currentRepair.student.room?.roomNumber }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ currentRepair.student.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">提交时间</span>
              <span class="info-value">{{ formatDate(currentRepair.createdAt) }}</span>
            </div>
            <div v-if="currentRepair.handler" class="info-item">
              <span class="info-label">处理人</span>
              <span class="info-value handler-name">{{ currentRepair.handler.name }}</span>
            </div>
            <div v-if="currentRepair.handledAt" class="info-item">
              <span class="info-label">处理时间</span>
              <span class="info-value">{{ formatDate(currentRepair.handledAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 处理意见（如果有） -->
        <div v-if="currentRepair.adminReply" class="detail-section reply-section">
          <div class="section-title">处理意见</div>
          <div class="section-content">{{ currentRepair.adminReply }}</div>
          <div v-if="currentRepair.handler" class="handler-signature">
            —— {{ currentRepair.handler.name }}
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button class="delete-btn" type="danger" @click="handleDeleteRepair">删除报修</el-button>
          <el-button class="cancel-btn" @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="报修处理" width="440px">
      <el-form v-if="currentRepair" label-width="80px" class="apple-form">
        <el-form-item label="报修类型">
          <el-input :model-value="getTypeText(currentRepair.type)" disabled />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input :model-value="currentRepair.description" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="提交人">
          <el-input :model-value="currentRepair.student.name" disabled />
        </el-form-item>
        <el-form-item label="宿舍位置">
          <el-input :model-value="`${currentRepair.student.room?.building.name}-${currentRepair.student.room?.roomNumber}`" disabled />
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input
            v-model="processForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入处理意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button class="cancel-btn" @click="processDialogVisible = false">取消</el-button>
          <el-button class="process-btn" type="primary" @click="confirmProcess">确认处理</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Clock, MapPin } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { repairApi } from '../../api/repair'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import type { Repair } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const statusFilter = ref('ALL')
const repairs = ref<Repair[]>([])
const dialogVisible = ref(false)
const currentRepair = ref<Repair | null>(null)
const processDialogVisible = ref(false)
const processForm = ref({
  reply: ''
})

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' }
]

const changeFilter = (value: string) => {
  statusFilter.value = value
  loadRepairs()
}

const loadRepairs = async () => {
  loading.value = true
  try {
    if (statusFilter.value === 'ALL') {
      repairs.value = await repairApi.getAll()
    } else {
      const all = await repairApi.getAll()
      repairs.value = all.filter(r => r.status === statusFilter.value)
    }
  } catch (error) {
    ElMessage.error('加载报修列表失败')
  } finally {
    loading.value = false
  }
}

const handleProcess = (repair: Repair) => {
  currentRepair.value = repair
  processForm.value.reply = ''
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  if (!currentRepair.value) return
  
  try {
    await repairApi.updateStatus(
      currentRepair.value.id, 
      'IN_PROGRESS' as any, 
      userStore.userInfo?.id, 
      processForm.value.reply || undefined
    )
    ElMessage.success('已开始处理')
    processDialogVisible.value = false
    loadRepairs()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComplete = async (repair: Repair) => {
  try {
    await ElMessageBox.confirm('确定该报修已维修完成吗？', '确认完成', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await repairApi.updateStatus(repair.id, 'COMPLETED' as any)
    ElMessage.success('已完成维修')
    loadRepairs()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const showDetail = (repair: Repair) => {
  currentRepair.value = repair
  dialogVisible.value = true
}

const handleDeleteRepair = async () => {
  if (!currentRepair.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除这条报修记录吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/repairs/${currentRepair.value.id}`)
    ElMessage.success('删除成功')
    dialogVisible.value = false
    loadRepairs()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    ELECTRICAL: '电路问题',
    PLUMBING: '水管问题',
    FURNITURE: '家具损坏',
    DOOR_WINDOW: '门窗问题',
    NETWORK: '网络问题',
    OTHER: '其他'
  }
  return map[type] || type
}

const getTypeColor = (type: string) => {
  const map: Record<string, any> = {
    ELECTRICAL: 'warning',
    PLUMBING: 'primary',
    FURNITURE: 'success',
    DOOR_WINDOW: 'info',
    NETWORK: 'danger',
    OTHER: 'info'
  }
  return map[type] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '处理中',
    COMPLETED: '已完成',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const getStatusColor = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRepairs()
})
</script>

<style scoped>
.repairs-view {
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

.filter-buttons {
  display: flex;
  gap: 12px;
  background: white;
  padding: 6px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #6b7280;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.filter-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.filter-btn.active {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.filter-btn.active:hover {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.repairs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.repair-card {
  background: white;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  min-height: 200px;
  overflow: hidden;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 500;
}

.card-body {
  flex: 1;
  margin-bottom: 16px;
}

.description-box {
  background: #f8fafc;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
  border-left: 3px solid #667eea;
}

.description {
  font-size: 13px;
  color: #374151;
  line-height: 1.6;
  margin: 0;
  word-break: break-word;
}

.repair-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 12px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #e5e7eb;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  font-size: 14px;
  overflow: hidden;
  flex-shrink: 0;
}

.student-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.student-avatar-text {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.student-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

.actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  white-space: nowrap;
}

.action-btn:active {
  transform: scale(0.96);
}

.primary-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.primary-btn:hover {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.success-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.success-btn:hover {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
  transform: translateY(-1px);
}

.detail-btn {
  background: #f3f4f6;
  color: #6b7280;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.detail-btn:hover {
  background: #e5e7eb;
  color: #374151;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.detail-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 8px;
}

.header-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-label {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 类型颜色 */
.tag-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.tag-info {
  background: linear-gradient(135deg, #a5b4fc 0%, #818cf8 100%);
  color: white;
}

.detail-section {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}

.section-content {
  font-size: 12px;
  color: #1f2937;
  line-height: 1.5;
}

.description-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 10px;
  color: #6b7280;
  font-weight: 500;
}

.info-value {
  font-size: 12px;
  color: #1f2937;
  font-weight: 500;
}

.reply-section {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-left: 4px solid #3b82f6;
}

.reply-section .section-title {
  color: #1e40af;
  border-bottom-color: #93c5fd;
}

.process-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.process-info {
  background: #f9fafb;
  padding: 14px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.info-label {
  min-width: 70px;
  color: #6b7280;
  font-weight: 500;
  flex-shrink: 0;
}

.info-text {
  flex: 1;
  color: #374151;
  line-height: 1.5;
}

.process-form {
  margin-top: 0;
}
</style>

<style>
/* Apple风格对话框 - 报修处理 */
.repairs-view .el-dialog {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15) !important;
  padding: 0 !important;
}

.repairs-view .el-dialog .el-dialog__header {
  padding: 18px 24px !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border-bottom: none !important;
  margin: 0 !important;
  border-radius: 16px 16px 0 0 !important;
}

.repairs-view .el-dialog__title {
  font-size: 17px !important;
  font-weight: 600 !important;
  color: white !important;
  letter-spacing: -0.2px !important;
}

.repairs-view .el-dialog__headerbtn {
  top: 16px !important;
  right: 20px !important;
  width: 28px !important;
  height: 28px !important;
}

.repairs-view .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  opacity: 0.95 !important;
}

.repairs-view .el-dialog__headerbtn:hover .el-dialog__close {
  opacity: 1 !important;
}

.repairs-view .el-dialog__body {
  padding: 24px !important;
  background: white !important;
}

.repairs-view .el-dialog__footer {
  padding: 0 24px 20px !important;
  background: white !important;
  border-top: none !important;
  margin: 0 !important;
}

/* 表单样式 */
.repairs-view .apple-form .el-form-item {
  margin-bottom: 16px !important;
}

.repairs-view .apple-form .el-form-item__label {
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #1f2937 !important;
  padding-right: 12px !important;
}

/* 输入框通用样式 */
.repairs-view .el-dialog .el-input__wrapper {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.2s ease !important;
  background: #f9fafb !important;
  box-shadow: none !important;
  padding: 8px 12px !important;
}

.repairs-view .el-dialog .el-input__wrapper:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

.repairs-view .el-dialog .el-input__wrapper.is-focus {
  border-color: #3b82f6 !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.repairs-view .el-dialog .el-input__wrapper.is-disabled {
  background: #f3f4f6 !important;
  border-color: #e5e7eb !important;
}

.repairs-view .el-dialog .el-input__inner {
  font-size: 14px !important;
  color: #374151 !important;
}

.repairs-view .el-dialog .el-input__inner::placeholder {
  color: #9ca3af !important;
}

/* 文本域样式 */
.repairs-view .el-dialog .el-textarea__inner {
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

.repairs-view .el-dialog .el-textarea__inner:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

.repairs-view .el-dialog .el-textarea__inner:focus {
  border-color: #3b82f6 !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
  outline: none !important;
}

.repairs-view .el-dialog .el-textarea__inner.is-disabled {
  background: #f3f4f6 !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
}

.repairs-view .el-dialog .el-textarea__inner::placeholder {
  color: #9ca3af !important;
}

/* 按钮容器 */
.dialog-footer-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 对话框按钮 */
.repairs-view .el-dialog__footer .el-button {
  padding: 9px 24px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
  border: none !important;
  min-width: 80px !important;
}

.repairs-view .el-dialog__footer .cancel-btn {
  background: #f3f4f6 !important;
  color: #6b7280 !important;
  border: 1px solid #e5e7eb !important;
}

.repairs-view .el-dialog__footer .cancel-btn:hover {
  background: #e5e7eb !important;
  color: #374151 !important;
}

.repairs-view .el-dialog__footer .cancel-btn:active {
  transform: scale(0.98) !important;
}

.repairs-view .el-dialog__footer .process-btn {
  background: #3b82f6 !important;
  color: white !important;
}

.repairs-view .el-dialog__footer .process-btn:hover {
  background: #2563eb !important;
}

.repairs-view .el-dialog__footer .process-btn:active {
  transform: scale(0.98) !important;
}

/* 处理人信息样式 */
.handler-name {
  color: #3b82f6 !important;
  font-weight: 600 !important;
}

.handler-signature {
  margin-top: 12px;
  text-align: right;
  color: #6b7280;
  font-size: 13px;
  font-style: italic;
}

/* 加载状态 */
.repairs-view .el-dialog .el-button.is-loading {
  opacity: 0.7 !important;
}
</style>
