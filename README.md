# Koin 多模块架构项目

## 项目概述

这是一个基于 Koin 依赖注入框架的多模块 Android 项目，采用分层架构设计，支持模块的动态加载和卸载，通过自定义注解处理器实现模块间的解耦通信。

## 项目架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        App Module                           │
│                    (应用程序入口)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                  Presentation Layer                         │
│                    (表现层)                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  ModuleD                            │   │
│  │              (UI组件、Activity等)                   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                   Business Layer                            │
│                    (业务层)                                 │
│  ┌─────────────┐              ┌─────────────┐              │
│  │   ModuleA   │              │   ModuleB   │              │
│  │ ┌─────────┐ │              │ ┌─────────┐ │              │
│  │ │   API   │ │              │ │   API   │ │              │
│  │ └─────────┘ │              │ └─────────┘ │              │
│  │ ┌─────────┐ │              │ ┌─────────┐ │              │
│  │ │  IMPL   │ │              │ │  IMPL   │ │              │
│  │ └─────────┘ │              │ └─────────┘ │              │
│  │ ┌─────────┐ │              │             │              │
│  │ │ SAMPLE  │ │              │             │              │
│  │ └─────────┘ │              │             │              │
│  └─────────────┘              └─────────────┘              │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                 Capability Layer                            │
│                   (能力层)                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  ModuleC                            │   │
│  │ ┌─────────┐              ┌─────────┐                │   │
│  │ │   API   │              │  IMPL   │                │   │
│  │ └─────────┘              └─────────┘                │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                    Core Layer                               │
│                    (核心层)                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  ModuleE                            │   │
│  │ ┌─────────┐              ┌─────────┐                │   │
│  │ │  IMPL   │              │ SAMPLE  │                │   │
│  │ └─────────┘              └─────────┘                │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   Build Tools                               │
│                  (构建工具)                                 │
│  ┌─────────────┐              ┌─────────────┐              │
│  │ Annotation  │              │  Processor  │              │
│  │   (注解)    │              │ (注解处理器) │              │
│  └─────────────┘              └─────────────┘              │
└─────────────────────────────────────────────────────────────┘
```

### 模块结构说明

#### 1. 分层架构

- **App Module**: 应用程序入口，负责整个应用的启动和配置
- **Presentation Layer (表现层)**: 包含UI相关组件，如Activity、Fragment等
- **Business Layer (业务层)**: 包含核心业务逻辑模块
- **Capability Layer (能力层)**: 提供通用能力和服务
- **Core Layer (核心层)**: 提供基础功能和核心组件

#### 2. 模块组织方式

每个业务模块采用 API-IMPL-SAMPLE 的组织方式：

- **API**: 定义模块对外暴露的接口
- **IMPL**: 具体实现逻辑
- **SAMPLE**: 示例代码和测试用例

#### 3. 构建工具模块
- **Annotation**: 自定义注解定义
- **Processor**: KSP注解处理器，用于代码生成

## 架构设计思想

### 1. 分层解耦

采用经典的分层架构模式，每一层只依赖于下层，实现了良好的解耦：

- 上层模块通过接口依赖下层模块
- 下层模块不感知上层模块的存在
- 通过依赖注入实现具体实现的注入

### 2. 模块化设计

- **单一职责**: 每个模块只负责特定的功能领域
- **高内聚低耦合**: 模块内部高度内聚，模块间松散耦合
- **可插拔**: 模块可以独立开发、测试和部署

### 3. 依赖注入驱动

- 基于 Koin 框架实现依赖注入
- 通过自定义注解简化模块配置
- 支持模块的动态加载和卸载

### 4. 接口导向编程

- 模块间通过接口进行通信
- 实现了面向接口编程的设计原则
- 便于单元测试和模块替换

## 模块间通信方案

### 工作原理

模块间通信基于 Koin 依赖注入框架，通过接口定义和实现分离的方式实现模块解耦。上层模块通过构造函数注入的方式获取下层模块提供的服务接口实例，从而实现跨模块调用。

### 1. 接口定义与实现分离

#### API模块定义接口

```kotlin
// layer-business/moduleA/api - IUserService.kt
package com.example.module.a.api

interface IUserService {
  fun getUserId(): String
  fun generateUserAge(): Int
  fun isValidUserId(userId: String): Boolean
  fun getUserName(): String
}
```

```kotlin
// layer-capability/moduleC/api - INameService.kt
package com.example.module.c.api

