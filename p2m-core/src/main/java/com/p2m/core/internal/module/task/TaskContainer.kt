package com.p2m.core.internal.module.task

import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskUnit
import com.p2m.core.module.task.TaskRegister

internal interface TaskContainer{
    /**
     * Found a task in the container.
     */
    fun find(clazz: Class<out Task<*, *>>): TaskUnit?

    fun getAll(): Collection<TaskUnit>
}


internal class TaskContainerImpl : TaskRegister, TaskContainer {
    private val container = HashMap<Class<out Task<*, *>>, TaskUnit>()

    val onlyHasTop: Boolean
        get() = container.size == 1
    
    init {
        register(TopTask.CLAZZ)
    }

    override fun registers(vararg clazz: Class<out Task<*, *>>) {
        for (clazz1 in clazz) {
            check(!container.containsKey(clazz1)) {
                "${clazz1.canonicalName} registered already."
            }
            val taskUnit = TaskUnitImpl(clazz1, null, clazz1.newInstance())
            container[clazz1] = taskUnit
        }
    }

    override fun <INPUT> register(clazz: Class<out Task<INPUT, *>>, input: INPUT?): TaskUnit {
        check(!container.containsKey(clazz)) {
            "${clazz.canonicalName} registered already."
        }
        @Suppress("UNCHECKED_CAST")
        val taskUnit = TaskUnitImpl(clazz, input, if (clazz == TopTask.CLAZZ) TopTask as Task<INPUT, *> else clazz.newInstance())
        container[clazz] = taskUnit
        return taskUnit
    }

    override fun find(clazz: Class<out Task<*, *>>): TaskUnit? = container[clazz]

    override fun getAll(): Collection<TaskUnit> = container.values
}