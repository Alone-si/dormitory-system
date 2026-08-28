import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { init, graphic, use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, BarChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])
import { Users, Home, TrendingUp, AlertCircle, Wrench, FileText } from 'lucide-vue-next'
import { dashboardApi } from '../../api/dashboard'
import { useUserStore } from '../../stores/user'
import type { DashboardStats } from '../../types'

export function useDashboardView() {
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
    const repairChart = init(repairChartRef.value)
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
    const buildingChart = init(buildingChartRef.value)
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
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#69bda6' },
            { offset: 1, color: '#4b9c84' }
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
  return {
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
  }
}