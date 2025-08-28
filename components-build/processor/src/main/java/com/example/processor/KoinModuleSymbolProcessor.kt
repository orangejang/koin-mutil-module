package com.example.processor

import com.example.annotation.KoinModule
import com.example.data.IModuleLifecycle
import com.example.data.KoinModuleInfo
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import org.koin.core.module.Module
import java.io.File

/**
 * 模块函数信息数据类
 */
data class ModuleFunctionInfo(
    val packageName: String,
    val functionName: String,
    val moduleId: String,
    val moduleName: String,
    val entryClass: String? = null
)


class KoinModuleSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    companion object {
        private const val MODULES_FILE_NAME = "koin-modules.txt"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("KSP: Starting to process symbols")
        // 处理@KoinModule注解的函数
        val koinModuleSymbols =
            resolver.getSymbolsWithAnnotation(KoinModule::class.java.canonicalName)
        logger.info("KSP: Found ${koinModuleSymbols.toList().size} symbols with @KoinModule annotation")
        koinModuleSymbols.forEach { symbol ->
            if (symbol is KSFunctionDeclaration) {
                val packageName = symbol.packageName.asString()
                val functionName = symbol.simpleName.asString()

                // 提取注解参数
                val koinModuleAnnotation = symbol.annotations.find {
                    it.shortName.asString() == "KoinModule"
                }

                val moduleId = getAnnotationValue(koinModuleAnnotation, "id") ?: functionName
                val moduleName = getAnnotationValue(koinModuleAnnotation, "name") ?: functionName
                val entryClass = getAnnotationClassValue(koinModuleAnnotation, "entry")

                logger.info("Found @KoinModule function: $packageName.$functionName (id=$moduleId, name=$moduleName)")
                logger.info("  EntryClass: $entryClass")

                // 将信息写入共享文件
                writeModuleToSharedFile(packageName, functionName, moduleId, moduleName, entryClass)
            }
        }


