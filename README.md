# 🏠 智能宿舍管理系统 (Smart Dorm System)

一个基于 **Spring Boot + Vue 3** 的现代化宿舍管理系统，提供宿舍分配、学生管理、报修管理等功能。

## 📋 项目简介

本系统是一个功能完善的现代化宿舍管理系统，包括：
- 🏢 宿舍楼栋和房间管理
- 👥 学生信息和入住管理（支持批量操作）
- 🔧 报修申请和处理流程
- 📢 宿舍公告发布和管理
- 📝 请假申请和审批
- 📊 数据统计和可视化分析
- 🔐 权限管理（管理员/学生双端）
- 🎨 现代化UI设计（苹果风格）

## 🛠 技术栈

### 后端
- **Spring Boot 4.0.0** - 最新版本的Spring框架
- **Spring Security** - 安全认证框架
- **Spring Data JPA + Hibernate** - 数据持久化
- **MySQL 8.0+** - 数据库
- **UUID Token** - 自定义认证系统
- **Lombok** - 简化Java代码
- **Maven** - 项目构建工具

### 前端
- **Vue 3 (Composition API)** - 渐进式JavaScript框架
- **Element Plus** - 企业级UI组件库
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP请求库
- **Lucide Vue Next** - 现代化图标库
- **Vite** - 快速的前端构建工具

## 📁 项目结构

```
SmartDormSystem/
├── backend/                    # 后端项目
│   └── bwxw/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/
│       │   │   │   └── org/example/bwxw/
│       │   │   │       ├── controller/    # 控制器层
│       │   │   │       ├── service/       # 业务逻辑层
│       │   │   │       ├── repository/    # 数据访问层
│       │   │   │       ├── entity/        # 实体类
│       │   │   │       └── BwxwApplication.java
│       │   │   └── resources/
│       │   │       └── application.properties
│       │   └── test/
│       └── pom.xml
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── views/             # 页面组件
    │   │   ├── Home.vue       # 首页
    │   │   └── Login.vue      # 登录页
    │   ├── router/            # 路由配置
    │   ├── utils/             # 工具函数
    │   ├── App.vue            # 根组件
    │   ├── main.js            # 入口文件
    │   └── style.css          # 全局样式
    ├── index.html
    ├── vite.config.js         # Vite配置
    └── package.json
```

## 🚀 快速开始

### 前置要求

- **Java 17+**
- **Node.js 18+**
- **MySQL 8.0+**
- **Maven 3.8+**

### 1️⃣ 数据库配置

创建数据库：
```sql
CREATE DATABASE alone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库配置已在 `backend/src/main/resources/application.properties` 中完成：
- 数据库名：`alone`
- 用户名：`root`
- 密码：在 `backend/config/application-local.properties` 中填写（该文件不会提交）
- 端口：`3306`

**注意**：系统会自动创建所有必要的数据表（JPA自动建表）

### 2️⃣ 启动后端

```bash
# 进入后端目录
cd backend

# 使用Maven启动
mvn spring-boot:run

# 或使用IDE（推荐）
# 直接运行 BwxwApplication.java
```

后端将在 **http://localhost:8080** 启动

**首次启动**：系统会自动创建管理员账号和测试数据

### 3️⃣ 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 **http://localhost:5173** 启动

### 4️⃣ 访问系统

打开浏览器访问：**http://localhost:5173**

**测试账号**：

管理员账号：
- 用户名：`admin`
- 密码：`123456`

学生账号：
- 学号：`20240001`
- 密码：`123456`

**注意**：系统包含100个测试学生数据（学号：20240001-20240100）

## 📦 构建部署

### 前端构建
```bash
cd frontend
npm run build
```
构建产物在 `frontend/dist` 目录

### 后端打包
```bash
cd backend
mvn clean package
```
JAR包在 `backend/target` 目录

### 生产环境部署
```bash
# 后端
java -jar backend/target/bwxw-0.0.1-SNAPSHOT.jar

# 前端（需配置Nginx）
# 将 frontend/dist 目录部署到Web服务器
```

## 🎨 界面特色

### 登录页面
- 🎨 现代化蓝色渐变背景
- ✨ 流畅的动画效果（背景渐变、浮动圆圈、卡片入场）
- 📱 完全响应式设计
- 🔐 支持学号/用户名双模式登录

### 管理员端
- 📊 数据统计仪表盘
- 👥 学生管理（支持批量入住、退宿、分班）
- 🏢 宿舍楼管理
- 🏠 房间分配和管理
- 🔧 报修处理
- 📢 公告发布
- 📝 请假审批

### 学生端
- 🏠 个人宿舍信息
- 👤 个人资料管理
- 🔧 在线报修
- 📢 查看公告
- 📝 请假申请
- 👥 查看室友信息

## 🔧 开发指南

### 后端开发
1. 在 `entity` 包下创建实体类
2. 在 `repository` 包下创建数据访问接口
3. 在 `service` 包下编写业务逻辑
4. 在 `controller` 包下创建API接口

### 前端开发
1. 在 `src/views` 下创建页面组件
2. 在 `src/router/index.js` 中配置路由
3. 使用 `src/utils/request.js` 发起API请求
4. 遵循Vue 3 Composition API规范

## 📝 核心功能

### 认证系统
- UUID Token 认证
- 双端权限控制（管理员/学生）
- 自动token刷新
- 安全的密码存储

### 学生管理
- 学生信息CRUD
- 批量导入/导出
- 批量入住/退宿
- 批量分班操作
- 智能宿舍分配

### 宿舍管理
- 楼栋管理（男/女生宿舍）
- 房间管理
- 入住状态跟踪
- 床位统计

### 报修管理
- 在线报修申请
- 报修类型分类
- 紧急程度标记
- 处理进度跟踪

### 公告系统
- 公告发布
- 优先级设置
- 目标群体选择
- 公告类型分类

### 请假管理
- 请假申请
- 请假类型分类
- 审批流程
- 状态跟踪

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证

## 👨‍💻 作者

智能宿舍管理系统开发团队

---

## ⚙️ 系统配置

### 日志配置
生产环境已配置简洁日志输出，只显示重要信息。

### 跨域配置
已配置CORS，支持前后端分离开发。

### 文件上传
- 最大文件大小：2MB
- 上传目录：`uploads/`

---

## 📌 注意事项

- ✅ 首次运行需要确保MySQL服务已启动
- ✅ 数据库表会自动创建（JPA自动建表）
- ✅ 开发环境下前端会自动代理API请求到后端
- ✅ 生产环境需要配置Nginx进行反向代理
- ✅ 系统已包含完整的测试数据
- ✅ 所有密码均为明文存储（开发/演示用途）

## 🎯 项目状态

✅ **项目已完成，可直接使用**

- 后端：完整的RESTful API
- 前端：现代化响应式界面
- 数据库：完整的测试数据
- 认证：UUID Token认证系统
- 功能：所有核心功能已实现

## 📞 技术支持

如有问题，请联系：279750552
