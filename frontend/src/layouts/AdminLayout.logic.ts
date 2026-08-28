import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  Building2, 
  LayoutDashboard, 
  Users, 
  Home, 
  Wrench, 
  FileText, 
  Bell,
  LogOut,
  User,
  UserCog
} from 'lucide-vue-next'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'

export function useAdminLayoutView() {
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 所有菜单项
const allMenuItems = [
  { path: '/admin/dashboard', label: '仪表盘', icon: LayoutDashboard },
  { path: '/admin/students', label: '学生管理', icon: Users },
  { path: '/admin/admins', label: '管理员管理', icon: UserCog, requireSuperAdmin: true },
  { path: '/admin/buildings', label: '宿舍楼管理', icon: Building2 },
  { path: '/admin/rooms', label: '宿舍管理', icon: Home },
  { path: '/admin/repairs', label: '报修处理', icon: Wrench },
  { path: '/admin/leaves', label: '缺寑请假', icon: FileText },
  { path: '/admin/notices', label: '宿舍通知', icon: Bell }
]

// 根据用户权限过滤菜单
const menuItems = computed(() => {
  const userInfo = userStore.userInfo as any
  const isSuperAdmin = userInfo?.adminType === 'SUPER_ADMIN'
  
  return allMenuItems.filter(item => {
    // 如果菜单项需要超级管理员权限，且当前用户不是超级管理员，则隐藏
    if (item.requireSuperAdmin && !isSuperAdmin) {
      return false
    }
    return true
  })
})

const currentPageTitle = computed(() => {
  const item = allMenuItems.find(m => m.path === route.path)
  return item?.label || '管理后台'
})

const currentDate = computed(() => {
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  }).format(new Date())
})

const roleText = computed(() => {
  const userInfo = userStore.userInfo as any
  return userInfo?.adminType === 'SUPER_ADMIN' ? '超级管理员' : '管理员'
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
  Building2,
  currentDate,
  currentPageTitle,
  handleLogout,
  LogOut,
  menuItems,
  roleText,
  User,
  userStore
  }
}