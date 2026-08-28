import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Delete, Edit } from '@element-plus/icons-vue'
import { Lock, X } from 'lucide-vue-next'
import * as userApi from '../../api/user'

export function useProfileView() {
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
    { min: 8, max: 128, message: '密码长度必须为8至128位', trigger: 'blur' }
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
  return {
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
  }
}
