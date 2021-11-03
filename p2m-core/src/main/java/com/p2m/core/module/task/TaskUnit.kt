package com.p2m.core.module.task

/**
 * The dependence of organizational tasks.
 */
interface TaskUnit {
    
    fun getOwner(): Class<out Task<*, *>>

    /**
     * Add the given dependency to this task.
     *
     * @param taskClass The dependency to add to this task.
     */
    fun dependOn(taskClass: Class<out Task<*, *>>)

    /**
     * Adds the given dependencies to this task.
     *
     * @param taskClass The dependencies to add to this task.
     */
    fun dependOn(vararg taskClass: Class<out Task<*, *>>)


    fun getDependencies(): Set<Class<out Task<*, *>>>
    
}