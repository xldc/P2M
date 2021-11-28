package com.p2m.core.internal.module.task

import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskUnit

internal class TaskOutputProviderImplForTask constructor(private val taskContainer: TaskContainerImpl, private val taskUnit: TaskUnit) : TaskOutputProvider {
    
    @Suppress("UNCHECKED_CAST")
    override fun <OUTPUT> outputOf(clazz: Class<out Task<*, OUTPUT>>): OUTPUT? {

        check(taskUnit.getDependencies().contains(clazz)) {
            "${taskUnit.getOwner().canonicalName} must depend on ${clazz.canonicalName}"
        }
        
        return (taskContainer.find(clazz)?.ownerInstance as? Task<*, OUTPUT>)?.output
    }
}
