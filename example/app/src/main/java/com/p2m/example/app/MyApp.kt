package com.p2m.example.app

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Observer
import com.p2m.core.P2M
import com.p2m.core.log.ILogger
import com.p2m.core.log.Level
import com.p2m.module.api.Login

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        P2M.getConfigManager()
            .setLogger(object :ILogger{
                override fun log(level: Level, msg: String, throwable: Throwable?) {
                    Log.e("P2M", msg)
                }
            })

        P2M.driverBuilder(this).build().open()


        P2M.moduleOf(Login::class.java).run {
            // 还需要显示闪屏页，因此监听时需要忽略粘值。
            event.loginState.observeForever(Observer { loginState ->
                if (!loginState) {
                    // 登录失效跳转登录界面
                    launcher.newActivityIntentOfLoginActivity(this@MyApp).run {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                }
            })
        }
    }
}