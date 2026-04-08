<template>
  <div class="students-view">
    <div class="view-header">
      <h2>学生管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索学生姓名或学号"
          :prefix-icon="Search"
          style="width: 200px"
          size="default"
          clearable
        />
        <el-select
          v-model="statusFilter"
          placeholder="学生状态"
          style="width: 120px"
          size="default"
          clearable
          popper-class="apple-select-dropdown"
          :teleported="true"
        >
          <el-option label="全部" value="" />
          <el-option label="在校" value="ACTIVE" />
          <el-option label="未入住" value="INACTIVE" />
          <el-option label="已毕业" value="GRADUATED" />
        </el-select>
        <el-select
          v-model="classFilter"
          placeholder="分班状态"
          style="width: 120px"
          size="default"
          clearable
          popper-class="apple-select-dropdown"
          :teleported="true"
        >
          <el-option label="全部" value="" />
          <el-option label="已分班" value="HAS_CLASS" />
          <el-option label="未分班" value="NO_CLASS" />
        </el-select>
        <el-select
          v-model="roomFilter"
          placeholder="宿舍状态"
          style="width: 120px"
          size="default"
          clearable
          popper-class="apple-select-dropdown"
          :teleported="true"
        >
          <el-option label="全部" value="" />
          <el-option label="已分配" value="HAS_ROOM" />
          <el-option label="未分配" value="NO_ROOM" />
        </el-select>
        
        <!-- 筛选结果统计 - 移到同一行 -->
        <div v-if="hasActiveFilters || searchQuery" class="filter-stats-inline">
          <span class="stats-text">
            筛选结果：{{ filteredStudents.length }} 名学生
            <span v-if="filteredStudents.length !== students.length" class="total-count">
              / {{ students.length }}
            </span>
          </span>
        </div>
        
        <button 
          v-if="hasActiveFilters"
          class="action-btn reset-btn" 
          @click="resetFilters"
          title="重置筛选条件"
        >
          <X :size="16" />
          <span>重置</span>
        </button>
        <button 
          v-if="userStore.userInfo?.adminType === 'SUPER_ADMIN'"
          class="action-btn archive-btn" 
          @click="batchArchiveGraduated"
          title="批量归档超过3年学制的学生"
        >
          <Archive :size="16" />
          <span>归档过期学生</span>
        </button>
        <button 
          v-if="!selectionMode" 
          class="action-btn selection-btn" 
          @click="enterSelectionMode"
        >
          <CheckSquare :size="16" />
          <span>批量操作</span>
        </button>
        <template v-if="selectionMode">
          <button class="action-btn cancel-btn" @click="exitSelectionMode">
            <X :size="16" />
            <span>取消选择</span>
          </button>
          <button 
            class="action-btn select-all-btn" 
            @click="toggleSelectAll"
          >
            <CheckSquare :size="16" />
            <span>{{ isAllSelected ? '取消全选' : '全选本页' }} ({{ selectedStudents.length }})</span>
          </button>
          <button 
            class="action-btn checkin-btn" 
            @click="batchCheckIn"
            :disabled="selectedStudents.length === 0"
          >
            <Home :size="16" />
            <span>批量入住 ({{ selectedStudents.length }})</span>
          </button>
          <button 
            class="action-btn checkout-btn" 
            @click="batchCheckOut"
            :disabled="selectedStudents.length === 0"
          >
            <LogOut :size="16" />
            <span>批量退寝 ({{ selectedStudents.length }})</span>
          </button>
          <button 
            class="action-btn class-btn" 
            @click="openBatchClassDialog"
            :disabled="selectedStudents.length === 0 || selectedStudents.length > 30"
          >
            <BookOpen :size="16" />
            <span>批量分班 ({{ selectedStudents.length }})</span>
          </button>
        </template>
        <template v-if="!selectionMode">
          <button class="action-btn export-btn" @click="exportStudents">
            <Download :size="16" />
            <span>导出学生</span>
          </button>
          <button class="action-btn import-btn" @click="showImportDialog">
            <Upload :size="16" />
            <span>批量导入</span>
          </button>
          <button class="action-btn create-btn" @click="showCreateDialog">
            <Plus :size="16" />
            <span>新建学生</span>
          </button>
        </template>
      </div>
    </div>
    
    <div class="students-grid" v-loading="loading">
      <div 
        v-for="student in paginatedStudents" 
        :key="student.id" 
        class="student-card" 
        :class="{ 'selected': isSelected(student.id), 'selection-mode': selectionMode }"
        @click="selectionMode ? toggleSelection(student) : handleEdit(student)"
      >
        <div v-if="selectionMode" class="selection-checkbox">
          <input 
            type="checkbox" 
            :checked="isSelected(student.id)"
            @click.stop="toggleSelection(student)"
          />
        </div>
        <div class="card-header">
          <div class="student-avatar" :class="student.gender === 'MALE' ? 'male' : 'female'">
            <img v-if="student.avatar" :src="student.avatar" :alt="student.name" class="avatar-image" />
            <span v-else class="avatar-text">{{ student.name ? student.name.charAt(0) : '?' }}</span>
          </div>
          <div class="student-basic">
            <div class="name-row">
              <span class="name">{{ student.name }}</span>
              <div class="custom-tag" :class="student.gender === 'MALE' ? 'tag-primary' : 'tag-danger'">
                {{ student.gender === 'MALE' ? '男' : '女' }}
              </div>
              <div class="status-tag-spacer"></div>
              <div class="custom-tag" :class="student.status === 'ACTIVE' ? 'tag-success' : 'tag-info'">
                {{ student.status === 'ACTIVE' ? '入住' : '未入住' }}
              </div>
            </div>
            <span class="student-id">{{ student.studentId }}</span>
          </div>
        </div>
        
        <div class="card-body">
          <div class="info-item" v-if="student.className">
            <BookOpen :size="13" />
            <span>{{ student.className }}</span>
          </div>
          <div class="info-item">
            <Home :size="13" />
            <span v-if="student.status === 'INACTIVE'" class="no-room">未入住</span>
            <span v-else-if="student.room" class="room-text">
              {{ student.room.building.name }}-{{ student.room.roomNumber }}
            </span>
            <span v-else class="no-room">未分配</span>
          </div>
          <div class="info-item" v-if="student.email">
            <Mail :size="13" />
            <span>{{ student.email }}</span>
          </div>
        </div>
        
        <div class="card-footer" @click.stop>
          <button 
            v-if="student.status === 'INACTIVE'" 
            class="apple-action-btn success"
            @click="handleCheckIn(student)"
          >
            办理入住
          </button>
          <button 
            v-else-if="!student.room && student.status === 'ACTIVE'" 
            class="apple-action-btn primary"
            @click="handleAssignRoom(student)"
          >
            分配宿舍
          </button>
          <button 
            v-else-if="student.room && student.status === 'ACTIVE'" 
            class="apple-action-btn danger"
            @click="handleCheckOut(student)"
          >
            办理退宿
          </button>
          <!-- 兜底情况：如果没有匹配的条件，显示状态信息 -->
          <span 
            v-else
            class="no-action-hint"
          >
            状态异常
          </span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && filteredStudents.length === 0" description="暂无学生数据" />
    
    <!-- 分页控件 -->
    <div v-if="filteredStudents.length > 0" class="pagination-container">
      <div class="page-size-selector">
        <span class="label">每页显示：</span>
        <select v-model="pageSize" @change="handlePageSizeChange" class="size-select">
          <option :value="30">30条</option>
          <option :value="60">60条</option>
          <option :value="90">90条</option>
          <option :value="120">120条</option>
        </select>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredStudents.length"
        layout="prev, pager, next, jumper, total"
        @current-change="handlePageChange"
        background
      />
    </div>
    
    <!-- 分配宿舍对话框 -->
    <el-dialog 
      v-model="assignDialogVisible" 
      title="分配宿舍"
      width="340px"
      :close-on-click-modal="false"
      custom-class="apple-assign-dialog"
    >
      <div class="compact-form">
        <div class="student-info">
          <span class="info-label">学生：</span>
          <span class="info-value">{{ currentStudent ? currentStudent.name : '' }}</span>
          <span class="gender-badge" :class="currentStudent?.gender?.toLowerCase()">
            {{ currentStudent ? (currentStudent.gender === 'MALE' ? '男' : '女') : '' }}
          </span>
        </div>
        
        <div class="room-select">
          <label class="select-label">选择宿舍</label>
          <el-select 
            v-model="assignForm.roomId" 
            placeholder="请选择宿舍" 
            style="width: 100%"
            size="default"
          >
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.building.name}-${room.roomNumber} (${room.occupied}/${room.capacity})`"
              :value="room.id"
            />
          </el-select>
        </div>
      </div>
      
      <template #footer>
        <div class="apple-dialog-footer">
          <button class="apple-btn cancel" @click="assignDialogVisible = false">
            取消
          </button>
          <button 
            class="apple-btn primary" 
            @click="confirmAssign" 
            :disabled="assigning"
          >
            <span v-if="assigning" class="loading-spinner">⏳</span>
            <span v-else>确定分配</span>
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量分班对话框 -->
    <el-dialog 
      v-model="batchClassDialogVisible" 
      width="360px"
      :show-close="false"
      custom-class="apple-batch-class-dialog"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <BookOpen :size="20" />
            </div>
            <div class="header-text">
              <h3>批量班级操作</h3>
              <p>为多个学生分配或移除班级</p>
            </div>
          </div>
          <button class="close-btn" @click="batchClassDialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <el-form :model="batchClassForm" label-width="80px">
        <el-form-item label="选择学生">
          <span>已选择 {{ selectedStudents.length }} 名学生</span>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchClassForm.operation">
            <el-radio value="assign">分配班级</el-radio>
            <el-radio value="remove">退出班级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="batchClassForm.operation === 'assign'" label="班级名称">
          <el-input v-model="batchClassForm.className" placeholder="例如：计算机2024-1班" />
        </el-form-item>
        <el-form-item v-if="batchClassForm.operation === 'remove'" label="确认操作">
          <el-alert 
            title="将清空所选学生的班级信息" 
            type="warning" 
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchClassDialogVisible = false" size="large">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmBatchClass" 
            :loading="batchClassLoading"
            :disabled="batchClassForm.operation === 'assign' && !batchClassForm.className"
            size="large"
          >
            {{ batchClassForm.operation === 'assign' ? '分配班级' : '退出班级' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 学生表单对话框 (新建/编辑) - 苹果风格 -->
    <el-dialog 
      v-model="studentDialogVisible" 
      width="350px"
      :close-on-click-modal="false"
      :show-close="false"
      custom-class="apple-student-dialog"
    >
      <template #header>
        <div class="custom-dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <UserPen v-if="isEditing" :size="20" />
              <UserPlus v-else :size="20" />
            </div>
            <div class="header-text">
              <h3>{{ isEditing ? '编辑学生' : '新建学生' }}</h3>
              <p>{{ isEditing ? '修改学生基本信息' : '添加新的学生账号' }}</p>
            </div>
          </div>
          <button class="close-btn" @click="studentDialogVisible = false">
            <X :size="20" />
          </button>
        </div>
      </template>
      <div class="apple-form-container">
        <el-form :model="studentForm" label-width="0" class="apple-form">
          <!-- 基本信息 -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">🎓</span>
              <span class="label-text">学号</span>
            </label>
            <el-input 
              v-model="studentForm.studentId" 
              placeholder="用于登录" 
              :disabled="isEditing"
              class="apple-input"
              size="small"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">👤</span>
              <span class="label-text">姓名</span>
            </label>
            <el-input 
              v-model="studentForm.name" 
              placeholder="学生姓名" 
              class="apple-input"
              size="small"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">⚧️</span>
              <span class="label-text">性别</span>
            </label>
            <div class="gender-selector">
              <div 
                class="gender-option" 
                :class="{ active: studentForm.gender === 'MALE' }"
                @click="studentForm.gender = 'MALE'"
              >
                <span class="gender-icon">👨</span>
                <span>男</span>
              </div>
              <div 
                class="gender-option" 
                :class="{ active: studentForm.gender === 'FEMALE' }"
                @click="studentForm.gender = 'FEMALE'"
              >
                <span class="gender-icon">👩</span>
                <span>女</span>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">📱</span>
              <span class="label-text">电话</span>
            </label>
            <el-input 
              v-model="studentForm.phone" 
              placeholder="联系电话"
              class="apple-input"
              size="small"
            />
          </div>
          
          <div class="form-group" v-if="isEditing && studentForm.email">
            <label class="form-label">
              <span class="label-icon">📧</span>
              <span class="label-text">邮箱</span>
            </label>
            <el-input 
              v-model="studentForm.email" 
              placeholder="未设置"
              class="apple-input readonly-input"
              size="small"
              disabled
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">🏫</span>
              <span class="label-text">班级</span>
            </label>
            <el-input 
              v-model="studentForm.className" 
              placeholder="例如：计算机2024-1班"
              class="apple-input"
              size="small"
            />
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <div class="apple-dialog-footer">
          <button 
            v-if="isEditing" 
            class="apple-btn danger" 
            @click="handleDeleteStudent"
            style="margin-right: auto;"
          >
            删除学生
          </button>
          <button class="apple-btn cancel" @click="studentDialogVisible = false">
            取消
          </button>
          <button 
            class="apple-btn primary" 
            @click="confirmSaveStudent" 
            :disabled="savingStudent"
          >
            <span v-if="savingStudent" class="loading-spinner">⏳</span>
            <span v-else>{{ isEditing ? '保存' : '创建' }}</span>
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="📊 批量导入学生" width="520px">
      <div class="import-container">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls,.csv"
          :show-file-list="true"
        >
          <div class="upload-icon-wrapper">
            <UploadCloud :size="48" />
          </div>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              <div class="tip-title">📋 支持格式：Excel (.xlsx/.xls) 或 CSV 文件</div>
              <div class="tip-format">
                <strong>Excel格式示例：</strong><br>
                <span class="format-example">姓名 | 学号 | 性别 | 电话 | 班级</span><br>
                <span class="format-example">张三 | 20240001 | 男 | 13800000001 | 计算机1班</span>
              </div>
              <div class="tip-rule">
                <strong>🔑 账号规则：用户名=学号，默认密码=123456</strong>
              </div>
              <div class="template-download">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="downloadTemplate"
                  style="margin-top: 8px;"
                >
                  📥 下载Excel模板
                </el-button>
              </div>
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importing">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { UserCircle as UserIcon, Phone, Mail, Home, Plus, Download, Upload, Search, CheckSquare, X, LogOut, UploadCloud, BookOpen, UserPen, UserPlus, Archive } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { roomApi } from '../../api/room'
import { useUserStore } from '../../stores/user'
import type { User, Room } from '../../types'

const userStore = useUserStore()

const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const classFilter = ref('')
const roomFilter = ref('')
const students = ref<User[]>([])  // 🔥 修复：后端返回User类型
const availableRooms = ref<Room[]>([])
const assignDialogVisible = ref(false)
const assigning = ref(false)
const currentStudent = ref<User | null>(null)  // 🔥 修复：使用User类型
const assignForm = ref({
  roomId: null as number | null
})

// 导入相关
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)

// 批量分班相关
const batchClassDialogVisible = ref(false)
const batchClassLoading = ref(false)
const batchClassForm = ref({
  operation: 'assign',
  className: ''
})

const openBatchClassDialog = () => {
  batchClassForm.value.operation = 'assign'
  batchClassForm.value.className = ''
  batchClassDialogVisible.value = true
}

const confirmBatchClass = async () => {
  if (selectedStudents.value.length > 30) {
    ElMessage.warning('单次批量操作最多支持30人')
    return
  }

  // 检查分配班级操作是否填写了班级名称
  if (batchClassForm.value.operation === 'assign' && !batchClassForm.value.className) {
    ElMessage.warning('请输入班级名称')
    return
  }
  
  const isAssign = batchClassForm.value.operation === 'assign'
  const operationName = isAssign ? '分配班级' : '退出班级'
  
  console.log(`=== 批量${operationName}开始 ===`)
  console.log('选中学生数量:', selectedStudents.value.length)
  console.log('操作类型:', batchClassForm.value.operation)
  if (isAssign) {
    console.log('班级名称:', batchClassForm.value.className)
  }
  console.log('选中的学生:', selectedStudents.value)
  
  batchClassLoading.value = true
  let successCount = 0
  let failCount = 0
  
  try {
    for (const student of selectedStudents.value) {
      try {
        console.log(`正在更新学生 ${student.name} (ID: ${student.id})`)
        const updateData = isAssign 
          ? { className: batchClassForm.value.className }
          : { className: null } // 退出班级时设置为null
        
        const response = await request.put(`/users/students/${student.id}`, updateData)
        console.log(`学生 ${student.name} 更新成功:`, response)
        successCount++
      } catch (error) {
        console.error(`学生 ${student.name} 更新失败:`, error)
        failCount++
      }
    }
    
    console.log(`批量更新完成: 成功 ${successCount} 人, 失败 ${failCount} 人`)
    
    if (successCount > 0) {
      ElMessage.success(`成功${operationName} ${successCount} 人${failCount > 0 ? `，失败 ${failCount} 人` : ''}`)
      batchClassDialogVisible.value = false
      loadStudents()
      exitSelectionMode()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error(`批量${operationName}异常:`, error)
    ElMessage.error('操作异常')
  } finally {
    batchClassLoading.value = false
  }
}

// 学生表单相关
const studentDialogVisible = ref(false)
const savingStudent = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const studentForm = ref({
  name: '',
  studentId: '',
  displayName: '',
  password: '',
  gender: 'MALE',
  phone: '',
  email: '',
  className: ''
})

const currentPage = ref(1)
const pageSize = ref(30)
const selectionMode = ref(false)
const selectedStudents = ref<any[]>([])

// 全选相关计算属性
const isAllSelected = computed(() => {
  return paginatedStudents.value.length > 0 && 
         paginatedStudents.value.every(student => selectedStudents.value.some(s => s.id === student.id))
})

const isSelected = (studentId: number) => {
  return selectedStudents.value.some(s => s.id === studentId)
}

const toggleSelection = (student: any) => {
  const index = selectedStudents.value.findIndex(s => s.id === student.id)
  if (index > -1) {
    selectedStudents.value.splice(index, 1)
  } else {
    selectedStudents.value.push(student)
  }
}

// 全选/取消全选功能 - 只针对当前页面
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    // 取消选择当前页面的所有学生
    const currentPageIds = paginatedStudents.value.map(s => s.id)
    selectedStudents.value = selectedStudents.value.filter(s => !currentPageIds.includes(s.id))
  } else {
    // 选择当前页面的所有学生
    const currentPageIds = paginatedStudents.value.map(s => s.id)
    const newSelections = paginatedStudents.value.filter(s => !selectedStudents.value.some(selected => selected.id === s.id))
    selectedStudents.value = [...selectedStudents.value, ...newSelections]
  }
}

const enterSelectionMode = () => {
  selectionMode.value = true
  selectedStudents.value = []
}

const exitSelectionMode = () => {
  selectionMode.value = false
  selectedStudents.value = []
}

const batchCheckIn = async () => {
  const studentsWithoutRoom = selectedStudents.value.filter(s => !s.room)
  
  if (studentsWithoutRoom.length === 0) {
    ElMessage.warning('所选学生都已分配宿舍')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要为 ${studentsWithoutRoom.length} 名学生批量分配宿舍吗？系统会智能分配：同班同学优先安排在同一楼栋，相邻宿舍。`,
      '🤖 智能批量入住',
      {
        confirmButtonText: '开始分配',
        cancelButtonText: '取消',
        type: 'info',
        customClass: 'apple-smart-assign-dialog',
        center: true
      }
    )
    
    // 开始智能分配
    await performSmartRoomAssignment(studentsWithoutRoom)
    
  } catch (error) {
    // 用户取消
  }
}

