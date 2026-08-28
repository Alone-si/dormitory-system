import { ref, computed, onMounted } from 'vue'
import { Building2, Plus, Home, Users, Trash2, X } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

export function useBuildingsView() {
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
  return {
  Building2,
  buildings,
  confirmCreate,
  currentPage,
  decreaseCapacity,
  dialogVisible,
  form,
  getOccupancyClass,
  getOccupancyRate,
  getRoomCount,
  getStudentCount,
  handleDelete,
  handlePageChange,
  handlePageSizeChange,
  Home,
  increaseCapacity,
  loading,
  pageSize,
  paginatedBuildings,
  Plus,
  saving,
  showCreateDialog,
  Trash2,
  Users,
  X
  }
}