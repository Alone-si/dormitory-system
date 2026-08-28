import { ref, onMounted } from 'vue'
import { Plus, Wrench, X } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { repairApi } from '../../api/repair'
import { useUserStore } from '../../stores/user'
import type { Repair } from '../../types'

export function useMyRepairsView() {
const userStore = useUserStore()
const repairs = ref<Repair[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  type: 'ELECTRICAL' as any,
  urgency: 'MEDIUM' as any,
  location: '',
  description: ''
})

const loadRepairs = async () => {
  try {
    const data = await repairApi.getByStudent(userStore.userInfo!.id)
    repairs.value = data
  } catch (error) {
    console.error('Failed to load repairs:', error)
    ElMessage.error('加载报修记录失败')
  }
}

const handleSubmit = async () => {
  if (!form.value.description.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }
  
  // 检查是否已分配宿舍
  if (!userStore.userInfo?.room) {
    ElMessage.warning('您还未分配宿舍，无法提交报修申请。请联系管理员为您分配宿舍。')
    return
  }
  
  submitting.value = true
  try {
    await repairApi.create({
      student: { id: userStore.userInfo!.id } as any,
      room: userStore.userInfo!.room!,
      ...form.value
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    form.value = {
      type: 'ELECTRICAL',
      urgency: 'MEDIUM',
      location: '',
      description: ''
    }
    loadRepairs()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    ELECTRICAL: '电路问题',
    PLUMBING: '水管问题',
    FURNITURE: '家具损坏',
    DOOR_WINDOW: '门窗问题',
    NETWORK: '网络问题',
    OTHER: '其他'
  }
  return map[type] || type
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '处理中',
    COMPLETED: '已完成',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const getStatusColor = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getUrgencyText = (urgency: string) => {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[urgency] || urgency
}

const getUrgencyColor = (urgency: string) => {
  const map: Record<string, any> = {
    LOW: 'info',
    MEDIUM: 'info',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[urgency] || 'info'
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRepairs()
})
  return {
  dialogVisible,
  form,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeText,
  getUrgencyColor,
  getUrgencyText,
  handleSubmit,
  Plus,
  repairs,
  submitting,
  Wrench,
  X
  }
}
