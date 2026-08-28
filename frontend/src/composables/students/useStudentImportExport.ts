import { ElMessage } from 'element-plus'
import request from '../../utils/request'

type StudentImportRow = {
  name: string
  studentId: string
  gender: 'MALE' | 'FEMALE'
  phone: string
  className: string
}

export async function exportStudents(students: any[]) {
  if (students.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  try {
    const XLSX = await import('xlsx')

    const excelData = students.map((student) => {
      const roomInfo = student.room
        ? `${student.room.building.name}-${student.room.roomNumber}`
        : '未分配'
      const status = student.status === 'ACTIVE' ? '入住' : '离校'
      const gender = student.gender === 'MALE' ? '男' : '女'

      return {
        姓名: student.name,
        学号: student.studentId,
        性别: gender,
        电话: student.phone || '',
        班级: student.className || '未分班',
        宿舍: roomInfo,
        状态: status
      }
    })

    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)
    ws['!cols'] = [
      { wch: 10 },
      { wch: 12 },
      { wch: 6 },
      { wch: 15 },
      { wch: 15 },
      { wch: 20 },
      { wch: 8 }
    ]
    XLSX.utils.book_append_sheet(wb, ws, '学生名单')

    const fileName = `学生名单_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`
    XLSX.writeFile(wb, fileName)
    ElMessage.success('学生名单导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

export async function parseStudentImportFile(file: File): Promise<StudentImportRow[]> {
  const fileName = file.name.toLowerCase()

  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls') && !fileName.endsWith('.csv')) {
    ElMessage.error('不支持的文件格式，请上传Excel(.xlsx/.xls)或CSV文件')
    return []
  }

  try {
    const XLSX = await import('xlsx')

    if (fileName.endsWith('.xlsx') || fileName.endsWith('.xls')) {
      const data = await file.arrayBuffer()
      const workbook = XLSX.read(data, { type: 'array' })
      const sheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[sheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

      if (jsonData.length <= 1) {
        ElMessage.warning('Excel文件内容为空或只有表头')
        return []
      }

      return jsonData.slice(1).flatMap((row: any[]) => {
        if (!row || row.length < 4 || !row[0] || !row[1]) return []
        return [
          {
            name: String(row[0]).trim(),
            studentId: String(row[1]).trim(),
            gender: String(row[2] || '男').includes('女') ? 'FEMALE' : 'MALE',
            phone: String(row[3] || '').trim(),
            className: String(row[4] || '').trim()
          }
        ]
      })
    }

    const text = await file.text()
    const rows = text.split('\n').filter((row) => row.trim())

    if (rows.length <= 1) {
      ElMessage.warning('CSV文件内容为空或只有表头')
      return []
    }

    return rows.slice(1).flatMap((row) => {
      const columns = row.split(',').map((col) => col.trim())
      if (columns.length < 4 || !columns[0] || !columns[1]) return []
      return [
        {
          name: columns[0],
          studentId: columns[1],
          gender: columns[2].includes('女') ? 'FEMALE' : 'MALE',
          phone: columns[3],
          className: columns[4] || ''
        }
      ]
    })
  } catch (error: any) {
    console.error('导入处理失败:', error)
    ElMessage.error(error.message || '导入失败，请检查文件格式')
    return []
  }
}

export async function importStudentsFromFile(file: File) {
  const students = await parseStudentImportFile(file)

  if (students.length === 0) {
    ElMessage.warning('没有找到有效的学生数据')
    return false
  }

  try {
    const response = await request.post('/students/batch-import', {
      students
    })
    ElMessage.success(response.data || '批量导入完成')
    return true
  } catch (error: any) {
    console.error('批量导入失败:', error)
    ElMessage.error(error.message || '批量导入失败')
    return false
  }
}

export async function downloadStudentTemplate() {
  try {
    const XLSX = await import('xlsx')

    const templateData = [
      {
        姓名: '张三',
        学号: '20240001',
        性别: '男',
        电话: '13800000001',
        班级: '计算机1班'
      },
      {
        姓名: '李四',
        学号: '20240002',
        性别: '女',
        电话: '13800000002',
        班级: '计算机1班'
      },
      {
        姓名: '王五',
        学号: '20240003',
        性别: '男',
        电话: '13800000003',
        班级: '计算机2班'
      }
    ]

    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(templateData)
    ws['!cols'] = [{ wch: 10 }, { wch: 12 }, { wch: 6 }, { wch: 15 }, { wch: 15 }]
    XLSX.utils.book_append_sheet(wb, ws, '学生导入模板')
    XLSX.writeFile(wb, '学生导入模板.xlsx')
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('模板下载失败:', error)
    ElMessage.error('模板下载失败，请重试')
  }
}
