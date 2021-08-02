package com.p2m.example.account.module_init

import android.util.Log
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.example.account.pre_api.LoginUserInfo
import com.p2m.example.account.UserDiskCache

// 读取登录用户信息的任务，input:String output:LoginUserInfo
class LoadLastUserTask: Task<String, LoginUserInfo>() {

    // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
    override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java)

        // 查询用户信息
        if (loginState == true) {
            val input = input // input 2
            Log.i("LoadLastUserTask", "onExecute input: $input")

            val userDiskCache = UserDiskCache(moduleProvider.context)
            output = userDiskCache.readLoginUserInfo()
        }
    }
} 