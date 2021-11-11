package com.p2m.gradle

import com.p2m.gradle.bean.LocalModuleProjectUnit
import com.p2m.gradle.utils.ModuleProjectUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * 当Module可以运行App时apply这个插件
 * 该插件主要提供了p2mRunAppBuildGradle闭包，在闭包内可以配置当运行app时的applicationId等...
 */
class ModuleWhenRunAppConfigPlugin implements Plugin<Project> {
    LocalModuleProjectUnit moduleProject

    @Override
    void apply(Project project) {
        moduleProject = project.p2mProject as LocalModuleProjectUnit
        project.ext.p2mRunAppBuildGradle = { Closure c ->
            p2mRunAppBuildGradle(moduleProject, c)
        }
    }

    private def p2mRunAppBuildGradle(LocalModuleProjectUnit moduleProject, Closure c) {
        if (!available(moduleProject)) return

        ConfigureUtil.configure(c, moduleProject.project)
    }

    private boolean available(LocalModuleProjectUnit moduleProject){
        if (!moduleProject.runApp){
            moduleProject.project.logger.info(
                    """
The following configuration is not in effect, in ${moduleProject.project.projectDir.absolutePath}/build.gradle
p2mRunAppBuildGradle {
    ...
}

If you want the configuration to take effect, please configure in ${moduleProject.project.rootProject.projectDir.absolutePath}/settings.gradle
p2m {
    ${ModuleProjectUtils.getStatement(moduleProject)} {
        runApp=true
    }
}
                    """
            )
            return false
        }
        if (moduleProject.runAppConfig.enabled) {
            moduleProject.project.logger.info(
                    """
The following configuration is not in effect, in ${moduleProject.project.projectDir.absolutePath}/build.gradle
p2mRunAppBuildGradle {
    ...
}

If you want the configuration to take effect, please configure in ${moduleProject.project.rootProject.projectDir.absolutePath}/settings.gradle
p2m {
    ${ModuleProjectUtils.getStatement(moduleProject)} {
        runAppConfig {
            enabled=false
        }
    }
}
                    """
            )
            return false
        }
        return true
    }

}