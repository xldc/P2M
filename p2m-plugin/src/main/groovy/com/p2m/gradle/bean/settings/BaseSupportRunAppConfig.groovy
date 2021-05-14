package com.p2m.gradle.bean.settings

import com.p2m.gradle.bean.RunAppConfig
import com.p2m.gradle.exception.P2MSettingsException
import com.p2m.gradle.utils.StatementPropertyUtils
import org.gradle.util.ConfigureUtil

abstract class BaseSupportRunAppConfig {
    private RunAppConfig runAppConfig = new RunAppConfig()

    RunAppConfig getRunAppConfig() {
        return runAppConfig
    }

    @Deprecated
    void runAppConfig(Closure c) {
        if (this instanceof AppProjectConfig) throw new P2MSettingsException("Not Support runAppConfig in p2m.app scope.")
        ConfigureUtil.configure(c, runAppConfig)
    }

    void extendRunAppConfig(BaseSupportRunAppConfig parent){
        runAppConfig.extend(parent.runAppConfig)
    }

    void checkRunAppConfig(){
        def property = runAppConfig.getMissingProperty()
        if (property != null) {
            throw new P2MSettingsException(StatementPropertyUtils.getAppRunConfigStatementMissingPropertyTip(this, property))
        }
    }
}
