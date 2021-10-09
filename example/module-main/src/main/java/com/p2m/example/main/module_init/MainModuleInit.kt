package com.p2m.example.main.module_init

import android.content.Intent
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.core.event.BackgroundObserver
import com.p2m.core.event.ObserveOn
import com.p2m.core.module.*
import com.p2m.module.api.Account
import com.p2m.module.api.Main
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

@ModuleInitializer
class MainModuleInit : ModuleInit {

    // 运行在子线程，用于注册模块内的任务，组织任务的依赖关系
    override fun onEvaluate(taskRegister: TaskRegister) {
        
    }

    // 运行在主线程，当所有的依赖模块开机成功且自身模块的任务执行完毕时调用
    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val account = moduleProvider.moduleApiOf(Account::class.java)
        
        // 登录成功跳转主页
        account.event.loginSuccess.observeForeverNoSticky(object: BackgroundObserver<Unit>(ObserveOn.BACKGROUND) {
            override fun onChanged(t: Unit) {
                // 登录成功启动主界面
                moduleProvider.moduleApiOf(Main::class.java)
                    .launcher
                    .newActivityIntentOfMainActivity(moduleProvider.context)
                    .run {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        moduleProvider.context.startActivity(this)
                    }
            }

        })
    }

}