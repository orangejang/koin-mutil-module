package com.example.koinapp

import android.app.Application
import com.example.modulea.moduleAModule
import com.example.moduleb.moduleBModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * 应用程序入口，负责初始化Koin
 */
class KoinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化Koin
        startKoin {
            // 使用Android日志
            androidLogger(Level.DEBUG)
            // 提供Android上下文
            androidContext(this@KoinApplication)
            // 加载所有模块
            modules(
                listOf(
                    // app模块
                    appModule,
                    // moduleA模块
                    moduleAModule,
                    // moduleB模块
                    moduleBModule
                )
            )
        }
    }
}