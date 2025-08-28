# Koin Multi-Module Project

基于 Koin 的多模块 Android 项目架构，采用注解处理器自动生成依赖注入配置。

## 项目结构

```
koin-mutil-module/
├── app/                           # 主应用模块
├── components-build/              # 构建工具模块
│   ├── annotation/               # 自定义注解定义
│   └── processor/                # 注解处理器实现
├── components-business/           # 业务逻辑模块
│   ├── moduleA/                  # 业务模块A
│   │   ├── api/                  # 对外接口
│   │   ├── impl/                 # 具体实现
│   │   └── sample/               # 示例代码
│   └── moduleB/                  # 业务模块B
│       ├── api/                  # 对外接口
│       └── impl/                 # 具体实现
├── components-capability/         # 能力模块
│   └── moduleC/                  # 能力模块C
│       ├── api/                  # 对外接口
│       └── impl/                 # 具体实现
├── components-core/              # 核心模块
│   └── moduleE/                  # 核心模块E
│       ├── impl/                 # 具体实现
│       └── sample/               # 示例代码
└── components-presentation/       # 表现层模块
    └── moduleD/                  # 表现层模块D
        └── src/                  # 源代码
```

## 核心特性

### 1. 自动化依赖注入

- 使用 `@KoinModule` 注解标记模块
- 注解处理器自动生成 Koin 模块配置
- 编译时检查和验证

### 2. 模块化架构

- **Business Layer**: 业务逻辑模块 (moduleA, moduleB)
- **Capability Layer**: 能力模块 (moduleC)
- **Core Layer**: 核心模块 (moduleE)
- **Presentation Layer**: 表现层模块 (moduleD)

### 3. 构建工具

- **Annotation**: 自定义注解定义
- **Processor**: KSP 注解处理器实现

## 重要限制

> **⚠️ <span style="color: red;">每个模块最多只能有一个 @KoinModule 注解</span>**
>
> 为了保证模块的单一职责和依赖管理的清晰性，每个模块只允许定义一个 KoinModule。如果需要多个配置，请考虑拆分模块或在单个
> KoinModule 中组织多个 provide 方法。

## 模块生命周期

### 1. 模块定义阶段
```kotlin
@KoinModule
class BusinessModuleA {
   @Provides
   fun provideService(): ServiceA = ServiceAImpl()

   @Provides
   fun provideRepository(service: ServiceA): RepositoryA = RepositoryAImpl(service)
}
```

### 2. 编译时处理

- KSP 注解处理器扫描所有 `@KoinModule` 注解
- 验证每个模块只有一个 KoinModule 注解
- 生成对应的 Koin 模块配置代码
- 创建模块注册清单

### 3. 运行时初始化
```kotlin
// 自动生成的模块配置
val generatedModules = listOf(
   businessModuleA,
   businessModuleB,
   capabilityModuleC,
   coreModuleE,
   presentationModuleD
)

// 应用启动时初始化
startKoin {
   androidLogger()
   androidContext(this@Application)
   modules(generatedModules)
}
```

### 4. 依赖解析

- Koin 容器根据类型和限定符解析依赖
- 支持单例、工厂和作用域实例
- 自动处理循环依赖检测

### 5. 模块卸载
```kotlin
// 动态卸载模块（如果需要）
unloadModules(listOf(businessModuleA))

// 重新加载模块
loadModules(listOf(newBusinessModuleA))
```

## 使用指南

### 1. 添加依赖

在模块的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
   implementation(project(":components-build:annotation"))
   kapt(project(":components-build:processor"))
   // 或使用 KSP
   ksp(project(":components-build:processor"))
}
```

### 2. 定义模块
```kotlin
@KoinModule
class YourModule {
   @Provides
   @Singleton
   fun provideYourService(): YourService = YourServiceImpl()

   @Provides
   fun provideYourRepository(
      service: YourService
   ): YourRepository = YourRepositoryImpl(service)
}
```

### 3. 注入依赖
```kotlin
class YourActivity : AppCompatActivity() {
   private val yourService: YourService by inject()
   private val yourRepository: YourRepository by inject()
}
```

## 最佳实践

### 1. 模块设计原则

- **单一职责**: 每个模块专注于特定的业务领域
- **接口隔离**: 通过 api 模块暴露接口，impl 模块提供实现
- **依赖倒置**: 高层模块不依赖低层模块，都依赖于抽象

### 2. 注解使用规范

- 每个模块只使用一个 `@KoinModule` 注解
- 合理使用 `@Singleton`、`@Factory`、`@Scoped` 等作用域注解
- 为复杂依赖提供明确的 `@Named` 限定符

### 3. 模块组织建议
```
module/
├── api/                    # 对外接口定义
│   └── src/main/java/
├── impl/                   # 接口实现
│   └── src/main/java/
│       └── di/            # 依赖注入配置
│           └── ModuleDI.kt # @KoinModule 注解类
└── sample/                # 示例和测试
    └── src/main/java/
```

## 构建和运行

### 1. 编译项目
```bash
./gradlew build
```

### 2. 运行应用

```bash
./gradlew :app:installDebug
```

### 3. 清理构建

```bash
./gradlew clean
```

## 故障排除

### 常见问题

1. **多个 KoinModule 注解错误**
   - 错误信息: "Multiple @KoinModule annotations found in module"
   - 解决方案: 确保每个模块只有一个 @KoinModule 注解类

2. **依赖循环引用**
   - 错误信息: "Circular dependency detected"
   - 解决方案: 重新设计模块依赖关系，使用接口解耦

3. **注解处理器未运行**
   - 检查 KSP 或 KAPT 配置是否正确
   - 确保注解处理器依赖已正确添加

## 技术栈

- **Kotlin**: 主要开发语言
- **Koin**: 依赖注入框架
- **KSP**: Kotlin Symbol Processing API
- **Android Gradle Plugin**: 构建工具
- **Gradle**: 项目构建系统

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。