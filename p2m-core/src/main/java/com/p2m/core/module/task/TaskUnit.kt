package com.p2m.core.module.task

/**
 * The dependence of organizational tasks.
 */
interface TaskUnit {
    
    fun getOwner(): Class<out Task<*, *>>

    /**
     * Add the given dependency to this task.
     *
     * @param taskClazz The dependency to add to this task.
     */
    fun dependOn(taskClazz: Class<out Task<*, *>>)

    /**
     * Adds the given dependencies to this task.
     *
     * @param taskClazz The dependencies to add to this task.
     */
    fun dependOn(vararg taskClazz: Class<out Task<*, *>>)


    fun getDependencies(): Set<Class<out Task<*, *>>>
    
}