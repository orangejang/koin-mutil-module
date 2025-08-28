package com.example.data

/**
 * 模块生命周期接口
 * 定义模块的创建和销毁方法
 */
interface IModuleLifecycle {
    /**
     * 模块加载时调用
     */
    fun onCreate()

    /**
     * 模块卸载时调用
     */
    fun onDestroy()
}
