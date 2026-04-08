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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Users, Home, TrendingUp, AlertCircle, Wrench, FileText } from 'lucide-vue-next'
import { dashboardApi } from '../../api/dashboard'
import { useUserStore } from '../../stores/user'
import type { DashboardStats } from '../../types'

const router = useRouter()
const userStore = useUserStore()

const stats = ref<DashboardStats | null>(null)
const repairChartRef = ref<HTMLElement>()
const buildingChartRef = ref<HTMLElement>()

const loadStats = async () => {
  try {
    stats.value = await dashboardApi.getStats()
    initCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const initCharts = () => {
  if (!stats.value) return
  
  // 报修类型分布图
  if (repairChartRef.value) {
    const repairChart = echarts.init(repairChartRef.value)
    const repairData = Object.entries(stats.value.repairsByType).map(([name, value]) => ({
      name: getRepairTypeName(name),
      value
    }))
    
    repairChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold' }
        },
        data: repairData
      }]
    })
  }
  
  // 楼栋房间数图
  if (buildingChartRef.value) {
    const buildingChart = echarts.init(buildingChartRef.value)
    const buildingData = Object.entries(stats.value.roomsByBuilding)
    
    buildingChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: buildingData.map(([name]) => name)
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: buildingData.map(([, value]) => value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [8, 8, 0, 0]
        }
      }]
    })
  }
}

const getRepairTypeName = (type: string) => {
  const map: Record<string, string> = {
    ELECTRICAL: '电路问题',
    PLUMBING: '水管问题',
    FURNITURE: '家具损坏',
    DOOR_WINDOW: '门窗问题',
    NETWORK: '网络问题',
    OTHER: '其他'
  }
  return map[type] || type
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.dashboard-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 20px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin: 0 0 8px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
}

.chart {
  width: 100%;
  height: 300px;
}

.quick-actions {
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.quick-actions h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 56px;
  border: none;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-action-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.quick-action-btn:active {
  transform: translateY(-2px) scale(0.98);
}

.students-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.rooms-btn {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.repairs-btn {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.leaves-btn {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}
</style>
