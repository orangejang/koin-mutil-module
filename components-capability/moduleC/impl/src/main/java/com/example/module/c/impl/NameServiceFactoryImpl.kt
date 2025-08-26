package com.example.module.c.impl

import com.example.module.c.api.INameService
import com.example.module.c.api.INameServiceFactory

/**
 * NameService工厂类，根据配置返回不同的实现
 */
class NameServiceFactoryImpl : INameServiceFactory {
    override fun createNameService(config: Int): INameService {
        return when (config) {
            1 -> NameServiceImpl()
            2 -> NameServiceImpl2()
            else -> throw IllegalArgumentException("Invalid config value: $config")
        }
    }
}