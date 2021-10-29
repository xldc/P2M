package com.p2m.core.module.task

interface TaskFactory {
    fun <TASK : Task<*, *>> newInstance(clazz: Class<TASK>): TASK
}