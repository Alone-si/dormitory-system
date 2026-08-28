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
import { useNoticesView } from './NoticesView.logic'

const {
  Clock,
  confirmSave,
  dialogTitle,
  dialogVisible,
  Edit2,
  form,
  formatDate,
  getPriorityColor,
  getPriorityText,
  getTargetColor,
  getTargetText,
  getTypeColor,
  getTypeText,
  handleCreate,
  handleDelete,
  handleEdit,
  loading,
  notices,
  Pin,
  Plus,
  saving,
  Trash2,
  User,
  Users
} = useNoticesView()
</script>


<style scoped src="./NoticesView.css"></style>


<style src="./NoticesView.global.css"></style>
