package com.p2m.gradle.bean.settings

import com.p2m.gradle.utils.NamedUtils
import org.gradle.api.initialization.Settings

class AppProjectConfig extends BaseProjectConfig {
    AppProjectConfig(Settings settings){
        super(NamedUtils.module("App"), settings)
    }
}