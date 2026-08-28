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
import { useLeavesView } from './LeavesView.logic'

const {
  approvalDialogVisible,
  approvalForm,
  approvalTitle,
  approvalType,
  approving,
  Calendar,
  changeFilter,
  Check,
  confirmApproval,
  currentLeave,
  filters,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleApprove,
  handleDelete,
  handleReject,
  leaves,
  loading,
  statusFilter,
  Trash2,
  User,
  X
} = useLeavesView()
</script>

<style scoped src="./LeavesView.css"></style>


<style src="./LeavesView.global.css"></style>
