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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Delete, Edit } from '@element-plus/icons-vue'
import { Lock, X } from 'lucide-vue-next'
import * as userApi from '../../api/user'

// 响应式数据
const userInfo = ref({
  id: 0,
  studentId: '',
  name: '',
  username: '',
  displayName: '',
  gender: 'MALE',
  className: '',
  phone: '',
  email: '',
  avatar: ''
})

const editingDisplayName = ref(false)
const editingPhone = ref(false)
const editingEmail = ref(false)
const editForm = reactive({
  displayName: '',
  phone: '',
  email: ''
})

const showChangePassword = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarInput = ref()
const displayNameInput = ref()
const phoneInput = ref()
const emailInput = ref()
const passwordFormRef = ref()

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response.code === 200) {
      const user = response.data
      userInfo.value = {
        id: user.id,
        studentId: user.studentId || '',
        name: user.name,
        username: user.username || user.name,
        displayName: (user as any).displayName || user.studentId || user.name, // 优先displayName，然后学号，最后姓名
        gender: user.gender || 'MALE',
        className: (user as any).className || '未分班',
        phone: user.phone || '',
        email: user.email || '',
        avatar: user.avatar || ''
      }
    } else {
      console.error('API响应错误:', response)
      ElMessage.error(response.message || '加载用户信息失败')
      
      // 如果用户不存在，提供重新登录的选项
      if (response.message?.includes('用户不存在') || response.message?.includes('不存在')) {
        ElMessageBox.confirm(
          '用户信息不存在，请重新登录以获取最新数据',
          '用户信息异常',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          // 清除本地存储并跳转到登录页
          localStorage.clear()
          window.location.href = '/login'
        })
      }
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('网络错误，请检查网络连接')
    
    // 如果是401错误（未授权），说明token失效
    if (error.response?.status === 401) {
      ElMessageBox.confirm(
        '登录状态已过期，请重新登录',
        '登录过期',
        {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        localStorage.clear()
        window.location.href = '/login'
      })
    }
  }
}

// 头像相关方法
const handleAvatarChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  // 验证文件类型 - 只支持静态图片
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请选择 JPG、PNG 或 WEBP 格式的图片')
    return
  }

  // 验证文件大小 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    userInfo.value.avatar = e.target?.result as string
  }
  reader.readAsDataURL(file)

  // TODO: 上传到服务器
  uploadAvatar(file)
}

const uploadAvatar = async (file: File) => {
  try {
    const response = await userApi.uploadAvatar(file)
    if (response.code === 200) {
      userInfo.value.avatar = response.data.avatarUrl
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

const removeAvatar = async () => {
  try {
    await ElMessageBox.confirm('确定要移除头像吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await userApi.removeAvatar()
    if (response.code === 200) {
      userInfo.value.avatar = ''
      ElMessage.success('头像已移除')
    } else {
      ElMessage.error(response.message || '移除头像失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除头像失败')
    }
  }
}

// 用户名编辑相关方法
const startEditDisplayName = () => {
  editForm.displayName = userInfo.value.displayName || userInfo.value.studentId
  editingDisplayName.value = true
  nextTick(() => {
    displayNameInput.value?.focus()
  })
}

const saveDisplayName = async () => {
  const newDisplayName = editForm.displayName.trim()
  
  // 如果输入为空，则清空displayName，显示默认学号
  try {
    const response = await userApi.updateDisplayName(newDisplayName)
    if (response.code === 200) {
      userInfo.value.displayName = newDisplayName
      editingDisplayName.value = false
      ElMessage.success('用户名修改成功')
    } else {
      ElMessage.error(response.message || '用户名修改失败')
    }
  } catch (error: any) {
    console.error('用户名修改失败:', error)
    ElMessage.error('用户名修改失败')
  }
}

// 电话编辑相关方法
const startEditPhone = () => {
  editForm.phone = userInfo.value.phone
  editingPhone.value = true
  nextTick(() => {
    phoneInput.value?.focus()
  })
}

const savePhone = async () => {
  if (!editForm.phone.trim()) {
    ElMessage.error('电话不能为空')
    return
  }

  // 简单的电话号码验证
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(editForm.phone)) {
    ElMessage.error('请输入正确的手机号码')
    return
  }

  try {
    const response = await userApi.updateUser({ phone: editForm.phone })
    if (response.code === 200) {
      userInfo.value.phone = editForm.phone
      editingPhone.value = false
      ElMessage.success('电话修改成功')
    } else {
      ElMessage.error(response.message || '电话修改失败')
    }
  } catch (error) {
    ElMessage.error('电话修改失败')
  }
}

// 邮箱编辑相关方法
const startEditEmail = () => {
  editForm.email = userInfo.value.email
  editingEmail.value = true
  nextTick(() => {
    emailInput.value?.focus()
  })
}

const saveEmail = async () => {
  const email = editForm.email.trim()
  
  // 如果邮箱不为空，验证格式
  if (email && !validateEmail(email)) {
    ElMessage.error('请输入正确的邮箱地址')
    return
  }

  try {
    const response = await userApi.updateEmail(email)
    if (response.code === 200) {
      userInfo.value.email = email
      editingEmail.value = false
      ElMessage.success('邮箱修改成功')
    } else {
      ElMessage.error(response.message || '邮箱修改失败')
    }
  } catch (error) {
    ElMessage.error('邮箱修改失败')
  }
}

// 邮箱格式验证
const validateEmail = (email: string): boolean => {
  if (!email || email.trim() === '') {
    return true // 空邮箱是有效的（表示清除）
  }
  const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  return emailRegex.test(email)
}

// 密码修改相关方法
const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    const response = await userApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      showChangePassword.value = false
      
      // 重置表单
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error('密码修改失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-view {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 24px;
}

.profile-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

/* 头像设置 */
.avatar-section {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-display {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-display img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  font-size: 32px;
  font-weight: 600;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 基本信息 */
.info-section {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  min-width: 80px;
}

.readonly {
  font-size: 14px;
  color: #1f2937;
}

.editable-field {
  flex: 1;
  text-align: right;
}

.editable-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #1f2937;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.editable-text:hover {
  background: #f3f4f6;
  color: #3b82f6;
}

.edit-icon {
  font-size: 12px;
  opacity: 0.6;
}

/* 账户安全 */
.security-section {
  padding: 24px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
}

.security-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.security-info p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-view {
    padding: 16px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .editable-field {
    text-align: left;
    width: 100%;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

/* 修改密码对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  padding: 0 !important;
}

:deep(.el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.el-dialog__body) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.el-dialog__footer) {
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

.password-dialog-content {
  padding: 24px;
}

.password-form {
  margin: 0;
}

.password-form .el-form-item {
  margin-bottom: 20px;
}

.password-form .el-form-item:last-child {
  margin-bottom: 0;
}

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
  font-weight: 500;
}

.field-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
