<template>
  <div class="leaves-view">
    <div class="view-header">
      <h2>缺寝请假</h2>
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
    
    <div class="leaves-grid" v-loading="loading">
      <div v-for="leave in leaves" :key="leave.id" class="leave-card">
        <div class="card-header">
          <div class="student-info">
            <div class="avatar">
              {{ leave.student.name.charAt(0) }}
            </div>
            <div class="student-detail">
              <div class="name-row">
                <span class="name">{{ leave.student.name }}</span>
                <span class="student-id">{{ leave.student.studentId }}</span>
              </div>
              <div class="room-info" v-if="leave.student.room">
                {{ leave.student.room.building.name }} - {{ leave.student.room.roomNumber }}
              </div>
            </div>
          </div>
          <div class="status-badge">
            <div class="custom-tag" :class="`tag-${getStatusColor(leave.status)}`">
              {{ getStatusText(leave.status) }}
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <div class="info-grid">
            <div class="info-col">
              <span class="label">类型</span>
              <div class="custom-tag" :class="getTypeColor(leave.type)">{{ getTypeText(leave.type) }}</div>
            </div>
            <div class="info-col">
              <span class="label">天数</span>
              <span class="days-value">{{ leave.days }} 天</span>
            </div>
          </div>
          
          <div class="time-range">
            <Calendar :size="14" />
            <span>{{ leave.startDate }} 至 {{ leave.endDate }}</span>
          </div>
          
          <div class="reason-box">
            <p class="reason">{{ leave.reason }}</p>
          </div>
        </div>
        
        <div class="card-footer" v-if="leave.status === 'PENDING'">
          <button class="action-btn approve-btn" @click="handleApprove(leave)">
            <Check :size="18" />
            <span>批准</span>
          </button>
          <button class="action-btn reject-btn" @click="handleReject(leave)">
            <X :size="18" />
            <span>拒绝</span>
          </button>
        </div>
        <div class="card-footer static" v-else>
          <div class="processed-info">
            <div class="processed-text">
              审批意见: {{ leave.adminComment || '无' }}
            </div>
            <div v-if="leave.approver" class="approver-info">
              <User :size="14" />
              <span>审批人: {{ leave.approver.name }}</span>
            </div>
          </div>
          <button class="action-btn delete-btn" @click="handleDelete(leave)">
            <Trash2 :size="18" />
            <span>删除</span>
          </button>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && leaves.length === 0" description="暂无请假记录" />
    
    <!-- 审批对话框 -->
    <el-dialog 
      v-model="approvalDialogVisible" 
      :title="approvalTitle" 
      width="440px"
      :class="approvalType === 'approve' ? 'approve-dialog' : 'reject-dialog'"
    >
      <el-form :model="approvalForm" label-width="80px" class="apple-form">
        <el-form-item label="学生">
          <el-input :model-value="currentLeave ? currentLeave.student.name : ''" disabled />
        </el-form-item>
        <el-form-item label="请假原因">
          <el-input :model-value="currentLeave ? currentLeave.reason : ''" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approvalForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button class="cancel-btn" @click="approvalDialogVisible = false">取消</el-button>
          <el-button 
            :class="approvalType === 'approve' ? 'approve-btn' : 'reject-btn'"
            :type="approvalType === 'approve' ? 'success' : 'danger'" 
            @click="confirmApproval" 
            :loading="approving"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Calendar, Check, X, User, Trash2 } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { leaveApi } from '../../api/leave'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import type { Leave } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const statusFilter = ref('ALL')
const leaves = ref<Leave[]>([])
const approvalDialogVisible = ref(false)
const approving = ref(false)

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待审批', value: 'PENDING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const changeFilter = (value: string) => {
  statusFilter.value = value
  loadLeaves()
}
const currentLeave = ref<Leave | null>(null)
const approvalType = ref<'approve' | 'reject'>('approve')
const approvalForm = ref({
  comment: ''
})

const approvalTitle = computed(() => {
  return approvalType.value === 'approve' ? '批准缺寝' : '拒绝缺寝'
})

