#!/bin/bash

# 项目依赖关系图生成脚本
# 功能：分析Gradle项目中各模块的依赖关系并生成可视化图表

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT=$(pwd)
OUTPUT_DIR="$PROJECT_ROOT/build/dependency_analysis"
MERMAID_FILE="$OUTPUT_DIR/dependency_graph.mmd"
DOT_FILE="$OUTPUT_DIR/dependency_graph.dot"
TEXT_FILE="$OUTPUT_DIR/dependency_report.txt"
PNG_FILE="$OUTPUT_DIR/dependency_graph.png"

echo -e "${GREEN}🚀 开始分析项目依赖关系...${NC}"
echo -e "${BLUE}📁 输出目录: $OUTPUT_DIR${NC}"

# 创建输出目录
mkdir -p "$OUTPUT_DIR"

# 清理旧文件
rm -f "$MERMAID_FILE" "$DOT_FILE" "$TEXT_FILE"

# 获取所有模块列表
echo -e "${BLUE}📋 获取项目模块列表...${NC}"
MODULES=$(./gradlew projects --quiet | grep "Project" | sed "s/.*'\(.*\)'.*/\1/" | grep -v "^:$" | sort)

if [ -z "$MODULES" ]; then
    echo -e "${RED}❌ 未找到任何模块！${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 找到以下模块：${NC}"
echo "$MODULES" | sed 's/^/  - /'

# 定义模块样式函数
get_module_color() {
    local module=$1
    case $module in
        ":app")
            echo "lightcoral"
            ;;
        *":moduleD"*)
            echo "lightblue"
            ;;
        *":moduleA"*|*":moduleB"*)
            echo "lightgreen"
            ;;
        *":moduleC"*)
            echo "lightyellow"
            ;;
        *":moduleE"*)
            echo "lightpink"
            ;;
        *"components-build"*)
            echo "lightgray"
            ;;
        *)
            echo "white"
            ;;
    esac
}

get_module_label() {
    local module=$1
    case $module in
        ":app")
            echo "应用模块\\n$module"
            ;;
        *":moduleD"*)
            echo "表现层\\n$module"
            ;;
        *":moduleA"*|*":moduleB"*)
            echo "业务层\\n$module"
            ;;
        *":moduleC"*)
            echo "能力层\\n$module"
            ;;
        *":moduleE"*)
            echo "核心层\\n$module"
            ;;
        *"components-build"*)
            echo "构建工具\\n$module"
            ;;
        *)
            echo "其他\\n$module"
            ;;
    esac
}

get_module_icon() {
    local module=$1
    case $module in
        ":app")
            echo "🚀"
            ;;
        *":moduleD"*)
            echo "🎨"
            ;;
        *":moduleA"*|*":moduleB"*)
            echo "💼"
            ;;
        *":moduleC"*)
            echo "⚡"
            ;;
        *":moduleE"*)
            echo "🔧"
            ;;
        *"components-build"*)
            echo "🛠️"
            ;;
        *)
            echo "📦"
            ;;
    esac
}

# 分析每个模块的依赖关系
echo -e "${BLUE}🔍 分析模块依赖关系...${NC}"

# 初始化输出文件
cat > "$TEXT_FILE" << EOF
# Koin Multi-Module 项目依赖关系分析报告
生成时间: $(date '+%Y-%m-%d %H:%M:%S')

## 模块概览
EOF

cat > "$MERMAID_FILE" << EOF
graph TD
    %% Koin Multi-Module 项目依赖关系图
    %% 生成时间: $(date '+%Y-%m-%d %H:%M:%S')
    
EOF

