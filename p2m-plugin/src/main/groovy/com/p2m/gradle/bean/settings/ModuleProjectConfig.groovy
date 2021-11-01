package com.p2m.gradle.bean.settings

import com.p2m.gradle.bean.ModuleNamed
import org.gradle.api.initialization.Settings

class ModuleProjectConfig extends com.p2m.gradle.bean.settings.BaseProjectConfig{

    ModuleProjectConfig(ModuleNamed named, Settings settings) {
        super(named, settings)
    }

    boolean runApp          // 是否可以运行
    boolean useRepo         // 使用已经发布到仓库中的模块，默认false。
    String groupId          // 组，用于发布和解析
    String versionName      // 版本，用于发布和解析

}
