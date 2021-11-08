package com.p2m.core.module.task


interface TaskOutputProvider {
    /**
     * Get output for a task registered already.
     *
     * @param clazz find instance of task by the param.
     */
    fun <OUTPUT> outputOf(clazz: Class<out Task<*, OUTPUT>>): OUTPUT?
}
