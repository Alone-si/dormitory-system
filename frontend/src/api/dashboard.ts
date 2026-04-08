import request from '../utils/request'
import type { DashboardStats } from '../types'

export const dashboardApi = {
  // 获取仪表盘统计数据
  getStats: () => {
    return request.get<any, DashboardStats>('/dashboard/stats')
  }
}
