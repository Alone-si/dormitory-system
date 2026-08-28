# 🐳 SmartDormSystem Docker 部署指南

## 📦 一键启动整个项目

### **前置要求**
- 已安装 Docker Desktop
- 已安装 Docker Compose

### **启动步骤**

#### 1️⃣ 打开命令行，进入项目根目录
```bash
cd z:\SmartDormSystem
```

#### 2️⃣ 一键启动所有服务（首次启动会自动构建镜像）
```bash
docker-compose up -d
```

**参数说明：**
- `up`：启动服务
- `-d`：后台运行（detached mode）

#### 3️⃣ 查看启动状态
```bash
docker-compose ps
```

你应该看到 3 个服务都在运行：
- `smartdorm-mysql` - 数据库
- `smartdorm-backend` - 后端 API
- `smartdorm-frontend` - 前端界面

#### 4️⃣ 访问系统
- **前端界面**：http://localhost
- **后端 API**：http://localhost:8080

---

## 🛠 常用命令

### **查看日志**
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### **停止服务**
```bash
# 停止所有服务（保留数据）
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 会删除数据库数据）
docker-compose down -v
```

### **重启服务**
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### **重新构建镜像**
```bash
# 代码修改后，重新构建并启动
docker-compose up -d --build

# 只重新构建特定服务
docker-compose build backend
docker-compose up -d backend
```

---

## 📊 数据持久化

数据库使用本机 MySQL；Docker Volume `upload-data` 只保存用户上传文件。

删除容器不会删除本机数据库，但仍应定期备份。

后端容器只校验数据库表结构，不会自动修改表。首次部署前，请先在开发模式启动一次后端完成建表或更新。

### **备份数据库**
```bash
mysqldump -u root -p alone > backup.sql
```

### **恢复数据库**
```bash
mysql -u root -p alone < backup.sql
```

---

## 🚀 分享给朋友

### **方法 1：导出镜像文件**
```bash
# 导出镜像
docker save smartdormsystem-backend:latest > backend.tar
docker save smartdormsystem-frontend:latest > frontend.tar

# 朋友导入镜像
docker load < backend.tar
docker load < frontend.tar
```

### **方法 2：上传到 Docker Hub**
```bash
# 登录 Docker Hub
docker login

# 打标签
docker tag smartdormsystem-backend:latest yourusername/smartdorm-backend:v1.0
docker tag smartdormsystem-frontend:latest yourusername/smartdorm-frontend:v1.0

# 推送到 Docker Hub
docker push yourusername/smartdorm-backend:v1.0
docker push yourusername/smartdorm-frontend:v1.0
```

朋友只需要：
```bash
docker pull yourusername/smartdorm-backend:v1.0
docker pull yourusername/smartdorm-frontend:v1.0
docker-compose up -d
```

---

## 🐛 故障排查

### **问题 1：端口被占用**
```bash
# 修改 docker-compose.yml 中的端口映射
ports:
  - "8081:8080"  # 将 8080 改为 8081
```

### **问题 2：后端连接不上数据库**
```bash
# 查看后端日志
docker-compose logs backend

# 检查 MySQL 是否启动成功
docker-compose ps mysql
```

### **问题 3：前端无法访问后端 API**
```bash
# 检查 Nginx 配置
docker exec smartdorm-frontend cat /etc/nginx/conf.d/default.conf

# 重启前端服务
docker-compose restart frontend
```

---

## 📝 默认账号

- **管理员**：admin / admin123
- **学生**：20240001 / 123456

---

## 🎉 完成！

现在你的项目已经完全 Docker 化了！
- ✅ 一键启动
- ✅ 环境隔离
- ✅ 数据持久化
- ✅ 轻松分享
