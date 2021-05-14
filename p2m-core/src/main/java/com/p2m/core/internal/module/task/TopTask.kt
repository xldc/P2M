package com.p2m.core.internal.module.task

import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.Task


internal object TopTask : Task<Unit, Unit>() {

    val CLAZZ = TopTask::class.java

    override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) { }
}