package com.example.koinapp

import com.example.data.KoinModuleInfo
import com.example.module.core.impl.KoinApplication
import com.example.modules.KoinModules

class MyApplication : KoinApplication() {

    override fun getKoinModuleInfos(): List<KoinModuleInfo> {
        return KoinModules.getModules()
    }
}