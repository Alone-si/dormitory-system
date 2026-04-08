<template>
  <div class="my-repairs-view">
    <div class="page-header">
      <div class="header-left">
        <h2>我的报修记录</h2>
        <p class="subtitle">查看和管理您提交的报修申请</p>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true" class="submit-btn">
        <Plus :size="18" />
        <span>提交报修</span>
      </el-button>
    </div>
    
    <div v-if="repairs.length > 0" class="repairs-list">
      <div v-for="repair in repairs" :key="repair.id" class="glass-card repair-card">
        <div class="card-header">
          <el-tag :type="getStatusColor(repair.status)" size="small">
            {{ getStatusText(repair.status) }}
          </el-tag>
          <span class="date">{{ repair.createdAt ? formatDate(repair.createdAt) : '-' }}</span>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <span class="label">类型：</span>
            <el-tag size="small">{{ getTypeText(repair.type) }}</el-tag>
          </div>
          <div class="info-row" v-if="repair.room">
            <span class="label">位置：</span>
            <span>{{ repair.room.building?.name || '未知楼栋' }} - {{ repair.room.roomNumber || '未知房间' }}</span>
          </div>
          <div class="info-row" v-if="repair.location">
            <span class="label">具体位置：</span>
            <span>{{ repair.location }}</span>
          </div>
          <div class="info-row">
            <span class="label">描述：</span>
            <span>{{ repair.description }}</span>
          </div>
          <div class="info-row" v-if="repair.urgency">
            <span class="label">紧急程度：</span>
            <el-tag :type="getUrgencyColor(repair.urgency)" size="small">
              {{ getUrgencyText(repair.urgency) }}
            </el-tag>
          </div>
          <div v-if="repair.adminReply" class="reply-section">
            <div class="reply-label">处理意见：</div>
            <div class="reply-content">{{ repair.adminReply }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <el-empty v-else description="暂无报修记录" />
    
    <!-- 提交报修对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      width="420px" 
      :close-on-click-modal="false"
      :show-close="false"
      class="apple-dialog"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <Wrench :size="20" />
            </div>
            <div class="header-text">
              <h3>提交报修</h3>
              <p>请详细描述需要维修的问题</p>
            </div>
          </div>
          <button class="close-btn" @click="dialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <el-form :model="form" label-position="top" class="apple-form">
        <el-form-item label="报修类型">
          <el-select v-model="form.type" placeholder="请选择报修类型" size="large">
            <el-option label="电路问题" value="ELECTRICAL" />
            <el-option label="水管问题" value="PLUMBING" />
            <el-option label="家具损坏" value="FURNITURE" />
            <el-option label="门窗问题" value="DOOR_WINDOW" />
            <el-option label="网络问题" value="NETWORK" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="form.urgency" placeholder="请选择紧急程度" size="large">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="具体位置">
          <el-input 
            v-model="form.location" 
            placeholder="如：宿舍靠窗位置" 
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述问题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large" class="submit-btn">
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Wrench, X } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { repairApi } from '../../api/repair'
import { useUserStore } from '../../stores/user'
import type { Repair } from '../../types'

const userStore = useUserStore()
const repairs = ref<Repair[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  type: 'ELECTRICAL' as any,
  urgency: 'MEDIUM' as any,
  location: '',
  description: ''
})

const loadRepairs = async () => {
  try {
    const data = await repairApi.getByStudent(userStore.userInfo!.id)
    repairs.value = data
    console.log('Loaded repairs:', data)
  } catch (error) {
    console.error('Failed to load repairs:', error)
    ElMessage.error('加载报修记录失败')
  }
}

const handleSubmit = async () => {
  if (!form.value.description.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }
  
  // 检查是否已分配宿舍
  if (!userStore.userInfo?.room) {
    ElMessage.warning('您还未分配宿舍，无法提交报修申请。请联系管理员为您分配宿舍。')
    return
  }
  
  submitting.value = true
  try {
    await repairApi.create({
      student: { id: userStore.userInfo!.id } as any,
      room: userStore.userInfo!.room!,
      ...form.value
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    form.value = {
      type: 'ELECTRICAL',
      urgency: 'MEDIUM',
      location: '',
      description: ''
    }
    loadRepairs()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
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
  return map[status] || ''
}

const getUrgencyText = (urgency: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[urgency] || urgency
}

const getUrgencyColor = (urgency: string) => {
  const map: Record<string, any> = {
    LOW: 'info',
    MEDIUM: '',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[urgency] || ''
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRepairs()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.glass-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}

.repairs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.repair-card {
  transition: all 0.3s ease;
}

.repair-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.date {
  font-size: 11px;
  color: #999;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
}

.label {
  color: #999;
  min-width: 50px;
  font-size: 11px;
}

.reply-section {
  margin-top: 6px;
  padding: 10px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 6px;
}

.reply-label {
  font-size: 11px;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 6px;
}

.reply-content {
  font-size: 12px;
  color: #333;
  line-height: 1.5;
}

/* 苹果风格对话框 */
:deep(.apple-dialog) {
  border-radius: 16px;
  overflow: hidden;
  padding: 0 !important;
}

:deep(.apple-dialog .el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.apple-dialog .el-dialog__body) {
  padding: 24px !important;
  margin: 0 !important;
}

:deep(.apple-dialog .el-dialog__footer) {
  padding: 0 !important;
  margin: 0 !important;
}

.custom-dialog-header {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  padding: 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.custom-dialog-header .header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.custom-dialog-header .header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.custom-dialog-header .header-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0 0 4px 0;
}

.custom-dialog-header .header-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.custom-dialog-header .close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.custom-dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 表单样式 */
.apple-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.apple-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 12px 16px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.2s;
}

.apple-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.apple-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #667eea inset;
}

.apple-form :deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

.apple-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
  font-family: inherit;
}

.apple-form :deep(.el-textarea__inner:hover) {
  border-color: #d1d5db;
}

.apple-form :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 按钮样式 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 16px 16px;
}

.dialog-footer .el-button {
  min-width: 100px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f5f5f5;
  border: none;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
  transform: translateY(-1px);
}

.dialog-footer .submit-btn {
  background: #3b82f6;
  border: none;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.dialog-footer .submit-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}
</style>
