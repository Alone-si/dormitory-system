<template>
  <div class="profile-view">
    <div class="profile-header">
      <h2>个人设置</h2>
    </div>

    <div class="profile-content">
      <!-- 头像设置 -->
      <div class="profile-card">
        <div class="card-header">
          <h3>头像设置</h3>
        </div>
        <div class="avatar-section">
          <div class="current-avatar">
            <div class="avatar-display" v-if="userInfo.avatar">
              <img :src="userInfo.avatar" :alt="userInfo.name" />
            </div>
            <div class="avatar-display default-avatar" v-else>
              {{ userInfo.name ? userInfo.name.charAt(0) : '?' }}
            </div>
          </div>
          <div class="avatar-actions">
            <input 
              type="file" 
              ref="avatarInput" 
              accept="image/jpeg,image/jpg,image/png,image/webp" 
              @change="handleAvatarChange"
              style="display: none"
            />
            <el-button type="primary" @click="() => avatarInput?.click()">
              <el-icon><Upload /></el-icon>
              上传头像
            </el-button>
            <el-button v-if="userInfo.avatar" @click="removeAvatar">
              <el-icon><Delete /></el-icon>
              移除头像
            </el-button>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="profile-card">
        <div class="card-header">
          <h3>基本信息</h3>
        </div>
        <div class="info-section">
          <div class="info-item">
            <label>学号</label>
            <span class="readonly">{{ userInfo.studentId }}</span>
          </div>
          <div class="info-item">
            <label>真实姓名</label>
            <span class="readonly">{{ userInfo.name }}</span>
          </div>
          <div class="info-item">
            <label>用户名</label>
            <div class="editable-field">
              <el-input 
                v-if="editingDisplayName" 
                v-model="editForm.displayName" 
                @blur="saveDisplayName"
                @keyup.enter="saveDisplayName"
                ref="displayNameInput"
                placeholder="个性化用户名"
              />
              <span v-else @click="startEditDisplayName" class="editable-text">
                {{ userInfo.displayName || `默认：${userInfo.studentId}` }}
                <el-icon class="edit-icon"><Edit /></el-icon>
              </span>
            </div>
          </div>
          <div class="info-item">
            <label>性别</label>
            <span class="readonly">{{ userInfo.gender === 'MALE' ? '男' : '女' }}</span>
          </div>
          <div class="info-item">
            <label>班级</label>
            <span class="readonly">{{ userInfo.className || '未分班' }}</span>
          </div>
          <div class="info-item">
            <label>电话</label>
            <div class="editable-field">
              <el-input 
                v-if="editingPhone" 
                v-model="editForm.phone" 
                @blur="savePhone"
                @keyup.enter="savePhone"
                ref="phoneInput"
              />
              <span v-else @click="startEditPhone" class="editable-text">
                {{ userInfo.phone || '未设置' }}
                <el-icon class="edit-icon"><Edit /></el-icon>
              </span>
            </div>
          </div>
          <div class="info-item">
            <label>邮箱</label>
            <div class="editable-field">
              <el-input 
                v-if="editingEmail" 
                v-model="editForm.email" 
                @blur="saveEmail"
                @keyup.enter="saveEmail"
                ref="emailInput"
                placeholder="请输入邮箱地址"
                type="email"
              />
              <span v-else @click="startEditEmail" class="editable-text">
                {{ userInfo.email || '未设置' }}
                <el-icon class="edit-icon"><Edit /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 账户安全 -->
      <div class="profile-card">
        <div class="card-header">
          <h3>账户安全</h3>
        </div>
        <div class="security-section">
          <div class="security-item">
            <div class="security-info">
              <h4>登录密码</h4>
              <p>定期更换密码可以提高账户安全性</p>
            </div>
            <el-button @click="showChangePassword = true">修改密码</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showChangePassword" 
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <Lock :size="20" />
            </div>
            <div class="header-text">
              <h3>修改密码</h3>
              <p>为了账户安全，请输入当前密码</p>
            </div>
          </div>
          <button class="close-btn" @click="showChangePassword = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      
      <div class="password-dialog-content">
        
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" class="password-form">
          <el-form-item prop="currentPassword">
            <el-input 
              v-model="passwordForm.currentPassword" 
              type="password" 
              show-password
              placeholder="请输入当前密码"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              show-password
              placeholder="请输入新密码"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              show-password
              placeholder="请再次输入新密码"
              size="large"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showChangePassword = false" size="large">取消</el-button>
          <el-button type="primary" @click="changePassword" :loading="passwordLoading" size="large">
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useProfileView } from './ProfileView.logic'

const {
  avatarInput,
  changePassword,
  Delete,
  displayNameInput,
  Edit,
  editForm,
  editingDisplayName,
  editingEmail,
  editingPhone,
  emailInput,
  handleAvatarChange,
  Lock,
  passwordForm,
  passwordFormRef,
  passwordLoading,
  passwordRules,
  phoneInput,
  removeAvatar,
  saveDisplayName,
  saveEmail,
  savePhone,
  showChangePassword,
  startEditDisplayName,
  startEditEmail,
  startEditPhone,
  Upload,
  userInfo,
  X
} = useProfileView()
</script>

<style scoped src="./ProfileView.css"></style>
