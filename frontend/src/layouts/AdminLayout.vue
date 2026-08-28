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
          <span class="date-chip">{{ currentDate }}</span>
        </div>
        <div class="user-info">
          <el-tag class="role-tag" effect="plain" size="small">{{ roleText }}</el-tag>
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
import { useAdminLayoutView } from './AdminLayout.logic'

const {
  Building2,
  currentDate,
  currentPageTitle,
  handleLogout,
  LogOut,
  menuItems,
  roleText,
  User,
  userStore
} = useAdminLayoutView()
</script>

<style scoped src="./AdminLayout.css"></style>

