import { ref, computed, type Ref } from 'vue'

export function useStudentSelection(paginatedStudents: Ref<any[]>) {
  const selectionMode = ref(false)
  const selectedStudents = ref<any[]>([])

  const isAllSelected = computed(() => {
    return (
      paginatedStudents.value.length > 0 &&
      paginatedStudents.value.every((student) =>
        selectedStudents.value.some((selected) => selected.id === student.id)
      )
    )
  })

  const isSelected = (studentId: number) => {
    return selectedStudents.value.some((student) => student.id === studentId)
  }

  const toggleSelection = (student: any) => {
    const index = selectedStudents.value.findIndex((item) => item.id === student.id)
    if (index > -1) {
      selectedStudents.value.splice(index, 1)
    } else {
      selectedStudents.value.push(student)
    }
  }

  const toggleSelectAll = () => {
    const currentPageIds = paginatedStudents.value.map((student) => student.id)

    if (isAllSelected.value) {
      selectedStudents.value = selectedStudents.value.filter(
        (student) => !currentPageIds.includes(student.id)
      )
      return
    }

    const newSelections = paginatedStudents.value.filter(
      (student) => !selectedStudents.value.some((selected) => selected.id === student.id)
    )
    selectedStudents.value = [...selectedStudents.value, ...newSelections]
  }

  const enterSelectionMode = () => {
    selectionMode.value = true
    selectedStudents.value = []
  }

  const exitSelectionMode = () => {
    selectionMode.value = false
    selectedStudents.value = []
  }

  return {
    selectionMode,
    selectedStudents,
    isAllSelected,
    isSelected,
    toggleSelection,
    toggleSelectAll,
    enterSelectionMode,
    exitSelectionMode
  }
}
