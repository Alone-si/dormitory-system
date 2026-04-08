# 🏠 宿舍管理系统（Dormitory Management System）

## 📌 项目简介
基于 Spring Boot + Vue3 的前后端分离 Web 项目，实现宿舍管理、学生管理及多角色权限控制等功能。  
系统覆盖学生、宿管、管理员三类用户，支持信息管理、申请流程处理等核心业务。

---

## 🛠 技术栈
- 后端：Spring Boot  
- 前端：Vue3 + Axios  
- 数据库：MySQL  
- 构建工具：Maven  
- 版本管理：Git / GitHub  

---

## 🎯 核心功能

### 👤 多角色权限控制
- 支持学生 / 宿管 / 管理员登录  
- 不同角色展示不同功能界面  
- 实现基础权限隔离  

### 📊 学生信息管理
- 学生信息新增 / 修改 / 删除 / 查询  
- 支持基本条件查询  

### 📝 业务流程处理
- 请假申请 → 审核 → 结果反馈  
- 报修申请处理流程  

### 🔗 前后端交互
- 使用 Axios 调用后端接口  
- 基于 RESTful API 设计  
- 使用 JSON 进行数据传输  

---

## ✨ 项目亮点

- ✅ 基于前后端分离架构，提升系统可维护性  
- ✅ 实现完整业务流程（申请 → 审核 → 反馈）  
- ✅ 具备基础权限控制能力（多角色管理）  
- ✅ 具备问题排查能力（404/403/接口异常/数据问题）  
- ✅ 熟悉 Web 项目完整开发流程（开发 → 调试 → 部署）  

---

## 📷 项目截图


- 登录页面
- <img width="2559" height="1525" alt="登录界面" src="https://github.com/user-attachments/assets/ec8d5b34-5829-47c8-a14f-7724a120c9de" />
- 管理后台
- <img width="2556" height="1533" alt="管理员后台" src="https://github.com/user-attachments/assets/8629693b-6340-47f9-8051-572360711a67" />
- 学生管理页面
- <img width="2556" height="1533" alt="管理员后台" src="https://github.com/user-attachments/assets/8629693b-6340-47f9-8051-572360711a67" />
- 申请流程页面
- <img width="2559" height="1515" alt="报修申请" src="https://github.com/user-attachments/assets/c9f56714-b741-465c-95bc-a0a53d794c7e" />
---

## 🚀 项目运行

### 1️⃣ 后端启动

```bash
cd backend
mvn spring-boot:run
### 2️⃣ 前端启动
cd frontend
npm install
npm run dev
