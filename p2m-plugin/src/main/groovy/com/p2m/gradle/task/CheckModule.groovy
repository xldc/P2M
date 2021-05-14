package com.p2m.gradle.task

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.AbstractTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.charset.StandardCharsets

abstract class CheckModule extends AbstractTask {

    @Inject
    CheckModule(ObjectFactory objects){
        propertiesConfigurableFile = objects.fileProperty()
    }

    @InputFiles
    ConfigurableFileCollection propertiesConfigurableFileCollection

    @OutputFile
    RegularFileProperty propertiesConfigurableFile

    @TaskAction
    void doCheck(){
        def file = propertiesConfigurableFileCollection.getSingleFile()
        file.newReader(StandardCharsets.UTF_8.name()).eachLine { line ->
            def split = line.split("=")
            def attr = split[0]
            def value = split[1]
            if (attr == "genModuleSource") {
                def exist = value.toBoolean()
                if (!exist) {
                    throw project.logger.error("""
Must add source code in Module[${project.p2mProject.getModuleName()}]：

@ModuleInitializer
class ${project.p2mProject.getModuleName()}Module:Module{

    override fun onEvaluate(taskRegister: TaskRegister) {
        // Evaluate stage of itself.
        // Here, You can use [taskRegister] to register some task for help initialize module fast,
        // and then these tasks will be executed order.
    }

    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        // Executed stage of itself, indicates will completed initialized of the module.
        // Called when its all tasks be completed and all dependencies completed initialized.
        // Here, You can use [taskOutputProvider] to get some output of itself tasks,
        // also use [moduleProvider] to get some dependency module.
    }
}
""")
                }
            }
        }
    }

}