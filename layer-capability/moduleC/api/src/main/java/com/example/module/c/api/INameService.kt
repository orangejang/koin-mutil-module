package com.example.module.c.api

/**
 * NameService接口，提供名称相关的服务
 */
interface INameService {
    /**
     * 获取默认名称
     * @return 默认名称
     */
    fun getUserName(): String
}