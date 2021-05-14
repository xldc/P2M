package com.p2m.gradle.utils

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task

class TaskUtils {

    static def whenTaskFound = { Project project, String taskName, Action<Task> action ->
        def task = project.tasks.findByName(taskName)
        if (task != null) {
            action.execute(task)
            return
        }

        project.tasks.whenTaskAdded { t ->
            if (t.name == taskName) {
                action.execute(t)
            }
        }
    }

}
