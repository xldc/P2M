package com.p2m.gradle.bean

import org.gradle.api.Project

abstract class BaseProject {
    Named moduleNamed
    Project project                                     // 项目
    Set<ModuleProject> dependencies = new HashSet()     // 依赖的模块
    RunAppConfig runAppConfig

    boolean isApp(){
        if (this instanceof AppProject) return true
        if (this instanceof RemoteModuleProject) return false
        if (this instanceof LocalModuleProject) {
            return ((LocalModuleProject)this).runApp
        }
        return false
    }

    String getModuleName() {
        return moduleNamed.get()
    }

    String getModuleNameLowerCase() {
        return getModuleName().toLowerCase()
    }

    void error(String message) {
        message = "P2M: $message"
        if (project != null) {
            project.logger.error(message)
        }
        throw P2MSettingsException(message)
    }


    @Override
    int hashCode() {
        return moduleNamed.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof BaseProject) {
            return false
        }
        return moduleNamed.equals((o as BaseProject).moduleNamed)
    }
}
