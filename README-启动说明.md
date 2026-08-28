# 🚀 SmartDormSystem 启动说明

## 📂 项目启动脚本说明

项目提供了**两种启动模式**，适用于不同场景：

---

## 🎯 快速选择

### **方式 1：使用启动菜单（推荐）**
双击运行：`🚀 启动菜单.bat`

在菜单中选择对应的模式即可。

---

## 📊 两种模式详细对比

### **模式 1️⃣：开发模式（日常开发推荐）**

**启动脚本：** `启动项目-开发模式.bat`

**特点：**
- ⚡ **启动快**：10-30秒即可启动
- 🔥 **热重载**：修改代码自动刷新，无需重启
- 🐛 **可调试**：可以使用 IDE 断点调试
- 📝 **详细日志**：可以看到完整的控制台输出

**访问地址：**
- 前端：http://localhost:5173
- 后端：http://localhost:8080

**环境要求：**
- ✅ Java 17
- ✅ Node.js 18+
- ✅ MySQL 8.0

**适用场景：**
- 👨‍💻 日常开发
- 🐛 调试代码
- 📝 修改功能

---

### **模式 2️⃣：Docker模式（演示/部署推荐）**

**启动脚本：** `启动项目-Docker模式.bat`

**特点：**
- 🐳 **环境隔离**：不影响本地开发环境
- 📦 **一键启动**：自动启动数据库、后端、前端
- 🚀 **易分享**：可以打包分享给朋友
- 💾 **数据持久化**：数据不会丢失

**访问地址：**
- 前端：http://localhost
- 后端：http://localhost:8080

**环境要求：**
- ✅ Docker Desktop

**适用场景：**
- 🎬 演示项目
- 🚀 部署到服务器
- 👥 分享给朋友
- 🧪 测试完整环境

---

## 🛠 其他实用脚本

### **数据库管理**
- `backend/backup-database.bat` - 备份数据库
- `backend/restore-database.bat` - 恢复数据库

### **项目管理**
- `scripts/export_database.bat` - 导出数据库
- `scripts/import-test-data.bat` - 导入测试数据
- `scripts/package_project.bat` - 打包项目

### **环境检查**
- `环境检查.bat` - 检查开发环境是否完整

---

## 💡 使用建议

### **日常开发流程**
```
1. 双击 "启动项目-开发模式.bat"
2. 等待启动完成（约30秒）
3. 浏览器访问 http://localhost:5173
4. 修改代码，保存后自动刷新
5. 完成后关闭两个命令行窗口
```

### **演示/部署流程**
```
1. 双击 "启动项目-Docker模式.bat"
2. 等待启动完成（首次约5-10分钟）
3. 浏览器访问 http://localhost
4. 演示完成后双击 "停止项目-Docker模式.bat"
```

---

## 🔧 故障排查

### **开发模式常见问题**

**问题 1：端口被占用**
```bash
# 查找占用端口的进程
netstat -ano | findstr "8080"
netstat -ano | findstr "5173"

# 结束进程（PID 为上面查到的进程号）
taskkill /F /PID <进程号>
```

**问题 2：MySQL 连接失败**
- 检查 MySQL 服务是否启动
- 检查 `backend/config/application-local.properties` 中的密码

**问题 3：前端依赖安装失败**
```bash
cd frontend
npm config set registry https://registry.npmmirror.com
npm install
```

---

### **Docker模式常见问题**

**问题 1：Docker 未启动**
- 打开 Docker Desktop
- 等待 Docker 引擎启动完成

**问题 2：端口冲突**
- 修改 `docker-compose.yml` 中的端口映射

**问题 3：查看日志**
```bash
docker-compose logs -f
```

---

## 📞 获取帮助

- 📖 开发模式详细文档：查看项目根目录的 README.md
- 🐳 Docker模式详细文档：查看 README-Docker.md
- 🐛 遇到问题：查看对应的日志输出

---

## 🎉 快速开始

**最简单的方式：**
1. 双击 `🚀 启动菜单.bat`
2. 选择对应的模式
3. 开始使用！

祝您使用愉快！✨
