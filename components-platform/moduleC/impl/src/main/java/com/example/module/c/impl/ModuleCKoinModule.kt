package com.example.module.c.impl

import com.example.module.c.api.INameService
import org.koin.dsl.module

/**
 * ModuleC的Koin模块定义
 */
val moduleCModule = module {
    // 提供NameService的单例实现
    single<INameService> { NameServiceImpl() }
}