interface INameService {
  fun getUserName(): String
}
```

#### IMPL模块实现接口
```kotlin
// layer-business/moduleA/impl - UserServiceImpl.kt
package com.example.module.a.impl

class UserServiceImpl(
  // 通过构造函数注入其他模块的服务，实现模块间通信
  private val numberService: INumberService,  // 来自moduleB
  private val nameService: INameService       // 来自moduleC
) : IUserService {

  override fun getUserId(): String {
    return UUID.randomUUID().toString()
  }

  override fun generateUserAge(): Int {
    // 调用moduleB中的NumberService生成18-80之间的随机年龄
    return numberService.generateRandomNumber(18, 80)
  }

  override fun isValidUserId(userId: String): Boolean {
    if (userId.isBlank() || userId.length < 10) {
      return false
    }
    // 使用moduleB中的NumberService检查用户ID的哈希值
    val hashCode = userId.hashCode()
    return numberService.isEven(hashCode)
  }

  override fun getUserName(): String {
    // 调用moduleC中的NameService获取用户名
    return nameService.getUserName()
  }
}
```

### 2. Koin模块配置实现依赖注入

```kotlin
// layer-business/moduleA/impl - ModuleAKoinModule.kt
@KoinModule(
  id = "moduleA",
  name = "用户服务模块",
  entry = ModuleAEntry::class
)
fun moduleAModule() = module {
  // 动态绑定NameService实现
  single<INameService> { get<INameServiceFactory>().createNameService(2) }

  // 提供UserService的单例实现
  // 通过get()获取其他模块的服务实例，实现模块间依赖注入
  single<IUserService> {
    UserServiceImpl(
      numberService = get<INumberService>(), // 注入moduleB的服务
      nameService = get<INameService>()      // 注入moduleC的服务
    )
  }
}
```

```kotlin
// layer-capability/moduleC/impl - ModuleCKoinModule.kt
@KoinModule(
  id = "moduleC",
  name = "名称服务模块",
  entry = ModuleCEntry::class
)
fun moduleCModule() = module {
  // 提供NameService的单例实现
  single<INameService> { NameServiceImpl() }
  single<INameServiceFactory> { NameServiceFactoryImpl() }
}
```

### 3. 通信流程说明

1. **接口定义**: 各模块在API子模块中定义对外暴露的服务接口
2. **实现注册**: 在IMPL子模块的Koin配置中注册接口实现
3. **依赖注入**: 其他模块通过构造函数参数声明依赖，Koin自动注入实例
4. **服务调用**: 通过注入的接口实例调用其他模块的服务

### 4. 通信特点

- **类型安全**: 基于接口的强类型通信
- **自动装配**: Koin自动解析和注入依赖
- **松耦合**: 模块只依赖接口，不依赖具体实现
- **可测试**: 便于使用Mock对象进行单元测试

## 模块的动态加载和卸载

### 工作原理

模块的动态加载和卸载基于KSP（Kotlin Symbol Processing）注解处理器和Koin依赖注入框架实现。在编译时，注解处理器扫描所有带有
`@KoinModule`注解的函数，收集模块信息并生成统一的模块管理类。运行时通过该管理类实现模块的动态加载和卸载。

### 1. 注解处理器收集模块信息

#### KSP注解处理器实现

```kotlin
// build-tools/processor - KoinModuleSymbolProcessor.kt
class KoinModuleSymbolProcessor : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    // 获取所有@KoinModule注解的函数
    val koinModuleSymbols = resolver.getSymbolsWithAnnotation(KoinModule::class.java.canonicalName)

    // 统计当前模块中@KoinModule注解的个数
    koinModuleSymbols.forEach { symbol ->
      if (symbol is KSFunctionDeclaration) {
        koinModuleCount++

        // 如果发现超过1个@KoinModule注解，立即抛出异常
        if (koinModuleCount > 1) {
          val errorMessage = """
                        编译错误：当前模块中发现多个 @KoinModule 注解！
                        每个模块最多只能有一个 @KoinModule 注解。
                    """.trimIndent()
          throw IllegalStateException(errorMessage)
        }

        // 提取注解参数并写入共享文件
        val moduleId = getAnnotationValue(koinModuleAnnotation, "id")
        val moduleName = getAnnotationValue(koinModuleAnnotation, "name")
        val entryClass = getAnnotationClassValue(koinModuleAnnotation, "entry")

        writeModuleToSharedFile(packageName, functionName, moduleId, moduleName, entryClass)
      }
    }
  }

  override fun finish() {
    // 在指定的收集器模块中生成KoinModules类
    if (shouldGenerateKoinModules) {
      generateKoinModulesClass(moduleFunctions)
    }
  }
}
```

### 2. 模块信息数据结构

#### 模块信息数据类
```kotlin
// build-tools/annotation - KoinModuleInfo.kt
data class KoinModuleInfo(
  val id: String,
  val name: String,
  val module: Module,
  val lifecycle: IModuleLifecycle? = null
) {
  override fun toString(): String {
    return "KoinModuleInfo(id='$id', name='$name', hasLifecycle=${lifecycle != null})"
  }
}
```

### 3. 自动生成的模块管理类

#### 编译时生成的KoinModules类
```kotlin
// 自动生成的代码示例
object KoinModules {
  /**
   * 获取所有Koin模块
   * @return 所有模块的列表
   */
  fun getModules(): List<KoinModuleInfo> {
    val modules = mutableListOf<KoinModuleInfo>()

    try {
      modules.add(
        KoinModuleInfo(
          id = "moduleA",
          name = "用户服务模块",
          module = moduleAModule(),
          lifecycle = ModuleAEntry()
        )
      )
      println("成功加载Koin模块: com.example.module.a.impl.moduleAModule (带生命周期管理)")
    } catch (ex: Exception) {
      println("模块加载失败: com.example.module.a.impl.moduleAModule - " + ex.javaClass.simpleName + ": " + ex.message)
    }

    try {
      modules.add(
        KoinModuleInfo(
          id = "moduleC",
          name = "名称服务模块",
          module = moduleCModule(),
          lifecycle = ModuleCEntry()
        )
      )
      println("成功加载Koin模块: com.example.module.c.impl.moduleCModule (带生命周期管理)")
    } catch (ex: Exception) {
      println("模块加载失败: com.example.module.c.impl.moduleCModule - " + ex.javaClass.simpleName + ": " + ex.message)
    }

    return modules
  }
}
```

### 4. 动态加载和卸载机制

#### 模块管理器实现
```kotlin
class ModuleManager {
  private val loadedModules = mutableMapOf<String, KoinModuleInfo>()

