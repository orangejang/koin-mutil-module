package com.example.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.module.a.api.IUserService
import com.example.module.b.api.INumberService
import com.example.module.core.impl.modules.ModulesManager
import com.example.module.core.impl.modules.data.ModuleInfo
import com.example.module.core.impl.modules.data.OperationType
import org.koin.android.ext.android.get

class MainActivity : AppCompatActivity() {

    // ModuleManager相关
    private lateinit var loadButton: Button
    private lateinit var unloadButton: Button
    private lateinit var callButton: Button
    private lateinit var textView: TextView

    private val logBuilder = StringBuilder()
    private val moduleManager = ModulesManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initModuleManager()
    }

    private fun initViews() {
        textView = findViewById(R.id.textResult)
        loadButton = findViewById(R.id.btn_load_module)
        unloadButton = findViewById(R.id.btn_unload_module)
        callButton = findViewById(R.id.btn_call_module)

        loadButton.setOnClickListener {
            loadModule()
        }

        unloadButton.setOnClickListener {
            unloadModule()
        }

        callButton.setOnClickListener {
            showOriginalContent()
        }
    }

    private fun initModuleManager() {
        // 添加监听器
        moduleManager.addListener(object : ModulesManager.IModuleManagerListener {
            override fun onModuleLoaded(moduleInfo: ModuleInfo) {
                runOnUiThread {
                    addLog("✅ 模块加载成功: ${moduleInfo.name} (ID: ${moduleInfo.id})")
                }
            }

            override fun onModuleUnloaded(moduleInfo: ModuleInfo) {
                runOnUiThread {
                    addLog("❌ 模块卸载成功: ${moduleInfo.name} (ID: ${moduleInfo.id})")
                }
            }

            override fun onModuleOperationFailed(
                moduleId: String,
                operationType: OperationType,
                error: Throwable
            ) {
                runOnUiThread {
                    addLog("⚠️ 模块操作失败: $moduleId")
                    addLog("   操作: $operationType")
                    addLog("   错误: ${error.message}")
                }
            }
        })
    }

    private fun showOriginalContent() {
        try {
            // 使用UserService（来自moduleA）
            val userService: IUserService? = get<IUserService>()
            if (userService == null) {
                addLog("⚠️ 无法获取 IUserService，确保 moduleA 已加载")
                return
            }
            val userId = userService.getUserId()
            val userAge = userService.generateUserAge()
            val isValidId = userService.isValidUserId(userId)
            val userName = userService.getUserName()

            // 使用NumberService（来自moduleB）
            val numberService: INumberService = get<INumberService>()
            val randomNumber = numberService.generateRandomNumber(1, 100)
            val isEven = numberService.isEven(randomNumber)

            // 输出结果
            val resultText = """
            [表现层 - ModuleD]
            用户ID: $userId
            用户名称: $userName
            用户年龄: $userAge
            ID是否有效: $isValidId
            随机数: $randomNumber
            是否为偶数: $isEven
        """.trimIndent()

            Log.d("PresentationLayer", resultText)

            // 显示在界面上
            addLog(resultText)
        } catch (e: Throwable) {
            addLog("❗ 调用服务失败: ${e.message}")
        }
    }

    private fun loadModule() {
        addLog("准备加载 modules...")
        // 加载模块
        moduleManager.loadModule("moduleA")
        moduleManager.loadModule("moduleB")
        moduleManager.loadModule("moduleC")
    }

    private fun unloadModule() {
        addLog("准备卸载 modules...")

        // 卸载模块
        moduleManager.unloadModule("moduleA")
        moduleManager.unloadModule("moduleB")
        moduleManager.unloadModule("moduleC")
    }

    private fun addLog(message: String) {
        logBuilder.append(message).append("\n")
        textView.text = logBuilder.toString()

        // 自动滚动到底部
        textView.post {
            val scrollAmount = textView.layout?.getLineTop(textView.lineCount) ?: 0
            if (scrollAmount > textView.height) {
                textView.scrollTo(0, scrollAmount - textView.height)
            }
        }
    }
}
