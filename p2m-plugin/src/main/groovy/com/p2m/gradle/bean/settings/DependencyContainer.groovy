package com.p2m.gradle.bean.settings

import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.utils.NamedUtils

class DependencyContainer {
    Set<ModuleNamed> _dependencies = new HashSet()

    ModuleNamed module(String name) {
        def moduleNamed = NamedUtils.module(name)
        _dependencies.add(moduleNamed)
        return moduleNamed
    }
}
