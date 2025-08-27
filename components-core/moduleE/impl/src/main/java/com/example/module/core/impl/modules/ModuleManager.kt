package com.example.module.core.impl.modules

import android.util.Log
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Koin模块动态管理器
 * 负责KoinModule的动态装载和卸载
 */
class ModuleManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ModuleManager? = null

        fun getInstance(): ModuleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModuleManager().also { INSTANCE = it }
            }
        }
    }

    // 存储已加载的模块信息
    private val loadedModules = ConcurrentHashMap<String, ModuleInfo>()

    // 监听器列表
    private val listeners = CopyOnWriteArrayList<IModuleManagerListener>()

    /**
     * 模块信息数据类
     */
    data class ModuleInfo(
        val id: String,                    // 唯一标识
        val name: String,                  // 模块名称
        val module: Module,                // Koin模块实例
        val isLoaded: Boolean = false      // 是否已加载
    )

    /**
     * 模块管理监听器接口
     */
    interface IModuleManagerListener {
        /**
         * 模块加载成功回调
         */
        fun onModuleLoaded(moduleInfo: ModuleInfo)

        /**
         * 模块卸载成功回调
         */
        fun onModuleUnloaded(moduleInfo: ModuleInfo)

        /**
         * 模块操作失败回调
         */
        fun onModuleOperationFailed(moduleId: String, operation: String, error: Throwable)
    }

    /**
     * 添加监听器
     */
    fun addListener(listener: IModuleManagerListener) {
        listeners.add(listener)
    }

    /**
     * 移除监听器
     */
    fun removeListener(listener: IModuleManagerListener) {
        listeners.remove(listener)
    }

    /**
     * 动态加载KoinModule
     */
    fun loadModule(moduleInfo: ModuleInfo) {
        try {
            // 检查模块是否已经加载
            if (loadedModules.containsKey(moduleInfo.id)) {
                notifyOperationFailed(
                    moduleInfo.id, "LOAD",
                    IllegalStateException("Module ${moduleInfo.id} is already loaded")
                )
                return
            }

            // 加载模块到Koin容器
            GlobalContext.get().loadModules(listOf(moduleInfo.module))

            // 更新模块状态
            val loadedModuleInfo = moduleInfo.copy(isLoaded = true)
            loadedModules[moduleInfo.id] = loadedModuleInfo

            // 通知监听器
            notifyModuleLoaded(loadedModuleInfo)

        } catch (e: Exception) {
            notifyOperationFailed(moduleInfo.id, "LOAD", e)
        }
    }

    /**
     * 动态卸载KoinModule
     */
    fun unloadModule(moduleId: String) {
        try {
            val moduleInfo = loadedModules[moduleId]
            if (moduleInfo == null) {
                notifyOperationFailed(
                    moduleId, "UNLOAD",
                    IllegalStateException("Module $moduleId is not loaded")
                )
                return
            }

            // 从Koin容器中卸载模块
            GlobalContext.get().unloadModules(listOf(moduleInfo.module))

            // 更新模块状态
            val unloadedModuleInfo = moduleInfo.copy(isLoaded = false)
            loadedModules.remove(moduleId)

            // 通知监听器
            notifyModuleUnloaded(unloadedModuleInfo)

        } catch (e: Exception) {
            notifyOperationFailed(moduleId, "UNLOAD", e)
        }
    }

    /**
     * 获取所有已加载的模块信息
     */
    fun getLoadedModules(): List<ModuleInfo> {
        return loadedModules.values.toList()
    }

    /**
     * 检查模块是否已加载
     */
    fun isModuleLoaded(moduleId: String): Boolean {
        return loadedModules.containsKey(moduleId)
    }

    /**
     * 获取指定模块信息
     */
    fun getModuleInfo(moduleId: String): ModuleInfo? {
        return loadedModules[moduleId]
    }

    /**
     * 通知模块加载成功
     */
    private fun notifyModuleLoaded(moduleInfo: ModuleInfo) {
        listeners.forEach { listener ->
            try {
                listener.onModuleLoaded(moduleInfo)
            } catch (e: Exception) {
                // 忽略监听器异常，避免影响主流程
            }
        }
    }

    /**
     * 通知模块卸载成功
     */
    private fun notifyModuleUnloaded(moduleInfo: ModuleInfo) {
        listeners.forEach { listener ->
            try {
                listener.onModuleUnloaded(moduleInfo)
            } catch (e: Exception) {
                // 忽略监听器异常，避免影响主流程
            }
        }
    }

    /**
     * 通知操作失败
     */
    private fun notifyOperationFailed(moduleId: String, operation: String, error: Throwable) {
        listeners.forEach { listener ->
            try {
                listener.onModuleOperationFailed(moduleId, operation, error)
            } catch (e: Exception) {
                // 忽略监听器异常，避免影响主流程
                Log.w("ModuleManager", "notifyOperationFailed exception: ${e.message}")
            }
        }
    }
}