const loadLeaves = async () => {
  loading.value = true
  try {
    if (statusFilter.value === 'ALL') {
      leaves.value = await leaveApi.getAll()
    } else if (statusFilter.value === 'PENDING') {
      leaves.value = await leaveApi.getPending()
    } else {
      const all = await leaveApi.getAll()
      leaves.value = all.filter(l => l.status === statusFilter.value)
    }
  } catch (error) {
    ElMessage.error('加载请假列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (leave: Leave) => {
  currentLeave.value = leave
  approvalType.value = 'approve'
  approvalForm.value.comment = '同意'
  approvalDialogVisible.value = true
}

const handleReject = (leave: Leave) => {
  currentLeave.value = leave
  approvalType.value = 'reject'
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

const confirmApproval = async () => {
  approving.value = true
  try {
    if (approvalType.value === 'approve') {
      await leaveApi.approve(currentLeave.value!.id, userStore.userInfo!.id, approvalForm.value.comment)
      ElMessage.success('已批准')
    } else {
      await leaveApi.reject(currentLeave.value!.id, userStore.userInfo!.id, approvalForm.value.comment)
      ElMessage.success('已拒绝')
    }
    approvalDialogVisible.value = false
    loadLeaves()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    approving.value = false
  }
}

const handleDelete = async (leave: Leave) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${leave.student.name}" 的请假记录吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/leaves/${leave.id}`)
    ElMessage.success('删除成功')
    loadLeaves()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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

onMounted(() => {
  loadLeaves()
})
</script>

<style scoped>
.leaves-view {
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

.leaves-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.leave-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s ease;
  min-height: 200px;
}

.leave-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.student-detail {
  flex: 1;
  min-width: 0;
}

.status-badge {
  flex-shrink: 0;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 20px;
  flex-shrink: 0;
}

.name-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 1;
  min-width: 0;
}

.student-id {
  font-size: 14px;
  color: #6b7280;
  flex-shrink: 0;
}

.room-info {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.card-body {
  padding: 14px;
  flex: 1;
}

.info-grid {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
}

.days-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 16px;
  background: #f3f4f6;
  padding: 10px 14px;
  border-radius: 8px;
}

.reason-box {
  background: #fff;
}

.reason {
  margin: 0;
  font-size: 15px;
  color: #374151;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.action-btn:active {
  transform: scale(0.96);
}

.approve-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.approve-btn:hover {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
  transform: translateY(-1px);
}

.reject-btn {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.reject-btn:hover {
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  transform: translateY(-1px);
}

.card-footer.static {
  background: #f9fafb;
  color: #6b7280;
  font-size: 14px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

/* 状态颜色 */
.tag-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
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

/* 请假类型颜色 */
.type-personal {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.type-family {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.type-sick {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.type-emergency {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  color: white;
}

.type-other {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: white;
}

.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.mr-1 {
  margin-right: 4px;
}
</style>


<style>
/* Apple风格对话框 - 全局样式 */
body .el-dialog {
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15) !important;
  padding: 0 !important;
}

/* 批准对话框 - 绿色头部 */
body .approve-dialog .el-dialog__header {
  padding: 18px 24px !important;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  border-bottom: none !important;
  margin: 0 !important;
  border-radius: 16px 16px 0 0 !important;
}

/* 拒绝对话框 - 红色头部 */
body .reject-dialog .el-dialog__header {
  padding: 18px 24px !important;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  border-bottom: none !important;
  margin: 0 !important;
  border-radius: 16px 16px 0 0 !important;
}

body .el-dialog__title {
  font-size: 17px !important;
  font-weight: 600 !important;
  color: white !important;
  letter-spacing: -0.2px !important;
}

body .el-dialog__headerbtn {
  top: 16px !important;
  right: 20px !important;
  width: 28px !important;
  height: 28px !important;
}

body .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  opacity: 0.95 !important;
}

body .el-dialog__headerbtn:hover .el-dialog__close {
  opacity: 1 !important;
}

body .el-dialog__body {
  padding: 24px !important;
  background: white !important;
}

body .el-dialog__footer {
  padding: 0 24px 20px !important;
  background: white !important;
  border-top: none !important;
  margin: 0 !important;
}

/* 表单样式 */
body .apple-form .el-form-item {
  margin-bottom: 16px !important;
}

body .apple-form .el-form-item__label {
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #1f2937 !important;
  padding-right: 12px !important;
}

/* 输入框通用样式 */
body .el-dialog .el-input__wrapper {
  border-radius: 8px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.2s ease !important;
  background: #f9fafb !important;
  box-shadow: none !important;
  padding: 8px 12px !important;
}

body .el-dialog .el-input__wrapper:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

body .el-dialog .el-input__wrapper.is-focus {
  border-color: #10b981 !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
}

body .el-dialog .el-input__wrapper.is-disabled {
  background: #f3f4f6 !important;
  border-color: #e5e7eb !important;
}

body .el-dialog .el-input__inner {
  font-size: 14px !important;
  color: #374151 !important;
}

body .el-dialog .el-input__inner::placeholder {
  color: #9ca3af !important;
}

/* 文本域样式 */
body .el-dialog .el-textarea__inner {
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

body .el-dialog .el-textarea__inner:hover {
  border-color: #d1d5db !important;
  background: white !important;
}

body .el-dialog .el-textarea__inner:focus {
  border-color: #10b981 !important;
  background: white !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
  outline: none !important;
}

body .el-dialog .el-textarea__inner.is-disabled {
  background: #f3f4f6 !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
}

body .el-dialog .el-textarea__inner::placeholder {
  color: #9ca3af !important;
}

/* 按钮容器 */
.dialog-footer-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 对话框按钮 */
body .el-dialog__footer .el-button {
  padding: 9px 24px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
  border: none !important;
  min-width: 80px !important;
}

body .el-dialog__footer .cancel-btn {
  background: #f3f4f6 !important;
  color: #6b7280 !important;
  border: 1px solid #e5e7eb !important;
}

body .el-dialog__footer .cancel-btn:hover {
  background: #e5e7eb !important;
  color: #374151 !important;
}

body .el-dialog__footer .cancel-btn:active {
  transform: scale(0.98) !important;
}

body .el-dialog__footer .approve-btn {
  background: #10b981 !important;
  color: white !important;
}

body .el-dialog__footer .approve-btn:hover {
  background: #059669 !important;
}

body .el-dialog__footer .approve-btn:active {
  transform: scale(0.98) !important;
}

body .el-dialog__footer .reject-btn {
  background: #ef4444 !important;
  color: white !important;
}

body .el-dialog__footer .reject-btn:hover {
  background: #dc2626 !important;
}

body .el-dialog__footer .reject-btn:active {
  transform: scale(0.98) !important;
}

/* 审批人信息样式 */
.processed-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.processed-text {
  color: #6b7280;
  font-size: 13px;
}

.approver-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #3b82f6;
  font-size: 13px;
  font-weight: 600;
}

/* 加载状态 */
body .el-dialog .el-button.is-loading {
  opacity: 0.7 !important;
}
</style>
