package com.p2m.gradle.bean.settings

import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.utils.NamedUtils
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

class ModuleProjectConfig extends BaseProjectConfig{

    ModuleProjectConfig(ModuleNamed named, Settings settings) {
        super(named, settings)
    }

    boolean runApp          // 是否可以运行
    boolean useRepo         // 使用已经发布到仓库中的模块，默认false。
    String groupId          // 组，用于发布和解析
    String versionName      // 版本，用于发布和解析

    @Override
    void include(String path, Closure<ProjectDescriptor> c){
        if (_projectNamed != null ) throw P2MSettingsException("Not use include repeatedly")
        this._projectDirPath = path

        String realPath = ":${_moduleNamed.get()}"
        _projectNamed = NamedUtils.project(realPath)
        this._projectPath = realPath

        this._projectDescriptorClosure = c
    }
}
