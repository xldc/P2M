package com.p2m.example.main

import android.content.Intent
import androidx.lifecycle.Observer
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.core.module.*
import com.p2m.module.api.Login
import com.p2m.module.api.Main
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

@ModuleInitializer
class MainModule:Module{

    override fun onEvaluate(taskRegister: TaskRegister) {
        
    }

    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val login = moduleProvider.moduleOf(Login::class.java)
        
        // 登录成功跳转主页
        login.event.loginSuccessJumpMain.observeForever(Observer { state ->
            if (state) {
                // 登录成功启动主界面
                moduleProvider.moduleOf(Main::class.java)
                    .launcher
                    .newActivityIntentOfMainActivity(moduleProvider.context)
                    .run {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        moduleProvider.context.startActivity(this)
                    }
                
            } else {
                // 退出登录
            }
        })
    }

}