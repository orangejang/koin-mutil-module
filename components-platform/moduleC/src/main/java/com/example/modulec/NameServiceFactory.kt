package com.example.modulec

/**
 * NameService工厂类，根据配置返回不同的实现
 */
object NameServiceFactory {
    fun createNameService(config: Int): INameService {
        return when (config) {
            1 -> NameServiceImpl()
            2 -> NameServiceImpl2()
            else -> throw IllegalArgumentException("Invalid config value: $config")
        }
    }
}