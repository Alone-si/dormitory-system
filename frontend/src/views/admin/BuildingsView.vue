<template>
  <div class="buildings-view">
    <div class="view-header">
      <h2>宿舍楼管理</h2>
      <button class="create-btn" @click="showCreateDialog">
        <Plus :size="18" />
        <span>新建宿舍楼</span>
      </button>
    </div>
    
    <div class="buildings-grid" v-loading="loading">
      <div v-for="building in paginatedBuildings" :key="building.id" class="building-card">
        <div class="card-header">
          <div class="building-icon" :class="building.type === 'MALE' ? 'male-building' : 'female-building'">
            <Building2 :size="24" />
          </div>
          <div class="building-info">
            <h3 class="building-name">{{ building.name }}</h3>
            <div class="custom-tag" :class="building.type === 'MALE' ? 'tag-primary' : 'tag-danger'">
              {{ building.type === 'MALE' ? '男生宿舍楼' : '女生宿舍楼' }}
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <span class="label">楼层数：</span>
            <span class="value">{{ building.floors }} 层</span>
          </div>
          <div class="info-row">
            <span class="label">每层房间数：</span>
            <span class="value">{{ building.roomsPerFloor }} 间</span>
          </div>
          <div class="info-row">
            <span class="label">房间容量：</span>
            <span class="value">{{ building.capacity }} 人间</span>
          </div>
          <div class="info-row">
            <span class="label">总房间数：</span>
            <span class="value">{{ building.floors * building.roomsPerFloor }} 间</span>
          </div>
          <div class="info-row">
            <span class="label">入住率：</span>
            <span class="value" :class="getOccupancyClass(building.id)">
              {{ getOccupancyRate(building.id) }}
            </span>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="stats">
            <div class="stat-item">
              <Home :size="14" />
              <span>{{ getRoomCount(building.id) }} 间房</span>
            </div>
            <div class="stat-item">
              <Users :size="14" />
              <span>{{ getStudentCount(building.id) }} 人</span>
            </div>
          </div>
          <button 
            class="delete-btn" 
            @click="handleDelete(building)"
            :disabled="getStudentCount(building.id) > 0"
            :title="getStudentCount(building.id) > 0 ? '该宿舍楼还有学生入住，无法删除' : '删除宿舍楼'"
          >
            <Trash2 :size="14" />
          </button>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && buildings.length === 0" description="暂无宿舍楼数据" />
    
    <!-- 分页控件 -->
    <div v-if="buildings.length > 0" class="pagination-container">
      <div class="page-size-selector">
        <span class="label">每页显示：</span>
        <select v-model="pageSize" @change="handlePageSizeChange" class="size-select">
          <option :value="15">15条</option>
          <option :value="30">30条</option>
          <option :value="60">60条</option>
          <option :value="120">120条</option>
        </select>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="buildings.length"
        layout="prev, pager, next, jumper, total"
        @current-change="handlePageChange"
        background
      />
    </div>
    
    <!-- 创建宿舍楼对话框 - 苹果风格 -->
    <el-dialog 
      v-model="dialogVisible" 
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      custom-class="apple-building-dialog"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <Building2 :size="20" />
            </div>
            <div class="header-text">
              <h3>新建宿舍楼</h3>
              <p>配置宿舍楼基本信息和房间布局</p>
            </div>
          </div>
          <button class="close-btn" @click="dialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <div class="apple-form-container">
        <el-form :model="form" label-width="0" class="apple-form">
          <div class="form-section">
            <div class="section-title">基本信息</div>
            
            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">🏢</span>
                <span class="label-text">宿舍楼名称</span>
              </label>
              <el-input 
                v-model="form.name" 
                placeholder="例如：1号楼"
                class="apple-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">
                <span class="label-icon">⚧️</span>
                <span class="label-text">宿舍楼类型</span>
              </label>
              <div class="type-selector">
                <div 
                  class="type-option" 
                  :class="{ active: form.type === 'MALE' }"
                  @click="form.type = 'MALE'"
                >
                  <span class="type-icon">👨</span>
                  <span>男生宿舍楼</span>
                </div>
                <div 
                  class="type-option" 
                  :class="{ active: form.type === 'FEMALE' }"
                  @click="form.type = 'FEMALE'"
                >
                  <span class="type-icon">👩</span>
                  <span>女生宿舍楼</span>
                </div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <div class="section-title">楼层配置</div>
            
            <div class="compact-grid">
              <div class="form-group">
                <label class="form-label">
                  <span class="label-icon">🏗️</span>
                  <span class="label-text">楼层数</span>
                </label>
                <div class="number-input-wrapper">
                  <button 
                    type="button" 
                    class="number-btn decrease" 
                    @click="form.floors = Math.max(4, form.floors - 1)"
                    :disabled="form.floors <= 4"
                  >
                    −
                  </button>
                  <div class="number-display">{{ form.floors }}</div>
                  <button 
                    type="button" 
                    class="number-btn increase" 
                    @click="form.floors = Math.min(10, form.floors + 1)"
                    :disabled="form.floors >= 10"
                  >
                    +
                  </button>
                </div>
              </div>
              
              <div class="form-group">
                <label class="form-label">
                  <span class="label-icon">🚪</span>
                  <span class="label-text">房间/层</span>
                </label>
                <div class="number-input-wrapper">
                  <button 
                    type="button" 
                    class="number-btn decrease" 
                    @click="form.roomsPerFloor = Math.max(6, form.roomsPerFloor - 1)"
                    :disabled="form.roomsPerFloor <= 6"
                  >
                    −
                  </button>
                  <div class="number-display">{{ form.roomsPerFloor }}</div>
                  <button 
                    type="button" 
                    class="number-btn increase" 
                    @click="form.roomsPerFloor = Math.min(12, form.roomsPerFloor + 1)"
                    :disabled="form.roomsPerFloor >= 12"
                  >
                    +
                  </button>
                </div>
              </div>
              
              <div class="form-group">
                <label class="form-label">
                  <span class="label-icon">👥</span>
                  <span class="label-text">床位数</span>
                </label>
                <div class="number-input-wrapper">
                  <button 
                    type="button" 
                    class="number-btn decrease" 
                    @click="decreaseCapacity()"
                  >
                    −
                  </button>
                  <div class="number-display">{{ form.capacity }}</div>
                  <button 
                    type="button" 
                    class="number-btn increase" 
                    @click="increaseCapacity()"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="form-section preview-section">
            <div class="preview-stats">
              <div class="stat-box">
                <div class="stat-value">{{ form.floors * form.roomsPerFloor }}</div>
                <div class="stat-label">房间数</div>
              </div>
              <div class="stat-box">
                <div class="stat-value">{{ form.floors * form.roomsPerFloor * form.capacity }}</div>
                <div class="stat-label">最大容纳</div>
              </div>
            </div>
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <div class="apple-dialog-footer">
          <button class="apple-btn cancel" @click="dialogVisible = false">
            取消
          </button>
          <button 
            class="apple-btn primary" 
            @click="confirmCreate" 
            :disabled="saving"
          >
            <span v-if="saving" class="loading-spinner">⏳</span>
            <span v-else>确定创建</span>
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Building2, Plus, Home, Users, Trash2, X } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

