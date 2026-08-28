import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import type { User } from '../../types'

export function useStudentList() {
  const loading = ref(false)
  const searchQuery = ref('')
  const statusFilter = ref('')
  const classFilter = ref('')
  const roomFilter = ref('')
  const students = ref<User[]>([])
  const currentPage = ref(1)
  const pageSize = ref(30)

  const filteredStudents = computed(() => {
    let filtered = students.value

    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filtered = filtered.filter(
        (student) =>
          student.name.toLowerCase().includes(query) ||
          student.studentId?.toLowerCase().includes(query)
      )
    }

    if (statusFilter.value) {
      filtered = filtered.filter((student) => student.status === statusFilter.value)
    }

    if (classFilter.value === 'HAS_CLASS') {
      filtered = filtered.filter((student) => student.className && student.className.trim() !== '')
    } else if (classFilter.value === 'NO_CLASS') {
      filtered = filtered.filter((student) => !student.className || student.className.trim() === '')
    }

    if (roomFilter.value === 'HAS_ROOM') {
      filtered = filtered.filter((student) => student.room)
    } else if (roomFilter.value === 'NO_ROOM') {
      filtered = filtered.filter((student) => !student.room)
    }

    return filtered
  })

  const hasActiveFilters = computed(() => {
    return Boolean(statusFilter.value || classFilter.value || roomFilter.value)
  })

  const paginatedStudents = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredStudents.value.slice(start, end)
  })

  const resetFilters = () => {
    statusFilter.value = ''
    classFilter.value = ''
    roomFilter.value = ''
    currentPage.value = 1
  }

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
      students.value = data
    } catch (error) {
      ElMessage.error('加载学生列表失败')
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    searchQuery,
    statusFilter,
    classFilter,
    roomFilter,
    students,
    currentPage,
    pageSize,
    filteredStudents,
    hasActiveFilters,
    paginatedStudents,
    resetFilters,
    handlePageChange,
    handlePageSizeChange,
    loadStudents
  }
}
