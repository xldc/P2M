package com.p2m.core.internal.module

import android.content.Context
import com.p2m.core.internal.graph.Node
import com.p2m.core.internal.module.task.TaskContainerImpl
import com.p2m.core.internal.module.task.TopTask
import com.p2m.core.module.Module
import com.p2m.core.internal.module.SafeModuleApiProvider
import com.p2m.core.module.task.TaskFactory

internal class ModuleNode constructor(
    val context: Context,
    val module: Module<*>,
    override val isTop: Boolean,
    taskFactory: TaskFactory
) : Node<ModuleNode>() {

    override val name: String
        get() = module.internalModuleUnit.moduleName
    val safeModuleApiProvider: SafeModuleApiProvider = SafeModuleApiProviderImpl(dependNodes, module)
    val taskContainer = TaskContainerImpl(TopTask::class.java, taskFactory)

    fun markSelfAvailable(){
        safeModuleApiProvider.selfAvailable = true
    }
}