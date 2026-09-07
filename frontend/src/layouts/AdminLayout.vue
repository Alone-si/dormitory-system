<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-titlebar">
        <div class="logo-section">
          <div class="brand-mark"><Building2 :size="18" /></div>
          <div class="brand-copy">
            <h2>宿舍管理</h2>
            <span>Dorm</span>
          </div>
        </div>
      </div>
      
      <nav class="nav-menu">
        <div v-for="group in menuGroups" :key="group.label" class="nav-group">
          <span class="nav-group-label">{{ group.label }}</span>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon"><component :is="item.icon" :size="18" /></span>
            <span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>
      
      <div class="sidebar-footer">
        <div class="account-avatar">{{ userStore.userInfo?.name?.charAt(0) || 'A' }}</div>
        <div class="account-copy">
          <strong>{{ userStore.userInfo?.name }}</strong>
          <span>{{ roleText }}</span>
        </div>
        <button @click="handleLogout" class="logout-btn" title="退出登录">
          <LogOut :size="17" />
        </button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <header class="top-bar">
        <div class="page-context">
          <h1>{{ currentPageTitle }}</h1>
          <p>{{ currentPageDescription }}</p>
        </div>
        <div class="toolbar-meta">
          <span class="toolbar-date">{{ currentDate }}</span>
          <span class="live-dot">运行正常</span>
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
  currentPageDescription,
  currentPageTitle,
  handleLogout,
  LogOut,
  menuGroups,
  roleText,
  userStore
} = useAdminLayoutView()
</script>
