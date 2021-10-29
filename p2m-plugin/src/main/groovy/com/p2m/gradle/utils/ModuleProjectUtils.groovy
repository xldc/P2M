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

    static def collectValidModuleTableFromTop = { BaseProject baseProject ->
        def moduleProjects = new HashMap<String, BaseProject>()
        moduleProjects[baseProject.moduleName] = baseProject
        //noinspection UnnecessaryQualifiedReference
        ModuleProjectUtils.putDependencies(baseProject.dependencies, moduleProjects)
        return moduleProjects
    }

    static def collectValidModuleProjectsFromTop = { BaseProject baseProject, boolean collectSelf ->
        HashSet moduleProjects = new HashSet<BaseProject>()
        if (collectSelf){
            moduleProjects.add(baseProject)
        }
        //noinspection UnnecessaryQualifiedReference
        ModuleProjectUtils.putDependenciesV2(baseProject.dependencies, moduleProjects)
        return moduleProjects
    }

    private static def putDependencies = { Set<ModuleProject> dependencies, HashMap<String, BaseProject> moduleProjects ->
        dependencies.forEach { dependencyModule ->
            moduleProjects[dependencyModule.moduleName] = dependencyModule
            //noinspection UnnecessaryQualifiedReference
            ModuleProjectUtils.putDependencies(dependencyModule.dependencies, moduleProjects)
        }
    }
    private static def putDependenciesV2 = { Set<ModuleProject> dependencies, HashSet<BaseProject> moduleProjects ->
        dependencies.forEach { dependencyModule ->
            moduleProjects.add(dependencyModule)
            //noinspection UnnecessaryQualifiedReference
            ModuleProjectUtils.putDependenciesV2(dependencyModule.dependencies, moduleProjects)
        }
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
