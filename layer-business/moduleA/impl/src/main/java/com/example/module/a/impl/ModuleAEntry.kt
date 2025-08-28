package com.example.module.a.impl

import com.example.data.IModuleLifecycle

class ModuleAEntry : IModuleLifecycle {
    override fun onCreate() {
        println("ModuleA lifecycle onCreate - initializing module resources")
    }

    override fun onDestroy() {
        println("ModuleA lifecycle onDestroy - cleaning up module resources")
    }
}