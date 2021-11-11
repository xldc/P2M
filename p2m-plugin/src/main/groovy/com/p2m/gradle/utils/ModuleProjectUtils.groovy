package com.p2m.gradle.utils

import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.BaseProjectUnit
import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.ModuleProjectUnit
import com.p2m.gradle.bean.ProjectNamed
import org.gradle.api.artifacts.Configuration

class ModuleProjectUtils {
    static def maybeCreateModuleApiConfiguration = { ModuleProjectUnit moduleProject ->
        AndroidUtils.forEachVariant(moduleProject.project){ BaseVariant variant->
            Configuration c = moduleProject.project.configurations.maybeCreate("$variant.buildType.name$Constant.P2M_CONFIGURATION_NAME_SUFFIX_MODULE_API")
            c.canBeResolved = false
            c.canBeConsumed = true
        }
    }

    static def collectValidDependencies = { BaseProjectUnit projectUnit, boolean collectSelf ->
        HashSet moduleProjects = new HashSet<BaseProjectUnit>()
        if (collectSelf){
            moduleProjects.add(projectUnit)
        }
        projectUnit.dependencies.forEach{ dependencyModule ->
            moduleProjects.add(dependencyModule)
        }
        return moduleProjects
    }

    static def getStatement = { BaseProjectUnit projectUnit->
        def named = projectUnit.moduleNamed
        if (named instanceof ModuleNamed) {
            return "module(\"${named.get()}\")"
        }else if (named instanceof ProjectNamed){
            return "project(\"${named.getInclude()}\")"
        }else {
            return named.get()
        }
    }
}
