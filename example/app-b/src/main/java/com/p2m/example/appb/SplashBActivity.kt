package com.p2m.example.appb

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.p2m.core.P2M
import com.p2m.module.api.Account
import com.p2m.module.api.Main


class SplashBActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        findViewById<View>(R.id.fullscreen_content).postDelayed( {
            // 获取登录状态
            val loginState = P2M.apiOf(Account::class.java).event.loginState.getValue()
            if (loginState == true) {
                // 登录过
                P2M.apiOf(Main::class.java)
                    .launcher
                    .activityOfMain
                    .createIntent(this)
                    .run {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                finish()
            }else{
                // 未登录
                P2M.apiOf(Account::class.java)
                    .launcher
                    .activityOfLogin
                    .createIntent(this)
                    .run {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                finish()
            }

        },2000L)
    }

}