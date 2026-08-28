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
import { useMyRepairsView } from './MyRepairsView.logic'

const {
  dialogVisible,
  form,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeText,
  getUrgencyColor,
  getUrgencyText,
  handleSubmit,
  Plus,
  repairs,
  submitting,
  Wrench,
  X
} = useMyRepairsView()
</script>

<style scoped src="./MyRepairsView.css"></style>
