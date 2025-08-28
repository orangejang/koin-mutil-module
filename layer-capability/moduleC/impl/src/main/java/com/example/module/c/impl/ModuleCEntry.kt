package com.example.module.c.impl

import com.example.data.IModuleLifecycle

class ModuleCEntry : IModuleLifecycle {
    override fun onCreate() {
        println("ModuleC lifecycle onCreate")
    }

    override fun onDestroy() {
        println("ModuleC lifecycle onDestroy")
    }
}
