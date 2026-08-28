<template>
  <div class="admins-view">
    <div class="view-header">
      <h2>管理员管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索管理员姓名或用户名"
          :prefix-icon="Search"
          style="width: 240px"
          size="default"
          clearable
        />
        <button class="action-btn primary-btn" @click="showAddDialog">
          <UserPlus :size="16" />
          <span>添加管理员</span>
        </button>
      </div>
    </div>

    <!-- 管理员列表 -->
    <div class="table-container">
      <el-table
        :data="filteredAdmins"
        style="width: 100%"
        :header-cell-style="{ background: '#f9fafb', color: '#374151', fontWeight: '600' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="phone" label="电话号码" min-width="140" />
        <el-table-column prop="adminType" label="管理员类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="row.adminType === 'SUPER_ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.adminType === 'SUPER_ADMIN' ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <button class="table-action-btn edit-btn" @click="showEditDialog(row)">
              <Edit2 :size="14" />
              <span>编辑</span>
            </button>
            <button class="table-action-btn reset-btn" @click="handleResetPassword(row)">
              <Key :size="14" />
              <span>重置密码</span>
            </button>
            <button 
              class="table-action-btn delete-btn" 
              @click="handleDelete(row)"
              :disabled="isCurrentUser(row)"
              :class="{ 'disabled': isCurrentUser(row) }"
              :title="isCurrentUser(row) ? '不能删除自己' : '删除管理员'"
            >
              <Trash2 :size="14" />
              <span>删除</span>
            </button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <UserPlus v-if="dialogMode === 'add'" :size="20" />
              <Edit2 v-else :size="20" />
            </div>
            <div class="header-text">
              <h3>{{ dialogMode === 'add' ? '添加管理员' : '编辑管理员' }}</h3>
              <p>{{ dialogMode === 'add' ? '创建新的管理员账号' : '修改管理员基本信息' }}</p>
            </div>
          </div>
          <button class="close-btn" @click="dialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名（用于登录）" clearable />
        </el-form-item>
        <el-form-item label="电话号码" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话号码" clearable />
        </el-form-item>
        <el-form-item label="管理员类型" prop="adminType">
          <el-select v-model="formData.adminType" placeholder="请选择管理员类型" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="普通管理员" value="NORMAL_ADMIN" />
          </el-select>
          <div style="color: #6b7280; font-size: 12px; margin-top: 4px;">
            超级管理员可管理其他管理员，普通管理员不可
          </div>
        </el-form-item>
        <el-form-item v-if="dialogMode === 'add'" label="初始密码">
          <el-input value="123456" disabled />
          <div style="color: #6b7280; font-size: 12px; margin-top: 4px;">
            默认密码为 123456，创建后可重置
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="dialogVisible = false">取消</button>
          <button class="dialog-btn confirm-btn" @click="handleSubmit">
            {{ dialogMode === 'add' ? '添加' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useAdminsView } from './AdminsView.logic'

const {
  dialogMode,
  dialogVisible,
  Edit2,
  filteredAdmins,
  formatDate,
  formData,
  formRef,
  formRules,
  handleDelete,
  handleResetPassword,
  handleSubmit,
  isCurrentUser,
  Key,
  Search,
  searchQuery,
  showAddDialog,
  showEditDialog,
  Trash2,
  UserPlus,
  X
} = useAdminsView()
</script>

<style src="./AdminsView.global.css"></style>
