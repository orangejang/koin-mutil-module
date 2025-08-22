package com.example.module.c.impl

import com.example.module.c.api.INameService

/**
 * NameService接口的第二种实现类
 */
class NameServiceImpl2 : INameService {
    override fun getUserName(): String {
        return "Li mei"
    }
}