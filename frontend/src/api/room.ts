import request from '../utils/request'
import type { Room } from '../types'

export const roomApi = {
  // 获取所有房间
  getAll: () => {
    return request.get<any, Room[]>('/rooms')
  },
  
  // 获取可用房间
  getAvailable: () => {
    return request.get<any, Room[]>('/rooms/available')
  },
  
  // 根据楼栋获取房间
  getByBuilding: (buildingId: number) => {
    return request.get<any, Room[]>(`/rooms/building/${buildingId}`)
  },
  
  // 分配学生到房间
  assignStudent: (studentId: number, roomId: number) => {
    return request.post<any, Room>('/rooms/assign', null, {
      params: { studentId, roomId }
    })
  },
  
  // 学生退宿
  removeStudent: (studentId: number) => {
    return request.post<any, Room>(`/rooms/remove/${studentId}`)
  }
}
