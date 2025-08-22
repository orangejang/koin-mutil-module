package com.example.modulea

import com.example.moduleb.INumberService
import com.example.modulec.INameService
import java.util.UUID

/**
 * UserService接口的实现类
 * 通过Koin注入NumberService实现模块间通信
 */
class UserServiceImpl(
    // 通过构造函数注入NumberService和NameService，实现模块间通信
    private val numberService: INumberService,
    private val nameService: INameService
) : IUserService {

    override fun getUserId(): String {
        return UUID.randomUUID().toString()
    }

    override fun generateUserAge(): Int {
        // 调用moduleB中的NumberService生成18-80之间的随机年龄
        return numberService.generateRandomNumber(18, 80)
    }

    override fun isValidUserId(userId: String): Boolean {
        // 简单的验证逻辑：用户ID长度必须大于10且不能为空
        if (userId.isBlank() || userId.length < 10) {
            return false
        }

        // 使用moduleB中的NumberService检查用户ID的哈希值是否为偶数
        val hashCode = userId.hashCode()
        return numberService.isEven(hashCode)
    }

    override fun getUserName(): String {
        return nameService.getUserName()
    }
}