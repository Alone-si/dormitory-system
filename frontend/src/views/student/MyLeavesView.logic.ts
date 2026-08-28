import { ref, computed, onMounted } from 'vue'
import { Plus, CalendarClock, X } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { leaveApi } from '../../api/leave'
import { useUserStore } from '../../stores/user'
import type { Leave } from '../../types'

export function useMyLeavesView() {
const userStore = useUserStore()
const leaves = ref<Leave[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  type: 'PERSONAL' as any,
  startDate: '',
  endDate: '',
  reason: '',
  emergencyContact: '',
  emergencyPhone: ''
})

const loadLeaves = async () => {
  try {
    leaves.value = await leaveApi.getByStudent(userStore.userInfo!.id)
  } catch (error) {
    ElMessage.error('加载请假记录失败')
  }
}

const handleSubmit = async () => {
  if (!form.value.startDate || !form.value.endDate) {
    ElMessage.warning('请选择请假日期')
    return
  }
  if (!form.value.reason.trim()) {
    ElMessage.warning('请填写缺寝原因')
    return
  }
  
  if (!userStore.userInfo?.room) {
    ElMessage.warning('您还未分配宿舍，无法提交缺寝请假申请。请联系管理员为您分配宿舍。')
    return
  }
  
  const start = new Date(form.value.startDate)
  const end = new Date(form.value.endDate)
  const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  
  if (days < 1) {
    ElMessage.warning('结束日期不能早于开始日期')
    return
  }
  
  if (days > 3) {
    ElMessage.warning('单次请假不能超过3天')
    return
  }
  
  submitting.value = true
  try {
    await leaveApi.create({
      student: { id: userStore.userInfo!.id } as any,
      days,
      ...form.value
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    form.value = {
      type: 'PERSONAL',
      startDate: '',
      endDate: '',
      reason: '',
      emergencyContact: '',
      emergencyPhone: ''
    }
    loadLeaves()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    PERSONAL: '事假',
    FAMILY: '公假',
    SICK: '病假',
    EMERGENCY: '紧急事假',
    OTHER: '其他'
  }
  return map[type] || type
}

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    PERSONAL: 'type-personal',
    FAMILY: 'type-family',
    SICK: 'type-sick',
    EMERGENCY: 'type-emergency',
    OTHER: 'type-other'
  }
  return map[type] || 'type-other'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const getStatusColor = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}

const formatDate = (date: string | undefined) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

onMounted(() => {
  loadLeaves()
})
  return {
  CalendarClock,
  dialogVisible,
  disabledDate,
  form,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleSubmit,
  leaves,
  Plus,
  submitting,
  X
  }
}