interface Building {
  id: number
  name: string
  type: 'MALE' | 'FEMALE'
  floors: number
  roomsPerFloor: number
  capacity: number
  createdAt: string
}

const loading = ref(false)
const buildings = ref<Building[]>([])
const studentCounts = ref<Record<number, number>>({})
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({
  name: '',
  type: 'MALE' as 'MALE' | 'FEMALE',
  floors: 6,
  roomsPerFloor: 10,
  capacity: 4
})

const capacityOptions = [2, 4, 6]

// 分页相关
const currentPage = ref(1)
const pageSize = ref(15)

const paginatedBuildings = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return buildings.value.slice(start, end)
})

const handlePageChange = (page: number) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePageSizeChange = () => {
  currentPage.value = 1
}

const loadBuildings = async () => {
  loading.value = true
  try {
    buildings.value = await request.get('/buildings')
    buildings.value.sort((a, b) => {
      return a.name.localeCompare(b.name, 'zh-CN')
    })
    await loadStudentCounts()
  } catch (error) {
    ElMessage.error('加载宿舍楼列表失败')
  } finally {
    loading.value = false
  }
}

const loadStudentCounts = async () => {
  try {
    const rooms: any[] = await request.get('/rooms')
    const counts: Record<number, number> = {}
    
    rooms.forEach((room: any) => {
      const buildingId = room.building.id
      if (!counts[buildingId]) {
        counts[buildingId] = 0
      }
      counts[buildingId] += room.occupied || 0
    })
    
    studentCounts.value = counts
  } catch (error) {
    console.error('加载入住人数失败:', error)
  }
}

