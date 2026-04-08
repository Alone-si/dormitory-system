<template>
  <div class="rooms-view">
    <div class="view-header">
      <h2>宿舍管理</h2>
      <div class="header-filters">
        <el-select
          v-model="genderFilter"
          placeholder="宿舍类型"
          style="width: 120px"
          size="default"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="男生宿舍" value="MALE" />
          <el-option label="女生宿舍" value="FEMALE" />
        </el-select>
        <el-select
          v-model="buildingFilter"
          placeholder="选择楼栋"
          style="width: 140px"
          size="default"
          clearable
          popper-class="room-filter-dropdown"
        >
          <el-option label="全部楼栋" value="" />
          <el-option
            v-for="building in uniqueBuildings"
            :key="building"
            :label="building"
            :value="building"
          />
        </el-select>
        <el-select
          v-model="classFilter"
          placeholder="选择班级"
          style="width: 140px"
          size="default"
          clearable
          popper-class="room-filter-dropdown"
        >
          <el-option label="全部班级" value="" />
          <el-option
            v-for="className in uniqueClasses"
            :key="className"
            :label="className"
            :value="className"
          />
        </el-select>
        <div v-if="hasActiveFilters" class="filter-stats">
          <span class="stats-text">
            筛选结果：{{ filteredRooms.length }} 间宿舍
            <span v-if="filteredRooms.length !== rooms.length" class="total-count">
              / {{ rooms.length }}
            </span>
          </span>
        </div>
        <button 
          v-if="hasActiveFilters"
          class="reset-filter-btn" 
          @click="resetFilters"
        >
          <span>重置筛选</span>
        </button>
      </div>
    </div>
    
    <div class="rooms-grid" v-loading="loading">
      <div v-for="room in paginatedRooms" :key="room.id" class="room-card">
        <div class="room-header">
          <div class="room-title">
            <Home :size="14" />
            <span>{{ room.building.name }} - {{ room.roomNumber }}</span>
          </div>
          <div class="status-tag" :class="`tag-${getStatusType(room.status)}`">
            {{ getStatusText(room.status) }}
          </div>
        </div>
        
        <div class="room-info">
          <div class="info-item">
            <span class="label">楼层：</span>
            <span class="value">{{ room.floor }}层</span>
          </div>
          <div class="info-item">
            <span class="label">类型：</span>
            <div class="type-tag" :class="room.building.type === 'MALE' ? 'tag-primary' : 'tag-danger'">
              {{ room.building.type === 'MALE' ? '男生宿舍' : '女生宿舍' }}
            </div>
          </div>
        </div>
        
        <div class="occupancy-section">
          <div class="occupancy-header">
            <span>入住情况</span>
            <span class="occupancy-text">{{ room.occupied }}/{{ room.capacity }}</span>
          </div>
          <el-progress 
            :percentage="Math.round((room.occupied / room.capacity) * 100)" 
            :color="getProgressColor(room.occupied, room.capacity)"
            :stroke-width="4"
          />
        </div>
        
        <!-- 学生信息田字格 -->
        <div class="students-section" v-if="room.students && room.students.length > 0">
          <div class="students-header">
            <Users :size="12" />
            <span>入住学生</span>
          </div>
          <div class="students-grid">
            <div 
              v-for="student in room.students" 
              :key="student.id" 
              class="student-cell clickable"
              @click="viewStudentDetail(student)"
              :title="`点击查看 ${student.name} 的详细信息`"
            >
              <div class="student-name">{{ student.name }}</div>
              <div class="student-id">{{ student.studentId }}</div>
              <div class="student-class">
                <span v-if="student.className" class="class-tag">{{ student.className }}</span>
                <span v-else class="class-tag no-class">未分班</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="room.occupied > 0" class="empty-room warning-state">
           <AlertCircle :size="12" />
           <span>暂无学生详细信息</span>
        </div>

        <div v-else class="empty-room vacant-state clickable" 
             @click="addStudentToRoom(room)"
             title="点击为此宿舍添加学生">
          <Home :size="12" />
          <span>暂无学生入住</span>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && rooms.length === 0" description="暂无宿舍数据" />
    
    <!-- 分页控件 -->
    <div v-if="filteredRooms.length > 0" class="pagination-container">
      <div class="page-size-selector">
        <span class="label">每页显示：</span>
        <select v-model="pageSize" @change="handlePageSizeChange" class="size-select">
          <option :value="21">21条</option>
          <option :value="42">42条</option>
          <option :value="63">63条</option>
          <option :value="84">84条</option>
        </select>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredRooms.length"
        layout="prev, pager, next, jumper, total"
        @current-change="handlePageChange"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Home, Users, AlertCircle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roomApi } from '../../api/room'
import request from '../../utils/request'
import type { Room } from '../../types'

const router = useRouter()

