import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, UserPlus, Edit2, Trash2, Key, X } from 'lucide-vue-next'
import { adminApi, type Admin, type AdminRequest, AdminType } from '../../api/admin'
import { useUserStore } from '../../stores/user'

export function useAdminsView() {
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
  adminType: AdminType.NORMAL_ADMIN,
  password: ''
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
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (dialogMode.value === 'edit') callback()
        else if (!value || value.length < 8 || value.length > 128) callback(new Error('初始密码长度必须为8至128位'))
        else callback()
      },
      trigger: 'blur'
    }
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
    adminType: AdminType.NORMAL_ADMIN,
    password: ''
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
    const { value } = await ElMessageBox.prompt(
      `为管理员“${admin.name}”设置临时密码。登录后必须立即修改。`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
        inputPlaceholder: '请输入8至128位临时密码',
        inputValidator: (password) =>
          password.length >= 8 && password.length <= 128 ? true : '临时密码长度必须为8至128位'
      }
    )
    
    await adminApi.resetPassword(admin.id, value)
    ElMessage.success('密码已重置，该管理员下次登录后必须修改密码')
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
  return {
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
  }
}