// 智能宿舍分配算法
const performSmartRoomAssignment = async (students: any[]) => {
  try {
    ElMessage.info('正在智能分配宿舍，请稍候...')
    
    // 1. 按班级和性别分组
    const groupedStudents = groupStudentsByClassAndGender(students)
    
    // 2. 获取所有可用宿舍
    const allRooms = await roomApi.getAvailable()
    
    let successCount = 0
    let failCount = 0
    const assignmentResults: string[] = []
    
    // 3. 为每个组分配宿舍
    for (const group of groupedStudents) {
      const availableRooms = allRooms.filter(r => 
        r.building.type === group.gender && 
        r.status === 'AVAILABLE' && 
        r.occupied < r.capacity
      )
      
      if (availableRooms.length === 0) {
        failCount += group.students.length
        assignmentResults.push(`❌ ${group.className || '未分班'} (${group.gender === 'MALE' ? '男' : '女'}): 无可用宿舍`)
        continue
      }
      
      // 按楼栋分组可用宿舍
      const roomsByBuilding = groupRoomsByBuilding(availableRooms)
      
      // 为该组学生分配宿舍
      const result = await assignRoomsForGroup(group, roomsByBuilding)
      successCount += result.success
      failCount += result.failed
      assignmentResults.push(...result.messages)
    }
    
    // 4. 显示分配结果
    showAssignmentResults(successCount, failCount, assignmentResults)
    
    if (successCount > 0) {
      loadStudents()
      exitSelectionMode()
    }
    
  } catch (error) {
    console.error('智能分配失败:', error)
    ElMessage.error('智能分配过程中出现错误')
  }
}

