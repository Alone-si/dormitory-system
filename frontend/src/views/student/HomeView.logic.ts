import { ref, onMounted } from 'vue'
import { Users, User } from 'lucide-vue-next'
import { useUserStore } from '../../stores/user'
import { getCurrentUser } from '../../api/user'
import request from '../../utils/request'

export function useHomeView() {
const userStore = useUserStore()
const roommates = ref<any[]>([])

// 加载最新用户信息
const loadUserInfo = async () => {
  try {
    const response = await getCurrentUser()
    if (response.code === 200) {
      // 资料接口未必返回首次改密标记，保留登录时的只读状态。
      userStore.setUserInfo({
        ...response.data,
        mustChangePassword: response.data.mustChangePassword
          ?? userStore.userInfo?.mustChangePassword
      })
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
  return {
  roommates,
  User,
  Users,
  userStore
  }
}