const showCreateDialog = () => {
  form.value = {
    name: '',
    type: 'MALE',
    floors: 6,
    roomsPerFloor: 10,
    capacity: 4
  }
  dialogVisible.value = true
}

const setCapacity = (value: number) => {
  form.value.capacity = value
}

const increaseCapacity = () => {
  if (form.value.capacity === 2) form.value.capacity = 4
  else if (form.value.capacity === 4) form.value.capacity = 6
  else if (form.value.capacity === 6) form.value.capacity = 2
}

const decreaseCapacity = () => {
  if (form.value.capacity === 2) form.value.capacity = 6
  else if (form.value.capacity === 4) form.value.capacity = 2
  else if (form.value.capacity === 6) form.value.capacity = 4
}

const confirmCreate = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入宿舍楼名称')
    return
  }
  
  saving.value = true
  try {
    await request.post('/buildings', form.value)
    ElMessage.success('宿舍楼创建成功，房间已自动生成')
    dialogVisible.value = false
    loadBuildings()
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    saving.value = false
  }
}

const getRoomCount = (buildingId: number) => {
  const building = buildings.value.find(b => b.id === buildingId)
  return building ? building.floors * building.roomsPerFloor : 0
}

const getStudentCount = (buildingId: number) => {
  return studentCounts.value[buildingId] || 0
}

const getOccupancyRate = (buildingId: number) => {
  const building = buildings.value.find(b => b.id === buildingId)
  if (!building) return '0%'
  
  const totalCapacity = building.floors * building.roomsPerFloor * building.capacity
  const occupied = getStudentCount(buildingId)
  
  if (totalCapacity === 0) return '0%'
  
  const rate = Math.round((occupied / totalCapacity) * 100)
  return `${rate}% (${occupied}/${totalCapacity})`
}

const getOccupancyClass = (buildingId: number) => {
  const building = buildings.value.find(b => b.id === buildingId)
  if (!building) return ''
  
  const totalCapacity = building.floors * building.roomsPerFloor * building.capacity
  const occupied = getStudentCount(buildingId)
  const rate = (occupied / totalCapacity) * 100
  
  if (rate >= 90) return 'occupancy-high'
  if (rate >= 60) return 'occupancy-medium'
  return 'occupancy-low'
}

const handleDelete = async (building: Building) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${building.name} 吗？删除后该楼的所有房间也会被删除。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.delete(`/buildings/${building.id}`)
    ElMessage.success('删除成功')
    loadBuildings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadBuildings()
})
</script>

<style scoped>
.buildings-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  overflow: hidden;
  box-sizing: border-box;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.view-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.create-btn:active {
  transform: scale(0.96);
}

.buildings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  margin-bottom: 16px;
  align-content: start;
}

