package com.p2m.gradle

import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.LocalModuleProject
import com.p2m.gradle.task.ApiJar
import com.p2m.gradle.task.ApiSourceJar
import com.p2m.gradle.task.CheckModule
import com.p2m.gradle.utils.AndroidUtils
import com.p2m.gradle.utils.Constant
import com.p2m.gradle.utils.ModuleProjectUtils
import com.p2m.gradle.utils.PublishUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.internal.KaptTask

import java.nio.charset.StandardCharsets

class ProductModuleApiPlugin implements Plugin<Project> {
    private LocalModuleProject moduleProject

    @Override
    void apply(Project project) {
        moduleProject = project.p2mProject

        ModuleProjectUtils.maybeCreateConfigurationForP2MApi(moduleProject)

        moduleProject.project.kapt.arguments {
            if (options == null) return
            def variant = variant as BaseVariant
            def variantName = variant.getName()
            def p2mApiSrcDir = new File(moduleProject.project.buildDir, "generated/p2m/src/${variantName}")

            arg("moduleName", moduleProject.getModuleName())
            arg("apiSrcDir", p2mApiSrcDir.absolutePath)
        }

        project.afterEvaluate {
            productModuleApi(project)
            if (!moduleProject.isApp()) {
                PublishUtils.createPublish(moduleProject)
            }
        }
    }

    private def productModuleApi = { Project project ->

        AndroidUtils.forEachVariant(project) { BaseVariant variant ->
            def variantName = variant.name
            def variantTaskMiddleName = variantName.capitalize()
            Provider<AbstractCompile> compileKotlin
            Provider<KaptTask> kaptKotlin
            try{
                project.tasks.named("bundle${variantTaskMiddleName}Aar")
                compileKotlin = project.tasks.named("compile${variantTaskMiddleName}Kotlin")
                kaptKotlin = project.tasks.named("kapt${variantTaskMiddleName}Kotlin")
            }catch( UnknownTaskException e){
                project.logger.debug(e.cause)
                return
            }

            def apiJarName = "p2m-module-api-${moduleProject.getModuleName()}.jar"
            def apiSourceJarName = "p2m-module-api-${moduleProject.getModuleName()}-sources.jar"
            def p2mApiJarDir = new File(project.buildDir, "generated/p2m/jar/${variantName}")
            def p2mKaptSrcDir = new File(project.buildDir, "generated/source/kapt/${variantName}")
            def p2mApiPropertiesFile = new File(p2mKaptSrcDir, "module_api.properties")
            def p2mApiJarFile =  new File(p2mApiJarDir, apiJarName)
            def p2mApiSourceJarFile =  new File(p2mApiJarDir, apiSourceJarName)
            def p2mApiPropertiesConfigurableFileCollection = project.files(p2mApiPropertiesFile)
            def p2mApiJarConfigurableFileCollection = project.files(p2mApiJarFile, p2mApiSourceJarFile)
            p2mApiPropertiesConfigurableFileCollection.builtBy(kaptKotlin)

            def checkModuleProvider = project.tasks.register("checkModule${variantTaskMiddleName}", CheckModule.class)
            def compileApiProvider = project.tasks.register("compileApi${variantTaskMiddleName}", ApiJar.class)
            def compileApiSourceProvider = project.tasks.register("compileApiSource${variantTaskMiddleName}", ApiSourceJar.class)

            checkModuleProvider.configure {
                group = Constant.P2M_MODULE_TASK_GROUP
                description = "check ${moduleProject.getModuleName()} module for ${variantName}"
                dependsOn(kaptKotlin)
                propertiesConfigurableFile.set(p2mApiPropertiesConfigurableFileCollection.singleFile)
                propertiesConfigurableFileCollection = p2mApiPropertiesConfigurableFileCollection
            }

            compileApiProvider.configure {
                group = Constant.P2M_MODULE_TASK_GROUP
                description = "compile ${moduleProject.getModuleName()} module api class for ${variantName}"
                dependsOn(kaptKotlin)
                dependsOn(checkModuleProvider)
                dependsOn(compileKotlin)
                archiveName = apiJarName
                destinationDir = p2mApiJarDir
                inputKaptDirCollection = kaptKotlin.get().destinationDir // for UP-TO-DATE
                inputKotlinCompilerOutput = compileKotlin.get().source // for UP-TO-DATE

                doFirst {

                    p2mApiPropertiesConfigurableFileCollection.getSingleFile().newReader(StandardCharsets.UTF_8.name()).eachLine { line ->
                        def split = line.split("=")
                        def attr = split[0]
                        def value = split[1]
                        if (attr == "exportApiClassPath") {
                            exportApiClassPathList.addAll(value.split(","))
                        }
                    }

                    from project.files(compileKotlin.get().outputs) {
                        exportApiClassPathList.forEach{ name->
                            project.logger.info("${moduleProject.getModuleName()}:${compileApiProvider.get().name} include $name")
                            include "${name}.*"
                        }

                    }

                }

            }

            compileApiSourceProvider.configure{
                group = Constant.P2M_MODULE_TASK_GROUP
                description = "compile ${moduleProject.getModuleName()} module api source for ${variantName}"
                dependsOn(kaptKotlin)
                dependsOn(checkModuleProvider)
                archiveName = apiSourceJarName
                // java/lang/String
                List<String> exportApiSourcePathList = new ArrayList<>()
                classifier 'sources'
                inputKaptDirCollection = kaptKotlin.get().destinationDir // for UP-TO-DATE
                inputKotlinCompilerOutput = compileKotlin.get().source // for UP-TO-DATE
                destinationDir = p2mApiJarDir
                doFirst {
                    p2mApiPropertiesConfigurableFileCollection.getSingleFile().newReader(StandardCharsets.UTF_8.name()).eachLine { line ->
                        def split = line.split("=")
                        def attr = split[0]
                        def value = split[1]
                        if (attr == "exportApiSourcePath") {
                            exportApiSourcePathList.addAll(value.split(","))
                        }
                    }

                    from project.files(kaptKotlin.get().outputs) {
                        exportApiSourcePathList.forEach{ name->
                            project.logger.info("${moduleProject.getModuleName()}:${compileApiSourceProvider.get().name} include $name")
                            include "${name}.*"
                        }

                    }
                }
            }

            variant.javaCompileProvider.configure{
                dependsOn(checkModuleProvider, compileApiProvider, compileApiSourceProvider)
            }

            p2mApiJarConfigurableFileCollection.builtBy(compileApiProvider, compileApiSourceProvider)
            project.configurations
                    .maybeCreate("${variantName}P2MApi")
                    .dependencies
                    .add(project.dependencies.create(p2mApiJarConfigurableFileCollection))

        }

    }
}