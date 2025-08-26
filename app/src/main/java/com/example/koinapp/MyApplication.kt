package com.example.koinapp

import com.example.module.core.impl.KoinApplication
import com.example.modules.KoinModules
import org.koin.core.module.Module

class MyApplication : KoinApplication() {

    override fun getModules(): List<Module> {
        val modules = KoinModules.getAllModules()
        if (modules.isEmpty()) {
            throw IllegalStateException("No Koin modules found， please check koin-modules.txt or KoinModules.kt")
        }
        return modules
    }
}