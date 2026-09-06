import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { UserCircle as UserIcon, Phone, Mail, Home, Plus, Download, Upload, Search, CheckSquare, X, LogOut, UploadCloud, BookOpen, UserPen, UserPlus, Archive } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { roomApi } from '../../api/room'
import { useUserStore } from '../../stores/user'
import type { User, Room } from '../../types'
import { performSmartRoomAssignment } from '../../composables/students/useSmartRoomAssignment'
import { exportStudents, importStudentsFromFile, downloadStudentTemplate } from '../../composables/students/useStudentImportExport'
import { useStudentList } from '../../composables/students/useStudentList'
import { useStudentSelection } from '../../composables/students/useStudentSelection'

export function useStudentsView() {
const userStore = useUserStore()

const {
  loading,
  searchQuery,
  statusFilter,
  classFilter,
  roomFilter,
  students,
  currentPage,
  pageSize,
  filteredStudents,
  hasActiveFilters,
  paginatedStudents,
  resetFilters,
  handlePageChange,
  handlePageSizeChange,
  loadStudents
} = useStudentList()

const {
  selectionMode,
  selectedStudents,
  isAllSelected,
  isSelected,
  toggleSelection,
  toggleSelectAll,
  enterSelectionMode,
  exitSelectionMode
} = useStudentSelection(paginatedStudents)
const availableRooms = ref<Room[]>([])
const assignDialogVisible = ref(false)
const assigning = ref(false)
const currentStudent = ref<User | null>(null)  // 🔥 修复：使用User类型
const assignForm = ref({
  roomId: null as number | null
})

// 导入相关
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)

// 批量分班相关
const batchClassDialogVisible = ref(false)
const batchClassLoading = ref(false)
const batchClassForm = ref({
  operation: 'assign',
  className: ''
})

const openBatchClassDialog = () => {
  batchClassForm.value.operation = 'assign'
  batchClassForm.value.className = ''
  batchClassDialogVisible.value = true
}

const confirmBatchClass = async () => {
  if (selectedStudents.value.length > 30) {
    ElMessage.warning('单次批量操作最多支持30人')
    return
  }

  // 检查分配班级操作是否填写了班级名称
  if (batchClassForm.value.operation === 'assign' && !batchClassForm.value.className) {
    ElMessage.warning('请输入班级名称')
    return
  }
  
  const isAssign = batchClassForm.value.operation === 'assign'
  const operationName = isAssign ? '分配班级' : '退出班级'
  
  console.log(`=== 批量${operationName}开始 ===`)
  console.log('选中学生数量:', selectedStudents.value.length)
  console.log('操作类型:', batchClassForm.value.operation)
  if (isAssign) {
    console.log('班级名称:', batchClassForm.value.className)
  }
  console.log('选中的学生:', selectedStudents.value)
  
  batchClassLoading.value = true
  let successCount = 0
  let failCount = 0
  
  try {
    for (const student of selectedStudents.value) {
      try {
        console.log(`正在更新学生 ${student.name} (ID: ${student.id})`)
        const updateData = isAssign 
          ? { className: batchClassForm.value.className }
          : { className: null } // 退出班级时设置为null
        
        const response = await request.put(`/users/students/${student.id}`, updateData)
        console.log(`学生 ${student.name} 更新成功:`, response)
        successCount++
      } catch (error) {
        console.error(`学生 ${student.name} 更新失败:`, error)
        failCount++
      }
    }
    
    console.log(`批量更新完成: 成功 ${successCount} 人, 失败 ${failCount} 人`)
    
    if (successCount > 0) {
      ElMessage.success(`成功${operationName} ${successCount} 人${failCount > 0 ? `，失败 ${failCount} 人` : ''}`)
      batchClassDialogVisible.value = false
      loadStudents()
      exitSelectionMode()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error(`批量${operationName}异常:`, error)
    ElMessage.error('操作异常')
  } finally {
    batchClassLoading.value = false
  }
}

// 学生表单相关
const studentDialogVisible = ref(false)
const savingStudent = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const studentForm = ref({
  name: '',
  studentId: '',
  displayName: '',
  password: '',
  gender: 'MALE',
  phone: '',
  email: '',
  className: ''
})

const batchCheckIn = async () => {
  const studentsWithoutRoom = selectedStudents.value.filter(s => !s.room)
  
  if (studentsWithoutRoom.length === 0) {
    ElMessage.warning('所选学生都已分配宿舍')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要为 ${studentsWithoutRoom.length} 名学生批量分配宿舍吗？系统会智能分配：同班同学优先安排在同一楼栋，相邻宿舍。`,
      '🤖 智能批量入住',
      {
        confirmButtonText: '开始分配',
        cancelButtonText: '取消',
        type: 'info',
        customClass: 'apple-smart-assign-dialog',
        center: true
      }
    )
    
    const result = await performSmartRoomAssignment(studentsWithoutRoom)
    if (result.successCount > 0) {
      await loadStudents()
      exitSelectionMode()
    }
    
  } catch (error) {
    // 用户取消
  }
}

const batchCheckOut = async () => {
  const studentsWithRoom = selectedStudents.value.filter(s => s.room)
  
  if (studentsWithRoom.length === 0) {
    ElMessage.warning('所选学生都未分配宿舍')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要为 ${studentsWithRoom.length} 名学生批量办理退寝吗？\n\n此操作将清空他们的宿舍分配信息。`,
      '批量退寝确认',
      {
        confirmButtonText: '确定退寝',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'apple-message-box',
        dangerouslyUseHTMLString: false,
        center: false
      }
    )
    
    let successCount = 0
    let failCount = 0
    
    for (const student of studentsWithRoom) {
      try {
        // 🔥 修复：后端返回的是User对象，直接使用id
        await request.post(`/rooms/remove/${student.id}`)
        successCount++
      } catch (error) {
        failCount++
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(`成功退寝 ${successCount} 人${failCount > 0 ? `，失败 ${failCount} 人` : ''}`)
      loadStudents()
      exitSelectionMode()
    }
  } catch (error) {
    // 用户取消
  }
}

const batchArchiveGraduated = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要归档所有超过3年学制的学生吗？归档后学生状态将变为"已毕业"，并自动退寝。',
      '批量归档确认',
      {
        confirmButtonText: '确定归档',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response: any = await request.post('/students/batch-archive')
    ElMessage.success(response.data || '归档成功')
    loadStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '归档失败')
    }
  }
}

