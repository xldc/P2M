package com.p2m.core.internal.module.task

import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskUnit

internal class TaskUnitImpl constructor(private val owner: Class<out Task<*, *>>, val input: Any?, val ownerInstance: Task<*, *>) : TaskUnit {
    private val dependencies = hashSetOf<Class<out Task<*, *>>>()

    override fun getOwner(): Class<out Task<*, *>> = owner

    override fun dependOn(taskClass: Class<out Task<*, *>>) {
        dependencies.add(taskClass)
    }
    
    override fun dependOn(vararg taskClass: Class<out Task<*, *>>) {
        dependencies.addAll(taskClass)
    }

    override fun getDependencies(): Set<Class<out Task<*, *>>> = dependencies

}