package com.p2m.core.internal.module.task

import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskFactory

internal class DefaultTaskFactory : TaskFactory {
    override fun <TASK : Task<*, *>> newInstance(clazz: Class<TASK>): TASK {
        return clazz.newInstance()
    }
}