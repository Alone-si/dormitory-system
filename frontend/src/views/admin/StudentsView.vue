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
      class="apple-assign-dialog"
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
      class="apple-batch-class-dialog"
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
      class="apple-student-dialog"
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
                  @click="downloadStudentTemplate"
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
import { useStudentsView } from './StudentsView.logic'

const {
  Archive,
  assignDialogVisible,
  assignForm,
  assigning,
  availableRooms,
  batchArchiveGraduated,
  batchCheckIn,
  batchCheckOut,
  batchClassDialogVisible,
  batchClassForm,
  batchClassLoading,
  BookOpen,
  CheckSquare,
  classFilter,
  confirmAssign,
  confirmBatchClass,
  confirmImport,
  confirmSaveStudent,
  currentPage,
  currentStudent,
  Download,
  enterSelectionMode,
  exitSelectionMode,
  exportStudents,
  filteredStudents,
  handleAssignRoom,
  handleCheckIn,
  handleCheckOut,
  handleDeleteStudent,
  handleEdit,
  handleFileChange,
  handlePageChange,
  handlePageSizeChange,
  hasActiveFilters,
  Home,
  importDialogVisible,
  importing,
  isAllSelected,
  isEditing,
  isSelected,
  loading,
  LogOut,
  Mail,
  openBatchClassDialog,
  pageSize,
  paginatedStudents,
  Plus,
  resetFilters,
  roomFilter,
  savingStudent,
  Search,
  searchQuery,
  selectedStudents,
  selectionMode,
  showCreateDialog,
  showImportDialog,
  statusFilter,
  studentDialogVisible,
  studentForm,
  students,
  toggleSelectAll,
  toggleSelection,
  Upload,
  UploadCloud,
  UserPen,
  UserPlus,
  userStore,
  downloadStudentTemplate,
  X
} = useStudentsView()
</script>

<style scoped src="./StudentsView.css"></style>

<style src="./StudentsView.global.css"></style>

<style src="./StudentsView.global2.css"></style>
