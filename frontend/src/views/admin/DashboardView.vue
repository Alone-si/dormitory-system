<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>管理员仪表盘</h1>
      <p>欢迎回来，{{ userStore.userInfo?.name }}</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <Users :size="32" />
        </div>
        <div class="stat-content">
          <p class="stat-label">在校学生</p>
          <h2 class="stat-value">{{ stats?.totalStudents || 0 }}</h2>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <Home :size="32" />
        </div>
        <div class="stat-content">
          <p class="stat-label">宿舍总数</p>
          <h2 class="stat-value">{{ stats?.totalRooms || 0 }}</h2>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <TrendingUp :size="32" />
        </div>
        <div class="stat-content">
          <p class="stat-label">入住率</p>
          <h2 class="stat-value">{{ stats?.occupancyRate?.toFixed(1) || 0 }}%</h2>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
          <AlertCircle :size="32" />
        </div>
        <div class="stat-content">
          <p class="stat-label">待处理事项</p>
          <h2 class="stat-value">{{ (stats?.pendingRepairs || 0) + (stats?.pendingLeaves || 0) }}</h2>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <h3>报修类型分布</h3>
        <div ref="repairChartRef" class="chart"></div>
      </div>
      
      <div class="chart-card">
        <h3>各楼栋房间数</h3>
        <div ref="buildingChartRef" class="chart"></div>
      </div>
    </div>
    
    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <button class="quick-action-btn students-btn" @click="router.push('/admin/students')">
          <Users :size="20" />
          <span>学生管理</span>
        </button>
        <button class="quick-action-btn rooms-btn" @click="router.push('/admin/rooms')">
          <Home :size="20" />
          <span>宿舍管理</span>
        </button>
        <button class="quick-action-btn repairs-btn" @click="router.push('/admin/repairs')">
          <Wrench :size="20" />
          <span>报修处理</span>
        </button>
        <button class="quick-action-btn leaves-btn" @click="router.push('/admin/leaves')">
          <FileText :size="20" />
          <span>缺寝请假</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDashboardView } from './DashboardView.logic'

const {
  AlertCircle,
  buildingChartRef,
  FileText,
  Home,
  repairChartRef,
  router,
  stats,
  TrendingUp,
  Users,
  userStore,
  Wrench
} = useDashboardView()
</script>

<style scoped src="./DashboardView.css"></style>
