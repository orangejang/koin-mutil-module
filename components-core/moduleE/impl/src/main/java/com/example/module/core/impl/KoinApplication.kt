package com.example.module.core.impl

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.data.KoinModuleInfo
import com.example.module.core.impl.modules.ModulesManager
import com.example.module.core.impl.modules.data.ModuleInfo
import com.example.module.core.impl.modules.data.OperationType
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * 应用程序入口，负责初始化Koin
 */
abstract class KoinApplication : Application(), ModulesManager.IModuleManagerListener {

    private var hasInitKoin = false

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        context?.let {
            initKoin(it)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initKoin(this)
        loadAllModules()
    }

    private fun initKoin(context: Context) {
        if (!hasInitKoin) {
            // 初始化Koin
            startKoin {
                // 使用Android日志
                androidLogger(Level.DEBUG)
                // 提供Android上下文
                androidContext(context)
                // 加载所有模块
            }
            hasInitKoin = true
        }
    }

    private fun loadAllModules() {
        val moduleInfos = getKoinModuleInfos()
        Log.i("KoinApplication", "loadAllModules: Found ${moduleInfos.size} modules")
        if (moduleInfos.isEmpty()) {
            Log.e("KoinApplication", "loadAllModules: No modules to load")
            throw IllegalStateException("KoinApplication.loadAllModules: No modules to load")
        }
        ModulesManager.getInstance().run {
            addListener(this@KoinApplication)
            init(moduleInfos)
            loadAllModules()
        }
    }

    override fun onModuleLoaded(moduleInfo: ModuleInfo) {
        Log.i("KoinApplication", "onModuleLoaded: $moduleInfo")
    }

    override fun onModuleUnloaded(moduleInfo: ModuleInfo) {
        Log.i("KoinApplication", "onModuleUnloaded: $moduleInfo")
    }

    override fun onModuleOperationFailed(
        moduleId: String,
        operationType: OperationType,
        error: Throwable
    ) {
        Log.w("KoinApplication", "onModuleOperationFailed: $moduleId, $operationType", error)
    }

    protected abstract fun getKoinModuleInfos(): List<KoinModuleInfo>

}