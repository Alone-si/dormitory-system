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
  { path: '/admin/dashboard', label: '总览', description: '掌握今日住宿运行状态', icon: LayoutDashboard, group: '工作台' },
  { path: '/admin/students', label: '学生', description: '学生档案与住宿安排', icon: Users, group: '住宿管理' },
  { path: '/admin/buildings', label: '楼栋', description: '楼栋资料与容量配置', icon: Building2, group: '住宿管理' },
  { path: '/admin/rooms', label: '房间', description: '房间状态与入住信息', icon: Home, group: '住宿管理' },
  { path: '/admin/admins', label: '管理员', description: '管理后台成员与权限', icon: UserCog, group: '住宿管理', requireSuperAdmin: true },
  { path: '/admin/repairs', label: '报修', description: '跟进学生报修与处理进度', icon: Wrench, group: '服务中心' },
  { path: '/admin/leaves', label: '请假', description: '审核缺寝与请假申请', icon: FileText, group: '服务中心' },
  { path: '/admin/notices', label: '通知', description: '发布和维护宿舍通知', icon: Bell, group: '服务中心' }
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

const menuGroups = computed(() => ['工作台', '住宿管理', '服务中心'].map(label => ({
  label,
  items: menuItems.value.filter(item => item.group === label)
})))

const currentPageTitle = computed(() => {
  const item = allMenuItems.find(m => m.path === route.path)
  return item?.label || '管理后台'
})

const currentPageDescription = computed(() => {
  const item = allMenuItems.find(m => m.path === route.path)
  return item?.description || '让住宿管理更清晰、更高效'
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
  currentPageDescription,
  currentPageTitle,
  handleLogout,
  LogOut,
  menuGroups,
  roleText,
  userStore
  }
}
