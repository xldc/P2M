package com.p2m.example.account.module_init

import android.content.Context
import android.util.Log
import com.p2m.core.module.SafeModuleApiProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.example.account.UserDiskCache

// 读取登录状态的任务，input:String output:Boolean
class LoadLoginStateTask: Task<String, Boolean>() {

    // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
    override fun onExecute(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
        val input = input // input的值是1
        Log.i("LoadLoginStateTask", "onExecute input: $input")

        output = UserDiskCache(context).readLoginState()
    }
} 