  fun loadAllModules() {
    val allModules = KoinModules.getModules()

    allModules.forEach { moduleInfo ->
      try {
        // 1. 启动Koin模块
        startKoin { modules(moduleInfo.module) }

        // 2. 调用生命周期onCreate方法
        moduleInfo.lifecycle?.onCreate()

        // 3. 记录已加载模块
        loadedModules[moduleInfo.id] = moduleInfo

        println("模块 ${moduleInfo.name} 加载成功")
      } catch (e: Exception) {
        println("模块 ${moduleInfo.name} 加载失败: ${e.message}")
      }
    }
  }

  fun unloadModule(moduleId: String) {
    loadedModules[moduleId]?.let { moduleInfo ->
      try {
        // 1. 调用生命周期onDestroy方法
        moduleInfo.lifecycle?.onDestroy()

        // 2. 从Koin中卸载模块
        unloadKoinModule(moduleInfo.module)

        // 3. 移除记录
        loadedModules.remove(moduleId)

        println("模块 ${moduleInfo.name} 卸载成功")
      } catch (e: Exception) {
        println("模块 ${moduleInfo.name} 卸载失败: ${e.message}")
      }
    }
  }
}
```

### 5. 加载流程说明

1. **编译时收集**: KSP注解处理器扫描所有`@KoinModule`注解的函数
2. **信息汇总**: 将模块信息写入共享文件，在收集器模块中生成`KoinModules`类
3. **运行时加载**: 通过`KoinModules.getModules()`获取所有模块信息
4. **依次加载**: 遍历模块列表，依次加载每个模块到Koin容器
5. **生命周期管理**: 调用模块的生命周期方法进行初始化和清理

## 模块的生命周期

### 工作原理

模块生命周期通过`IModuleLifecycle`
接口定义，每个模块可以选择实现该接口来管理自己的生命周期。生命周期包括模块的创建和销毁两个阶段，在模块加载和卸载时自动调用相应的生命周期方法。

### 1. 生命周期接口定义

```kotlin
// build-tools/annotation - IModuleLifecycle.kt
interface IModuleLifecycle {
  /**
   * 模块加载时调用
   */
  fun onCreate()