.building-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.building-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.building-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.male-building {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.female-building {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
}

.building-info {
  flex: 1;
}

.building-name {
  margin: 0 0 6px 0;
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.label {
  color: #6b7280;
  font-weight: 500;
}

.value {
  color: #1f2937;
  font-weight: 600;
}

.occupancy-high {
  color: #ef4444 !important;
  font-weight: 700;
}

.occupancy-medium {
  color: #f59e0b !important;
  font-weight: 700;
}

.occupancy-low {
  color: #10b981 !important;
  font-weight: 700;
}

.card-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats {
  display: flex;
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #6b7280;
  font-size: 13px;
}

.delete-btn {
  padding: 8px;
  border: none;
  background: transparent;
  color: #ef4444;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover:not(:disabled) {
  background: #fee2e2;
  transform: scale(1.1);
}

.delete-btn:disabled {
  color: #d1d5db;
  cursor: not-allowed;
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

/* 苹果风格宿舍楼对话框 */
.apple-building-dialog {
  border-radius: 16px !important;
  padding: 0 !important;
  max-width: 420px !important;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(0, 0, 0, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.apple-building-dialog .el-dialog__header {
  padding: 0 !important;
  margin: 0 !important;
}

.apple-building-dialog .el-dialog__body {
  padding: 0 !important;
  max-height: 500px !important;
  overflow-y: auto !important;
}

.apple-building-dialog .el-dialog__footer {
  padding: 0 !important;
  margin: 0 !important;
}

.apple-building-dialog .custom-dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.apple-building-dialog .custom-dialog-header .header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.apple-building-dialog .custom-dialog-header .header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.apple-building-dialog .custom-dialog-header .header-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0 0 4px 0;
}

.apple-building-dialog .custom-dialog-header .header-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.apple-building-dialog .custom-dialog-header .close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.apple-building-dialog .custom-dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 表单容器 */
.apple-form-container {
  padding: 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.apple-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 表单分组 */
.form-section {
  background: rgba(248, 250, 252, 0.6);
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.preview-section {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border: 1px solid rgba(102, 126, 234, 0.15);
  padding: 10px !important;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

/* 表单组 */
.form-group {
  margin-bottom: 10px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.label-icon {
  font-size: 14px;
  width: 18px;
  text-align: center;
}

.label-text {
  flex: 1;
}

/* 苹果风格输入框 */
.apple-input .el-input__wrapper {
  background: white !important;
  border: 1.5px solid #e5e7eb !important;
  border-radius: 10px !important;
  padding: 10px 14px !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.apple-input .el-input__wrapper:hover {
  border-color: #667eea !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15) !important;
}

.apple-input .el-input__wrapper.is-focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
}

.apple-input .el-input__inner {
  font-size: 13px !important;
  color: #1f2937 !important;
  font-weight: 500 !important;
}

.apple-input .el-input__inner::placeholder {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

/* 类型选择器 */
.type-selector {
  display: flex;
  gap: 12px;
}

.type-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.type-option:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.type-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.type-icon {
  font-size: 16px;
}

/* 数字输入器 */
.number-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.number-input-wrapper:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.number-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f8fafc;
  color: #667eea;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.number-btn:hover:not(:disabled) {
  background: #667eea;
  color: white;
}

.number-btn:disabled {
  color: #d1d5db;
  cursor: not-allowed;
  background: #f9fafb;
}

.number-display {
  flex: 1;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  padding: 10px;
  background: white;
}

/* 预览区域 - 紧凑统计 */
.preview-stats {
  display: flex;
  gap: 10px;
}

.stat-box {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: white;
  border-radius: 8px;
  border: 1px solid rgba(102, 126, 234, 0.15);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

/* 紧凑网格布局 */
.compact-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.compact-grid .form-group {
  margin-bottom: 0;
}

/* 对话框底部 */
.apple-dialog-footer {
  display: flex;
  gap: 10px;
  padding: 14px 20px 18px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(248, 250, 252, 0.8);
  border-radius: 0 0 16px 16px;
}

.apple-btn {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.apple-btn.cancel {
  background: white;
  color: #6b7280;
  border: 1.5px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.apple-btn.cancel:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.apple-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.apple-btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.6);
}

.apple-btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 滚动条样式 */
.apple-building-dialog .el-dialog__body::-webkit-scrollbar {
  width: 6px;
}

.apple-building-dialog .el-dialog__body::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.apple-building-dialog .el-dialog__body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.apple-building-dialog .el-dialog__body::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