// 按班级和性别分组学生
const groupStudentsByClassAndGender = (students: any[]) => {
  const groups: { [key: string]: any } = {}
  
  students.forEach(student => {
    const key = `${student.className || '未分班'}_${student.gender}`
    if (!groups[key]) {
      groups[key] = {
        className: student.className || '未分班',
        gender: student.gender,
        students: []
      }
    }
    groups[key].students.push(student)
  })
  
  // 按班级人数排序，人数多的班级优先分配
  return Object.values(groups).sort((a: any, b: any) => b.students.length - a.students.length)
}

// 按楼栋分组宿舍
const groupRoomsByBuilding = (rooms: any[]) => {
  const roomsByBuilding: { [key: string]: any[] } = {}
  
  rooms.forEach(room => {
    const buildingName = room.building.name
    if (!roomsByBuilding[buildingName]) {
      roomsByBuilding[buildingName] = []
    }
    roomsByBuilding[buildingName].push(room)
  })
  
  // 每个楼栋的宿舍按楼层和房间号排序
  Object.keys(roomsByBuilding).forEach(building => {
    roomsByBuilding[building].sort((a, b) => {
      if (a.floor !== b.floor) return a.floor - b.floor
      return a.roomNumber.localeCompare(b.roomNumber)
    })
  })
  
  return roomsByBuilding
}

