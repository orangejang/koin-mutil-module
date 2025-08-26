package com.example.koinapp

import com.example.module.core.impl.KoinApplication
import com.example.modules.KoinModules
import org.koin.core.module.Module

class MyApplication : KoinApplication() {

    override fun getModules(): List<Module> {
        return KoinModules.getAllModules()
    }
}