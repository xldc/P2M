package com.p2m.example.account.module_init

import android.content.Intent
import androidx.lifecycle.Observer
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.core.module.*
import com.p2m.module.api.Account
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

@ModuleInitializer
class AccountModuleInit : ModuleInit {

    // 运行在子线程，用于注册模块内的任务，组织任务的依赖关系
    override fun onEvaluate(taskRegister: TaskRegister) {
        // 注册读取登录状态的任务
        taskRegister.register(LoadLoginStateTask::class.java, "input 1")

        // 注册读取登录用户信息的任务
        taskRegister
            .register(LoadLastUserTask::class.java, "input 2")
            .dependOn(LoadLoginStateTask::class.java) // 执行顺序一定为LoadLoginStateTask.onExecute() > LoadLastUserTask.onExecute()
    }

    // 运行在主线程，当所有的依赖模块开机成功且自身模块的任务执行完毕时调用
    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java) // 获取登录状态
        val loginInfo = taskOutputProvider.getOutputOf(LoadLastUserTask::class.java)    // 获取用户信息

        val account = moduleProvider.moduleApiOf(Account::class.java)  // 找到自身的Api区，在Module init区不能调用P2M.moduleApiOf()
        account.event.loginState.setValue(loginState ?: false)      // 保存到事件持有者，提供给被依赖的模块使用
        account.event.loginInfo.setValue(loginInfo)                 // 保存到事件持有者，提供给被依赖的模块使用

        // 一般APP先显示闪屏页，因此监听时需要忽略粘值。
        account.event.loginState.observeForeverNoSticky(Observer { loginState ->
            if (!loginState) {
                // 登录失效跳转登录界面
                account.launcher.newActivityIntentOfLoginActivity(moduleProvider.context).run {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    moduleProvider.context.startActivity(this)
                }
            }
        })
    }

}