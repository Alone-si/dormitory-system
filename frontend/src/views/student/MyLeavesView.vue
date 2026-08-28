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
import { useMyLeavesView } from './MyLeavesView.logic'

const {
  CalendarClock,
  dialogVisible,
  disabledDate,
  form,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleSubmit,
  leaves,
  Plus,
  submitting,
  X
} = useMyLeavesView()
</script>

<style scoped src="./MyLeavesView.css"></style>
