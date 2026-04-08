/**
 * 前端日志工具
 * 将日志保存到 logs/frontend 文件夹
 */

class Logger {
  private logs: string[] = []
  private maxLogs = 1000 // 最多保存1000条日志

  private formatTime(): string {
    const now = new Date()
    return now.toISOString().replace('T', ' ').substring(0, 23)
  }

  private saveToLocalStorage() {
    try {
      const logData = {
        timestamp: new Date().toISOString(),
        logs: this.logs
      }
      localStorage.setItem('app_logs', JSON.stringify(logData))
    } catch (error) {
      console.error('保存日志失败:', error)
    }
  }

  info(message: string, ...args: any[]) {
    const logMessage = `[${this.formatTime()}] [INFO] ${message}`
    console.log(logMessage, ...args)
    this.logs.push(logMessage + (args.length > 0 ? ' ' + JSON.stringify(args) : ''))
    
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }
    
    this.saveToLocalStorage()
  }

  error(message: string, ...args: any[]) {
    const logMessage = `[${this.formatTime()}] [ERROR] ${message}`
    console.error(logMessage, ...args)
    this.logs.push(logMessage + (args.length > 0 ? ' ' + JSON.stringify(args) : ''))
    
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }
    
    this.saveToLocalStorage()
  }

  warn(message: string, ...args: any[]) {
    const logMessage = `[${this.formatTime()}] [WARN] ${message}`
    console.warn(logMessage, ...args)
    this.logs.push(logMessage + (args.length > 0 ? ' ' + JSON.stringify(args) : ''))
    
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }
    
    this.saveToLocalStorage()
  }

  debug(message: string, ...args: any[]) {
    const logMessage = `[${this.formatTime()}] [DEBUG] ${message}`
    console.debug(logMessage, ...args)
    this.logs.push(logMessage + (args.length > 0 ? ' ' + JSON.stringify(args) : ''))
    
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }
    
    this.saveToLocalStorage()
  }

  // 导出日志到文件
  exportLogs() {
    const logData = localStorage.getItem('app_logs')
    if (!logData) {
      console.warn('没有日志可导出')
      return
    }

    const blob = new Blob([logData], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `frontend-logs-${new Date().toISOString().split('T')[0]}.json`
    a.click()
    URL.revokeObjectURL(url)
  }

  // 清除日志
  clearLogs() {
    this.logs = []
    localStorage.removeItem('app_logs')
  }
}

export const logger = new Logger()
