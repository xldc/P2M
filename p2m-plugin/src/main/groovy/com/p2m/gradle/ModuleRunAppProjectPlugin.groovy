package com.p2m.gradle

import com.p2m.gradle.bean.LocalModuleProject
import com.p2m.gradle.bytecode.P2MTransform
import com.p2m.gradle.utils.ModuleProjectUtils
import com.p2m.gradle.utils.RunAppConfigUtils
import com.p2m.gradle.BaseSupportDependencyModulePlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * 当Module可以单独运行app时，apply这个插件
 */
class ModuleRunAppProjectPlugin extends BaseSupportDependencyModulePlugin {
    LocalModuleProject moduleProject

    @Override
    void doAction(Project project) {
        super.doAction(project)
        moduleProject = project.p2mProject
        if (runAppConfig.enabled) {
            project.android {
                defaultConfig {
                    applicationId = moduleProject.runAppConfig.applicationId
                    versionCode = runAppConfig.versionCode
                    versionName = runAppConfig.versionName
                }
            }
            RunAppConfigUtils.modifyMergedManifestXmlForRunApp(moduleProject, runAppConfig)
        }
        project.android {
            registerTransform(new P2MTransform(ModuleProjectUtils.collectAvailableModulesFromTop(moduleProject), true))
        }

        project.dependencies { DependencyHandler handler ->
            handler.add("implementation", project._p2mApi())
            handler.add("implementation", project._p2mAnnotation())
            handler.add("kapt", project._p2mCompiler())
        }
    }
}