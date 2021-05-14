package com.p2m.gradle.utils

import com.p2m.gradle.bean.AppProject
import com.p2m.gradle.bean.LocalModuleProject
import com.p2m.gradle.bean.RemoteModuleProject
import com.p2m.gradle.bean.settings.AppProjectConfig
import com.p2m.gradle.bean.settings.ModuleProjectConfig

class ProjectFactory {

    static LocalModuleProject createLocalModuleProject(ModuleProjectConfig moduleConfig) {
        def moduleProject = new LocalModuleProject()
        moduleProject.moduleNamed = moduleConfig._moduleNamed
        moduleProject.groupId = moduleConfig.groupId ?: moduleProject.moduleName
        moduleProject.versionName = moduleConfig.versionName ?: "unspecified"
        moduleProject.runApp = moduleConfig.runApp
        moduleProject.runAppConfig = moduleConfig.runAppConfig
        return moduleProject
    }

    static RemoteModuleProject createRemoteModuleProject(ModuleProjectConfig moduleConfig) {
        def moduleProject = new RemoteModuleProject()
        moduleProject.moduleNamed = moduleConfig._moduleNamed
        moduleProject.groupId = moduleConfig.groupId ?: moduleProject.moduleName
        moduleProject.versionName = moduleConfig.versionName ?: "unspecified"
        return moduleProject
    }
    
    static AppProject createMainAppProject(AppProjectConfig config) {
        def appProject = new AppProject()
        appProject.moduleNamed = config._moduleNamed
        appProject.runAppConfig = config.runAppConfig
        return appProject
    }


}
