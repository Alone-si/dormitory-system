import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, UserRole } from '../types'

export const useUserStore = defineStore('user', () => {
  // 从localStorage恢复数据
  const token = ref<string>(localStorage.getItem('token') || '')
  const storedUserInfo = localStorage.getItem('userInfo')
  const userInfo = ref<User | null>(storedUserInfo ? JSON.parse(storedUserInfo) : null)
  const role = ref<UserRole | null>(userInfo.value?.role || null)
  
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUserInfo = (info: User) => {
    userInfo.value = info
    role.value = info.role
    // 持久化用户信息
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    role.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  const isAdmin = () => role.value === 'ADMIN'
  const isStudent = () => role.value === 'STUDENT'
  
  return {
    token,
    userInfo,
    role,
    setToken,
    setUserInfo,
    clearAuth,
    isAdmin,
    isStudent
  }
})
