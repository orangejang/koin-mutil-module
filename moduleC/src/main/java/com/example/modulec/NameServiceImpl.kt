package com.example.modulec

/**
 * NameService接口的实现类
 */
class NameServiceImpl : INameService {
    override fun getUserName(): String {
        return "Json Wang"
    }
}