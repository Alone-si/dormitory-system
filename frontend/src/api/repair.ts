import request from '../utils/request'
import type { Repair, RepairStatus } from '../types'

export const repairApi = {
  // 获取所有报修
  getAll: () => {
    return request.get<any, Repair[]>('/repairs')
  },
  
  // 获取学生的报修记录
  getByStudent: (studentId: number) => {
    return request.get<any, Repair[]>(`/repairs/student/${studentId}`)
  },
  
  // 获取待处理报修
  getPending: () => {
    return request.get<any, Repair[]>('/repairs/pending')
  },
  
  // 创建报修
  create: (data: Partial<Repair>) => {
    return request.post<any, Repair>('/repairs', data)
  },
  
  // 更新报修状态
  updateStatus: (id: number, status: RepairStatus, handlerId?: number, reply?: string) => {
    return request.put<any, Repair>(`/repairs/${id}/status`, {
      status,
      handlerId,
      reply
    })
  }
}
