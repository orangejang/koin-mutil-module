package com.example.processor

import com.example.annotation.KoinModule
import com.example.data.KoinModuleInfo
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
    val moduleName: String
)

class KoinModuleSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    companion object {
        private const val FILE_NAME = "koin-modules.txt"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(KoinModule::class.java.canonicalName)

        symbols.forEach { symbol ->
            if (symbol is KSFunctionDeclaration) {
                val packageName = symbol.packageName.asString()
                val functionName = symbol.simpleName.asString()

                // 提取注解参数
                val koinModuleAnnotation = symbol.annotations.find {
                    it.shortName.asString() == "KoinModule"
                }

                val moduleId = getAnnotationValue(koinModuleAnnotation, "id") ?: functionName
                val moduleName = getAnnotationValue(koinModuleAnnotation, "name") ?: functionName

                logger.info("Found @KoinModule function: $packageName.$functionName (id=$moduleId, name=$moduleName)")
                // 将信息写入共享文件
                writeToSharedFile(packageName, functionName, moduleId, moduleName)
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

    private fun writeToSharedFile(
        packageName: String,
        functionName: String,
        moduleId: String,
        moduleName: String
    ) {
        try {
            val filePath = options["koin.modules.collect.result.path"]
            val sharedDir = File(filePath)
            if (!sharedDir.exists()) {
                sharedDir.mkdirs()
            }

            val sharedFile = File(sharedDir, FILE_NAME)
            val moduleInfo = "$packageName:$functionName:$moduleId:$moduleName"

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
                val filePath = options["koin.modules.collect.result.path"] + "/" + FILE_NAME
                val moduleInfoFile = File(filePath)
                if (moduleInfoFile.exists()) {
                    val allModuleInfo = moduleInfoFile.readLines()

                    for (line in allModuleInfo) {
                        if (line.isNotBlank() && line.contains(":")) {
                            val parts = line.split(":")
                            if (parts.size == 4) {
                                moduleFunctions.add(
                                    ModuleFunctionInfo(
                                        packageName = parts[0],
                                        functionName = parts[1],
                                        moduleId = parts[2],
                                        moduleName = parts[3]
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

    private fun generateKoinModulesClass(moduleFunctions: List<ModuleFunctionInfo>) {
        val packageName = options["koin.modules.package.name"] ?: "com.example.modules"
        val fileName = options["koin.modules.file.name"] ?: "KoinModules"
        val koinModuleInfoClass = KoinModuleInfo::class.java
        
        val fileBuilder = FileSpec.builder(packageName, fileName)
            .addImport(koinModuleInfoClass.packageName, koinModuleInfoClass.simpleName)
            .addImport(Module::class.java.packageName, Module::class.java.simpleName)

        val koinModulesClass = TypeSpec.objectBuilder(fileName)
            .addKdoc("自动生成的Koin模块收集类\n")
            .addKdoc("包含所有被@KoinModule注解标记的模块\n")
            .addKdoc("总共收集了 ${moduleFunctions.size} 个模块\n")

        // 添加getAllModuleInfos方法 - 返回ModuleInfo列表
        val getAllModuleInfosFunc = FunSpec.builder("getAllModuleInfos")
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
            getAllModuleInfosFunc.addStatement("val modules = mutableListOf<KoinModuleInfo>()")

            moduleFunctions.forEach { moduleFunc ->
                // 添加import语句
                fileBuilder.addImport(moduleFunc.packageName, moduleFunc.functionName)

                // 创建ModuleInfo实例
                getAllModuleInfosFunc.addStatement(
                    """
                    try {
                        modules.add(KoinModuleInfo(
                            id = "${moduleFunc.moduleId}",
                            name = "${moduleFunc.moduleName}",
                            module = ${moduleFunc.functionName}()
                        ))
                        println("成功加载Koin模块: ${moduleFunc.packageName}.${moduleFunc.functionName}")
                    } catch (ex: Exception) {
                        println("模块加载失败: ${moduleFunc.packageName}.${moduleFunc.functionName} - " + ex.javaClass.simpleName + ": " + ex.message)
                    }
                """.trimIndent()
                )
            }

            getAllModuleInfosFunc.addStatement("return modules")
        } else {
            getAllModuleInfosFunc.addStatement("return emptyList()")
        }

        koinModulesClass.addFunction(getAllModuleInfosFunc.build())
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

            logger.info("Generated KoinModules.kt with ${moduleFunctions.size} modules returning ModuleInfo")
        } catch (e: Exception) {
            logger.error("Failed to generate KoinModules.kt: ${e.message}")
        }
    }
}

class KoinModuleSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KoinModuleSymbolProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}