        return emptyList()
    }

    /**
     * 从注解中获取参数值
     */
    private fun getAnnotationValue(annotation: KSAnnotation?, paramName: String): String? {
        return annotation?.arguments?.find { it.name?.asString() == paramName }?.value?.toString()
    }

    /**
     * 从注解中获取类参数值
     */
    private fun getAnnotationClassValue(annotation: KSAnnotation?, paramName: String): String? {
        val argument = annotation?.arguments?.find { it.name?.asString() == paramName }
        val value = argument?.value
        return when (value) {
            is KSType -> {
                val declaration = value.declaration
                val qualifiedName = declaration.qualifiedName?.asString()
                // 过滤掉Any类型
                if (qualifiedName == "kotlin.Any") null else qualifiedName
            }

            else -> value?.toString()
        }
    }

    private fun writeModuleToSharedFile(
        packageName: String,
        functionName: String,
        moduleId: String,
        moduleName: String,
        entryClass: String? = null
    ) {
        try {
            val filePath = options["koin.modules.collect.result.path"]
            val sharedDir = File(filePath)
            if (!sharedDir.exists()) {
                sharedDir.mkdirs()
            }

            val sharedFile = File(sharedDir, MODULES_FILE_NAME)
            val moduleInfo = "$packageName:$functionName:$moduleId:$moduleName:${entryClass ?: ""}"

            // 读取现有内容，避免重复
            val existingLines = if (sharedFile.exists()) {
                sharedFile.readLines().toMutableSet()
            } else {
                mutableSetOf()
            }

            existingLines.add(moduleInfo)
            sharedFile.writeText(existingLines.joinToString("\n"))

            logger.info("Written module info to shared file: $moduleInfo")
        } catch (e: Exception) {
            logger.warn("Could not write to shared file: ${e.message}")
        }
    }


    override fun finish() {
        // 检查是否应该生成KoinModules类（只在moduleC中生成）
        val shouldGenerateKoinModules = options["koin.modules.collector"] == "true"

        if (shouldGenerateKoinModules) {
            val moduleFunctions = mutableListOf<ModuleFunctionInfo>()

            // 从共享文件读取所有模块信息
            try {
                val filePath = options["koin.modules.collect.result.path"] + "/" + MODULES_FILE_NAME
                val moduleInfoFile = File(filePath)
                if (moduleInfoFile.exists()) {
                    val allModuleInfo = moduleInfoFile.readLines()

                    for (line in allModuleInfo) {
                        if (line.isNotBlank() && line.contains(":")) {
                            val parts = line.split(":")
                            if (parts.size >= 4) {
                                val entryClass =
                                    if (parts.size > 4 && parts[4].isNotEmpty()) parts[4] else null

                                moduleFunctions.add(
                                    ModuleFunctionInfo(
                                        packageName = parts[0],
                                        functionName = parts[1],
                                        moduleId = parts[2],
                                        moduleName = parts[3],
                                        entryClass = entryClass
                                    )
                                )
                            }
                        }
                    }
                    logger.info("Loaded ${moduleFunctions.size} modules from shared file")
                }
            } catch (e: Exception) {
                logger.warn("Could not read shared module info: ${e.message}")
            }

            // 生成KoinModules类
            generateKoinModulesClass(moduleFunctions)
        } else {
            logger.info("Skipping KoinModules generation (not collector module)")
        }
    }

    private fun generateKoinModulesClass(
        moduleFunctions: List<ModuleFunctionInfo>
    ) {
        val packageName = options["koin.modules.package.name"] ?: "com.example.modules"
        val fileName = options["koin.modules.file.name"] ?: "KoinModules"
        val koinModuleInfoClass = KoinModuleInfo::class.java
        val moduleClass = Module::class.java
        val moduleLifecycleClass = IModuleLifecycle::class.java

        val fileBuilder = FileSpec.builder(packageName, fileName)
            .addImport(koinModuleInfoClass.packageName, koinModuleInfoClass.simpleName)
            .addImport(moduleClass.packageName, moduleClass.simpleName)
            .addImport(moduleLifecycleClass.packageName, moduleLifecycleClass.simpleName)

        val koinModulesClass = TypeSpec.objectBuilder(fileName)
            .addKdoc("自动生成的Koin模块收集类\n")
            .addKdoc("包含所有被@KoinModule注解标记的模块\n")
            .addKdoc("总共收集了 ${moduleFunctions.size} 个模块\n")

        // 添加getAllModuleInfos方法 - 返回ModuleInfo列表
        val getModulesFunc = FunSpec.builder("getModules")
            .returns(
                LIST.parameterizedBy(
                    ClassName(
                        koinModuleInfoClass.packageName,
                        koinModuleInfoClass.simpleName
                    )
                )
            )
            .addKdoc("获取所有Koin模块\n")
            .addKdoc("@return 所有模块的列表\n")

        // 构建代码
        if (moduleFunctions.isNotEmpty()) {
            getModulesFunc.addStatement("val modules = mutableListOf<KoinModuleInfo>()")

            // 处理模块函数
            moduleFunctions.forEach { moduleFunc ->
                // 添加import语句
                fileBuilder.addImport(moduleFunc.packageName, moduleFunc.functionName)

                if (moduleFunc.entryClass != null) {
                    // 解析entry类的包名和类名
                    val lastDotIndex = moduleFunc.entryClass.lastIndexOf('.')
                    if (lastDotIndex > 0) {
                        val entryPackageName = moduleFunc.entryClass.substring(0, lastDotIndex)
                        val entryClassName = moduleFunc.entryClass.substring(lastDotIndex + 1)

                        // 添加entry类的import
                        fileBuilder.addImport(entryPackageName, entryClassName)

                        // 创建带生命周期的ModuleInfo实例
                        getModulesFunc.addStatement(getStatement(moduleFunc, entryClassName))
                    } else {
                        // entry类名格式不正确，创建不带生命周期的实例
                        getModulesFunc.addStatement(getStatement(moduleFunc, null))
                    }
                } else {
                    // 创建不带生命周期的ModuleInfo实例
                    getModulesFunc.addStatement(getStatement(moduleFunc, null))
                }
            }

            getModulesFunc.addStatement("return modules")
        } else {
            getModulesFunc.addStatement("return emptyList()")
        }

        koinModulesClass.addFunction(getModulesFunc.build())
        fileBuilder.addType(koinModulesClass.build())

        // 写入文件
        try {
            val dependencies = Dependencies(false)

            val file = codeGenerator.createNewFile(
                dependencies,
                packageName,
                fileName
            )

            file.use { outputStream ->
                outputStream.writer().use { writer ->
                    fileBuilder.build().writeTo(writer)
                }
            }

            logger.info("Generated KoinModules.kt with ${moduleFunctions.size} modules")
        } catch (e: Exception) {
            logger.error("Failed to generate KoinModules.kt: ${e.message}")
        }
    }

    private fun getStatement(moduleFunc: ModuleFunctionInfo, entryClassName: String?): String {
        val lifecycle = entryClassName?.let { "$it()" } ?: "null"
        return """
                            try {
                                modules.add(KoinModuleInfo(
                                    id = "${moduleFunc.moduleId}",
                                    name = "${moduleFunc.moduleName}",
                                    module = ${moduleFunc.functionName}(),
                                    lifecycle = $lifecycle
                                ))
                                println("成功加载Koin模块: ${moduleFunc.packageName}.${moduleFunc.functionName} (带生命周期管理)")
                            } catch (ex: Exception) {
                                println("模块加载失败: ${moduleFunc.packageName}.${moduleFunc.functionName} - " + ex.javaClass.simpleName + ": " + ex.message)
                            }
                            """.trimIndent()
    }
}