  /**
   * 模块卸载时调用
   */
  fun onDestroy()
}
```

### 2. 模块生命周期实现示例

#### ModuleA的生命周期实现

```kotlin
// layer-business/moduleA/impl - ModuleAEntry.kt
class ModuleAEntry : IModuleLifecycle {
  override fun onCreate() {
    println("ModuleA lifecycle onCreate - initializing module resources")
    // 在这里可以进行模块初始化工作：
    // - 初始化数据库连接
    // - 注册事件监听器
    // - 启动后台服务
    // - 加载配置文件
  }

  override fun onDestroy() {
    println("ModuleA lifecycle onDestroy - cleaning up module resources")
    // 在这里可以进行模块清理工作：
    // - 关闭数据库连接
    // - 注销事件监听器
    // - 停止后台服务
    // - 释放资源
  }
}
```

#### ModuleC的生命周期实现

```kotlin
// layer-capability/moduleC/impl - ModuleCEntry.kt
class ModuleCEntry : IModuleLifecycle {
  override fun onCreate() {
    println("ModuleC lifecycle onCreate")
    // ModuleC的初始化逻辑
  }

  override fun onDestroy() {
    println("ModuleC lifecycle onDestroy")
    // ModuleC的清理逻辑
  }
}
```

### 3. 生命周期与Koin模块的关联

```kotlin
// 在@KoinModule注解中指定生命周期入口类
@KoinModule(
  id = "moduleA",
  name = "用户服务模块",
  entry = ModuleAEntry::class  // 指定生命周期管理类
)
fun moduleAModule() = module {
  // Koin模块定义
}
```

### 4. 生命周期调用时机

- **onCreate()**:
  - 调用时机：模块首次加载到Koin容器时
  - 用途：初始化模块资源、建立连接、注册监听器等
  - 执行顺序：在Koin模块注册之后立即调用

- **onDestroy()**:
  - 调用时机：模块从Koin容器卸载时
  - 用途：清理模块资源、关闭连接、注销监听器等
  - 执行顺序：在Koin模块卸载之前调用

### 5. 生命周期管理特点

- **可选实现**: 模块可以选择是否实现生命周期接口
- **自动调用**: 框架自动在适当时机调用生命周期方法
- **异常安全**: 生命周期方法执行异常不会影响其他模块
- **日志输出**: 自动记录模块加载和卸载的日志信息

## 重要限制

### 1. 每个模块最多只能有一个 @KoinModule 注解

这是项目的核心限制，确保：

- 模块职责单一，避免模块功能过于复杂
- 简化依赖关系管理
- 提高模块加载性能
- 便于模块生命周期管理

### 2. 模块依赖限制

- 不允许循环依赖
- 上层模块不能被下层模块依赖
- 同层模块间的依赖需要谨慎设计

### 3. 接口稳定性要求

- API模块的接口一旦发布，需要保持向后兼容
- 接口变更需要遵循版本管理策略

## 技术栈

### 核心技术

- **Kotlin**: 主要开发语言
- **Koin 3.4.0**: 依赖注入框架
- **KSP (Kotlin Symbol Processing)**: 注解处理
- **Android Gradle Plugin**: 构建工具

### 开发工具

- **Gradle**: 构建系统
- **KSP**: 编译时代码生成
- **自定义注解处理器**: 模块配置自动化

## 快速开始

### 1. 环境要求

- Android Studio Arctic Fox 或更高版本
- Kotlin 1.8.22
- Gradle 7.1.3
- JDK 11 或更高版本

### 2. 项目构建
```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd koin-mutil-module

# 构建项目
./gradlew build

# 运行应用
./gradlew :app:installDebug
```

### 3. 创建新模块

1. 在对应的层级目录下创建新模块
2. 添加 `@KoinModule` 注解
3. 实现 `IModuleLifecycle` 接口（可选）
4. 在 `settings.gradle` 中注册模块
5. 配置模块依赖关系

## 新增模块的步骤操作说明

### 步骤概述

新增一个模块需要遵循项目的分层架构和模块化设计原则，包括创建模块目录结构、定义对外接口、实现具体功能、配置Koin模块等步骤。

### 步骤1：创建模块目录结构

根据模块的功能定位，在相应的层级目录下创建新模块。以在业务层创建`moduleF`为例：

```
layer-business/
└── moduleF/
    ├── api/
    │   ├── build.gradle
    │   └── src/main/java/com/example/module/f/api/
    ├── impl/
    │   ├── build.gradle
    │   └── src/main/java/com/example/module/f/impl/
    └── sample/ (可选)
        ├── build.gradle
        └── src/main/java/com/example/module/f/sample/
