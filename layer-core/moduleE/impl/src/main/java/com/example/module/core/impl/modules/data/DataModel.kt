package com.example.module.core.impl.modules.data

import com.example.data.KoinModuleInfo

enum class OperationType {
    LOAD,
    UNLOAD
}

/**
 * 模块信息数据类
 */
data class ModuleInfo(
    val id: String,                    // 唯一标识
    val name: String,                  // 模块名称
    val isLoaded: Boolean = false      // 是否已加载
) {
    companion object {
        fun create(koinModuleInfo: KoinModuleInfo, isLoaded: Boolean = false): ModuleInfo {
            return ModuleInfo(
                id = koinModuleInfo.id,
                name = koinModuleInfo.name,
                isLoaded = isLoaded
            )
        }
    }

    override fun toString(): String {
        return "ModuleInfo(id='$id', name='$name', isLoaded=$isLoaded)"
    }
}