cat > "$DOT_FILE" << EOF
digraph DependencyGraph {
    rankdir=TB;
    node [shape=box, style=filled];
    
    // 图例
    subgraph cluster_legend {
        label="图例";
        style=filled;
        color=lightgrey;
        
        app_legend [label="应用模块", fillcolor=lightcoral];
        presentation_legend [label="表现层", fillcolor=lightblue];
        business_legend [label="业务层", fillcolor=lightgreen];
        capability_legend [label="能力层", fillcolor=lightyellow];
        core_legend [label="核心层", fillcolor=lightpink];
        build_legend [label="构建工具", fillcolor=lightgray];
    }
    
EOF

# 创建临时文件存储依赖关系
TEMP_DEPS_FILE=$(mktemp)
TEMP_MERMAID_NODES=$(mktemp)
TEMP_MERMAID_EDGES=$(mktemp)
TEMP_DOT_NODES=$(mktemp)
TEMP_DOT_EDGES=$(mktemp)

# 获取模块依赖关系
get_module_dependencies() {
    local module=$1
    echo -e "${YELLOW}  分析模块: $module${NC}"
    
    # 获取该模块的依赖
    local deps=""
    if [ -f "$module/build.gradle" ] || [ -f "$module/build.gradle.kts" ]; then
        deps=$(./gradlew "$module:dependencies" --configuration implementation 2>/dev/null | \
               grep "project :" | \
               sed 's/.*project \(:[^)]*\).*/\1/' | \
               sort | uniq || echo "")
    fi
    
    # 存储依赖关系
    echo "$module|$deps" >> "$TEMP_DEPS_FILE"
    
    # 写入文本报告
    echo "" >> "$TEXT_FILE"
    echo "### $module" >> "$TEXT_FILE"
    if [ -n "$deps" ]; then
        echo "依赖模块:" >> "$TEXT_FILE"
        echo "$deps" | sed 's/^/  - /' >> "$TEXT_FILE"
    else
        echo "无内部模块依赖" >> "$TEXT_FILE"
    fi
}

# 分析所有模块
for module in $MODULES; do
    get_module_dependencies "$module"
done

echo -e "${BLUE}📊 生成依赖关系图...${NC}"

# 生成Mermaid节点
while IFS='|' read -r module deps; do
    if [ -n "$module" ]; then
        # 清理模块名用于Mermaid
        clean_module=$(echo "$module" | sed 's/[:-]/_/g' | sed 's/^_//')
        icon=$(get_module_icon "$module")
        
        # 添加节点定义
        case $module in
            ":app")
                echo "    $clean_module[\"$icon 应用模块<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef appStyle fill:#ffcccb,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module appStyle" >> "$TEMP_MERMAID_NODES"
                ;;
            *":moduleD"*)
                echo "    $clean_module[\"$icon 表现层<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef presentationStyle fill:#add8e6,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module presentationStyle" >> "$TEMP_MERMAID_NODES"
                ;;
            *":moduleA"*|*":moduleB"*)
                echo "    $clean_module[\"$icon 业务层<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef businessStyle fill:#90ee90,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module businessStyle" >> "$TEMP_MERMAID_NODES"
                ;;
            *":moduleC"*)
                echo "    $clean_module[\"$icon 能力层<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef capabilityStyle fill:#ffffe0,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module capabilityStyle" >> "$TEMP_MERMAID_NODES"
                ;;
            *":moduleE"*)
                echo "    $clean_module[\"$icon 核心层<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef coreStyle fill:#ffb6c1,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module coreStyle" >> "$TEMP_MERMAID_NODES"
                ;;
            *"components-build"*)
                echo "    $clean_module[\"$icon 构建工具<br/>$module\"]" >> "$TEMP_MERMAID_NODES"
                echo "    classDef buildStyle fill:#d3d3d3,stroke:#333,stroke-width:2px" >> "$TEMP_MERMAID_NODES"
                echo "    class $clean_module buildStyle" >> "$TEMP_MERMAID_NODES"
                ;;
        esac
        
        # 生成DOT节点
        clean_module_dot=$(echo "$module" | sed 's/[:-]/_/g' | sed 's/^_//')
        color=$(get_module_color "$module")
        label=$(get_module_label "$module")
        echo "    \"$clean_module_dot\" [label=\"$label\", fillcolor=$color];" >> "$TEMP_DOT_NODES"
    fi
done < "$TEMP_DEPS_FILE"

# 生成依赖关系边
while IFS='|' read -r module deps; do
    if [ -n "$module" ] && [ -n "$deps" ]; then
        clean_module=$(echo "$module" | sed 's/[:-]/_/g' | sed 's/^_//')
        
        for dep in $deps; do
            if [ -n "$dep" ]; then
                clean_dep=$(echo "$dep" | sed 's/[:-]/_/g' | sed 's/^_//')
                echo "    $clean_module --> $clean_dep" >> "$TEMP_MERMAID_EDGES"
                echo "    \"$clean_module\" -> \"$clean_dep\";" >> "$TEMP_DOT_EDGES"
            fi
        done
    fi
done < "$TEMP_DEPS_FILE"

# 组装最终文件
cat "$TEMP_MERMAID_NODES" >> "$MERMAID_FILE"
echo "" >> "$MERMAID_FILE"
cat "$TEMP_MERMAID_EDGES" >> "$MERMAID_FILE"

cat "$TEMP_DOT_NODES" >> "$DOT_FILE"
echo "" >> "$DOT_FILE"
cat "$TEMP_DOT_EDGES" >> "$DOT_FILE"
echo "}" >> "$DOT_FILE"

# 清理临时文件
rm -f "$TEMP_DEPS_FILE" "$TEMP_MERMAID_NODES" "$TEMP_MERMAID_EDGES" "$TEMP_DOT_NODES" "$TEMP_DOT_EDGES"

