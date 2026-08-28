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
      class="apple-building-dialog"
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
import { useBuildingsView } from './BuildingsView.logic'

const {
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
} = useBuildingsView()
</script>

<style scoped src="./BuildingsView.css"></style>