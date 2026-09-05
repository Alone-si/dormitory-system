# SmartDormSystem 启动说明

日常开发可直接双击根目录的 `启动项目-开发模式.bat`，它会检查 Java、Node.js 和项目目录，然后分别启动后端与前端。

访问地址：

- 前端：<http://localhost:5173>
- 后端：<http://localhost:8080>

停止时运行 `停止项目-开发模式.bat`。

Docker 没有单独的批处理启动菜单，请按照 [README-Docker.md](README-Docker.md) 使用 `docker compose`。

数据库密码保存在被 Git 忽略的 `backend/config/application-local.properties` 中。首次管理员通过环境变量安全创建，具体见主 README；项目不会自动创建测试学生。
