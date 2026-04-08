<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo-section">
        <Building2 :size="32" class="logo-icon" />
        <h2>智能宿舍</h2>
      </div>
      
      <nav class="nav-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="active"
        >
          <component :is="item.icon" :size="20" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">
          <LogOut :size="20" />
          <span>退出登录</span>
        </button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <div class="breadcrumb">
          <span class="current-page">{{ currentPageTitle }}</span>
        </div>
        <div class="user-info">
          <span class="user-name">{{ userStore.userInfo?.name }}</span>
          <div class="user-avatar">
            <User :size="20" />
          </div>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
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
import { ElMessage } from 'element-plus'

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

const handleLogout = () => {
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo-section {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  color: white;
}

.logo-section h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 4px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-size: 15px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 64px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.current-page {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: #666;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.content-area {
  flex: 1;
  overflow: hidden;
  background: #f5f7fa;
  height: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
