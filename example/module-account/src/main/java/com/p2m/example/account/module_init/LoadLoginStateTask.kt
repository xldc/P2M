package com.p2m.example.account.module_init

import android.content.Context
import android.util.Log
import com.p2m.core.module.SafeModuleApiProvider
import com.p2m.core.module.task.Task
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.example.account.UserDiskCache

// 读取登录状态的任务，input:UserDiskCache output:Boolean
class LoadLoginStateTask: Task<UserDiskCache, Boolean>() {

    // 运行在子线程，当所有的依赖模块完成模块初始化且所有的依赖任务执行完毕时调用
    override fun onExecute(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
        val userDiskCache = input
        output = userDiskCache?.readLoginState()
    }
} 