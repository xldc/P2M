package com.p2m.gradle

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.AppProject
import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import com.p2m.gradle.task.GenerateModuleAutoCollector
import com.p2m.gradle.utils.AndroidUtils
import com.p2m.gradle.utils.GenerateModuleAutoCollectorJavaTaskRegister
import com.p2m.gradle.utils.ModuleProjectUtils
import com.p2m.gradle.utils.RunAppConfigUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.TaskProvider

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
        }

        project.dependencies { DependencyHandler handler ->
            handler.add("implementation", project._p2mApi())
            handler.add("implementation", project._p2mAnnotation())
        }


        GenerateModuleAutoCollectorJavaTaskRegister.register(appProject, false)
    }

}
