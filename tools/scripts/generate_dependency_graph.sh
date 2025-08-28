#!/bin/bash

# 模块依赖关系图生成脚本
# 使用方法: ./generate_dependency_graph.sh

set -e

# 获取脚本所在目录的项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "=== 模块依赖关系图生成工具 ==="
echo "项目根目录: ${PROJECT_ROOT}"
echo ""

# 切换到项目根目录
cd "${PROJECT_ROOT}"

# 检查是否存在 gradlew
if [ ! -f "./gradlew" ]; then
    echo "错误: 未找到 gradlew 文件，请确保在正确的项目根目录下运行此脚本"
    exit 1
fi

# 执行依赖关系图生成任务
echo "正在生成模块依赖关系图..."
./gradlew -q --init-script tools/gradle/dependency-analysis.gradle generateDependencyGraph

echo ""
echo "=== 生成完成 ==="
echo "输出目录: ${PROJECT_ROOT}/build/dependency_analysis"
echo ""
echo "生成的文件:"
echo "  - dependency_graph.mmd (Mermaid格式)"
echo "  - dependency_graph.dot (DOT格式)"
echo "  - module_statistics.txt (模块统计信息)"

# 检查是否生成了PNG文件
if [ -f "${PROJECT_ROOT}/build/dependency_analysis/dependency_graph.png" ]; then
    echo "  - dependency_graph.png (PNG图片)"
else
    echo "  - dependency_graph.png (未生成，需要安装Graphviz)"
    echo ""
    echo "提示: 要生成PNG图片，请安装Graphviz:"
    echo "  macOS: brew install graphviz"
    echo "  Ubuntu: sudo apt-get install graphviz"
    echo "  CentOS: sudo yum install graphviz"
fi

echo ""
echo "使用说明:"
echo "  1. 查看 module_statistics.txt 了解模块统计信息"
echo "  2. 使用支持Mermaid的工具查看 .mmd 文件"
echo "  3. 使用Graphviz工具查看 .dot 文件"
echo "  4. 直接查看 .png 图片文件（如果已生成）"