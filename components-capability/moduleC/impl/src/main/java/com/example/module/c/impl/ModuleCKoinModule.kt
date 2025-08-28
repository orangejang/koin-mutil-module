package com.example.module.c.impl

import com.example.annotation.KoinModule
import com.example.module.c.api.INameService
import com.example.module.c.api.INameServiceFactory
import org.koin.dsl.module

/**
 * ModuleC的Koin模块定义
 * 使用entry参数指定模块入口类
 */
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