```

### 步骤2：定义对外接口

在API子模块中定义模块对外暴露的服务接口：

```kotlin
// layer-business/moduleF/api/src/main/java/com/example/module/f/api/ICalculatorService.kt
package com.example.module.f.api

/**
 * 计算器服务接口，提供基本的数学运算功能
 */
interface ICalculatorService {
  /**
   * 加法运算
   * @param a 第一个数
   * @param b 第二个数
   * @return 运算结果
   */
  fun add(a: Double, b: Double): Double

  /**
   * 减法运算
   * @param a 被减数
   * @param b 减数
   * @return 运算结果
   */
  fun subtract(a: Double, b: Double): Double

  /**
   * 乘法运算
   * @param a 第一个数
   * @param b 第二个数
   * @return 运算结果
   */
  fun multiply(a: Double, b: Double): Double

  /**
   * 除法运算
   * @param a 被除数
   * @param b 除数
   * @return 运算结果
   * @throws IllegalArgumentException 当除数为0时抛出异常
   */
  fun divide(a: Double, b: Double): Double
}
```

### 步骤3：实现服务接口

在IMPL子模块中实现具体的业务逻辑：

```kotlin
// layer-business/moduleF/impl/src/main/java/com/example/module/f/impl/CalculatorServiceImpl.kt
package com.example.module.f.impl

import com.example.module.f.api.ICalculatorService

/**
 * 计算器服务实现类
 */
class CalculatorServiceImpl : ICalculatorService {

  override fun add(a: Double, b: Double): Double {
    return a + b
  }

  override fun subtract(a: Double, b: Double): Double {
    return a - b
  }

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun divide(a: Double, b: Double): Double {
    if (b == 0.0) {
      throw IllegalArgumentException("除数不能为0")
    }
    return a / b
  }
}
```

### 步骤4：创建模块生命周期管理类（可选）

如果模块需要生命周期管理，创建实现`IModuleLifecycle`接口的类：

```kotlin
// layer-business/moduleF/impl/src/main/java/com/example/module/f/impl/ModuleFEntry.kt
package com.example.module.f.impl

import com.example.data.IModuleLifecycle

class ModuleFEntry : IModuleLifecycle {
  override fun onCreate() {
    println("ModuleF lifecycle onCreate - 计算器模块初始化")
    // 在这里可以进行模块初始化工作：
    // - 加载计算器配置
    // - 初始化计算精度设置
    // - 注册计算事件监听器
  }

  override fun onDestroy() {
    println("ModuleF lifecycle onDestroy - 计算器模块清理")
    // 在这里可以进行模块清理工作：
    // - 保存计算历史
    // - 清理缓存数据
    // - 注销事件监听器
  }
}
```

### 步骤5：使用@KoinModule定义模块

创建Koin模块配置文件，使用`@KoinModule`注解标记：

```kotlin
// layer-business/moduleF/impl/src/main/java/com/example/module/f/impl/ModuleFKoinModule.kt
package com.example.module.f.impl

import com.example.annotation.KoinModule
import com.example.module.f.api.ICalculatorService
import org.koin.dsl.module

/**
 * ModuleF的Koin模块定义
 * 使用@KoinModule注解标记，指定模块ID、名称和生命周期入口类
 */
@KoinModule(
  id = "moduleF",
  name = "计算器服务模块",
  entry = ModuleFEntry::class  // 指定生命周期管理类
)
fun moduleFModule() = module {
  // 提供CalculatorService的单例实现
  single<ICalculatorService> { CalculatorServiceImpl() }

  // 如果需要依赖其他模块的服务，可以通过get()注入
  // 例如：依赖日志服务
  // single<ICalculatorService> { 
  //     CalculatorServiceImpl(logger = get<ILogService>()) 
  // }
}
```

### 步骤6：配置模块构建文件

#### API模块的build.gradle

```gradle
// layer-business/moduleF/api/build.gradle
plugins {
    id 'com.android.library'
    id 'kotlin-android'
}

// 应用公共配置
apply from: rootProject.file('tools/gradle/common-android-config.gradle')

dependencies {
    // API模块通常只需要基础依赖
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
}
```

#### IMPL模块的build.gradle

```gradle
// layer-business/moduleF/impl/build.gradle
plugins {
    id 'com.android.library'
    id 'kotlin-android'
    id 'com.google.devtools.ksp'
}

// 应用公共配置
apply from: rootProject.file('tools/gradle/common-android-config.gradle')
// 应用注解处理器配置
apply from: rootProject.file('tools/gradle/common-processor-config.gradle')

