package com.example.module.b.impl

import com.example.annotation.KoinModule
import com.example.module.b.api.INumberService
import org.koin.dsl.module

/**
 * ModuleB的Koin模块定义
 * 使用entry参数指定模块入口类
 */
@KoinModule(
    id = "moduleB",
    name = "数字服务模块",
    entry = ModuleBEntry::class
)
fun moduleBModule() = module {
    // 提供NumberService的单例实现，传入默认的min和max值
    single<INumberService> { NumberServiceImpl(defaultMin = 0, defaultMax = 100) }
}