package com.p2m.example.main.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.p2m.core.P2M
import com.p2m.module.api.Login
import com.p2m.module.api.Main

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(View(this))

        // 获取登录状态
        val loginState = P2M.moduleOf(Login::class.java).event.loginState.getValue()
        if (loginState == true) {
            // 登录成功
            P2M.moduleOf(Main::class.java)
                .launcher
                .newActivityIntentOfMainActivity(this).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(this)
                }
            finish()
        }else{
            // 登录失败跳转到登录页
            P2M.moduleOf(Login::class.java).launcher.newActivityIntentOfLoginActivity(this)
                .run {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(this)
                }
            finish()
        }

    }
}