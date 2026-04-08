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
import { ref, onMounted } from 'vue'
import { Users, User } from 'lucide-vue-next'
import { useUserStore } from '../../stores/user'
import { getCurrentUser } from '../../api/user'
import request from '../../utils/request'

const userStore = useUserStore()
const roommates = ref<any[]>([])

// 加载最新用户信息
const loadUserInfo = async () => {
  try {
    const response = await getCurrentUser()
    if (response.code === 200) {
      userStore.setUserInfo(response.data)
      // 如果有宿舍信息，加载室友
      if (response.data.room?.id) {
        await loadRoommates(response.data.room.id)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载室友信息
const loadRoommates = async (roomId: number) => {
  try {
    const response = await request.get(`/rooms/${roomId}/students`)
    if (response && Array.isArray(response)) {
      // 过滤掉当前用户
      roommates.value = response.filter((s: any) => s.id !== userStore.userInfo?.id)
    }
  } catch (error) {
    console.error('加载室友信息失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.section-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.section-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #667eea;
}

.section-header h3,
.section-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

/* 个人信息卡片 */
.personal-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #e0f2fe;
}

.personal-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.personal-avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.personal-avatar .avatar-text {
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.basic-info h4 {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 2px 0;
}

.basic-info p {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item .label {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.2px;
  width: fit-content;
}

.status-active {
  background: #d1f4e0;
  color: #0d7d3f;
}

.status-inactive {
  background: #e5e7eb;
  color: #6b7280;
}

.roommates-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.roommate-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.roommate-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
  border-color: #10b981;
}

.roommate-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  overflow: hidden;
  flex-shrink: 0;
  margin-bottom: 12px;
}

.roommate-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.roommate-avatar-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.roommate-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.roommate-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  text-align: center;
}

.roommate-id {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
  text-align: center;
}

.roommate-gender {
  font-size: 12px;
  color: #10b981;
  font-weight: 500;
  margin: 0;
  text-align: center;
}

.status-card h3 {
  margin-bottom: 12px;
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-item .label {
  font-size: 13px;
  color: #999;
}

.status-item .value {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
}

.status-item .value.highlight {
  color: #667eea;
}

.health-tag {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.rules-card {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 16px;
  padding: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.rules-card h3 {
  color: white;
  margin: 0 0 12px 0;
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rules-list li {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
  font-size: 14px;
  line-height: 1.6;
}

.rules-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  font-size: 18px;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .rules-card {
    grid-column: 1;
  }
}
</style>
