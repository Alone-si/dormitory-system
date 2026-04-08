import request from '../utils/request'
import type { LoginRequest, LoginResponse } from '../types'

export const authApi = {
  // 登录
  login: (data: LoginRequest) => {
    return request.post<any, LoginResponse>('/auth/login', data)
  }
}
