package com.example.moduleb

import kotlin.random.Random

/**
 * NumberService接口的实现类
 */
class NumberServiceImpl(private val defaultMin: Int, private val defaultMax: Int) : INumberService {

    override fun generateRandomNumber(min: Int, max: Int): Int {
        val effectiveMin = if (min == Int.MIN_VALUE) defaultMin else min
        val effectiveMax = if (max == Int.MAX_VALUE) defaultMax else max
        return Random.nextInt(effectiveMin, effectiveMax + 1)
    }

    override fun isEven(number: Int): Boolean {
        return number % 2 == 0
    }
}