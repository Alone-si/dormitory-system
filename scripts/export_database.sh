#!/bin/bash

echo "=========================================="
echo "智能宿舍管理系统 - 数据库导出工具"
echo "=========================================="
echo

# 设置变量
DB_NAME="alone"
EXPORT_FILE="smartdorm_complete.sql"
SCHEMA_FILE="smartdorm_schema.sql"

# 获取MySQL密码
echo -n "请输入MySQL root密码: "
read -s MYSQL_PASSWORD
echo

echo
echo "正在导出完整数据库..."
mysqldump -u root -p$MYSQL_PASSWORD $DB_NAME > $EXPORT_FILE

if [ $? -eq 0 ]; then
    echo "✅ 完整数据库导出成功: $EXPORT_FILE"
else
    echo "❌ 数据库导出失败"
    exit 1
fi

echo
echo "正在导出数据库结构..."
mysqldump --no-data -u root -p$MYSQL_PASSWORD $DB_NAME > $SCHEMA_FILE

if [ $? -eq 0 ]; then
    echo "✅ 数据库结构导出成功: $SCHEMA_FILE"
else
    echo "❌ 数据库结构导出失败"
fi

echo
echo "=========================================="
echo "导出完成！"
echo "文件位置:"
echo "- 完整数据库: $(pwd)/$EXPORT_FILE"
echo "- 仅结构: $(pwd)/$SCHEMA_FILE"
echo "=========================================="
echo
echo "迁移时请使用: $EXPORT_FILE"
echo
