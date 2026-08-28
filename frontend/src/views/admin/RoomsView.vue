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
import { useRoomsView } from './RoomsView.logic'

const {
  addStudentToRoom,
  AlertCircle,
  buildingFilter,
  classFilter,
  currentPage,
  filteredRooms,
  genderFilter,
  getProgressColor,
  getStatusText,
  getStatusType,
  handlePageChange,
  handlePageSizeChange,
  hasActiveFilters,
  Home,
  loading,
  pageSize,
  paginatedRooms,
  resetFilters,
  rooms,
  uniqueBuildings,
  uniqueClasses,
  Users,
  viewStudentDetail
} = useRoomsView()
</script>

<style scoped src="./RoomsView.css"></style>

<style src="./RoomsView.global.css"></style>
