package com.p2m.gradle.utils

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.BaseProjectUnit
import com.p2m.gradle.bean.ModuleProjectUnit
import com.p2m.gradle.task.GenerateModuleAutoCollector
import org.gradle.api.tasks.TaskProvider

class GenerateModuleAutoCollectorJavaTaskRegister {

    public static void register(BaseProjectUnit moduleProject, boolean collectSelf){

        HashSet<ModuleProjectUnit> validDependencies = ModuleProjectUtils.collectValidDependencies(moduleProject, collectSelf)
        def project = moduleProject.project
        AndroidUtils.forEachVariant(project) { BaseVariant variant ->
            variant = variant as ApplicationVariant
            def variantName = variant.name
            def variantTaskMiddleName = variantName.capitalize()
            def taskName = "generate${variantTaskMiddleName}ModuleAutoCollector"
            HashSet<String> validDependenciesName = new HashSet<String>()
            validDependencies.forEach { validDependenciesName.add(it.moduleName) }
            def generateModuleAutoCollectorSourceOutputDir = new File(
                    project.getBuildDir().absolutePath
                            + File.separator
                            + "generated"
                            + File.separator
                            + "source"
                            + File.separator
                            + "moduleAutoCollector"
                            + File.separator
                            + variantName)
            TaskProvider taskProvider = project.tasks.register(taskName, GenerateModuleAutoCollector.class) { task ->
                task.sourceOutputDir.set(generateModuleAutoCollectorSourceOutputDir)
                task.packageName.set(variant.applicationId)
                task.validDependenciesName.set(validDependenciesName)
            }

            variant.registerJavaGeneratingTask(taskProvider.get(), generateModuleAutoCollectorSourceOutputDir)
            variant.addJavaSourceFoldersToModel(generateModuleAutoCollectorSourceOutputDir)
        }
    }


}
