package com.p2m.gradle.bean.settings


import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.ProjectNamed
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

abstract class BaseProjectConfig extends BaseSupportRunAppConfig {
    private Settings settings

    ModuleNamed _moduleNamed
    String _projectPath
    String _projectDirPath
    Closure<ProjectDescriptor> _projectDescriptorClosure
    ProjectNamed _projectNamed                     // 项目名称
    DependencyContainer _dependencyContainer = new DependencyContainer()        // 依赖的模块

    public BaseProjectConfig(ModuleNamed named, Settings settings) {
        this.settings = settings
        this._moduleNamed = named
    }

    void dependencies(Closure<DependencyContainer> c) {
        ConfigureUtil.configure(c, _dependencyContainer)
    }

    void include(String path){
        if (_projectNamed != null ) throw P2MSettingsException("Not use include repeatedly")
        include(path) { }
    }

    abstract void include(String path, Closure<ProjectDescriptor> c)

    @Override
    int hashCode() {
        return _moduleNamed.get().hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof BaseProjectConfig) {
            return false
        }
        return _moduleNamed.equals((o as BaseProjectConfig)._moduleNamed)
    }

}
