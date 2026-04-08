// 用户角色
export type UserRole = 'ADMIN' | 'STUDENT'

// 管理员类型
export type AdminType = 'SUPER_ADMIN' | 'NORMAL_ADMIN'

// 性别
export type Gender = 'MALE' | 'FEMALE'

// 用户
export interface User {
  id: number
  username: string
  name: string
  studentId?: string
  displayName?: string
  role: UserRole
  adminType?: AdminType
  gender?: Gender
  phone?: string
  email?: string
  avatar?: string
  className?: string
  room?: Room
  status?: string
  createdAt?: string
  updatedAt?: string
}

// 楼栋
export interface Building {
  id: number
  name: string
  type: Gender
  floors: number
  address?: string
  rooms?: Room[]
  createdAt?: string
}

// 楼栋简化信息（用于房间列表）
export interface BuildingSimple {
  id: number
  name: string
  type: Gender
  floors: number
  address?: string
}

// 房间状态
export type RoomStatus = 'AVAILABLE' | 'FULL' | 'MAINTENANCE'

// 学生简化信息（用于房间列表）- 重命名为 UserSimple 以保持一致性
export interface UserSimple {
  id: number
  name: string
  studentId: string
  gender: string
  phone?: string
  className?: string
  avatar?: string
}

// 房间
export interface Room {
  id: number
  building: BuildingSimple
  roomNumber: string
  floor: number
  capacity: number
  occupied: number
  status: RoomStatus
  students?: UserSimple[]
  dormContract?: string
  createdAt?: string
  updatedAt?: string
}

// 报修类型
export type RepairType = 'ELECTRICAL' | 'PLUMBING' | 'FURNITURE' | 'DOOR_WINDOW' | 'NETWORK' | 'OTHER'

// 报修状态
export type RepairStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED'

// 紧急程度
export type UrgencyLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

// 报修
export interface Repair {
  id: number
  student: User
  room: Room
  type: RepairType
  description: string
  location?: string
  status: RepairStatus
  urgency?: UrgencyLevel
  adminReply?: string
  handler?: User
  handledAt?: string
  createdAt?: string
  updatedAt?: string
}

// 请假类型
export type LeaveType = 'SICK' | 'PERSONAL' | 'FAMILY' | 'EMERGENCY' | 'OTHER'

// 请假状态
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

// 请假
export interface Leave {
  id: number
  student: User
  type: LeaveType
  startDate: string
  endDate: string
  days: number
  reason: string
  emergencyContact?: string
  emergencyPhone?: string
  status: LeaveStatus
  adminComment?: string
  approver?: User
  approvedAt?: string
  createdAt?: string
  updatedAt?: string
}

// 通知类型
export type NoticeType = 'ANNOUNCEMENT' | 'MAINTENANCE' | 'EVENT' | 'REGULATION' | 'EMERGENCY'

// 通知优先级
export type NoticePriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

// 通知目标
export type NoticeTarget = 'ALL' | 'MALE' | 'FEMALE'

// 通知
export interface Notice {
  id: number
  title: string
  content: string
  type: NoticeType
  priority: NoticePriority
  publisher: User
  target?: NoticeTarget
  pinned?: boolean
  publishedAt?: string
  createdAt?: string
  updatedAt?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
  role: UserRole
  userId: number
  username: string
  name: string
  user: User  // 完整的用户对象，包含room等关联数据
}

// API响应
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 仪表盘统计
export interface DashboardStats {
  totalStudents: number
  totalRooms: number
  occupiedRooms: number
  occupancyRate: number
  pendingRepairs: number
  pendingLeaves: number
  repairsByType: Record<string, number>
  roomsByBuilding: Record<string, number>
}
