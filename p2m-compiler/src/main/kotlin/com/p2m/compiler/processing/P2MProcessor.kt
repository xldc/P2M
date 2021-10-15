package com.p2m.compiler.processing

import com.google.auto.service.AutoService
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.annotation.module.api.*
import com.p2m.compiler.processing.BaseProcessor.Companion.OPTION_MODULE_NAME
import com.p2m.compiler.*
import com.p2m.compiler.bean.GenModuleApiResult
import com.p2m.compiler.bean.GenModuleEventResult
import com.p2m.compiler.bean.GenModuleLauncherResult
import com.p2m.compiler.bean.GenModuleResult
import com.p2m.compiler.bean.GenModuleServiceResult
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
    OPTION_MODULE_NAME,
    "org.gradle.annotation.processing.aggregating"
)
class P2MProcessor : BaseProcessor() {

    companion object {
        private var TAG = "P2MProcessor"
    }

    private var genApiSource = false
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

            // gen and provide module api classes for dependant module
            genApiAndImpl(roundEnv)

            // gen module initialization env
            genInitializationEnvForModule(roundEnv)

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
        this["genApiSource"] = "$genApiSource"
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
    
    private fun genInitializationEnvForModule(roundEnv: RoundEnvironment) {
        val ImplPackageName = PACKAGE_NAME_IMPL
        val ModuleInitClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_MODULE_INIT")
        val moduleImplFileName = "_${optionModuleName}ModuleInit"

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

                    override fun onEvaluate(taskRegister: TaskRegister) {
                        // Evaluate stage of itself.
                        // Here, You can use [taskRegister] to register some task for help initialize module fast,
                        // and then these tasks will be executed order.
                    }

                    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
                        // Executed stage of itself, indicates will completed initialized of the module.
                        // Called when its all tasks be completed and all dependencies completed initialized.
                        // Here, You can use [taskOutputProvider] to get some output of itself tasks,
                        // also use [moduleProvider] to get some dependency module.
                    }
                }
                
                open https://github.com/wangdaqi77/P2M to see more.
            """.trimIndent()
        }

        moduleInitElement.checkKotlinClass()
        val implFileSpecBuilder = FileSpec
            .builder(ImplPackageName, moduleImplFileName)
            .addFileComment()

        this.genModuleInitClassForKotlin(
            moduleInitElement,
            ImplPackageName,
            ModuleInitClassName,
            implFileSpecBuilder
        )
        implFileSpecBuilder.build().writeTo(mFiler)
        genModuleInitSource = true
    }

    private fun genApiAndImpl(roundEnv: RoundEnvironment) {
        // of p2m-core
        val ModuleApiClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_MODULE_API")
        val ModuleLauncherClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_LAUNCHER")
        val ModuleServiceClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_SERVICE")
        val ModuleEventClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_EVENT")
        val EmptyLauncherClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_LAUNCHER_EMPTY")
        val EmptyServiceClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_SERVICE_EMPTY")
        val EmptyEventClassName = ClassName.bestGuess("$PACKAGE_NAME_CORE.module.$CLASS_API_EVENT_EMPTY")

        // for generated
        val apiPackageName = PACKAGE_NAME_API
        val implPackageName = PACKAGE_NAME_IMPL
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
        val genLauncherResult: GenModuleLauncherResult = genLauncherClassForKotlin(
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

        // gen api
        genApiClassForKotlin(
            ModuleApiClassName,
            apiPackageName,
            implPackageName,
            optionModuleName,
            genLauncherResult,
            genServiceResult,
            genEventResult,
            apiFileSpecBuilder,
            implFileSpecBuilder
        )

        // write for api
        apiFileSpecBuilder.build().writeTo(mFiler)
        implFileSpecBuilder.build().writeTo(mFiler)

        // copy source to provide for dependant
        // mLogger.info("apiSrcDir:$apiSrcDir")
        // if (!apiSrcDir.exists()) apiSrcDir.mkdirs()
        // apiFileSpecBuilder.build().writeTo(apiSrcDir)
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

    private fun genLauncherManagerClassForKotlin(
        roundEnv: RoundEnvironment,
        ModuleLauncherClassName: ClassName,
        launcherPackageName: String,
        launcherClassSimpleName: String
    ): Pair<TypeSpec?, MutableList<CodeBlock>> {

        val kdocsOfLauncherInterface = mutableListOf<CodeBlock>()
        val elements = roundEnv.getElementsAnnotatedWith(Launcher::class.java)
        if (elements.isEmpty()) return null to kdocsOfLauncherInterface

        val activityTm = elementUtils.getTypeElement(CLASS_ACTIVITY).asType()
        val serviceTm = elementUtils.getTypeElement(CLASS_SERVICE).asType()
        val fragmentTm = elementUtils.getTypeElement(CLASS_FRAGMENT).asType()
        val fragmentTmV4 = elementUtils.getTypeElement(CLASS_FRAGMENT_V4).asType()
        val fragmentTmAndroidX = elementUtils.getTypeElement(CLASS_FRAGMENT_ANDROID_X).asType()

        val contextClassName = ClassName.bestGuess(CLASS_CONTEXT)
        val intentClassName = ClassName.bestGuess(CLASS_INTENT)
        val contextParameter = ParameterSpec.builder("context", contextClassName).build()

        val launcherFileSpecBuilder = FileSpec
            .builder(launcherPackageName, launcherClassSimpleName)
            .addFileComment()

        val launcherTypeBuilder = TypeSpec
            .classBuilder(launcherClassSimpleName)
            .addSuperinterface(ModuleLauncherClassName)

        for (element in elements) {
            val className = element.className()
            val classSimpleName = element.simpleName
            val kDoc = elementUtils.getDocComment(element)?.let { CodeBlock.of(it) }
            val funSpec = when {

                typeUtils.isSubtype(element.asType(), activityTm) -> { // Activity
                    /*
                     * fun newActivityIntentOfXX(context: Context): Intent {
                     *     val intent = Intent(context, XXActivity::class.java)
                     *     return intent
                     * }
                     */
                    FunSpec
                        .builder("newActivityIntentOf${classSimpleName}")
                        .addParameter(contextParameter)
                        .returns(intentClassName)
                        .addCode(
                            """
                                val intent = %T(context, ${className}::class.java)
                                return intent
                            """.trimIndent()
                        , intentClassName)
                        .apply { kDoc?.let(::addKdoc) }
                        .build()
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
                    FunSpec
                        .builder("newFragmentOf${classSimpleName}")
                        .returns(fragmentClassName)
                        .addCode(
                            """
                                val fragment = %T()
                                return fragment
                            """.trimIndent()
                        , className)
                        .apply { kDoc?.let(::addKdoc) }
                        .build()

                }
                typeUtils.isSubtype(element.asType(), serviceTm) -> { // Service
                    /*
                     * fun newServiceIntentOfXX(context: Context): Intent {
                     *     val intent = Intent(context, XXActivity::class.java)
                     *     return intent
                     * }
                     */
                    FunSpec
                        .builder("newServiceIntentOf${classSimpleName}")
                        .addParameter(contextParameter)
                        .returns(intentClassName)
                        .addCode(
                            """
                                val intent = %T(context, ${className}::class.java)
                                return intent
                            """.trimIndent()
                        , intentClassName)
                        .apply { kDoc?.let(::addKdoc) }
                        .build()
                }
                else -> throw IllegalArgumentException("@Launcher not support in ${className.canonicalName}.")
            }
            launcherTypeBuilder.addFunction(funSpec)
            kdocsOfLauncherInterface.add(CodeBlock.of("Use [${funSpec.name}] to launch [%T].\n", className))
        }

        val launcherType = launcherTypeBuilder.build()
        launcherFileSpecBuilder
            .addType(launcherType)
            .build()
            .writeTo(mFiler)
        return launcherType to kdocsOfLauncherInterface
    }

    private fun genModuleInitClassForKotlin(
        moduleInitElement: TypeElement,
        implPackageName: String,
        ModuleInitClassName: ClassName,
        implFileSpecBuilder: FileSpec.Builder
    ): GenModuleResult {

        val moduleInitClassNameOrigin = ClassName(moduleInitElement.packageName(), moduleInitElement.simpleName.toString())

        check(moduleInitElement.interfaces.size != 0) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName}" }
        check(moduleInitElement.interfaces.size == 1) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName} only." }
        check(moduleInitElement.interfaces[0].toString() == ModuleInitClassName.canonicalName) { "${moduleInitElement.qualifiedName} must extends ${ModuleInitClassName.canonicalName}" }

        // 服务代理类，代理被注解的类
        val moduleInitImplClassName = ClassName(implPackageName, "_${optionModuleName}ModuleInit")
        val moduleInitRealRefName = "moduleInitReal"
        val moduleInitImplTypeSpecBuilder = TypeSpec.classBuilder(moduleInitImplClassName)
        moduleInitImplTypeSpecBuilder.addSuperinterface(ModuleInitClassName, delegate = CodeBlock.of(moduleInitRealRefName) )
        moduleInitImplTypeSpecBuilder.primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec.builder(moduleInitRealRefName, ModuleInitClassName)
                        .defaultValue("%T()", moduleInitClassNameOrigin)
                        .build()
                )
                .build()
        )

        val moduleImplTypeSpec = moduleInitImplTypeSpecBuilder.build()

        implFileSpecBuilder.addType(moduleImplTypeSpec)

        return GenModuleResult(moduleInitImplClassName)
    }

    private fun genApiClassForKotlin(
        ModuleApiClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        moduleApiInterfaceName: String,
        genModuleLauncherResult: GenModuleLauncherResult,
        genModuleServiceResult: GenModuleServiceResult,
        genModuleEventResult: GenModuleEventResult,
        moduleInterfaceFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenModuleApiResult {

        // 模块类名
        val moduleApiInterfaceClassName = ClassName(apiPackageName, moduleApiInterfaceName)
        val moduleApiImplClassName = ClassName(implPackageName, "_${moduleApiInterfaceName}")

        // 参数化后的模块接口类型
        val superModuleParameterizedTypeName = ModuleApiClassName.parameterizedBy(
            genModuleLauncherResult.launcherInterfaceClassName,
            genModuleServiceResult.serviceInterfaceClassName,
            genModuleEventResult.eventInterfaceClassName
        )

        val launcherVarName = "launcher"
        val serviceVarName = "service"
        val eventVarName = "event"

        // 模块基类
        val moduleAbsTypeSpecBuilder = TypeSpec
            .interfaceBuilder(moduleApiInterfaceClassName)
            .addSuperinterface(superModuleParameterizedTypeName)
            .addKdoc("A api class of $optionModuleName module.\n")
            .addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>()` to get the instance.\n")
            .addKdoc("\n")
            .addKdoc("@see %T - $launcherVarName, use `P2M.moduleApiOf<$optionModuleName>().$launcherVarName` to get the instance.\n", genModuleLauncherResult.launcherInterfaceClassName)
            .addKdoc("@see %T - $serviceVarName, use `P2M.moduleApiOf<$optionModuleName>().$serviceVarName` to get the instance.\n", genModuleServiceResult.serviceInterfaceClassName)
            .addKdoc("@see %T - $eventVarName, use `P2M.moduleApiOf<$optionModuleName>().$eventVarName` to get the instance.\n", genModuleEventResult.eventInterfaceClassName)

        val moduleAbsTypeSpec = moduleAbsTypeSpecBuilder.build()

        val launcherProperty = PropertySpec.builder(
            launcherVarName,
            genModuleLauncherResult.launcherInterfaceClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleLauncherResult.getImplInstanceStatement()} }").build()

        val serviceProperty = PropertySpec.builder(
            serviceVarName,
            genModuleServiceResult.serviceInterfaceClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleServiceResult.getImplInstanceStatement()} }").build()

        val eventProperty = PropertySpec.builder(
            eventVarName,
            genModuleEventResult.eventInterfaceClassName,
            KModifier.OVERRIDE
        ).mutable(false).delegate("lazy() { ${genModuleEventResult.getImplInstanceStatement()} }").build()

        // 模块实现类
        val apiImplTypeSpecBuilder = TypeSpec
            .classBuilder(moduleApiImplClassName)
            .addSuperinterface(moduleApiInterfaceClassName)
            .addProperty(launcherProperty)
            .addProperty(serviceProperty)
            .addProperty(eventProperty)

        val apiImplTypeSpec = apiImplTypeSpecBuilder.build()

        moduleInterfaceFileSpecBuilder.addType(moduleAbsTypeSpec)
        apiImplFileSpecBuilder.addType(apiImplTypeSpec)

        genApiSource = true
        return GenModuleApiResult(
            moduleApiInterfaceClassName,
            moduleApiImplClassName
        ).also {
            exportApiClassPath.add(it.moduleApiClassName)
        }
    }

    private fun genLauncherClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyLauncherClassName: ClassName,
        ModuleLauncherClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenModuleLauncherResult {
        val launcherPackageName = PACKAGE_NAME_IMPL_LAUNCHER
        val launcherInterfaceSimpleName = "${optionModuleName}ModuleLauncher"
        val launcherClassSimpleName = "Real$launcherInterfaceSimpleName"

        val result = genLauncherManagerClassForKotlin(roundEnv, ModuleLauncherClassName, launcherPackageName, launcherClassSimpleName)
        val launcherType = result.first
        val kdocsOfLauncherInterface = result.second
        return if (launcherType == null) {

            GenModuleLauncherResult(
                EmptyLauncherClassName,
                EmptyLauncherClassName,
                true
            )
        } else {
            genLauncherClassForKotlin(
                ModuleLauncherClassName,
                launcherType,
                launcherInterfaceSimpleName,
                ClassName(launcherPackageName, launcherClassSimpleName),
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder,
                kdocsOfLauncherInterface
            ).also {
                exportApiClassPath.add(it.launcherInterfaceClassName)
            }
        }

    }

    private fun genLauncherClassForKotlin(
        ModuleLauncherClassName: ClassName,
        launcherType: TypeSpec,
        launcherInterfaceSimpleName: String,
        launcherClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder,
        kdocsOfLauncherInterface: MutableList<CodeBlock>
    ): GenModuleLauncherResult {

        // 接口
        val launcherInterfaceClassName = ClassName(apiPackageName, launcherInterfaceSimpleName)
        val launcherInterfaceTypeSpecBuilder = TypeSpec.interfaceBuilder(launcherInterfaceClassName)
            .addSuperinterface(ModuleLauncherClassName)
            .addKdoc("A launcher class of $optionModuleName module.\n")
            .addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().launcher` to get the instance.\n")
            .addKdoc("\n")
        kdocsOfLauncherInterface.forEach { launcherInterfaceTypeSpecBuilder.addKdoc(it) }

        val launcherInterfaceFunSpecs = launcherType
            .funSpecs.map {
                val funSpecBuilder = it.toBuilder()
                funSpecBuilder.annotations.clear()
                funSpecBuilder.clearBody().addModifiers(KModifier.ABSTRACT)
                funSpecBuilder.build()
            }

        launcherInterfaceTypeSpecBuilder.funSpecs.clear()
        launcherInterfaceTypeSpecBuilder.addFunctions(launcherInterfaceFunSpecs)
        val launcherInterfaceTypeSpec = launcherInterfaceTypeSpecBuilder.build()

        // 服务代理类，代理被注解的类
        val launcherImplClassName = ClassName(implPackageName, "_${launcherInterfaceSimpleName}")

        // real属性
        val launcherRealRefName = "launcherReal"
        val launcherRealProperty = PropertySpec.builder(
            launcherRealRefName,
            launcherClassName,
            KModifier.PRIVATE
        ).mutable(false).delegate("lazy() { %T() }", launcherClassName).build()

        val launcherImplTypeSpecBuilder = launcherInterfaceTypeSpec.toBuilder(
            TypeSpec.Kind.CLASS,
            name = launcherImplClassName.simpleName
        )
        launcherImplTypeSpecBuilder.addSuperinterface(launcherInterfaceClassName)
        launcherImplTypeSpecBuilder.addProperty(launcherRealProperty)
        val launcherImplFunSpecs = launcherType.funSpecs.map {
            val funSpecBuilder = it.toBuilder()
            funSpecBuilder.modifiers.remove(KModifier.ABSTRACT)
            funSpecBuilder.addModifiers(KModifier.OVERRIDE)
            funSpecBuilder.clearBody()
            funSpecBuilder.addStatement(
                "return %L.%L(%L)",
                launcherRealRefName,
                it.name,
                it.parameters.convertRealParamsForKotlin()
            )
            funSpecBuilder.build()
        }

        launcherImplTypeSpecBuilder.funSpecs.clear()
        launcherImplTypeSpecBuilder.addFunctions(launcherImplFunSpecs)
        val launcherImplTypeSpec = launcherImplTypeSpecBuilder.build()

        apiFileSpecBuilder.addType(launcherInterfaceTypeSpec)
        apiImplFileSpecBuilder.addType(launcherImplTypeSpec)
        return GenModuleLauncherResult(
            launcherInterfaceClassName,
            launcherImplClassName
        )
    }

    private fun genServiceClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyServiceClassName: ClassName,
        ModuleServiceClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenModuleServiceResult {

        val serviceInterfaceSimpleName = "${optionModuleName}ModuleService"

        val serviceElement = roundEnv.getSingleTypeElementAnnotatedWith(
            mLogger,
            optionModuleName,
            Service::class.java
        ) as? TypeElement
        return if (serviceElement == null) {
            GenModuleServiceResult(
                EmptyServiceClassName,
                EmptyServiceClassName,
                true
            )
        } else {
            serviceElement.checkKotlinClass()

            genServiceClassForKotlin(
                ModuleServiceClassName,
                serviceInterfaceSimpleName,
                serviceElement,
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder
            ).also {
                exportApiClassPath.add(it.serviceInterfaceClassName)
            }
        }
    }

    private fun genServiceClassForKotlin(
        ModuleServiceClassName: ClassName,
        serviceInterfaceSimpleName: String,
        serviceElement: TypeElement,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenModuleServiceResult {

        // service类型源
        val serviceTypeSpecOrigin = serviceElement.toTypeSpec().also {
            check(it.kind === TypeSpec.Kind.CLASS) {
                "${serviceElement.qualifiedName} must is a class."
            }
        }

        val serviceClassNameOrigin = serviceElement.className()

        // 服务接口
        val serviceInterfaceClassName = ClassName(apiPackageName, serviceInterfaceSimpleName)
        val serviceInterfaceTypeSpecBuilder = TypeSpec.interfaceBuilder(serviceInterfaceClassName)
            .addSuperinterface(ModuleServiceClassName)
            .addKdoc("A service class of $optionModuleName module.\n")
            .addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().service` to get the instance.\n")
            .addKdoc("\n")
            .addKdoc("@see %T - origin.", serviceClassNameOrigin)
            .addKdoc("\n")

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

        val serviceInterfaceFunSpecs = validFunSpecs.map { funSpec ->
                funSpec.toBuilder().apply {
                    annotations.clear()
                    clearBody().addModifiers(KModifier.ABSTRACT)
                    val sign = funSpec.name + funSpec.parameters.map { it.name }.toString()
                   val kDoc =  methodDocMap[sign]
                    kDoc?.let(::addKdoc)
                }.build()
            }

        serviceInterfaceTypeSpecBuilder.funSpecs.clear()
        serviceInterfaceTypeSpecBuilder.addFunctions(serviceInterfaceFunSpecs)
        val serviceInterfaceTypeSpec = serviceInterfaceTypeSpecBuilder.build()

        // 服务代理类，代理被注解的类
        val serviceImplClassName = ClassName(implPackageName, "_${serviceInterfaceSimpleName}")

        // real属性
        val serviceRealRefName = "serviceReal"
        // private val serviceReal : XXService = XXService()
        val serviceRealProperty = PropertySpec.builder(
            serviceRealRefName,
            serviceClassNameOrigin,
            KModifier.PRIVATE
        ).mutable(false).delegate("lazy() { %T() }", serviceClassNameOrigin).build()

        val serviceImplTypeSpecBuilder = serviceInterfaceTypeSpec.toBuilder(
            TypeSpec.Kind.CLASS,
            name = serviceImplClassName.simpleName
        )
        serviceImplTypeSpecBuilder.addSuperinterface(serviceInterfaceClassName)
        serviceImplTypeSpecBuilder.addProperty(serviceRealProperty)
        val serviceImplFunSpecs = validFunSpecs.map { funSpec ->
            val funSpecBuilder = funSpec.toBuilder()
            funSpecBuilder.modifiers.remove(KModifier.ABSTRACT)
            funSpecBuilder.addModifiers(KModifier.OVERRIDE)
            funSpecBuilder.clearBody()
            funSpecBuilder.addStatement(
                "return %L.%L(%L)",
                serviceRealRefName,
                funSpec.name,
                funSpec.parameters.convertRealParamsForKotlin()
            )
            val sign = funSpec.name + funSpec.parameters.map { it.name }.toString()
            val kDoc =  methodDocMap[sign]
            kDoc?.let(funSpecBuilder::addKdoc)
            funSpecBuilder.build()
        }

        serviceImplTypeSpecBuilder.funSpecs.clear()
        serviceImplTypeSpecBuilder.addFunctions(serviceImplFunSpecs)
        val serviceImplTypeSpec = serviceImplTypeSpecBuilder.build()

        apiFileSpecBuilder.addType(serviceInterfaceTypeSpec)
        apiImplFileSpecBuilder.addType(serviceImplTypeSpec)
        return GenModuleServiceResult(
            serviceInterfaceClassName,
            serviceImplClassName
        )
    }

    private fun genEventClassForKotlin(
        roundEnv: RoundEnvironment,
        EmptyEventClassName: ClassName,
        ModuleEventClassName: ClassName,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder
    ): GenModuleEventResult {
        val eventElement = roundEnv.getSingleTypeElementAnnotatedWith(
            mLogger,
            optionModuleName,
            Event::class.java
        ) as? TypeElement
        val eventInterfaceSimpleName = "${optionModuleName}ModuleEvent"
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
                eventFieldName to (eventFieldElement.getAnnotation(EventField::class.java) to elementUtils.getDocComment(eventFieldElement)?.let { CodeBlock.of(it) }
                )
            }.toTypedArray())
        )

        return if (eventElement == null) {
            GenModuleEventResult(
                EmptyEventClassName,
                EmptyEventClassName,
                true
            )
        } else {

            eventElement.checkKotlinClass()

            genEventClassForKotlin(
                ModuleEventClassName,
                eventInterfaceSimpleName,
                eventElement,
                apiPackageName,
                implPackageName,
                apiFileSpecBuilder,
                apiImplFileSpecBuilder,
                eventFieldMap
            ).also {
                exportApiClassPath.add(it.eventInterfaceClassName)
            }
        }
    }

    private fun genEventClassForKotlin(
        ModuleEventClassName: ClassName,
        eventInterfaceSimpleName: String,
        eventElement: TypeElement,
        apiPackageName: String,
        implPackageName: String,
        apiFileSpecBuilder: FileSpec.Builder,
        apiImplFileSpecBuilder: FileSpec.Builder,
        eventFieldMap: MutableMap<String, Pair<EventField, CodeBlock?>>
    ): GenModuleEventResult {
        // event类型源
        val eventTypeSpecOrigin = eventElement.toTypeSpec().also {
            check(it.kind === TypeSpec.Kind.INTERFACE) {
                "${eventElement.qualifiedName} must is a class."
            }
        }
        val eventClassNameOrigin = eventElement.className()

        // Event接口
        val eventInterfaceClassName = ClassName(apiPackageName, eventInterfaceSimpleName)
        val eventInterfaceTypeSpecBuilder = TypeSpec.interfaceBuilder(eventInterfaceClassName)
            .addSuperinterface(ModuleEventClassName)
            .addKdoc("A event class of $optionModuleName module.\n")
            .addKdoc("Use `P2M.moduleApiOf<${optionModuleName}>().event` to get the instance.\n")
            .addKdoc("\n")
            .addKdoc("@see %T - origin.", eventClassNameOrigin)
            .addKdoc("\n")

        val eventOriginPropertySpecs = eventTypeSpecOrigin.propertySpecs
            .filter { eventFieldMap.containsKey(it.name) }

        val eventClassNames = mutableMapOf<String, ClassName>()
        val eventInterfacePropertySpecs = eventOriginPropertySpecs
            .filter { eventFieldMap.containsKey(it.name) }.map {
            val eventField = eventFieldMap[it.name]?.first
            val eventDoc = eventFieldMap[it.name]?.second
            val eventOn = eventField?.eventOn ?: EventOn.MAIN
            val eventClassName = when(eventOn) {
                EventOn.MAIN-> ClassName(PACKAGE_NAME_EVENT, CLASS_LIVE_EVENT)
                EventOn.BACKGROUND-> ClassName(PACKAGE_NAME_EVENT, CLASS_BACKGROUND_EVENT)
            }
            eventClassNames[it.name] = eventClassName
            val propertySpecBuilder = PropertySpec.builder(
                it.name, eventClassName.parameterizedBy(it.type)
            )

            propertySpecBuilder.mutable(false)
            propertySpecBuilder.annotations.clear()
            propertySpecBuilder.apply { eventDoc?.let(::addKdoc) }
            propertySpecBuilder.build()
        }

        eventInterfaceTypeSpecBuilder.propertySpecs.clear()
        eventInterfaceTypeSpecBuilder.addProperties(eventInterfacePropertySpecs)
        val eventInterfaceTypeSpec = eventInterfaceTypeSpecBuilder.build()


        // Event代理类，代理被注解的类
        val eventImplClassName = ClassName(implPackageName, "_${eventInterfaceSimpleName}")
        val internalMutableEventImplClassName = ClassName(implPackageName, "_${eventInterfaceSimpleName}_Mutable")

        // real属性
        val eventRealRefName = "eventReal"
        // private val eventReal : XXEvent = XXEvent()

        val eventRealProperty = PropertySpec.builder(
            eventRealRefName,
            eventClassNameOrigin,
            KModifier.PRIVATE
        ).mutable(false).initializer("%T()", eventClassNameOrigin).build()

        val eventImplTypeSpecBuilder = eventInterfaceTypeSpec.toBuilder(
            TypeSpec.Kind.CLASS,
            name = eventImplClassName.simpleName
        )

        val internalMutableEventImplType = TypeSpec.classBuilder(
            name = internalMutableEventImplClassName.simpleName
        ).run {
            val srcPropertyRefName = "real"
            addModifiers(KModifier.INTERNAL)
            primaryConstructor(FunSpec.constructorBuilder().run {
                addParameter(srcPropertyRefName, eventImplClassName)
                build()
            })
            addProperty(
                PropertySpec.builder(srcPropertyRefName, eventImplClassName)
                    .initializer(srcPropertyRefName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            addProperty(PropertySpec.builder(srcPropertyRefName, eventImplClassName).run {
                addModifiers(KModifier.PRIVATE)
                mutable(false)
                build()
            })
            addProperties(eventOriginPropertySpecs.map {
                val eventField = eventFieldMap[it.name]?.first
                val eventDoc = eventFieldMap[it.name]?.second
                val eventOn = eventField?.eventOn ?: EventOn.MAIN
                val eventClassName = when (eventOn) {
                    EventOn.MAIN -> ClassName(PACKAGE_NAME_EVENT, CLASS_MUTABLE_LIVE_EVENT)
                    EventOn.BACKGROUND -> ClassName(
                        PACKAGE_NAME_EVENT,
                        CLASS_MUTABLE_BACKGROUND_EVENT
                    )
                }
                PropertySpec.builder(it.name, eventClassName.parameterizedBy(it.type)).run {
                    mutable(false)
                    delegate(
                        "%T.$CLASS_EVENT_INTERNAL_MUTABLE_DELEGATE(%L.%L)",
                        eventClassNames[it.name],
                        srcPropertyRefName,
                        it.name
                    )
                    eventDoc?.let(::addKdoc)
                    build()
                }

            })
            build()
        }

        eventImplTypeSpecBuilder.addSuperinterface(eventInterfaceClassName)
        eventImplTypeSpecBuilder.addProperty(eventRealProperty)
        val eventImplPropertySpecs = eventOriginPropertySpecs.map {
            val eventDoc = eventFieldMap[it.name]?.second
            val propertySpecBuilder = PropertySpec.builder(it.name, eventClassNames[it.name]!!.parameterizedBy(
                it.type
            ))
            propertySpecBuilder.modifiers.remove(KModifier.ABSTRACT)
            propertySpecBuilder.addModifiers(KModifier.OVERRIDE)
            propertySpecBuilder.mutable(false)

            propertySpecBuilder.delegate(
                CodeBlock
                    .builder()
                    .add("%T.$CLASS_EVENT_DELEGATE()", eventClassNames[it.name])
                    .build()
            )
            propertySpecBuilder.apply { eventDoc?.let(::addKdoc) }
            propertySpecBuilder.build()
        }

        val eventImplInternalMutableEventPropertyName = "_mutable"
        val eventImplInternalMutableEventProperty : PropertySpec = PropertySpec.builder(eventImplInternalMutableEventPropertyName, internalMutableEventImplClassName).run {
            addModifiers(KModifier.INTERNAL)
            mutable(false)
            delegate("lazy() { %T(this) }", internalMutableEventImplClassName)
            build()
        }

        eventImplTypeSpecBuilder.propertySpecs.clear()
        eventImplTypeSpecBuilder.addProperty(eventImplInternalMutableEventProperty)
        eventImplTypeSpecBuilder.addProperties(eventImplPropertySpecs)
        val eventImplTypeSpec = eventImplTypeSpecBuilder.build()

        val eventImplInternalMutableExtFun = FunSpec.builder("mutable")
            .addModifiers(KModifier.INTERNAL)
            .receiver(eventInterfaceClassName)
            .returns(internalMutableEventImplClassName)
            .addStatement("return (this as %T).%L", eventImplClassName, eventImplInternalMutableEventPropertyName)
            .build()

        apiFileSpecBuilder.addType(eventInterfaceTypeSpec)
        apiImplFileSpecBuilder.addType(eventImplTypeSpec)
        apiImplFileSpecBuilder.addFunction(eventImplInternalMutableExtFun)
        apiImplFileSpecBuilder.addType(internalMutableEventImplType)
        return GenModuleEventResult(
            eventInterfaceClassName,
            eventImplClassName
        )
    }
}