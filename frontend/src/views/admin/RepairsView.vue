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
import { useRepairsView } from './RepairsView.logic'

const {
  changeFilter,
  Clock,
  confirmProcess,
  currentRepair,
  dialogVisible,
  filters,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleComplete,
  handleDeleteRepair,
  handleProcess,
  loading,
  MapPin,
  processDialogVisible,
  processForm,
  repairs,
  showDetail,
  statusFilter
} = useRepairsView()
</script>

<style scoped src="./RepairsView.css"></style>

<style src="./RepairsView.global.css"></style>
