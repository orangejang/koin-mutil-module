package com.example.module.b.api

/**
 * NumberService接口，提供数字相关的服务
 */
interface INumberService {
    /**
     * 生成一个随机数
     * @param min 最小值
     * @param max 最大值
     * @return 生成的随机数
     */
    fun generateRandomNumber(min: Int, max: Int): Int

    /**
     * 检查一个数字是否为偶数
     * @param number 要检查的数字
     * @return 如果是偶数返回true，否则返回false
     */
    fun isEven(number: Int): Boolean
}