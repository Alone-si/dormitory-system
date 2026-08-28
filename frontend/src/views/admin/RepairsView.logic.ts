import { ref, onMounted } from 'vue'
import { Clock, MapPin } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { repairApi } from '../../api/repair'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import type { Repair } from '../../types'

export function useRepairsView() {
const userStore = useUserStore()
const loading = ref(false)
const statusFilter = ref('ALL')
const repairs = ref<Repair[]>([])
const dialogVisible = ref(false)
const currentRepair = ref<Repair | null>(null)
const processDialogVisible = ref(false)
const processForm = ref({
  reply: ''
})

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' }
]

const changeFilter = (value: string) => {
  statusFilter.value = value
  loadRepairs()
}

const loadRepairs = async () => {
  loading.value = true
  try {
    if (statusFilter.value === 'ALL') {
      repairs.value = await repairApi.getAll()
    } else {
      const all = await repairApi.getAll()
      repairs.value = all.filter(r => r.status === statusFilter.value)
    }
  } catch (error) {
    ElMessage.error('加载报修列表失败')
  } finally {
    loading.value = false
  }
}

const handleProcess = (repair: Repair) => {
  currentRepair.value = repair
  processForm.value.reply = ''
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  if (!currentRepair.value) return
  
  try {
    await repairApi.updateStatus(
      currentRepair.value.id, 
      'IN_PROGRESS' as any, 
      userStore.userInfo?.id, 
      processForm.value.reply || undefined
    )
    ElMessage.success('已开始处理')
    processDialogVisible.value = false
    loadRepairs()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComplete = async (repair: Repair) => {
  try {
    await ElMessageBox.confirm('确定该报修已维修完成吗？', '确认完成', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await repairApi.updateStatus(repair.id, 'COMPLETED' as any)
    ElMessage.success('已完成维修')
    loadRepairs()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const showDetail = (repair: Repair) => {
  currentRepair.value = repair
  dialogVisible.value = true
}

const handleDeleteRepair = async () => {
  if (!currentRepair.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除这条报修记录吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/repairs/${currentRepair.value.id}`)
    ElMessage.success('删除成功')
    dialogVisible.value = false
    loadRepairs()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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

const getTypeColor = (type: string) => {
  const map: Record<string, any> = {
    ELECTRICAL: 'warning',
    PLUMBING: 'primary',
    FURNITURE: 'success',
    DOOR_WINDOW: 'info',
    NETWORK: 'danger',
    OTHER: 'info'
  }
  return map[type] || 'info'
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

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRepairs()
})
  return {
  changeFilter,
  Clock,
  confirmProcess,
  currentRepair,
  dialogVisible,
  filters,
  formatDate,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleComplete,
  handleDeleteRepair,
  handleProcess,
  loading,
  MapPin,
  processDialogVisible,
  processForm,
  repairs,
  showDetail,
  statusFilter
  }
}