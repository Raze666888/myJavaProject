# GitHub Actions CI/CD 配置指南

## 工作流概览

本项目使用GitHub Actions自动化测试流程，包含以下工作流：

### 1. 主CI/CD流程 (ci-cd.yml)
**触发条件**:
- Push到 `main` 或 `develop` 分支
- PR到 `main` 或 `develop` 分支
- 手动触发

**执行步骤**:
1. **单元测试** (Unit Tests)
   - 运行所有单元测试
   - 生成测试报告
   - 上传覆盖率到Codecov

2. **集成测试** (Integration Tests)
   - 启动MySQL和Redis容器
   - 运行集成测试
   - 需要单元测试通过

3. **API测试** (API Tests)
   - 启动应用服务器
   - 运行API集成测试
   - 需要单元测试通过

4. **代码质量检查** (Code Quality)
   - 运行SonarQube分析（可选）
   - 需要单元测试通过

5. **构建产物** (Build Artifact)
   - 构建JAR包
   - 上传构建产物

6. **Docker镜像构建和推送** (Build and Push Docker Image)
   - 构建多平台Docker镜像
   - 推送到GitHub Container Registry
   - 生成构建证明
   - 仅在push到main分支时执行

7. **生产环境部署** (Deploy to Production)
   - 部署到生产环境
   - 仅在push到main分支时执行
   - 需要production环境批准

### 2. E2E测试流程 (e2e-tests.yml)
**触发条件**:
- 每天凌晨2点（UTC）定时执行
- 手动触发
- 修改E2E测试文件

## 所需的GitHub Secrets

在GitHub项目设置中添加以下Secrets（如使用相应功能）:

```
SONAR_HOST_URL       # SonarQube服务器地址（可选）
SONAR_LOGIN          # SonarQube登录Token（可选）

# CD部署相关（必需）
GHCR_TOKEN           # 用于Ubuntu虚拟机拉取GHCR私有镜像（read:packages）
GHCR_USERNAME        # GitHub用户名
VM_HOST              # Ubuntu虚拟机IP
VM_USER              # Ubuntu登录用户
VM_SSH_KEY           # SSH私钥
VM_SSH_PORT          # SSH端口（可选，默认22）
VM_DEPLOY_PATH       # 部署目录（可选，默认/opt/myjavaproject）
DB_ROOT_PASSWORD     # MySQL root密码
DB_USERNAME          # 应用数据库用户名
DB_PASSWORD          # 应用数据库密码
REDIS_PASSWORD       # Redis密码（可为空）
```

## 环境配置

### 生产环境 (Production)
在GitHub项目设置中创建名为 `production` 的环境，用于生产部署的额外保护。

**环境设置**:
- **Environment protection rules**: 启用 "Required reviewers"
- **Deployment branches**: 限制为 `main` 分支

## 工作流日志查看

### 在GitHub上查看
1. 进入项目的 **Actions** 标签
2. 选择工作流运行
3. 查看各个Job的输出

### 在本地查看
```bash
# 列出最近的运行
gh run list

# 查看特定运行的日志
gh run view <RUN_ID>

# 下载日志
gh run download <RUN_ID>
```

## 测试报告

### 测试报告位置
- **单元测试报告**: Checks → Unit Tests Report
- **集成测试报告**: Checks → Integration Tests Report
- **API测试报告**: Checks → API Tests Report

### 覆盖率报告
- 通过Codecov上传和查看
- 访问 https://codecov.io 查看详细报告

## 故障排查

### 常见问题

#### 1. 数据库连接失败
**症状**: `Connection refused` 错误

**解决**:
```yaml
services:
  mysql:
    # 确保health checks配置正确
    options: >-
      --health-cmd="mysqladmin ping -h localhost"
      --health-interval=10s
      --health-timeout=5s
      --health-retries=3
```

#### 2. Maven缓存问题
**症状**: 依赖下载失败

**解决**: 清除缓存并重新运行
```bash
# 本地清除缓存
rm -rf ~/.m2/repository

# GitHub Actions会自动处理
```

#### 3. 超时问题
**症状**: `timeout` 错误

**解决**: 增加超时时间
```yaml
- name: Run tests
  timeout-minutes: 30
  run: mvn clean test
```

#### 4. 权限问题
**症状**: `Permission denied` 错误

**解决**: 检查分支保护规则
- 设置 → Branches → Branch protection rules
- 确保CI检查配置正确

## 手动触发工作流

### 通过GitHub界面
1. 进入 **Actions** 标签
2. 选择工作流
3. 点击 **Run workflow** 按钮

### 通过CLI
```bash
# 列出可用的工作流
gh workflow list

# 触发工作流
gh workflow run ci-cd.yml
gh workflow run e2e-tests.yml
```

## 工作流定制

### 修改触发条件
编辑 `.github/workflows/ci-cd.yml`:

```yaml
on:
  push:
    branches: [ main, develop ]
    paths:
      - 'src/**'          # 只在src目录改动时运行
      - 'pom.xml'
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 0 * * *'   # 每天午夜运行
```

### 修改运行环境
```yaml
jobs:
  test:
    runs-on: ubuntu-latest    # 改为其他系统: windows-latest, macos-latest
```

### 添加新的构建矩阵
```yaml
strategy:
  matrix:
    java-version: ['8', '11', '17']
    
steps:
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java-version }}
```

## 性能优化

### 1. 使用Actions缓存
```yaml
- uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-m2-
```

### 2. 并行执行Jobs
```yaml
jobs:
  unit-tests:
    runs-on: ubuntu-latest
  integration-tests:
    runs-on: ubuntu-latest
    needs: unit-tests    # 等待unit-tests完成
```

### 3. 条件执行
```yaml
- name: Run only on main
  if: github.ref == 'refs/heads/main'
  run: mvn deploy
```

## 最佳实践

### 1. 测试隔离
- 每个Job在独立的容器中运行
- 使用服务容器隔离外部依赖
- 清理测试数据

### 2. 失败通知
- 在工作流中配置通知
- 与Slack/钉钉集成

### 3. 安全考虑
- 不在日志中输出敏感信息
- 使用GitHub Secrets存储凭证
- 定期轮换密钥

### 4. 文档维护
- 保持README.md最新
- 记录配置变更
- 提供故障排查指南

## 与IDE集成

### VSCode
安装 GitHub Actions 扩展:
- Extension ID: `GitHub.vscode-github-actions`

### IntelliJ IDEA
- 使用 GitHub Actions 插件
- 可在IDE中查看和管理工作流

## 相关资源

- [GitHub Actions文档](https://docs.github.com/en/actions)
- [工作流语法](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Marketplace中的Actions](https://github.com/marketplace?type=actions)
- [Maven GitHub Actions](https://github.com/actions/setup-java)

## 常用命令

```bash
# 检查workflow语法
gh workflow view ci-cd.yml

# 触发特定workflow
gh workflow run ci-cd.yml --ref main

# 查看上一次运行结果
gh run view --limit 1

# 下载日志
gh run download <RUN_ID> -D logs

# 取消运行
gh run cancel <RUN_ID>
```

---

**最后更新**: 2026-05-09
**维护者**: 开发团队
