package com.example.module.core.impl

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module

/**
 * 应用程序入口，负责初始化Koin
 */
abstract class KoinApplication : Application() {

    private var hasInitModules = false

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        context?.let {
            initModules(it)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initModules(this)
    }

    private fun initModules(context: Context) {
        if (!hasInitModules) {
            // 初始化Koin
            startKoin {
                // 使用Android日志
                androidLogger(Level.DEBUG)
                // 提供Android上下文
                androidContext(context)
                // 加载所有模块
                modules(getModules())
            }
            hasInitModules = true
        }
    }

    protected abstract fun getModules(): List<Module>

}