import request from '../utils/request'

/**
 * 管理员类型枚举
 */
export enum AdminType {
  SUPER_ADMIN = 'SUPER_ADMIN',
  NORMAL_ADMIN = 'NORMAL_ADMIN'
}

/**
 * 管理员类型定义
 */
export interface Admin {
  id: number
  name: string
  username: string
  phone: string
  status: string
  adminType: AdminType
  createdAt: string
  updatedAt: string
}

/**
 * 管理员请求类型
 */
export interface AdminRequest {
  name: string
  username: string
  phone: string
  adminType: AdminType
}

/**
 * 管理员管理API
 */
export const adminApi = {
  // 获取所有管理员列表
  getAdmins: () => {
    return request.get<any, Admin[]>('/users/admins')
  },

  // 创建新管理员
  createAdmin: (data: AdminRequest) => {
    return request.post<any, Admin>('/users/admins', data)
  },

  // 更新管理员信息
  updateAdmin: (id: number, data: AdminRequest) => {
    return request.put<any, Admin>(`/users/admins/${id}`, data)
  },

  // 删除管理员
  deleteAdmin: (id: number) => {
    return request.delete<any, void>(`/users/admins/${id}`)
  },

  // 重置管理员密码
  resetPassword: (id: number) => {
    return request.put<any, void>(`/users/admins/${id}/reset-password`)
  }
}
