package com.example.module.core.impl.modules

import android.util.Log
import com.example.data.KoinModuleInfo
import com.example.module.core.impl.modules.data.ModuleInfo
import com.example.module.core.impl.modules.data.OperationType
import org.koin.core.context.GlobalContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Koin模块动态管理器
 * 负责KoinModule的动态装载和卸载
 */
class ModulesManager private constructor() {

    private val allModules = CopyOnWriteArrayList<KoinModuleInfo>()

    // 存储已加载的模块信息
    private val loadedModules = ConcurrentHashMap<String, ModuleInfo>()

    // 监听器列表
    private val listeners = CopyOnWriteArrayList<IModuleManagerListener>()

    fun init(koinModuleInfos: List<KoinModuleInfo>) {
        allModules.clear()
        allModules.addAll(koinModuleInfos)
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

    fun getModuleInfo(moduleId: String): ModuleInfo? {
        return loadedModules[moduleId] ?: allModules.find { it.id == moduleId }?.let {
            ModuleInfo.create(it, isLoaded = false)
        }
    }

    /**
     * 一次性加载所有模块
     */
    internal fun loadAllModules() {
        allModules.forEach { moduleInfo ->
            loadModule(moduleInfo, true)
        }
    }

    fun loadModule(moduleId: String, forceLoad: Boolean = false) {
        val moduleInfo = getKoinModuleInfo(moduleId)
        if (moduleInfo == null) {
            notifyOperationFailed(
                moduleId, OperationType.LOAD,
                IllegalStateException("Module $moduleId not found in allModules")
            )
            return
        }
        loadModule(moduleInfo, forceLoad)
    }

    /**
     * 动态卸载KoinModule
     */
    fun unloadModule(moduleId: String) {
        try {
            val moduleInfo = loadedModules[moduleId]
            if (moduleInfo == null) {
                notifyOperationFailed(
                    moduleId, OperationType.UNLOAD,
                    IllegalStateException("Module $moduleId is not loaded")
                )
                return
            }

            val koinModuleInfo = getKoinModuleInfo(moduleId)
            if (koinModuleInfo == null) {
                notifyOperationFailed(
                    moduleId, OperationType.UNLOAD,
                    IllegalStateException("Module $moduleId not found in allModules")
                )
                return
            }

            // 从Koin容器中卸载模块
            GlobalContext.get().unloadModules(listOf(koinModuleInfo.module))
            koinModuleInfo.lifecycle?.onDestroy()

            // 更新模块状态
            val unloadedModuleInfo = moduleInfo.copy(isLoaded = false)
            loadedModules.remove(moduleId)

            // 通知监听器
            notifyModuleUnloaded(unloadedModuleInfo)

        } catch (e: Exception) {
            notifyOperationFailed(moduleId, OperationType.UNLOAD, e)
        }
    }

    /**
     * 动态加载KoinModule
     */
    private fun loadModule(koinModuleInfo: KoinModuleInfo, forceLoad: Boolean = false) {
        try {
            // 检查模块是否已经加载
            if (!forceLoad && loadedModules.containsKey(koinModuleInfo.id)) {
                notifyOperationFailed(
                    koinModuleInfo.id, OperationType.LOAD,
                    IllegalStateException("Module ${koinModuleInfo.id} is already loaded")
                )
                return
            }

            // 加载模块到Koin容器
            GlobalContext.get().loadModules(listOf(koinModuleInfo.module))
            koinModuleInfo.lifecycle?.onCreate()

            // 更新模块状态
            val loadedModuleInfo = ModuleInfo.create(koinModuleInfo, isLoaded = true)
            loadedModules[koinModuleInfo.id] = loadedModuleInfo

            // 通知监听器
            notifyModuleLoaded(loadedModuleInfo)

        } catch (e: Exception) {
            notifyOperationFailed(koinModuleInfo.id, OperationType.LOAD, e)
        }
    }

    private fun getKoinModuleInfo(moduleId: String): KoinModuleInfo? {
        return allModules.find { it.id == moduleId }
    }

    /**
     * 通知模块加载成功
     */
    private fun notifyModuleLoaded(moduleInfo: ModuleInfo) {
        listeners.forEach { listener ->
            try {
                listener.onModuleLoaded(moduleInfo)
            } catch (e: Exception) {
                Log.w("ModuleManager", "notifyModuleLoaded exception: ${e.message}")
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
                Log.w("ModuleManager", "notifyModuleUnloaded exception: ${e.message}")
            }
        }
    }

    /**
     * 通知操作失败
     */
    private fun notifyOperationFailed(
        moduleId: String,
        operationType: OperationType,
        error: Throwable
    ) {
        listeners.forEach { listener ->
            try {
                listener.onModuleOperationFailed(moduleId, operationType, error)
            } catch (e: Exception) {
                Log.w("ModuleManager", "notifyOperationFailed exception: ${e.message}")
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ModulesManager? = null

        fun getInstance(): ModulesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModulesManager().also { INSTANCE = it }
            }
        }
    }

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
        fun onModuleOperationFailed(
            moduleId: String,
            operationType: OperationType,
            error: Throwable
        )
    }
}