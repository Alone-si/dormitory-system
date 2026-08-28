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
        <span class="date-chip">{{ currentDate }}</span>
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
        <div v-if="isReadOnly" class="readonly-banner">
          <span>当前为只读访客模式，只能查看信息。</span>
          <el-button type="primary" plain size="small" @click="changePassword">修改密码并解锁操作</el-button>
        </div>

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
import { useStudentLayoutView } from './StudentLayout.logic'

const {
  Bell,
  Building2,
  currentDate,
  currentSemester,
  changePassword,
  FileText,
  handleLogout,
  Home,
  isReadOnly,
  LogOut,
  roomText,
  Settings,
  userStore,
  Wrench
} = useStudentLayoutView()
</script>

<style scoped src="./StudentLayout.css"></style>
