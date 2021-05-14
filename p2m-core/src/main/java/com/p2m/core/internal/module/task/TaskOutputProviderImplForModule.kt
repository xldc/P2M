package com.p2m.core.internal.module.task

import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.Task

internal class TaskOutputProviderImplForModule(private val taskContainer: TaskContainerImpl) : TaskOutputProvider {
    
    @Suppress("UNCHECKED_CAST")
    override fun <OUTPUT> getOutputOf(clazz: Class<out Task<*, OUTPUT>>): OUTPUT? {
        return ((taskContainer.find(clazz) as TaskUnitImpl).ownerInstance as Task<*, OUTPUT>).output
    }
}
