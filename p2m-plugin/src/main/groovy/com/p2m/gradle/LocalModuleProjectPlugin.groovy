package com.p2m.gradle

import com.p2m.gradle.bean.LocalModuleProject
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * 当Module提供给其他模块和App使用时，apply这个插件
 */
class LocalModuleProjectPlugin extends BaseSupportDependencyModulePlugin {
    private LocalModuleProject moduleProject

    @Override
    void doAction(Project project) {
        super.doAction(project)
        moduleProject = project.p2mProject

        project.dependencies { DependencyHandler handler ->
            handler.add("compileOnly", project._p2mApi())
            handler.add("compileOnly", project._p2mAnnotation())
            handler.add("kapt", project._p2mCompiler())
        }

    }
}