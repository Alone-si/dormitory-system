<template>
  <div class="my-leaves-view">
    <div class="page-header">
      <div class="header-left">
        <h2>我的缺寝记录</h2>
        <p class="subtitle">查看和管理您提交的缺寝申请</p>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true" class="submit-btn">
        <Plus :size="18" />
        <span>申请缺寝</span>
      </el-button>
    </div>
    
    <div v-if="leaves.length > 0" class="leaves-list">
      <div v-for="leave in leaves" :key="leave.id" class="glass-card leave-card">
        <div class="card-header">
          <el-tag :type="getStatusColor(leave.status)" size="small">
            {{ getStatusText(leave.status) }}
          </el-tag>
          <span class="date">{{ formatDate(leave.createdAt) }}</span>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <span class="label">类型：</span>
            <span class="type-tag" :class="getTypeColor(leave.type)">{{ getTypeText(leave.type) }}</span>
          </div>
          <div class="info-row">
            <span class="label">时间：</span>
            <span>{{ leave.startDate }} 至 {{ leave.endDate }} ({{ leave.days }}天)</span>
          </div>
          <div class="info-row">
            <span class="label">原因：</span>
            <span>{{ leave.reason }}</span>
          </div>
          <div v-if="leave.adminComment" class="reply-section">
            <div class="reply-label">审批意见：</div>
            <div class="reply-content">{{ leave.adminComment }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <el-empty v-else description="暂无缺寝记录" />
    
    <!-- 申请缺寝对话框 -->
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
              <CalendarClock :size="20" />
            </div>
            <div class="header-text">
              <h3>申请缺寝</h3>
              <p>请填写缺寝信息并提供紧急联系方式</p>
            </div>
          </div>
          <button class="close-btn" @click="dialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <el-form :model="form" label-position="top" class="apple-form">
        <el-form-item label="缺寝类型">
          <el-select v-model="form.type" placeholder="请选择缺寝类型" size="large">
            <el-option label="事假" value="PERSONAL" />
            <el-option label="公假" value="FAMILY" />
            <el-option label="病假" value="SICK" />
            <el-option label="紧急事假" value="EMERGENCY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker 
            v-model="form.startDate" 
            type="date" 
            placeholder="选择开始日期"
            size="large"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker 
            v-model="form.endDate" 
            type="date" 
            placeholder="选择结束日期"
            size="large"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="缺寝原因">
          <el-input 
            v-model="form.reason" 
            type="textarea" 
            :rows="2" 
            placeholder="请详细说明缺寝原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="紧急联系人">
          <el-input 
            v-model="form.emergencyContact" 
            placeholder="联系人姓名" 
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input 
            v-model="form.emergencyPhone" 
            placeholder="联系人电话" 
            size="large"
            clearable
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
import { ref, computed, onMounted } from 'vue'
import { Plus, CalendarClock, X } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { leaveApi } from '../../api/leave'
import { useUserStore } from '../../stores/user'
import type { Leave } from '../../types'

const userStore = useUserStore()
const leaves = ref<Leave[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  type: 'PERSONAL' as any,
  startDate: '',
  endDate: '',
  reason: '',
  emergencyContact: '',
  emergencyPhone: ''
})

const loadLeaves = async () => {
  try {
    leaves.value = await leaveApi.getByStudent(userStore.userInfo!.id)
  } catch (error) {
    ElMessage.error('加载请假记录失败')
  }
}

const handleSubmit = async () => {
  if (!form.value.startDate || !form.value.endDate) {
    ElMessage.warning('请选择请假日期')
    return
  }
  if (!form.value.reason.trim()) {
    ElMessage.warning('请填写缺寝原因')
    return
  }
  
  if (!userStore.userInfo?.room) {
    ElMessage.warning('您还未分配宿舍，无法提交缺寝请假申请。请联系管理员为您分配宿舍。')
    return
  }
  
  const start = new Date(form.value.startDate)
  const end = new Date(form.value.endDate)
  const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  
  if (days < 1) {
    ElMessage.warning('结束日期不能早于开始日期')
    return
  }
  
  if (days > 3) {
    ElMessage.warning('单次请假不能超过3天')
    return
  }
  
  submitting.value = true
  try {
    await leaveApi.create({
      student: { id: userStore.userInfo!.id } as any,
      days,
      ...form.value
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    form.value = {
      type: 'PERSONAL',
      startDate: '',
      endDate: '',
      reason: '',
      emergencyContact: '',
      emergencyPhone: ''
    }
    loadLeaves()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    PERSONAL: '事假',
    FAMILY: '公假',
    SICK: '病假',
    EMERGENCY: '紧急事假',
    OTHER: '其他'
  }
  return map[type] || type
}

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    PERSONAL: 'type-personal',
    FAMILY: 'type-family',
    SICK: 'type-sick',
    EMERGENCY: 'type-emergency',
    OTHER: 'type-other'
  }
  return map[type] || 'type-other'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const getStatusColor = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}

const formatDate = (date: string | undefined) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

onMounted(() => {
  loadLeaves()
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
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.leaves-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.leave-card {
  transition: all 0.3s ease;
}

.leave-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.date {
  font-size: 13px;
  color: #999;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
}

.label {
  color: #999;
  min-width: 60px;
}

.reply-section {
  margin-top: 8px;
  padding: 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
}

.reply-label {
  font-size: 13px;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 8px;
}

.reply-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* 类型标签样式 */
.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.type-personal {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.type-family {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.type-sick {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
}

.type-emergency {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
}

.type-other {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
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
.apple-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.apple-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.apple-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 10px 14px;
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
  padding: 10px 14px;
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

.apple-form :deep(.el-date-editor) {
  width: 100%;
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