dependencies {
    // 依赖本模块的API
    implementation project(':layer-business:moduleF:api')
    
    // 如果需要依赖其他模块的API，在这里添加
    // implementation project(':layer-capability:moduleC:api')
    
    // Koin依赖
    implementation "io.insert-koin:koin-android:${rootProject.ext.koin_version}"
}
```

### 步骤7：注册模块到项目

在`settings.gradle`中注册新模块：

```gradle
// settings.gradle
include ':app'
// ... 其他模块 ...

// 新增的moduleF
include ':layer-business:moduleF:api'
include ':layer-business:moduleF:impl'
include ':layer-business:moduleF:sample'  // 如果有sample模块
```

### 步骤8：在应用中使用新模块

在需要使用新模块的地方添加依赖：

```gradle
// app/build.gradle
dependencies {
    // ... 其他依赖 ...
    
    // 添加新模块的依赖
    implementation project(':layer-business:moduleF:impl')
}
```

### 步骤9：验证模块集成

编译项目验证模块是否正确集成：

```bash
# 清理并重新构建项目
./gradlew clean build

# 检查生成的KoinModules类是否包含新模块
# 查看 build/generated/ksp/debug/kotlin/com/example/modules/KoinModules.kt
```

### 步骤10：使用新模块的服务

在其他模块中通过依赖注入使用新模块的服务：

```kotlin
// 在其他模块中使用CalculatorService
class SomeOtherService(
  private val calculatorService: ICalculatorService  // 通过构造函数注入
) {
  fun performCalculation() {
    val result = calculatorService.add(10.0, 20.0)
    println("计算结果: $result")
  }
}

// 在Koin模块中配置依赖注入
fun someOtherModule() = module {
  single<SomeOtherService> {
    SomeOtherService(calculatorService = get<ICalculatorService>())
  }
}
```

### 注意事项

1. **单一@KoinModule限制**: 每个模块的IMPL子模块中只能有一个`@KoinModule`注解的函数
2. **依赖方向**: 确保依赖关系符合分层架构原则，避免循环依赖
3. **接口稳定性**: API模块的接口一旦发布应保持向后兼容
4. **命名规范**: 遵循项目的命名规范，保持代码风格一致
5. **测试覆盖**: 为新模块编写相应的单元测试和集成测试

### 完整示例总结

通过以上步骤，我们成功创建了一个新的计算器服务模块`moduleF`，它：

- 遵循了项目的分层架构设计
- 实现了接口与实现分离
- 支持依赖注入和模块间通信
- 具备完整的生命周期管理
- 可以被其他模块安全地使用

这个模块将在编译时被KSP注解处理器自动收集，并在运行时通过生成的`KoinModules`类进行动态加载和管理。

## 最佳实践

### 1. 模块设计原则

- 遵循单一职责原则
- 保持接口简洁明确
- 避免过度设计
- 考虑模块的可测试性

### 2. 依赖管理

- 优先使用接口依赖
- 避免传递性依赖
- 合理使用作用域
- 注意内存泄漏

### 3. 性能优化

- 延迟加载非核心模块
- 合理使用单例模式
- 避免重复初始化
- 监控模块加载时间

### 4. 测试策略

- 为每个模块编写单元测试
- 使用模拟对象隔离依赖
- 集成测试验证模块间通信
- 性能测试验证加载效率

## 故障排除

### 1. 常见问题

- **模块加载失败**: 检查依赖关系和注解配置
- **循环依赖**: 重新设计模块依赖关系
- **内存泄漏**: 确保正确实现生命周期方法
- **性能问题**: 优化模块加载顺序和策略

### 2. 调试技巧

- 启用 Koin 日志输出
- 使用断点调试模块加载过程
- 监控内存使用情况
- 分析模块依赖图

## 贡献指南

### 1. 代码规范

- 遵循 Kotlin 编码规范
- 使用有意义的命名
- 添加必要的注释
- 保持代码简洁

### 2. 提交规范

- 使用清晰的提交信息
- 每次提交解决单一问题
- 包含必要的测试用例
- 更新相关文档

### 3. 代码审查

- 所有代码变更需要经过审查
- 确保测试覆盖率
- 验证性能影响
- 检查向后兼容性

## 版本历史

### v1.0.0

- 初始版本发布
- 基础模块化架构
- 动态加载卸载功能
- 自定义注解处理器

## 许可证

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 参与讨论

---

**注意**: 本项目仍在持续开发中，API 可能会发生变化。建议在生产环境使用前进行充分测试。