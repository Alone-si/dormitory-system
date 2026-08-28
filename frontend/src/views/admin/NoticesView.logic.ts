import { ref, computed, onMounted } from 'vue'
import { Plus, Pin, Clock, Users, Edit2, Trash2, User } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi } from '../../api/notice'
import { useUserStore } from '../../stores/user'
import type { Notice } from '../../types'

export function useNoticesView() {
const userStore = useUserStore()
const loading = ref(false)
const notices = ref<Notice[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  title: '',
  content: '',
  type: 'ANNOUNCEMENT' as any,
  priority: 'NORMAL' as any,
  target: 'ALL' as any,
  pinned: false
})

const dialogTitle = computed(() => editingId.value ? '编辑通知' : '发布通知')

const loadNotices = async () => {
  loading.value = true
  try {
    notices.value = await noticeApi.getAll()
  } catch (error) {
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingId.value = null
  form.value = {
    title: '',
    content: '',
    type: 'ANNOUNCEMENT',
    priority: 'NORMAL',
    target: 'ALL',
    pinned: false
  }
  dialogVisible.value = true
}

const handleEdit = (notice: Notice) => {
  editingId.value = notice.id
  form.value = {
    title: notice.title,
    content: notice.content,
    type: notice.type,
    priority: notice.priority,
    target: notice.target || 'ALL',
    pinned: notice.pinned || false
  }
  dialogVisible.value = true
}

const confirmSave = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  saving.value = true
  try {
    const data = {
      ...form.value,
      publisher: { id: userStore.userInfo!.id } as any
    }
    
    if (editingId.value) {
      await noticeApi.update(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await noticeApi.create(data)
      ElMessage.success('发布成功')
    }
    
    dialogVisible.value = false
    loadNotices()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (notice: Notice) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', { 
      type: 'warning',
      customClass: 'apple-message-box'
    })
    await noticeApi.delete(notice.id)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    ANNOUNCEMENT: '公告',
    MAINTENANCE: '维护通知',
    EVENT: '活动通知',
    REGULATION: '规章制度',
    EMERGENCY: '紧急通知'
  }
  return map[type] || type
}

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    ANNOUNCEMENT: 'announcement',
    MAINTENANCE: 'maintenance',
    EVENT: 'event',
    REGULATION: 'regulation',
    EMERGENCY: 'emergency'
  }
  return map[type] || ''
}

const getPriorityText = (priority: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    NORMAL: '普通',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority] || priority
}

const getPriorityColor = (priority: string) => {
  const map: Record<string, any> = {
    LOW: 'low',
    NORMAL: 'normal',
    HIGH: 'high',
    URGENT: 'urgent'
  }
  return map[priority] || ''
}

const getTargetText = (target?: string) => {
  if (!target) return '全体'
  const map: Record<string, string> = {
    ALL: '全体',
    MALE: '男生',
    FEMALE: '女生'
  }
  return map[target] || target
}

const getTargetColor = (target?: string) => {
  if (!target) return 'all'
  const map: Record<string, string> = {
    ALL: 'all',
    MALE: 'male',
    FEMALE: 'female'
  }
  return map[target] || 'all'
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotices()
})
  return {
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
  }
}