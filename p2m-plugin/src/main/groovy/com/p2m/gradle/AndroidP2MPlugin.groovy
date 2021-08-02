package com.p2m.gradle


import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import com.p2m.gradle.bean.RemoteModuleProject
import com.p2m.gradle.exception.P2MSettingsException
import com.p2m.gradle.bean.settings.BaseProjectConfig
import com.p2m.gradle.extension.settings.P2MConfig
import com.p2m.gradle.bean.LocalModuleProject
import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.settings.ModuleProjectConfig
import com.p2m.gradle.bean.Named
import com.p2m.gradle.utils.Constant
import com.p2m.gradle.utils.NamedUtils
import com.p2m.gradle.utils.ProjectFactory
import com.p2m.gradle.utils.StatementClosureUtils
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.util.ConfigureUtil

class AndroidP2MPlugin implements Plugin<Settings> {
    // private def moduleGraph = new ModuleGraph()
    private P2MConfig p2mConfig
    boolean existRunAppModule = false
    ModuleProjectConfig supportRunAppModuleProjectConfig

    Map<ModuleNamed, ModuleProject> moduleProjectTable = new HashMap()
    Map<ModuleNamed, LocalModuleProject> localModuleProjectTable = new HashMap<>()
    Map<ModuleNamed, RemoteModuleProject> remoteModuleProjectTable = new HashMap<>()

    @Override
    void apply(final Settings settings) {

        p2mConfig = settings.extensions.create("p2m", P2MConfig.class, settings)
        settings.gradle.addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {

            }

            @Override
            void settingsEvaluated(Settings settings1) {


                checkAndLoadDevEnv(settings)

                evaluateConfig(settings)

                startInclude(settings)
            }

            @Override
            void projectsLoaded(Gradle gradle) {
                settings.gradle.removeListener(this)

                def rootProject = gradle.rootProject
                rootProject.ext.p2mDevEnv = p2mConfig._devEnv
                genP2MDependencyConfig(rootProject)

                genModuleProjectTable(rootProject)

                configLocalModuleProject(rootProject)

                configRemoteModuleProject(rootProject)

                if (!existRunAppModule) {
                    configAppProject(rootProject)
                }

                dependencyConfigCommon(rootProject)

            }

            @Override
            void projectsEvaluated(Gradle gradle) { }

            @Override
            void buildFinished(BuildResult buildResult) { }
        })
    }

    private def checkAppConfig(BaseProject project) {
        def applicationId = project.project.android.defaultConfig.applicationId
        if (applicationId == null) {
            project.error(
                    """
                        Please configure at the end in ${project.project.projectDir.absolutePath}/build.gradle.
                        p2mRunAppBuildGradle {
                            android {
                            
                                defaultConfig{
                                    applicationId "your value"
                                }
                        
                                sourceSets {
                                    main {
                                        // your src dir for run app.
                                        java.srcDirs += 'src/app/java'
                                        
                                        // your AndroidManifest.xml path for run app.
                                        manifest.srcFile 'src/app/AndroidManifest.xml'
                                    }
                                }
                            }
                        
                            dependencies {
                                // ...
                            }
                        }
                    """
            )
        }
    }

