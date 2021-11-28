package com.p2m.gradle.bean.settings

import com.p2m.gradle.utils.NamedUtils
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

class AppProjectConfig extends BaseProjectConfig {
    AppProjectConfig(Settings settings){
        super(NamedUtils.module("App"), settings)
    }

    @Override
    void include(String path, Closure<ProjectDescriptor> c){
        if (_projectNamed != null ) throw P2MSettingsException("Not use include repeatedly")
        _projectNamed = NamedUtils.project(path)
        this._projectPath = path
        this._projectDescriptorClosure = c
    }
}