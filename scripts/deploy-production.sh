#!/bin/bash

# Java项目生产环境部署脚本
# 使用方法: ./deploy-production.sh [tag]
# 如果不指定tag，默认使用latest

set -e

# 配置
IMAGE_NAME="ghcr.io/Raze666888/myJavaproject"
TAG=${1:-latest}
CONTAINER_NAME="myJavaProject-prod"
APP_PORT=7002

echo "🚀 开始部署 Java 项目到生产环境"
echo "镜像: $IMAGE_NAME:$TAG"
echo "容器名: $CONTAINER_NAME"
echo "端口: $APP_PORT"

# 停止并删除旧容器（如果存在）
echo "停止旧容器..."
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

# 拉取最新镜像
echo "拉取镜像..."
docker pull $IMAGE_NAME:$TAG

# 启动新容器
echo "启动新容器..."
docker run -d \
  --name $CONTAINER_NAME \
  --restart unless-stopped \
  -p $APP_PORT:$APP_PORT \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /app/logs:/app/logs \
  $IMAGE_NAME:$TAG

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
    docker logs $CONTAINER_NAME
    exit 1
fi

# 清理悬空镜像
echo "清理悬空镜像..."
docker image prune -f

echo "🎉 部署完成！"