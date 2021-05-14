package com.p2m.example.login

import android.content.Context
import android.util.Log
import com.p2m.core.P2M
import com.p2m.annotation.module.ModuleInitializer
import com.p2m.annotation.module.api.*
import com.p2m.core.module.*
import com.p2m.module.api.Login
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

@ModuleInitializer
class LoginModule : Module {

    // running in work thread
    override fun onEvaluate(taskRegister: TaskRegister) {
        taskRegister.register(LocalLoadLoggedUserTask::class.java, "test input")
    }

    // running in main thread
    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val loginInfo = taskOutputProvider.getOutputOf(LocalLoadLoggedUserTask::class.java)
        val isLogged = loginInfo != null

        val loginModule = moduleProvider.moduleOf(Login::class.java)
        loginModule.event.loginState.setValue(isLogged)
        loginModule.event.loginInfo.setValue(loginInfo)
    }

}

@Service
class LoginService{
    /**
     * 退出登录
     */
    fun logout(){
        P2M.moduleOf(Login::class.java)
            .event
            .apply {
                val sp = P2M.getContext().getSharedPreferences("login_user", Context.MODE_PRIVATE)
                sp.edit().apply { 
                    remove("login_state")
                    remove("login_user")
                    remove("login_id")
                    apply()
                }
                
                // notify
                loginState.setValue(false)
                loginInfo.setValue(null)
            }
    }
}

@Event
interface LoginModuleEvent{
    /**
     * 登录用户信息
     */
    @EventField
    val loginInfo: LoginUserInfo?

    /**
     * 登录状态
     */
    @EventField(eventOn = EventOn.BACKGROUND)
    val loginState: Boolean

    /**
     * 登录成功跳转主界面
     */
    @EventField(eventOn = EventOn.BACKGROUND, eventObservation = EventObservation.NO_STICKY)
    val loginSuccessJumpMain: Boolean
}