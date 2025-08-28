package com.example.module.b.impl

import com.example.data.IModuleLifecycle

class ModuleBEntry : IModuleLifecycle {
    override fun onCreate() {
        println("ModuleB lifecycle onCreate - initializing module resources")
    }

    override fun onDestroy() {
        println("ModuleB lifecycle onDestroy - cleaning up module resources")
    }
}