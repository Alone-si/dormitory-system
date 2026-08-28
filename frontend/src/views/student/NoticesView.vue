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
import { useNoticesView } from './NoticesView.logic'

const {
  detailVisible,
  formatDate,
  getPreview,
  getPriorityColor,
  getPriorityText,
  getTypeText,
  notices,
  Pin,
  selectedNotice,
  showDetail
} = useNoticesView()
</script>

<style scoped src="./NoticesView.css"></style>