//    private def checkAppConflictWhenEvaluated(BaseProject project) {
//        AndroidUtils.forAppEachVariant(project.project) { variant ->
//            variant.getPreBuildProvider().get().doFirst {
//                checkAppConflict(project)
//            }
//        }
//    }
//
//    private void checkAppConflict(BaseProject owner) {
//        def dependencies = owner.dependencies
//        for (int i = 0; i < dependencies.size(); i++) {
//            ModuleProject moduleProject = dependencies[i]
//            if (moduleProject.isApp()) {
//                moduleProject.error(
//                        "Only one can runApp=true, ${ModuleProjectUtils.getStatement(owner)} depend on ${ModuleProjectUtils.getStatement(moduleProject)}, " +
//                                "please check that in settings.gradle."
//                )
//            }
//            // checkAppConflict(moduleProject)
//        }
//    }

    private def configRemoteModuleProject(Project rootProject) {
        remoteModuleProjectTable.values().forEach { RemoteModuleProject moduleProject ->
        }
    }

    private def configLocalModuleProject(Project rootProject) {
        localModuleProjectTable.values().forEach { LocalModuleProject moduleProject ->
            moduleProject.project.ext.runApp = moduleProject.runApp
            moduleProject.project.ext.p2mProject = moduleProject
            moduleProject.project.ext._p2mMavenRepositoryClosure = p2mConfig._p2mMavenRepositoryClosure
        }

        localModuleProjectTable.values().forEach { LocalModuleProject moduleProject ->
            moduleProject.project.beforeEvaluate {
                moduleProject.project.plugins.apply(moduleProject.isApp() ? Constant.PLUGIN_ID_ANDROID_APP : Constant.PLUGIN_ID_ANDROID_LIBRARY)
                moduleProject.project.plugins.apply(Constant.PLUGIN_ID_KOTLIN_ANDROID)
                moduleProject.project.plugins.apply(Constant.PLUGIN_ID_KOTLIN_KAPT)
                moduleProject.project.plugins.apply(ModuleWhenRunAppConfigPlugin)
                moduleProject.project.plugins.apply(ProductModuleApiPlugin)
            }
        }

        localModuleProjectTable.values().forEach { LocalModuleProject moduleProject ->
            moduleProject.project.beforeEvaluate {
                if (moduleProject.isApp()) {
                    moduleProject.project.plugins.apply(ModuleRunAppProjectPlugin)
                    // checkAppConflictWhenEvaluated(moduleProject)
                    moduleProject.project.afterEvaluate {
                        checkAppConfig(moduleProject)
                    }
                } else {
                    def mavenPublishPlugin = moduleProject.project.plugins.findPlugin(Constant.PLUGIN_ID_MAVEN_PUBLISH)
                    if (mavenPublishPlugin == null) {
                        moduleProject.project.plugins.apply(Constant.PLUGIN_ID_MAVEN_PUBLISH)
                    }
                    moduleProject.project.plugins.apply(LocalModuleProjectPlugin)
                }
            }

        }
    }


    private def configAppProject(Project rootProject) {
        p2mConfig.appProjectConfigs.forEach { appProjectConfig ->
            def appProject = createMainAppProject(rootProject, appProjectConfig, moduleProjectTable)
            appProject.project.ext.p2mProject = appProject
            appProject.project.ext._p2mMavenRepositoryClosure = p2mConfig._p2mMavenRepositoryClosure

            appProject.project.afterEvaluate {
                appProject.project.plugins.apply(MainAppProjectPlugin)
            }
            appProject.project.beforeEvaluate {
                appProject.project.plugins.apply(Constant.PLUGIN_ID_ANDROID_APP)

            }
            // checkAppConflictWhenEvaluated(appProject)
        }
    }

    private def dependencyConfigCommon(Project rootProject) {
        rootProject.allprojects.each { project ->
            project.beforeEvaluate {
                if (project.name != Constant.P2M_NAME_API && project.plugins.hasPlugin(Constant.PLUGIN_ID_ANDROID_LIBRARY)) {
                    project.dependencies.add("compileOnly", project._p2mApi())
                }
            }
        }
    }

    private def genP2MDependencyConfig(Project rootProject) {
        rootProject.allprojects {
            repositories {
                if (p2mConfig._isRepoLocal) {
                    mavenLocal()
                }
                maven { url Constant.REPO_P2M_REMOTE }
            }
        }

        if (p2mConfig._devEnv) {
            rootProject.ext._p2mApi = { return rootProject.project(":${Constant.P2M_NAME_API}") }
            rootProject.ext._p2mAnnotation = { return rootProject.project(":${Constant.P2M_NAME_ANNOTATION}") }
            rootProject.ext._p2mCompiler = { return rootProject.project(":${Constant.P2M_NAME_COMPILER}") }
        } else {
            rootProject.ext._p2mApi = { return "${Constant.P2M_GROUP_ID}:${Constant.P2M_NAME_API}:${Constant.P2M_VERSION}" }
            rootProject.ext._p2mAnnotation = { return "${Constant.P2M_GROUP_ID}:${Constant.P2M_NAME_ANNOTATION}:${Constant.P2M_VERSION}" }
            rootProject.ext._p2mCompiler = { return "${Constant.P2M_GROUP_ID}:${Constant.P2M_NAME_COMPILER}:${Constant.P2M_VERSION}" }
        }
    }

    private def genModuleProjectTable(Project rootProject) {
        moduleProjectTable.clear()
        localModuleProjectTable.clear()
        remoteModuleProjectTable.clear()
        Map<ModuleProject, Set<ModuleNamed>> moduleProjectConfigDependencies = new HashMap()

        def projects = new HashSet<Project>()
        projects.addAll(rootProject.subprojects)
        def moduleConfigs = p2mConfig.modulesConfig.values()
        def iterator = moduleConfigs.iterator()

        // for remote aar
        while (iterator.hasNext()) {
            def moduleConfig = iterator.next()
            if (moduleConfig.useRepo) { // 创建远端的module
                def moduleProject = ProjectFactory.createRemoteModuleProject(moduleConfig)
                moduleProjectConfigDependencies.put(moduleProject, moduleConfig._dependencyContainer._dependencies)
                remoteModuleProjectTable[moduleConfig._moduleNamed] = moduleProject
                moduleProjectTable.put(moduleConfig._moduleNamed, moduleProject)
                moduleProject.project = rootProject
                iterator.remove()
            }
        }

        // for local source
        projects.forEach { Project project ->
            iterator = moduleConfigs.iterator()
            while (iterator.hasNext()) {
                def moduleConfig = iterator.next()
                if (!moduleConfig.useRepo && NamedUtils.project(project.name) == moduleConfig._projectNamed) {
                    def moduleProject = ProjectFactory.createLocalModuleProject(moduleConfig)
                    moduleProject.project = rootProject.project(moduleConfig._projectNamed.include)
                    moduleProjectConfigDependencies.put(moduleProject, moduleConfig._dependencyContainer._dependencies)
                    localModuleProjectTable[moduleConfig._moduleNamed] = moduleProject
                    moduleProjectTable.put(moduleConfig._moduleNamed, moduleProject)
                    iterator.remove()
                }
            }
        }


        if (!moduleConfigs.empty) {
            rootProject.logger.warn("genModuleProjectTable fail count is ${moduleConfigs.size()}.", new IllegalStateException())
        }

        // 建立module依赖关系
        moduleProjectConfigDependencies.forEach { ModuleProject moduleProject, Set<ModuleNamed> dependencies ->
            configDependenciesToProjectDependencies(moduleProject, dependencies, moduleProjectTable)
        }
    }

    private def createMainAppProject(Project rootProject, appProjectConfig, Map<ModuleNamed, ModuleProject> moduleProjectTable) {

        def iterator = rootProject.subprojects.iterator()
        while (iterator.hasNext()) {
            def project = iterator.next()
            if (appProjectConfig._projectNamed == NamedUtils.project(project.name)) {
                def appProject = ProjectFactory.createMainAppProject(appProjectConfig)
                appProject.project = project
                configDependenciesToProjectDependencies(appProject, appProjectConfig._dependencyContainer._dependencies, moduleProjectTable)
                return appProject
            }
        }

        throw new P2MSettingsException("UnKnow Error.")
    }


    private def configDependenciesToProjectDependencies = { BaseProject ownerProject, ownerConfigDependencies, Map<ModuleNamed, ModuleProject> moduleTable ->
        // moduleGraph.addDepends(project.moduleNamed, configDependencies)
        if (ownerConfigDependencies == null) return
//        if (project instanceof AppProject) {
//            moduleGraph.setTopNode(project.moduleNamed)
//        }
        ownerConfigDependencies.forEach { ModuleNamed dependencyModuleNamed ->
            if (moduleTable[dependencyModuleNamed] == null) {
                ownerProject.error("Dependency ${NamedUtils.getStatement(dependencyModuleNamed)} not exist, Please check in settings.gradle.")
            }
            println("${ownerProject} depends on ${moduleTable[dependencyModuleNamed]}")
            if (ownerProject.moduleNamed == dependencyModuleNamed) {
                ownerProject.error("Unsupport depends on self! Please check ${NamedUtils.getStatement(ownerProject.moduleNamed)} in settings.gradle.")
            }
            ownerProject.dependencies.add(moduleTable[dependencyModuleNamed])
        }
    }


    private static def includeProject(Settings settings, BaseProjectConfig projectConfig) {
        println("include(\"${projectConfig._projectPath}\")")
        settings.include(projectConfig._projectPath)
        if (projectConfig._projectDescriptorClosure != null) {
            def obj = settings.project(projectConfig._projectPath)
            ConfigureUtil.configure(projectConfig._projectDescriptorClosure, obj)
        }
    }


    private def startInclude(Settings settings) {

        if (!existRunAppModule) {
            p2mConfig.appProjectConfigs.forEach { appProjectConfig ->
                println("============ include App ============")
                includeProject(settings, appProjectConfig)
                println("========================================")
            }

        }

        p2mConfig.modulesConfig.forEach { key, moduleConfig ->

            println("====== include Module ${moduleConfig._moduleNamed.get()} ======")
            if (moduleConfig.useRepo) {
                println("include aar(group=${moduleConfig.groupId} version=${moduleConfig.versionName})")
            } else {
                includeProject(settings, moduleConfig)
            }
            println("========================================")
        }
    }

    private def moduleConfigEvaluate(Settings settings, BaseProjectConfig moduleConfig) {
        def projectDescriptor = moduleConfig._projectPath
        if (projectDescriptor == null) {
            throw new P2MSettingsException(StatementClosureUtils.getStatementMissingClosureTip(moduleConfig, "include", "your project name", "projectDir = new File(\"your project path\")"))
        }

        if (moduleConfig._projectDescriptorClosure == null) {
            def projectDir = new File(settings.rootDir, moduleConfig._projectNamed.get())
            if (!projectDir.exists()) {
                throw new P2MSettingsException(StatementClosureUtils.getStatementMissingClosureTip(moduleConfig, "include", moduleConfig._projectNamed.include, "projectDir = new File(\"your project path\")"))
            }
        }


/*        def projectName = moduleConfig.projectName
        if (projectName == null || projectName.isEmpty()) {
            throw new P2MSettingsException(StatementPropertyUtils.getStatementMissingPropertyTip(moduleConfig, "projectName"))
        }

        def projectDir = moduleConfig.projectDir ?: new File(settings.getRootProject().projectDir, projectName)
        projectDir = new File(projectDir.absolutePath)
        moduleConfig.projectDir = projectDir

        if (!projectDir.exists()) {
            throw new P2MSettingsException("\nPlease check config in settings.gradle.\n" +
                    StatementPropertyUtils.getStatementPropertyTip(moduleConfig, "projectDir") +
                    "Path:${moduleConfig.projectDir} is not exists. Please create the project dir." +
                    "\n"
            )
        }

        moduleConfig.projectNamed = NamedUtils.project(projectName)*/
    }

    private def checkAndLoadDevEnv(Settings settings) {
        def isDevEnv = false
        def isRepoLocal = false
        def localProperties = new Properties()
        def localPropertiesFile = new File(settings.buildscript.sourceFile.parentFile, "local.properties")

        if (localPropertiesFile.exists()) {
            localPropertiesFile.withReader('UTF-8') { reader ->
                localProperties.load(reader)
            }

            if (localProperties.getProperty(Constant.LOCAL_PROPERTY_DEV_ENV) != null) {
                isDevEnv = localProperties.getProperty(Constant.LOCAL_PROPERTY_DEV_ENV).toBoolean()
            }

            if (localProperties.getProperty(Constant.LOCAL_PROPERTY_REPO_LOCAL) != null) {
                isRepoLocal = localProperties.getProperty(Constant.LOCAL_PROPERTY_REPO_LOCAL).toBoolean()
            }
        }

        if (isDevEnv) {
            settings.include(":${Constant.PROJECT_NAME_P2M_CORE}")
            settings.project(":${Constant.PROJECT_NAME_P2M_CORE}").projectDir = new File("../${Constant.PROJECT_NAME_P2M_CORE}")
            settings.include(":${Constant.PROJECT_NAME_P2M_COMPILER}")
            settings.project(":${Constant.PROJECT_NAME_P2M_COMPILER}").projectDir = new File("../${Constant.PROJECT_NAME_P2M_COMPILER}")
            settings.include(":${Constant.PROJECT_NAME_P2M_ANNOTATION}")
            settings.project(":${Constant.PROJECT_NAME_P2M_ANNOTATION}").projectDir = new File("../${Constant.PROJECT_NAME_P2M_ANNOTATION}")
        }
        p2mConfig._devEnv = isDevEnv
        p2mConfig._isRepoLocal = isRepoLocal
    }

    private def evaluateConfig(Settings settings) {

        def appProjectConfigs = p2mConfig.appProjectConfigs
        if (appProjectConfigs == null || appProjectConfigs.empty) {
            throw new P2MSettingsException("\nPlease config content in settings.gradle\n" +
                    "p2m {\n" +
                    "   app {\n" +
                    "       include(\":{your app project name}\") {\n" +
                    "           projectDir = new File(\"{project path}\")\n" +
                    "       }\n" +
                    "       dependencies {\n" +
                    "           ${NamedUtils.getStatement(new ModuleNamed("{your module 1}"))}\n" +
                    "           ${NamedUtils.getStatement(new ModuleNamed("{your module 2}"))}\n" +
                    "           ... \n" +
                    "       }\n" +
                    "   }\n" +
                    "}\n" +
                    "\n"
            )
        }
        appProjectConfigs.forEach { appProjectConfig ->
            appProjectConfig.runAppConfig.enabled = false
            moduleConfigEvaluate(settings, appProjectConfig)
        }


        p2mConfig.modulesConfig.forEach { Named named, ModuleProjectConfig moduleConfig ->
            moduleConfigEvaluate(settings, moduleConfig)

            if (moduleConfig.runApp) {

                if (supportRunAppModuleProjectConfig != null) {
                    throw new P2MSettingsException("""
                            ${NamedUtils.getStatement(supportRunAppModuleProjectConfig._moduleNamed)} setted runApp=true already, only one module can set runApp=true.
                            Please check config in settings.gradle.
                        """
                    )
                }
                supportRunAppModuleProjectConfig = moduleConfig
                existRunAppModule = true
//                moduleConfig.extendRunAppConfig(p2m)
//                moduleConfig.checkRunAppConfig()
            }

        }

        if (existRunAppModule) {
            reevaluateModulesIfModuleRunApp(supportRunAppModuleProjectConfig)
        }

    }

    private Set<ModuleNamed> collectAllDependencies(ModuleProjectConfig moduleProjectConfig) {
        Set<ModuleNamed> allDependencies = new HashSet<ModuleNamed>()
        allDependencies.add(moduleProjectConfig._moduleNamed)
        def dependencies = moduleProjectConfig._dependencyContainer._dependencies
        if (dependencies != null) {
            dependencies.forEach { moduleNamed ->
                allDependencies.addAll(collectAllDependencies(p2mConfig.modulesConfig[moduleNamed]))
            }
        }
        return allDependencies
    }

    private def reevaluateModulesIfModuleRunApp(ModuleProjectConfig supportRunAppModuleProjectConfig) {
        Set<ModuleNamed> allDependencies = collectAllDependencies(supportRunAppModuleProjectConfig)
        Set<ModuleNamed> willRemove = new HashSet<ModuleNamed>()
        willRemove.addAll(p2mConfig.modulesConfig.keySet())
        willRemove.removeAll(allDependencies)
        willRemove.forEach { remove ->
            p2mConfig.modulesConfig.remove(remove)
        }
    }
}
