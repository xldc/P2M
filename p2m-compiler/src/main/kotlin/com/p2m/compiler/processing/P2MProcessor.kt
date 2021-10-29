package com.p2m.compiler.processing

import com.google.auto.service.AutoService
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.annotation.module.api.*
import com.p2m.compiler.processing.BaseProcessor.Companion.OPTION_MODULE_NAME
import com.p2m.compiler.*
import com.p2m.compiler.bean.*
import com.p2m.compiler.processing.BaseProcessor.Companion.OPTION_DEPENDENCIES
import com.p2m.compiler.utils.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.sun.tools.javac.code.Symbol
import java.io.File
import java.io.Writer
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.StandardLocation


@Suppress("SameParameterValue", "unused")
@KotlinPoetMetadataPreview
@AutoService(Processor::class)
@SupportedAnnotationTypes(
    "com.p2m.annotation.module.api.Launcher",
    "com.p2m.annotation.module.api.Service",
    "com.p2m.annotation.module.api.Event",
    "com.p2m.annotation.module.api.ApiUse",
    "com.p2m.annotation.module.ModuleInitializer"
)
@SupportedOptions(
    OPTION_DEPENDENCIES,
    OPTION_MODULE_NAME,
    "org.gradle.annotation.processing.aggregating"
)
class P2MProcessor : BaseProcessor() {

    companion object {
        private var TAG = "P2MProcessor"
    }