# 生成统计信息
echo "" >> "$TEXT_FILE"
echo "## 统计信息" >> "$TEXT_FILE"
echo "- 总模块数: $(echo "$MODULES" | wc -l)" >> "$TEXT_FILE"
echo "- 应用模块: $(echo "$MODULES" | grep -c ":app" || echo "0")" >> "$TEXT_FILE"
echo "- 表现层模块: $(echo "$MODULES" | grep -c "moduleD" || echo "0")" >> "$TEXT_FILE"
echo "- 业务层模块: $(echo "$MODULES" | grep -c -E "moduleA|moduleB" || echo "0")" >> "$TEXT_FILE"
echo "- 能力层模块: $(echo "$MODULES" | grep -c "moduleC" || echo "0")" >> "$TEXT_FILE"
echo "- 核心层模块: $(echo "$MODULES" | grep -c "moduleE" || echo "0")" >> "$TEXT_FILE"
echo "- 构建工具模块: $(echo "$MODULES" | grep -c "components-build" || echo "0")" >> "$TEXT_FILE"

# 生成使用说明
cat >> "$TEXT_FILE" << EOF

## 文件说明

1. **dependency_report.txt**: 详细的文本格式依赖关系报告
2. **dependency_graph.mmd**: Mermaid格式的依赖关系图，可在GitHub、GitLab等平台直接渲染
3. **dependency_graph.dot**: Graphviz DOT格式，可生成高质量的图像

## 使用方法

### 查看Mermaid图
将dependency_graph.mmd内容复制到以下任一平台：
- GitHub/GitLab的Markdown文件中
- Mermaid Live Editor: https://mermaid.live/
- VS Code的Mermaid插件

### 生成PNG/SVG图像
安装Graphviz后运行：
\`\`\`bash
# 生成PNG图像
dot -Tpng dependency_graph.dot -o dependency_graph.png

# 生成SVG图像  
dot -Tsvg dependency_graph.dot -o dependency_graph.svg

# 生成PDF
dot -Tpdf dependency_graph.dot -o dependency_graph.pdf
\`\`\`

### 使用Mermaid CLI生成图像
\`\`\`bash
# 安装mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# 生成PNG
mmdc -i dependency_graph.mmd -o dependency_graph.png

# 生成SVG
mmdc -i dependency_graph.mmd -o dependency_graph.svg
\`\`\`
EOF

echo -e "${GREEN}✅ 依赖关系分析完成！${NC}"
echo -e "${CYAN}📁 输出文件位置: $OUTPUT_DIR${NC}"
echo -e "${CYAN}📄 文本报告: $TEXT_FILE${NC}"
echo -e "${CYAN}🎨 Mermaid图: $MERMAID_FILE${NC}"
echo -e "${CYAN}🔗 DOT图: $DOT_FILE${NC}"

# 显示简要统计
echo ""
echo -e "${PURPLE}📊 项目统计:${NC}"
echo -e "  总模块数: ${YELLOW}$(echo "$MODULES" | wc -l)${NC}"
echo -e "  应用模块: ${YELLOW}$(echo "$MODULES" | grep -c ":app" || echo "0")${NC}"
echo -e "  表现层模块: ${YELLOW}$(echo "$MODULES" | grep -c "moduleD" || echo "0")${NC}"
echo -e "  业务层模块: ${YELLOW}$(echo "$MODULES" | grep -c -E "moduleA|moduleB" || echo "0")${NC}"
echo -e "  能力层模块: ${YELLOW}$(echo "$MODULES" | grep -c "moduleC" || echo "0")${NC}"
echo -e "  核心层模块: ${YELLOW}$(echo "$MODULES" | grep -c "moduleE" || echo "0")${NC}"
echo -e "  构建工具模块: ${YELLOW}$(echo "$MODULES" | grep -c "components-build" || echo "0")${NC}"

echo ""
# 如果安装了Graphviz，自动生成PNG图像
if command -v dot >/dev/null 2>&1; then
    echo ""
    echo -e "${BLUE}🖼️  检测到Graphviz，正在生成PNG图像...${NC}"
    if dot -Tpng "$DOT_FILE" -o "$PNG_FILE" 2>/dev/null; then
        echo -e "${GREEN}✅ PNG图像已生成: $PNG_FILE${NC}"
    else
        echo -e "${YELLOW}⚠️  PNG图像生成失败，请检查DOT文件格式${NC}"
    fi
else
    echo ""
    echo -e "${YELLOW}💡 未检测到Graphviz工具，无法自动生成PNG图像${NC}"
    echo -e "${YELLOW}   安装Graphviz后可自动生成图像文件:${NC}"
    echo -e "  ${CYAN}brew install graphviz${NC}  # macOS"
    echo -e "  ${CYAN}sudo apt install graphviz${NC}  # Ubuntu/Debian"
fi

echo ""
echo -e "${GREEN}🎉 可以使用以下命令查看结果:${NC}"
echo -e "  ${CYAN}cat $TEXT_FILE${NC}                    # 查看文本报告"
echo -e "  ${CYAN}cat $MERMAID_FILE${NC}                 # 查看Mermaid图代码"
if command -v dot >/dev/null 2>&1; then
    echo -e "  ${CYAN}open $PNG_FILE${NC}                   # 查看PNG图像"
else
    echo -e "  ${CYAN}dot -Tpng $DOT_FILE -o dependency_graph.png${NC}  # 生成PNG图像"
fi
