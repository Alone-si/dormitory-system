import request from '../utils/request'
import type { Notice, NoticeTarget } from '../types'

export const noticeApi = {
  // 获取所有通知
  getAll: () => {
    return request.get<any, Notice[]>('/notices')
  },
  
  // 根据目标获取通知
  getByTarget: (target: NoticeTarget) => {
    return request.get<any, Notice[]>(`/notices/target/${target}`)
  },
  
  // 创建通知
  create: (data: Partial<Notice>) => {
    return request.post<any, Notice>('/notices', data)
  },
  
  // 更新通知
  update: (id: number, data: Partial<Notice>) => {
    return request.put<any, Notice>(`/notices/${id}`, data)
  },
  
  // 删除通知
  delete: (id: number) => {
    return request.delete<any, void>(`/notices/${id}`)
  }
}