    private var genModuleInitSource = false
    private var exportApiClassPath = mutableListOf<ClassName>()
    private var exportApiSourcePath = mutableListOf<ClassName>()
    
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()){
            // collect classes of the module api scope, final they be compile
            // into jar provide to dependant module.
            collectModuleApiClassesToPropertiesFile()
            return true
        }

        kotlin.runCatching {

            // gen module api
            val moduleApiResult = genModuleApi(roundEnv).also {
                exportApiClassPath.add(it.apiClassName)
            }

            // gen module init
            val moduleInitResult = genModuleInit(roundEnv).also {
                exportApiClassPath.add(it.apiClassName)
            }

            // gen module
            genModule(moduleApiResult, moduleInitResult).also {
                exportApiClassPath.add(it.apiClassName)
            }

            // collect and provide annotated ApiUse classes for dependant module
            collectClassesForAnnotatedApiUse(roundEnv)

        }.apply {
            if (isFailure) {
                val throwable = exceptionOrNull()
                if (throwable is IllegalStateException || throwable is IllegalArgumentException) {
                    mLogger.error(throwable)
                }
            }
        }

        return true
    }

    private fun collectClassesForAnnotatedApiUse(roundEnv: RoundEnvironment) {
        // find classes for annotated ApiUse
        val apiUseElements = roundEnv.getElementsAnnotatedWith(ApiUse::class.java)
        apiUseElements?.forEach { element ->
            val typeElement = element as TypeElement
            exportApiClassPath.add(typeElement.className())
        }

    }

    private fun genModuleApiProperties() = mutableMapOf<String, String>().apply {
        this["genModuleInitSource"] = "$genModuleInitSource"
        this["exportApiClassPath"] = exportApiClassPath.joinToString(",") { className ->
            // com.android.os.Test.InnerClass -> com/android/os/Test
            val prefix = className.packageName.replace(".", File.separator)
            val suffix = className.canonicalName.removePrefix(className.packageName + ".").substringBefore(".")
            prefix + File.separator + suffix
        }

        this["exportApiSourcePath"] = exportApiSourcePath.joinToString(",") { className ->
            // com.android.os.Test.InnerClass -> com/android/os/Test
            val prefix = className.packageName.replace(".", File.separator)
            val suffix = className.canonicalName.removePrefix(className.packageName + ".").substringBefore(".")
            prefix + File.separator + suffix
        }
    }

    private fun genModule(
        moduleApiResult: GenResult,
        moduleInitResult: GenResult
    ): GenResult {
        val ModuleClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_MODULE")
        val apiPackageName = packageNameApi
        val apiFileName = optionModuleName
        val implPackageName = packageNameImpl
        val implFileName = "_${apiFileName}"

        val apiFileSpecBuilder = FileSpec
            .builder(apiPackageName, apiFileName)
            .addFileComment()
        exportApiSourcePath.add(ClassName(apiPackageName, apiFileName))

        val implFileSpecBuilder = FileSpec
            .builder(implPackageName, implFileName)
            .addFileComment()

        return genModuleClassForKotlin(
            moduleApiResult,
            moduleInitResult,
            ModuleClassName,
            apiPackageName,
            apiFileName,
            implPackageName,
            implFileName,
            apiFileSpecBuilder,
            implFileSpecBuilder
        ).apply {
            apiFileSpecBuilder.build().writeTo(mFiler)
            implFileSpecBuilder.build().writeTo(mFiler)
        }
    }

    private fun genModuleClassForKotlin(
        moduleApiResult: GenResult,
        moduleInitResult: GenResult,
        moduleClassName: ClassName,
        apiPackageName: String,
        apiName: String,
        implPackageName: String,
        implName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        implFileSpecBuilder: FileSpec.Builder
    ): GenResult {
        val apiClassName = ClassName(apiPackageName, apiName)
        TypeSpec.classBuilder(apiClassName)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(
                moduleClassName.parameterizedBy(
                    moduleApiResult.apiClassName,
                    moduleInitResult.apiClassName
                )
            )
            .build()
            .run(apiFileSpecBuilder::addType)


        val implClassName = ClassName(implPackageName, implName)
        TypeSpec.classBuilder(implClassName)
            .superclass(apiClassName)
            .addProperty(
                PropertySpec.builder("api", moduleApiResult.apiClassName)
                    .addModifiers(KModifier.OVERRIDE)
                    .delegate("lazy { ${moduleApiResult.getImplInstanceStatement()} }")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("init", moduleInitResult.apiClassName)
                    .addModifiers(KModifier.OVERRIDE)
                    .addModifiers(KModifier.PROTECTED)
                    .delegate("lazy { ${moduleInitResult.getImplInstanceStatement()} }")
                    .build()
            )
            .apply {
                dependencies.forEach{
                    addInitializerBlock(
                        CodeBlock.of(
                            "dependOn(%T::class.java, \"%L\")",
                            ClassName.bestGuess(it.key),
                            it.value
                        )
                    )
                }
            }
            .build()
            .run(implFileSpecBuilder::addType)

        return GenResult(apiClassName, implClassName)
    }

    private fun genModuleInit(roundEnv: RoundEnvironment): GenResult {
        val ModuleInitClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_MODULE_INIT")
        val apiPackageName = packageNameApi
        val apiFileName = "${optionModuleName}ModuleInit"
        val implPackageName = packageNameImpl
        val implFileName = "_${apiFileName}"

        val moduleInitElement = roundEnv.getSingleTypeElementAnnotatedWith(
            mLogger,
            optionModuleName,
            ModuleInitializer::class.java
        ) as? TypeElement

        check(moduleInitElement != null) {
            """
                Must add source code in Module[${optionModuleName}]:
                
                @ModuleInitializer
                class ${optionModuleName}ModuleInit : ModuleInit{

                    override fun onEvaluate(context: Context, taskRegister: TaskRegister<out TaskUnit>) {
                        // Evaluate stage of itself.
                        // Here, You can use [taskRegister] to register some task for help initialize module fast,
                        // and then these tasks will be executed order.
                    }

                    override fun onExecuted(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
                        // Executed stage of itself, indicates will completed initialized of the module.
                        // Called when its all tasks be completed and all dependencies completed initialized.
                        // Here, You can use [taskOutputProvider] to get some output of itself tasks,
                        // also use [moduleApiProvider] to get some dependency module.
                    }
                }
                
                open https://github.com/wangdaqi77/P2M to see more.
            """.trimIndent()
        }

        moduleInitElement.checkKotlinClass()
        val apiFileSpecBuilder = FileSpec
            .builder(apiPackageName, apiFileName)
            .addFileComment()
        exportApiSourcePath.add(ClassName(apiPackageName, apiFileName))

        val implFileSpecBuilder = FileSpec
            .builder(implPackageName, implFileName)
            .addFileComment()

        return genModuleInitClassForKotlin(
            moduleInitElement,
            apiPackageName,
            apiFileName,
            implPackageName,
            implFileName,
            ModuleInitClassName,
            apiFileSpecBuilder,
            implFileSpecBuilder
        ).apply {
            apiFileSpecBuilder.build().writeTo(mFiler)
            implFileSpecBuilder.build().writeTo(mFiler)
            genModuleInitSource = true
        }
    }

    private fun genModuleApi(roundEnv: RoundEnvironment): GenResult {
        // of p2m-core
        val ModuleApiClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_MODULE_API")
        val ModuleLauncherClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_LAUNCHER")
        val ModuleServiceClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_SERVICE")
        val ModuleEventClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_EVENT")
        val EmptyLauncherClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_LAUNCHER_EMPTY")
        val EmptyServiceClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_SERVICE_EMPTY")
        val EmptyEventClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE_MODULE.$CLASS_API_EVENT_EMPTY")

        // for generated
        val apiPackageName = packageNameApi
        val implPackageName = packageNameImpl
        val apiFileName = "${optionModuleName}ModuleApi"
        val implFileName = "_${optionModuleName}ModuleApi"

        // of api kt file, need export
        val apiFileSpecBuilder = FileSpec
            .builder(apiPackageName, apiFileName)
            .addFileComment()
        exportApiSourcePath.add(ClassName(apiPackageName, apiFileName))

        // of impl kt file
        val implFileSpecBuilder = FileSpec
            .builder(implPackageName, implFileName)
            .addFileComment()

        // gen launcher
        val genLauncherResult: GenResult = genLauncherClassForKotlin(
            roundEnv,
            EmptyLauncherClassName,
            ModuleLauncherClassName,
            apiPackageName,
            implPackageName,
            apiFileSpecBuilder,
            implFileSpecBuilder
        )

        // gen service
        val genServiceResult = genServiceClassForKotlin(
            roundEnv,
            EmptyServiceClassName,
            ModuleServiceClassName,
            apiPackageName,
            implPackageName,
            apiFileSpecBuilder,
            implFileSpecBuilder
        )

        // gen event
        val genEventResult = genEventClassForKotlin(
            roundEnv,
            EmptyEventClassName,
            ModuleEventClassName,
            apiPackageName,
            implPackageName,
            apiFileSpecBuilder,
            implFileSpecBuilder
        )

        // gen module api
        return genModuleApiClassForKotlin(
            ModuleApiClassName,
            apiPackageName,
            implPackageName,
            apiFileName,
            genLauncherResult,
            genServiceResult,
            genEventResult,
            apiFileSpecBuilder,
            implFileSpecBuilder
        ).apply {
            // write for api
            apiFileSpecBuilder.build().writeTo(mFiler)
            implFileSpecBuilder.build().writeTo(mFiler)

            // copy source to provide for dependant
            // mLogger.info("apiSrcDir:$apiSrcDir")
            // if (!apiSrcDir.exists()) apiSrcDir.mkdirs()
            // apiFileSpecBuilder.build().writeTo(apiSrcDir)
        }
    }

    private fun collectModuleApiClassesToPropertiesFile() {
        val propertiesFile = mFiler.createResource(StandardLocation.SOURCE_OUTPUT, "",
            FILE_NAME_PROPERTIES
        )
        propertiesFile.openWriter().use(::writeModuleApiClassesProperties)
//        val apiSrcDirPath = apiSrcDir.toPath()
//        Files.createDirectories(apiSrcDirPath)
//        val outputPath = apiSrcDirPath.resolve(FILE_NAME_PROPERTIES)
//        OutputStreamWriter(Files.newOutputStream(outputPath), StandardCharsets.UTF_8).use(::writeModuleApiProperties)
    }

    private fun writeModuleApiClassesProperties(writer: Writer) {
        genModuleApiProperties().forEach { (attr, value) ->
            writer.write("${attr}=${value}\n")
            // mLogger.info("$optionModuleName -> $FILE_NAME_PROPERTIES ${attr}=${value}\n")
        }
    }

    private fun genRealLauncherClassForKotlin(
        roundEnv: RoundEnvironment,
        ModuleLauncherClassName: ClassName,
        launcherPackageName: String,
        realLauncherClassSimpleName: String
    ): Pair<TypeSpec?, MutableMap<String, Element>> {

        val elements = roundEnv.getElementsAnnotatedWith(Launcher::class.java)
        if (elements.isEmpty()) return null to mutableMapOf()

        val activityTm = elementUtils.getTypeElement(CLASS_ACTIVITY).asType()
        val serviceTm = elementUtils.getTypeElement(CLASS_SERVICE).asType()
        val fragmentTm = elementUtils.getTypeElement(CLASS_FRAGMENT).asType()
        val fragmentTmV4 = elementUtils.getTypeElement(CLASS_FRAGMENT_V4).asType()
        val fragmentTmAndroidX = elementUtils.getTypeElement(CLASS_FRAGMENT_ANDROID_X).asType()

        val contextClassName = ClassName.bestGuess(CLASS_CONTEXT)
        val intentClassName = ClassName.bestGuess(CLASS_INTENT)
        val contextParameter = ParameterSpec.builder("context", contextClassName).build()

        val launcherFileSpecBuilder = FileSpec
            .builder(launcherPackageName, realLauncherClassSimpleName)
            .addFileComment()

        val launcherTypeBuilder = TypeSpec
            .classBuilder(realLauncherClassSimpleName)
            .addSuperinterface(ModuleLauncherClassName)
        val elementMap = mutableMapOf<String, Element>()
        for (element in elements) {
            val className = element.className()
            val classSimpleName = element.simpleName
            val funSpec = when {

                typeUtils.isSubtype(element.asType(), activityTm) -> { // Activity
                    /*
                     * fun newActivityIntentOfXX(context: Context): Intent {
                     *     val intent = Intent(context, XXActivity::class.java)
                     *     return intent
                     * }
                     */
                    FunSpec.builder("newActivityIntentOf${classSimpleName}").run {
                        addParameter(contextParameter)
                        returns(intentClassName)
                        addCode(
                            """
                                val intent = %T(context, ${className}::class.java)
                                return intent
                            """.trimIndent(), intentClassName
                        )
                        build()
                    }
                }
                typeUtils.isSubtype(element.asType(), fragmentTm)
                        || typeUtils.isSubtype(element.asType(), fragmentTmV4)
                        || typeUtils.isSubtype(element.asType(), fragmentTmAndroidX) -> {
                    val fragmentClassName = ClassName.bestGuess(
                        when {
                            typeUtils.isSubtype(element.asType(), fragmentTmV4) -> CLASS_FRAGMENT_V4
                            typeUtils.isSubtype(element.asType(), fragmentTmAndroidX) -> CLASS_FRAGMENT_ANDROID_X
                            typeUtils.isSubtype(element.asType(), fragmentTm) -> CLASS_FRAGMENT
                            else -> CLASS_FRAGMENT
                        }
                    )

                    /*
                     * fun newFragmentOfXX(context: Context): Fragment {
                     *     return intent
                     * }
                     */
                    FunSpec.builder("newFragmentOf${classSimpleName}").run {
                        returns(fragmentClassName)
                        addCode(
                            """
                                val fragment = %T()
                                return fragment
                            """.trimIndent(), className
                        )
                        build()
                    }
                }
                typeUtils.isSubtype(element.asType(), serviceTm) -> { // Service
                    /*
                     * fun newServiceIntentOfXX(context: Context): Intent {
                     *     val intent = Intent(context, XXActivity::class.java)
                     *     return intent
                     * }
                     */
                    FunSpec.builder("newServiceIntentOf${classSimpleName}").run {
                        addParameter(contextParameter)
                        returns(intentClassName)
                        addCode(
                            """
                                val intent = %T(context, ${className}::class.java)
                                return intent
                            """.trimIndent(), intentClassName
                        )
                        build()
                    }

                }
                else -> throw IllegalArgumentException("@Launcher not support in ${className.canonicalName}.")
            }
            launcherTypeBuilder.addFunction(funSpec)
            elementMap[funSpec.name] = element
        }

        val launcherType = launcherTypeBuilder.build()
        launcherFileSpecBuilder
            .addType(launcherType)
            .build()
            .writeTo(mFiler)
        return launcherType to elementMap
    }

    private fun genModuleInitClassForKotlin(
        moduleInitElement: TypeElement,
        apiPackageName: String,
        apiName: String,
        implPackageName: String,
        implName: String,
        ModuleInitClassName: ClassName,
        apiFileSpecBuilder: FileSpec.Builder,
        implFileSpecBuilder: FileSpec.Builder
    ): GenResult {

        val moduleInitClassNameOrigin = ClassName(moduleInitElement.packageName(), moduleInitElement.simpleName.toString())

        check(moduleInitElement.interfaces.size != 0) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName}" }
        check(moduleInitElement.interfaces.size == 1) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName} only." }
        check(moduleInitElement.interfaces[0].toString() == ModuleInitClassName.canonicalName) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName}" }

        //接口
        val moduleInitApiClassName = ClassName(apiPackageName, apiName)
        TypeSpec.interfaceBuilder(moduleInitApiClassName)
            .addSuperinterface(ModuleInitClassName)
            .build()
            .run(apiFileSpecBuilder::addType)

        // 服务代理类，代理被注解的类
        val moduleInitImplClassName = ClassName(implPackageName, implName)
        val moduleInitRealRefName = "moduleInitReal"
        TypeSpec.classBuilder(moduleInitImplClassName)
            .addSuperinterface(moduleInitApiClassName)
            .addSuperinterface(ModuleInitClassName, delegate = CodeBlock.of(moduleInitRealRefName))
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(moduleInitRealRefName, ModuleInitClassName)
                            .defaultValue("%T()", moduleInitClassNameOrigin)
                            .build()
                    )
                    .build()
            )
            .build()
            .run(implFileSpecBuilder::addType)

        return GenResult(moduleInitApiClassName, moduleInitImplClassName)
    }

    private fun genModuleApiClassForKotlin(
        ModuleApiClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileName: String,
        genModuleLauncherResult: GenResult,
        genModuleServiceResult: GenResult,
        genModuleEventResult: GenResult,
        moduleInterfaceFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenResult {
        val apiClassName = ClassName(apiPackageName, apiFileName)
        val implClassName = ClassName(implPackageName, "_${apiFileName}")

        val superModuleApiParameterizedTypeName = ModuleApiClassName.parameterizedBy(
            genModuleLauncherResult.apiClassName,
            genModuleServiceResult.apiClassName,
            genModuleEventResult.apiClassName
        )

        val launcherVarName = "launcher"
        val serviceVarName = "service"
        val eventVarName = "event"

        val apiTypeSpecBuilder = TypeSpec
            .interfaceBuilder(apiClassName)
            .addSuperinterface(superModuleApiParameterizedTypeName)
            .addKdoc("A api class of $optionModuleName module.\n")
            .addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>()` to get the instance.\n")
            .addKdoc("\n")
            .addKdoc("@see %T - $launcherVarName, use `P2M.moduleApiOf<$optionModuleName>().$launcherVarName` to get the instance.\n", genModuleLauncherResult.apiClassName)
            .addKdoc("@see %T - $serviceVarName, use `P2M.moduleApiOf<$optionModuleName>().$serviceVarName` to get the instance.\n", genModuleServiceResult.apiClassName)
            .addKdoc("@see %T - $eventVarName, use `P2M.moduleApiOf<$optionModuleName>().$eventVarName` to get the instance.\n", genModuleEventResult.apiClassName)

        val apiTypeSpec = apiTypeSpecBuilder.build()

        val launcherProperty = PropertySpec.builder(
            launcherVarName,
            genModuleLauncherResult.apiClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleLauncherResult.getImplInstanceStatement()} }").build()

        val serviceProperty = PropertySpec.builder(
            serviceVarName,
            genModuleServiceResult.apiClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleServiceResult.getImplInstanceStatement()} }").build()

        val eventProperty = PropertySpec.builder(
            eventVarName,
            genModuleEventResult.apiClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleEventResult.getImplInstanceStatement()} }").build()

        // 模块实现类
        val implTypeSpecBuilder = TypeSpec
            .classBuilder(implClassName)
            .addSuperinterface(apiClassName)
            .addProperty(launcherProperty)
            .addProperty(serviceProperty)
            .addProperty(eventProperty)

        val implTypeSpec = implTypeSpecBuilder.build()

        moduleInterfaceFileSpecBuilder.addType(apiTypeSpec)
        apiImplFileSpecBuilder.addType(implTypeSpec)

        return GenResult(apiClassName, implClassName)
    }

    private fun genLauncherClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyLauncherClassName: ClassName,
        ModuleLauncherClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenResult {
        val launcherPackageName = packageNameImplLauncher
        val apiName = "${optionModuleName}ModuleLauncher"
        val realName = "Real$apiName"
        val realClassName = ClassName(launcherPackageName, realName)

        val result = genRealLauncherClassForKotlin(roundEnv, ModuleLauncherClassName, launcherPackageName, realName)
        val (launcherType, elementMap) = result
        return if (launcherType == null) {
            GenResult(
                EmptyLauncherClassName,
                EmptyLauncherClassName,
                true
            )
        } else {
            genLauncherClassForKotlin(
                ModuleLauncherClassName,
                launcherType,
                realClassName,
                apiName,
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder,
                elementMap
            ).also {
                exportApiClassPath.add(it.apiClassName)
            }
        }

    }

    private fun genLauncherClassForKotlin(
        ModuleLauncherClassName: ClassName,
        launcherType: TypeSpec,
        realClassName: ClassName,
        apiName: String,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder,
        elementMap: MutableMap<String, Element>
    ): GenResult {
        val apiFunSpecs = launcherType
            .funSpecs.map {
                it.toBuilder().run {
                    annotations.clear()
                    addModifiers(KModifier.ABSTRACT)
                    clearBody()
                    elementMap[it.name]?.run {
                        elementUtils.getDocComment(this)?.apply { addKdoc(CodeBlock.of(this)) }
                        addKdoc("\n")
                        addKdoc("@see %T - origin.", this.className())
                    }
                    build()
                }
            }

        // 接口
        val apiClassName = ClassName(apiPackageName, apiName)
        val apiTypeSpec = TypeSpec.interfaceBuilder(apiClassName).run {
            addSuperinterface(ModuleLauncherClassName)
            funSpecs.clear()
            addFunctions(apiFunSpecs)
            build()
        }

        // 服务代理类，代理被注解的类
        val implClassName = ClassName(implPackageName, "_${apiName}")
        val launcherRealRefName = "launcherReal"
        val launcherRealProperty = PropertySpec.builder(
            launcherRealRefName,
            realClassName,
            KModifier.PRIVATE
        ).mutable(false).delegate("lazy() { %T() }", realClassName).build()

        val implFunSpecs = launcherType.funSpecs.map {
            it.toBuilder().run {
                modifiers.remove(KModifier.ABSTRACT)
                addModifiers(KModifier.OVERRIDE)
                clearBody()
                addStatement(
                    "return %L.%L(%L)",
                    launcherRealRefName,
                    it.name,
                    it.parameters.convertRealParamsForKotlin()
                )
                build()
            }
        }
        val implTypeSpec = apiTypeSpec.toBuilder(TypeSpec.Kind.CLASS, name = implClassName.simpleName).run {
            addSuperinterface(apiClassName)
            addProperty(launcherRealProperty)
            funSpecs.clear()
            addFunctions(implFunSpecs)
            build()
        }

        apiFileSpecBuilder.addType(apiTypeSpec.toBuilder().run {
            addKdoc("A launcher class of $optionModuleName module.\n")
            addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().launcher` to get the instance.\n")
            build()
        })
        apiImplFileSpecBuilder.addType(implTypeSpec)
        return GenResult(apiClassName, implClassName)
    }

    private fun genServiceClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyServiceClassName: ClassName,
        ModuleServiceClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenResult {
        val serviceElement = roundEnv.getSingleTypeElementAnnotatedWith(
            mLogger,
            optionModuleName,
            Service::class.java
        ) as? TypeElement
        return if (serviceElement == null) {
            GenResult(
                EmptyServiceClassName,
                EmptyServiceClassName,
                true
            )
        } else {
            serviceElement.checkKotlinClass()

            genServiceClassForKotlin(
                ModuleServiceClassName,
                serviceElement,
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder
            ).also {
                exportApiClassPath.add(it.apiClassName)
            }
        }
    }

    private fun genServiceClassForKotlin(
        ModuleServiceClassName: ClassName,
        serviceElement: TypeElement,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenResult {

        // service类型源
        val serviceTypeSpecOrigin = serviceElement.toTypeSpec().also {
            check(it.kind === TypeSpec.Kind.CLASS) {
                "${serviceElement.qualifiedName} must is a class."
            }
        }

        val serviceClassNameOrigin = serviceElement.className()

        // 服务接口
        val apiName = "${optionModuleName}ModuleService"

        val notSupportedModifier = mutableSetOf(
            Modifier.PRIVATE,
            Modifier.DEFAULT,
            Modifier.PROTECTED,
            Modifier.STATIC,
            Modifier.ABSTRACT
        )

        var constructorCount = 0
        val methodDocMap = mutableMapOf(*(serviceElement.enclosedElements
            .filter { element ->

                // check constructor
                if (element is Symbol.MethodSymbol && element.simpleName.toString() == "<init>") {
                    check (constructorCount++ == 0) {"Not has multi constructor, at ${serviceClassNameOrigin.canonicalName}"}
                    check (element.params.isEmpty()) {"Params of constructor must empty, at ${serviceClassNameOrigin.canonicalName}"}
                }

                // modifiers filter
                element is Symbol.MethodSymbol && element.modifiers.toMutableSet().let{
                    val size = it.size
                    it.removeAll(notSupportedModifier)

                    (size == it.size).also {include-> check(include) { "Not supported modifies of $it at ${element.qualifiedName}." } }
                }
            }
            .map { element ->
                val methodSymbol = element as Symbol.MethodSymbol
                val kDoc = elementUtils.getDocComment(methodSymbol)?.let { CodeBlock.of(it) }
                val sign = methodSymbol.simpleName.toString() + methodSymbol.params.map { it.name }.toString()

                sign to kDoc
            }.toTypedArray())
        )

        val validFunSpecs= serviceTypeSpecOrigin.funSpecs
            .filter { funSpec ->
                val sign = funSpec.name + funSpec.parameters.map { it.name }.toString()
                methodDocMap.containsKey(sign)
            }

        val apiFunSpecs = validFunSpecs.map { funSpec ->
            funSpec.toBuilder().run {
                annotations.clear()
                clearBody().addModifiers(KModifier.ABSTRACT)
                val sign = funSpec.name + funSpec.parameters.map { it.name }.toString()
                val kDoc =  methodDocMap[sign]
                kDoc?.let(::addKdoc)
                addKdoc("\n")
                addKdoc("@see %T.%L - origin.", serviceClassNameOrigin, funSpec.name)
                build()
            }
        }

        val apiClassName = ClassName(apiPackageName, apiName)
        val apiTypeSpec = TypeSpec.interfaceBuilder(apiClassName).run {
            addSuperinterface(ModuleServiceClassName)
            funSpecs.clear()
            addFunctions(apiFunSpecs)
            build()
        }

        // 服务代理类，代理被注解的类
        val implClassName = ClassName(implPackageName, "_${apiName}")
        val serviceRealRefName = "serviceReal"
        val serviceRealProperty = PropertySpec.builder(
            serviceRealRefName,
            serviceClassNameOrigin,
            KModifier.PRIVATE
        ).mutable(false).delegate("lazy() { %T() }", serviceClassNameOrigin).build()

        val implFunSpecs = validFunSpecs.map { funSpec ->
            funSpec.toBuilder().run {
                modifiers.remove(KModifier.ABSTRACT)
                addModifiers(KModifier.OVERRIDE)
                clearBody()
                addStatement(
                    "return %L.%L(%L)",
                    serviceRealRefName,
                    funSpec.name,
                    funSpec.parameters.convertRealParamsForKotlin()
                )
                build()
            }
        }

        val implTypeSpec = apiTypeSpec.toBuilder(TypeSpec.Kind.CLASS, name = implClassName.simpleName).run {
            addSuperinterface(apiClassName)
            addProperty(serviceRealProperty)
            funSpecs.clear()
            addFunctions(implFunSpecs)
            build()
        }

        apiFileSpecBuilder.addType(apiTypeSpec.toBuilder().run {
            addKdoc("A service class of $optionModuleName module.\n")
            addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().service` to get the instance.\n")
            addKdoc("\n")
            addKdoc("@see %T - origin.", serviceClassNameOrigin)
            addKdoc("\n")
            build()
        })

        apiImplFileSpecBuilder.addType(implTypeSpec)
        return GenResult(apiClassName, implClassName)
    }

    private fun genEventClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyEventClassName: ClassName,
        ModuleEventClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenResult {
        val eventElement = roundEnv.getSingleTypeElementAnnotatedWith(
            mLogger,
            optionModuleName,
            Event::class.java
        ) as? TypeElement
        val eventFieldElements = roundEnv.getElementsAnnotatedWith(EventField::class.java)
        val eventFieldMap = mutableMapOf(
            *(eventFieldElements.map { eventFieldElement ->
                //  @Event
                //  public interface ClassName{
                //      public static final class DefaultImpls {
                //          @EventField
                //          public static void eventFieldName$annotations() {
                //          }
                //      }
                //  }
                val eventFieldName = eventFieldElement.simpleName.toString().split("$")[0]
                val interfaceName =
                    eventFieldElement.enclosingElement.enclosingElement.simpleName.toString()
                check(eventFieldElement.enclosingElement?.enclosingElement?.hasAnnotation(Event::class.java) == true) {
                    "${eventFieldElement.simpleName} not use ${EventField::class.java.canonicalName} annotated, because owner $interfaceName interface no use ${Event::class.java.canonicalName} annotated."
                }
                val eventField = eventFieldElement.getAnnotation(EventField::class.java)
                val kdoc = elementUtils.getDocComment(eventFieldElement)?.let { CodeBlock.of(it) }
                eventFieldName to (eventField to kdoc)
            }.toTypedArray())
        )

        return if (eventElement == null) {
            GenResult(
                EmptyEventClassName,
                EmptyEventClassName,
                true
            )
        } else {

            eventElement.checkKotlinClass()

            genEventClassForKotlin(
                ModuleEventClassName,
                eventElement,
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder,
                eventFieldMap
            ).also {
                exportApiClassPath.add(it.apiClassName)
            }
        }
    }

    @Suppress("LocalVariableName")
    private fun genEventClassForKotlin(
        ModuleEventClassName: ClassName,
        eventElement: TypeElement,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder,
        eventFieldMap: MutableMap<String, Pair<EventField, CodeBlock?>>
    ): GenResult {
        // event类型源
        val eventTypeSpecOrigin = eventElement.toTypeSpec().also {
            check(it.kind === TypeSpec.Kind.INTERFACE) {
                "${eventElement.qualifiedName} must is a interface class."
            }
        }

        val LiveEvent = ClassName(PACKAGE_NAME_EVENT, CLASS_LIVE_EVENT)
        val MutableLiveEvent = ClassName(PACKAGE_NAME_EVENT, CLASS_MUTABLE_LIVE_EVENT)
        val BackgroundLiveEvent = ClassName(PACKAGE_NAME_EVENT, CLASS_BACKGROUND_EVENT)
        val MutableBackgroundLiveEvent = ClassName(PACKAGE_NAME_EVENT, CLASS_MUTABLE_BACKGROUND_EVENT)
        val getEventClassName = { eventOn: EventOn, mutableFromExternal: Boolean ->
            when (eventOn) {
                EventOn.MAIN -> if (!mutableFromExternal) LiveEvent else MutableLiveEvent
                EventOn.BACKGROUND -> if (!mutableFromExternal) BackgroundLiveEvent else MutableBackgroundLiveEvent
            }
        }
        val getDelegateOuterClassName = { eventOn: EventOn ->
            when (eventOn) {
                EventOn.MAIN -> LiveEvent
                EventOn.BACKGROUND -> BackgroundLiveEvent
            }
        }

        val eventClassNameOrigin = eventElement.className()
        val apiName = "${optionModuleName}ModuleEvent"
        val apiClassName = ClassName(apiPackageName, apiName)
        val implClassName = ClassName(implPackageName, "_${apiName}")
        val internalMutableImplClassName = ClassName(implPackageName, "_${apiName}_Mutable")

        val eventOriginPropertySpecs = eventTypeSpecOrigin.propertySpecs // 被注解EventField的所有字段
            .filter { eventFieldMap.containsKey(it.name) }
        val apiPropertySpecsBuilders = eventOriginPropertySpecs.map { // 所有的属性builder
            val (eventField, eventDoc) = eventFieldMap[it.name]!!
            val eventClassName = getEventClassName(eventField.eventOn, eventField.mutableFromExternal)
            PropertySpec.builder(it.name, eventClassName.parameterizedBy(it.type)).apply {
                mutable(false)
                annotations.clear()
                eventDoc?.let(::addKdoc)
                addKdoc("\n")
                addKdoc("@see %T.%L - origin.", eventClassNameOrigin, it.name)
                addKdoc("\n")
            }
        }

        // Event接口
        val apiPropertySpecs = apiPropertySpecsBuilders.map { it.build() }
        val apiTypeSpec = TypeSpec.interfaceBuilder(apiClassName).run {
            addSuperinterface(ModuleEventClassName)
            addProperties(apiPropertySpecs)
            build()
        }

        // Event实现类
        val implInternalMutableEventPropertyName = "_mutable"
        val implInternalMutableEventProperty : PropertySpec = PropertySpec.builder(implInternalMutableEventPropertyName, internalMutableImplClassName).run {
            addModifiers(KModifier.INTERNAL)
            mutable(false)
            delegate("lazy() { %T(this) }", internalMutableImplClassName)
            build()
        }

        val implPropertySpecs = eventOriginPropertySpecs.map {
            val (eventField, _) = eventFieldMap[it.name]!!
            val mutableFromExternal = eventField.mutableFromExternal
            val eventClassName = getEventClassName(eventField.eventOn, eventField.mutableFromExternal)
            val delegateOuterClassName = getDelegateOuterClassName(eventField.eventOn)
            val delegateName = if(mutableFromExternal) CLASS_EVENT_MUTABLE_DELEGATE else CLASS_EVENT_DELEGATE
            PropertySpec.builder(it.name, eventClassName.parameterizedBy(it.type)).run {
                mutable(false)
                addModifiers(KModifier.OVERRIDE)
                delegate(
                    CodeBlock
                        .builder()
                        .add("%T.%L()", delegateOuterClassName, delegateName)
                        .build()
                )
                build()
            }

        }

        val implTypeSpec = apiTypeSpec.toBuilder(TypeSpec.Kind.CLASS, implClassName.simpleName).run {
            addSuperinterface(apiClassName)
            propertySpecs.clear()
            addProperty(implInternalMutableEventProperty)
            addProperties(implPropertySpecs)
            build()
        }

        // 模块内部可变的Event实现类
        val implInternalMutableEventType = TypeSpec.classBuilder(
            name = internalMutableImplClassName.simpleName
        ).run {
            addModifiers(KModifier.INTERNAL)

            // 构造
            val srcPropertyRefName = "real"
            primaryConstructor(FunSpec.constructorBuilder().run {
                addParameter(srcPropertyRefName, implClassName)
                build()
            })
            addProperty(
                PropertySpec.builder(srcPropertyRefName, implClassName).run {
                    initializer(srcPropertyRefName)
                    addModifiers(KModifier.PRIVATE)
                    build()
                }
            )
            addProperty(PropertySpec.builder(srcPropertyRefName, implClassName).run {
                addModifiers(KModifier.PRIVATE)
                mutable(false)
                build()
            })


            // 属性
            addProperties(eventOriginPropertySpecs.map {
                val (eventField, _) = eventFieldMap[it.name]!!
                val eventClassName = getEventClassName(eventField.eventOn, true)
                val delegateOuterClassName = getDelegateOuterClassName(eventField.eventOn)
                PropertySpec.builder(it.name, eventClassName.parameterizedBy(it.type)).run {
                    mutable(false)
                    delegate(
                        "%T.$CLASS_EVENT_INTERNAL_MUTABLE_DELEGATE(%L.%L)",
                        delegateOuterClassName,
                        srcPropertyRefName,
                        it.name
                    )
                    build()
                }

            })
            build()
        }

        // 内部拓展函数
        val implInternalMutableExtFun = FunSpec.builder("mutable")
            .addModifiers(KModifier.INTERNAL)
            .receiver(apiClassName)
            .returns(internalMutableImplClassName)
            .addStatement("return (this as %T).%L", implClassName, implInternalMutableEventPropertyName)
            .build()

        apiFileSpecBuilder.addType(apiTypeSpec.toBuilder().run {
            addKdoc("A event class of $optionModuleName module.\n")
            addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().event` to get the instance.\n")
            addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().event.mutable()` to get holder instance of mutable event in internal module.\n")
            addKdoc("\n")
            addKdoc("@see %T - origin.", eventClassNameOrigin)
            build()
        })
        apiImplFileSpecBuilder.addType(implTypeSpec)
        apiImplFileSpecBuilder.addFunction(implInternalMutableExtFun)
        apiImplFileSpecBuilder.addType(implInternalMutableEventType)
        return GenResult(apiClassName, implClassName)
    }
}