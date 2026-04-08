<template>
  <div class="student-layout">
    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="nav-left">
        <div class="logo-wrapper">
          <Building2 :size="28" class="logo-icon" />
        </div>
        <div class="title-section">
          <h1 class="main-title">智能宿舍</h1>
          <p class="sub-title">智能宿舍管理系统</p>
        </div>
      </div>
      <div class="nav-right">
        <span class="school-label">当前身份:</span>
        <el-tag type="primary" effect="plain" size="large">学生</el-tag>
        <button @click="handleLogout" class="menu-btn">
          <LogOut :size="20" />
        </button>
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <div class="user-info">
            <div class="avatar">
              <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :alt="userStore.userInfo.name" class="avatar-image" />
              <span v-else class="avatar-text">{{ userStore.userInfo?.name?.charAt(0) || 'U' }}</span>
            </div>
            <div class="user-details">
              <h2>你好，{{ userStore.userInfo?.name }}</h2>
              <p class="meta">
                学号: {{ userStore.userInfo?.studentId }} | 
                宿舍: {{ roomText }}
              </p>
            </div>
          </div>
          <div class="semester-tag">
            <el-tag type="primary" effect="plain">当前学期: {{ currentSemester }}</el-tag>
          </div>
        </div>

        <!-- Tab 导航 -->
        <div class="tab-nav">
          <router-link to="/student/home" class="tab-item">
            <Home :size="18" />
            <span>我的信息</span>
          </router-link>
          <router-link to="/student/repairs" class="tab-item">
            <Wrench :size="18" />
            <span>报修服务</span>
          </router-link>
          <router-link to="/student/leaves" class="tab-item">
            <FileText :size="18" />
            <span>缺寝请假</span>
          </router-link>
          <router-link to="/student/notices" class="tab-item">
            <Bell :size="18" />
            <span>宿舍通知</span>
          </router-link>
          <router-link to="/student/profile" class="tab-item">
            <Settings :size="18" />
            <span>个人设置</span>
          </router-link>
        </div>

        <!-- 路由视图 -->
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, LogOut, Home, Wrench, FileText, Bell, Settings } from 'lucide-vue-next'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const roomText = computed(() => {
  if (!userStore.userInfo?.room) return '未分配'
  return `${userStore.userInfo.room.building.name}-${userStore.userInfo.room.roomNumber}`
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

const handleLogout = () => {
  userStore.clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.top-nav {
  height: 64px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-wrapper {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  color: white;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.main-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  line-height: 1;
}

.sub-title {
  font-size: 12px;
  color: #999;
  margin: 0;
  line-height: 1;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.school-label {
  font-size: 14px;
  color: #666;
}

.menu-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f5f7fa;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-btn:hover {
  background: #e5e7eb;
  color: #667eea;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: #f5f7fa;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.user-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  overflow: hidden;
  position: relative;
}

.avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar .avatar-text {
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.user-details h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px 0;
}

.meta {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.tab-nav {
  background: white;
  border-radius: 12px;
  padding: 8px;
  margin-bottom: 24px;
  display: flex;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  text-decoration: none;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-item:hover {
  background: #f5f7fa;
  color: #667eea;
}

.tab-item.router-link-active {
  background: #667eea;
  color: white;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
