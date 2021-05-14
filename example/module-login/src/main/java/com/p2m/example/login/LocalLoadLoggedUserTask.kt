package com.p2m.example.login

import android.content.Context
import android.util.Log
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider

// input:String output:LoggedInUser
class LocalLoadLoggedUserTask: Task<String, LoginUserInfo>() {
    
    // running in work thread
    override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val arg = input // test input
        Log.e("LocalLoadLoggedUserTask", "onExecute input: $arg")

        val sp = moduleProvider.context.getSharedPreferences("login_user", Context.MODE_PRIVATE)
        val loginState = sp.getBoolean("login_state", false)
        if (loginState) {
            val userName = sp.getString("login_user", null) ?: ""
            val userId = sp.getString("login_id", null) ?: ""
            // set output
            output = LoginUserInfo().apply { 
                this.userId = userId
                this.userName = userName
            }
        }
    }
} 