# MyJavaProject

Spring Boot 宠物服务应用 - 提供宠物寄养、医疗、美容等全方位服务

## 🚀 快速开始

### 本地开发环境

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd myJavaProject
   ```

2. **启动开发环境**
   ```bash
   # 使用Docker Compose启动所有服务
   docker-compose up -d
   ```

3. **运行应用**
   ```bash
   # 编译并运行
   mvn clean spring-boot:run
   ```

4. **访问应用**
   - 主应用: http://localhost:7002
   - API文档: http://localhost:7002/swagger-ui.html

## 🏗️ 项目结构

```
src/
├── main/
│   ├── java/           # Java源代码
│   ├── resources/      # 配置文件
│   └── webapp/         # 前端资源
└── test/               # 测试代码
```

## 🧪 测试

### 运行所有测试
```bash
mvn clean test
```

### 运行特定测试类型
```bash
# 仅单元测试
mvn test -Dtest=*Test

# 仅集成测试
mvn verify -DincludeITs
```

## 🐳 Docker 部署

### 开发环境
```bash
docker-compose up -d
```

### 生产环境
```bash
# 1. 配置环境变量
cp .env.prod.example .env.prod
# 编辑 .env.prod 文件，填写实际配置

# 2. 启动生产环境
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# 3. 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 4. 查看日志
docker-compose -f docker-compose.prod.yml logs -f app
```

### 使用部署脚本
```bash
# 部署到生产环境
chmod +x scripts/deploy-production.sh
./scripts/deploy-production.sh [tag]
```

## 🚢 CI/CD 流程

本项目使用 GitHub Actions 实现完整的 CI/CD 流程：

### 持续集成 (CI)
- ✅ 单元测试
- ✅ 集成测试
- ✅ API测试
- ✅ 代码质量检查 (SonarQube)
- ✅ 构建产物

### 持续部署 (CD)
- ✅ Docker 镜像构建和推送
- ✅ 生产环境自动部署
- ✅ 构建证明生成

### 工作流触发条件
- **CI**: Push 到 `main` 或 `develop` 分支，PR 提交
- **CD**: Push 到 `main` 分支时自动触发

### 查看工作流状态
访问项目 GitHub 页面的 **Actions** 标签页查看工作流执行状态。

## 🔧 配置说明

### 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring 配置文件 | `dev` |
| `DB_USERNAME` | 数据库用户名 | - |
| `DB_PASSWORD` | 数据库密码 | - |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |

### 生产环境配置

复制 `.env.prod.example` 为 `.env.prod` 并配置以下变量：
- 数据库连接信息
- Redis 配置
- JWT 密钥
- 文件上传限制
- 监控配置

## 📊 监控和日志

### 健康检查
- 端点: `/actuator/health`
- 返回应用健康状态

### 日志文件
- 位置: `/app/logs/myJavaProject.log`
- 包含 GC 日志和应用日志

### 性能监控
- 端点: `/actuator/metrics`
- 提供 JVM、HTTP 请求等指标

## 🔒 安全配置

详见 [SECURITY-CONFIG.md](SECURITY-CONFIG.md)

## 📚 API 文档

启动应用后访问：
- Swagger UI: http://localhost:7002/swagger-ui.html
- API Docs: http://localhost:7002/v3/api-docs

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

项目维护者 - your-email@example.com

项目链接: [GitHub Repository](https://github.com/your-username/myJavaProject)