// 为一个组分配宿舍
const assignRoomsForGroup = async (group: any, roomsByBuilding: { [key: string]: any[] }) => {
  let success = 0
  let failed = 0
  const messages: string[] = []
  
  // 选择最适合的楼栋（可用床位最多的）
  let bestBuilding = ''
  let maxAvailableBeds = 0
  
  Object.keys(roomsByBuilding).forEach(building => {
    const availableBeds = roomsByBuilding[building].reduce((sum, room) => 
      sum + (room.capacity - room.occupied), 0
    )
    if (availableBeds > maxAvailableBeds) {
      maxAvailableBeds = availableBeds
      bestBuilding = building
    }
  })
  
  if (!bestBuilding) {
    messages.push(`❌ ${group.className} (${group.gender === 'MALE' ? '男' : '女'}): 无可用楼栋`)
    return { success, failed: group.students.length, messages }
  }
  
  const selectedRooms = roomsByBuilding[bestBuilding]
  let roomIndex = 0
  
  messages.push(`🏠 ${group.className} (${group.gender === 'MALE' ? '男' : '女'}) → ${bestBuilding}`)
  
  // 为每个学生分配宿舍
  for (const student of group.students) {
    try {
      // 找到有空位的宿舍
      while (roomIndex < selectedRooms.length && selectedRooms[roomIndex].occupied >= selectedRooms[roomIndex].capacity) {
        roomIndex++
      }
      
      if (roomIndex >= selectedRooms.length) {
        messages.push(`   ❌ ${student.name}: 该楼栋已满`)
        failed++
        continue
      }
      
      const room = selectedRooms[roomIndex]
      
      // 分配宿舍 - 🔥 修复：后端返回的是User对象，直接使用id
      await roomApi.assignStudent(student.id, room.id)
      
      // 更新房间占用数
      room.occupied++
      
      messages.push(`   ✅ ${student.name} → ${room.roomNumber}`)
      success++
      
    } catch (error) {
      messages.push(`   ❌ ${student.name}: 分配失败`)
      failed++
    }
  }
  
  return { success, failed, messages }
}

// 显示分配结果 - 苹果风格
const showAssignmentResults = (successCount: number, failCount: number, results: string[]) => {
  // 创建自定义对话框内容HTML
  const createResultHTML = () => {
    // 统计信息
    const statsHtml = `
      <div class="result-stats">
        <div class="stat-item success">
          <div class="stat-number">${successCount}</div>
          <div class="stat-label">成功入住</div>
        </div>
        ${failCount > 0 ? `
        <div class="stat-item failed">
          <div class="stat-number">${failCount}</div>
          <div class="stat-label">分配失败</div>
        </div>
        ` : ''}
      </div>
    `
    
    // 详细结果
    let detailsHtml = '<div class="result-details">'
    
    results.forEach(line => {
      if (line.startsWith('🏠')) {
        // 楼栋标题
        const buildingInfo = line.replace('🏠 ', '').split(' → ')
        detailsHtml += `
          <div class="building-group">
            <div class="building-header">
              <div class="building-icon">🏠</div>
              <div class="building-info">
                <div class="class-name">${buildingInfo[0]}</div>
                <div class="building-name">${buildingInfo[1]}</div>
              </div>
            </div>
            <div class="students-list">
        `
      } else if (line.startsWith('   ✅')) {
        // 成功分配的学生
        const studentInfo = line.replace('   ✅ ', '').split(' → ')
        detailsHtml += `
          <div class="student-item success">
            <div class="student-avatar">
              <div class="avatar-circle success">✓</div>
            </div>
            <div class="student-info">
              <div class="student-name">${studentInfo[0]}</div>
              <div class="room-number">${studentInfo[1]}</div>
            </div>
          </div>
        `
      } else if (line.startsWith('   ❌')) {
        // 失败的学生
        const studentInfo = line.replace('   ❌ ', '')
        detailsHtml += `
          <div class="student-item failed">
            <div class="student-avatar">
              <div class="avatar-circle failed">×</div>
            </div>
            <div class="student-info">
              <div class="student-name">${studentInfo.split(':')[0]}</div>
              <div class="error-reason">${studentInfo.split(':')[1] || '分配失败'}</div>
            </div>
          </div>
        `
      }
      
      // 检查是否需要关闭学生列表
      const nextIndex = results.indexOf(line) + 1
      if (nextIndex < results.length && (results[nextIndex].startsWith('🏠') || nextIndex === results.length)) {
        detailsHtml += '</div></div>' // 关闭 students-list 和 building-group
      }
    })
    
    detailsHtml += '</div>'
    
    return `<div class="apple-assignment-result">${statsHtml}${detailsHtml}</div>`
  }
  
  // 使用自定义对话框
  ElMessageBox({
    title: '🎉 智能分配完成',
    message: createResultHTML(),
    showCancelButton: false,
    confirmButtonText: '完成',
    customClass: 'apple-result-dialog',
    dangerouslyUseHTMLString: true,
    center: true
  })
  
  if (successCount > 0) {
    ElMessage.success(`智能分配完成！成功入住 ${successCount} 人${failCount > 0 ? `，${failCount} 人分配失败` : ''}`)
  } else {
    ElMessage.error('批量入住失败，请检查宿舍资源')
  }
}

const batchCheckOut = async () => {
  const studentsWithRoom = selectedStudents.value.filter(s => s.room)
  
  if (studentsWithRoom.length === 0) {
    ElMessage.warning('所选学生都未分配宿舍')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要为 ${studentsWithRoom.length} 名学生批量办理退寝吗？\n\n此操作将清空他们的宿舍分配信息。`,
      '批量退寝确认',
      {
        confirmButtonText: '确定退寝',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'apple-message-box',
        dangerouslyUseHTMLString: false,
        center: false
      }
    )
    
    let successCount = 0
    let failCount = 0
    
    for (const student of studentsWithRoom) {
      try {
        // 🔥 修复：后端返回的是User对象，直接使用id
        await request.post(`/rooms/remove/${student.id}`)
        successCount++
      } catch (error) {
        failCount++
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(`成功退寝 ${successCount} 人${failCount > 0 ? `，失败 ${failCount} 人` : ''}`)
      loadStudents()
      exitSelectionMode()
    }
  } catch (error) {
    // 用户取消
  }
}

const filteredStudents = computed(() => {
  let filtered = students.value

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(s => 
      s.name.toLowerCase().includes(query) || 
      s.studentId?.toLowerCase().includes(query)
    )
  }

  // 入住状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(s => s.status === statusFilter.value)
  }

  // 分班状态筛选
  if (classFilter.value) {
    if (classFilter.value === 'HAS_CLASS') {
      filtered = filtered.filter(s => s.className && s.className.trim() !== '')
    } else if (classFilter.value === 'NO_CLASS') {
      filtered = filtered.filter(s => !s.className || s.className.trim() === '')
    }
  }

  // 宿舍状态筛选
  if (roomFilter.value) {
    if (roomFilter.value === 'HAS_ROOM') {
      filtered = filtered.filter(s => s.room)
    } else if (roomFilter.value === 'NO_ROOM') {
      filtered = filtered.filter(s => !s.room)
    }
  }

  return filtered
})

// 检查是否有激活的筛选器
const hasActiveFilters = computed(() => {
  return statusFilter.value || classFilter.value || roomFilter.value
})

// 重置所有筛选器
const resetFilters = () => {
  statusFilter.value = ''
  classFilter.value = ''
  roomFilter.value = ''
  currentPage.value = 1
}

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

const handlePageChange = (page: number) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePageSizeChange = () => {
  currentPage.value = 1
}

const loadStudents = async () => {
  loading.value = true
  try {
    const data = await request.get<any, any[]>('/students')
    console.log('=== 学生列表数据 ===')
    console.log('总数:', data.length)
    console.log('张三数据:', data.find(s => s.name === '张三'))
    students.value = data
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const batchArchiveGraduated = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要归档所有超过3年学制的学生吗？归档后学生状态将变为"已毕业"，并自动退寝。',
      '批量归档确认',
      {
        confirmButtonText: '确定归档',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response: any = await request.post('/students/batch-archive')
    ElMessage.success(response.data || '归档成功')
    loadStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '归档失败')
    }
  }
}

const currentStudentRoomText = computed(() => {
  if (!currentStudent.value || !currentStudent.value.room) return '未分配'
  return `${currentStudent.value.room.building.name} - ${currentStudent.value.room.roomNumber}`
})

const showCreateDialog = () => {
  isEditing.value = false
  editingId.value = null
  currentStudent.value = null
  studentForm.value = {
    name: '',
    studentId: '',
    displayName: '',
    password: '', // 默认留空，后端处理默认值
    gender: 'MALE',
    phone: '',
    email: '',
    className: ''
  }
  studentDialogVisible.value = true
}

const handleEdit = (student: User) => {
  isEditing.value = true
  editingId.value = student.id
  currentStudent.value = student
  studentForm.value = {
    name: student.name,
    studentId: student.studentId || '',
    displayName: student.displayName || '',  // 🔥 修复：后端返回的是User对象，直接使用displayName
    password: '', // 编辑时不显示密码
    gender: student.gender || 'MALE',
    phone: student.phone || '',
    email: student.email || '',
    className: student.className || ''
  }
  studentDialogVisible.value = true
}

const confirmSaveStudent = async () => {
  if (!studentForm.value.name || !studentForm.value.studentId) {
    ElMessage.warning('请填写完整信息')
    return
  }

  savingStudent.value = true
  try {
    if (isEditing.value) {
      // 更新
      const data: any = { ...studentForm.value }
      if (!data.password) delete data.password // 如果没填密码就不传
      await request.put(`/users/students/${editingId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await request.post('/students', {
        ...studentForm.value,
        role: 'STUDENT',
        password: studentForm.value.password || '123456'
      })
      ElMessage.success('创建成功')
    }
    studentDialogVisible.value = false
    loadStudents()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    savingStudent.value = false
  }
}

