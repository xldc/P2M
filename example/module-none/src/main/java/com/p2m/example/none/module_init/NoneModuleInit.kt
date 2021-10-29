package com.p2m.example.none.module_init

import android.content.Context
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.core.module.*
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.TaskUnit

@ModuleInitializer
class NoneModuleInit : ModuleInit {

    override fun onEvaluate(context: Context, taskRegister: TaskRegister<out TaskUnit>) { }

    override fun onExecuted(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) { }

}