const loading = ref(false)
const rooms = ref<Room[]>([])
const currentPage = ref(1)
const pageSize = ref(21)

// 筛选条件
const genderFilter = ref('')
const buildingFilter = ref('')
const classFilter = ref('')

// 获取唯一的楼栋列表
const uniqueBuildings = computed(() => {
  const buildings = new Set(rooms.value.map(room => room.building.name))
  return Array.from(buildings).sort()
})

// 获取唯一的班级列表
const uniqueClasses = computed(() => {
  const classes = new Set<string>()
  rooms.value.forEach(room => {
    if (room.students && room.students.length > 0) {
      room.students.forEach(student => {
        if (student.className) {
          classes.add(student.className)
        }
      })
    }
  })
  return Array.from(classes).sort()
})

// 检查是否有激活的筛选器
const hasActiveFilters = computed(() => {
  return genderFilter.value || buildingFilter.value || classFilter.value
})

// 重置筛选
const resetFilters = () => {
  genderFilter.value = ''
  buildingFilter.value = ''
  classFilter.value = ''
  currentPage.value = 1
}

// 筛选后的宿舍列表
const filteredRooms = computed(() => {
  let filtered = rooms.value

  // 性别筛选
  if (genderFilter.value) {
    filtered = filtered.filter(room => room.building.type === genderFilter.value)
  }

  // 楼栋筛选
  if (buildingFilter.value) {
    filtered = filtered.filter(room => room.building.name === buildingFilter.value)
  }

  // 班级筛选
  if (classFilter.value) {
    filtered = filtered.filter(room => {
      if (!room.students || room.students.length === 0) return false
      return room.students.some(student => student.className === classFilter.value)
    })
  }

  return filtered
})

const paginatedRooms = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRooms.value.slice(start, end)
})

const totalOccupied = computed(() => {
  return rooms.value.reduce((total, room) => total + room.occupied, 0)
})

const handlePageChange = (page: number) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePageSizeChange = () => {
  currentPage.value = 1
}