const handleDeleteStudent = async () => {
  if (!editingId.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${currentStudent.value?.name}" 吗？此操作不可恢复！`,
      '⚠️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await request.delete(`/users/students/${editingId.value}`)
    ElMessage.success('删除成功')
    studentDialogVisible.value = false
    loadStudents()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const loadAvailableRooms = async (gender: string) => {
  try {
    const rooms = await roomApi.getAvailable()
    availableRooms.value = rooms.filter(r => r.building.type === gender)
  } catch (error) {
    ElMessage.error('加载可用宿舍失败')
  }
}

const handleAssignRoom = async (student: User) => {
  currentStudent.value = student
  await loadAvailableRooms(student.gender!)
  if (availableRooms.value.length === 0) {
    ElMessage.warning('暂无可用宿舍')
    return
  }
  assignDialogVisible.value = true
}

// 对话框打开时应用样式
const onDialogOpened = () => {
  nextTick(() => {
    // 查找对话框元素，使用更精确的选择器
    const dialog = document.querySelector('.el-dialog.apple-assign-dialog-wrapper') || 
                   document.querySelector('.apple-assign-dialog-wrapper')
    
    if (dialog) {
      const dialogEl = dialog as HTMLElement
      // 确保对话框居中显示
      dialogEl.style.position = 'fixed'
      dialogEl.style.top = '50%'
      dialogEl.style.left = '50%'
      dialogEl.style.transform = 'translate(-50%, -50%)'
      dialogEl.style.margin = '0'
      
      // 应用圆角和阴影
      dialogEl.style.borderRadius = '20px'
      dialogEl.style.overflow = 'hidden'
      dialogEl.style.boxShadow = '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
      
      // 查找并修改标题栏
      const header = dialog.querySelector('.el-dialog__header')
      if (header) {
        const headerEl = header as HTMLElement
        headerEl.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
        headerEl.style.borderRadius = '20px 20px 0 0'
        headerEl.style.padding = '20px 24px 16px 24px'
        headerEl.style.borderBottom = 'none'
      }
      
      // 修改标题文字
      const title = dialog.querySelector('.el-dialog__title')
      if (title) {
        const titleEl = title as HTMLElement
        titleEl.style.color = 'white'
        titleEl.style.fontWeight = '700'
        titleEl.style.textAlign = 'center'
      }
      
      // 修改关闭按钮
      const closeBtn = dialog.querySelector('.el-dialog__close')
      if (closeBtn) {
        const closeBtnEl = closeBtn as HTMLElement
        closeBtnEl.style.color = 'white'
        closeBtnEl.style.fontSize = '18px'
      }
      
      // 修改body
      const body = dialog.querySelector('.el-dialog__body')
      if (body) {
        const bodyEl = body as HTMLElement
        bodyEl.style.padding = '0'
      }
    }
  })
}

const confirmAssign = async () => {
  if (!assignForm.value.roomId) {
    ElMessage.warning('请选择宿舍')
    return
  }
  
  assigning.value = true
  try {
    // 🔥 修复：后端返回的是User对象，直接使用id
    const userId = currentStudent.value!.id
    await roomApi.assignStudent(userId, assignForm.value.roomId)
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    assignForm.value.roomId = null
    loadStudents()
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败')
  } finally {
    assigning.value = false
  }
}

const handleCheckIn = async (student: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要让 ${student.name} 办理入住吗？`,
      '确认入住',
      { 
        type: 'info',
        customClass: 'apple-message-box',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 🔥 修复：后端返回的是User对象，直接使用id
    console.log('调用入住API，userId:', student.id)
    await request.post(`/students/${student.id}/checkin`)
    
    ElMessage.success(`${student.name} 入住成功`)
    loadStudents()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('入住失败:', error)
      ElMessage.error(error.response?.data?.message || '入住失败')
    }
  }
}

const handleCheckOut = async (student: User) => {
  try {
    // 检查学生是否有宿舍
    if (!student.room) {
      ElMessage.warning('该学生未分配宿舍')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要让 ${student.name} 退宿吗？`,
      '确认退宿',
      { 
        type: 'warning',
        customClass: 'apple-message-box',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 🔥 修复：后端返回的是User对象，直接使用id
    console.log('调用退宿API，userId:', student.id)
    await roomApi.removeStudent(student.id)
    ElMessage.success('退宿成功')
    loadStudents()
  } catch (error: any) {
    console.error('退宿失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退宿失败')
    }
  }
}

// 导出学生 - Excel格式
const exportStudents = async () => {
  if (students.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  try {
    // 动态导入xlsx库
    const XLSX = await import('xlsx')
    
    // 准备Excel数据
    const excelData = students.value.map(s => {
      const roomInfo = s.room ? `${s.room.building.name}-${s.room.roomNumber}` : '未分配'
      const status = s.status === 'ACTIVE' ? '入住' : '离校'
      const gender = s.gender === 'MALE' ? '男' : '女'
      
      return {
        '姓名': s.name,
        '学号': s.studentId,
        '性别': gender,
        '电话': s.phone || '',
        '班级': s.className || '未分班',
        '宿舍': roomInfo,
        '状态': status
      }
    })
    
    // 创建工作簿和工作表
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)
    
    // 设置列宽
    const colWidths = [
      { wch: 10 }, // 姓名
      { wch: 12 }, // 学号
      { wch: 6 },  // 性别
      { wch: 15 }, // 电话
      { wch: 15 }, // 班级
      { wch: 20 }, // 宿舍
      { wch: 8 }   // 状态
    ]
    ws['!cols'] = colWidths
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, '学生名单')
    
    // 生成文件名
    const fileName = `学生名单_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('学生名单导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

// 导入相关逻辑
const showImportDialog = () => {
  importDialogVisible.value = true
  importFile.value = null
}

const handleFileChange = (file: any) => {
  importFile.value = file.raw
}

const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  importing.value = true
  
  try {
    // 动态导入xlsx库
    const XLSX = await import('xlsx')
    
    // 检查文件类型（移到外部作用域）
    const fileName = importFile.value!.name.toLowerCase()
    
    const reader = new FileReader()
    
    reader.onload = async (e) => {
      try {
        const data = e.target?.result
        let students: any[] = []
        
        if (fileName.endsWith('.xlsx') || fileName.endsWith('.xls')) {
          // 处理Excel文件
          const workbook = XLSX.read(data, { type: 'array' })
          const sheetName = workbook.SheetNames[0]
          const worksheet = workbook.Sheets[sheetName]
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
          
          if (jsonData.length <= 1) {
            ElMessage.warning('Excel文件内容为空或只有表头')
            importing.value = false
            return
          }
          
          // 解析Excel数据 (跳过表头)
          for (let i = 1; i < jsonData.length; i++) {
            const row = jsonData[i] as any[]
            if (row && row.length >= 4 && row[0] && row[1]) {
              students.push({
                name: String(row[0]).trim(),
                studentId: String(row[1]).trim(),
                gender: String(row[2] || '男').includes('女') ? 'FEMALE' : 'MALE',
                phone: String(row[3] || '').trim(),
                className: String(row[4] || '').trim()
              })
            }
          }
        } else if (fileName.endsWith('.csv')) {
          // 处理CSV文件（兼容性保留）
          const text = new TextDecoder('utf-8').decode(data as ArrayBuffer)
          const rows = text.split('\n').filter(row => row.trim())
          
          if (rows.length <= 1) {
            ElMessage.warning('CSV文件内容为空或只有表头')
            importing.value = false
            return
          }
          
          // 解析CSV数据 (跳过表头)
          for (let i = 1; i < rows.length; i++) {
            const columns = rows[i].split(',').map(col => col.trim())
            if (columns.length >= 4 && columns[0] && columns[1]) {
              students.push({
                name: columns[0],
                studentId: columns[1],
                gender: columns[2].includes('女') ? 'FEMALE' : 'MALE',
                phone: columns[3],
                className: columns[4] || ''
              })
            }
          }
        } else {
          ElMessage.error('不支持的文件格式，请上传Excel(.xlsx/.xls)或CSV文件')
          importing.value = false
          return
        }
        
        if (students.length === 0) {
          ElMessage.warning('没有找到有效的学生数据')
          importing.value = false
          return
        }
        
        // 调用批量导入API
        const response = await request.post('/students/batch-import', {
          students: students
        })
        
        ElMessage.success(response.data || '批量导入完成')
        importDialogVisible.value = false
        loadStudents()
      } catch (error: any) {
        console.error('导入处理失败:', error)
        ElMessage.error(error.message || '导入失败，请检查文件格式')
        importing.value = false
      }
    }
    
    // 根据文件类型选择读取方式
    if (fileName.endsWith('.xlsx') || fileName.endsWith('.xls')) {
      reader.readAsArrayBuffer(importFile.value!)
    } else {
      reader.readAsArrayBuffer(importFile.value!) // CSV也用ArrayBuffer读取以支持编码检测
    }
  } catch (error: any) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请重试')
    importing.value = false
  }
}

// 下载Excel模板
const downloadTemplate = async () => {
  try {
    // 动态导入xlsx库
    const XLSX = await import('xlsx')
    
    // 创建模板数据
    const templateData = [
      {
        '姓名': '张三',
        '学号': '20240001',
        '性别': '男',
        '电话': '13800000001',
        '班级': '计算机1班'
      },
      {
        '姓名': '李四',
        '学号': '20240002',
        '性别': '女',
        '电话': '13800000002',
        '班级': '计算机1班'
      },
      {
        '姓名': '王五',
        '学号': '20240003',
        '性别': '男',
        '电话': '13800000003',
        '班级': '计算机2班'
      }
    ]
    
    // 创建工作簿和工作表
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(templateData)
    
    // 设置列宽
    const colWidths = [
      { wch: 10 }, // 姓名
      { wch: 12 }, // 学号
      { wch: 6 },  // 性别
      { wch: 15 }, // 电话
      { wch: 15 }  // 班级
    ]
    ws['!cols'] = colWidths
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, '学生导入模板')
    
    // 导出文件
    XLSX.writeFile(wb, '学生导入模板.xlsx')
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('模板下载失败:', error)
    ElMessage.error('模板下载失败，请重试')
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.students-view {
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
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.action-btn {
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
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.action-btn:active {
  transform: scale(0.96);
}

.export-btn {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.import-btn {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.selection-btn {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.archive-btn {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.cancel-btn {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
}

.select-all-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.checkin-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.checkin-btn:disabled,
.checkout-btn:disabled,
.class-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  background: #9ca3af !important;
}

.checkout-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.class-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.reset-btn {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
}

.reset-btn:hover {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%);
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 1px 6px;
  border-radius: 6px;
  font-size: 9px;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.tag-primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.tag-danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.tag-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.tag-info {
  background: linear-gradient(135deg, #a5b4fc 0%, #818cf8 100%);
  color: white;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

/* 统一输入框和下拉框高度 */
.header-actions :deep(.el-input__wrapper) {
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.header-actions :deep(.el-select .el-input__wrapper) {
  height: 32px;
}

.header-actions :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
}

/* 筛选结果统计 - 内联样式，与按钮高度一致 */
.filter-stats-inline {
  display: inline-flex;
  align-items: center;
  padding: 6px 15px;
  height: 32px;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  white-space: nowrap;
  box-sizing: border-box;
}

.filter-stats-inline .stats-text {
  font-size: 14px;
  color: #1e40af;
  font-weight: 600;
  line-height: 1.2;
}

.filter-stats-inline .total-count {
  color: #6b7280;
  font-weight: 500;
  margin-left: 2px;
}

.mr-1 {
  margin-right: 4px;
}

/* 网格布局 */
.students-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  grid-auto-rows: min-content;
  gap: 8px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  align-content: start;
}

.student-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  min-height: 195px;
  display: flex;
  flex-direction: column;
}

/* 确保内部元素没有边框 - 超强优先级 */
.student-card *,
.student-card *::before,
.student-card *::after {
  border: none !important;
  border-top: none !important;
  border-bottom: none !important;
  border-left: none !important;
  border-right: none !important;
}

.student-card .card-header,
.student-card .card-body,
.student-card .card-footer {
  border: none !important;
  border-top: none !important;
  border-bottom: none !important;
  border-left: none !important;
  border-right: none !important;
  box-shadow: none !important;
}

/* 特别针对可能的分割线 */
.student-card .card-body::before,
.student-card .card-body::after,
.student-card .card-footer::before,
.student-card .card-footer::after {
  display: none !important;
  content: none !important;
}

.student-card.selected {
  border-color: #3b82f6;
  background: #dbeafe;
}

.student-card.selected .card-body {
  background: #dbeafe;
}

.student-card.selected .card-footer {
  background: #dbeafe;
}

.student-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.selection-checkbox {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

.selection-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #3b82f6;
}

.card-header {
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: none !important;
}

.student-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.student-avatar.male {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.student-avatar.female {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-text {
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.student-basic {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.status-tag-spacer {
  flex: 1;
}

.name {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.student-id {
  font-size: 10px;
  color: #6b7280;
  font-weight: 500;
}

.card-body {
  padding: 6px 10px;
  background: white;
  border-bottom: none !important;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
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
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  background: white;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
  color: #4b5563;
  font-size: 11px;
  font-weight: 500;
}

.info-item:last-child {
  margin-bottom: 0;
}

.room-text {
  color: #059669;
  font-weight: 600;
}

.no-room {
  color: #9ca3af;
  font-style: italic;
}

.card-footer {
  padding: 6px 10px;
  background: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  border: none !important;
  border-top: none !important;
  outline: none;
}

/* 当只有文本提示时，减少高度 */
.card-footer:has(.no-action-hint) {
  min-height: 24px;
  padding: 4px 10px;
}

.card-footer::before,
.card-footer::after {
  display: none !important;
}

.no-action-hint {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
}

/* 苹果风格操作按钮 */
.apple-action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.apple-action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.apple-action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.apple-action-btn.success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.apple-action-btn.success:hover {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.apple-action-btn.primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
}

.apple-action-btn.primary:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.apple-action-btn.danger {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.apple-action-btn.danger:hover {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}


.upload-icon-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
  color: #9ca3af;
}

.el-upload__tip {
  margin-top: 12px;
  text-align: left;
  color: #6b7280;
  line-height: 1.5;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
  text-align: center;
}

.tip-format {
  background: rgba(59, 130, 246, 0.05);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 8px;
  padding: 10px;
  margin: 8px 0;
  font-size: 13px;
}

.format-example {
  font-family: 'Courier New', monospace;
  background: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: #1e40af;
}

.tip-rule {
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 8px;
  padding: 8px;
  margin-top: 8px;
  text-align: center;
  font-size: 13px;
  color: #059669;
}

.template-download {
  text-align: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}
</style>

<style>
/* Apple风格筛选器样式 */
.students-view .header-actions .el-select {
  border-radius: 12px;
}

.students-view .header-actions .el-select .el-select__wrapper {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1.5px solid #e5e7eb !important;
  border-radius: 12px !important;
  padding: 8px 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.students-view .header-actions .el-select .el-select__wrapper:hover {
  border-color: #3b82f6 !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15) !important;
  transform: translateY(-1px);
}

.students-view .header-actions .el-select .el-select__wrapper.is-focused {
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.students-view .header-actions .el-select .el-select__placeholder {
  color: #9ca3af !important;
  font-weight: 500 !important;
  font-size: 14px !important;
}

.students-view .header-actions .el-select .el-select__selected-item {
  color: #1f2937 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
}

.students-view .header-actions .el-input__wrapper {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1.5px solid #e5e7eb !important;
  border-radius: 12px !important;
  padding: 8px 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.students-view .header-actions .el-input__wrapper:hover {
  border-color: #3b82f6 !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15) !important;
  transform: translateY(-1px);
}

.students-view .header-actions .el-input__wrapper.is-focus {
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.students-view .header-actions .el-input__inner {
  color: #1f2937 !important;
  font-weight: 500 !important;
  font-size: 14px !important;
}

.students-view .header-actions .el-input__inner::placeholder {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

/* 下拉菜单样式 - Apple风格 */
.apple-select-dropdown.el-select-dropdown.el-popper {
  border-radius: 12px !important;
  border: 1px solid rgba(0, 0, 0, 0.08) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08) !important;
  padding: 8px !important;
  margin-top: 8px !important;
  background: white !important;
  backdrop-filter: blur(20px) !important;
}

.apple-select-dropdown .el-popper__arrow {
  display: none !important;
}

.apple-select-dropdown .el-scrollbar {
  border-radius: 8px !important;
  background: transparent !important;
}

.apple-select-dropdown .el-scrollbar__view {
  background: transparent !important;
}

.apple-select-dropdown .el-select-dropdown__wrap {
  max-height: 240px !important;
  background: transparent !important;
}

.apple-select-dropdown .el-select-dropdown__list {
  padding: 0 !important;
  background: transparent !important;
}

.apple-select-dropdown .el-select-dropdown__item {
  border-radius: 8px !important;
  padding: 10px 14px !important;
  margin: 2px 0 !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #1f2937 !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  line-height: 1.5 !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  cursor: pointer !important;
}

.apple-select-dropdown .el-select-dropdown__item:hover,
.apple-select-dropdown .el-select-dropdown__item.is-hovering {
  background: rgba(59, 130, 246, 0.1) !important;
  color: #3b82f6 !important;
  transform: scale(1.02) !important;
}

.apple-select-dropdown .el-select-dropdown__item.is-selected {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.15) 100%) !important;
  color: #2563eb !important;
  font-weight: 600 !important;
}

.apple-select-dropdown .el-select-dropdown__item.is-selected:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(37, 99, 235, 0.2) 100%) !important;
}

/* 自定义消息框样式 */
.custom-message-box {
  border-radius: 16px !important;
  padding: 24px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3) !important;
}

.custom-message-box .el-message-box__header {
  padding-bottom: 16px !important;
}

.custom-message-box .el-message-box__title {
  font-size: 20px !important;
  font-weight: 700 !important;
  color: #1f2937 !important;
}

.custom-message-box .el-message-box__content {
  padding: 16px 0 !important;
}

.custom-message-box .el-message-box__message {
  font-size: 15px !important;
  line-height: 1.6 !important;
  color: #4b5563 !important;
}

.custom-message-box .el-message-box__btns {
  padding-top: 20px !important;
  display: flex !important;
  gap: 12px !important;
  justify-content: flex-end !important;
}

.custom-message-box .el-button {
  padding: 10px 24px !important;
  border-radius: 12px !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  transition: all 0.3s ease !important;
}

.custom-message-box .el-button--default {
  background: #f3f4f6 !important;
  border: none !important;
  color: #6b7280 !important;
}

.custom-message-box .el-button--default:hover {
  background: #e5e7eb !important;
  transform: translateY(-1px);
}

.custom-message-box .el-button--primary {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3) !important;
}

.custom-message-box .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4) !important;
}

.custom-message-box .el-message-box__status {
  font-size: 24px !important;
}

/* 苹果风格分配结果对话框 */
.apple-result-dialog {
  border-radius: 20px !important;
  padding: 0 !important;
  max-width: 520px !important;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(20px) !important;
  background: rgba(255, 255, 255, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.apple-result-dialog .el-message-box__header {
  padding: 24px 24px 16px 24px !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-radius: 20px 20px 0 0 !important;
  margin: 0 !important;
}

.apple-result-dialog .el-message-box__title {
  font-size: 20px !important;
  font-weight: 700 !important;
  color: white !important;
  text-align: center !important;
  letter-spacing: -0.5px !important;
}

.apple-result-dialog .el-message-box__content {
  padding: 0 !important;
  max-height: 500px !important;
  overflow: hidden !important;
}

.apple-result-dialog .el-message-box__message {
  padding: 0 !important;
  margin: 0 !important;
}

.apple-result-dialog .el-message-box__btns {
  padding: 16px 24px 24px 24px !important;
  border-top: 1px solid rgba(0, 0, 0, 0.06) !important;
  background: rgba(248, 250, 252, 0.8) !important;
  border-radius: 0 0 20px 20px !important;
}

.apple-result-dialog .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 12px 32px !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.apple-result-dialog .el-button--primary:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.6) !important;
}

/* 分配结果内容样式 */
.apple-assignment-result {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.9) 100%);
}

.stat-item {
  text-align: center;
  padding: 12px 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  min-width: 80px;
  transition: all 0.3s ease;
}

.stat-item.success {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(5, 150, 105, 0.1) 100%);
  border-color: rgba(16, 185, 129, 0.2);
}

.stat-item.failed {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(220, 38, 38, 0.1) 100%);
  border-color: rgba(239, 68, 68, 0.2);
}

.stat-number {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-item.success .stat-number {
  color: #059669;
}

.stat-item.failed .stat-number {
  color: #dc2626;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  letter-spacing: 0.5px;
}

.result-details {
  max-height: 240px;
  overflow-y: auto;
  padding: 0 16px 16px 16px;
}

.building-group {
  margin-bottom: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.building-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.building-icon {
  font-size: 20px;
  margin-right: 10px;
}

.building-info {
  flex: 1;
}

.class-name {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 2px;
}

.building-name {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.students-list {
  padding: 4px 0;
}

.student-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

.student-item:hover {
  background: rgba(0, 0, 0, 0.02);
}

.student-avatar {
  margin-right: 12px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  color: white;
}

.avatar-circle.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.avatar-circle.failed {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.student-info {
  flex: 1;
}

.student-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.room-number {
  font-size: 13px;
  color: #059669;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  padding: 2px 8px;
  border-radius: 6px;
  display: inline-block;
}

.error-reason {
  font-size: 13px;
  color: #dc2626;
  font-weight: 500;
  background: rgba(239, 68, 68, 0.1);
  padding: 2px 8px;
  border-radius: 6px;
  display: inline-block;
}

/* 滚动条样式 */
.result-details::-webkit-scrollbar {
  width: 6px;
}

.result-details::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.result-details::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

/* 苹果风格学生对话框 */
.apple-student-dialog {
  border-radius: 20px !important;
  padding: 0 !important;
  max-width: 480px !important;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(0, 0, 0, 0.05) !important;
  overflow: hidden !important;
}

.apple-student-dialog .el-dialog__header {
  padding: 0 !important;
  margin: 0 !important;
}

.apple-student-dialog .el-dialog__body {
  padding: 0 !important;
  max-height: 500px !important;
  overflow-y: auto !important;
}

.apple-student-dialog .el-dialog__footer {
  padding: 0 !important;
  margin: 0 !important;
}

.apple-student-dialog .custom-dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.apple-student-dialog .custom-dialog-header .header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.apple-student-dialog .custom-dialog-header .header-icon {
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

.apple-student-dialog .custom-dialog-header .header-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0 0 4px 0;
}

.apple-student-dialog .custom-dialog-header .header-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.apple-student-dialog .custom-dialog-header .close-btn {
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

.apple-student-dialog .custom-dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 表单容器 */
.apple-form-container {
  padding: 12px 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.apple-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 表单分组 */
.form-section {
  background: rgba(248, 250, 252, 0.6);
  border-radius: 10px;
  padding: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title::before {
  content: '';
  width: 3px;
  height: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

/* 表单组 */
.form-group {
  margin-bottom: 4px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 3px;
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
  padding: 8px 12px !important;
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
  font-size: 14px !important;
  color: #1f2937 !important;
  font-weight: 500 !important;
}

.apple-input .el-input__inner::placeholder {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

/* 宿舍显示 */
.room-display {
  background: white;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 性别选择器 */
.gender-selector {
  display: flex;
  gap: 10px;
}

.gender-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}

.gender-option:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.gender-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.gender-icon {
  font-size: 16px;
}

/* 对话框底部 */
.apple-dialog-footer {
  display: flex;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 20px 20px;
}

.apple-btn {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  font-size: 15px;
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

.apple-btn.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.apple-btn.danger:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.6);
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 滚动条样式 */
.apple-student-dialog .el-dialog__body::-webkit-scrollbar {
  width: 6px;
}

.apple-student-dialog .el-dialog__body::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.apple-student-dialog .el-dialog__body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.apple-student-dialog .el-dialog__body::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.result-details::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* 分配宿舍表单特殊样式 */
.apple-assign-dialog .apple-select .el-select__wrapper {
  background: rgba(248, 250, 252, 0.8) !important;
  border: 1.5px solid #e5e7eb !important;
  border-radius: 12px !important;
  padding: 12px 16px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.apple-assign-dialog .apple-select .el-select__wrapper:hover {
  border-color: #10b981 !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
}

.apple-assign-dialog .apple-select .el-select__wrapper.is-focused {
  border-color: #10b981 !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2) !important;
}

.apple-assign-dialog .apple-select .el-select__placeholder {
  color: #9ca3af !important;
  font-weight: 500 !important;
}

.apple-assign-dialog .apple-select .el-select__input {
  color: #374151 !important;
  font-weight: 600 !important;
}

/* 绿色主题按钮 */
.apple-assign-dialog .apple-btn.primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4) !important;
}

.apple-assign-dialog .apple-btn.primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669 0%, #047857 100%) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.6) !important;
}

/* 紧凑型表单样式 */
.compact-form {
  padding: 16px 20px;
}

.student-info {
  background: rgba(16, 185, 129, 0.05);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  flex: 1;
}

.gender-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.gender-badge.male {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.gender-badge.female {
  background: rgba(236, 72, 153, 0.1);
  color: #db2777;
  border: 1px solid rgba(236, 72, 153, 0.3);
}

.room-select {
  margin-bottom: 8px;
}

.select-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

/* 批量分班对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  padding: 0 !important;
}

/* 批量分班对话框 */
:deep(.apple-batch-class-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  padding: 0 !important;
}

:deep(.apple-batch-class-dialog .el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.apple-batch-class-dialog .el-dialog__body) {
  padding: 24px !important;
}

:deep(.apple-batch-class-dialog .el-dialog__footer) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.el-dialog__body) {
  padding: 24px !important;
}

:deep(.el-dialog__footer) {
  padding: 0 !important;
  margin: 0 !important;
}

/* 自定义对话框标题栏 */
.custom-dialog-header {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.custom-dialog-header .header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.custom-dialog-header .header-icon {
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

.custom-dialog-header .header-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0 0 4px 0;
}

.custom-dialog-header .header-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.custom-dialog-header .close-btn {
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

.custom-dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 对话框底部按钮区域 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 16px 16px;
}

.dialog-footer .el-button {
  min-width: 100px;
  font-weight: 500;
}

/* 只读字段样式 */
.readonly-hint {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
  margin-left: 4px;
}

.readonly-input.is-disabled {
  background-color: #f9fafb !important;
  border-color: #e5e7eb !important;
  cursor: not-allowed;
}

.readonly-input.is-disabled .el-input__inner {
  color: #6b7280 !important;
  -webkit-text-fill-color: #6b7280 !important;
}

.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
}

/* 字段提示样式 */
.field-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.4;
}
</style>

<style>
/* 分配宿舍对话框 - 全局样式（必须在 scoped 外面） */
.apple-assign-dialog.el-dialog {
  border-radius: 16px !important;
  overflow: hidden !important;
  padding: 0 !important;
  margin: 0 !important;
}

.apple-assign-dialog .el-dialog__header {
  padding: 20px 24px !important;
  margin: 0 !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border-radius: 0 !important;
}

.apple-assign-dialog .el-dialog__header.show-close {
  padding: 20px 24px !important;
  margin: 0 !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border-radius: 0 !important;
}

.apple-assign-dialog .el-dialog__title {
  color: white !important;
  font-weight: 600 !important;
  font-size: 18px !important;
}

.apple-assign-dialog .el-dialog__body {
  padding: 24px !important;
  margin: 0 !important;
}

.apple-assign-dialog .el-dialog__headerbtn {
  top: 20px !important;
  right: 24px !important;
}

.apple-assign-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 20px !important;
}

.apple-assign-dialog .el-dialog__headerbtn .el-dialog__close:hover {
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 强制覆盖 Element Plus 的所有可能样式 */
.el-overlay .apple-assign-dialog.el-dialog {
  padding: 0 !important;
}

.el-overlay .apple-assign-dialog .el-dialog__header {
  margin: 0 !important;
  padding: 20px 24px !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
}
</style>
