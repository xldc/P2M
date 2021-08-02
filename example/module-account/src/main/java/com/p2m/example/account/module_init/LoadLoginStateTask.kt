package com.p2m.example.account.module_init

import android.util.Log
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.example.account.UserDiskCache

// 读取登录状态的任务，input:String output:Boolean
class LoadLoginStateTask: Task<String, Boolean>() {

    // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
    override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
        val input = input // input 1
        Log.i("LoadLoginStateTask", "onExecute input: $input")

        val userDiskCache = UserDiskCache(moduleProvider.context)
        output = userDiskCache.readLoginState()
    }
} 