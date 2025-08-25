package com.example.module.core.impl

import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module

class DynamicModuleLoader {

    private val koin: Koin = GlobalContext.get()

    /**
     * 从网络获取配置
     */
    suspend fun fetchConfig(): Map<String, Boolean> {
        // 模拟网络请求，返回配置
        return mapOf(
            "moduleA" to true,
            "moduleB" to false,
            "moduleC" to true
        )
    }

    /**
     * 根据配置动态加载或卸载模块
     */
    suspend fun loadModules() {
        val config = fetchConfig()
        config.forEach { (moduleName, shouldLoad) ->
//            when (moduleName) {
//                "moduleA" -> handleModule(shouldLoad, moduleAModule)
//                "moduleB" -> handleModule(shouldLoad, moduleBModule)
//                "moduleC" -> handleModule(shouldLoad, moduleCModule)
//            }
        }
    }

    /**
     * 处理模块的加载或卸载
     */
    private fun handleModule(shouldLoad: Boolean, module: Module) {
        if (shouldLoad) {
            if (!module.isLoaded) {
                koin.loadModules(listOf(module))
            }
        } else {
            if (module.isLoaded) {
                koin.unloadModules(listOf(module))
            }
        }
    }
}