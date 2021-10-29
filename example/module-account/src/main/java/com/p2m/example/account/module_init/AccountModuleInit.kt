package com.p2m.example.account.module_init

import android.content.Context
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.core.module.*
import com.p2m.module.api.Account
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.TaskUnit
import com.p2m.example.account.UserDiskCache
import com.p2m.module.impl.mutable

@ModuleInitializer
class AccountModuleInit : ModuleInit {

    // 运行在子线程，用于注册模块内的任务，组织任务的依赖关系
    override fun onEvaluate(context: Context, taskRegister: TaskRegister<out TaskUnit>) {
        // 注册读取登录状态的任务
        taskRegister.register(LoadLoginStateTask::class.java, "1")

        // 注册读取登录用户信息的任务
        taskRegister
            .register(LoadLastUserTask::class.java, "2")
            .dependOn(LoadLoginStateTask::class.java) // 执行顺序一定为LoadLoginStateTask.onExecute() > LoadLastUserTask.onExecute()
    }

    // 运行在主线程，当所有的依赖模块开机成功且自身模块的任务执行完毕时调用
    override fun onExecuted(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
        val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java) // 获取登录状态
        val loginInfo = taskOutputProvider.getOutputOf(LoadLastUserTask::class.java)    // 获取用户信息

        val account = moduleApiProvider.moduleApiOf(Account::class.java)        // 找到自身的Api区，在Module init区不能调用P2M.moduleApiOf()
        account.event.mutable().loginState.setValue(loginState ?: false)        // 保存到事件持有者，提供给被依赖的模块使用
        account.event.mutable().loginInfo.setValue(loginInfo)                   // 保存到事件持有者，提供给被依赖的模块使用
    }

}