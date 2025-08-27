package com.example.data

import org.koin.core.module.Module

/**
 * 模块信息数据类
 * 包含模块的基本信息和Koin模块实例
 */
data class KoinModuleInfo(val id: String, val name: String, val module: Module) {
    override fun toString(): String {
        return "KoinModuleInfo(id='$id', name='$name')"
    }
}