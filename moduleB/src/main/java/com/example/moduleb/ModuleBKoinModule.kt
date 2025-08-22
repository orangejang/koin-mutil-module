package com.example.moduleb

import org.koin.dsl.module

/**
 * ModuleB的Koin模块定义
 */
val moduleBModule = module {
    // 提供NumberService的单例实现，传入默认的min和max值
    single<INumberService> { NumberServiceImpl(defaultMin = 0, defaultMax = 100) }
}