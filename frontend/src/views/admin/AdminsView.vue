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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, UserPlus, Edit2, Trash2, Key, X } from 'lucide-vue-next'
import { adminApi, type Admin, type AdminRequest, AdminType } from '../../api/admin'
import { useUserStore } from '../../stores/user'

// 数据
const userStore = useUserStore()
const admins = ref<Admin[]>([])
const searchQuery = ref('')
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const currentEditId = ref<number | null>(null)

// 表单数据
const formData = ref<AdminRequest>({
  name: '',
  username: '',
  phone: '',
  adminType: AdminType.NORMAL_ADMIN
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  adminType: [
    { required: true, message: '请选择管理员类型', trigger: 'change' }
  ]
}

// 计算属性 - 筛选后的管理员列表
const filteredAdmins = computed(() => {
  if (!searchQuery.value) {
    return admins.value
  }
  const query = searchQuery.value.toLowerCase()
  return admins.value.filter(admin =>
    admin.name.toLowerCase().includes(query) ||
    admin.username.toLowerCase().includes(query)
  )
})

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载管理员列表
const loadAdmins = async () => {
  try {
    admins.value = await adminApi.getAdmins()
  } catch (error: any) {
    ElMessage.error(error.message || '加载管理员列表失败')
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogMode.value = 'add'
  currentEditId.value = null
  formData.value = {
    name: '',
    username: '',
    phone: '',
    adminType: AdminType.NORMAL_ADMIN
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (admin: Admin) => {
  dialogMode.value = 'edit'
  currentEditId.value = admin.id
  formData.value = {
    name: admin.name,
    username: admin.username,
    phone: admin.phone,
    adminType: admin.adminType
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (dialogMode.value === 'add') {
        await adminApi.createAdmin(formData.value)
        ElMessage.success('管理员添加成功')
      } else {
        if (!currentEditId.value) return
        await adminApi.updateAdmin(currentEditId.value, formData.value)
        ElMessage.success('管理员信息更新成功')
      }
      dialogVisible.value = false
      await loadAdmins()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

// 重置密码
const handleResetPassword = async (admin: Admin) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置管理员 "${admin.name}" 的密码吗？密码将重置为 123456`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.resetPassword(admin.id)
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置密码失败')
    }
  }
}

// 判断是否是当前登录用户
const isCurrentUser = (admin: Admin) => {
  return admin.username === userStore.userInfo?.username
}

// 删除管理员
const handleDelete = async (admin: Admin) => {
  try {
    // 防止删除自己
    if (isCurrentUser(admin)) {
      ElMessage.warning('不能删除自己')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除管理员 "${admin.name}" 吗？此操作不可恢复！`,
      '删除管理员',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await adminApi.deleteAdmin(admin.id)
    ElMessage.success('管理员删除成功')
    await loadAdmins()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadAdmins()
})
</script>

<style>
.admins-view {
  padding: 24px;
  background: #f9fafb;
  min-height: 100vh;
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
  color: #111827;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 按钮样式 */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  outline: none;
  white-space: nowrap;
}

.primary-btn {
  background: #3b82f6;
  color: white;
}

.primary-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}

.primary-btn:active {
  transform: translateY(0);
}

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 表格操作按钮 */
.table-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 0 4px;
}

.edit-btn {
  background: #eff6ff;
  color: #3b82f6;
}

.edit-btn:hover {
  background: #dbeafe;
  transform: translateY(-1px);
}

.reset-btn {
  background: #fef3c7;
  color: #f59e0b;
}

.reset-btn:hover {
  background: #fde68a;
  transform: translateY(-1px);
}

.delete-btn {
  background: #fee2e2;
  color: #ef4444;
}

.delete-btn:hover:not(.disabled) {
  background: #fecaca;
  transform: translateY(-1px);
}

.delete-btn.disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

.delete-btn.disabled:hover {
  transform: none;
}

/* 对话框样式 */
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
  padding: 24px !important;
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

/* 对话框按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 16px 16px;
}

.dialog-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: #f3f4f6;
  color: #374151;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.confirm-btn {
  background: #3b82f6;
  color: white;
}

.confirm-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}

/* Element Plus 样式覆盖 */
.admins-view .el-table {
  font-size: 14px;
}

.admins-view .el-table th {
  font-weight: 600;
  color: #374151;
}

.admins-view .el-table td {
  color: #111827;
}

.admins-view .el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.2s ease;
}

.admins-view .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #3b82f6 inset;
}

.admins-view .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px #3b82f6 inset;
}

.admins-view .el-dialog {
  border-radius: 12px !important;
}

.admins-view .el-dialog__header {
  padding: 20px 24px !important;
  border-bottom: none !important;
  background: #3b82f6 !important;
  border-radius: 12px 12px 0 0 !important;
}

.admins-view .el-dialog__title {
  font-size: 18px !important;
  font-weight: 700 !important;
  color: white !important;
}

.admins-view .el-dialog__headerbtn {
  top: 20px !important;
}

.admins-view .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 20px !important;
}

.admins-view .el-dialog__headerbtn .el-dialog__close:hover {
  color: rgba(255, 255, 255, 0.8) !important;
}

.admins-view .el-dialog__body {
  padding: 24px !important;
}

.admins-view .el-form-item__label {
  font-weight: 600 !important;
  color: #374151 !important;
  text-align: right !important;
  padding-right: 12px !important;
}

/* 修复下拉框选中项文字显示 */
.admins-view .el-select {
  width: 100%;
}

.admins-view .el-select .el-select__wrapper {
  min-height: 50px;
  height: auto;
  padding: 8px 12px;
  box-sizing: border-box;
}

.admins-view .el-select .el-select__selected-item {
  white-space: normal;
  line-height: 1.5;
  overflow: visible;
  text-overflow: clip;
  height: auto;
}

.admins-view .el-select .el-select__placeholder {
  white-space: normal;
  line-height: 1.5;
}

/* 修复下拉框选项显示 */
.el-select-dropdown__item {
  font-size: 14px;
  padding: 10px 16px;
  color: #374151;
  white-space: normal;
  line-height: 1.5;
  height: auto;
  min-height: 40px;
}

.el-select-dropdown__item:hover {
  background-color: #f3f4f6;
}

.el-select-dropdown__item.selected {
  color: #3b82f6;
  font-weight: 600;
}
</style>
