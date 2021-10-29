package com.p2m.gradle.utils

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import com.p2m.gradle.task.GenerateModuleAutoCollector
import org.gradle.api.tasks.TaskProvider

class GenerateModuleAutoCollectorJavaTaskRegister {

    public static void register(BaseProject moduleProject, boolean collectSelf){
        HashSet<ModuleProject> validModuleProjects = ModuleProjectUtils.collectValidModuleProjectsFromTop(moduleProject, collectSelf)
        def project = moduleProject.project
        AndroidUtils.forEachVariant(project) { BaseVariant variant ->
            variant = variant as ApplicationVariant
            def variantName = variant.name
            def variantTaskMiddleName = variantName.capitalize()
            def taskName = "generate${variantTaskMiddleName}ModuleAutoCollector"
            HashSet<String> modules = new HashSet<String>()
            validModuleProjects.forEach { modules.add(it.moduleName) }
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
                task.modules.set(modules)
            }

            variant.registerJavaGeneratingTask(taskProvider.get(), generateModuleAutoCollectorSourceOutputDir)
            variant.addJavaSourceFoldersToModel(generateModuleAutoCollectorSourceOutputDir)
        }
    }


}
