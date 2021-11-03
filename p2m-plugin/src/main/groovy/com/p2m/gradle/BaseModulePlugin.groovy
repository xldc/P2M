package com.p2m.gradle

import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import com.p2m.gradle.bean.RunAppConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BaseModulePlugin implements Plugin<Project> {
    protected BaseProject baseProject
    protected RunAppConfig runAppConfig
    protected Set<ModuleProject> moduleDependencies

    @Override
    void apply(final Project project) {
        baseProject = project.p2mProject
        runAppConfig = baseProject.runAppConfig
        moduleDependencies = baseProject.dependencies
        doAction(project)
    }

    abstract void doAction(Project project)
}
