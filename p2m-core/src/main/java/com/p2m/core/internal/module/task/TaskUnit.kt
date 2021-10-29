package com.p2m.core.internal.module.task

import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskUnit

internal class TaskUnitImpl constructor(private val owner: Class<out Task<*, *>>, val input: Any?, val ownerInstance: Task<*, *>) : TaskUnit {
    private val dependencies = hashSetOf<Class<out Task<*, *>>>()

    override fun getOwner(): Class<out Task<*, *>> = owner

    override fun dependOn(taskClazz: Class<out Task<*, *>>) {
        dependencies.add(taskClazz)
    }
    
    override fun dependOn(vararg taskClazz: Class<out Task<*, *>>) {
        dependencies.addAll(taskClazz)
    }

    override fun getDependencies(): Set<Class<out Task<*, *>>> = dependencies

}