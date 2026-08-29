import { ref, onMounted } from 'vue'
import { Pin } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { noticeApi } from '../../api/notice'
import { useUserStore } from '../../stores/user'
import type { Notice } from '../../types'

export function useNoticesView() {
const userStore = useUserStore()
const notices = ref<Notice[]>([])
const detailVisible = ref(false)
const selectedNotice = ref<Notice | null>(null)

const showDetail = (notice: Notice) => {
  selectedNotice.value = notice
  detailVisible.value = true
}

const getPreview = (content: string) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

const loadNotices = async () => {
  try {
    const gender = userStore.userInfo?.gender
    if (gender) {
      notices.value = await noticeApi.getByTarget(gender)
    } else {
      notices.value = await noticeApi.getAll()
    }
  } catch (error) {
    ElMessage.error('加载通知失败')
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
  const map: Record<string, 'info' | 'warning' | 'danger' | undefined> = {
    LOW: 'info',
    NORMAL: undefined,
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[priority]
}

const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotices()
})
  return {
  detailVisible,
  formatDate,
  getPreview,
  getPriorityColor,
  getPriorityText,
  getTypeText,
  notices,
  Pin,
  selectedNotice,
  showDetail
  }
}
