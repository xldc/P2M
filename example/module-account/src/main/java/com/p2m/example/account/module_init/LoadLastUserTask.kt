package com.p2m.example.account.module_init

import android.content.Context
import android.util.Log
import com.p2m.core.module.SafeModuleApiProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.example.account.pre_api.LoginUserInfo
import com.p2m.example.account.UserDiskCache

// 读取登录用户信息的任务，input:String output:LoginUserInfo
class LoadLastUserTask: Task<String, LoginUserInfo>() {

    // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
    override fun onExecute(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
        val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java)

        // 查询用户信息
        if (loginState == true) {
            val input = input // input的值是2
            Log.i("LoadLastUserTask", "onExecute input: $input")

            output = UserDiskCache(context).readLoginUserInfo()
        }
    }
} 