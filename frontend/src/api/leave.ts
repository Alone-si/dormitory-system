import request from '../utils/request'
import type { Leave } from '../types'

export const leaveApi = {
  // 获取所有请假
  getAll: () => {
    return request.get<any, Leave[]>('/leaves')
  },
  
  // 获取学生的请假记录
  getByStudent: (studentId: number) => {
    return request.get<any, Leave[]>(`/leaves/student/${studentId}`)
  },
  
  // 获取待审批请假
  getPending: () => {
    return request.get<any, Leave[]>('/leaves/pending')
  },
  
  // 创建请假
  create: (data: Partial<Leave>) => {
    return request.post<any, Leave>('/leaves', data)
  },
  
  // 批准请假
  approve: (id: number, approverId: number, comment?: string) => {
    return request.post<any, Leave>(`/leaves/${id}/approve`, {
      approverId,
      comment
    })
  },
  
  // 拒绝请假
  reject: (id: number, approverId: number, comment?: string) => {
    return request.post<any, Leave>(`/leaves/${id}/reject`, {
      approverId,
      comment
    })
  }
}
