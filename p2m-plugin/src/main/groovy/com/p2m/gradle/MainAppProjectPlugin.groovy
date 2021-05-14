package com.p2m.gradle

import com.p2m.gradle.bean.AppProject
import com.p2m.gradle.bytecode.P2MTransform
import com.p2m.gradle.utils.ModuleProjectUtils
import com.p2m.gradle.utils.RunAppConfigUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * App壳工程插件
 */
class MainAppProjectPlugin extends BaseSupportDependencyModulePlugin {

    AppProject appProject
    @Override
    void doAction(Project project) {
        super.doAction(project)
        appProject = baseProject as AppProject
        project.android {
            if (runAppConfig.enabled) {
                defaultConfig {
                    applicationId = appProject.runAppConfig.applicationId
                    versionCode = runAppConfig.versionCode
                    versionName = runAppConfig.versionName
                }

                RunAppConfigUtils.modifyMergedManifestXmlForRunApp(appProject, runAppConfig)
            }
            registerTransform(new P2MTransform(ModuleProjectUtils.collectAvailableModulesFromTop(appProject), false))
        }

        project.dependencies { DependencyHandler handler ->
            handler.add("implementation", project._p2mApi())
            handler.add("implementation", project._p2mAnnotation())
        }
    }
}