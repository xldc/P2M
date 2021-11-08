package com.p2m.gradle.utils

import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.ModuleProject
import com.p2m.gradle.bean.ProjectNamed
import org.gradle.api.artifacts.Configuration

class ModuleProjectUtils {
    static def maybeCreateModuleApiConfiguration = { ModuleProject moduleProject ->
        AndroidUtils.forEachVariant(moduleProject.project){ BaseVariant variant->
            Configuration c = moduleProject.project.configurations.maybeCreate("$variant.buildType.name$Constant.P2M_CONFIGURATION_NAME_SUFFIX_MODULE_API")
            c.canBeResolved = false
            c.canBeConsumed = true
        }
    }

    static def collectValidModuleProjectsFromTop = { BaseProject baseProject, boolean collectSelf ->
        HashSet moduleProjects = new HashSet<BaseProject>()
        if (collectSelf){
            moduleProjects.add(baseProject)
        }
        baseProject.dependencies.forEach{ dependencyModule ->
            moduleProjects.add(dependencyModule)
        }
        return moduleProjects
    }

    static def getStatement = { BaseProject baseProject->
        def named = baseProject.moduleNamed
        if (named instanceof ModuleNamed) {
            return "module(\"${named.get()}\")"
        }else if (named instanceof ProjectNamed){
            return "project(\"${named.getInclude()}\")"
        }else {
            return named.get()
        }
    }
}
