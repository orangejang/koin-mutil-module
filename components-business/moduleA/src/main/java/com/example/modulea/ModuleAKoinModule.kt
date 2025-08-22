package com.example.modulea

import com.example.moduleb.INumberService
import com.example.modulec.INameService
import com.example.modulec.NameServiceFactory
import com.example.modulec.moduleCModule
import org.koin.dsl.module

/**
 * ModuleA的Koin模块定义
 */
val moduleAModule = module {

    includes(moduleCModule)

    // 动态绑定NameService实现
    single<INameService> { NameServiceFactory.createNameService(get()) }

    // 提供UserService的单例实现
    // 注意这里通过get()获取NumberService的实例，实现了模块间的依赖注入
    single<IUserService> {
        UserServiceImpl(
            numberService = get<INumberService>(),
            nameService = get<INameService>()
        )
    }
}