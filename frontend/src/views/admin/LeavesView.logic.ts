import { ref, computed, onMounted } from 'vue'
import { Calendar, Check, X, User, Trash2 } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { leaveApi } from '../../api/leave'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import type { Leave } from '../../types'

export function useLeavesView() {
const userStore = useUserStore()
const loading = ref(false)
const statusFilter = ref('ALL')
const leaves = ref<Leave[]>([])
const approvalDialogVisible = ref(false)
const approving = ref(false)

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待审批', value: 'PENDING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const changeFilter = (value: string) => {
  statusFilter.value = value
  loadLeaves()
}
const currentLeave = ref<Leave | null>(null)
const approvalType = ref<'approve' | 'reject'>('approve')
const approvalForm = ref({
  comment: ''
})

const approvalTitle = computed(() => {
  return approvalType.value === 'approve' ? '批准缺寝' : '拒绝缺寝'
})

const loadLeaves = async () => {
  loading.value = true
  try {
    if (statusFilter.value === 'ALL') {
      leaves.value = await leaveApi.getAll()
    } else if (statusFilter.value === 'PENDING') {
      leaves.value = await leaveApi.getPending()
    } else {
      const all = await leaveApi.getAll()
      leaves.value = all.filter(l => l.status === statusFilter.value)
    }
  } catch (error) {
    ElMessage.error('加载请假列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (leave: Leave) => {
  currentLeave.value = leave
  approvalType.value = 'approve'
  approvalForm.value.comment = '同意'
  approvalDialogVisible.value = true
}

const handleReject = (leave: Leave) => {
  currentLeave.value = leave
  approvalType.value = 'reject'
  approvalForm.value.comment = ''
  approvalDialogVisible.value = true
}

const confirmApproval = async () => {
  approving.value = true
  try {
    if (approvalType.value === 'approve') {
      await leaveApi.approve(currentLeave.value!.id, userStore.userInfo!.id, approvalForm.value.comment)
      ElMessage.success('已批准')
    } else {
      await leaveApi.reject(currentLeave.value!.id, userStore.userInfo!.id, approvalForm.value.comment)
      ElMessage.success('已拒绝')
    }
    approvalDialogVisible.value = false
    loadLeaves()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    approving.value = false
  }
}

const handleDelete = async (leave: Leave) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${leave.student.name}" 的请假记录吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/leaves/${leave.id}`)
    ElMessage.success('删除成功')
    loadLeaves()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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

onMounted(() => {
  loadLeaves()
})
  return {
  approvalDialogVisible,
  approvalForm,
  approvalTitle,
  approvalType,
  approving,
  Calendar,
  changeFilter,
  Check,
  confirmApproval,
  currentLeave,
  filters,
  getStatusColor,
  getStatusText,
  getTypeColor,
  getTypeText,
  handleApprove,
  handleDelete,
  handleReject,
  leaves,
  loading,
  statusFilter,
  Trash2,
  User,
  X
  }
}