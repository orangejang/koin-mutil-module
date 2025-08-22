package com.example.koinapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.module.a.api.IUserService
import com.example.module.b.api.INumberService
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    // 通过Koin注入UserService和NumberService
    // 注意：虽然UserService在moduleA中，NumberService在moduleB中
    // 但我们可以在app模块中直接注入并使用它们
    private val userService: IUserService by inject()
    private val numberService: INumberService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 使用UserService（来自moduleA）
        val userId = userService.getUserId()
        val userAge = userService.generateUserAge()
        val isValidId = userService.isValidUserId(userId)
        val userName = userService.getUserName()

        // 使用NumberService（来自moduleB）
        val randomNumber = numberService.generateRandomNumber(1, 100)
        val isEven = numberService.isEven(randomNumber)

        // 输出结果
        val resultText = """
            用户ID: $userId
            用户名称: $userName
            用户年龄: $userAge
            ID是否有效: $isValidId
            随机数: $randomNumber
            是否为偶数: $isEven
        """.trimIndent()

        Log.d("MainActivity", resultText)

        // 显示在界面上
        findViewById<TextView>(R.id.textResult).text = resultText
    }
}