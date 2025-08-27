package com.example.module.a.impl

import com.example.annotation.KoinModule
import com.example.module.a.api.IUserService
import com.example.module.b.api.INumberService
import com.example.module.c.api.INameService
import com.example.module.c.api.INameServiceFactory
import org.koin.dsl.module

/**
 * ModuleA的Koin模块定义
 */
@KoinModule(id = "moduleA", name = "用户服务模块")
fun moduleAModule() = module {

    // 动态绑定NameService实现
    single<INameService> { get<INameServiceFactory>().createNameService(2) }

    // 提供UserService的单例实现
    // 注意这里通过get()获取NumberService的实例，实现了模块间的依赖注入
    single<IUserService> {
        UserServiceImpl(
            numberService = get<INumberService>(),
            nameService = get<INameService>()
        )
    }
}