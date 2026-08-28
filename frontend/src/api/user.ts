import axios from 'axios'
import type { ApiResponse, User } from '../types'

const API_BASE_URL = '/api'

// 创建axios实例并添加token
const createAuthHeaders = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

// 获取当前用户信息
export const getCurrentUser = async (): Promise<ApiResponse<User>> => {
  const response = await axios.get(`${API_BASE_URL}/users/current`, {
    headers: createAuthHeaders()
  })
  return response.data
}

// 更新用户信息
export const updateUser = async (userData: Partial<User>): Promise<ApiResponse<User>> => {
  const response = await axios.put(`${API_BASE_URL}/users/current`, userData, {
    headers: createAuthHeaders()
  })
  return response.data
}

// 上传头像
export const uploadAvatar = async (file: File): Promise<ApiResponse<{ avatarUrl: string }>> => {
  const formData = new FormData()
  formData.append('avatar', file)
  
  const response = await axios.post(`${API_BASE_URL}/users/avatar`, formData, {
    headers: {
      ...createAuthHeaders(),
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

// 删除头像
export const removeAvatar = async (): Promise<ApiResponse<void>> => {
  const response = await axios.delete(`${API_BASE_URL}/users/avatar`, {
    headers: createAuthHeaders()
  })
  return response.data
}

// 修改密码
export const changePassword = async (passwordData: {
  currentPassword: string
  newPassword: string
}): Promise<ApiResponse<void>> => {
  const response = await axios.put(`${API_BASE_URL}/users/password`, passwordData, {
    headers: createAuthHeaders()
  })
  return response.data
}

// 更新显示名称
export const updateDisplayName = async (displayName: string): Promise<ApiResponse<void>> => {
  const response = await axios.put(`${API_BASE_URL}/users/display-name`, { displayName }, {
    headers: createAuthHeaders()
  })
  return response.data
}

// 更新邮箱
export const updateEmail = async (email: string): Promise<ApiResponse<User>> => {
  const response = await axios.put(`${API_BASE_URL}/users/email`, { email }, {
    headers: createAuthHeaders()
  })
  return response.data
}
