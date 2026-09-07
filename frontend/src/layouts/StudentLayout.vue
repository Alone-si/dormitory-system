<template>
  <div class="student-layout">
    <aside class="student-sidebar">
      <div class="student-sidebar-titlebar">
        <div class="student-brand">
          <div class="logo-wrapper">
            <Building2 :size="18" class="logo-icon" />
          </div>
          <div class="title-section">
            <h1 class="main-title">我的宿舍</h1>
            <p class="sub-title">Dorm</p>
          </div>
        </div>
      </div>

      <div class="student-account">
        <div class="student-avatar">
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :alt="userStore.userInfo.name" />
          <span v-else>{{ userStore.userInfo?.name?.charAt(0) || 'U' }}</span>
        </div>
        <strong>{{ userStore.userInfo?.name }}</strong>
        <span>{{ userStore.userInfo?.studentId }}</span>
      </div>

      <nav class="student-nav">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="student-nav-item">
          <component :is="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="student-sidebar-footer">
        <button v-if="isReadOnly" class="unlock-button" @click="changePassword">修改密码解锁</button>
        <button @click="handleLogout" class="student-logout"><LogOut :size="17" /><span>退出登录</span></button>
      </div>
    </aside>

    <div class="student-main">
      <header class="student-topbar">
        <div>
          <h1>{{ currentPage.label }}</h1>
          <p>{{ currentPage.description }}</p>
        </div>
        <div class="student-topbar-actions">
          <span class="student-date">{{ currentDate }} · {{ currentSemester }}</span>
          <span class="student-role">学生</span>
          <button @click="handleLogout" class="mobile-logout" title="退出登录"><LogOut :size="18" /></button>
        </div>
      </header>

      <main class="student-content">
        <div v-if="isReadOnly" class="readonly-banner">
          <span>当前为只读访客模式，只能查看信息。</span>
          <el-button type="primary" plain size="small" @click="changePassword">修改密码并解锁操作</el-button>
        </div>

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
import { useStudentLayoutView } from './StudentLayout.logic'

const {
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
} = useStudentLayoutView()
</script>
