package com.p2m.example.login

import android.app.Application
import android.util.Log
import com.p2m.core.P2M
import com.p2m.core.log.ILogger
import com.p2m.core.log.Level
class LoginApp : Application() {
    override fun onCreate() {
        super.onCreate()
        P2M.getConfigManager().setLogger(object : ILogger {
                override fun log(level: Level, msg: String, throwable: Throwable?) {
                    Log.e("P2M", msg)
                }
            })
        P2M.driverBuilder(this)
            .build()
            .open()
    }
}