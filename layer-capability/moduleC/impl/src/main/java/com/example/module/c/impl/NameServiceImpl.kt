package com.example.module.c.impl

import com.example.module.c.api.INameService

/**
 * NameService接口的实现类
 */
class NameServiceImpl : INameService {
    override fun getUserName(): String {
        return "Json Wang"
    }
}