const currentStudentRoomText = computed(() => {
  if (!currentStudent.value || !currentStudent.value.room) return '未分配'
  return `${currentStudent.value.room.building.name} - ${currentStudent.value.room.roomNumber}`
})

const showCreateDialog = () => {
  isEditing.value = false
  editingId.value = null
  currentStudent.value = null
  studentForm.value = {
    name: '',
    studentId: '',
    displayName: '',
    password: '', // 默认留空，后端处理默认值
    gender: 'MALE',
    phone: '',
    email: '',
    className: ''
  }
  studentDialogVisible.value = true
}

const handleEdit = (student: User) => {
  isEditing.value = true
  editingId.value = student.id
  currentStudent.value = student
  studentForm.value = {
    name: student.name,
    studentId: student.studentId || '',
    displayName: student.displayName || '',  // 🔥 修复：后端返回的是User对象，直接使用displayName
    password: '', // 编辑时不显示密码
    gender: student.gender || 'MALE',
    phone: student.phone || '',
    email: student.email || '',
    className: student.className || ''
  }
  studentDialogVisible.value = true
}

const confirmSaveStudent = async () => {
  if (!studentForm.value.name || !studentForm.value.studentId) {
    ElMessage.warning('请填写完整信息')
    return
  }

  savingStudent.value = true
  try {
    if (isEditing.value) {
      // 更新
      const data: any = { ...studentForm.value }
      if (!data.password) delete data.password // 如果没填密码就不传
      await request.put(`/users/students/${editingId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await request.post('/students', {
        ...studentForm.value,
        role: 'STUDENT',
        password: studentForm.value.password || '123456'
      })
      ElMessage.success('创建成功')
    }
    studentDialogVisible.value = false
    loadStudents()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    savingStudent.value = false
  }
}

const handleDeleteStudent = async () => {
  if (!editingId.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${currentStudent.value?.name}" 吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/users/students/${editingId.value}`)
    ElMessage.success('删除成功')
    studentDialogVisible.value = false
    loadStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const loadAvailableRooms = async (gender: string) => {
  try {
    const rooms = await roomApi.getAvailable()
    availableRooms.value = rooms.filter(r => r.building.type === gender)
  } catch (error) {
    ElMessage.error('加载可用宿舍失败')
  }
}

const handleAssignRoom = async (student: User) => {
  currentStudent.value = student
  await loadAvailableRooms(student.gender!)
  if (availableRooms.value.length === 0) {
    ElMessage.warning('暂无可用宿舍')
    return
  }
  assignDialogVisible.value = true
}

// 对话框打开时应用样式
const onDialogOpened = () => {
  nextTick(() => {
    // 查找对话框元素，使用更精确的选择器
    const dialog = document.querySelector('.el-dialog.apple-assign-dialog-wrapper') || 
                   document.querySelector('.apple-assign-dialog-wrapper')
    
    if (dialog) {
      const dialogEl = dialog as HTMLElement
      // 确保对话框居中显示
      dialogEl.style.position = 'fixed'
      dialogEl.style.top = '50%'
      dialogEl.style.left = '50%'
      dialogEl.style.transform = 'translate(-50%, -50%)'
      dialogEl.style.margin = '0'
      
      // 应用圆角和阴影
      dialogEl.style.borderRadius = '20px'
      dialogEl.style.overflow = 'hidden'
      dialogEl.style.boxShadow = '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
      
      // 查找并修改标题栏
      const header = dialog.querySelector('.el-dialog__header')
      if (header) {
        const headerEl = header as HTMLElement
        headerEl.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
        headerEl.style.borderRadius = '20px 20px 0 0'
        headerEl.style.padding = '20px 24px 16px 24px'
        headerEl.style.borderBottom = 'none'
      }
      
      // 修改标题文字
      const title = dialog.querySelector('.el-dialog__title')
      if (title) {
        const titleEl = title as HTMLElement
        titleEl.style.color = 'white'
        titleEl.style.fontWeight = '700'
        titleEl.style.textAlign = 'center'
      }
      
      // 修改关闭按钮
      const closeBtn = dialog.querySelector('.el-dialog__close')
      if (closeBtn) {
        const closeBtnEl = closeBtn as HTMLElement
        closeBtnEl.style.color = 'white'
        closeBtnEl.style.fontSize = '18px'
      }
      
      // 修改body
      const body = dialog.querySelector('.el-dialog__body')
      if (body) {
        const bodyEl = body as HTMLElement
        bodyEl.style.padding = '0'
      }
    }
  })
}

const confirmAssign = async () => {
  if (!assignForm.value.roomId) {
    ElMessage.warning('请选择宿舍')
    return
  }
  
  assigning.value = true
  try {
    // 🔥 修复：后端返回的是User对象，直接使用id
    const userId = currentStudent.value!.id
    await roomApi.assignStudent(userId, assignForm.value.roomId)
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    assignForm.value.roomId = null
    loadStudents()
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败')
  } finally {
    assigning.value = false
  }
}

const handleCheckIn = async (student: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要让 ${student.name} 办理入住吗？`,
      '确认入住',
      { 
        type: 'info',
        customClass: 'apple-message-box',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 🔥 修复：后端返回的是User对象，直接使用id
    console.log('调用入住API，userId:', student.id)
    await request.post(`/students/${student.id}/checkin`)
    
    ElMessage.success(`${student.name} 入住成功`)
    loadStudents()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('入住失败:', error)
      ElMessage.error(error.response?.data?.message || '入住失败')
    }
  }
}

const handleCheckOut = async (student: User) => {
  try {
    // 检查学生是否有宿舍
    if (!student.room) {
      ElMessage.warning('该学生未分配宿舍')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要让 ${student.name} 退宿吗？`,
      '确认退宿',
      { 
        type: 'warning',
        customClass: 'apple-message-box',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 🔥 修复：后端返回的是User对象，直接使用id
    console.log('调用退宿API，userId:', student.id)
    await roomApi.removeStudent(student.id)
    ElMessage.success('退宿成功')
    loadStudents()
  } catch (error: any) {
    console.error('退宿失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退宿失败')
    }
  }
}

// 导入相关逻辑
const showImportDialog = () => {
  importDialogVisible.value = true
  importFile.value = null
}

const handleFileChange = (file: any) => {
  importFile.value = file.raw
}

const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  importing.value = true
  try {
    const success = await importStudentsFromFile(importFile.value)
    if (success) {
      importDialogVisible.value = false
      loadStudents()
    }
  } finally {
    importing.value = false
  }
}

// 下载Excel模板
onMounted(() => {
  loadStudents()
})
  return {
  Archive,
  assignDialogVisible,
  assignForm,
  assigning,
  availableRooms,
  batchArchiveGraduated,
  batchCheckIn,
  batchCheckOut,
  batchClassDialogVisible,
  batchClassForm,
  batchClassLoading,
  BookOpen,
  CheckSquare,
  classFilter,
  confirmAssign,
  confirmBatchClass,
  confirmImport,
  confirmSaveStudent,
  currentPage,
  currentStudent,
  Download,
  enterSelectionMode,
  exitSelectionMode,
  exportStudents,
  filteredStudents,
  handleAssignRoom,
  handleCheckIn,
  handleCheckOut,
  handleDeleteStudent,
  handleEdit,
  handleFileChange,
  handlePageChange,
  handlePageSizeChange,
  hasActiveFilters,
  Home,
  importDialogVisible,
  importing,
  isAllSelected,
  isEditing,
  isSelected,
  loading,
  LogOut,
  Mail,
  openBatchClassDialog,
  pageSize,
  paginatedStudents,
  Plus,
  resetFilters,
  roomFilter,
  savingStudent,
  Search,
  searchQuery,
  selectedStudents,
  selectionMode,
  showCreateDialog,
  showImportDialog,
  statusFilter,
  studentDialogVisible,
  studentForm,
  students,
  toggleSelectAll,
  toggleSelection,
  Upload,
  UploadCloud,
  UserPen,
  UserPlus,
  userStore,
  downloadStudentTemplate,
  X
  }
}
