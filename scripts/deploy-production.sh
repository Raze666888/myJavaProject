#!/bin/bash

# Java项目生产环境部署脚本
# 使用方法: ./deploy-production.sh [tag]
# 执行前请确保当前目录存在 docker-compose.prod.yml 和 .env.prod

set -e

# 配置
IMAGE_NAME="ghcr.io/raze666888/myjavaproject"
TAG=${1:-latest}
APP_PORT=${SERVER_PORT:-7002}

echo "🚀 开始部署 Java 项目到生产环境"
echo "镜像: $IMAGE_NAME:$TAG"
echo "端口: $APP_PORT"

if [ ! -f ".env.prod" ]; then
    echo "❌ 缺少 .env.prod，请先从 .env.prod.example 复制并填写生产环境变量"
    exit 1
fi

echo "更新镜像标签..."
sed -i "s#image: .*#image: ${IMAGE_NAME}:${TAG}#g" docker-compose.prod.yml

echo "拉取并启动生产环境服务..."
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --remove-orphans

# 等待应用启动
echo "等待应用启动..."
sleep 30

# 健康检查
echo "执行健康检查..."
HEALTH_CHECK_URL="http://localhost:$APP_PORT/actuator/health"
if curl -f -s $HEALTH_CHECK_URL > /dev/null; then
    echo "✅ 部署成功！应用正在运行"
    echo "🌐 应用地址: http://localhost:$APP_PORT"
else
    echo "❌ 健康检查失败，请检查应用日志"
    docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=100 app
    exit 1
fi

# 清理悬空镜像
echo "清理悬空镜像..."
docker image prune -f

echo "🎉 部署完成！"
