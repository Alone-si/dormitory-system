import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Building2, LogOut, Home, Wrench, FileText, Bell, Settings } from 'lucide-vue-next'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'

export function useStudentLayoutView() {
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isReadOnly = computed(() => Boolean(userStore.userInfo?.mustChangePassword))

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

const navItems = [
  { path: '/student/home', label: '我的宿舍', description: '查看住宿与室友信息', icon: Home },
  { path: '/student/repairs', label: '报修', description: '提交和跟进维修事项', icon: Wrench },
  { path: '/student/leaves', label: '请假', description: '管理缺寝与请假记录', icon: FileText },
  { path: '/student/notices', label: '通知', description: '查看宿舍最新消息', icon: Bell },
  { path: '/student/profile', label: '我的账户', description: '维护个人资料与安全', icon: Settings }
]

const currentPage = computed(() => navItems.find(item => item.path === route.path) || navItems[0])

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
const changePassword = () => router.push('/change-password')
  return {
  Building2,
  currentDate,
  currentPage,
  currentSemester,
  changePassword,
  handleLogout,
  isReadOnly,
  LogOut,
  navItems,
  userStore
  }
}
