<template>
  <div class="student-home">
    <div class="content-grid">
      <!-- 个人信息 -->
      <div class="section-card personal-info-card">
        <div class="section-header">
          <User :size="18" />
          <h3>个人信息</h3>
        </div>
        <div class="personal-info" v-if="userStore.userInfo">
          <div class="avatar-section">
            <div class="personal-avatar">
              <img v-if="userStore.userInfo.avatar" :src="userStore.userInfo.avatar" :alt="userStore.userInfo.name" class="avatar-image" />
              <span v-else class="avatar-text">{{ userStore.userInfo.name ? userStore.userInfo.name.charAt(0) : '?' }}</span>
            </div>
            <div class="basic-info">
              <h4>{{ userStore.userInfo.name }}</h4>
              <p>{{ userStore.userInfo.studentId }}</p>
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ userStore.userInfo.gender === 'MALE' ? '男' : '女' }}</span>
            </div>
            <div class="info-item">
              <span class="label">班级</span>
              <span class="value">{{ userStore.userInfo.className || '未分班' }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话</span>
              <span class="value">{{ userStore.userInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userStore.userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态</span>
              <span class="status-badge" :class="userStore.userInfo.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
                {{ userStore.userInfo.status === 'ACTIVE' ? '在校' : '离校' }}
              </span>
            </div>
          </div>
        </div>
        <el-empty v-else description="个人信息加载中..." :image-size="60" />
      </div>

      <!-- 室友信息 -->
      <div class="section-card roommates-card">
        <div class="section-header">
          <Users :size="18" />
          <h3>室友信息</h3>
        </div>
        <div class="roommates-list" v-if="roommates.length > 0">
          <div v-for="mate in roommates" :key="mate.id" class="roommate-card">
            <div class="roommate-avatar">
              <img v-if="mate.avatar" :src="mate.avatar" :alt="mate.name || '室友'" class="roommate-avatar-image" />
              <span v-else class="roommate-avatar-text">{{ mate.name ? mate.name.charAt(0) : '?' }}</span>
            </div>
            <div class="roommate-details">
              <h4 class="roommate-name">{{ mate.name || '未知' }}</h4>
              <p class="roommate-id">学号: {{ mate.studentId || '-' }}</p>
              <p class="roommate-gender">{{ mate.gender === 'MALE' ? '男' : '女' }}</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无室友信息" :image-size="80" />
      </div>

      <!-- 宿舍状态 -->
      <div class="section-card status-card">
        <h3>宿舍状态</h3>
        <div class="status-grid" v-if="userStore.userInfo?.room">
          <div class="status-item">
            <span class="label">房间号</span>
            <span class="value">{{ userStore.userInfo.room.roomNumber }}</span>
          </div>
          <div class="status-item">
            <span class="label">楼层</span>
            <span class="value">{{ userStore.userInfo.room.floor }}层</span>
          </div>
          <div class="status-item">
            <span class="label">核定人数</span>
            <span class="value">{{ userStore.userInfo.room.capacity }}人</span>
          </div>
          <div class="status-item">
            <span class="label">实住人数</span>
            <span class="value highlight">{{ userStore.userInfo.room.occupied }}人</span>
          </div>
        </div>

        <el-empty v-else description="暂未分配宿舍" :image-size="80" />
      </div>

      <!-- 宿舍公约 -->
      <div class="rules-card">
        <h3>宿舍公约</h3>
        <ul class="rules-list">
          <li>晚熄 23:00 熄灯，请保持安静。</li>
          <li>严禁使用违禁电器（热得快、电磁炉等）。</li>
          <li>保持宿舍清洁卫生，每周定期打扫。</li>
          <li>保持卫生，每日清洁宿舍。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useHomeView } from './HomeView.logic'

const {
  roommates,
  User,
  Users,
  userStore
} = useHomeView()
</script>

<style scoped src="./HomeView.css"></style>
