package com.example.modulea

/**
 * UserService接口，提供用户相关的服务
 */
interface IUserService {
    /**
     * 获取用户ID
     * @return 用户ID
     */
    fun getUserId(): String

    /**
     * 生成用户随机年龄
     * @return 用户年龄
     */
    fun generateUserAge(): Int

    /**
     * 检查用户ID是否有效
     * @param userId 用户ID
     * @return 如果有效返回true，否则返回false
     */
    fun isValidUserId(userId: String): Boolean

    fun getUserName(): String
}