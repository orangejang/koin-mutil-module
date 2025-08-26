# Koin Multi-Module Android Project

这是一个基于Koin依赖注入框架的Android多模块项目，展示了如何在复杂的模块化架构中使用Koin进行依赖管理和模块组织。

## 项目架构

### 模块结构

```
koin-mutil-module/
├── app/                           # 主应用模块
├── components-build/              # 构建相关组件
│   ├── annotation/               # 自定义注解
│   └── processor/                # KSP注解处理器
├── components-business/           # 业务逻辑组件
│   ├── moduleA/
│   │   ├── api/                  # 模块A的API接口
│   │   ├── impl/                 # 模块A的实现
│   │   └── sample/               # 模块A的示例
│   └── moduleB/
│       ├── api/                  # 模块B的API接口
│       └── impl/                 # 模块B的实现
├── components-framework/          # 框架层组件
│   └── core/
│       ├── impl/                 # 核心框架实现
│       └── sample/               # 核心框架示例
├── components-platform/           # 平台层组件
│   └── moduleC/
│       ├── api/                  # 模块C的API接口
│       └── impl/                 # 模块C的实现
└── tools/                        # 工具和配置
    └── gradle/                   # Gradle配置文件
```

### 架构分层

- **App Layer**: 应用程序入口，负责整合所有模块
- **Business Layer**: 业务逻辑模块，包含具体的业务功能实现
- **Framework Layer**: 框架层，提供核心的应用框架和基础设施
- **Platform Layer**: 平台层，提供平台相关的功能和服务

## 技术栈

- **Kotlin**: 主要开发语言
- **Android Gradle Plugin**: 7.4.2
- **Kotlin**: 1.8.22
- **Koin**: 依赖注入框架
- **KSP (Kotlin Symbol Processing)**: 1.8.22-1.0.11，用于代码生成
- **KotlinPoet**: 用于生成Kotlin代码

## 核心特性

### 1. 自动化依赖注入

项目使用自定义的`@KoinModule`注解来标记Koin模块：

```kotlin
@KoinModule
fun moduleAModule() = module {
        // 模块A的依赖配置
    }
```

### 2. 自动模块收集

通过KSP注解处理器自动收集所有标记了`@KoinModule`的函数，并生成`KoinModules`类：

```kotlin
object KoinModules {
    fun getAllModules(): List<Module> {
        // 自动生成的模块收集逻辑
    }
}
```

### 3. 统一依赖管理

所有impl模块都依赖于`tools/gradle/koin-dependencies.gradle`文件，确保Koin依赖的版本一致性。

## 快速开始

### 环境要求

- Android Studio Arctic Fox或更高版本
- JDK 11或更高版本
- Android SDK API 33

### 构建项目

1. 克隆项目：

```bash
git clone <repository-url>
cd koin-mutil-module
```

2. 构建项目：

```bash
./gradlew build
```

3. 运行应用：

```bash
./gradlew :app:installDebug
```

## 模块开发指南

### 添加新的业务模块

1. 在`components-business`下创建新模块目录
2. 创建`api`和`impl`子模块
3. 在`impl`模块的`build.gradle`中添加：

```gradle
apply from: rootProject.file('tools/gradle/koin-dependencies.gradle')
```

4. 创建Koin模块：

```kotlin
@KoinModule
fun yourModuleName() = module {
        // 依赖配置
    }
```

### KSP处理器配置

KSP处理器会自动扫描所有标记了`@KoinModule`注解的函数，并在app模块中生成`KoinModules`类。

配置选项：

- `koin.modules.collector`: 设置为"true"以启用模块收集
- `koin.modules.package.name`: 生成类的包名（默认：com.example.modules）
- `koin.modules.file.name`: 生成类的文件名（默认：KoinModules）

## 项目配置

### Gradle配置

主要的Gradle配置文件：

- `build.gradle`: 根项目配置
- `tools/gradle/koin-dependencies.gradle`: Koin依赖统一管理
- 各模块的`build.gradle`: 模块特定配置

### 版本管理

项目使用统一的版本管理：

- Kotlin: 1.8.22
- KSP: 1.8.22-1.0.11
- Android Gradle Plugin: 7.4.2
- Gradle: 7.5

## 故障排除

### 常见问题

1. **KSP编译错误**
    - 确保Kotlin和KSP版本兼容
    - 清理构建缓存：`./gradlew clean`

2. **模块未被自动收集**
    - 检查`@KoinModule`注解是否正确添加
    - 确认模块的`build.gradle`包含KSP配置

3. **依赖冲突**
    - 检查所有impl模块是否都应用了`koin-dependencies.gradle`
    - 使用`./gradlew dependencies`检查依赖树

### 清理缓存

如果遇到构建问题，可以尝试清理所有缓存：

```bash
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew build
```

## 贡献指南

1. Fork项目
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 创建Pull Request

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 联系方式

如有问题或建议，请创建Issue或联系项目维护者。