const loadRooms = async () => {
  loading.value = true
  try {
    const data: Room[] = await request.get('/rooms/with-students')
    rooms.value = data
  } catch (error) {
    ElMessage.error('加载房间列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    AVAILABLE: 'success',
    FULL: 'warning',
    MAINTENANCE: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    AVAILABLE: '可入住',
    FULL: '已满',
    MAINTENANCE: '维护中'
  }
  return map[status] || status
}

const getProgressColor = (occupied: number, capacity: number) => {
  const rate = occupied / capacity
  if (rate < 0.5) return '#67c23a'
  if (rate < 0.8) return '#e6a23c'
  return '#f56c6c'
}

// 查看学生详情
const viewStudentDetail = (student: any) => {
  // 生成头像显示内容
  const getAvatarContent = () => {
    if (student.avatar) {
      // 如果有自定义头像，显示图片
      return `<img src="${student.avatar}" alt="${student.name}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;" />`
    } else {
      // 没有头像则显示姓名首字母
      const firstChar = student.name ? student.name.charAt(0) : '?'
      return `<div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 600; color: white;">${firstChar}</div>`
    }
  }

  ElMessageBox({
    title: '',
    message: `
      <div class="simple-student-detail">
        <div class="student-header">
          <div class="student-avatar">
            ${getAvatarContent()}
          </div>
          <div class="student-info">
            <h3>${student.name}</h3>
            <p>学号：${student.studentId}</p>
          </div>
        </div>
        
        <div class="student-details">
          <div class="detail-row">
            <span class="label">班级</span>
            <span class="value">${student.className || '未分班'}</span>
          </div>
          <div class="detail-row">
            <span class="label">性别</span>
            <span class="value">${student.gender === 'MALE' ? '男' : '女'}</span>
          </div>
          ${student.phone ? `
          <div class="detail-row">
            <span class="label">电话</span>
            <span class="value">${student.phone}</span>
          </div>
          ` : ''}
        </div>
      </div>
    `,
    showCancelButton: false,
    confirmButtonText: '关闭',
    dangerouslyUseHTMLString: true,
    customClass: 'simple-student-dialog',
    center: true
  })
}

// 为宿舍添加学生
const addStudentToRoom = (room: any) => {
  ElMessageBox({
    title: '',
    message: `
      <div class="simple-add-student-dialog">
        <div class="dialog-header">
          <div class="header-icon">🏠</div>
          <div class="header-content">
            <div class="room-title">${room.building.name} - ${room.roomNumber}</div>
            <div class="room-subtitle">为此宿舍添加学生</div>
          </div>
        </div>
        
        <div class="room-stats">
          <div class="stat-item">
            <div class="stat-icon">🏠</div>
            <div class="stat-number">${room.capacity}</div>
            <div class="stat-label">房间容量</div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">👥</div>
            <div class="stat-number">${room.occupied}</div>
            <div class="stat-label">当前入住</div>
          </div>
          <div class="stat-item available">
            <div class="stat-icon">✨</div>
            <div class="stat-number">${room.capacity - room.occupied}</div>
            <div class="stat-label">剩余床位</div>
          </div>
        </div>
        
        <div class="action-guide">
          <div class="guide-icon">💡</div>
          <div class="guide-text">
            请前往学生管理页面进行批量入住操作<br>
            或在学生详情页面为单个学生分配宿舍
          </div>
        </div>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: '前往学生管理',
    cancelButtonText: '取消',
    dangerouslyUseHTMLString: true,
    customClass: 'simple-add-student-dialog-box',
    center: true
  }).then(() => {
    // 跳转到学生管理页面
    router.push('/admin/students')
  }).catch(() => {
    // 用户取消，不做任何操作
  })
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.rooms-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  overflow: hidden;
  box-sizing: border-box;
}

.view-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.view-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

/* 筛选器样式 - 苹果风格 */
.header-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

/* 下拉框苹果化样式 */
.header-filters :deep(.el-input__wrapper) {
  height: 32px;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  background: white;
}

.header-filters :deep(.el-input__wrapper:hover) {
  border-color: #3b82f6;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.15);
}

.header-filters :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.header-filters :deep(.el-select .el-input__wrapper) {
  height: 32px;
}

.header-filters :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
}

.header-filters :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
  font-weight: 400;
}

/* 筛选结果统计 - 苹果风格 */
.filter-stats {
  display: inline-flex;
  align-items: center;
  padding: 6px 15px;
  height: 32px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(37, 99, 235, 0.08) 100%);
  border: 1px solid rgba(59, 130, 246, 0.25);
  border-radius: 16px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.1);
  transition: all 0.2s ease;
}

.filter-stats:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(37, 99, 235, 0.12) 100%);
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.15);
}

.filter-stats .stats-text {
  font-size: 13px;
  color: #1e40af;
  font-weight: 600;
}

.filter-stats .total-count {
  color: #6b7280;
  font-weight: 500;
  margin-left: 2px;
}

/* 重置按钮 - 苹果风格 */
.reset-filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 15px;
  height: 32px;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(107, 114, 128, 0.3);
}

.reset-filter-btn:hover {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.4);
}

.reset-filter-btn:active {
  transform: translateY(-1px) scale(0.98);
  box-shadow: 0 2px 6px rgba(107, 114, 128, 0.35);
}

/* 宿舍卡片网格 */
.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  grid-auto-rows: min-content;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  margin-bottom: 16px;
  align-content: start;
}

.room-card {
  background: white;
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.room-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
}

.status-tag, .type-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 状态标签颜色 */
.tag-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.tag-danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

/* 房间信息 */
.room-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.info-item .label {
  color: #6b7280;
}

.info-item .value {
  color: #1f2937;
  font-weight: 500;
}

/* 入住情况 */
.occupancy-section {
  margin-bottom: 8px;
}

.occupancy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 11px;
  color: #6b7280;
}

.occupancy-text {
  font-weight: 600;
  color: #1f2937;
}

/* 学生信息区域 */
.students-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.students-header {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: #6b7280;
  margin-bottom: 6px;
  font-weight: 600;
}

/* 学生信息田字格 */
.students-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
  margin-top: 4px;
}

.student-cell {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 5px;
  padding: 6px;
  font-size: 10px;
  transition: all 0.2s ease;
  text-align: center;
}

.student-cell:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.student-cell.clickable {
  cursor: pointer;
}

.student-cell.clickable:hover {
  background: #dbeafe;
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.2);
}

.student-cell.clickable:active {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.student-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
  font-size: 11px;
}

.student-id {
  color: #6b7280;
  font-size: 9px;
  margin-bottom: 3px;
}

.student-class {
  display: flex;
  justify-content: center;
}

.class-tag {
  display: inline-block;
  font-size: 8px;
  padding: 1px 3px;
  border-radius: 3px;
  font-weight: 600;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  color: white;
  box-shadow: 0 1px 2px rgba(20, 184, 166, 0.3);
}

.class-tag.no-class {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
}

/* 空状态 */
.empty-room {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.empty-room.clickable {
  cursor: pointer;
}

.empty-room.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.warning-state {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
  border: 1px solid #fbbf24;
}

.vacant-state {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.vacant-state.clickable:hover {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8;
  border-color: #3b82f6;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  margin-top: auto;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-size-selector .label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.size-select {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #1f2937;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.size-select:hover {
  border-color: #3b82f6;
}

.size-select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
</style>

<style>
/* 全局样式 - 简洁学生详情对话框 */
.simple-student-dialog.el-message-box {
  border-radius: 12px !important;
  padding: 0 !important;
  max-width: 380px !important;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15) !important;
  background: white !important;
  border: 1px solid #e5e7eb !important;
}

.simple-student-dialog.el-message-box .el-message-box__header {
  display: none !important;
}

.simple-student-dialog.el-message-box .el-message-box__content {
  padding: 0 !important;
}

.simple-student-dialog.el-message-box .el-message-box__message {
  padding: 0 !important;
  margin: 0 !important;
}

.simple-student-dialog.el-message-box .el-message-box__btns {
  padding: 16px 24px !important;
  border-top: 1px solid #f3f4f6 !important;
  background: #fafafa !important;
  border-radius: 0 0 12px 12px !important;
  text-align: center !important;
}

.simple-student-dialog.el-message-box .el-button--primary {
  background: #3b82f6 !important;
  border: none !important;
  color: white !important;
  padding: 10px 24px !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  font-size: 14px !important;
  transition: all 0.2s ease !important;
}

.simple-student-dialog.el-message-box .el-button--primary:hover {
  background: #2563eb !important;
  transform: translateY(-1px) !important;
}

/* 简洁学生详情内容 */
.simple-student-detail {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.student-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px 12px 0 0;
}

.student-avatar {
  font-size: 40px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
}

.student-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.student-info p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.student-details {
  padding: 20px 24px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.detail-row .value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
}

/* 简洁添加学生对话框 */
.simple-add-student-dialog-box.el-message-box {
  border-radius: 16px !important;
  padding: 0 !important;
  margin: 0 !important;
  max-width: 420px !important;
  width: 90% !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3) !important;
  border: none !important;
  overflow: hidden !important;
}

.simple-add-student-dialog-box.el-message-box .el-message-box__header {
  display: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

.simple-add-student-dialog-box.el-message-box .el-message-box__content {
  padding: 0 !important;
  margin: 0 !important;
}

.simple-add-student-dialog-box.el-message-box .el-message-box__message {
  padding: 0 !important;
  margin: 0 !important;
}

.simple-add-student-dialog-box.el-message-box .el-message-box__btns {
  padding: 20px 24px !important;
  border-top: 1px solid #f3f4f6 !important;
  background: #fafafa !important;
  border-radius: 0 0 12px 12px !important;
  display: flex !important;
  gap: 12px !important;
}

.simple-add-student-dialog-box.el-message-box .el-button {
  flex: 1 !important;
  padding: 10px 20px !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  font-size: 14px !important;
  transition: all 0.2s ease !important;
}

.simple-add-student-dialog-box.el-message-box .el-button--default {
  background: white !important;
  color: #6b7280 !important;
  border: 1px solid #d1d5db !important;
}

.simple-add-student-dialog-box.el-message-box .el-button--default:hover {
  background: #f9fafb !important;
  border-color: #9ca3af !important;
  transform: translateY(-1px) !important;
}

.simple-add-student-dialog-box.el-message-box .el-button--primary {
  background: #3b82f6 !important;
  border: none !important;
  color: white !important;
}

.simple-add-student-dialog-box.el-message-box .el-button--primary:hover {
  background: #2563eb !important;
  transform: translateY(-1px) !important;
}

/* 简洁添加学生内容 */
.simple-add-student-dialog {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 0;
}

.simple-add-student-dialog .dialog-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 0;
  margin: 0;
}

.simple-add-student-dialog .header-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.simple-add-student-dialog .header-content {
  flex: 1;
}

.simple-add-student-dialog .room-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin-bottom: 2px;
}

.simple-add-student-dialog .room-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

.room-stats {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: #f8fafc;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 16px 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.stat-item.available {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stat-icon {
  font-size: 20px;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-item.available .stat-number {
  color: #059669;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.action-guide {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px 24px 24px 24px;
  background: white;
}

.guide-icon {
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.guide-text {
  flex: 1;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}



/* 隐藏页面级别的滚动条 */
body {
  overflow: hidden !important;
}

/* 筛选下拉框样式 - 限制高度并添加滚动条 */
.room-filter-dropdown {
  max-height: 200px !important;
}

.room-filter-dropdown .el-select-dropdown__wrap {
  max-height: 200px !important;
}

.room-filter-dropdown .el-scrollbar__view {
  max-height: 200px !important;
}

/* 下拉选项样式优化 */
.room-filter-dropdown .el-select-dropdown__item {
  height: 36px;
  line-height: 36px;
  padding: 0 16px;
  font-size: 13px;
  color: #374151;
  transition: all 0.2s ease;
}

.room-filter-dropdown .el-select-dropdown__item:hover {
  background: rgba(59, 130, 246, 0.08);
  color: #1e40af;
}

.room-filter-dropdown .el-select-dropdown__item.selected {
  background: rgba(59, 130, 246, 0.12);
  color: #1e40af;
  font-weight: 600;
}

/* 对话框内容样式 - 保留用于其他功能 */
</style>
