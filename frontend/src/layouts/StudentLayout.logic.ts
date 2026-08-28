import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, LogOut, Home, Wrench, FileText, Bell, Settings } from 'lucide-vue-next'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'

export function useStudentLayoutView() {
const router = useRouter()
const userStore = useUserStore()

const roomText = computed(() => {
  if (!userStore.userInfo?.room) return '未分配'
  return `${userStore.userInfo.room.building.name}-${userStore.userInfo.room.roomNumber}`
})

const currentDate = computed(() => {
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  }).format(new Date())
})

// 自动计算当前学期
const currentSemester = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1 // 0-11，需要+1
  
  // 9月到次年1月为秋季学期，2月到8月为春季学期
  if (month >= 9) {
    return `${year}-${year + 1} 秋季`
  } else if (month >= 2) {
    return `${year - 1}-${year} 春季`
  } else {
    return `${year - 1}-${year} 秋季`
  }
})

const handleLogout = async () => {
  try {
    await authApi.logout()
  } catch {
    // 即使后端登出失败，也继续清理本地登录状态
  }
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}
  return {
  Bell,
  Building2,
  currentDate,
  currentSemester,
  FileText,
  handleLogout,
  Home,
  LogOut,
  roomText,
  Settings,
  userStore,
  Wrench
  }
}