package com.p2m.gradle.extension.settings

import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.settings.AppProjectConfig
import com.p2m.gradle.bean.settings.BaseSupportRunAppConfig
import com.p2m.gradle.bean.settings.ModuleProjectConfig
import com.p2m.gradle.utils.NamedUtils
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

class P2MConfig extends BaseSupportRunAppConfig{
    private Settings settings
    Closure<MavenArtifactRepository> _p2mMavenRepositoryClosure

    P2MConfig(final Settings settings){
        this.settings = settings
    }

    boolean _devEnv
    boolean _isRepoLocal

    private final Map<ModuleNamed, ModuleProjectConfig> modulesConfig = new HashMap()
    private final List<AppProjectConfig> appProjectConfigs = new ArrayList<>()

    void app(Closure<AppProjectConfig> c) {
        def appProjectConfig = new AppProjectConfig(settings)
        ConfigureUtil.configure(c, appProjectConfig)
        appProjectConfigs.add(appProjectConfig)
    }

    void module(String name, Closure<ModuleProjectConfig> c) {
        def moduleNamed = NamedUtils.module(name)
        def moduleConfig = new ModuleProjectConfig(moduleNamed, settings)
        ConfigureUtil.configure(c, moduleConfig)
        modulesConfig.put(moduleNamed, moduleConfig)
    }

    void p2mMavenRepository (Closure<MavenArtifactRepository> c) {
        _p2mMavenRepositoryClosure = c
    }

    List<AppProjectConfig> getAppProjectConfigs() { return appProjectConfigs }
    Map<ModuleNamed, ModuleProjectConfig> getModulesConfig() { return modulesConfig }

}

